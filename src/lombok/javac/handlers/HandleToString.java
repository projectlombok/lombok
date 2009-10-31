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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import lombok.ToString;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

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
 * Handles the {@code ToString} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleToString implements JavacAnnotationHandler<ToString> {
	private void checkForBogusFieldNames(JavacNode type, AnnotationValues<ToString> annotation) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().exclude()), type, true, false)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().of()), type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	@Override public boolean handle(AnnotationValues<ToString> annotation, JCAnnotation ast, JavacNode annotationNode) {
		ToString ann = annotation.getInstance();
		List<String> excludes = List.from(ann.exclude());
		List<String> includes = List.from(ann.of());
		JavacNode typeNode = annotationNode.up();
		
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		return generateToString(typeNode, annotationNode, excludes, includes, ann.includeFieldNames(), callSuper, true);
	}
	
	public void generateToStringForType(JavacNode typeNode, JavacNode errorNode) {
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (Javac.annotationTypeMatches(ToString.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		boolean includeFieldNames = true;
		try {
			includeFieldNames = ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}
		generateToString(typeNode, errorNode, null, null, includeFieldNames, null, false);
	}
	
	private boolean generateToString(JavacNode typeNode, JavacNode errorNode, List<String> excludes, List<String> includes,
			boolean includeFieldNames, Boolean callSuper, boolean whineIfExists) {
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		}
		
		if (callSuper == null) {
			try {
				callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}
		
		if (notAClass) {
			errorNode.addError("@ToString is only supported on a class.");
			return false;
		}
		
		List<JavacNode> nodesForToString = List.nil();
		if (includes != null) {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				if (includes.contains(fieldDecl.name.toString())) nodesForToString = nodesForToString.append(child);
			}
		} else {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				//Skip static fields.
				if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
				//Skip excluded fields.
				if (excludes != null && excludes.contains(fieldDecl.name.toString())) continue;
				//Skip fields that start with $.
				if (fieldDecl.name.toString().startsWith("$")) continue;
				nodesForToString = nodesForToString.append(child);
			}
		}
		
		switch (methodExists("toString", typeNode)) {
		case NOT_EXISTS:
			JCMethodDecl method = createToString(typeNode, nodesForToString, includeFieldNames, callSuper);
			injectMethod(typeNode, method);
			return true;
		case EXISTS_BY_LOMBOK:
			return true;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating toString(): A method with that name already exists");
			}
			return true;
		}

	}
	
	private JCMethodDecl createToString(JavacNode typeNode, List<JavacNode> fields, boolean includeFieldNames, boolean callSuper) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = chainDots(maker, typeNode, "java", "lang", "String");
		
		boolean first = true;
		
		String typeName = ((JCClassDecl) typeNode.get()).name.toString();
		String infix = ", ";
		String suffix = ")";
		String prefix;
		if (callSuper) {
			prefix = typeName + "(super=";
		} else if (fields.isEmpty()) {
			prefix = typeName + "()";
		} else if (includeFieldNames) {
			prefix = typeName + "(" + ((JCVariableDecl)fields.iterator().next().get()).name.toString() + "=";
		} else {
			prefix = typeName + "(";
		}
		
		JCExpression current = maker.Literal(prefix);
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("toString")),
					List.<JCExpression>nil());
			current = maker.Binary(JCTree.PLUS, current, callToSuper);
			first = false;
		}
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression expr;
			
			if (field.vartype instanceof JCArrayTypeTree) {
				boolean multiDim = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepTS = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepTS ? "deepToString" : "toString");
				expr = maker.Apply(List.<JCExpression>nil(), hcMethod, List.<JCExpression>of(maker.Ident(field.name)));
			} else expr = maker.Ident(field.name);
			
			if (first) {
				current = maker.Binary(JCTree.PLUS, current, expr);
				first = false;
				continue;
			}
			
			if (includeFieldNames) {
				current = maker.Binary(JCTree.PLUS, current, maker.Literal(infix + fieldNode.getName() + "="));
			} else {
				current = maker.Binary(JCTree.PLUS, current, maker.Literal(infix));
			}
			
			current = maker.Binary(JCTree.PLUS, current, expr);
		}
		
		if (!first) current = maker.Binary(JCTree.PLUS, current, maker.Literal(suffix));
		
		JCStatement returnStatement = maker.Return(current);
		
		JCBlock body = maker.Block(0, List.of(returnStatement));
		
		return maker.MethodDef(mods, typeNode.toName("toString"), returnType,
				List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
}
