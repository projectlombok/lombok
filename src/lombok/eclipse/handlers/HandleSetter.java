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
import lombok.Setter;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the <code>lombok.Setter</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSetter implements EclipseAnnotationHandler<Setter> {
	/**
	 * Generates a setter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a <code>lombok.Setter</code> annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the setter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateSetterForField(Node fieldNode, ASTNode pos) {
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(Setter.class, child) ) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createSetterForField(AccessLevel.PUBLIC, fieldNode, fieldNode, pos, false);
	}
	
	public boolean handle(AnnotationValues<Setter> annotation, Annotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		if ( fieldNode.getKind() != Kind.FIELD ) return false;
		AccessLevel level = annotation.getInstance().value();
		return createSetterForField(level, fieldNode, annotationNode, annotationNode.get(), true);
	}
	
	private boolean createSetterForField(AccessLevel level, Node fieldNode, Node errorNode, ASTNode pos, boolean whineIfExists) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Setter is only supported on a field.");
			return true;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		String setterName = TransformationsUtil.toSetterName(new String(field.name));
		
		int modifier = toModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		switch ( methodExists(setterName, fieldNode) ) {
		case EXISTS_BY_LOMBOK:
			return true;
		case EXISTS_BY_USER:
			if ( whineIfExists ) errorNode.addWarning(
					String.format("Not generating %s(%s %s): A method with that name already exists",
					setterName, field.type, new String(field.name)));
			return true;
		default:
		case NOT_EXISTS:
			//continue with creating the setter
		}
		
		
		MethodDeclaration method = generateSetter((TypeDeclaration) fieldNode.up().get(), field, setterName, modifier, pos);
		
		injectMethod(fieldNode.up(), method);
		
		return true;
	}
	
	private MethodDeclaration generateSetter(TypeDeclaration parent, FieldDeclaration field, String name,
			int modifier, ASTNode ast) {
		
		long pos = (((long)ast.sourceStart) << 32) | ast.sourceEnd;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = modifier;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.annotations = null;
		Argument param = new Argument(field.name, pos, Eclipse.copyType(field.type), 0);
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = parent.scope == null ? null : new MethodScope(parent.scope, method, false);
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		FieldReference thisX = new FieldReference(field.name, pos);
		thisX.receiver = new ThisReference(ast.sourceStart, ast.sourceEnd);
		Assignment assignment = new Assignment(thisX, new SingleNameReference(field.name, pos), (int)pos);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = ast.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = ast.sourceEnd;
		
		Annotation nonNull = findNonNullannotation(field);
		if (nonNull == null) {
			method.statements = new Statement[] { assignment };
		}
		else {
			AllocationExpression exception = new AllocationExpression();
			exception.type = new QualifiedTypeReference(Eclipse.fromQualifiedName("java.lang.NullPointerException"), new long[]{0, 0, 0});
			exception.arguments = new Expression[] { new StringLiteral(field.name, 0, field.name.length - 1, 0)};
			ThrowStatement throwStatement = new ThrowStatement(exception, 0, 0);
			
			IfStatement nullCheck = new IfStatement(new EqualExpression(new SingleNameReference(field.name, 0),
					new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL), throwStatement, 0, 0);
			
			method.statements = new Statement[] { nullCheck, assignment };
		}
		return method;
	}
}
