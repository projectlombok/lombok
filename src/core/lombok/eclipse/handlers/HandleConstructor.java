/*
 * Copyright (C) 2010-2017 The Project Lombok Authors.
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

public class HandleConstructor {
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleNoArgsConstructor extends EclipseAnnotationHandler<NoArgsConstructor> {
		@Override public void handle(AnnotationValues<NoArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.NO_ARGS_CONSTRUCTOR_FLAG_USAGE, "@NoArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NoArgsConstructor.class.getSimpleName())) return;
			NoArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			if (level == AccessLevel.NONE) return;
			
			boolean force = ann.force();
			
			List<EclipseNode> fields = force ? findFinalFields(typeNode) : Collections.<EclipseNode>emptyList();
			List<Annotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@NoArgsConstructor(onConstructor", annotationNode);
			
			new HandleConstructor().generateConstructor(typeNode, level, fields, force, staticName, SkipIfConstructorExists.NO, onConstructor, annotationNode);
		}
	}
	
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleRequiredArgsConstructor extends EclipseAnnotationHandler<RequiredArgsConstructor> {
		@Override public void handle(AnnotationValues<RequiredArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE, "@RequiredArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, RequiredArgsConstructor.class.getSimpleName())) return;
			RequiredArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			
			List<Annotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@RequiredArgsConstructor(onConstructor", annotationNode);
			
			new HandleConstructor().generateConstructor(
				typeNode, level, findRequiredFields(typeNode), false, staticName, SkipIfConstructorExists.NO,
				onConstructor, annotationNode);
		}
	}
	
	private static List<EclipseNode> findRequiredFields(EclipseNode typeNode) {
		return findFields(typeNode, true);
	}
	
	private static List<EclipseNode> findFinalFields(EclipseNode typeNode) {
		return findFields(typeNode, false);
	}
	
	private static List<EclipseNode> findFields(EclipseNode typeNode, boolean nullMarked) {
		List<EclipseNode> fields = new ArrayList<EclipseNode>();
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			if (!filterField(fieldDecl)) continue;
			boolean isFinal = (fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0;
			boolean isNonNull = nullMarked && findAnnotations(fieldDecl, NON_NULL_PATTERN).length != 0;
			if ((isFinal || isNonNull) && fieldDecl.initialization == null) fields.add(child);
		}
		return fields;
	}
	
	static List<EclipseNode> findAllFields(EclipseNode typeNode) {
		return findAllFields(typeNode, false);
	}
	
	static List<EclipseNode> findAllFields(EclipseNode typeNode, boolean evenFinalInitialized) {
		List<EclipseNode> fields = new ArrayList<EclipseNode>();
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			if (!filterField(fieldDecl)) continue;
			
			if (!evenFinalInitialized && ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0) && fieldDecl.initialization != null) continue;
			
			fields.add(child);
		}
		return fields;
	}
	
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleAllArgsConstructor extends EclipseAnnotationHandler<AllArgsConstructor> {
		@Override public void handle(AnnotationValues<AllArgsConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.ALL_ARGS_CONSTRUCTOR_FLAG_USAGE, "@AllArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			EclipseNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, AllArgsConstructor.class.getSimpleName())) return;
			AllArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			
			List<Annotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@AllArgsConstructor(onConstructor", annotationNode);
			
			new HandleConstructor().generateConstructor(
				typeNode, level, findAllFields(typeNode), false, staticName, SkipIfConstructorExists.NO,
				onConstructor, annotationNode);
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
	
	public void generateRequiredArgsConstructor(
			EclipseNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists,
			List<Annotation> onConstructor, EclipseNode sourceNode) {
		
		generateConstructor(typeNode, level, findRequiredFields(typeNode), false, staticName, skipIfConstructorExists, onConstructor, sourceNode);
	}
	
	public void generateAllArgsConstructor(
			EclipseNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists,
			List<Annotation> onConstructor, EclipseNode sourceNode) {
		
		generateConstructor(typeNode, level, findAllFields(typeNode), false, staticName, skipIfConstructorExists, onConstructor, sourceNode);
	}
	
	public enum SkipIfConstructorExists {
		YES, NO, I_AM_BUILDER;
	}
	
	public void generateConstructor(
		EclipseNode typeNode, AccessLevel level, List<EclipseNode> fields, boolean allToDefault, String staticName, SkipIfConstructorExists skipIfConstructorExists,
		List<Annotation> onConstructor, EclipseNode sourceNode) {
		
		ASTNode source = sourceNode.get();
		boolean staticConstrRequired = staticName != null && !staticName.equals("");
		
		if (skipIfConstructorExists != SkipIfConstructorExists.NO && constructorExists(typeNode) != MemberExistsResult.NOT_EXISTS) return;
		if (skipIfConstructorExists != SkipIfConstructorExists.NO) {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					boolean skipGeneration = (annotationTypeMatches(NoArgsConstructor.class, child) ||
						annotationTypeMatches(AllArgsConstructor.class, child) ||
						annotationTypeMatches(RequiredArgsConstructor.class, child));
					
					if (!skipGeneration && skipIfConstructorExists == SkipIfConstructorExists.YES) {
						skipGeneration = annotationTypeMatches(Builder.class, child);
					}
					
					if (skipGeneration) {
						if (staticConstrRequired) {
							// @Data has asked us to generate a constructor, but we're going to skip this instruction, as an explicit 'make a constructor' annotation
							// will take care of it. However, @Data also wants a specific static name; this will be ignored; the appropriate way to do this is to use
							// the 'staticName' parameter of the @XArgsConstructor you've stuck on your type.
							// We should warn that we're ignoring @Data's 'staticConstructor' param.
							typeNode.addWarning(
								"Ignoring static constructor name: explicit @XxxArgsConstructor annotation present; its `staticName` parameter will be used.",
								source.sourceStart, source.sourceEnd);
						}
						return;
					}
				}
			}
		}
		
		ConstructorDeclaration constr = createConstructor(
			staticConstrRequired ? AccessLevel.PRIVATE : level, typeNode, fields, allToDefault,
			sourceNode, onConstructor);
		injectMethod(typeNode, constr);
		if (staticConstrRequired) {
			MethodDeclaration staticConstr = createStaticConstructor(level, staticName, typeNode, allToDefault ? Collections.<EclipseNode>emptyList() : fields, source);
			injectMethod(typeNode, staticConstr);
		}
	}
	
	private static final char[][] JAVA_BEANS_CONSTRUCTORPROPERTIES = new char[][] { "java".toCharArray(), "beans".toCharArray(), "ConstructorProperties".toCharArray() };
	public static Annotation[] createConstructorProperties(ASTNode source, Collection<EclipseNode> fields) {
		if (fields.isEmpty()) return null;
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
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
			char[] fieldName = removePrefixFromField(field);
			fieldNames.expressions[ctr] = new StringLiteral(fieldName, pS, pE, 0);
			setGeneratedBy(fieldNames.expressions[ctr], source);
			ctr++;
		}
		
		ann.memberValue = fieldNames;
		setGeneratedBy(ann, source);
		setGeneratedBy(ann.memberValue, source);
		return new Annotation[] { ann };
	}
	
	public static ConstructorDeclaration createConstructor(
		AccessLevel level, EclipseNode type, Collection<EclipseNode> fields, boolean allToDefault,
		EclipseNode sourceNode, List<Annotation> onConstructor) {
		
		ASTNode source = sourceNode.get();
		TypeDeclaration typeDeclaration = ((TypeDeclaration) type.get());
		long p = (long) source.sourceStart << 32 | source.sourceEnd;
		
		boolean isEnum = (((TypeDeclaration) type.get()).modifiers & ClassFileConstants.AccEnum) != 0;
		
		if (isEnum) level = AccessLevel.PRIVATE;
		
		boolean suppressConstructorProperties;
		if (fields.isEmpty()) {
			suppressConstructorProperties = false;
		} else {
			suppressConstructorProperties = Boolean.TRUE.equals(type.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = toEclipseModifier(level);
		constructor.selector = typeDeclaration.name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		constructor.constructorCall.sourceStart = source.sourceStart;
		constructor.constructorCall.sourceEnd = source.sourceEnd;
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
			char[] rawName = field.name;
			char[] fieldName = removePrefixFromField(fieldNode);
			FieldReference thisX = new FieldReference(rawName, p);
			int s = (int) (p >> 32);
			int e = (int) p;
			thisX.receiver = new ThisReference(s, e);
			
			Expression assignmentExpr = allToDefault ? getDefaultExpr(field.type, s, e) : new SingleNameReference(fieldName, p);
			
			Assignment assignment = new Assignment(thisX, assignmentExpr, (int) p);
			assignment.sourceStart = (int) (p >> 32); assignment.sourceEnd = assignment.statementEnd = (int) (p >> 32);
			assigns.add(assignment);
			if (!allToDefault) {
				long fieldPos = (((long) field.sourceStart) << 32) | field.sourceEnd;
				Argument parameter = new Argument(fieldName, fieldPos, copyType(field.type, source), Modifier.FINAL);
				Annotation[] nonNulls = findAnnotations(field, NON_NULL_PATTERN);
				Annotation[] nullables = findAnnotations(field, NULLABLE_PATTERN);
				if (nonNulls.length != 0) {
					Statement nullCheck = generateNullCheck(field, sourceNode);
					if (nullCheck != null) nullChecks.add(nullCheck);
				}
				parameter.annotations = copyAnnotations(source, nonNulls, nullables);
				params.add(parameter);
			}
		}
		
		nullChecks.addAll(assigns);
		constructor.statements = nullChecks.isEmpty() ? null : nullChecks.toArray(new Statement[nullChecks.size()]);
		constructor.arguments = params.isEmpty() ? null : params.toArray(new Argument[params.size()]);
		
		/* Generate annotations that must  be put on the generated method, and attach them. */ {
			Annotation[] constructorProperties = null;
			if (!allToDefault && !suppressConstructorProperties && !isLocalType(type)) {
				constructorProperties = createConstructorProperties(source, fields);
			}
			
			constructor.annotations = copyAnnotations(source,
				onConstructor.toArray(new Annotation[0]),
				constructorProperties);
		}
		
		constructor.traverse(new SetGeneratedByVisitor(source), typeDeclaration.scope);
		return constructor;
	}
	
	private static Expression getDefaultExpr(TypeReference type, int s, int e) {
		boolean array = type instanceof ArrayTypeReference;
		if (array) return new NullLiteral(s, e);
		char[] lastToken = type.getLastToken();
		if (Arrays.equals(TypeConstants.BOOLEAN, lastToken)) return new FalseLiteral(s, e);
		if (Arrays.equals(TypeConstants.CHAR, lastToken)) return new CharLiteral(new char[] {'\'', '\\', '0', '\''}, s, e);
		if (Arrays.equals(TypeConstants.BYTE, lastToken) || Arrays.equals(TypeConstants.SHORT, lastToken) ||
			Arrays.equals(TypeConstants.INT, lastToken)) return IntLiteral.buildIntLiteral(new char[] {'0'}, s, e);
		if (Arrays.equals(TypeConstants.LONG, lastToken)) return LongLiteral.buildLongLiteral(new char[] {'0',  'L'}, s, e);
		if (Arrays.equals(TypeConstants.FLOAT, lastToken)) return new FloatLiteral(new char[] {'0', 'F'}, s, e);
		if (Arrays.equals(TypeConstants.DOUBLE, lastToken)) return new DoubleLiteral(new char[] {'0', 'D'}, s, e);
		
		return new NullLiteral(s, e);
	}
	
	public static boolean isLocalType(EclipseNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
	
	public MethodDeclaration createStaticConstructor(AccessLevel level, String name, EclipseNode type, Collection<EclipseNode> fields, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration constructor = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = toEclipseModifier(level) | ClassFileConstants.AccStatic;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		constructor.returnType = EclipseHandlerUtil.namePlusTypeParamsToTypeReference(typeDecl.name, typeDecl.typeParameters, p);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = copyTypeParams(((TypeDeclaration) type.get()).typeParameters, source);
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		
		List<Argument> params = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.sourceStart = pS; statement.sourceEnd = pE;
		statement.type = copyType(constructor.returnType, source);
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long) field.sourceStart) << 32) | field.sourceEnd;
			SingleNameReference nameRef = new SingleNameReference(field.name, fieldPos);
			assigns.add(nameRef);
			
			Argument parameter = new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL);
			parameter.annotations = copyAnnotations(source, findAnnotations(field, NON_NULL_PATTERN), findAnnotations(field, NULLABLE_PATTERN));
			params.add(parameter);
		}
		
		statement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = params.isEmpty() ? null : params.toArray(new Argument[params.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int) (p >> 32), (int)p) };
		
		constructor.traverse(new SetGeneratedByVisitor(source), typeDecl.scope);
		return constructor;
	}
}
