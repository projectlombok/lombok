/*
 * Copyright (C) 2010-2011 The Project Lombok Authors.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

public class HandleConstructor {
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleNoArgsConstructor extends EclipseAnnotationHandler<NoArgsConstructor> {
		@Override public void handle(AnnotationValues<NoArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NoArgsConstructor.class.getSimpleName())) return;
			NoArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			if (level == AccessLevel.NONE) return;
			List<EclipseNode> fields = new ArrayList<EclipseNode>();
			new HandleConstructor().generateConstructor(typeNode, level, fields, staticName, false, false, ast);
		}
	}
	
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleRequiredArgsConstructor extends EclipseAnnotationHandler<RequiredArgsConstructor> {
		@Override public void handle(AnnotationValues<RequiredArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, RequiredArgsConstructor.class.getSimpleName())) return;
			RequiredArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			@SuppressWarnings("deprecation")
			boolean suppressConstructorProperties = ann.suppressConstructorProperties();
			if (level == AccessLevel.NONE) return;
			new HandleConstructor().generateConstructor(typeNode, level, findRequiredFields(typeNode), staticName, false, suppressConstructorProperties, ast);
		}
	}
	
	private static List<EclipseNode> findRequiredFields(EclipseNode typeNode) {
		List<EclipseNode> fields = new ArrayList<EclipseNode>();
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			if (!filterField(fieldDecl)) continue;
			boolean isFinal = (fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0;
			boolean isNonNull = findAnnotations(fieldDecl, TransformationsUtil.NON_NULL_PATTERN).length != 0;
			if ((isFinal || isNonNull) && fieldDecl.initialization == null) fields.add(child);
		}
		return fields;
	}
	
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleAllArgsConstructor extends EclipseAnnotationHandler<AllArgsConstructor> {
		@Override public void handle(AnnotationValues<AllArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, AllArgsConstructor.class.getSimpleName())) return;
			AllArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			@SuppressWarnings("deprecation")
			boolean suppressConstructorProperties = ann.suppressConstructorProperties();
			if (level == AccessLevel.NONE) return;
			List<EclipseNode> fields = new ArrayList<EclipseNode>();
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				if (!filterField(fieldDecl)) continue;
				
				// Skip initialized final fields.
				if (((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0) && fieldDecl.initialization != null) continue;
				
				fields.add(child);
			}
			new HandleConstructor().generateConstructor(typeNode, level, fields, staticName, false, suppressConstructorProperties, ast);
		}
	}
	
	static boolean checkLegality(EclipseNode typeNode, EclipseNode errorNode, String name) {
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError(name + " is only supported on a class or an enum.");
			return false;
		}
		
		return true;
	}
	
	public void generateRequiredArgsConstructor(EclipseNode typeNode, AccessLevel level, String staticName, boolean skipIfConstructorExists, ASTNode source) {
		generateConstructor(typeNode, level, findRequiredFields(typeNode), staticName, skipIfConstructorExists, false, source);
	}
	
	public void generateConstructor(EclipseNode typeNode, AccessLevel level, List<EclipseNode> fields, String staticName, boolean skipIfConstructorExists, boolean suppressConstructorProperties, ASTNode source) {
		if (skipIfConstructorExists && constructorExists(typeNode) != MemberExistsResult.NOT_EXISTS) return;
		if (skipIfConstructorExists) {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(NoArgsConstructor.class, child) ||
							annotationTypeMatches(AllArgsConstructor.class, child) ||
							annotationTypeMatches(RequiredArgsConstructor.class, child))
						return;
				}
			}
		}
		
		boolean staticConstrRequired = staticName != null && !staticName.equals("");
		
		ConstructorDeclaration constr = createConstructor(staticConstrRequired ? AccessLevel.PRIVATE : level, typeNode, fields, suppressConstructorProperties, source);
		injectMethod(typeNode, constr);
		if (staticConstrRequired) {
			MethodDeclaration staticConstr = createStaticConstructor(level, staticName, typeNode, fields, source);
			injectMethod(typeNode, staticConstr);
		}
	}
	
	private static final char[][] JAVA_BEANS_CONSTRUCTORPROPERTIES = new char[][] { "java".toCharArray(), "beans".toCharArray(), "ConstructorProperties".toCharArray() };
	private static Annotation[] createConstructorProperties(ASTNode source, Annotation[] originalAnnotationArray, Collection<EclipseNode> fields) {
		if (fields.isEmpty()) return originalAnnotationArray;
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		long[] poss = new long[3];
		Arrays.fill(poss, p);
		QualifiedTypeReference constructorPropertiesType = new QualifiedTypeReference(JAVA_BEANS_CONSTRUCTORPROPERTIES, poss);
		setGeneratedBy(constructorPropertiesType, source);
		SingleMemberAnnotation ann = new SingleMemberAnnotation(constructorPropertiesType, pS);
		ann.declarationSourceEnd = pE;
		
		ArrayInitializer fieldNames = new ArrayInitializer();
		fieldNames.sourceStart = pS;
		fieldNames.sourceEnd = pE;
		fieldNames.expressions = new Expression[fields.size()];
		
		int ctr = 0;
		for (EclipseNode field : fields) {
			fieldNames.expressions[ctr] = new StringLiteral(field.getName().toCharArray(), pS, pE, 0);
			setGeneratedBy(fieldNames.expressions[ctr], source);
			ctr++;
		}
		
		ann.memberValue = fieldNames;
		setGeneratedBy(ann, source);
		setGeneratedBy(ann.memberValue, source);
		if (originalAnnotationArray == null) return new Annotation[] { ann };
		Annotation[] newAnnotationArray = Arrays.copyOf(originalAnnotationArray, originalAnnotationArray.length + 1);
		newAnnotationArray[originalAnnotationArray.length] = ann;
		return newAnnotationArray;
	}
	
	private ConstructorDeclaration createConstructor(AccessLevel level,
			EclipseNode type, Collection<EclipseNode> fields, boolean suppressConstructorProperties, ASTNode source) {
		long p = (long)source.sourceStart << 32 | source.sourceEnd;
		
		boolean isEnum = (((TypeDeclaration)type.get()).modifiers & ClassFileConstants.AccEnum) != 0;
		
		if (isEnum) level = AccessLevel.PRIVATE;
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(constructor, source);
		
		constructor.modifiers = toEclipseModifier(level);
		constructor.selector = ((TypeDeclaration)type.get()).name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		setGeneratedBy(constructor.constructorCall, source);
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		constructor.arguments = null;
		
		List<Argument> params = new ArrayList<Argument>();
		List<Statement> assigns = new ArrayList<Statement>();
		List<Statement> nullChecks = new ArrayList<Statement>();
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			FieldReference thisX = new FieldReference(field.name, p);
			setGeneratedBy(thisX, source);
			thisX.receiver = new ThisReference((int)(p >> 32), (int)p);
			setGeneratedBy(thisX.receiver, source);
			
			SingleNameReference assignmentNameRef = new SingleNameReference(field.name, p);
			setGeneratedBy(assignmentNameRef, source);
			Assignment assignment = new Assignment(thisX, assignmentNameRef, (int)p);
			setGeneratedBy(assignment, source);
			assigns.add(assignment);
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			Argument parameter = new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL);
			setGeneratedBy(parameter, source);
			Annotation[] nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
			Annotation[] nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
			if (nonNulls.length != 0) {
				Statement nullCheck = generateNullCheck(field, source);
				if (nullCheck != null) nullChecks.add(nullCheck);
			}
			Annotation[] copiedAnnotations = copyAnnotations(source, nonNulls, nullables);
			if (copiedAnnotations.length != 0) parameter.annotations = copiedAnnotations;
			params.add(parameter);
		}
		
		nullChecks.addAll(assigns);
		constructor.statements = nullChecks.isEmpty() ? null : nullChecks.toArray(new Statement[nullChecks.size()]);
		constructor.arguments = params.isEmpty() ? null : params.toArray(new Argument[params.size()]);
		
		if (!suppressConstructorProperties && level != AccessLevel.PRIVATE && !isLocalType(type)) {
			constructor.annotations = createConstructorProperties(source, constructor.annotations, fields);
		}
		
		return constructor;
	}
	
	private boolean isLocalType(EclipseNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
	
	private MethodDeclaration createStaticConstructor(AccessLevel level, String name, EclipseNode type, Collection<EclipseNode> fields, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration constructor = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(constructor, source);
		
		constructor.modifiers = toEclipseModifier(level) | Modifier.STATIC;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		if (typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0) {
			TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
			int idx = 0;
			for (TypeParameter param : typeDecl.typeParameters) {
				TypeReference typeRef = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
				setGeneratedBy(typeRef, source);
				refs[idx++] = typeRef;
			}
			constructor.returnType = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
		} else constructor.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
		setGeneratedBy(constructor.returnType, source);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = copyTypeParams(((TypeDeclaration)type.get()).typeParameters, source);
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		
		List<Argument> params = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.sourceStart = pS; statement.sourceEnd = pE;
		setGeneratedBy(statement, source);
		statement.type = copyType(constructor.returnType, source);
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			SingleNameReference nameRef = new SingleNameReference(field.name, fieldPos);
			setGeneratedBy(nameRef, source);
			assigns.add(nameRef);
			
			Argument parameter = new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL);
			setGeneratedBy(parameter, source);

			Annotation[] copiedAnnotations = copyAnnotations(source, findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN), findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN));
			if (copiedAnnotations.length != 0) parameter.annotations = copiedAnnotations;
			params.add(new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL));
		}
		
		statement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = params.isEmpty() ? null : params.toArray(new Argument[params.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int)(p >> 32), (int)p) };
		setGeneratedBy(constructor.statements[0], source);
		return constructor;
	}
}
