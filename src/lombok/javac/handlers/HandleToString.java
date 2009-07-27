/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;

import lombok.ToString;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the <code>ToString</code> annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleToString implements JavacAnnotationHandler<ToString> {
	private void checkForBogusExcludes(Node type, AnnotationValues<ToString> annotation) {
		List<String> list = List.from(annotation.getInstance().exclude());
		boolean[] matched = new boolean[list.size()];
		
		for ( Node child : type.down() ) {
			if ( list.isEmpty() ) break;
			if ( child.getKind() != Kind.FIELD ) continue;
			if ( (((JCVariableDecl)child.get()).mods.flags & Flags.STATIC) != 0 ) continue;
			int idx = list.indexOf(child.getName());
			if ( idx > -1 ) matched[idx] = true;
		}
		
		for ( int i = 0 ; i < list.size() ; i++ ) {
			if ( !matched[i] ) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
	}
	
	@Override public boolean handle(AnnotationValues<ToString> annotation, JCAnnotation ast, Node annotationNode) {
		ToString ann = annotation.getInstance();
		List<String> excludes = List.from(ann.exclude());
		Node typeNode = annotationNode.up();
		
		checkForBogusExcludes(typeNode, annotation);
		
		return generateToString(typeNode, annotationNode, excludes, ann.includeFieldNames(), ann.callSuper(), true);
	}
	
	public void generateToStringForType(Node typeNode, Node errorNode) {
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Javac.annotationTypeMatches(ToString.class, child) ) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		boolean includeFieldNames = false;
		boolean callSuper = false;
		try {
			includeFieldNames = ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch ( Exception ignore ) {}
		try {
			callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
		} catch ( Exception ignore ) {}
		generateToString(typeNode, errorNode, List.<String>nil(),includeFieldNames, callSuper, false);
	}
	
	private boolean generateToString(Node typeNode, Node errorNode, List<String> excludes, 
			boolean includeFieldNames, boolean callSuper, boolean whineIfExists) {
		boolean notAClass = true;
		if ( typeNode.get() instanceof JCClassDecl ) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		}
		
		if ( notAClass ) {
			errorNode.addError("@ToString is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForToString = List.nil();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			//Skip static fields.
			if ( (fieldDecl.mods.flags & Flags.STATIC) != 0 ) continue;
			//Skip excluded fields
			if ( excludes.contains(fieldDecl.name.toString()) ) continue;
			nodesForToString = nodesForToString.append(child);
		}
		
		switch ( methodExists("toString", typeNode) ) {
		case NOT_EXISTS:
			JCMethodDecl method = createToString(typeNode, nodesForToString, includeFieldNames, callSuper);
			injectMethod(typeNode, method);
			return true;
		case EXISTS_BY_LOMBOK:
			return true;
		default:
		case EXISTS_BY_USER:
			if ( whineIfExists ) {
				errorNode.addWarning("Not generating toString(): A method with that name already exists");
			}
			return true;
		}

	}
	
	private JCMethodDecl createToString(Node typeNode, List<Node> fields, boolean includeFieldNames, boolean callSuper) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = chainDots(maker, typeNode, "java", "lang", "String");
		
		JCExpression current = maker.Literal(((JCClassDecl) typeNode.get()).name.toString() + "(");
		boolean first = true;
		
		if ( callSuper ) {
			current = maker.Binary(JCTree.PLUS, current, maker.Literal("super=["));
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("toString")),
					List.<JCExpression>nil());
			current = maker.Binary(JCTree.PLUS, current, callToSuper);
			current = maker.Binary(JCTree.PLUS, current, maker.Literal("]"));
			first = false;
		}
		
		for ( Node fieldNode : fields ) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			if ( !first ) current = maker.Binary(JCTree.PLUS, current, maker.Literal(", "));
			first = false;
			if ( includeFieldNames ) {
				current = maker.Binary(JCTree.PLUS, current, maker.Literal(fieldNode.getName() + "="));
			}
			if ( field.vartype instanceof JCArrayTypeTree ) {
				boolean multiDim = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepTS = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepTS ? "deepToString" : "toString");
				current = maker.Binary(JCTree.PLUS, current, maker.Apply(
						List.<JCExpression>nil(), hcMethod, List.<JCExpression>of(maker.Ident(field.name))));
			} else current = maker.Binary(JCTree.PLUS, current, maker.Ident(field.name));
		}
		
		current = maker.Binary(JCTree.PLUS, current, maker.Literal(")"));
		
		JCStatement returnStatement = maker.Return(current);
		
		JCBlock body = maker.Block(0, List.of(returnStatement));
		
		return maker.MethodDef(mods, typeNode.toName("toString"), returnType,
				List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
}
