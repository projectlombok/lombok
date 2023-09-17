/*
 * Copyright (C) 2013-2021 The Project Lombok Authors.
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

import static lombok.eclipse.handlers.HandleBuilder.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.ConfigurationKeys;
import lombok.Singular;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.CopyJavadoc;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.EclipseSingularsRecipes.StatementMaker;
import lombok.eclipse.handlers.EclipseSingularsRecipes.TypeReferenceMaker;
import lombok.eclipse.handlers.HandleBuilder.BuilderFieldData;
import lombok.eclipse.handlers.HandleBuilder.BuilderJob;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.spi.Provides;

@Provides
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleSuperBuilder extends EclipseAnnotationHandler<SuperBuilder> {
	private static final char[] SELF_METHOD_NAME = "self".toCharArray();
	private static final char[] FILL_VALUES_METHOD_NAME = "$fillValuesFrom".toCharArray();
	private static final char[] FILL_VALUES_STATIC_METHOD_NAME = "$fillValuesFromInstanceIntoBuilder".toCharArray();
	private static final char[] INSTANCE_VARIABLE_NAME = "instance".toCharArray();
	private static final String BUILDER_VARIABLE_NAME_STRING = "b";
	private static final char[] BUILDER_VARIABLE_NAME = BUILDER_VARIABLE_NAME_STRING.toCharArray();
	
	class SuperBuilderJob extends BuilderJob {
		void init(AnnotationValues<SuperBuilder> annValues, SuperBuilder ann, EclipseNode node) {
			accessOuters = accessInners = AccessLevel.PUBLIC;
			oldFluent = true;
			oldChain = true;
			
			builderMethodName = ann.builderMethodName();
			buildMethodName = ann.buildMethodName();
			toBuilder = ann.toBuilder();
			
			if (builderMethodName == null) builderMethodName = "builder";
			if (buildMethodName == null) buildMethodName = "build";
			builderClassName = getBuilderClassNameTemplate(node, null);
		}
		
		EclipseNode builderAbstractType;
		String builderAbstractClassName;
		char[] builderAbstractClassNameArr;
		EclipseNode builderImplType;
		String builderImplClassName;
		char[] builderImplClassNameArr;
		private TypeParameter[] builderTypeParams_;
		void setBuilderToImpl() {
			builderType = builderImplType;
			builderClassName = builderImplClassName;
			builderClassNameArr = builderImplClassNameArr;
			builderTypeParams = typeParams;
		}
		
		void setBuilderToAbstract() {
			builderType = builderAbstractType;
			builderClassName = builderAbstractClassName;
			builderClassNameArr = builderAbstractClassNameArr;
			builderTypeParams = builderTypeParams_;
		}
	}
	
	@Override public void handle(AnnotationValues<SuperBuilder> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.SUPERBUILDER_FLAG_USAGE, "@SuperBuilder");
		SuperBuilderJob job = new SuperBuilderJob();
		job.sourceNode = annotationNode;
		job.source = ast;
		job.checkerFramework = getCheckerFrameworkVersion(annotationNode);
		job.isStatic = true;
		
		SuperBuilder annInstance = annotation.getInstance();
		job.init(annotation, annInstance, annotationNode);
		
		boolean generateBuilderMethod;
		if (job.builderMethodName.isEmpty()) {
			generateBuilderMethod = false;
		} else if (!checkName("builderMethodName", job.builderMethodName, annotationNode)) {
			return;
		} else {
			generateBuilderMethod = true;
		}
		
		if (!checkName("buildMethodName", job.buildMethodName, annotationNode)) return;
		
		EclipseNode parent = annotationNode.up();
		
		job.builderFields = new ArrayList<BuilderFieldData>();
		TypeReference buildMethodReturnType;
		
		boolean addCleaning = false;
		
		List<EclipseNode> nonFinalNonDefaultedFields = null;
		
		if (!isClass(parent)) {
			annotationNode.addError("@SuperBuilder is only supported on classes.");
			return;
		}
		if (!isStaticAllowed(parent)) {
			annotationNode.addError("@SuperBuilder is not supported on non-static nested classes.");
			return;
		}
		
		job.parentType = parent;
		TypeDeclaration td = (TypeDeclaration) parent.get();
		
		// Gather all fields of the class that should be set by the builder.
		List<EclipseNode> allFields = new ArrayList<EclipseNode>();
		
		boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation("lombok.experimental.Value", parent));
		for (EclipseNode fieldNode : HandleConstructor.findAllFields(parent, true)) {
			FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
			EclipseNode isDefault = findAnnotation(Builder.Default.class, fieldNode);
			boolean isFinal = ((fd.modifiers & ClassFileConstants.AccFinal) != 0) || (valuePresent && !hasAnnotation(NonFinal.class, fieldNode));
			
			Annotation[] copyableAnnotations = findCopyableAnnotations(fieldNode);
			
			BuilderFieldData bfd = new BuilderFieldData();
			bfd.rawName = fieldNode.getName().toCharArray();
			bfd.name = removePrefixFromField(fieldNode);
			bfd.builderFieldName = bfd.name;
			bfd.annotations = copyAnnotations(fd, copyableAnnotations);
			bfd.type = fd.type;
			bfd.singularData = getSingularData(fieldNode, ast, annInstance.setterPrefix());
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
				if (isFinal) continue;
				if (nonFinalNonDefaultedFields == null) nonFinalNonDefaultedFields = new ArrayList<EclipseNode>();
				nonFinalNonDefaultedFields.add(fieldNode);
			}
			
			if (isDefault != null) {
				bfd.nameOfDefaultProvider = prefixWith(DEFAULT_PREFIX, bfd.name);
				bfd.nameOfSetFlag = prefixWith(bfd.name, SET_PREFIX);
				bfd.builderFieldName = prefixWith(bfd.name, VALUE_PREFIX);
				
				MethodDeclaration md = HandleBuilder.generateDefaultProvider(bfd.nameOfDefaultProvider, td.typeParameters, fieldNode, ast);
				if (md != null) injectMethod(parent, md);
			}
			addObtainVia(bfd, fieldNode);
			job.builderFields.add(bfd);
			allFields.add(fieldNode);
		}
		
		job.typeParams = td.typeParameters != null ? td.typeParameters : new TypeParameter[0];
		buildMethodReturnType = job.createBuilderParentTypeReference();
		
		// <C, B> are the generics for our builder.
		String classGenericName = "C";
		String builderGenericName = "B";
		// We have to make sure that the generics' names do not collide with any generics on the annotated class,
		// the classname itself, or any member type name of the annotated class.
		// For instance, if there are generics <B, B2, C> on the annotated class, use "C2" and "B3" for our builder.
		java.util.Set<String> usedNames = gatherUsedTypeNames(job.typeParams, td);
		classGenericName = generateNonclashingNameFor(classGenericName, usedNames);
		builderGenericName = generateNonclashingNameFor(builderGenericName, usedNames);
		
		TypeParameter[] paddedTypeParameters; {
			paddedTypeParameters = new TypeParameter[job.typeParams.length + 2];
			System.arraycopy(job.typeParams, 0, paddedTypeParameters, 0, job.typeParams.length);
			
			TypeParameter c = new TypeParameter();
			c.name = classGenericName.toCharArray();
			c.type = cloneSelfType(job.parentType, job.source);
			paddedTypeParameters[paddedTypeParameters.length - 2] = c;
			
			TypeParameter b = new TypeParameter();
			b.name = builderGenericName.toCharArray();
			b.type = cloneSelfType(job.parentType, job.source);
			paddedTypeParameters[paddedTypeParameters.length - 1] = b;
		}
		job.builderTypeParams = job.builderTypeParams_ = paddedTypeParameters;
		
		TypeReference extendsClause = td.superclass;
		TypeReference superclassBuilderClass = null;
		TypeReference[] typeArguments = new TypeReference[] {
			new SingleTypeReference(classGenericName.toCharArray(), 0), 
			new SingleTypeReference(builderGenericName.toCharArray(), 0), 
		};
		if (extendsClause instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference)extendsClause;
			char[] superclassClassName = qualifiedTypeReference.getLastToken();
			String builderClassNameTemplate = BuilderJob.getBuilderClassNameTemplate(annotationNode, null);
			String superclassBuilderClassName = job.replaceBuilderClassName(superclassClassName, builderClassNameTemplate);
			
			char[][] tokens = Arrays.copyOf(qualifiedTypeReference.tokens, qualifiedTypeReference.tokens.length + 1);
			tokens[tokens.length-1] = superclassBuilderClassName.toCharArray();
			long[] poss = new long[tokens.length];
			Arrays.fill(poss, job.getPos());
			
			TypeReference[] superclassTypeArgs = getTypeParametersFrom(extendsClause);
			
			// Every token may potentially have type args. Here, we only have
			// type args for the last token, the superclass' builder.
			TypeReference[][] typeArgsForTokens = new TypeReference[tokens.length][];
			typeArgsForTokens[typeArgsForTokens.length-1] = mergeTypeReferences(superclassTypeArgs, typeArguments);
			
			superclassBuilderClass = new ParameterizedQualifiedTypeReference(tokens, typeArgsForTokens, 0, poss);
		} else if (extendsClause != null) {
			char[] superclassClassName = extendsClause.getTypeName()[0];
			String builderClassNameTemplate = BuilderJob.getBuilderClassNameTemplate(annotationNode, null);
			String superclassBuilderClassName = job.replaceBuilderClassName(superclassClassName, builderClassNameTemplate);
			
			char[][] tokens = new char[][] {superclassClassName, superclassBuilderClassName.toCharArray()};
			long[] poss = new long[tokens.length];
			Arrays.fill(poss, job.getPos());
			
			TypeReference[] superclassTypeArgs = getTypeParametersFrom(extendsClause);
			
			// Every token may potentially have type args. Here, we only have
			// type args for the last token, the superclass' builder.
			TypeReference[][] typeArgsForTokens = new TypeReference[tokens.length][];
			typeArgsForTokens[typeArgsForTokens.length-1] = mergeTypeReferences(superclassTypeArgs, typeArguments);
			
			superclassBuilderClass = new ParameterizedQualifiedTypeReference(tokens, typeArgsForTokens, 0, poss);
		}
		job.builderAbstractClassName = job.builderClassName = job.replaceBuilderClassName(td.name);
		job.builderAbstractClassNameArr = job.builderClassNameArr = job.builderAbstractClassName.toCharArray();
		job.builderImplClassName = job.builderAbstractClassName + "Impl";
		job.builderImplClassNameArr = job.builderImplClassName.toCharArray();
		
		// If there is no superclass, superclassBuilderClassExpression is still == null at this point.
		// You can use it to check whether to inherit or not.
		
		if (!constructorExists(parent, job.builderClassName)) {
			generateBuilderBasedConstructor(job, superclassBuilderClass != null);
		}
		
		// Create the abstract builder class, or reuse an existing one.
		job.builderAbstractType = findInnerClass(parent, job.builderClassName);
		if (job.builderAbstractType == null) {
			job.builderAbstractType = generateBuilderAbstractClass(job, superclassBuilderClass, classGenericName, builderGenericName);
		} else {
			TypeDeclaration builderTypeDeclaration = (TypeDeclaration) job.builderAbstractType.get();
			if ((builderTypeDeclaration.modifiers & (ClassFileConstants.AccStatic | ClassFileConstants.AccAbstract)) == 0) {
				annotationNode.addError("Existing Builder must be an abstract static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(job.builderAbstractType, annotationNode);
			// Generate errors for @Singular BFDs that have one already defined node.
			for (BuilderFieldData bfd : job.builderFields) {
				SingularData sd = bfd.singularData;
				if (sd == null) continue;
				EclipseSingularizer singularizer = sd.getSingularizer();
				if (singularizer == null) continue;
				if (singularizer.checkForAlreadyExistingNodesAndGenerateError(job.builderAbstractType, sd)) {
					bfd.singularData = null;
				}
			}
		}
		
		// Check validity of @ObtainVia fields, and add check if adding cleaning for @Singular is necessary.
		for (BuilderFieldData bfd : job.builderFields) {
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
		job.setBuilderToAbstract();
		generateBuilderFields(job);
		if (addCleaning) {
			FieldDeclaration cleanDecl = new FieldDeclaration(CLEAN_FIELD_NAME, 0, -1);
			cleanDecl.declarationSourceEnd = -1;
			cleanDecl.modifiers = ClassFileConstants.AccPrivate;
			cleanDecl.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
			injectFieldAndMarkGenerated(job.builderType, cleanDecl);
		}
		
		if (job.toBuilder) {
			// Generate $fillValuesFrom() method in the abstract builder.
			injectMethod(job.builderType, generateFillValuesMethod(job, superclassBuilderClass != null, builderGenericName, classGenericName));
			// Generate $fillValuesFromInstanceIntoBuilder() method in the builder implementation class.
			injectMethod(job.builderType, generateStaticFillValuesMethod(job, annInstance.setterPrefix()));
		}
		
		// Create the setter methods in the abstract builder.
		for (BuilderFieldData bfd : job.builderFields) {
			generateSetterMethodsForBuilder(job, bfd, builderGenericName, annInstance.setterPrefix());
		}
		
		// Generate abstract self() and build() methods in the abstract builder.
		injectMethod(job.builderType, generateAbstractSelfMethod(job, superclassBuilderClass != null, builderGenericName));
		job.setBuilderToAbstract();
		injectMethod(job.builderType, generateAbstractBuildMethod(job, superclassBuilderClass != null, classGenericName));
		
		// Create the toString() method for the abstract builder.
		if (methodExists("toString", job.builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			List<Included<EclipseNode, ToString.Include>> fieldNodes = new ArrayList<Included<EclipseNode, ToString.Include>>();
			for (BuilderFieldData bfd : job.builderFields) {
				for (EclipseNode f : bfd.createdFields) {
					fieldNodes.add(new Included<EclipseNode, ToString.Include>(f, null, true, false));
				}
			}
			// Let toString() call super.toString() if there is a superclass, so that it also shows fields from the superclass' builder.
			MethodDeclaration md = HandleToString.createToString(job.builderType, fieldNodes, true, superclassBuilderClass != null, ast, FieldAccess.ALWAYS_FIELD);
			if (md != null) {
				injectMethod(job.builderType, md);
			}
		}
		
		if (addCleaning) {
			job.setBuilderToAbstract();
			injectMethod(job.builderType, generateCleanMethod(job));
		}
		
		boolean isAbstract = (td.modifiers & ClassFileConstants.AccAbstract) != 0;
		if (isAbstract) {
			// Only non-abstract classes get the Builder implementation.
			return;
		}
		
		// Create the builder implementation class, or reuse an existing one.
		job.builderImplType = findInnerClass(parent, job.builderImplClassName);
		if (job.builderImplType == null) {
			job.builderImplType = generateBuilderImplClass(job, job.builderImplClassName);
		} else {
			TypeDeclaration builderImplTypeDeclaration = (TypeDeclaration) job.builderImplType.get();
			if ((builderImplTypeDeclaration.modifiers & ClassFileConstants.AccAbstract) != 0 ||
					(builderImplTypeDeclaration.modifiers & ClassFileConstants.AccStatic) == 0) {
				annotationNode.addError("Existing BuilderImpl must be a non-abstract static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(job.builderImplType, annotationNode);
		}
		
		job.setBuilderToImpl();
		if (job.toBuilder) {
			// Add the toBuilder() method to the annotated class.
			switch (methodExists(TO_BUILDER_METHOD_NAME_STRING, job.parentType, 0)) {
			case EXISTS_BY_USER:
				break;
			case NOT_EXISTS:
				injectMethod(parent, generateToBuilderMethod(job));
				break;
			default:
				// Should not happen.
			}
		}
		
		// Create the self() and build() methods in the BuilderImpl.
		job.setBuilderToImpl();
		injectMethod(job.builderImplType, generateSelfMethod(job));
		
		if (methodExists(job.buildMethodName, job.builderImplType, -1) == MemberExistsResult.NOT_EXISTS) {
			job.setBuilderToImpl();
			injectMethod(job.builderImplType, generateBuildMethod(job, buildMethodReturnType));
		}
		
		// Add the builder() method to the annotated class.
		if (generateBuilderMethod && methodExists(job.builderMethodName, parent, -1) != MemberExistsResult.NOT_EXISTS) generateBuilderMethod = false;
		if (generateBuilderMethod) {
			MethodDeclaration md = generateBuilderMethod(job);
			if (md != null) injectMethod(parent, md);
		}
		
		if (nonFinalNonDefaultedFields != null && generateBuilderMethod) {
			for (EclipseNode fieldNode : nonFinalNonDefaultedFields) {
				fieldNode.addWarning("@SuperBuilder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
		}
	}
	
	private EclipseNode generateBuilderAbstractClass(BuilderJob job, TypeReference superclassBuilderClass, String classGenericName, String builderGenericName) {
		TypeDeclaration parent = (TypeDeclaration) job.parentType.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccStatic | ClassFileConstants.AccAbstract;
		builder.name = job.builderClassNameArr;
		
		// Keep any type params of the annotated class.
		builder.typeParameters = Arrays.copyOf(copyTypeParams(job.typeParams, job.source), job.typeParams.length + 2);
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method, named "C", which extends the annotated class.
		TypeParameter o = new TypeParameter();
		o.name = classGenericName.toCharArray();
		o.type = cloneSelfType(job.parentType, job.source);
		builder.typeParameters[builder.typeParameters.length - 2] = o;
		// 2. The return type for all setter methods, named "B", which extends this builder class.
		o = new TypeParameter();
		o.name = builderGenericName.toCharArray();
		TypeReference[] typerefs = appendBuilderTypeReferences(job.typeParams, classGenericName, builderGenericName);
		o.type = generateParameterizedTypeReference(job.parentType, job.builderClassNameArr, false, typerefs, 0);
		builder.typeParameters[builder.typeParameters.length - 1] = o;
		
		if (superclassBuilderClass != null) builder.superclass = copyType(superclassBuilderClass, job.source);
		
		builder.createDefaultConstructor(false, true);
		
		builder.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		return injectType(job.parentType, builder);
	}
	
	private EclipseNode generateBuilderImplClass(BuilderJob job, String builderImplClass) {
		TypeDeclaration parent = (TypeDeclaration) job.parentType.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal;
		builder.name = builderImplClass.toCharArray();
		
		// Add type params if there are any.
		if (job.typeParams != null && job.typeParams.length > 0) builder.typeParameters = copyTypeParams(job.typeParams, job.source);
		
		if (job.builderClassName != null) {
			// Extend the abstract builder.
			// 1. Add any type params of the annotated class.
			TypeReference[] typeArgs = new TypeReference[job.typeParams.length + 2];
			for (int i = 0; i < job.typeParams.length; i++) {
				typeArgs[i] = new SingleTypeReference(job.typeParams[i].name, 0);
			}
			// 2. The return type for the build() method (named "C" in the abstract builder), which is the annotated class.
			// 3. The return type for all setter methods (named "B" in the abstract builder), which is this builder class.
			typeArgs[typeArgs.length - 2] = cloneSelfType(job.parentType, job.source);
			typeArgs[typeArgs.length - 1] = createTypeReferenceWithTypeParameters(job.parentType, builderImplClass, job.typeParams);
			builder.superclass = generateParameterizedTypeReference(job.parentType, job.builderClassNameArr, false, typeArgs, 0);
		}
		
		builder.createDefaultConstructor(false, true);
		
		builder.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		return injectType(job.parentType, builder);
	}
	
	/**
	 * Generates a constructor that has a builder as the only parameter.
	 * The values from the builder are used to initialize the fields of new instances.
	 *
	 * @param callBuilderBasedSuperConstructor
	 *            If {@code true}, the constructor will explicitly call a super
	 *            constructor with the builder as argument. Requires
	 *            {@code builderClassAsParameter != null}.
	 */
	private void generateBuilderBasedConstructor(BuilderJob job, boolean callBuilderBasedSuperConstructor) {
		TypeDeclaration typeDeclaration = ((TypeDeclaration) job.parentType.get());
		long p = job.getPos();
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(((CompilationUnitDeclaration) job.parentType.top().get()).compilationResult);
		
		constructor.modifiers = toEclipseModifier(AccessLevel.PROTECTED);
		constructor.selector = typeDeclaration.name;
		if (callBuilderBasedSuperConstructor) {
			constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.Super);
			constructor.constructorCall.arguments = new Expression[] {new SingleNameReference(BUILDER_VARIABLE_NAME, p)};
		} else {
			constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		}
		constructor.constructorCall.sourceStart = job.source.sourceStart;
		constructor.constructorCall.sourceEnd = job.source.sourceEnd;
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = job.source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = job.source.sourceEnd;
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND)};
		TypeReference builderType = generateParameterizedTypeReference(job.parentType, job.builderClassNameArr, false, mergeToTypeReferences(job.typeParams, wildcards), p);
		constructor.arguments = new Argument[] {new Argument(BUILDER_VARIABLE_NAME, p, builderType, Modifier.FINAL)};
		
		List<Statement> statements = new ArrayList<Statement>();
		
		for (BuilderFieldData fieldNode : job.builderFields) {
			FieldReference fieldInThis = new FieldReference(fieldNode.rawName, p);
			int s = (int) (p >> 32);
			int e = (int) p;
			fieldInThis.receiver = new ThisReference(s, e);
			
			Expression assignmentExpr;
			if (fieldNode.singularData != null && fieldNode.singularData.getSingularizer() != null) {
				fieldNode.singularData.getSingularizer().appendBuildCode(fieldNode.singularData, job.parentType, statements, fieldNode.builderFieldName, BUILDER_VARIABLE_NAME_STRING);
				assignmentExpr = new SingleNameReference(fieldNode.builderFieldName, p);
			} else {
				char[][] variableInBuilder = new char[][] {BUILDER_VARIABLE_NAME, fieldNode.builderFieldName};
				long[] positions = new long[] {p, p};
				assignmentExpr = new QualifiedNameReference(variableInBuilder, positions, s, e);
			}
			Statement assignment = new Assignment(fieldInThis, assignmentExpr, (int) p);
			
			// In case of @Builder.Default, set the value to the default if it was NOT set in the builder.
			if (fieldNode.nameOfSetFlag != null) {
				char[][] setVariableInBuilder = new char[][] {BUILDER_VARIABLE_NAME, fieldNode.nameOfSetFlag};
				long[] positions = new long[] {p, p};
				QualifiedNameReference setVariableInBuilderRef = new QualifiedNameReference(setVariableInBuilder, positions, s, e);

				MessageSend defaultMethodCall = new MessageSend();
				defaultMethodCall.sourceStart = job.source.sourceStart;
				defaultMethodCall.sourceEnd = job.source.sourceEnd;
				defaultMethodCall.receiver = generateNameReference(job.parentType, 0L);
				defaultMethodCall.selector = fieldNode.nameOfDefaultProvider;
				defaultMethodCall.typeArguments = typeParameterNames(((TypeDeclaration) job.parentType.get()).typeParameters);
				
				Statement defaultAssignment = new Assignment(fieldInThis, defaultMethodCall, (int) p);
				IfStatement ifBlockForDefault = new IfStatement(setVariableInBuilderRef, assignment, defaultAssignment, s, e);
				statements.add(ifBlockForDefault);
			} else {
				statements.add(assignment);
			}
			
			if (hasNonNullAnnotations(fieldNode.originalFieldNode)) {
				Statement nullCheck = generateNullCheck((FieldDeclaration) fieldNode.originalFieldNode.get(), job.sourceNode, null);
				if (nullCheck != null) statements.add(nullCheck);
			}
		}
		
		constructor.statements = statements.isEmpty() ? null : statements.toArray(new Statement[0]);
		if (job.checkerFramework.generateSideEffectFree()) {
			constructor.annotations = new Annotation[] {generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE)};
		}
		
		constructor.traverse(new SetGeneratedByVisitor(job.source), typeDeclaration.scope);
		
		injectMethod(job.parentType, constructor);
	}

	private MethodDeclaration generateBuilderMethod(SuperBuilderJob job) {
		int pS = job.source.sourceStart, pE = job.source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = job.builderMethodName.toCharArray();
		out.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		// Add type params if there are any.
		if (job.typeParams != null && job.typeParams.length > 0) out.typeParameters = copyTypeParams(job.typeParams, job.source);
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND) };
		out.returnType = generateParameterizedTypeReference(job.parentType, job.builderAbstractClassNameArr, false, mergeToTypeReferences(job.typeParams, wildcards), p);
		if (job.checkerFramework.generateUnique()) {
			int len = out.returnType.getTypeName().length;
			out.returnType.annotations = new Annotation[len][];
			out.returnType.annotations[len - 1] = new Annotation[] {generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__UNIQUE)};
		}
		
		AllocationExpression invoke = new AllocationExpression();
		invoke.type = namePlusTypeParamsToTypeReference(job.parentType, job.builderImplClassNameArr, false, job.typeParams, p);
		out.statements = new Statement[] {new ReturnStatement(invoke, pS, pE)};
		if (job.checkerFramework.generateSideEffectFree()) {
			out.annotations = new Annotation[] {generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE)};
		}
		
		createRelevantNonNullAnnotation(job.parentType, out);
		out.traverse(new SetGeneratedByVisitor(job.source), ((TypeDeclaration) job.parentType.get()).scope);
		return out;
	}
	
	/**
	 * Generates a <code>toBuilder()</code> method in the annotated class that looks like this:
	 * <pre>
	 * public <i>Foobar</i>.<i>Foobar</i>Builder&lt;?, ?&gt; toBuilder() {
	 *     return new <i.Foobar</i>.<i>Foobar</i>BuilderImpl().$fillValuesFrom(this);
	 * }
	 * </pre>
	 */
	private MethodDeclaration generateToBuilderMethod(SuperBuilderJob job) {
		int pS = job.source.sourceStart, pE = job.source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = TO_BUILDER_METHOD_NAME;
		out.modifiers = ClassFileConstants.AccPublic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND) };
		out.returnType = generateParameterizedTypeReference(job.parentType, job.builderAbstractClassNameArr, false, mergeToTypeReferences(job.typeParams, wildcards), p);
		if (job.checkerFramework.generateUnique()) {
			int len = out.returnType.getTypeName().length;
			out.returnType.annotations = new Annotation[len][];
			out.returnType.annotations[len - 1] = new Annotation[] {generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__UNIQUE)};
		}
		
		AllocationExpression newClass = new AllocationExpression();
		newClass.type = namePlusTypeParamsToTypeReference(job.parentType, job.builderImplClassNameArr, false, job.typeParams, p);
		MessageSend invokeFillMethod = new MessageSend();
		invokeFillMethod.receiver = newClass;
		invokeFillMethod.selector = FILL_VALUES_METHOD_NAME;
		invokeFillMethod.arguments = new Expression[] {new ThisReference(0, 0)};
		out.statements = new Statement[] {new ReturnStatement(invokeFillMethod, pS, pE)};
		if (job.checkerFramework.generateSideEffectFree()) {
			out.annotations = new Annotation[] {generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE)};
		}
		
		createRelevantNonNullAnnotation(job.parentType, out);
		out.traverse(new SetGeneratedByVisitor(job.source), ((TypeDeclaration) job.parentType.get()).scope);
		return out;
	}

	/**
	 * Generates a <code>$fillValuesFrom()</code> method in the abstract builder class that looks
	 * like this:
	 * <pre>
	 * protected B $fillValuesFrom(final C instance) {
	 *     super.$fillValuesFrom(instance);
	 *     Foobar.FoobarBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
	 *     return self();
	 * }
	 * </pre>
	 */
	private MethodDeclaration generateFillValuesMethod(SuperBuilderJob job, boolean inherited, String builderGenericName, String classGenericName) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = FILL_VALUES_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccProtected;
		if (inherited) out.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, job.parentType.get())};
		out.returnType = new SingleTypeReference(builderGenericName.toCharArray(), 0);
		
		TypeReference builderType = new SingleTypeReference(classGenericName.toCharArray(), 0);
		out.arguments = new Argument[] {new Argument(INSTANCE_VARIABLE_NAME, 0, builderType, Modifier.FINAL)};

		List<Statement> body = new ArrayList<Statement>();

		if (inherited) {
			// Call super.
			MessageSend callToSuper = new MessageSend();
			callToSuper.receiver = new SuperReference(0, 0);
			callToSuper.selector = FILL_VALUES_METHOD_NAME;
			callToSuper.arguments = new Expression[] {new SingleNameReference(INSTANCE_VARIABLE_NAME, 0)};
			body.add(callToSuper);
		}

		// Call the builder implemention's helper method that actually fills the values from the instance.
		MessageSend callStaticFillValuesMethod = new MessageSend();
		callStaticFillValuesMethod.receiver = generateNameReference(job.parentType, job.builderAbstractClassNameArr, 0);
		callStaticFillValuesMethod.selector = FILL_VALUES_STATIC_METHOD_NAME;
		callStaticFillValuesMethod.arguments = new Expression[] {new SingleNameReference(INSTANCE_VARIABLE_NAME, 0), new ThisReference(0, 0)};
		body.add(callStaticFillValuesMethod);

		// Return self().
		MessageSend returnCall = new MessageSend();
		returnCall.receiver = ThisReference.implicitThis();
		returnCall.selector = SELF_METHOD_NAME;
		body.add(new ReturnStatement(returnCall, 0, 0));
		
		out.statements = body.isEmpty() ? null : body.toArray(new Statement[0]);
		
		return out;
	}

	/**
	 * Generates a <code>$fillValuesFromInstanceIntoBuilder()</code> method in
	 * the builder implementation class that copies all fields from the instance
	 * to the builder. It looks like this:
	 * 
	 * <pre>
	 * protected B $fillValuesFromInstanceIntoBuilder(Foobar instance, FoobarBuilder&lt;?, ?&gt; b) {
	 * 	b.field(instance.field);
	 * }
	 * </pre>
	 * @param setterPrefix the prefix for setter methods
	 */
	private MethodDeclaration generateStaticFillValuesMethod(BuilderJob job, String setterPrefix) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = FILL_VALUES_STATIC_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic;
		out.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		
		TypeReference[] wildcards = new TypeReference[] {new Wildcard(Wildcard.UNBOUND), new Wildcard(Wildcard.UNBOUND)};
		TypeReference builderType = generateParameterizedTypeReference(job.parentType, job.builderClassNameArr, false, mergeToTypeReferences(job.typeParams, wildcards), 0);
		Argument builderArgument = new Argument(BUILDER_VARIABLE_NAME, 0, builderType, Modifier.FINAL);
		TypeReference[] typerefs = null;
		if (job.typeParams.length > 0) {
			typerefs = new TypeReference[job.typeParams.length];
			for (int i = 0; i < job.typeParams.length; i++) typerefs[i] = new SingleTypeReference(job.typeParams[i].name, 0);
		}
		
		long p = job.getPos();
		
		TypeReference parentArgument = typerefs == null ? generateTypeReference(job.parentType, p) : generateParameterizedTypeReference(job.parentType, typerefs, p);
		out.arguments = new Argument[] {new Argument(INSTANCE_VARIABLE_NAME, 0, parentArgument, Modifier.FINAL), builderArgument};
		
		// Add type params if there are any.
		if (job.typeParams.length > 0) out.typeParameters = copyTypeParams(job.typeParams, job.source);
		
		List<Statement> body = new ArrayList<Statement>();
		
		// Call the builder's setter methods to fill the values from the instance.
		for (BuilderFieldData bfd : job.builderFields) {
			MessageSend exec = createSetterCallWithInstanceValue(bfd, job.parentType, job.source, setterPrefix);
			body.add(exec);
		}
		
		out.statements = body.isEmpty() ? null : body.toArray(new Statement[0]);
		out.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		
		return out;
	}
	
	private MessageSend createSetterCallWithInstanceValue(BuilderFieldData bfd, EclipseNode type, ASTNode source, String setterPrefix) {
		char[] setterName = HandlerUtil.buildAccessorName(type, setterPrefix, String.valueOf(bfd.name)).toCharArray();
		MessageSend ms = new MessageSend();
		Expression[] tgt = new Expression[bfd.singularData == null ? 1 : 2];
		
		if (bfd.obtainVia == null || !bfd.obtainVia.field().isEmpty()) {
			char[] fieldName = bfd.obtainVia == null ? bfd.rawName : bfd.obtainVia.field().toCharArray();
			for (int i = 0; i < tgt.length; i++) {
				FieldReference fr = new FieldReference(fieldName, 0);
				fr.receiver = new SingleNameReference(INSTANCE_VARIABLE_NAME, 0);
				tgt[i] = fr;
			}
		} else {
			String obtainName = bfd.obtainVia.method();
			boolean obtainIsStatic = bfd.obtainVia.isStatic();
			for (int i = 0; i < tgt.length; i++) {
				MessageSend obtainExpr = new MessageSend();
				obtainExpr.receiver = obtainIsStatic ? generateNameReference(type, 0) : new SingleNameReference(INSTANCE_VARIABLE_NAME, 0);
				obtainExpr.selector = obtainName.toCharArray();
				if (obtainIsStatic) obtainExpr.arguments = new Expression[] {new SingleNameReference(INSTANCE_VARIABLE_NAME, 0)};
				tgt[i] = obtainExpr;
			}
		}
		if (bfd.singularData == null) {
			ms.arguments = tgt;
		} else {
			Expression ifNull = new EqualExpression(tgt[0], new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
			MessageSend emptyCollection = bfd.singularData.getSingularizer().getEmptyExpression(bfd.singularData.getTargetFqn(), bfd.singularData, type, source);
			ms.arguments = new Expression[] {new ConditionalExpression(ifNull, emptyCollection, tgt[1])};
		}
		ms.receiver = new SingleNameReference(BUILDER_VARIABLE_NAME, 0);
		ms.selector = setterName;
		return ms;
	}
	
	private MethodDeclaration generateAbstractSelfMethod(BuilderJob job, boolean override, String builderGenericName) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = SELF_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccAbstract | ClassFileConstants.AccProtected | ExtraCompilerModifiers.AccSemicolonBody;
		Annotation overrideAnn = override ? makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, job.parentType.get()) : null;
		Annotation sefAnn = job.checkerFramework.generatePure() ? generateNamedAnnotation(job.parentType.get(), CheckerFrameworkVersion.NAME__PURE): null;
		if (overrideAnn != null && sefAnn != null) out.annotations = new Annotation[] {overrideAnn, sefAnn};
		else if (overrideAnn != null) out.annotations = new Annotation[] {overrideAnn};
		else if (sefAnn != null) out.annotations = new Annotation[] {sefAnn};
		out.returnType = new SingleTypeReference(builderGenericName.toCharArray(), 0);
		addCheckerFrameworkReturnsReceiver(out.returnType, job.parentType.get(), job.checkerFramework);
		return out;
	}
	
	private MethodDeclaration generateSelfMethod(BuilderJob job) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.selector = SELF_METHOD_NAME;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.modifiers = ClassFileConstants.AccProtected;
		Annotation overrideAnn = makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, job.builderType.get());
		Annotation sefAnn = job.checkerFramework.generatePure() ? generateNamedAnnotation(job.builderType.get(), CheckerFrameworkVersion.NAME__PURE) : null;
		if (sefAnn != null) out.annotations = new Annotation[] {overrideAnn, sefAnn};
		else out.annotations = new Annotation[] {overrideAnn};
		out.returnType = namePlusTypeParamsToTypeReference(job.builderType, job.typeParams, job.getPos());
		addCheckerFrameworkReturnsReceiver(out.returnType, job.parentType.get(), job.checkerFramework);
		out.statements = new Statement[] {new ReturnStatement(new ThisReference(0, 0), 0, 0)};
		return out;
	}
	
	private MethodDeclaration generateAbstractBuildMethod(BuilderJob job, boolean override, String classGenericName) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		out.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract | ExtraCompilerModifiers.AccSemicolonBody;
		out.selector = job.buildMethodName.toCharArray();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = new SingleTypeReference(classGenericName.toCharArray(), 0);
		Annotation overrideAnn = override ? makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, job.source) : null;
		Annotation sefAnn = job.checkerFramework.generateSideEffectFree() ? generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE): null;
		if (overrideAnn != null && sefAnn != null) out.annotations = new Annotation[] {overrideAnn, sefAnn};
		else if (overrideAnn != null) out.annotations = new Annotation[] {overrideAnn};
		else if (sefAnn != null) out.annotations = new Annotation[] {sefAnn};
		out.receiver = HandleBuilder.generateBuildReceiver(job);
		out.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		return out;
	}
	
	private MethodDeclaration generateBuildMethod(BuilderJob job, TypeReference returnType) {
		MethodDeclaration out = job.createNewMethodDeclaration();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		List<Statement> statements = new ArrayList<Statement>();
		
		out.modifiers = ClassFileConstants.AccPublic;
		out.selector = job.buildMethodName.toCharArray();
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = returnType;
		Annotation overrideAnn = makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, job.source);
		Annotation sefAnn = job.checkerFramework.generateSideEffectFree() ? generateNamedAnnotation(job.source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE): null;
		if (sefAnn != null) out.annotations = new Annotation[] {overrideAnn, sefAnn};
		else out.annotations = new Annotation[] {overrideAnn};
		
		AllocationExpression allocationStatement = new AllocationExpression();
		allocationStatement.type = copyType(out.returnType);
		// Use a constructor that only has this builder as parameter.
		allocationStatement.arguments = new Expression[] {new ThisReference(0, 0)};
		statements.add(new ReturnStatement(allocationStatement, 0, 0));
		out.statements = statements.isEmpty() ? null : statements.toArray(new Statement[0]);
		out.receiver = HandleBuilder.generateBuildReceiver(job);
		createRelevantNonNullAnnotation(job.builderType, out);
		out.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		return out;
	}
	
	private MethodDeclaration generateCleanMethod(BuilderJob job) {
		List<Statement> statements = new ArrayList<Statement>();
		
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendCleaningCode(bfd.singularData, job.builderType, statements);
			}
		}
		
		FieldReference thisUnclean = new FieldReference(CLEAN_FIELD_NAME, 0);
		thisUnclean.receiver = new ThisReference(0, 0);
		statements.add(new Assignment(thisUnclean, new FalseLiteral(0, 0), 0));
		MethodDeclaration decl = job.createNewMethodDeclaration();
		//new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		decl.selector = CLEAN_METHOD_NAME;
		decl.modifiers = ClassFileConstants.AccPrivate;
		decl.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		decl.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		decl.statements = statements.toArray(new Statement[0]);
		decl.traverse(new SetGeneratedByVisitor(job.source), (ClassScope) null);
		return decl;
	}
	
	private void generateBuilderFields(BuilderJob job) {
		List<EclipseNode> existing = new ArrayList<EclipseNode>();
		for (EclipseNode child : job.builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.createdFields.addAll(bfd.singularData.getSingularizer().generateFields(bfd.singularData, job.builderType));
			} else {
				EclipseNode field = null, setFlag = null;
				for (EclipseNode exists : existing) {
					char[] n = ((FieldDeclaration) exists.get()).name;
					if (Arrays.equals(n, bfd.builderFieldName)) field = exists;
					if (bfd.nameOfSetFlag != null && Arrays.equals(n, bfd.nameOfSetFlag)) setFlag = exists;
				}
				
				if (field == null) {
					FieldDeclaration fd = new FieldDeclaration(bfd.builderFieldName.clone(), 0, 0);
					fd.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
					fd.modifiers = ClassFileConstants.AccPrivate;
					fd.type = copyType(bfd.type);
					fd.traverse(new SetGeneratedByVisitor(job.source), (MethodScope) null);
					field = injectFieldAndMarkGenerated(job.builderType, fd);
				}
				if (setFlag == null && bfd.nameOfSetFlag != null) {
					FieldDeclaration fd = new FieldDeclaration(bfd.nameOfSetFlag, 0, 0);
					fd.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
					fd.modifiers = ClassFileConstants.AccPrivate;
					fd.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
					fd.traverse(new SetGeneratedByVisitor(job.source), (MethodScope) null);
					injectFieldAndMarkGenerated(job.builderType, fd);
				}
				bfd.createdFields.add(field);
			}
		}
	}
	
	private void generateSetterMethodsForBuilder(BuilderJob job, BuilderFieldData bfd, final String builderGenericName, String setterPrefix) {
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
			generateSimpleSetterMethodForBuilder(job, deprecate, bfd.createdFields.get(0), bfd.name, bfd.nameOfSetFlag, returnTypeMaker.make(), returnStatementMaker.make(), bfd.annotations, bfd.originalFieldNode, setterPrefix);
		} else {
			bfd.singularData.getSingularizer().generateMethods(job.checkerFramework, bfd.singularData, deprecate, job.builderType, true, returnTypeMaker, returnStatementMaker, AccessLevel.PUBLIC);
		}
	}
	
	private void generateSimpleSetterMethodForBuilder(BuilderJob job, boolean deprecate, EclipseNode fieldNode, char[] paramName, char[] nameOfSetFlag, TypeReference returnType, Statement returnStatement, Annotation[] annosOnParam, EclipseNode originalFieldNode, String setterPrefix) {
		TypeDeclaration td = (TypeDeclaration) job.builderType.get();
		AbstractMethodDeclaration[] existing = td.methods;
		if (existing == null) existing = EMPTY_METHODS;
		int len = existing.length;
		
		String setterName = HandlerUtil.buildAccessorName(job.sourceNode, setterPrefix, new String(paramName));
		
		for (int i = 0; i < len; i++) {
			if (!(existing[i] instanceof MethodDeclaration)) continue;
			char[] existingName = existing[i].selector;
			if (Arrays.equals(setterName.toCharArray(), existingName) && !isTolerate(fieldNode, existing[i])) return;
		}
		
		List<Annotation> methodAnnsList = Arrays.asList(EclipseHandlerUtil.findCopyableToSetterAnnotations(originalFieldNode));
		addCheckerFrameworkReturnsReceiver(returnType, job.source, job.checkerFramework);
		MethodDeclaration setter = HandleSetter.createSetter(td, deprecate, fieldNode, setterName, paramName, nameOfSetFlag, returnType, returnStatement, ClassFileConstants.AccPublic,
			job.sourceNode, methodAnnsList, annosOnParam != null ? Arrays.asList(copyAnnotations(job.source, annosOnParam)) : Collections.<Annotation>emptyList());
		if (job.sourceNode.up().getKind() == Kind.METHOD) {
			copyJavadocFromParam(originalFieldNode.up(), setter, td, paramName.toString());
		} else {
			copyJavadoc(originalFieldNode, setter, td, CopyJavadoc.SETTER, true);
		}
		injectMethod(job.builderType, setter);
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
	 * @param setterPrefix the prefix for setter methods 
	 */
	private SingularData getSingularData(EclipseNode node, ASTNode source, String setterPrefix) {
		for (EclipseNode child : node.down()) {
			if (!annotationTypeMatches(Singular.class, child)) continue;
			
			char[] pluralName = node.getKind() == Kind.FIELD ? removePrefixFromField(node) : ((AbstractVariableDeclaration) node.get()).name;
			AnnotationValues<Singular> ann = createAnnotation(Singular.class, child);
			Singular singularInstance = ann.getInstance();
			String explicitSingular = singularInstance.value();
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
			
			return new SingularData(child, singularName, pluralName, typeArgs == null ? Collections.<TypeReference>emptyList() : Arrays.asList(typeArgs), targetFqn, singularizer, source, singularInstance.ignoreNullCollections(), setterPrefix.toCharArray());
		}
		
		return null;
	}
	
	private java.util.Set<String> gatherUsedTypeNames(TypeParameter[] typeParams, TypeDeclaration td) {
		java.util.HashSet<String> usedNames = new HashSet<String>();
		
		// 1. Add type parameter names.
		for (TypeParameter typeParam : typeParams)
			usedNames.add(typeParam.toString());
		
		// 2. Add class name.
		usedNames.add(String.valueOf(td.name));
		
		// 3. Add used type names.
		if (td.fields != null) {
			for (FieldDeclaration field : td.fields) {
				if (field instanceof Initializer) continue; 
				addFirstToken(usedNames, field.type);
			}
		}
		
		// 4. Add extends and implements clauses.
		addFirstToken(usedNames, td.superclass);
		if (td.superInterfaces != null) {
			for (TypeReference typeReference : td.superInterfaces) {
				addFirstToken(usedNames, typeReference);
			}
		}
		
		return usedNames;
	}

	private void addFirstToken(java.util.Set<String> usedNames, TypeReference type) {
		if (type == null) 
			return;
		// Add the first token, because only that can collide.
		char[][] typeName = type.getTypeName();
		if (typeName != null && typeName.length >= 1)
			usedNames.add(String.valueOf(typeName[0]));
	}
	
	private String generateNonclashingNameFor(String classGenericName, java.util.Set<String> typeParamStrings) {
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
	
	private static TypeReference createTypeReferenceWithTypeParameters(EclipseNode parent, String referenceName, TypeParameter[] typeParams) {
		if (typeParams.length > 0) {
			TypeReference[] typerefs = new TypeReference[typeParams.length];
			for (int i = 0; i < typeParams.length; i++) {
				typerefs[i] = new SingleTypeReference(typeParams[i].name, 0);
			}
			return generateParameterizedTypeReference(parent, referenceName.toCharArray(), false, typerefs, 0);
		} else {
			return generateTypeReference(parent, referenceName.toCharArray(), false, 0);
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
	
	private TypeReference[] typeParameterNames(TypeParameter[] typeParameters) {
		if (typeParameters == null) return null;
		
		TypeReference[] trs = new TypeReference[typeParameters.length];
		for (int i = 0; i < trs.length; i++) {
			trs[i] = new SingleTypeReference(typeParameters[i].name, 0);
		}
		return trs;
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
	
	private boolean constructorExists(EclipseNode type, String builderClassName) {
		if (type != null && type.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)type.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (def instanceof ConstructorDeclaration) {
					if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
					if (!def.isConstructor()) continue;
					if (isTolerate(type, def)) continue;
					if (def.arguments == null || def.arguments.length != 1) continue;
					
					// Cannot use typeMatches() here, because the parameter could be fully-qualified, partially-qualified, or not qualified.
					// A string-compare of the last part should work. If it's a false-positive, users could still @Tolerate it.
					char[] typeName = def.arguments[0].type.getLastToken();
					if (builderClassName.equals(String.valueOf(typeName)))
						return true;
				}
			}
		}
		
		return false;
	}
}
