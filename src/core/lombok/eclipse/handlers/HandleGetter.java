/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Getter} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleGetter implements EclipseAnnotationHandler<Getter> {
	public boolean generateGetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelGetter) {
		if (checkForTypeLevelGetter) {
			if (typeNode != null) for (EclipseNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(Getter.class, child)) {
						//The annotation will make it happen, so we can skip it.
						return true;
					}
				}
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			pos.addError("@Getter is only supported on a class or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForGetterGeneration(field)) generateGetterForField(field, pos.get(), level, null);
		}
		return true;
	}
	
	public boolean fieldQualifiesForGetterGeneration(EclipseNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		//Skip fields that start with $
		if (fieldDecl.name.length > 0 && fieldDecl.name[0] == '$') return false;
		//Skip static fields.
		if ((fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0) return false;
		return true;
	}
	
	/**
	 * Generates a getter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.Getter} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the getter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateGetterForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level, Annotation[] onMethod) {
		for (EclipseNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(Getter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createGetterForField(level, fieldNode, fieldNode, pos, false, onMethod);
	}
	
	public boolean handle(AnnotationValues<Getter> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		if (level == AccessLevel.NONE) return true;
		
		if (node == null) return false;
		
		Annotation[] onMethod = getAndRemoveAnnotationParameter(ast, "onMethod");
		if (node.getKind() == Kind.FIELD) {
			return createGetterForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true, onMethod);
		}
		if (node.getKind() == Kind.TYPE) {
			if (onMethod != null && onMethod.length != 0) annotationNode.addError("'onMethod' is not supported for @Getter on a type.");
			return generateGetterForType(node, annotationNode, level, false);
		}
		return false;
	}
	
	private boolean createGetterForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists, Annotation[] onMethod) {
		for (EclipseNode fieldNode : fieldNodes) {
			createGetterForField(level, fieldNode, errorNode, source, whineIfExists, onMethod);
		}
		return true;
	}
	
	private boolean createGetterForField(AccessLevel level,
			EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists, Annotation[] onMethod) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@Getter is only supported on a class or a field.");
			return true;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = copyType(field.type, source);
		String fieldName = new String(field.name);
		boolean isBoolean = nameEquals(fieldType.getTypeName(), "boolean") && fieldType.dimensions() == 0;
		String getterName = TransformationsUtil.toGetterName(fieldName, isBoolean);
		
		int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for (String altName : TransformationsUtil.toAllGetterNames(fieldName, isBoolean)) {
			switch (methodExists(altName, fieldNode, false)) {
			case EXISTS_BY_LOMBOK:
				return true;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(getterName)) altNameExpl = String.format(" (%s)", altName);
					errorNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", getterName, altNameExpl));
				}
				return true;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		MethodDeclaration method = generateGetter((TypeDeclaration) fieldNode.up().get(), fieldNode, getterName, modifier, source);
		Annotation[] copiedAnnotations = copyAnnotations(source, findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN), findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN), onMethod);
		if (copiedAnnotations.length != 0) {
			method.annotations = copiedAnnotations;
		}
		
		injectMethod(fieldNode.up(), method);
		
		return true;
	}
	
	private MethodDeclaration generateGetter(TypeDeclaration parent, EclipseNode fieldNode, String name, int modifier, ASTNode source) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = copyType(field.type, source);
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		Expression fieldRef = createFieldAccessor(fieldNode, true, source);
		Statement returnStatement = new ReturnStatement(fieldRef, field.sourceStart, field.sourceEnd);
		Eclipse.setGeneratedBy(returnStatement, source);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
}
