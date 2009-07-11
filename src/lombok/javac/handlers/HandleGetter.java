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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Handles the <code>lombok.Getter</code> annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter implements JavacAnnotationHandler<Getter> {
	/**
	 * Generates a getter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a <code>lombok.Getter</code> annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the getter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateGetterForField(Node fieldNode, DiagnosticPosition pos) {
		AccessLevel level = AccessLevel.PUBLIC;
		Node errorNode = fieldNode;
		boolean whineIfExists = false;
		
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Javac.annotationTypeMatches(Getter.class, child) ) {
					level = Javac.createAnnotation(Getter.class, child).getInstance().value();
					errorNode = child;
					pos = child.get();
					whineIfExists = true;
					break;
				}
			}
		}
		
		createGetterForField(level, fieldNode, errorNode, pos, whineIfExists);
	}
	
	@Override public boolean handle(AnnotationValues<Getter> annotation, JCAnnotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		return createGetterForField(level, fieldNode, annotationNode, annotationNode.get(), true);
	}
	
	private boolean createGetterForField(AccessLevel level, Node fieldNode, Node errorNode, DiagnosticPosition pos, boolean whineIfExists) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Getter is only supported on a field.");
			return true;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		String methodName = toGetterName(fieldDecl);
		
		switch ( methodExists(methodName, fieldNode) ) {
		case EXISTS_BY_LOMBOK:
			return true;
		case EXISTS_BY_USER:
			if ( whineIfExists ) errorNode.addWarning(
					String.format("Not generating %s(): A method with that name already exists",  methodName));
			return true;
		default:
		case NOT_EXISTS:
			//continue with creating the getter
		}
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		injectMethod(fieldNode.up(), createGetter(access, fieldNode, fieldNode.getTreeMaker()));
		
		return true;
	}
	
	private JCMethodDecl createGetter(long access, Node field, TreeMaker treeMaker) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident(fieldNode.getName()));
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = field.toName(toGetterName(fieldNode));
		JCExpression methodType = fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
