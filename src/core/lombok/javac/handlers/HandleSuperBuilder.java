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
package lombok.javac.handlers;

import static lombok.javac.handlers.HandleBuilder.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.lang.model.element.Modifier;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.ConfigurationKeys;
import lombok.Singular;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleBuilder.BuilderFieldData;
import lombok.javac.handlers.HandleBuilder.BuilderJob;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;
import lombok.javac.handlers.JavacHandlerUtil.JCAnnotatedTypeReflect;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.javac.handlers.JavacSingularsRecipes.ExpressionMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.javac.handlers.JavacSingularsRecipes.StatementMaker;
import lombok.spi.Provides;

@Provides
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleSuperBuilder extends JavacAnnotationHandler<SuperBuilder> {
	private static final String SELF_METHOD = "self";
	private static final String FILL_VALUES_METHOD_NAME = "$fillValuesFrom";
	private static final String STATIC_FILL_VALUES_METHOD_NAME = "$fillValuesFromInstanceIntoBuilder";
	private static final String INSTANCE_VARIABLE_NAME = "instance";
	private static final String BUILDER_VARIABLE_NAME = "b";
	
	class SuperBuilderJob extends BuilderJob {
		JavacNode builderAbstractType;
		String builderAbstractClassName;
		JavacNode builderImplType;
		String builderImplClassName;
		List<JCTypeParameter> builderTypeParams_;
		
		void init(AnnotationValues<SuperBuilder> annValues, SuperBuilder ann, JavacNode node) {
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
		
		void setBuilderToImpl() {
			builderType = builderImplType;
			builderClassName = builderImplClassName;
			builderTypeParams = typeParams;
		}
		
		void setBuilderToAbstract() {
			builderType = builderAbstractType;
			builderClassName = builderAbstractClassName;
			builderTypeParams = builderTypeParams_;
		}
	}
	
	@Override
	public void handle(AnnotationValues<SuperBuilder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.SUPERBUILDER_FLAG_USAGE, "@SuperBuilder");
		SuperBuilderJob job = new SuperBuilderJob();
		job.sourceNode = annotationNode;
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
		
		// Do not delete the SuperBuilder annotation here, we need it for @Jacksonized.
		
		JavacNode parent = annotationNode.up();
		
		job.builderFields = new ArrayList<BuilderFieldData>();
		job.typeParams = List.nil();
		List<JCExpression> buildMethodThrownExceptions = List.nil();
		List<JCExpression> superclassTypeParams = List.nil();
		boolean addCleaning = false;
		
		if (!isClass(parent)) {
			annotationNode.addError("@SuperBuilder is only supported on classes.");
			return;
		}
		if (!isStaticAllowed(parent)) {
			annotationNode.addError("@SuperBuilder is not supported on non-static nested classes.");
			return;
		}
		
		job.parentType = parent;
		JCClassDecl td = (JCClassDecl) parent.get();
		
		// Gather all fields of the class that should be set by the builder.
		ArrayList<JavacNode> nonFinalNonDefaultedFields = null;
		
		boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation("lombok.experimental.Value", parent));
		for (JavacNode fieldNode : HandleConstructor.findAllFields(parent, true)) {
			JCVariableDecl fd = (JCVariableDecl) fieldNode.get();
			JavacNode isDefault = findAnnotation(Builder.Default.class, fieldNode, false);
			boolean isFinal = (fd.mods.flags & Flags.FINAL) != 0 || (valuePresent && !hasAnnotation(NonFinal.class, fieldNode));
			BuilderFieldData bfd = new BuilderFieldData();
			bfd.rawName = fd.name;
			bfd.name = removePrefixFromField(fieldNode);
			bfd.builderFieldName = bfd.name;
			bfd.annotations = findCopyableAnnotations(fieldNode);
			bfd.type = fd.vartype;
			bfd.singularData = getSingularData(fieldNode, annInstance.setterPrefix());
			bfd.originalFieldNode = fieldNode;
			
			if (bfd.singularData != null && isDefault != null) {
				isDefault.addError("@Builder.Default and @Singular cannot be mixed.");
				findAnnotation(Builder.Default.class, fieldNode, true);
				isDefault = null;
			}
			
			if (fd.init == null && isDefault != null) {
				isDefault.addWarning("@Builder.Default requires an initializing expression (' = something;').");
				findAnnotation(Builder.Default.class, fieldNode, true);
				isDefault = null;
			}
			
			if (fd.init != null && isDefault == null) {
				if (isFinal) continue;
				if (nonFinalNonDefaultedFields == null) nonFinalNonDefaultedFields = new ArrayList<JavacNode>();
				nonFinalNonDefaultedFields.add(fieldNode);
			}
			
			if (isDefault != null) {
				bfd.nameOfDefaultProvider = parent.toName(DEFAULT_PREFIX + bfd.name);
				bfd.nameOfSetFlag = parent.toName(bfd.name + SET_PREFIX);
				bfd.builderFieldName = parent.toName(bfd.name + VALUE_PREFIX);
				JCMethodDecl md = HandleBuilder.generateDefaultProvider(bfd.nameOfDefaultProvider, fieldNode, td.typarams, job);
				if (md != null) injectMethod(parent, md);
			}
			addObtainVia(bfd, fieldNode);
			job.builderFields.add(bfd);
		}
		
		job.typeParams = job.builderTypeParams = td.typarams;
		job.builderClassName = job.replaceBuilderClassName(td.name);
		if (!checkName("builderClassName", job.builderClassName, annotationNode)) return;
		
		// <C, B> are the generics for our builder.
		String classGenericName = "C";
		String builderGenericName = "B";
		// We have to make sure that the generics' names do not collide with any generics on the annotated class,
		// the classname itself, or any member type name of the annotated class.
		// For instance, if there are generics <B, B2, C> on the annotated class, use "C2" and "B3" for our builder.
		java.util.HashSet<String> usedNames = gatherUsedTypeNames(job.typeParams, td);
		classGenericName = generateNonclashingNameFor(classGenericName, usedNames);
		builderGenericName = generateNonclashingNameFor(builderGenericName, usedNames);
		
		JavacTreeMaker maker = annotationNode.getTreeMaker();
		
		{
			JCExpression annotatedClass = namePlusTypeParamsToTypeReference(maker, parent, job.typeParams);
			JCTypeParameter c = maker.TypeParameter(parent.toName(classGenericName), List.<JCExpression>of(annotatedClass));
			ListBuffer<JCExpression> typeParamsForBuilder = getTypeParamExpressions(job.typeParams, maker, job.sourceNode);
			typeParamsForBuilder.append(maker.Ident(parent.toName(classGenericName)));
			typeParamsForBuilder.append(maker.Ident(parent.toName(builderGenericName)));
			JCTypeApply typeApply = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, parent, job.getBuilderClassName(), false, List.<JCTypeParameter>nil()), typeParamsForBuilder.toList());
			JCTypeParameter d = maker.TypeParameter(parent.toName(builderGenericName), List.<JCExpression>of(typeApply));
			if (job.typeParams == null || job.typeParams.isEmpty()) {
				job.builderTypeParams_ = List.of(c, d);
			} else {
				job.builderTypeParams_ = job.typeParams.append(c).append(d);
			}
		}
		
		JCTree extendsClause = Javac.getExtendsClause(td);
		JCExpression superclassBuilderClass = null;
		if (extendsClause instanceof JCTypeApply) {
			// Remember the type arguments, because we need them for the extends clause of our abstract builder class.
			superclassTypeParams = ((JCTypeApply) extendsClause).getTypeArguments();
			// A class name with a generics type, e.g., "Superclass<A>".
			extendsClause = ((JCTypeApply) extendsClause).getType();
		}
		if (extendsClause instanceof JCFieldAccess) {
			Name superclassName = ((JCFieldAccess) extendsClause).getIdentifier();
			String builderClassNameTemplate = BuilderJob.getBuilderClassNameTemplate(annotationNode, null);
			String superclassBuilderClassName = job.replaceBuilderClassName(superclassName.toString(), builderClassNameTemplate);
			superclassBuilderClass = parent.getTreeMaker().Select(cloneType(maker, (JCFieldAccess) extendsClause, annotationNode), parent.toName(superclassBuilderClassName));
		} else if (extendsClause != null) {
			String builderClassNameTemplate = BuilderJob.getBuilderClassNameTemplate(annotationNode, null);
			String superclassBuilderClassName = job.replaceBuilderClassName(extendsClause.toString(), builderClassNameTemplate);
			superclassBuilderClass = chainDots(parent, extendsClause.toString(), superclassBuilderClassName);
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
		
		job.builderAbstractClassName = job.builderClassName = job.replaceBuilderClassName(td.name);
		job.builderImplClassName = job.builderAbstractClassName + "Impl";
		
		// Create the abstract builder class.
		job.builderAbstractType = findInnerClass(parent, job.builderClassName);
		if (job.builderAbstractType == null) {
			job.builderAbstractType = generateBuilderAbstractClass(job, superclassBuilderClass, superclassTypeParams, classGenericName, builderGenericName);
			recursiveSetGeneratedBy(job.builderAbstractType.get(), annotationNode);
		} else {
			JCClassDecl builderTypeDeclaration = (JCClassDecl) job.builderAbstractType.get();
			if (!builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)
				|| !builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
				
				annotationNode.addError("Existing Builder must be an abstract static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(job.builderAbstractType, annotationNode);
			// Generate errors for @Singular BFDs that have one already defined node.
			for (BuilderFieldData bfd : job.builderFields) {
				SingularData sd = bfd.singularData;
				if (sd == null) continue;
				JavacSingularizer singularizer = sd.getSingularizer();
				if (singularizer == null) continue;
				if (singularizer.checkForAlreadyExistingNodesAndGenerateError(job.builderAbstractType, sd)) {
					bfd.singularData = null;
				}
			}
		}
		
		// Generate the fields in the abstract builder class that hold the values for the instance.
		job.setBuilderToAbstract();
		generateBuilderFields(job.builderType, job.builderFields, annotationNode);
		if (addCleaning) {
			JCVariableDecl uncleanField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), job.toName("$lombokUnclean"), maker.TypeIdent(CTC_BOOLEAN), null);
			recursiveSetGeneratedBy(uncleanField, annotationNode);
			injectFieldAndMarkGenerated(job.builderType, uncleanField);
		}
		
		if (job.toBuilder) {
			// Generate $fillValuesFrom() method in the abstract builder.
			JCMethodDecl fvm = generateFillValuesMethod(job, superclassBuilderClass != null, builderGenericName, classGenericName);
			recursiveSetGeneratedBy(fvm, annotationNode);
			injectMethod(job.builderType, fvm);
			// Generate $fillValuesFromInstanceIntoBuilder() method in the builder implementation class.
			JCMethodDecl sfvm = generateStaticFillValuesMethod(job, annInstance.setterPrefix());
			recursiveSetGeneratedBy(sfvm, annotationNode);
			injectMethod(job.builderType, sfvm);
		}
		
		// Create the setter methods in the abstract builder.
		for (BuilderFieldData bfd : job.builderFields) {
			generateSetterMethodsForBuilder(job, bfd, builderGenericName, annInstance.setterPrefix());
		}
		
		// Generate abstract self() and build() methods in the abstract builder.
		JCMethodDecl asm = generateAbstractSelfMethod(job, superclassBuilderClass != null, builderGenericName);
		recursiveSetGeneratedBy(asm, annotationNode);
		injectMethod(job.builderType, asm);
		JCMethodDecl abm = generateAbstractBuildMethod(job, superclassBuilderClass != null, classGenericName);
		recursiveSetGeneratedBy(abm, annotationNode);
		injectMethod(job.builderType, abm);
		
		// Create the toString() method for the abstract builder.
		java.util.List<Included<JavacNode, ToString.Include>> fieldNodes = new ArrayList<Included<JavacNode, ToString.Include>>();
		for (BuilderFieldData bfd : job.builderFields) {
			for (JavacNode f : bfd.createdFields) {
				fieldNodes.add(new Included<JavacNode, ToString.Include>(f, null, true, false));
			}
		}
		
		// Let toString() call super.toString() if there is a superclass, so that it also shows fields from the superclass' builder.
		JCMethodDecl toStringMethod = HandleToString.createToString(job.builderType, fieldNodes, true, superclassBuilderClass != null, FieldAccess.ALWAYS_FIELD, annotationNode);
		if (toStringMethod != null) injectMethod(job.builderType, toStringMethod);
		
		// If clean methods are requested, add them now.
		if (addCleaning) {
			JCMethodDecl md = generateCleanMethod(job.builderFields, job.builderType, annotationNode);
			recursiveSetGeneratedBy(md, annotationNode);
			injectMethod(job.builderType, md);
		}
		
		boolean isAbstract = (td.mods.flags & Flags.ABSTRACT) != 0;
		if (!isAbstract) {
			// Only non-abstract classes get the Builder implementation.
			
			// Create the builder implementation class.
			job.builderImplType = findInnerClass(parent, job.builderImplClassName);
			if (job.builderImplType == null) {
				job.builderImplType = generateBuilderImplClass(job);
				recursiveSetGeneratedBy(job.builderImplType.get(), annotationNode);
			} else {
				JCClassDecl builderImplTypeDeclaration = (JCClassDecl) job.builderImplType.get();
				if (!builderImplTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)
						|| builderImplTypeDeclaration.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
					annotationNode.addError("Existing BuilderImpl must be a non-abstract static inner class.");
					return;
				}
				sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(job.builderImplType, annotationNode);
			}

			// Create a simple constructor for the BuilderImpl class.
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PRIVATE, List.<JCAnnotation>nil(), job.builderImplType, List.<JavacNode>nil(), false, annotationNode);
			if (cd != null) injectMethod(job.builderImplType, cd);
			job.setBuilderToImpl();
			
			// Create the self() and build() methods in the BuilderImpl.
			JCMethodDecl selfMethod = generateSelfMethod(job);
			recursiveSetGeneratedBy(selfMethod, annotationNode);
			injectMethod(job.builderType, selfMethod);
			if (methodExists(job.buildMethodName, job.builderType, -1) == MemberExistsResult.NOT_EXISTS) {
				JCMethodDecl buildMethod = generateBuildMethod(job, buildMethodThrownExceptions);
				recursiveSetGeneratedBy(buildMethod, annotationNode);
				injectMethod(job.builderType, buildMethod);
			}
		}
		
		// Generate a constructor in the annotated class that takes a builder as argument.
		if (!constructorExists(job.parentType, job.builderAbstractClassName)) {
			job.setBuilderToAbstract();
			generateBuilderBasedConstructor(job, superclassBuilderClass != null);
		}
		
		if (!isAbstract) {
			// Only non-abstract classes get the builder() and toBuilder() methods.
			
			// Add the builder() method to the annotated class.
			// Allow users to specify their own builder() methods, e.g., to provide default values.
			if (generateBuilderMethod && methodExists(job.builderMethodName, job.parentType, -1) != MemberExistsResult.NOT_EXISTS) generateBuilderMethod = false;
			if (generateBuilderMethod) {
				JCMethodDecl builderMethod = generateBuilderMethod(job);
				if (builderMethod != null) {
					recursiveSetGeneratedBy(builderMethod, annotationNode);
					injectMethod(job.parentType, builderMethod);
				}
			}
	
			// Add the toBuilder() method to the annotated class.
			if (job.toBuilder) {
				switch (methodExists(TO_BUILDER_METHOD_NAME, job.parentType, 0)) {
				case EXISTS_BY_USER:
					break;
				case NOT_EXISTS:
					JCMethodDecl md = generateToBuilderMethod(job);
					if (md != null) {
						recursiveSetGeneratedBy(md, annotationNode);
						injectMethod(job.parentType, md);
					}
					break;
				default:
					// Should not happen.
				}
			}
		}
		
		if (nonFinalNonDefaultedFields != null && generateBuilderMethod) {
			for (JavacNode fieldNode : nonFinalNonDefaultedFields) {
				fieldNode.addWarning("@SuperBuilder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
		}
	}
	
	/**
	 * Creates and returns the abstract builder class and injects it into the annotated class.
	 */
	private JavacNode generateBuilderAbstractClass(SuperBuilderJob job, JCExpression superclassBuilderClass, List<JCExpression> superclassTypeParams, String classGenericName, String builderGenericName) {
		JavacTreeMaker maker = job.parentType.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.ABSTRACT | Flags.PUBLIC);
		
		// Keep any type params of the annotated class.
		ListBuffer<JCTypeParameter> allTypeParams = new ListBuffer<JCTypeParameter>();
		allTypeParams.appendList(copyTypeParams(job.sourceNode, job.typeParams));
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method, named "C", which extends the annotated class.
		JCExpression annotatedClass = namePlusTypeParamsToTypeReference(maker, job.parentType, job.typeParams);
		
		allTypeParams.append(maker.TypeParameter(job.toName(classGenericName), List.<JCExpression>of(annotatedClass)));
		// 2. The return type for all setter methods, named "B", which extends this builder class.
		Name builderClassName = job.toName(job.builderClassName);
		ListBuffer<JCExpression> typeParamsForBuilder = getTypeParamExpressions(job.typeParams, maker, job.sourceNode);
		typeParamsForBuilder.append(maker.Ident(job.toName(classGenericName)));
		typeParamsForBuilder.append(maker.Ident(job.toName(builderGenericName)));
		JCTypeApply typeApply = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, job.parentType, builderClassName, false, List.<JCTypeParameter>nil()), typeParamsForBuilder.toList());
		allTypeParams.append(maker.TypeParameter(job.toName(builderGenericName), List.<JCExpression>of(typeApply)));
		
		JCExpression extending = null;
		if (superclassBuilderClass != null) {
			// If the annotated class extends another class, we want this builder to extend the builder of the superclass.
			// 1. Add the type parameters of the superclass.
			typeParamsForBuilder = getTypeParamExpressions(superclassTypeParams, maker, job.sourceNode);
			// 2. Add the builder type params <C, B>.
			typeParamsForBuilder.append(maker.Ident(job.toName(classGenericName)));
			typeParamsForBuilder.append(maker.Ident(job.toName(builderGenericName)));
			extending = maker.TypeApply(superclassBuilderClass, typeParamsForBuilder.toList());
		}
		
		JCClassDecl builder = maker.ClassDef(mods, builderClassName, allTypeParams.toList(), extending, List.<JCExpression>nil(), List.<JCTree>nil());
		recursiveSetGeneratedBy(builder, job.sourceNode);
		return injectType(job.parentType, builder);
	}
	
	/**
	 * Creates and returns the concrete builder implementation class and injects it into the annotated class.
	 */
	private JavacNode generateBuilderImplClass(SuperBuilderJob job) {
		JavacTreeMaker maker = job.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.PRIVATE | Flags.FINAL);
		
		// Extend the abstract builder.
		JCExpression extending = namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderAbstractClassName), false, List.<JCTypeParameter>nil());
		
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method (named "C" in the abstract builder), which is the annotated class.
		JCExpression annotatedClass = namePlusTypeParamsToTypeReference(maker, job.parentType, job.typeParams);
		// 2. The return type for all setter methods (named "B" in the abstract builder), which is this builder class.
		JCExpression builderImplClassExpression = namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderImplClassName), false, job.typeParams);
		
		ListBuffer<JCExpression> typeParamsForBuilder = getTypeParamExpressions(job.typeParams, maker, job.sourceNode);
		typeParamsForBuilder.append(annotatedClass);
		typeParamsForBuilder.append(builderImplClassExpression);
		extending = maker.TypeApply(extending, typeParamsForBuilder.toList());
		
		JCClassDecl builder = maker.ClassDef(mods, job.toName(job.builderImplClassName), copyTypeParams(job.parentType, job.typeParams), extending, List.<JCExpression>nil(), List.<JCTree>nil());
		recursiveSetGeneratedBy(builder, job.sourceNode);
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
	private void generateBuilderBasedConstructor(SuperBuilderJob job, boolean callBuilderBasedSuperConstructor) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		AccessLevel level = AccessLevel.PROTECTED;
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		Name builderVariableName = job.toName(BUILDER_VARIABLE_NAME);
		for (BuilderFieldData bfd : job.builderFields) {
			JCExpression rhs;
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, bfd.originalFieldNode, job.sourceNode, statements, bfd.builderFieldName, "b");
				rhs = maker.Ident(bfd.singularData.getPluralName());
			} else {
				rhs = maker.Select(maker.Ident(builderVariableName), bfd.builderFieldName);
			}
			JCFieldAccess fieldInThis = maker.Select(maker.Ident(job.toName("this")), bfd.rawName);
			
			JCStatement assign = maker.Exec(maker.Assign(fieldInThis, rhs));
			
			// In case of @Builder.Default, set the value to the default if it was not set in the builder.
			if (bfd.nameOfSetFlag != null) {
				JCFieldAccess setField = maker.Select(maker.Ident(builderVariableName), bfd.nameOfSetFlag);
				fieldInThis = maker.Select(maker.Ident(job.toName("this")), bfd.rawName);
				JCExpression parentTypeRef = namePlusTypeParamsToTypeReference(maker, job.parentType, List.<JCTypeParameter>nil());
				JCAssign assignDefault = maker.Assign(fieldInThis, maker.Apply(typeParameterNames(maker, ((JCClassDecl) job.parentType.get()).typarams), maker.Select(parentTypeRef, bfd.nameOfDefaultProvider), List.<JCExpression>nil()));
				statements.append(maker.If(setField, assign, maker.Exec(assignDefault)));
			} else {
				statements.append(assign);
			}
			
			if (hasNonNullAnnotations(bfd.originalFieldNode)) {
				JCStatement nullCheck = generateNullCheck(maker, bfd.originalFieldNode, job.sourceNode);
				if (nullCheck != null) statements.append(nullCheck);
			}
		}
		
		List<JCAnnotation> annsOnMethod = job.checkerFramework.generateSideEffectFree() ? List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil())) : List.<JCAnnotation>nil();
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), annsOnMethod);
		
		// Create a constructor that has just the builder as parameter.
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, job.getContext());
		// First add all generics that are present on the parent type.
		ListBuffer<JCExpression> typeParamsForBuilderParameter = getTypeParamExpressions(job.typeParams, maker, job.sourceNode);
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.append(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.append(wildcard);
		JCTypeApply paramType = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), false, List.<JCTypeParameter>nil()), typeParamsForBuilderParameter.toList());
		JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), builderVariableName, paramType, null);
		params.append(param);
		
		if (callBuilderBasedSuperConstructor) {
			// The first statement must be the call to the super constructor.
			JCMethodInvocation callToSuperConstructor = maker.Apply(List.<JCExpression>nil(),
					maker.Ident(job.toName("super")),
					List.<JCExpression>of(maker.Ident(builderVariableName)));
			statements.prepend(maker.Exec(callToSuperConstructor));
		}
		
		JCMethodDecl constr = recursiveSetGeneratedBy(maker.MethodDef(mods, job.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, statements.toList()), null), job.sourceNode);
		
		injectMethod(job.parentType, constr);
	}
	
	private JCMethodDecl generateBuilderMethod(SuperBuilderJob job) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		JCExpression call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderImplClassName), false, job.typeParams), List.<JCExpression>nil(), null);
		JCStatement statement = maker.Return(call);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PUBLIC;
		modifiers |= Flags.STATIC;
		
		// Add any type params of the annotated class to the return type.
		ListBuffer<JCExpression> typeParameterNames = new ListBuffer<JCExpression>();
		typeParameterNames.appendList(typeParameterNames(maker, job.typeParams));
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.append(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.append(wildcard);
		// And return type annotations.
		List<JCAnnotation> annsOnParamType = List.nil();
		if (job.checkerFramework.generateUnique()) annsOnParamType = List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__UNIQUE), List.<JCExpression>nil()));
		JCTypeApply returnType = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderAbstractClassName), false, List.<JCTypeParameter>nil(), annsOnParamType), typeParameterNames.toList());
		
		List<JCAnnotation> annsOnMethod = job.checkerFramework.generateSideEffectFree() ? List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil())) : List.<JCAnnotation>nil();
		JCMethodDecl methodDef = maker.MethodDef(maker.Modifiers(modifiers, annsOnMethod), job.toName(job.builderMethodName), returnType, copyTypeParams(job.sourceNode, job.typeParams), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		createRelevantNonNullAnnotation(job.parentType, methodDef);
		return methodDef;
	}
	
	/**
	 * Generates a <code>toBuilder()</code> method in the annotated class that looks like this:
	 * <pre>
	 * public ParentBuilder&lt;?, ?&gt; toBuilder() {
	 *     return new <i>Foobar</i>BuilderImpl().$fillValuesFrom(this);
	 * }
	 * </pre>
	 */
	private JCMethodDecl generateToBuilderMethod(SuperBuilderJob job) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		JCExpression newClass = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderImplClassName), false, job.typeParams), List.<JCExpression>nil(), null);
		List<JCExpression> methodArgs = List.<JCExpression>of(maker.Ident(job.toName("this")));
		JCMethodInvocation invokeFillMethod = maker.Apply(List.<JCExpression>nil(), maker.Select(newClass, job.toName(FILL_VALUES_METHOD_NAME)), methodArgs);
		JCStatement statement = maker.Return(invokeFillMethod);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PUBLIC;
		
		// Add any type params of the annotated class to the return type.
		ListBuffer<JCExpression> typeParameterNames = new ListBuffer<JCExpression>();
		typeParameterNames.appendList(typeParameterNames(maker, job.typeParams));
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.append(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.append(wildcard);
		JCTypeApply returnType = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderAbstractClassName), false, List.<JCTypeParameter>nil()), typeParameterNames.toList());
		
		List<JCAnnotation> annsOnMethod = job.checkerFramework.generateSideEffectFree() ? List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil())) : List.<JCAnnotation>nil();
		JCMethodDecl methodDef = maker.MethodDef(maker.Modifiers(modifiers, annsOnMethod), job.toName(TO_BUILDER_METHOD_NAME), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		createRelevantNonNullAnnotation(job.parentType, methodDef);
		return methodDef;
	}

	/**
	 * Generates a <code>$fillValuesFrom()</code> method in the abstract builder class that looks
	 * like this:
	 * <pre>
	 * protected B $fillValuesFrom(final C instance) {
	 *     super.$fillValuesFrom(instance);
	 *     FoobarBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
	 *     return self();
	 * }
	 * </pre>
	 */
	private JCMethodDecl generateFillValuesMethod(SuperBuilderJob job, boolean inherited, String builderGenericName, String classGenericName) {
		JavacTreeMaker maker = job.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		if (inherited) {
			JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(job.builderType, "Override"), List.<JCExpression>nil());
			annotations = List.of(overrideAnnotation);
		}
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED, annotations);
		Name name = job.toName(FILL_VALUES_METHOD_NAME);
		JCExpression returnType = maker.Ident(job.toName(builderGenericName));
		
		JCExpression classGenericNameExpr = maker.Ident(job.toName(classGenericName));
		JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.PARAMETER | Flags.FINAL), job.toName(INSTANCE_VARIABLE_NAME), classGenericNameExpr, null);

		ListBuffer<JCStatement> body = new ListBuffer<JCStatement>();
		
		if (inherited) {
			// Call super.
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
				maker.Select(maker.Ident(job.toName("super")), name),
				List.<JCExpression>of(maker.Ident(job.toName(INSTANCE_VARIABLE_NAME))));
			body.append(maker.Exec(callToSuper));
		}
		
		// Call the builder implemention's helper method that actually fills the values from the instance.
		JCExpression ref = namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), false, List.<JCTypeParameter>nil());
		JCMethodInvocation callStaticFillValuesMethod = maker.Apply(List.<JCExpression>nil(),
			maker.Select(ref, job.toName(STATIC_FILL_VALUES_METHOD_NAME)),
			List.<JCExpression>of(maker.Ident(job.toName(INSTANCE_VARIABLE_NAME)), maker.Ident(job.toName("this"))));
		body.append(maker.Exec(callStaticFillValuesMethod));
		
		JCReturn returnStatement = maker.Return(maker.Apply(List.<JCExpression>nil(), maker.Ident(job.toName(SELF_METHOD)), List.<JCExpression>nil()));
		body.append(returnStatement);
		JCBlock bodyBlock = maker.Block(0, body.toList());
		
		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.of(param), List.<JCExpression>nil(), bodyBlock, null);
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
	 */
	private JCMethodDecl generateStaticFillValuesMethod(SuperBuilderJob job, String setterPrefix) {
		JavacTreeMaker maker = job.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		JCModifiers modifiers = maker.Modifiers(Flags.PRIVATE | Flags.STATIC, annotations);
		Name name = job.toName(STATIC_FILL_VALUES_METHOD_NAME);
		JCExpression returnType = maker.TypeIdent(CTC_VOID);
		
		// 1st parameter: "Foobar instance"
		JCVariableDecl paramInstance = maker.VarDef(maker.Modifiers(Flags.PARAMETER | Flags.FINAL), job.toName(INSTANCE_VARIABLE_NAME), cloneSelfType(job.parentType), null);

		// 2nd parameter: "FoobarBuilder<?, ?> b" (plus generics on the annotated type)
		// First add all generics that are present on the parent type.
		ListBuffer<JCExpression> typeParamsForBuilderParameter = getTypeParamExpressions(job.typeParams, maker, job.sourceNode);
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.append(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.append(wildcard);
		JCTypeApply builderType = maker.TypeApply(namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), false, List.<JCTypeParameter>nil()), typeParamsForBuilderParameter.toList());
		JCVariableDecl paramBuilder = maker.VarDef(maker.Modifiers(Flags.PARAMETER | Flags.FINAL), job.toName(BUILDER_VARIABLE_NAME), builderType, null);
		
		ListBuffer<JCStatement> body = new ListBuffer<JCStatement>();
		
		// Call the builder's setter methods to fill the values from the instance.
		for (BuilderFieldData bfd : job.builderFields) {
			JCExpressionStatement exec = createSetterCallWithInstanceValue(bfd, job, setterPrefix);
			body.append(exec);
		}
		
		JCBlock bodyBlock = maker.Block(0, body.toList());
		
		return maker.MethodDef(modifiers, name, returnType, copyTypeParams(job.builderType, job.typeParams), List.of(paramInstance, paramBuilder), List.<JCExpression>nil(), bodyBlock, null);
	}
	
	private JCExpressionStatement createSetterCallWithInstanceValue(BuilderFieldData bfd, SuperBuilderJob job, String setterPrefix) {
		JavacTreeMaker maker = job.getTreeMaker();
		JCExpression[] tgt = new JCExpression[bfd.singularData == null ? 1 : 2];
		if (bfd.obtainVia == null || !bfd.obtainVia.field().isEmpty()) {
			for (int i = 0; i < tgt.length; i++) {
				tgt[i] = maker.Select(maker.Ident(job.toName(INSTANCE_VARIABLE_NAME)), bfd.obtainVia == null ? bfd.rawName : job.toName(bfd.obtainVia.field()));
			}
		} else {
			if (bfd.obtainVia.isStatic()) {
				for (int i = 0; i < tgt.length; i++) {
					JCExpression typeRef = namePlusTypeParamsToTypeReference(maker, job.parentType, List.<JCTypeParameter>nil());
					JCExpression c = maker.Select(typeRef, job.toName(bfd.obtainVia.method()));
					tgt[i] = maker.Apply(List.<JCExpression>nil(), c, List.<JCExpression>of(maker.Ident(job.toName(INSTANCE_VARIABLE_NAME))));
				}
			} else {
				for (int i = 0; i < tgt.length; i++) {
					JCExpression c = maker.Select(maker.Ident(job.toName(INSTANCE_VARIABLE_NAME)), job.toName(bfd.obtainVia.method()));
					tgt[i] = maker.Apply(List.<JCExpression>nil(), c, List.<JCExpression>nil());
				}
			}
		}
		
		JCExpression arg;
		if (bfd.singularData == null) {
			arg = tgt[0];
		} else {
			JCExpression eqNull = maker.Binary(CTC_EQUAL, tgt[0], maker.Literal(CTC_BOT, null));
			JCExpression emptyCollection = bfd.singularData.getSingularizer().getEmptyExpression(bfd.singularData.getTargetFqn(), maker, bfd.singularData, job.parentType, job.sourceNode);
			arg = maker.Conditional(eqNull, emptyCollection, tgt[1]);
		}
		
		String setterName = HandlerUtil.buildAccessorName(job.sourceNode, setterPrefix, bfd.name.toString());
		JCMethodInvocation apply = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(job.toName(BUILDER_VARIABLE_NAME)), job.toName(setterName)), List.of(arg));
		JCExpressionStatement exec = maker.Exec(apply);
		return exec;
	}
	
	private JCMethodDecl generateAbstractSelfMethod(SuperBuilderJob job, boolean override, String builderGenericName) {
		JavacTreeMaker maker = job.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		JCAnnotation overrideAnnotation = override ? maker.Annotation(genJavaLangTypeRef(job.builderType, "Override"), List.<JCExpression>nil()) : null;
		JCAnnotation sefAnnotation = job.checkerFramework.generatePure() ? maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__PURE), List.<JCExpression>nil()) : null;
		if (sefAnnotation != null) annotations = annotations.prepend(sefAnnotation);
		if (overrideAnnotation != null) annotations = annotations.prepend(overrideAnnotation);
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED | Flags.ABSTRACT, annotations);
		Name name = job.toName(SELF_METHOD);
		JCExpression returnType = maker.Ident(job.toName(builderGenericName));
		returnType = addCheckerFrameworkReturnsReceiver(returnType, maker, job.builderType, job.checkerFramework);

		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
	}
	
	private JCMethodDecl generateSelfMethod(SuperBuilderJob job) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(job.builderType, "Override"), List.<JCExpression>nil());
		JCAnnotation sefAnnotation = job.checkerFramework.generatePure() ? maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__PURE), List.<JCExpression>nil()) : null;
		List<JCAnnotation> annsOnMethod = List.nil();
		if (sefAnnotation != null) annsOnMethod = annsOnMethod.prepend(sefAnnotation);
		annsOnMethod = annsOnMethod.prepend(overrideAnnotation);
		
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED, annsOnMethod);
		Name name = job.toName(SELF_METHOD);
		
		JCExpression returnType = namePlusTypeParamsToTypeReference(maker, job.builderType.up(), job.getBuilderClassName(), false, job.typeParams);
		returnType = addCheckerFrameworkReturnsReceiver(returnType, maker, job.builderType, job.checkerFramework);
		JCStatement statement = maker.Return(maker.Ident(job.toName("this")));
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		
		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	private JCMethodDecl generateAbstractBuildMethod(SuperBuilderJob job, boolean override, String classGenericName) {
		JavacTreeMaker maker = job.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		if (override) {
			JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(job.builderType, "Override"), List.<JCExpression>nil());
			annotations = List.of(overrideAnnotation);
		}
		if (job.checkerFramework.generateSideEffectFree()) annotations = annotations.prepend(maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		JCModifiers modifiers = maker.Modifiers(Flags.PUBLIC | Flags.ABSTRACT, annotations);
		Name name = job.toName(job.buildMethodName);
		JCExpression returnType = maker.Ident(job.toName(classGenericName));
		
		JCVariableDecl recv = HandleBuilder.generateReceiver(job);
		JCMethodDecl methodDef;
		if (recv != null && maker.hasMethodDefWithRecvParam()) {
			methodDef = maker.MethodDefWithRecvParam(modifiers, name, returnType, List.<JCTypeParameter>nil(), recv, List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
		} else {
			methodDef = maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
		}
		return methodDef;
	}
	
	private JCMethodDecl generateBuildMethod(SuperBuilderJob job, List<JCExpression> thrownExceptions) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		JCExpression call;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		// Use a constructor that only has this builder as parameter.
		List<JCExpression> builderArg = List.<JCExpression>of(maker.Ident(job.toName("this")));
		call = maker.NewClass(null, List.<JCExpression>nil(), cloneSelfType(job.parentType), builderArg, null);
		statements.append(maker.Return(call));
		
		JCBlock body = maker.Block(0, statements.toList());
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(job.builderType, "Override"), List.<JCExpression>nil());
		List<JCAnnotation> annsOnMethod = List.of(overrideAnnotation);
		if (job.checkerFramework.generateSideEffectFree()) annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		JCModifiers modifiers = maker.Modifiers(Flags.PUBLIC, annsOnMethod);
		
		JCVariableDecl recv = HandleBuilder.generateReceiver(job);
		JCMethodDecl methodDef;
		if (recv != null && maker.hasMethodDefWithRecvParam()) {
			methodDef = maker.MethodDefWithRecvParam(modifiers, job.toName(job.buildMethodName), cloneSelfType(job.parentType), List.<JCTypeParameter>nil(), recv, List.<JCVariableDecl>nil(), thrownExceptions, body, null);
		} else {
			methodDef = maker.MethodDef(modifiers, job.toName(job.buildMethodName), cloneSelfType(job.parentType), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
		}
		createRelevantNonNullAnnotation(job.builderType, methodDef);
		return methodDef;
	}
	
	private JCMethodDecl generateCleanMethod(java.util.List<BuilderFieldData> builderFields, JavacNode type, JavacNode source) {
		JavacTreeMaker maker = type.getTreeMaker();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendCleaningCode(bfd.singularData, type, source, statements);
			}
		}
		
		statements.append(maker.Exec(maker.Assign(maker.Select(maker.Ident(type.toName("this")), type.toName("$lombokUnclean")), maker.Literal(CTC_BOOLEAN, 0))));
		JCBlock body = maker.Block(0, statements.toList());
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), type.toName("$lombokClean"), maker.Type(Javac.createVoidType(type.getSymbolTable(), CTC_VOID)), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	private void generateBuilderFields(JavacNode builderType, java.util.List<BuilderFieldData> builderFields, JavacNode source) {
		int len = builderFields.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		java.util.List<JCVariableDecl> generated = new ArrayList<JCVariableDecl>();
		
		for (int i = len - 1; i >= 0; i--) {
			BuilderFieldData bfd = builderFields.get(i);
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				java.util.List<JavacNode> fields = bfd.singularData.getSingularizer().generateFields(bfd.singularData, builderType, source);
				for (JavacNode field : fields) {
					generated.add((JCVariableDecl) field.get());
				}
				bfd.createdFields.addAll(fields);
			} else {
				JavacNode field = null, setFlag = null;
				for (JavacNode exists : existing) {
					Name n = ((JCVariableDecl) exists.get()).name;
					if (n.equals(bfd.builderFieldName)) field = exists;
					if (n.equals(bfd.nameOfSetFlag)) setFlag = exists;
				}
				JavacTreeMaker maker = builderType.getTreeMaker();
				if (field == null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.builderFieldName, cloneType(maker, bfd.type, source), null);
					field = injectFieldAndMarkGenerated(builderType, newField);
					generated.add(newField);
				}
				if (setFlag == null && bfd.nameOfSetFlag != null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.nameOfSetFlag, maker.TypeIdent(CTC_BOOLEAN), null);
					injectFieldAndMarkGenerated(builderType, newField);
					generated.add(newField);
				}
				bfd.createdFields.add(field);
			}
		}
		for (JCVariableDecl gen : generated)  recursiveSetGeneratedBy(gen, source);
	}
	
	private void generateSetterMethodsForBuilder(final SuperBuilderJob job, BuilderFieldData fieldNode, final String builderGenericName, String setterPrefix) {
		boolean deprecate = isFieldDeprecated(fieldNode.originalFieldNode);
		final JavacTreeMaker maker = job.getTreeMaker();
		ExpressionMaker returnTypeMaker = new ExpressionMaker() { @Override public JCExpression make() {
			return maker.Ident(job.toName(builderGenericName));
		}};
		
		StatementMaker returnStatementMaker = new StatementMaker() { @Override public JCStatement make() {
			return maker.Return(maker.Apply(List.<JCExpression>nil(), maker.Ident(job.toName(SELF_METHOD)), List.<JCExpression>nil()));
		}};
		
		if (fieldNode.singularData == null || fieldNode.singularData.getSingularizer() == null) {
			generateSimpleSetterMethodForBuilder(job, deprecate, fieldNode.createdFields.get(0), fieldNode.name, fieldNode.nameOfSetFlag, returnTypeMaker.make(), returnStatementMaker.make(), fieldNode.annotations, fieldNode.originalFieldNode, setterPrefix);
		} else {
			fieldNode.singularData.getSingularizer().generateMethods(job.checkerFramework, fieldNode.singularData, deprecate, job.builderType,
				job.sourceNode, true, returnTypeMaker, returnStatementMaker, AccessLevel.PUBLIC);
		}
	}
	
	private void generateSimpleSetterMethodForBuilder(SuperBuilderJob job, boolean deprecate, JavacNode fieldNode, Name paramName, Name nameOfSetFlag, JCExpression returnType, JCStatement returnStatement, List<JCAnnotation> annosOnParam, JavacNode originalFieldNode, String setterPrefix) {
		String setterName = HandlerUtil.buildAccessorName(job.sourceNode, setterPrefix, paramName.toString());
		Name setterName_ = job.builderType.toName(setterName);
		
		for (JavacNode child : job.builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			JCMethodDecl methodDecl = (JCMethodDecl) child.get();
			Name existingName = methodDecl.name;
			if (existingName.equals(setterName_) && !isTolerate(fieldNode, methodDecl)) return;
		}
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		List<JCAnnotation> methodAnns = JavacHandlerUtil.findCopyableToSetterAnnotations(originalFieldNode);
		returnType = addCheckerFrameworkReturnsReceiver(returnType, maker, job.builderType, job.checkerFramework);

		JCMethodDecl newMethod = HandleSetter.createSetter(Flags.PUBLIC, deprecate, fieldNode, maker, setterName, paramName, nameOfSetFlag, returnType, returnStatement, job.sourceNode, methodAnns, annosOnParam);
		if (job.sourceNode.up().getKind() == Kind.METHOD) {
			copyJavadocFromParam(originalFieldNode.up(), newMethod, paramName.toString());
		} else {
			copyJavadoc(originalFieldNode, newMethod, CopyJavadoc.SETTER, true);
		}
		injectMethod(job.builderType, newMethod);
	}
	
	private void addObtainVia(BuilderFieldData bfd, JavacNode node) {
		for (JavacNode child : node.down()) {
			if (!annotationTypeMatches(ObtainVia.class, child)) continue;
			AnnotationValues<ObtainVia> ann = createAnnotation(ObtainVia.class, child);
			bfd.obtainVia = ann.getInstance();
			bfd.obtainViaNode = child;
			deleteAnnotationIfNeccessary(child, ObtainVia.class);
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
	private SingularData getSingularData(JavacNode node, String setterPrefix) {
		for (JavacNode child : node.down()) {
			if (!annotationTypeMatches(Singular.class, child)) continue;
			Name pluralName = node.getKind() == Kind.FIELD ? removePrefixFromField(node) : ((JCVariableDecl) node.get()).name;
			AnnotationValues<Singular> ann = createAnnotation(Singular.class, child);
			Singular singularInstance = ann.getInstance();
			deleteAnnotationIfNeccessary(child, Singular.class);
			String explicitSingular = singularInstance.value();
			if (explicitSingular.isEmpty()) {
				if (Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_AUTO))) {
					node.addError("The singular must be specified explicitly (e.g. @Singular(\"task\")) because auto singularization is disabled.");
					explicitSingular = pluralName.toString();
				} else {
					explicitSingular = autoSingularize(pluralName.toString());
					if (explicitSingular == null) {
						node.addError("Can't singularize this name; please specify the singular explicitly (i.e. @Singular(\"sheep\"))");
						explicitSingular = pluralName.toString();
					}
				}
			}
			Name singularName = node.toName(explicitSingular);
			
			JCExpression type = null;
			if (node.get() instanceof JCVariableDecl) type = ((JCVariableDecl) node.get()).vartype;
			
			String name = null;
			List<JCExpression> typeArgs = List.nil();
			if (type instanceof JCTypeApply) {
				typeArgs = ((JCTypeApply) type).arguments;
				type = ((JCTypeApply) type).clazz;
			}
			
			name = type.toString();
			
			String targetFqn = JavacSingularsRecipes.get().toQualified(name);
			JavacSingularizer singularizer = JavacSingularsRecipes.get().getSingularizer(targetFqn, node);
			if (singularizer == null) {
				node.addError("Lombok does not know how to create the singular-form builder methods for type '" + name + "'; they won't be generated.");
				return null;
			}
			
			return new SingularData(child, singularName, pluralName, typeArgs, targetFqn, singularizer, singularInstance.ignoreNullCollections(), setterPrefix);
		}
		
		return null;
	}
	
	private java.util.HashSet<String> gatherUsedTypeNames(List<JCTypeParameter> typeParams, JCClassDecl td) {
		java.util.HashSet<String> usedNames = new HashSet<String>();
		
		// 1. Add type parameter names.
		for (JCTypeParameter typeParam : typeParams)
			usedNames.add(typeParam.getName().toString());
		
		// 2. Add class name.
		usedNames.add(td.name.toString());
		
		// 3. Add used type names.
		for (JCTree member : td.getMembers()) {
			if (member.getKind() == com.sun.source.tree.Tree.Kind.VARIABLE && member instanceof JCVariableDecl) {
				JCTree type = ((JCVariableDecl)member).getType();
				if (type instanceof JCIdent)
					usedNames.add(((JCIdent)type).getName().toString());
			}
		}
		
		// 4. Add extends and implements clauses.
		addFirstToken(usedNames, Javac.getExtendsClause(td));
		for (JCExpression impl : td.getImplementsClause()) {
			addFirstToken(usedNames, impl);
		}
		
		return usedNames;
	}
	
	private void addFirstToken(java.util.Set<String> usedNames, JCTree type) {
		if (type == null) 
			return;
		if (type instanceof JCTypeApply) {
			type = ((JCTypeApply)type).clazz;
		}
		while (type instanceof JCFieldAccess && ((JCFieldAccess)type).selected != null) {
			// Add the first token, because only that can collide.
			type = ((JCFieldAccess)type).selected;
		}
		usedNames.add(type.toString());
	}
	
	private String generateNonclashingNameFor(String classGenericName, java.util.HashSet<String> typeParamStrings) {
		if (!typeParamStrings.contains(classGenericName)) return classGenericName;
		int counter = 2;
		while (typeParamStrings.contains(classGenericName + counter)) counter++;
		return classGenericName + counter;
	}
	
	private JavacNode findInnerClass(JavacNode parent, String name) {
		for (JavacNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			JCClassDecl td = (JCClassDecl) child.get();
			if (td.name.contentEquals(name)) return child;
		}
		return null;
	}
	
	private ListBuffer<JCExpression> getTypeParamExpressions(List<? extends JCTree> typeParams, JavacTreeMaker maker, JavacNode source) {
		ListBuffer<JCExpression> typeParamsForBuilderParameter = new ListBuffer<JCExpression>();
		for (JCTree typeParam : typeParams) {
			if (typeParam instanceof JCTypeParameter) {
				typeParamsForBuilderParameter.append(maker.Ident(((JCTypeParameter) typeParam).getName()));
			} else if (typeParam instanceof JCIdent) {
				typeParamsForBuilderParameter.append(maker.Ident(((JCIdent) typeParam).getName()));
			} else if (typeParam instanceof JCFieldAccess) {
				typeParamsForBuilderParameter.append(copySelect(maker, (JCFieldAccess) typeParam));
			} else if (typeParam instanceof JCTypeApply) {
				typeParamsForBuilderParameter.append(cloneType(maker, (JCTypeApply) typeParam, source));
			} else if (typeParam instanceof JCArrayTypeTree) {
				typeParamsForBuilderParameter.append(cloneType(maker, (JCArrayTypeTree) typeParam, source));
			} else if (JCAnnotatedTypeReflect.is(typeParam)) {
				typeParamsForBuilderParameter.append(cloneType(maker, (JCExpression) typeParam, source));
			}
		}
		return typeParamsForBuilderParameter;
	}

	private JCExpression copySelect(JavacTreeMaker maker, JCFieldAccess typeParam) {
		java.util.List<Name> chainNames = new ArrayList<Name>();
		JCExpression expression = typeParam;
		while (expression != null) {
			if (expression instanceof JCFieldAccess) {
				chainNames.add(((JCFieldAccess) expression).getIdentifier());
				expression = ((JCFieldAccess) expression).getExpression();
			} else if (expression instanceof JCIdent) {
				chainNames.add(((JCIdent) expression).getName());
				expression = null;
			}
		}

		Collections.reverse(chainNames);
		JCExpression typeParameter = null;
		for (Name name : chainNames) {
			if (typeParameter == null) {
				typeParameter = maker.Ident(name);
			} else {
				typeParameter = maker.Select(typeParameter, name);
			}
		}
		return typeParameter;
	}
	
	/**
	 * Checks if there is a manual constructor in the given type with a single parameter (builder).
	 */
	private boolean constructorExists(JavacNode type, String builderClassName) {
		if (type != null && type.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)type.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					String name = md.name.toString();
					boolean matches = name.equals("<init>");
					if (isTolerate(type, md)) 
						continue;
					if (matches && md.params != null && md.params.length() == 1) {
						// Cannot use typeMatches() here, because the parameter could be fully-qualified, partially-qualified, or not qualified.
						// A string-compare of the last part should work. If it's a false-positive, users could still @Tolerate it.
						String typeName = md.params.get(0).getType().toString();
						int lastIndexOfDot = typeName.lastIndexOf('.');
						if (lastIndexOfDot >= 0) {
							typeName = typeName.substring(lastIndexOfDot+1);
						}
						if ((builderClassName+"<?, ?>").equals(typeName))
							return true;
					}
				}
			}
		}
		return false;
	}
}
