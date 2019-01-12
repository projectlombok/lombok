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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
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
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.javac.handlers.JavacSingularsRecipes.ExpressionMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.javac.handlers.JavacSingularsRecipes.StatementMaker;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleSuperBuilder extends JavacAnnotationHandler<SuperBuilder> {
	private static final String SELF_METHOD = "self";
	private static final String TO_BUILDER_METHOD_NAME = "toBuilder";
	private static final String FILL_VALUES_METHOD_NAME = "$fillValuesFrom";
	private static final String STATIC_FILL_VALUES_METHOD_NAME = "$fillValuesFromInstanceIntoBuilder";
	private static final String INSTANCE_VARIABLE_NAME = "instance";
	private static final String BUILDER_VARIABLE_NAME = "b";

	@Override
	public void handle(AnnotationValues<SuperBuilder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.SUPERBUILDER_FLAG_USAGE, "@SuperBuilder");
		
		SuperBuilder superbuilderAnnotation = annotation.getInstance();
		deleteAnnotationIfNeccessary(annotationNode, SuperBuilder.class);
		
		String builderMethodName = superbuilderAnnotation.builderMethodName();
		String buildMethodName = superbuilderAnnotation.buildMethodName();
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		
		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		
		boolean toBuilder = superbuilderAnnotation.toBuilder();

		JavacNode tdParent = annotationNode.up();
		
		java.util.List<BuilderFieldData> builderFields = new ArrayList<BuilderFieldData>();
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrownExceptions = List.nil();
		List<JCExpression> superclassTypeParams = List.nil();
		
		boolean addCleaning = false;
		
		if (!(tdParent.get() instanceof JCClassDecl)) {
			annotationNode.addError("@SuperBuilder is only supported on types.");
			return;
		}
		
		// Gather all fields of the class that should be set by the builder.
		JCClassDecl td = (JCClassDecl) tdParent.get();
		ListBuffer<JavacNode> allFields = new ListBuffer<JavacNode>();
		boolean valuePresent = (hasAnnotation(lombok.Value.class, tdParent) || hasAnnotation("lombok.experimental.Value", tdParent));
		for (JavacNode fieldNode : HandleConstructor.findAllFields(tdParent, true)) {
			JCVariableDecl fd = (JCVariableDecl) fieldNode.get();
			JavacNode isDefault = findAnnotation(Builder.Default.class, fieldNode, true);
			boolean isFinal = (fd.mods.flags & Flags.FINAL) != 0 || (valuePresent && !hasAnnotation(NonFinal.class, fieldNode));
			BuilderFieldData bfd = new BuilderFieldData();
			bfd.rawName = fd.name;
			bfd.name = removePrefixFromField(fieldNode);
			bfd.annotations = findCopyableAnnotations(fieldNode);
			bfd.type = fd.vartype;
			bfd.singularData = getSingularData(fieldNode);
			bfd.originalFieldNode = fieldNode;
			
			if (bfd.singularData != null && isDefault != null) {
				isDefault.addError("@Builder.Default and @Singular cannot be mixed.");
				isDefault = null;
			}
			
			if (fd.init == null && isDefault != null) {
				isDefault.addWarning("@Builder.Default requires an initializing expression (' = something;').");
				isDefault = null;
			}
			
			if (fd.init != null && isDefault == null) {
				if (isFinal) continue;
				fieldNode.addWarning("@SuperBuilder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
			
			if (isDefault != null) {
				bfd.nameOfDefaultProvider = tdParent.toName("$default$" + bfd.name);
				bfd.nameOfSetFlag = tdParent.toName(bfd.name + "$set");
				bfd.nameOfSetFlag = tdParent.toName(bfd.name + "$set");
				JCMethodDecl md = HandleBuilder.generateDefaultProvider(bfd.nameOfDefaultProvider, fieldNode, td.typarams);
				recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
				if (md != null) injectMethod(tdParent, md);
			}
			addObtainVia(bfd, fieldNode);
			builderFields.add(bfd);
			allFields.append(fieldNode);
		}
		
		// Set the names of the builder classes.
		String builderClassName = td.name.toString() + "Builder";
		String builderImplClassName = builderClassName + "Impl";
		JCTree extendsClause = Javac.getExtendsClause(td);
		JCExpression superclassBuilderClassExpression = null;
		if (extendsClause instanceof JCTypeApply) {
			// Remember the type arguments, because we need them for the extends clause of our abstract builder class.
			superclassTypeParams = ((JCTypeApply) extendsClause).getTypeArguments();
			// A class name with a generics type, e.g., "Superclass<A>".
			extendsClause = ((JCTypeApply) extendsClause).getType();
		}
		if (extendsClause instanceof JCFieldAccess) {
			Name superclassClassName = ((JCFieldAccess)extendsClause).getIdentifier();
			String superclassBuilderClassName = superclassClassName + "Builder";
			superclassBuilderClassExpression = tdParent.getTreeMaker().Select((JCFieldAccess) extendsClause,
					tdParent.toName(superclassBuilderClassName));
		} else if (extendsClause != null) {
			String superclassBuilderClassName = extendsClause.toString() + "Builder";
			superclassBuilderClassExpression = chainDots(tdParent, extendsClause.toString(), superclassBuilderClassName);
		}
		// If there is no superclass, superclassBuilderClassExpression is still == null at this point.
		// You can use it to check whether to inherit or not.
		
		typeParams = td.typarams;
		
		// <C, B> are the generics for our builder.
		String classGenericName = "C";
		String builderGenericName = "B";
		// If these generics' names collide with any generics on the annotated class, modify them.
		// For instance, if there are generics <B, B2, C> on the annotated class, use "C2" and "B3" for our builder.
		java.util.List<String> typeParamStrings = new ArrayList<String>();
		for (JCTypeParameter typeParam : typeParams) typeParamStrings.add(typeParam.getName().toString());
		classGenericName = generateNonclashingNameFor(classGenericName, typeParamStrings);
		builderGenericName = generateNonclashingNameFor(builderGenericName, typeParamStrings);
		
		thrownExceptions = List.nil();
		
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
		
		// Create the abstract builder class.
		JavacNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = generateBuilderAbstractClass(annotationNode, tdParent, builderClassName, superclassBuilderClassExpression,
				typeParams, superclassTypeParams, classGenericName, builderGenericName);
			recursiveSetGeneratedBy(builderType.get(), ast, annotationNode.getContext());
		} else {
			JCClassDecl builderTypeDeclaration = (JCClassDecl) builderType.get();
			if (!builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)
				|| !builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
				
				annotationNode.addError("Existing Builder must be an abstract static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderType, annotationNode);
			// Generate errors for @Singular BFDs that have one already defined node.
			for (BuilderFieldData bfd : builderFields) {
				SingularData sd = bfd.singularData;
				if (sd == null) continue;
				JavacSingularizer singularizer = sd.getSingularizer();
				if (singularizer == null) continue;
				if (singularizer.checkForAlreadyExistingNodesAndGenerateError(builderType, sd)) {
					bfd.singularData = null;
				}
			}
		}
		
		// Generate the fields in the abstract builder class that hold the values for the instance.
		generateBuilderFields(builderType, builderFields, ast);
		if (addCleaning) {
			JavacTreeMaker maker = builderType.getTreeMaker();
			JCVariableDecl uncleanField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName("$lombokUnclean"), maker.TypeIdent(CTC_BOOLEAN), null);
			recursiveSetGeneratedBy(uncleanField, ast, annotationNode.getContext());
			injectFieldAndMarkGenerated(builderType, uncleanField);
		}
		
		if (toBuilder) {
			// Generate $fillValuesFrom() method in the abstract builder.
			JCMethodDecl fvm = generateFillValuesMethod(tdParent, superclassBuilderClassExpression != null, builderGenericName, classGenericName, builderClassName);
			recursiveSetGeneratedBy(fvm, ast, annotationNode.getContext());
			injectMethod(builderType, fvm);
			// Generate $fillValuesFromInstanceIntoBuilder() method in the builder implementation class.
			JCMethodDecl sfvm = generateStaticFillValuesMethod(tdParent, builderClassName, typeParams, builderFields);
			recursiveSetGeneratedBy(sfvm, ast, annotationNode.getContext());
			injectMethod(builderType, sfvm);
		}
		
		// Generate abstract self() and build() methods in the abstract builder.
		JCMethodDecl asm = generateAbstractSelfMethod(tdParent, superclassBuilderClassExpression != null, builderGenericName);
		recursiveSetGeneratedBy(asm, ast, annotationNode.getContext());
		injectMethod(builderType, asm);
		JCMethodDecl abm = generateAbstractBuildMethod(tdParent, buildMethodName, superclassBuilderClassExpression != null, classGenericName);
		recursiveSetGeneratedBy(abm, ast, annotationNode.getContext());
		injectMethod(builderType, abm);
		
		// Create the setter methods in the abstract builder.
		for (BuilderFieldData bfd : builderFields) {
			generateSetterMethodsForBuilder(builderType, bfd, annotationNode, builderGenericName);
		}
		
		// Create the toString() method for the abstract builder.
		java.util.List<Included<JavacNode, ToString.Include>> fieldNodes = new ArrayList<Included<JavacNode, ToString.Include>>();
		for (BuilderFieldData bfd : builderFields) {
			for (JavacNode f : bfd.createdFields) {
				fieldNodes.add(new Included<JavacNode, ToString.Include>(f, null, true));
			}
		}
		
		// Let toString() call super.toString() if there is a superclass, so that it also shows fields from the superclass' builder.
		JCMethodDecl toStringMethod = HandleToString.createToString(builderType, fieldNodes, true, superclassBuilderClassExpression != null, FieldAccess.ALWAYS_FIELD, ast);
		if (toStringMethod != null) injectMethod(builderType, toStringMethod);
		
		// If clean methods are requested, add them now.
		if (addCleaning) {
			JCMethodDecl md = generateCleanMethod(builderFields, builderType, ast);
			recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
			injectMethod(builderType, md);
		}
		
		boolean isAbstract = (td.mods.flags & Flags.ABSTRACT) != 0;
		if (!isAbstract) {
			// Only non-abstract classes get the Builder implementation.
			
			// Create the builder implementation class.
			JavacNode builderImplType = findInnerClass(tdParent, builderImplClassName);
			if (builderImplType == null) {
				builderImplType = generateBuilderImplClass(annotationNode, tdParent, builderImplClassName, builderClassName, typeParams);
				recursiveSetGeneratedBy(builderImplType.get(), ast, annotationNode.getContext());
			} else {
				JCClassDecl builderImplTypeDeclaration = (JCClassDecl) builderImplType.get();
				if (!builderImplTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)
						|| builderImplTypeDeclaration.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
					annotationNode.addError("Existing BuilderImpl must be a non-abstract static inner class.");
					return;
				}
				sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderImplType, annotationNode);
			}

			// Create a simple constructor for the BuilderImpl class.
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PRIVATE, List.<JCAnnotation>nil(), builderImplType, List.<JavacNode>nil(), false, annotationNode);
			if (cd != null) injectMethod(builderImplType, cd);
			
			// Create the self() and build() methods in the BuilderImpl.
			JCMethodDecl selfMethod = generateSelfMethod(builderImplType, typeParams);
			recursiveSetGeneratedBy(selfMethod, ast, annotationNode.getContext());
			injectMethod(builderImplType, selfMethod);
			if (methodExists(buildMethodName, builderImplType, -1) == MemberExistsResult.NOT_EXISTS) {
				JCMethodDecl buildMethod = generateBuildMethod(buildMethodName, tdParent, builderImplType, thrownExceptions);
				recursiveSetGeneratedBy(buildMethod, ast, annotationNode.getContext());
				injectMethod(builderImplType, buildMethod);
			}
		}
		
		// Generate a constructor in the annotated class that takes a builder as argument.
		generateBuilderBasedConstructor(tdParent, typeParams, builderFields, annotationNode, builderClassName,
			superclassBuilderClassExpression != null);
		
		if (isAbstract) {
			// Only non-abstract classes get the builder() and toBuilder() methods.
			return;
		}
			
		// Add the builder() method to the annotated class.
		// Allow users to specify their own builder() methods, e.g., to provide default values.
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl builderMethod = generateBuilderMethod(builderMethodName, builderClassName, builderImplClassName, annotationNode, tdParent, typeParams);
			recursiveSetGeneratedBy(builderMethod, ast, annotationNode.getContext());
			if (builderMethod != null) injectMethod(tdParent, builderMethod);
		}

		// Add the toBuilder() method to the annotated class.
		if (toBuilder) {
			switch (methodExists(TO_BUILDER_METHOD_NAME, tdParent, 0)) {
			case EXISTS_BY_USER:
				annotationNode.addWarning("Not generating toBuilder() as it already exists.");
				return;
			case NOT_EXISTS:
				JCMethodDecl md = generateToBuilderMethod(builderClassName, builderImplClassName, annotationNode, tdParent, typeParams);
				if (md != null) {
					recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
					injectMethod(tdParent, md);
				}
			default:
				// Should not happen.
			}
		}
	}
	
	/**
	 * Creates and returns the abstract builder class and injects it into the annotated class.
	 */
	private JavacNode generateBuilderAbstractClass(JavacNode source, JavacNode tdParent, String builderClass,
		JCExpression superclassBuilderClassExpression, List<JCTypeParameter> typeParams,
		List<JCExpression> superclassTypeParams, String classGenericName, String builderGenericName) {
		
		JavacTreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.ABSTRACT | Flags.PUBLIC);
		
		// Keep any type params of the annotated class.
		ListBuffer<JCTypeParameter> allTypeParams = new ListBuffer<JCTypeParameter>();
		allTypeParams.addAll(copyTypeParams(source, typeParams));
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method, named "C", which extends the annotated class.
		JCExpression annotatedClass = maker.Ident(tdParent.toName(tdParent.getName()));
		if (typeParams.nonEmpty()) {
			// Add type params of the annotated class.
			annotatedClass = maker.TypeApply(annotatedClass, getTypeParamExpressions(typeParams, maker).toList());
		}
		allTypeParams.add(maker.TypeParameter(tdParent.toName(classGenericName), List.<JCExpression>of(annotatedClass)));
		// 2. The return type for all setter methods, named "B", which extends this builder class.
		Name builderClassName = tdParent.toName(builderClass);
		ListBuffer<JCExpression> typeParamsForBuilder = getTypeParamExpressions(typeParams, maker);
		typeParamsForBuilder.add(maker.Ident(tdParent.toName(classGenericName)));
		typeParamsForBuilder.add(maker.Ident(tdParent.toName(builderGenericName)));
		JCTypeApply typeApply = maker.TypeApply(maker.Ident(builderClassName), typeParamsForBuilder.toList());
		allTypeParams.add(maker.TypeParameter(tdParent.toName(builderGenericName), List.<JCExpression>of(typeApply)));
		
		JCExpression extending = null;
		if (superclassBuilderClassExpression != null) {
			// If the annotated class extends another class, we want this builder to extend the builder of the superclass.
			// 1. Add the type parameters of the superclass.
			typeParamsForBuilder = getTypeParamExpressions(superclassTypeParams, maker);
			// 2. Add the builder type params <C, B>.
			typeParamsForBuilder.add(maker.Ident(tdParent.toName(classGenericName)));
			typeParamsForBuilder.add(maker.Ident(tdParent.toName(builderGenericName)));
			extending = maker.TypeApply(superclassBuilderClassExpression, typeParamsForBuilder.toList());
		}
		
		JCClassDecl builder = maker.ClassDef(mods, builderClassName, allTypeParams.toList(), extending, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
	}
	
	/**
	 * Creates and returns the concrete builder implementation class and injects it into the annotated class.
	 */
	private JavacNode generateBuilderImplClass(JavacNode source, JavacNode tdParent, String builderImplClass, String builderAbstractClass, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.PRIVATE | Flags.FINAL);
		
		// Extend the abstract builder.
		JCExpression extending = maker.Ident(tdParent.toName(builderAbstractClass));
		// Add any type params of the annotated class.
		ListBuffer<JCTypeParameter> allTypeParams = new ListBuffer<JCTypeParameter>();
		allTypeParams.addAll(copyTypeParams(source, typeParams));
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method (named "C" in the abstract builder), which is the annotated class.
		JCExpression annotatedClass = maker.Ident(tdParent.toName(tdParent.getName()));
		if (typeParams.nonEmpty()) {
			// Add type params of the annotated class.
			annotatedClass = maker.TypeApply(annotatedClass, getTypeParamExpressions(typeParams, maker).toList());
		}
		// 2. The return type for all setter methods (named "B" in the abstract builder), which is this builder class.
		JCExpression builderImplClassExpression = maker.Ident(tdParent.toName(builderImplClass));
		if (typeParams.nonEmpty()) {
			builderImplClassExpression = maker.TypeApply(builderImplClassExpression, getTypeParamExpressions(typeParams, maker).toList());
		}
		ListBuffer<JCExpression> typeParamsForBuilder = getTypeParamExpressions(typeParams, maker);
		typeParamsForBuilder.add(annotatedClass);
		typeParamsForBuilder.add(builderImplClassExpression);
		extending = maker.TypeApply(extending, typeParamsForBuilder.toList());
		
		JCClassDecl builder = maker.ClassDef(mods, tdParent.toName(builderImplClass), copyTypeParams(source, typeParams), extending, List.<JCExpression>nil(), List.<JCTree>nil());
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
	private void generateBuilderBasedConstructor(JavacNode typeNode, List<JCTypeParameter> typeParams, java.util.List<BuilderFieldData> builderFields, JavacNode source, String builderClassName, boolean callBuilderBasedSuperConstructor) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		AccessLevel level = AccessLevel.PROTECTED;
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		Name builderVariableName = typeNode.toName(BUILDER_VARIABLE_NAME);
		for (BuilderFieldData bfd : builderFields) {
			JCExpression rhs;
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, bfd.originalFieldNode, bfd.type, statements, bfd.name, "b");
				rhs = maker.Ident(bfd.singularData.getPluralName());
			} else {
				rhs = maker.Select(maker.Ident(builderVariableName), bfd.name);
			}
			JCFieldAccess fieldInThis = maker.Select(maker.Ident(typeNode.toName("this")), bfd.rawName);
			
			JCStatement assign = maker.Exec(maker.Assign(fieldInThis, rhs));
			
			// In case of @Builder.Default, set the value to the default if it was not set in the builder.
			if (bfd.nameOfSetFlag != null) {
				JCFieldAccess setField = maker.Select(maker.Ident(builderVariableName), bfd.nameOfSetFlag);
				fieldInThis = maker.Select(maker.Ident(typeNode.toName("this")), bfd.rawName);
				JCAssign assignDefault = maker.Assign(fieldInThis, maker.Apply(typeParameterNames(maker, ((JCClassDecl) typeNode.get()).typarams), maker.Select(maker.Ident(((JCClassDecl) typeNode.get()).name), bfd.nameOfDefaultProvider), List.<JCExpression>nil()));
				statements.append(maker.If(setField, assign, maker.Exec(assignDefault)));
			} else {
				statements.append(assign);
			}
			
			if (hasNonNullAnnotations(bfd.originalFieldNode)) {
				JCStatement nullCheck = generateNullCheck(maker, bfd.originalFieldNode, source);
				if (nullCheck != null) statements.append(nullCheck);
			}
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		
		// Create a constructor that has just the builder as parameter.
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
		Name builderClassname = typeNode.toName(builderClassName);
		// First add all generics that are present on the parent type.
		ListBuffer<JCExpression> typeParamsForBuilderParameter = getTypeParamExpressions(typeParams, maker);
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.add(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.add(wildcard);
		JCTypeApply paramType = maker.TypeApply(maker.Ident(builderClassname), typeParamsForBuilderParameter.toList());
		JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), builderVariableName, paramType, null);
		params.append(param);
		
		if (callBuilderBasedSuperConstructor) {
			// The first statement must be the call to the super constructor.
			JCMethodInvocation callToSuperConstructor = maker.Apply(List.<JCExpression>nil(),
					maker.Ident(typeNode.toName("super")),
					List.<JCExpression>of(maker.Ident(builderVariableName)));
			statements.prepend(maker.Exec(callToSuperConstructor));
		}
		
		JCMethodDecl constr = recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, statements.toList()), null), source.get(), typeNode.getContext());
		
		injectMethod(typeNode, constr, null, Javac.createVoidType(typeNode.getSymbolTable(), CTC_VOID));
	}
	
	private JCMethodDecl generateBuilderMethod(String builderMethodName, String builderClassName, String builderImplClassName, JavacNode source, JavacNode type, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : typeParams) typeArgs.append(maker.Ident(typeParam.name));
		
		JCExpression call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, type.toName(builderImplClassName), typeParams), List.<JCExpression>nil(), null);
		JCStatement statement = maker.Return(call);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PUBLIC;
		modifiers |= Flags.STATIC;
		
		// Add any type params of the annotated class to the return type.
		ListBuffer<JCExpression> typeParameterNames = new ListBuffer<JCExpression>();
		typeParameterNames.addAll(typeParameterNames(maker, typeParams));
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.add(wildcard);
		typeParameterNames.add(wildcard);
		JCTypeApply returnType = maker.TypeApply(maker.Ident(type.toName(builderClassName)), typeParameterNames.toList());
		
		return maker.MethodDef(maker.Modifiers(modifiers), type.toName(builderMethodName), returnType, copyTypeParams(source, typeParams), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	/**
	 * Generates a <code>toBuilder()</code> method in the annotated class that looks like this:
	 * <pre>
	 * public ParentBuilder&lt;?, ?&gt; toBuilder() {
	 *     return new <i>Foobar</i>BuilderImpl().$fillValuesFrom(this);
	 * }
	 * </pre>
	 */
	private JCMethodDecl generateToBuilderMethod(String builderClassName, String builderImplClassName, JavacNode source, JavacNode type, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : typeParams) typeArgs.append(maker.Ident(typeParam.name));
		
		JCExpression newClass = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, type.toName(builderImplClassName), typeParams), List.<JCExpression>nil(), null);
		List<JCExpression> methodArgs = List.<JCExpression>of(maker.Ident(type.toName("this")));
		JCMethodInvocation invokeFillMethod = maker.Apply(List.<JCExpression>nil(), maker.Select(newClass, type.toName(FILL_VALUES_METHOD_NAME)), methodArgs);
		JCStatement statement = maker.Return(invokeFillMethod);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PUBLIC;
		
		// Add any type params of the annotated class to the return type.
		ListBuffer<JCExpression> typeParameterNames = new ListBuffer<JCExpression>();
		typeParameterNames.addAll(typeParameterNames(maker, typeParams));
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParameterNames.add(wildcard);
		typeParameterNames.add(wildcard);
		JCTypeApply returnType = maker.TypeApply(maker.Ident(type.toName(builderClassName)), typeParameterNames.toList());
		
		return maker.MethodDef(maker.Modifiers(modifiers), type.toName(TO_BUILDER_METHOD_NAME), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}

	/**
	 * Generates a <code>$fillValuesFrom()</code> method in the abstract builder class that looks
	 * like this:
	 * <pre>
	 * protected B $fillValuesFrom(final C instance) {
	 *     super.$fillValuesFrom(instance);
	 *     FoobarBuilderImpl.$fillValuesFromInstanceIntoBuilder(instance, this);
	 *     return self();
	 * }
	 * </pre>
	 */
	private JCMethodDecl generateFillValuesMethod(JavacNode type, boolean inherited, String builderGenericName, String classGenericName, String builderImplClassName) {
		JavacTreeMaker maker = type.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		if (inherited) {
			JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(type, "Override"), List.<JCExpression>nil());
			annotations = List.of(overrideAnnotation);
		}
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED, annotations);
		Name name = type.toName(FILL_VALUES_METHOD_NAME);
		JCExpression returnType = maker.Ident(type.toName(builderGenericName));
		
		JCExpression classGenericNameExpr = maker.Ident(type.toName(classGenericName));
		JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.LocalVarFlags), type.toName(INSTANCE_VARIABLE_NAME), classGenericNameExpr, null);

		ListBuffer<JCStatement> body = new ListBuffer<JCStatement>();
		
		if (inherited) {
			// Call super.
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
				maker.Select(maker.Ident(type.toName("super")), name),
				List.<JCExpression>of(maker.Ident(type.toName(INSTANCE_VARIABLE_NAME))));
			body.append(maker.Exec(callToSuper));
		}
		
		// Call the builder implemention's helper method that actually fills the values from the instance.
		JCMethodInvocation callStaticFillValuesMethod = maker.Apply(List.<JCExpression>nil(),
			maker.Select(maker.Ident(type.toName(builderImplClassName)), type.toName(STATIC_FILL_VALUES_METHOD_NAME)),
			List.<JCExpression>of(maker.Ident(type.toName(INSTANCE_VARIABLE_NAME)), maker.Ident(type.toName("this"))));
		body.append(maker.Exec(callStaticFillValuesMethod));
		
		JCReturn returnStatement = maker.Return(maker.Apply(List.<JCExpression>nil(), maker.Ident(type.toName(SELF_METHOD)), List.<JCExpression>nil()));
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
	private JCMethodDecl generateStaticFillValuesMethod(JavacNode type, String builderClassname, List<JCTypeParameter> typeParams, java.util.List<BuilderFieldData> builderFields) {
		JavacTreeMaker maker = type.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		JCModifiers modifiers = maker.Modifiers(Flags.PRIVATE | Flags.STATIC, annotations);
		Name name = type.toName(STATIC_FILL_VALUES_METHOD_NAME);
		JCExpression returnType = maker.TypeIdent(CTC_VOID);
		
		// 1st parameter: "Foobar instance"
		JCVariableDecl paramInstance = maker.VarDef(maker.Modifiers(Flags.LocalVarFlags), type.toName(INSTANCE_VARIABLE_NAME), cloneSelfType(type), null);

		// 2nd parameter: "FoobarBuilder<?, ?> b" (plus generics on the annotated type)
		// First add all generics that are present on the parent type.
		ListBuffer<JCExpression> typeParamsForBuilderParameter = getTypeParamExpressions(typeParams, maker);
		// Now add the <?, ?>.
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.add(wildcard);
		wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParamsForBuilderParameter.add(wildcard);
		JCTypeApply builderType = maker.TypeApply(maker.Ident(type.toName(builderClassname)), typeParamsForBuilderParameter.toList());
		JCVariableDecl paramBuilder = maker.VarDef(maker.Modifiers(Flags.LocalVarFlags), type.toName(BUILDER_VARIABLE_NAME), builderType, null);

		ListBuffer<JCStatement> body = new ListBuffer<JCStatement>();
		
		// Call the builder's setter methods to fill the values from the instance.
		for (BuilderFieldData bfd : builderFields) {
			JCExpressionStatement exec = createSetterCallWithInstanceValue(bfd, type, maker);
			body.append(exec);
		}
		
		JCBlock bodyBlock = maker.Block(0, body.toList());

		return maker.MethodDef(modifiers, name, returnType, copyTypeParams(type, typeParams), List.of(paramInstance, paramBuilder), List.<JCExpression>nil(), bodyBlock, null);
	}
	
	private JCExpressionStatement createSetterCallWithInstanceValue(BuilderFieldData bfd, JavacNode type, JavacTreeMaker maker) {
		JCExpression[] tgt = new JCExpression[bfd.singularData == null ? 1 : 2];
		if (bfd.obtainVia == null || !bfd.obtainVia.field().isEmpty()) {
			for (int i = 0; i < tgt.length; i++) {
				tgt[i] = maker.Select(maker.Ident(type.toName(INSTANCE_VARIABLE_NAME)), bfd.obtainVia == null ? bfd.rawName : type.toName(bfd.obtainVia.field()));
			}
		} else {
			if (bfd.obtainVia.isStatic()) {
				for (int i = 0; i < tgt.length; i++) {
					JCExpression c = maker.Select(maker.Ident(type.toName(type.getName())), type.toName(bfd.obtainVia.method()));
					tgt[i] = maker.Apply(List.<JCExpression>nil(), c, List.<JCExpression>of(maker.Ident(type.toName(INSTANCE_VARIABLE_NAME))));
				}
			} else {
				for (int i = 0; i < tgt.length; i++) {
					JCExpression c = maker.Select(maker.Ident(type.toName(INSTANCE_VARIABLE_NAME)), type.toName(bfd.obtainVia.method()));
					tgt[i] = maker.Apply(List.<JCExpression>nil(), c, List.<JCExpression>nil());
				}
			}
		}
		
		JCExpression arg;
		if (bfd.singularData == null) {
			arg = tgt[0];
		} else {
			JCExpression eqNull = maker.Binary(CTC_EQUAL, tgt[0], maker.Literal(CTC_BOT, null));
			JCExpression emptyList = maker.Apply(List.<JCExpression>nil(), chainDots(type, "java", "util", "Collections", "emptyList"), List.<JCExpression>nil());
			arg = maker.Conditional(eqNull, emptyList, tgt[1]);
		}
		JCMethodInvocation apply = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(type.toName(BUILDER_VARIABLE_NAME)), bfd.name), List.of(arg));
		JCExpressionStatement exec = maker.Exec(apply);
		return exec;
	}
	
	private JCMethodDecl generateAbstractSelfMethod(JavacNode type, boolean override, String builderGenericName) {
		JavacTreeMaker maker = type.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		if (override) {
			JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(type, "Override"), List.<JCExpression>nil());
			annotations = List.of(overrideAnnotation);
		}
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED | Flags.ABSTRACT, annotations);
		Name name = type.toName(SELF_METHOD);
		JCExpression returnType = maker.Ident(type.toName(builderGenericName));

		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
	}
	
	private JCMethodDecl generateSelfMethod(JavacNode builderImplType, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = builderImplType.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(builderImplType, "Override"), List.<JCExpression>nil());
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED, List.of(overrideAnnotation));
		Name name = builderImplType.toName(SELF_METHOD);
		
		JCExpression returnType = namePlusTypeParamsToTypeReference(maker, builderImplType.toName(builderImplType.getName()), typeParams);
		JCStatement statement = maker.Return(maker.Ident(builderImplType.toName("this")));
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		
		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	private JCMethodDecl generateAbstractBuildMethod(JavacNode type, String methodName, boolean override, String classGenericName) {
		JavacTreeMaker maker = type.getTreeMaker();
		List<JCAnnotation> annotations = List.nil();
		if (override) {
			JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(type, "Override"), List.<JCExpression>nil());
			annotations = List.of(overrideAnnotation);
		}
		JCModifiers modifiers = maker.Modifiers(Flags.PUBLIC | Flags.ABSTRACT, annotations);
		Name name = type.toName(methodName);
		JCExpression returnType = maker.Ident(type.toName(classGenericName));
		
		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
	}

	private JCMethodDecl generateBuildMethod(String buildName, JavacNode returnType, JavacNode type, List<JCExpression> thrownExceptions) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		JCExpression call;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		// Use a constructor that only has this builder as parameter.
		List<JCExpression> builderArg = List.<JCExpression>of(maker.Ident(type.toName("this")));
		call = maker.NewClass(null, List.<JCExpression>nil(), cloneSelfType(returnType), builderArg, null);
		statements.append(maker.Return(call));
		
		JCBlock body = maker.Block(0, statements.toList());
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(type, "Override"), List.<JCExpression>nil());
		JCModifiers modifiers = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		
		return maker.MethodDef(modifiers, type.toName(buildName), cloneSelfType(returnType), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
	}
	
	private JCMethodDecl generateCleanMethod(java.util.List<BuilderFieldData> builderFields, JavacNode type, JCTree source) {
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
	
	private void generateBuilderFields(JavacNode builderType, java.util.List<BuilderFieldData> builderFields, JCTree source) {
		int len = builderFields.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		java.util.List<JCVariableDecl> generated = new ArrayList<JCVariableDecl>();
		
		for (int i = len - 1; i >= 0; i--) {
			BuilderFieldData bfd = builderFields.get(i);
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.createdFields.addAll(bfd.singularData.getSingularizer().generateFields(bfd.singularData, builderType, source));
			} else {
				JavacNode field = null, setFlag = null;
				for (JavacNode exists : existing) {
					Name n = ((JCVariableDecl) exists.get()).name;
					if (n.equals(bfd.name)) field = exists;
					if (n.equals(bfd.nameOfSetFlag)) setFlag = exists;
				}
				JavacTreeMaker maker = builderType.getTreeMaker();
				if (field == null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.name, cloneType(maker, bfd.type, source, builderType.getContext()), null);
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
		for (JCVariableDecl gen : generated)  recursiveSetGeneratedBy(gen, source, builderType.getContext());
	}
	
	private void generateSetterMethodsForBuilder(final JavacNode builderType, BuilderFieldData fieldNode, JavacNode source, final String builderGenericName) {
		boolean deprecate = isFieldDeprecated(fieldNode.originalFieldNode);
		final JavacTreeMaker maker = builderType.getTreeMaker();
		ExpressionMaker returnTypeMaker = new ExpressionMaker() { @Override public JCExpression make() {
			return maker.Ident(builderType.toName(builderGenericName));
		}};
		
		StatementMaker returnStatementMaker = new StatementMaker() { @Override public JCStatement make() {
			return maker.Return(maker.Apply(List.<JCExpression>nil(), maker.Ident(builderType.toName(SELF_METHOD)), List.<JCExpression>nil()));
		}};
		
		if (fieldNode.singularData == null || fieldNode.singularData.getSingularizer() == null) {
			generateSimpleSetterMethodForBuilder(builderType, deprecate, fieldNode.createdFields.get(0), fieldNode.nameOfSetFlag, source, true, returnTypeMaker.make(), returnStatementMaker.make(), fieldNode.annotations);
		} else {
			fieldNode.singularData.getSingularizer().generateMethods(fieldNode.singularData, deprecate, builderType, source.get(), true, returnTypeMaker, returnStatementMaker);
		}
	}
	
	private void generateSimpleSetterMethodForBuilder(JavacNode builderType, boolean deprecate, JavacNode fieldNode, Name nameOfSetFlag, JavacNode source, boolean fluent, JCExpression returnType, JCStatement returnStatement, List<JCAnnotation> annosOnParam) {
		Name fieldName = ((JCVariableDecl) fieldNode.get()).name;
		
		for (JavacNode child : builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			JCMethodDecl methodDecl = (JCMethodDecl) child.get();
			Name existingName = methodDecl.name;
			if (existingName.equals(fieldName) && !isTolerate(fieldNode, methodDecl)) return;
		}
		
		String setterName = fluent ? fieldNode.getName() : HandlerUtil.buildAccessorName("set", fieldNode.getName());
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		JCMethodDecl newMethod = HandleSetter.createSetter(Flags.PUBLIC, deprecate, fieldNode, maker, setterName, nameOfSetFlag, returnType, returnStatement, source, List.<JCAnnotation>nil(), annosOnParam);
		
		injectMethod(builderType, newMethod);
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
	 */
	private SingularData getSingularData(JavacNode node) {
		for (JavacNode child : node.down()) {
			if (!annotationTypeMatches(Singular.class, child)) continue;
			Name pluralName = node.getKind() == Kind.FIELD ? removePrefixFromField(node) : ((JCVariableDecl) node.get()).name;
			AnnotationValues<Singular> ann = createAnnotation(Singular.class, child);
			deleteAnnotationIfNeccessary(child, Singular.class);
			String explicitSingular = ann.getInstance().value();
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
			
			return new SingularData(child, singularName, pluralName, typeArgs, targetFqn, singularizer);
		}
		
		return null;
	}
	
	private String generateNonclashingNameFor(String classGenericName, java.util.List<String> typeParamStrings) {
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
	
	private ListBuffer<JCExpression> getTypeParamExpressions(List<? extends JCTree> typeParams, JavacTreeMaker maker) {
		ListBuffer<JCExpression> typeParamsForBuilderParameter = new ListBuffer<JCExpression>();
		for (JCTree typeParam : typeParams) {
			if (typeParam instanceof JCTypeParameter) {
				typeParamsForBuilderParameter.add(maker.Ident(((JCTypeParameter)typeParam).getName()));
			} else if (typeParam instanceof JCIdent) {
				typeParamsForBuilderParameter.add(maker.Ident(((JCIdent)typeParam).getName()));
			}
		}
		return typeParamsForBuilderParameter;
	}
}
