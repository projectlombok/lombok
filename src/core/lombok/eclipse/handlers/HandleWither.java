/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Wither;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleWither extends EclipseAnnotationHandler<Wither> {
	public boolean generateWitherForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelWither) {
		if (checkForTypeLevelWither) {
			if (hasAnnotation(Wither.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			pos.addError("@Wither is only supported on a class or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
			if (!filterField(fieldDecl)) continue;
			
			//Skip final fields.
			if ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0 && fieldDecl.initialization != null) continue;
			
			generateWitherForField(field, pos, level);
		}
		return true;
	}
	
	
	/**
	 * Generates a wither on the stated field.
	 * 
	 * Used by {@link HandleValue}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.experimental.Wither} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the wither is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateWitherForField(EclipseNode fieldNode, EclipseNode sourceNode, AccessLevel level) {
		for (EclipseNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(Wither.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		List<Annotation> empty = Collections.emptyList();
		createWitherForField(level, fieldNode, sourceNode, false, empty, empty);
	}
	
	@Override public void handle(AnnotationValues<Wither> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.WITHER_FLAG_USAGE, "@Wither");
		
		EclipseNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		if (level == AccessLevel.NONE || node == null) return;
		
		List<Annotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Wither(onMethod", annotationNode);
		List<Annotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@Wither(onParam", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createWitherForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, true, onMethod, onParam);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) {
				annotationNode.addError("'onMethod' is not supported for @Wither on a type.");
			}
			if (!onParam.isEmpty()) {
				annotationNode.addError("'onParam' is not supported for @Wither on a type.");
			}
			generateWitherForType(node, annotationNode, level, false);
			break;
		}
	}
	
	public void createWitherForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode sourceNode, boolean whineIfExists, List<Annotation> onMethod, List<Annotation> onParam) {
		for (EclipseNode fieldNode : fieldNodes) {
			createWitherForField(level, fieldNode, sourceNode, whineIfExists, onMethod, onParam);
		}
	}
	
	public void createWitherForField(
			AccessLevel level, EclipseNode fieldNode, EclipseNode sourceNode,
			boolean whineIfExists, List<Annotation> onMethod,
			List<Annotation> onParam) {
		
		ASTNode source = sourceNode.get();
		if (fieldNode.getKind() != Kind.FIELD) {
			sourceNode.addError("@Wither is only supported on a class or a field.");
			return;
		}
		
		EclipseNode typeNode = fieldNode.up();
		boolean makeAbstract = typeNode != null && typeNode.getKind() == Kind.TYPE && (((TypeDeclaration) typeNode.get()).modifiers & ClassFileConstants.AccAbstract) != 0;
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = copyType(field.type, source);
		boolean isBoolean = isBoolean(fieldType);
		String witherName = toWitherName(fieldNode, isBoolean);
		
		if (witherName == null) {
			fieldNode.addWarning("Not generating wither for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((field.modifiers & ClassFileConstants.AccStatic) != 0) {
			fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for static fields.");
			return;
		}
		
		if ((field.modifiers & ClassFileConstants.AccFinal) != 0 && field.initialization != null) {
			fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for final, initialized fields.");
			return;
		}
		
		if (field.name != null && field.name.length > 0 && field.name[0] == '$') {
			fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for fields starting with $.");
			return;
		}
		
		for (String altName : toAllWitherNames(fieldNode, isBoolean)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(witherName)) altNameExpl = String.format(" (%s)", altName);
					fieldNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", witherName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		int modifier = toEclipseModifier(level);
		
		MethodDeclaration method = createWither((TypeDeclaration) fieldNode.up().get(), fieldNode, witherName, modifier, sourceNode, onMethod, onParam, makeAbstract);
		injectMethod(fieldNode.up(), method);
	}
	
	public MethodDeclaration createWither(TypeDeclaration parent, EclipseNode fieldNode, String name, int modifier, EclipseNode sourceNode, List<Annotation> onMethod, List<Annotation> onParam, boolean makeAbstract ) {
		ASTNode source = sourceNode.get();
		if (name == null) return null;
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		if (makeAbstract) modifier = modifier | ClassFileConstants.AccAbstract | ExtraCompilerModifiers.AccSemicolonBody;
		method.modifiers = modifier;
		method.returnType = cloneSelfType(fieldNode, source);
		if (method.returnType == null) return null;
		
		Annotation[] deprecated = null;
		if (isFieldDeprecated(fieldNode)) {
			deprecated = new Annotation[] { generateDeprecatedAnnotation(source) };
		}
		method.annotations = copyAnnotations(source, onMethod.toArray(new Annotation[0]), deprecated);
		Argument param = new Argument(field.name, p, copyType(field.type, source), ClassFileConstants.AccFinal);
		param.sourceStart = pS; param.sourceEnd = pE;
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		Annotation[] nonNulls = findAnnotations(field, NON_NULL_PATTERN);
		Annotation[] nullables = findAnnotations(field, NULLABLE_PATTERN);
		
		if (!makeAbstract) {
			List<Expression> args = new ArrayList<Expression>();
			for (EclipseNode child : fieldNode.up().down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration childDecl = (FieldDeclaration) child.get();
				// Skip fields that start with $
				if (childDecl.name != null && childDecl.name.length > 0 && childDecl.name[0] == '$') continue;
				long fieldFlags = childDecl.modifiers;
				// Skip static fields.
				if ((fieldFlags & ClassFileConstants.AccStatic) != 0) continue;
				// Skip initialized final fields.
				if (((fieldFlags & ClassFileConstants.AccFinal) != 0) && childDecl.initialization != null) continue;
				if (child.get() == fieldNode.get()) {
					args.add(new SingleNameReference(field.name, p));
				} else {
					args.add(createFieldAccessor(child, FieldAccess.ALWAYS_FIELD, source));
				}
			}
			
			AllocationExpression constructorCall = new AllocationExpression();
			constructorCall.arguments = args.toArray(new Expression[0]);
			constructorCall.type = cloneSelfType(fieldNode, source);
			
			Expression identityCheck = new EqualExpression(
					createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source),
					new SingleNameReference(field.name, p),
					OperatorIds.EQUAL_EQUAL);
			ThisReference thisRef = new ThisReference(pS, pE);
			Expression conditional = new ConditionalExpression(identityCheck, thisRef, constructorCall);
			Statement returnStatement = new ReturnStatement(conditional, pS, pE);
			method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
			method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
			
			List<Statement> statements = new ArrayList<Statement>(5);
			if (nonNulls.length > 0) {
				Statement nullCheck = generateNullCheck(field, sourceNode);
				if (nullCheck != null) statements.add(nullCheck);
			}
			statements.add(returnStatement);
			
			method.statements = statements.toArray(new Statement[0]);
		}
		param.annotations = copyAnnotations(source, nonNulls, nullables, onParam.toArray(new Annotation[0]));
		
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		return method;
	}
}
