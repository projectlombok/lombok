/*
 * Copyright (C) 2013-2018 The Project Lombok Authors.
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
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.ConfigurationKeys;
import lombok.Singular;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.EclipseSingularsRecipes.StatementMaker;
import lombok.eclipse.handlers.EclipseSingularsRecipes.TypeReferenceMaker;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleSuperBuilder extends EclipseAnnotationHandler<SuperBuilder> {
	private static final char[] CLEAN_FIELD_NAME = "$lombokUnclean".toCharArray();
	private static final char[] CLEAN_METHOD_NAME = "$lombokClean".toCharArray();
	private static final char[] SET_PREFIX = "$set".toCharArray();
	private static final char[] SELF_METHOD_NAME = "self".toCharArray();
	
	private static final AbstractMethodDeclaration[] EMPTY_METHODS = {};
	
	private static class BuilderFieldData {
		TypeReference type;
		char[] rawName;
		char[] name;
		char[] nameOfSetFlag;
		SingularData singularData;
		ObtainVia obtainVia;
		EclipseNode obtainViaNode;
		EclipseNode originalFieldNode;
		
		List<EclipseNode> createdFields = new ArrayList<EclipseNode>();
	}
	
	@Override
	public void handle(AnnotationValues<SuperBuilder> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.SUPERBUILDER_FLAG_USAGE, "@SuperBuilder");
		
		long p = (long) ast.sourceStart << 32 | ast.sourceEnd;
		
		SuperBuilder builderInstance = annotation.getInstance();
		
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		
		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		
		EclipseNode tdParent = annotationNode.up();
		
		java.util.List<BuilderFieldData> builderFields = new ArrayList<BuilderFieldData>();
		TypeReference returnType;
		TypeParameter[] typeParams;
		
		boolean addCleaning = false;
		
		if (!(tdParent.get() instanceof TypeDeclaration)) {
			annotationNode.addError("@SuperBuilder is only supported on types.");
			return;
		}
		TypeDeclaration td = (TypeDeclaration) tdParent.get();
		
		// Gather all fields of the class that should be set by the builder.
		List<EclipseNode> allFields = new ArrayList<EclipseNode>();
		boolean valuePresent = (hasAnnotation(lombok.Value.class, tdParent) || hasAnnotation("lombok.experimental.Value", tdParent));
		for (EclipseNode fieldNode : HandleConstructor.findAllFields(tdParent, true)) {
			FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
			EclipseNode isDefault = findAnnotation(Builder.Default.class, fieldNode);
			boolean isFinal = ((fd.modifiers & ClassFileConstants.AccFinal) != 0) || (valuePresent && !hasAnnotation(NonFinal.class, fieldNode));
			
			BuilderFieldData bfd = new BuilderFieldData();
			bfd.rawName = fieldNode.getName().toCharArray();
			bfd.name = removePrefixFromField(fieldNode);
			bfd.type = fd.type;
			bfd.singularData = getSingularData(fieldNode, ast);
			bfd.originalFieldNode = fieldNode;
			
			if (bfd.singularData != null && isDefault != null) {
				isDefault.addError("@Builder.Default and @Singular cannot be mixed.");
				isDefault = null;
			}
			
			if (fd.initialization == null && isDefault != null) {
				isDefault.addWarning("@Builder.Default requires an initializing expression (' = something;').");
				isDefault = null;
			}
			
			if (fd.initialization != null && isDefault == null) {
				if (isFinal) {
					continue;
				}
				fieldNode.addWarning("@Builder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
			
			if (isDefault != null) {
				bfd.nameOfSetFlag = prefixWith(bfd.name, SET_PREFIX);
				// The @Builder annotation removes the initializing expression on the field and moves
				// it to a method called "$default$FIELDNAME". This method is then called upon building.
				// We do NOT do this, because this is unexpected and may lead to bugs when using other 
				// constructors (see, e.g., issue #1347).
				// Instead, we keep the init expression and only set a new value in the builder-based
				// constructor if it was set in the builder. Drawback is that the init expression is
				// always executed, even if it was unnecessary because its value is overwritten by the 
				// builder.
				// TODO: Once the issue is resolved in @Builder, we can adapt the solution here. 
			}
			addObtainVia(bfd, fieldNode);
			builderFields.add(bfd);
			allFields.add(fieldNode);
		}
		
		// Set the names of the builder classes.
		String builderClassName = String.valueOf(td.name) + "Builder";
		String builderImplClassName = builderClassName + "Impl";
		
		typeParams = td.typeParameters != null ? td.typeParameters : new TypeParameter[0];
		returnType = namePlusTypeParamsToTypeReference(td.name, typeParams, p);
		
		// <C, B> are the generics for our builder.
		String classGenericName = "C";
		String builderGenericName = "B";
		// If these generics' names collide with any generics on the annotated class, modify them.
		// For instance, if there are generics <B, B2, C> on the annotated class, use "C2" and "B3" for our builder.
		java.util.List<String> typeParamStrings = new ArrayList<String>();
		for (TypeParameter typeParam : typeParams) typeParamStrings.add(typeParam.toString());
		classGenericName = generateNonclashingNameFor(classGenericName, typeParamStrings);
		builderGenericName = generateNonclashingNameFor(builderGenericName, typeParamStrings);
		
		TypeReference extendsClause = td.superclass;
		TypeReference superclassBuilderClass = null;
		TypeReference[] typeArguments = new TypeReference[] {
				new SingleTypeReference(classGenericName.toCharArray(), 0), 
				new SingleTypeReference(builderGenericName.toCharArray(), 0)
		};
		if (extendsClause instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference)extendsClause;
			String superclassClassName = String.valueOf(qualifiedTypeReference.getLastToken());
			String superclassBuilderClassName = superclassClassName + "Builder";
			
			char[][] tokens = Arrays.copyOf(qualifiedTypeReference.tokens, qualifiedTypeReference.tokens.length + 1);
			tokens[tokens.length] = superclassBuilderClassName.toCharArray();
			long[] poss = new long[tokens.length];
			Arrays.fill(poss, p);
			
			TypeReference[] superclassTypeArgs = getTypeParametersFrom(extendsClause);
			
			// Every token may potentially have type args. Here, we only have
			// type args for the last token, the superclass' builder.
			TypeReference[][] typeArgsForTokens = new TypeReference[tokens.length][];
			typeArgsForTokens[typeArgsForTokens.length-1] = mergeTypeReferences(superclassTypeArgs, typeArguments);
			
			superclassBuilderClass = new ParameterizedQualifiedTypeReference(tokens, typeArgsForTokens, 0, poss);
		} else if (extendsClause != null) {
			String superClass = String.valueOf(extendsClause.getTypeName()[0]);
			String superclassBuilderClassName = superClass + "Builder";
			
			char[][] tokens = new char[][] {superClass.toCharArray(), superclassBuilderClassName.toCharArray()};
			long[] poss = new long[tokens.length];
			Arrays.fill(poss, p);
			
			TypeReference[] superclassTypeArgs = getTypeParametersFrom(extendsClause);
			
			// Every token may potentially have type args. Here, we only have
			// type args for the last token, the superclass' builder.
			TypeReference[][] typeArgsForTokens = new TypeReference[tokens.length][];
			typeArgsForTokens[typeArgsForTokens.length-1] = mergeTypeReferences(superclassTypeArgs, typeArguments);
			
			superclassBuilderClass = new ParameterizedQualifiedTypeReference(tokens, typeArgsForTokens, 0, poss);
		}
		// If there is no superclass, superclassBuilderClassExpression is still == null at this point.
		// You can use it to check whether to inherit or not.
		
		generateBuilderBasedConstructor(tdParent, typeParams, builderFields, annotationNode, builderClassName,
			superclassBuilderClass != null);
		
		// Create the abstract builder class.
		EclipseNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = generateBuilderAbstractClass(tdParent, builderClassName, superclassBuilderClass,
				typeParams, ast, classGenericName, builderGenericName);
		} else {
			annotationNode.addError("@SuperBuilder does not support customized builders. Use @Builder instead.");
			return;
		}
		
		// Check validity of @ObtainVia fields, and add check if adding cleaning for @Singular is necessary.
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				if (bfd.singularData.getSingularizer().requiresCleaning()) {
					addCleaning = true;
					break;
				}
			}
			if (bfd.obtainVia != null) {
				if (bfd.obtainVia.field().isEmpty() == bfd.obtainVia.method().isEmpty()) {
					bfd.obtainViaNode.addError("The syntax is either @ObtainVia(field = \"fieldName\") or @ObtainVia(method = \"methodName\").");
					return;
				}
				if (bfd.obtainVia.method().isEmpty() && bfd.obtainVia.isStatic()) {
					bfd.obtainViaNode.addError("@ObtainVia(isStatic = true) is not valid unless 'method' has been set.");
					return;
				}
			}
		}
		
		// Generate the fields in the abstract builder class that hold the values for the instance.
		generateBuilderFields(builderType, builderFields, ast);
		if (addCleaning) {
			FieldDeclaration cleanDecl = new FieldDeclaration(CLEAN_FIELD_NAME, 0, -1);
			cleanDecl.declarationSourceEnd = -1;
			cleanDecl.modifiers = ClassFileConstants.AccPrivate;
			cleanDecl.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
			injectFieldAndMarkGenerated(builderType, cleanDecl);
		}
		
		// Generate abstract self() and build() methods in the abstract builder.
		injectMethod(builderType, generateAbstractSelfMethod(tdParent, superclassBuilderClass != null, builderGenericName));
		injectMethod(builderType, generateAbstractBuildMethod(tdParent, buildMethodName, superclassBuilderClass != null, classGenericName, ast));
		
		// Create the setter methods in the abstract builder.
		for (BuilderFieldData bfd : builderFields) {
			generateSetterMethodsForBuilder(builderType, bfd, annotationNode, builderGenericName);
		}
		
		// Create the toString() method for the abstract builder.
		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			List<Included<EclipseNode, ToString.Include>> fieldNodes = new ArrayList<Included<EclipseNode, ToString.Include>>();
			for (BuilderFieldData bfd : builderFields) {
				for (EclipseNode f : bfd.createdFields) {
					fieldNodes.add(new Included<EclipseNode, ToString.Include>(f, null, true));
				}
			}
			// Let toString() call super.toString() if there is a superclass, so that it also shows fields from the superclass' builder.
			MethodDeclaration md = HandleToString.createToString(builderType, fieldNodes, true, superclassBuilderClass != null, ast, FieldAccess.ALWAYS_FIELD);
			if (md != null) {
				injectMethod(builderType, md);
			}
		}
		
		if (addCleaning) injectMethod(builderType, generateCleanMethod(builderFields, builderType, ast));
		
		if ((td.modifiers & ClassFileConstants.AccAbstract) != 0) {
			// Only non-abstract classes get the Builder implementation.
			return;
		}
		
		// Create the builder implementation class.
		EclipseNode builderImplType = findInnerClass(tdParent, builderImplClassName);
		if (builderImplType == null) {
			builderImplType = generateBuilderImplClass(tdParent, builderImplClassName, builderClassName, typeParams, ast);
		} else {
			annotationNode.addError("@SuperBuilder does not support customized builders. Use @Builder instead.");
			return;
		}
		
		// Create the self() and build() methods in the BuilderImpl.
		injectMethod(builderImplType, generateSelfMethod(builderImplType));
		injectMethod(builderImplType, generateBuildMethod(tdParent, buildMethodName, returnType, ast));
		
		// Add the builder() method to the annotated class.
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			MethodDeclaration md = generateBuilderMethod(builderMethodName, builderClassName, builderImplClassName, tdParent, typeParams, ast);
			if (md != null) injectMethod(tdParent, md);
		}
	}
	
	private EclipseNode generateBuilderAbstractClass(EclipseNode tdParent, String builderClass,
			TypeReference superclassBuilderClass, TypeParameter[] typeParams,
			ASTNode source, String classGenericName, String builderGenericName) {
		
		TypeDeclaration parent = (TypeDeclaration) tdParent.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccStatic | ClassFileConstants.AccAbstract;
		builder.name = builderClass.toCharArray();
		
		// Keep any type params of the annotated class.
		builder.typeParameters = Arrays.copyOf(copyTypeParams(typeParams, source), typeParams.length + 2);
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method, named "C", which extends the annotated class.
		TypeParameter o = new TypeParameter();
		o.name = classGenericName.toCharArray();
		o.type = cloneSelfType(tdParent, source);
		builder.typeParameters[builder.typeParameters.length - 2] = o;
		// 2. The return type for all setter methods, named "B", which extends this builder class.
		o = new TypeParameter();
		o.name = builderGenericName.toCharArray();
		TypeReference[] typerefs = appendBuilderTypeReferences(typeParams, classGenericName, builderGenericName);
		o.type = new ParameterizedSingleTypeReference(builderClass.toCharArray(), typerefs, 0, 0);
		builder.typeParameters[builder.typeParameters.length - 1] = o;
		
		builder.superclass = copyType(superclassBuilderClass, source);
		
		builder.createDefaultConstructor(false, true);
			
		builder.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return injectType(tdParent, builder);
	}
	
	private EclipseNode generateBuilderImplClass(EclipseNode tdParent, String builderImplClass, String builderAbstractClass, TypeParameter[] typeParams, ASTNode source) {
		TypeDeclaration parent = (TypeDeclaration) tdParent.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal;
		builder.name = builderImplClass.toCharArray();
		
		// Add type params if there are any.
		if (typeParams != null && typeParams.length > 0) builder.typeParameters = copyTypeParams(typeParams, source);
		
		if (builderAbstractClass != null) {
			// Extend the abstract builder.
			// 1. Add any type params of the annotated class.
			TypeReference[] typeArgs = new TypeReference[typeParams.length + 2];
			for (int i = 0; i < typeParams.length; i++) {
				typeArgs[i] = new SingleTypeReference(typeParams[i].name, 0);
			}
			// 2. The return type for the build() method (named "C" in the abstract builder), which is the annotated class.
			// 3. The return type for all setter methods (named "B" in the abstract builder), which is this builder class.
			typeArgs[typeArgs.length - 2] = cloneSelfType(tdParent, source);
			typeArgs[typeArgs.length - 1] = createTypeReferenceWithTypeParameters(builderImplClass, typeParams);
			builder.superclass = new ParameterizedSingleTypeReference(builderAbstractClass.toCharArray(), typeArgs, 0, 0);
		}
		
		builder.createDefaultConstructor(false, true);
		
		builder.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return injectType(tdParent, builder);
	}
	
	/**
	 * Generates a constructor that has a builder as the only parameter.
	 * The values from the builder are used to initialize the fields of new instances.
	 *
	 * @param typeNode
	 *            the type (with the {@code @Builder} annotation) for which a
	 *            constructor should be generated.
	 * @param typeParams
	 * @param builderFields a list of fields in the builder which should be assigned to new instances.
	 * @param source the annotation (used for setting source code locations for the generated code).
	 * @param callBuilderBasedSuperConstructor
	 *            If {@code true}, the constructor will explicitly call a super
	 *            constructor with the builder as argument. Requires
	 *            {@code builderClassAsParameter != null}.
	 */
	private void generateBuilderBasedConstructor(EclipseNode typeNode, TypeParameter[] typeParams, List<BuilderFieldData> builderFields,
			EclipseNode sourceNode, String builderClassName, boolean callBuilderBasedSuperConstructor) {
		
		ASTNode source = sourceNode.get();
		
		TypeDeclaration typeDeclaration = ((TypeDeclaration) typeNode.get());
		long p = (long) source.sourceStart << 32 | source.sourceEnd;
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(((CompilationUnitDeclaration) typeNode.top().get()).compilationResult);
		
		constructor.modifiers = toEclipseModifier(AccessLevel.PROTECTED);
		constructor.selector = typeDeclaration.name;
		if (callBuilderBasedSuperConstructor) {
			constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.Super);
			constructor.constructorCall.arguments = new Expression[] {new SingleNameReference("b".toCharArray(), p)};
		} else {
			constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		}
		constructor.constructorCall.sourceStart = source.sourceStart;
		constructor.constructorCall.sourceEnd = source.sourceEnd;
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND)};
		TypeReference builderType = new ParameterizedSingleTypeReference(builderClassName.toCharArray(), mergeToTypeReferences(typeParams, wildcards), 0, p);
		constructor.arguments = new Argument[] {new Argument("b".toCharArray(), p, builderType, Modifier.FINAL)};
		
		List<Statement> statements = new ArrayList<Statement>();
		List<Statement> nullChecks = new ArrayList<Statement>();
		
		for (BuilderFieldData fieldNode : builderFields) {
			char[] fieldName = removePrefixFromField(fieldNode.originalFieldNode);
			FieldReference thisX = new FieldReference(fieldNode.rawName, p);
			int s = (int) (p >> 32);
			int e = (int) p;
			thisX.receiver = new ThisReference(s, e);
			
			Expression assignmentExpr;
			if (fieldNode.singularData != null && fieldNode.singularData.getSingularizer() != null) {
				fieldNode.singularData.getSingularizer().appendBuildCode(fieldNode.singularData, typeNode, statements, fieldNode.name, "b");
				assignmentExpr = new SingleNameReference(fieldNode.name, p);
			} else {
				char[][] variableInBuilder = new char[][] {"b".toCharArray(), fieldName};
				long[] positions = new long[] {p, p};
				assignmentExpr = new QualifiedNameReference(variableInBuilder, positions, s, e);
			}
			Statement assignment = new Assignment(thisX, assignmentExpr, (int) p);
			
			// In case of @Builder.Default, only set the value if it really was set in the builder.
			if (fieldNode.nameOfSetFlag != null) {
				char[][] variableInBuilder = new char[][] {"b".toCharArray(), fieldNode.nameOfSetFlag};
				long[] positions = new long[] {p, p};
				QualifiedNameReference builderRef = new QualifiedNameReference(variableInBuilder, positions, s, e);
				assignment = new IfStatement(builderRef, assignment, s, e);
			}
			statements.add(assignment);
			
			Annotation[] nonNulls = findAnnotations((FieldDeclaration)fieldNode.originalFieldNode.get(), NON_NULL_PATTERN);
			if (nonNulls.length != 0) {
				Statement nullCheck = generateNullCheck((FieldDeclaration)fieldNode.originalFieldNode.get(), sourceNode);
				if (nullCheck != null) {
					nullChecks.add(nullCheck);
				}
			}
		}
		
		nullChecks.addAll(statements);
		constructor.statements = nullChecks.isEmpty() ? null : nullChecks.toArray(new Statement[nullChecks.size()]);
		
		constructor.traverse(new SetGeneratedByVisitor(source), typeDeclaration.scope);
		
		injectMethod(typeNode, constructor);
	}

	private MethodDeclaration generateBuilderMethod(String builderMethodName, String builderClassName, String builderImplClassName, EclipseNode type, TypeParameter[] typeParams, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		out.selector = builderMethodName.toCharArray();
		out.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		// Add type params if there are any.
		if (typeParams != null && typeParams.length > 0) out.typeParameters = copyTypeParams(typeParams, source);
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND) };
		out.returnType = new ParameterizedSingleTypeReference(builderClassName.toCharArray(), mergeToTypeReferences(typeParams, wildcards), 0, p);
		
		AllocationExpression invoke = new AllocationExpression();
		invoke.type = namePlusTypeParamsToTypeReference(builderImplClassName.toCharArray(), typeParams, p);
		out.statements = new Statement[] {new ReturnStatement(invoke, pS, pE)};
		
		out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) type.get()).scope);
		return out;
	}
	
	private MethodDeclaration generateAbstractSelfMethod(EclipseNode tdParent, boolean override, String builderGenericName) {
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) tdParent.top().get()).compilationResult);
		out.selector = SELF_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccAbstract | ClassFileConstants.AccProtected | ExtraCompilerModifiers.AccSemicolonBody;
		if (override) out.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, tdParent.get())};
		out.returnType = new SingleTypeReference(builderGenericName.toCharArray(), 0);
		return out;
	}
	
	private MethodDeclaration generateSelfMethod(EclipseNode builderImplType) {
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) builderImplType.top().get()).compilationResult);
		out.selector = SELF_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccProtected;
		out.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, builderImplType.get())};
		out.returnType = new SingleTypeReference(builderImplType.getName().toCharArray(), 0);
		out.statements = new Statement[] {new ReturnStatement(new ThisReference(0, 0), 0, 0)};
		return out;
	}
	
	private MethodDeclaration generateAbstractBuildMethod(EclipseNode tdParent, String methodName, boolean override,
			String classGenericName, ASTNode source) {
		
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) tdParent.top().get()).compilationResult);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		out.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract | ExtraCompilerModifiers.AccSemicolonBody;
		out.selector = methodName.toCharArray();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = new SingleTypeReference(classGenericName.toCharArray(), 0);
		if (override) out.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		out.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return out;
	}
	
	private MethodDeclaration generateBuildMethod(EclipseNode tdParent, String name, TypeReference returnType, ASTNode source) {
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) tdParent.top().get()).compilationResult);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		List<Statement> statements = new ArrayList<Statement>();
		
		out.modifiers = ClassFileConstants.AccPublic;
		out.selector = name.toCharArray();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = returnType;
		out.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		
		AllocationExpression allocationStatement = new AllocationExpression();
		allocationStatement.type = copyType(out.returnType);
		// Use a constructor that only has this builder as parameter.
		allocationStatement.arguments = new Expression[] {new ThisReference(0, 0)};
		statements.add(new ReturnStatement(allocationStatement, 0, 0));
		out.statements = statements.isEmpty() ? null : statements.toArray(new Statement[statements.size()]);
		out.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return out;
	}
	
	private MethodDeclaration generateCleanMethod(List<BuilderFieldData> builderFields, EclipseNode builderType, ASTNode source) {
		List<Statement> statements = new ArrayList<Statement>();
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendCleaningCode(bfd.singularData, builderType, statements);
			}
		}
		
		FieldReference thisUnclean = new FieldReference(CLEAN_FIELD_NAME, 0);
		thisUnclean.receiver = new ThisReference(0, 0);
		statements.add(new Assignment(thisUnclean, new FalseLiteral(0, 0), 0));
		MethodDeclaration decl = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		decl.selector = CLEAN_METHOD_NAME;
		decl.modifiers = ClassFileConstants.AccPrivate;
		decl.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		decl.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		decl.statements = statements.toArray(new Statement[0]);
		decl.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return decl;
	}
	
	private void generateBuilderFields(EclipseNode builderType, List<BuilderFieldData> builderFields, ASTNode source) {
		List<EclipseNode> existing = new ArrayList<EclipseNode>();
		for (EclipseNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.createdFields.addAll(bfd.singularData.getSingularizer().generateFields(bfd.singularData, builderType));
			} else {
				EclipseNode field = null, setFlag = null;
				for (EclipseNode exists : existing) {
					char[] n = ((FieldDeclaration) exists.get()).name;
					if (Arrays.equals(n, bfd.name)) field = exists;
					if (bfd.nameOfSetFlag != null && Arrays.equals(n, bfd.nameOfSetFlag)) setFlag = exists;
				}
				
				if (field == null) {
					FieldDeclaration fd = new FieldDeclaration(bfd.name, 0, 0);
					fd.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
					fd.modifiers = ClassFileConstants.AccPrivate;
					fd.type = copyType(bfd.type);
					fd.traverse(new SetGeneratedByVisitor(source), (MethodScope) null);
					field = injectFieldAndMarkGenerated(builderType, fd);
				}
				if (setFlag == null && bfd.nameOfSetFlag != null) {
					FieldDeclaration fd = new FieldDeclaration(bfd.nameOfSetFlag, 0, 0);
					fd.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
					fd.modifiers = ClassFileConstants.AccPrivate;
					fd.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
					fd.traverse(new SetGeneratedByVisitor(source), (MethodScope) null);
					injectFieldAndMarkGenerated(builderType, fd);
				}
				bfd.createdFields.add(field);
			}
		}
	}
	
	private void generateSetterMethodsForBuilder(EclipseNode builderType, BuilderFieldData bfd, EclipseNode sourceNode, final String builderGenericName) {
		boolean deprecate = isFieldDeprecated(bfd.originalFieldNode);
		
		TypeReferenceMaker returnTypeMaker = new TypeReferenceMaker() {
			@Override public TypeReference make() {
				return new SingleTypeReference(builderGenericName.toCharArray(), 0);
			}
		};
		
		StatementMaker returnStatementMaker = new StatementMaker() {
			@Override public ReturnStatement make() {
				MessageSend returnCall = new MessageSend();
				returnCall.receiver = ThisReference.implicitThis();
				returnCall.selector = SELF_METHOD_NAME;
				return new ReturnStatement(returnCall, 0, 0);
			}
		};
		
		if (bfd.singularData == null || bfd.singularData.getSingularizer() == null) {
			generateSimpleSetterMethodForBuilder(builderType, deprecate, bfd.createdFields.get(0), bfd.nameOfSetFlag, returnTypeMaker.make(), returnStatementMaker.make(), sourceNode);
		} else {
			bfd.singularData.getSingularizer().generateMethods(bfd.singularData, deprecate, builderType, true, returnTypeMaker, returnStatementMaker);
		}
	}

	private void generateSimpleSetterMethodForBuilder(EclipseNode builderType, boolean deprecate, EclipseNode fieldNode, char[] nameOfSetFlag, TypeReference returnType, Statement returnStatement, EclipseNode sourceNode) {
		TypeDeclaration td = (TypeDeclaration) builderType.get();
		AbstractMethodDeclaration[] existing = td.methods;
		if (existing == null) existing = EMPTY_METHODS;
		int len = existing.length;
		FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
		char[] name = fd.name;
		
		for (int i = 0; i < len; i++) {
			if (!(existing[i] instanceof MethodDeclaration)) continue;
			char[] existingName = existing[i].selector;
			if (Arrays.equals(name, existingName) && !isTolerate(fieldNode, existing[i])) return;
		}
		
		String setterName = fieldNode.getName();
		
		MethodDeclaration setter = HandleSetter.createSetter(td, deprecate, fieldNode, setterName, nameOfSetFlag, returnType, returnStatement, ClassFileConstants.AccPublic,
			sourceNode, Collections.<Annotation>emptyList(), Collections.<Annotation>emptyList());
		injectMethod(builderType, setter);
	}
	
	private void addObtainVia(BuilderFieldData bfd, EclipseNode node) {
		for (EclipseNode child : node.down()) {
			if (!annotationTypeMatches(ObtainVia.class, child)) continue;
			AnnotationValues<ObtainVia> ann = createAnnotation(ObtainVia.class, child);
			bfd.obtainVia = ann.getInstance();
			bfd.obtainViaNode = child;
			return;
		}
	}
	
	/**
	 * Returns the explicitly requested singular annotation on this node (field
	 * or parameter), or null if there's no {@code @Singular} annotation on it.
	 *
	 * @param node The node (field or method param) to inspect for its name and potential {@code @Singular} annotation.
	 */
	private SingularData getSingularData(EclipseNode node, ASTNode source) {
		for (EclipseNode child : node.down()) {
			if (!annotationTypeMatches(Singular.class, child)) continue;
			
			char[] pluralName = node.getKind() == Kind.FIELD ? removePrefixFromField(node) : ((AbstractVariableDeclaration) node.get()).name;
			AnnotationValues<Singular> ann = createAnnotation(Singular.class, child);
			String explicitSingular = ann.getInstance().value();
			if (explicitSingular.isEmpty()) {
				if (Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_AUTO))) {
					node.addError("The singular must be specified explicitly (e.g. @Singular(\"task\")) because auto singularization is disabled.");
					explicitSingular = new String(pluralName);
				} else {
					explicitSingular = autoSingularize(new String(pluralName));
					if (explicitSingular == null) {
						node.addError("Can't singularize this name; please specify the singular explicitly (i.e. @Singular(\"sheep\"))");
						explicitSingular = new String(pluralName);
					}
				}
			}
			char[] singularName = explicitSingular.toCharArray();
			
			TypeReference type = ((AbstractVariableDeclaration) node.get()).type;
			TypeReference[] typeArgs = null;
			String typeName;
			if (type instanceof ParameterizedSingleTypeReference) {
				typeArgs = ((ParameterizedSingleTypeReference) type).typeArguments;
				typeName = new String(((ParameterizedSingleTypeReference) type).token);
			} else if (type instanceof ParameterizedQualifiedTypeReference) {
				TypeReference[][] tr = ((ParameterizedQualifiedTypeReference) type).typeArguments;
				if (tr != null) typeArgs = tr[tr.length - 1];
				char[][] tokens = ((ParameterizedQualifiedTypeReference) type).tokens;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < tokens.length; i++) {
					if (i > 0) sb.append(".");
					sb.append(tokens[i]);
				}
				typeName = sb.toString();
			} else {
				typeName = type.toString();
			}
			
			String targetFqn = EclipseSingularsRecipes.get().toQualified(typeName);
			EclipseSingularizer singularizer = EclipseSingularsRecipes.get().getSingularizer(targetFqn);
			if (singularizer == null) {
				node.addError("Lombok does not know how to create the singular-form builder methods for type '" + typeName + "'; they won't be generated.");
				return null;
			}
			
			return new SingularData(child, singularName, pluralName, typeArgs == null ? Collections.<TypeReference>emptyList() : Arrays.asList(typeArgs), targetFqn, singularizer, source);
		}
		
		return null;
	}
	
	private String generateNonclashingNameFor(String classGenericName, java.util.List<String> typeParamStrings) {
		if (!typeParamStrings.contains(classGenericName)) return classGenericName;
		int counter = 2;
		while (typeParamStrings.contains(classGenericName + counter)) counter++;
		return classGenericName + counter;
	}
	
	private TypeReference[] appendBuilderTypeReferences(TypeParameter[] typeParams, String classGenericName, String builderGenericName) {
		TypeReference[] typeReferencesToAppend = new TypeReference[2];
		typeReferencesToAppend[typeReferencesToAppend.length - 2] = new SingleTypeReference(classGenericName.toCharArray(), 0); 
		typeReferencesToAppend[typeReferencesToAppend.length - 1] = new SingleTypeReference(builderGenericName.toCharArray(), 0);
		return mergeToTypeReferences(typeParams, typeReferencesToAppend);
	}
	
	private TypeReference[] getTypeParametersFrom(TypeReference typeRef) {
		TypeReference[][] typeArgss = null;
		if (typeRef instanceof ParameterizedQualifiedTypeReference) {
			typeArgss = ((ParameterizedQualifiedTypeReference) typeRef).typeArguments;
		} else if (typeRef instanceof ParameterizedSingleTypeReference) {
			typeArgss = new TypeReference[][] {((ParameterizedSingleTypeReference) typeRef).typeArguments};
		}
		TypeReference[] typeArgs = new TypeReference[0];
		if (typeArgss != null && typeArgss.length > 0) {
			typeArgs = typeArgss[typeArgss.length - 1];
		}
		return typeArgs;
	}
	
	private static SingleTypeReference createTypeReferenceWithTypeParameters(String referenceName, TypeParameter[] typeParams) {
		if (typeParams.length > 0) {
			TypeReference[] typerefs = new TypeReference[typeParams.length];
			for (int i = 0; i < typeParams.length; i++) {
				typerefs[i] = new SingleTypeReference(typeParams[i].name, 0);
			}
			return new ParameterizedSingleTypeReference(referenceName.toCharArray(), typerefs, 0, 0);
		} else {
			return new SingleTypeReference(referenceName.toCharArray(), 0);
		}
	}
	
	private TypeReference[] mergeToTypeReferences(TypeParameter[] typeParams, TypeReference[] typeReferencesToAppend) {
		TypeReference[] typerefs = new TypeReference[typeParams.length + typeReferencesToAppend.length];
		for (int i = 0; i < typeParams.length; i++) {
			typerefs[i] = new SingleTypeReference(typeParams[i].name, 0);
		}
		for (int i = 0; i < typeReferencesToAppend.length; i++) {
			typerefs[typeParams.length + i] = typeReferencesToAppend[i];
		}
		return typerefs;
	}
	
	private TypeReference[] mergeTypeReferences(TypeReference[] refs1, TypeReference[] refs2) {
		TypeReference[] result = new TypeReference[refs1.length + refs2.length];
		for (int i = 0; i < refs1.length; i++) result[i] = refs1[i];
		for (int i = 0; i < refs2.length; i++) result[refs1.length + i] = refs2[i];
		return result;
	}
	
	private EclipseNode findInnerClass(EclipseNode parent, String name) {
		char[] c = name.toCharArray();
		for (EclipseNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			TypeDeclaration td = (TypeDeclaration) child.get();
			if (Arrays.equals(td.name, c)) return child;
		}
		return null;
	}
	
	private static final char[] prefixWith(char[] prefix, char[] name) {
		char[] out = new char[prefix.length + name.length];
		System.arraycopy(prefix, 0, out, 0, prefix.length);
		System.arraycopy(name, 0, out, prefix.length, name.length);
		return out;
	}
}
