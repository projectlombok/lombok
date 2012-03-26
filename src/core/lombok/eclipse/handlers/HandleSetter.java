/*
 * Copyright (C) 2009-2011 The Project Lombok Authors.
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.FieldAccess;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Setter} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSetter extends EclipseAnnotationHandler<Setter> {
	public boolean generateSetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelSetter) {
		if (checkForTypeLevelSetter) {
			if (typeNode != null) for (EclipseNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(Setter.class, child)) {
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
			pos.addError("@Setter is only supported on a class or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
			if (!filterField(fieldDecl)) continue;
			
			//Skip final fields.
			if ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0) continue;
			
			generateSetterForField(field, pos.get(), level);
		}
		return true;
	}
	
	/**
	 * Generates a setter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.Setter} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the setter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateSetterForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level) {
		for (EclipseNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(Setter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createSetterForField(level, fieldNode, fieldNode, pos, false);
	}
	
	public void handle(AnnotationValues<Setter> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		if (level == AccessLevel.NONE || node == null) return;
		
		switch (node.getKind()) {
		case FIELD:
			createSetterForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true);
			break;
		case TYPE:
			generateSetterForType(node, annotationNode, level, false);
			break;
		}
	}
	
	private void createSetterForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		for (EclipseNode fieldNode : fieldNodes) {
			createSetterForField(level, fieldNode, errorNode, source, whineIfExists);
		}
	}
	
	private void createSetterForField(AccessLevel level,
			EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@Setter is only supported on a class or a field.");
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = copyType(field.type, source);
		boolean isBoolean = nameEquals(fieldType.getTypeName(), "boolean") && fieldType.dimensions() == 0;
		String setterName = toSetterName(fieldNode, isBoolean);
		boolean shouldReturnThis = shouldReturnThis(fieldNode);
		if (setterName == null) {
			errorNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for (String altName : toAllSetterNames(fieldNode, isBoolean)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(setterName)) altNameExpl = String.format(" (%s)", altName);
					errorNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", setterName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		MethodDeclaration method = generateSetter((TypeDeclaration) fieldNode.up().get(), fieldNode, setterName, shouldReturnThis, modifier, source);
		injectMethod(fieldNode.up(), method);
	}
	
	private MethodDeclaration generateSetter(TypeDeclaration parent, EclipseNode fieldNode, String name, boolean shouldReturnThis, int modifier, ASTNode source) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		setGeneratedBy(method, source);
		method.modifiers = modifier;
		if (shouldReturnThis) {
			EclipseNode type = fieldNode;
			while (type != null && type.getKind() != Kind.TYPE) type = type.up();
			if (type != null && type.get() instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) type.get();
				if (typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0) {
					TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
					int idx = 0;
					for (TypeParameter param : typeDecl.typeParameters) {
						TypeReference typeRef = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
						setGeneratedBy(typeRef, source);
						refs[idx++] = typeRef;
					}
					method.returnType = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
				} else method.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
			}
		}
		
		if (method.returnType == null) {
			method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
			method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
			shouldReturnThis = false;
		}
		setGeneratedBy(method.returnType, source);
		if (isFieldDeprecated(fieldNode)) {
			method.annotations = new Annotation[] { generateDeprecatedAnnotation(source) };
		}
		Argument param = new Argument(field.name, p, copyType(field.type, source), Modifier.FINAL);
		param.sourceStart = pS; param.sourceEnd = pE;
		setGeneratedBy(param, source);
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
		NameReference fieldNameRef = new SingleNameReference(field.name, p);
		setGeneratedBy(fieldNameRef, source);
		Assignment assignment = new Assignment(fieldRef, fieldNameRef, (int)p);
		assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
		setGeneratedBy(assignment, source);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		
		Annotation[] nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
		Annotation[] nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
		List<Statement> statements = new ArrayList<Statement>(5);
		if (nonNulls.length == 0) {
			statements.add(assignment);
		} else {
			Statement nullCheck = generateNullCheck(field, source);
			if (nullCheck != null) statements.add(nullCheck);
			statements.add(assignment);
		}
		
		if (shouldReturnThis) {
			ThisReference thisRef = new ThisReference(pS, pE);
			setGeneratedBy(thisRef, source);
			ReturnStatement returnThis = new ReturnStatement(thisRef, pS, pE);
			setGeneratedBy(returnThis, source);
			statements.add(returnThis);
		}
		method.statements = statements.toArray(new Statement[0]);
		
		Annotation[] copiedAnnotations = copyAnnotations(source, nonNulls, nullables);
		if (copiedAnnotations.length != 0) param.annotations = copiedAnnotations;
		return method;
	}
}
