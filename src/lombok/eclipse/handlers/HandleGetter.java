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
package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the <code>lombok.Getter</code> annotation for javac.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleGetter implements EclipseAnnotationHandler<Getter> {
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
	public void generateGetterForField(Node fieldNode, ASTNode pos) {
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(Getter.class, child) ) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createGetterForField(AccessLevel.PUBLIC, fieldNode, fieldNode, pos, false);
	}
	
	public boolean handle(AnnotationValues<Getter> annotation, Annotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		return createGetterForField(level, fieldNode, annotationNode, annotationNode.get(), true);
	}
	
	private boolean createGetterForField(AccessLevel level, Node fieldNode, Node errorNode, ASTNode pos, boolean whineIfExists) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Getter is only supported on a field.");
			return true;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = Eclipse.copyType(field.type);
		String fieldName = new String(field.name);
		boolean isBoolean = nameEquals(fieldType.getTypeName(), "boolean") && fieldType.dimensions() == 0;
		String getterName = TransformationsUtil.toGetterName(fieldName, isBoolean);
		
		int modifier = toModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for ( String altName : TransformationsUtil.toAllGetterNames(fieldName, isBoolean) ) {
			switch ( methodExists(altName, fieldNode) ) {
			case EXISTS_BY_LOMBOK:
				return true;
			case EXISTS_BY_USER:
				if ( whineIfExists ) {
					String altNameExpl = "";
					if ( !altName.equals(getterName) ) altNameExpl = String.format(" (%s)", altName);
					errorNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s",  getterName, altNameExpl));
				}
				return true;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		MethodDeclaration method = generateGetter((TypeDeclaration) fieldNode.up().get(), field, getterName, modifier, pos);
		
		injectMethod(fieldNode.up(), method);
		
		return true;
	}
	
	private MethodDeclaration generateGetter(TypeDeclaration parent, FieldDeclaration field, String name,
			int modifier, ASTNode pos) {
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = modifier;
		method.returnType = Eclipse.copyType(field.type);
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		Expression fieldExpression = new SingleNameReference(field.name, (field.declarationSourceStart << 32) | field.declarationSourceEnd);
		Statement returnStatement = new ReturnStatement(fieldExpression, field.sourceStart, field.sourceEnd);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
}
