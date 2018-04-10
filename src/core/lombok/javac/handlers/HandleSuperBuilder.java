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

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
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
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.HandlerUtil;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleSuperBuilder extends JavacAnnotationHandler<SuperBuilder> {

	private static final String SELF_METHOD = "self";

	private static class BuilderFieldData {
		JCExpression type;
		Name rawName;
		Name name;
		Name nameOfDefaultProvider;
		Name nameOfSetFlag;
		SingularData singularData;
		ObtainVia obtainVia;
		JavacNode obtainViaNode;
		JavacNode originalFieldNode;
		
		java.util.List<JavacNode> createdFields = new ArrayList<JavacNode>();
	}
	
	@Override public void handle(AnnotationValues<SuperBuilder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		SuperBuilder builderInstance = annotation.getInstance();
		
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		
		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		
		JavacNode tdParent = annotationNode.up();
		
		java.util.List<BuilderFieldData> builderFields = new ArrayList<BuilderFieldData>();
		JCExpression returnType;
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrownExceptions = List.nil();
		
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
				JCMethodDecl md = generateDefaultProvider(bfd.nameOfDefaultProvider, fieldNode, td.typarams);
				recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
				if (md != null) injectMethod(tdParent, md);
			}
			addObtainVia(bfd, fieldNode);
			builderFields.add(bfd);
			allFields.append(fieldNode);
		}

		// Set the default names of the builder classes if not customized via annotation.
		String builderClassName = td.name.toString() + "Builder";
		String builderImplClassName = builderClassName + "Impl";
		JCTree extendsClause = Javac.getExtendsClause(td);
		String superclassBuilderClassName = null;
		if (extendsClause instanceof JCFieldAccess) {
			// The extends clause consists of a fully-qualified name. 
			superclassBuilderClassName = ((JCFieldAccess)extendsClause).getIdentifier() + "Builder";
		} else {
			// A simple class name is used in the extends clause.
			superclassBuilderClassName = extendsClause + "Builder";
		}

		boolean useInheritanceOnBuilder = extendsClause != null;
		generateBuilderBasedConstructor(tdParent, builderFields, annotationNode, builderClassName, useInheritanceOnBuilder);

		returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
		typeParams = td.typarams;
		thrownExceptions = List.nil();

		// Create the abstract builder class.
		JavacNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = makeBuilderClass(annotationNode, tdParent, builderClassName, useInheritanceOnBuilder ? superclassBuilderClassName : null, typeParams, ast);
		} else {
			annotationNode.addError("@SuperBuilder does not support customized builders. Use @Builder instead.");
			return;
		}
		
		// Check that all builder fields for @Singular and @ObtainVia.
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
			JavacTreeMaker maker = builderType.getTreeMaker();
			JCVariableDecl uncleanField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName("$lombokUnclean"), maker.TypeIdent(CTC_BOOLEAN), null);
			injectFieldAndMarkGenerated(builderType, uncleanField);
		}

		// Generate abstract self() and build() methods in the abstract builder.
		injectMethod(builderType, generateAbstractSelfMethod(tdParent));
		injectMethod(builderType, generateAbstractBuildMethod(tdParent, buildMethodName));

		// Create the setter methods in the abstract builder.
		for (BuilderFieldData bfd : builderFields) {
			makeSetterMethodsForBuilder(builderType, bfd, annotationNode);
		}
		
		// Create the toString() method for the abstract builder.
		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			java.util.List<JavacNode> fieldNodes = new ArrayList<JavacNode>();
			for (BuilderFieldData bfd : builderFields) {
				fieldNodes.addAll(bfd.createdFields);
			}
			JCMethodDecl md = HandleToString.createToString(builderType, fieldNodes, true, false, FieldAccess.ALWAYS_FIELD, ast);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (addCleaning) injectMethod(builderType, generateCleanMethod(builderFields, builderType, ast));
		
		// Create the builder implementation class.
		JavacNode builderImplType = findInnerClass(tdParent, builderImplClassName);
		if (builderImplType == null) {
			builderImplType = makeBuilderImplClass(annotationNode, tdParent, builderImplClassName, builderClassName, typeParams, ast);
		} else {
			annotationNode.addError("@SuperBuilder does not support customized builders. Use @Builder instead.");
			return;
		}

		// Create a simple constructor for the BuilderImpl class.
		JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PRIVATE, List.<JCAnnotation>nil(), builderImplType, List.<JavacNode>nil(), false, annotationNode);
		if (cd != null) injectMethod(builderImplType, cd);

		// Create the self() and build() methods in the BuilderImpl.
		injectMethod(builderImplType, generateSelfMethod(builderImplType));
		injectMethod(builderImplType, generateBuildMethod(tdParent, buildMethodName, returnType, builderImplType, thrownExceptions, ast));
		
		// Add the builder() method to the annotated class.
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuilderMethod(builderMethodName, builderClassName, builderImplClassName, annotationNode, tdParent, typeParams);
			recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
			if (md != null) injectMethod(tdParent, md);
		}
		
		recursiveSetGeneratedBy(builderType.get(), ast, annotationNode.getContext());
		recursiveSetGeneratedBy(builderImplType.get(), ast, annotationNode.getContext());
	}

	/**
	 * Generates a constructor that has a builder as the only parameter.
	 * The values from the builder are used to initialize the fields of new instances.
	 *
	 * @param typeNode
	 *            the type (with the {@code @Builder} annotation) for which a
	 *            constructor should be generated.
	 * @param builderFields a list of fields in the builder which should be assigned to new instances.
	 * @param source the annotation (used for setting source code locations for the generated code).
	 * @param callBuilderBasedSuperConstructor
	 *            If {@code true}, the constructor will explicitly call a super
	 *            constructor with the builder as argument. Requires
	 *            {@code builderClassAsParameter != null}.
	 */
	private void generateBuilderBasedConstructor(JavacNode typeNode, java.util.List<BuilderFieldData> builderFields, JavacNode source, String builderClassName, boolean callBuilderBasedSuperConstructor) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		AccessLevel level = AccessLevel.PROTECTED;
		boolean isEnum = (((JCClassDecl) typeNode.get()).mods.flags & Flags.ENUM) != 0;
		if (isEnum) {
			level = AccessLevel.PRIVATE;
		}
		
		ListBuffer<JCStatement> nullChecks = new ListBuffer<JCStatement>();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		Name builderVariableName = typeNode.toName("b");
		for (BuilderFieldData bfd : builderFields) {
			List<JCAnnotation> nonNulls = findAnnotations(bfd.originalFieldNode, NON_NULL_PATTERN);
			if (!nonNulls.isEmpty()) {
				JCStatement nullCheck = generateNullCheck(maker, bfd.originalFieldNode, source);
				if (nullCheck != null) {
					nullChecks.append(nullCheck);
				}
			}
			
			JCExpression rhs;
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, bfd.originalFieldNode, bfd.type, statements, bfd.name, "b");
				rhs = maker.Ident(bfd.singularData.getPluralName());
			} else {
				rhs = maker.Select(maker.Ident(builderVariableName), bfd.rawName);
			}
			JCFieldAccess thisX = maker.Select(maker.Ident(bfd.originalFieldNode.toName("this")), bfd.rawName);
			
			JCExpression assign = maker.Assign(thisX, rhs);
				
			statements.append(maker.Exec(assign));
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		
		// Create a constructor that has just the builder as parameter.
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
		Name builderClassname = typeNode.toName(builderClassName);
		// Now add the <?, ?>.
		ListBuffer<JCExpression> typeParams = new ListBuffer<JCExpression>();
		JCWildcard wildcard = maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
		typeParams.add(wildcard);
		typeParams.add(wildcard);
		JCTypeApply paramType = maker.TypeApply(maker.Ident(builderClassname), typeParams.toList());
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
			maker.Block(0L, nullChecks.appendList(statements).toList()), null), source.get(), typeNode.getContext());

		injectMethod(typeNode, constr, null, Javac.createVoidType(typeNode.getSymbolTable(), CTC_VOID));
	}

	private JCMethodDecl generateAbstractSelfMethod(JavacNode type) {
		JavacTreeMaker maker = type.getTreeMaker();
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED | Flags.ABSTRACT);
		Name name = type.toName(SELF_METHOD);
		JCExpression returnType = maker.Ident(type.toName("B"));

		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
	}
	
	private JCMethodDecl generateSelfMethod(JavacNode builderImplType) {
		JavacTreeMaker maker = builderImplType.getTreeMaker();
		JCModifiers modifiers = maker.Modifiers(Flags.PROTECTED);
		Name name = builderImplType.toName(SELF_METHOD);
		JCExpression returnType = maker.Ident(builderImplType.toName(builderImplType.getName()));

		JCStatement statement = maker.Return(maker.Ident(builderImplType.toName("this")));
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));

		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	private JCMethodDecl generateAbstractBuildMethod(JavacNode type, String methodName) {
		JavacTreeMaker maker = type.getTreeMaker();
		JCModifiers modifiers = maker.Modifiers(Flags.PUBLIC | Flags.ABSTRACT);
		Name name = type.toName(methodName);
		JCExpression returnType = maker.Ident(type.toName("C"));

		return maker.MethodDef(modifiers, name, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null);
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
	
	private JCMethodDecl generateBuildMethod(JavacNode tdParent, String buildName, JCExpression returnType, JavacNode type, List<JCExpression> thrownExceptions, JCTree source) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		JCExpression call;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		// Use a constructor that only has this builder as parameter.
		List<JCExpression> builderArg = List.<JCExpression>of(maker.Ident(type.toName("this")));
		call = maker.NewClass(null, List.<JCExpression>nil(), returnType, builderArg, null);
		statements.append(maker.Return(call));
		
		JCBlock body = maker.Block(0, statements.toList());
		
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), type.toName(buildName), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
	}
	
	public JCMethodDecl generateDefaultProvider(Name methodName, JavacNode fieldNode, List<JCTypeParameter> params) {
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		
		JCStatement statement = maker.Return(field.init);
		field.init = null;
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PRIVATE | Flags.STATIC;
		return maker.MethodDef(maker.Modifiers(modifiers), methodName, cloneType(maker, field.vartype, field, fieldNode.getContext()), copyTypeParams(fieldNode, params), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	public JCMethodDecl generateBuilderMethod(String builderMethodName, String builderClassName, String builderImplClassName, JavacNode source, JavacNode type, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : typeParams) {
			typeArgs.append(maker.Ident(typeParam.name));
		}
		
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
	
	public void generateBuilderFields(JavacNode builderType, java.util.List<BuilderFieldData> builderFields, JCTree source) {
		int len = builderFields.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
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
				}
				if (setFlag == null && bfd.nameOfSetFlag != null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.nameOfSetFlag, maker.TypeIdent(CTC_BOOLEAN), null);
					injectFieldAndMarkGenerated(builderType, newField);
				}
				bfd.createdFields.add(field);
			}
		}
	}
	
	public void makeSetterMethodsForBuilder(JavacNode builderType, BuilderFieldData fieldNode, JavacNode source) {
		boolean deprecate = isFieldDeprecated(fieldNode.originalFieldNode);
		if (fieldNode.singularData == null || fieldNode.singularData.getSingularizer() == null) {
			makeSimpleSetterMethodForBuilder(builderType, deprecate, fieldNode.createdFields.get(0), fieldNode.nameOfSetFlag, source, true, true);
		} else {
			// TODO: Fix singular methods to return self().
			fieldNode.singularData.getSingularizer().generateMethods(fieldNode.singularData, deprecate, builderType, source.get(), true, true);
		}
	}
	
	private void makeSimpleSetterMethodForBuilder(JavacNode builderType, boolean deprecate, JavacNode fieldNode, Name nameOfSetFlag, JavacNode source, boolean fluent, boolean chain) {
		Name fieldName = ((JCVariableDecl) fieldNode.get()).name;
		
		for (JavacNode child : builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			JCMethodDecl methodDecl = (JCMethodDecl) child.get();
			Name existingName = methodDecl.name;
			if (existingName.equals(fieldName) && !isTolerate(fieldNode, methodDecl)) return;
		}
		
		String setterName = fluent ? fieldNode.getName() : HandlerUtil.buildAccessorName("set", fieldNode.getName());
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		JCExpression returnType = maker.Ident(builderType.toName("B"));
		JCReturn returnStatement = maker.Return(maker.Apply(List.<JCExpression>nil(), maker.Ident(builderType.toName(SELF_METHOD)), List.<JCExpression>nil()));

		JCMethodDecl newMethod = HandleSetter.createSetter(Flags.PUBLIC, deprecate, fieldNode, maker, setterName, nameOfSetFlag, returnType, returnStatement, source, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
		
		injectMethod(builderType, newMethod);
	}
	
	public JavacNode findInnerClass(JavacNode parent, String name) {
		for (JavacNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			JCClassDecl td = (JCClassDecl) child.get();
			if (td.name.contentEquals(name)) return child;
		}
		return null;
	}
	
	public JavacNode makeBuilderClass(JavacNode source, JavacNode tdParent, String builderClass, String parentBuilderClass, List<JCTypeParameter> typeParams, JCAnnotation ast) {
		JavacTreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.ABSTRACT | Flags.PUBLIC);

		// Keep any type params of the annotated class.
		// TODO: Prevent name clashes with type params from annotated class.
		ListBuffer<JCTypeParameter> allTypeParams = new ListBuffer<JCTypeParameter>();
		allTypeParams.addAll(copyTypeParams(source, typeParams));
		// Add builder-specific type params required for inheritable builders.
		// 1. The return type for the build() method, named "C", which extends the annotated class.
		JCIdent annotatedClass = maker.Ident(tdParent.toName(tdParent.getName()));
		allTypeParams.add(maker.TypeParameter(tdParent.toName("C"), List.<JCExpression>of(annotatedClass)));
		// 2. The return type for all setter methods, named "B", which extends this builder class.
		Name builderClassName = tdParent.toName(builderClass);
		JCTypeApply typeApply = maker.TypeApply(maker.Ident(builderClassName), 
				List.<JCExpression>of(maker.Ident(tdParent.toName("C")), maker.Ident(tdParent.toName("B"))));
		allTypeParams.add(maker.TypeParameter(tdParent.toName("B"), List.<JCExpression>of(typeApply)));

		JCExpression extending = null;
		if (parentBuilderClass != null) {
			// If the annotated class extends another class, we want this builder to extend the builder of the superclass.
			extending = maker.TypeApply(maker.Ident(tdParent.toName(parentBuilderClass)), 
					List.<JCExpression>of(maker.Ident(tdParent.toName("C")), maker.Ident(tdParent.toName("B"))));
			// TODO: type params from annotated class
		}
		
		JCClassDecl builder = maker.ClassDef(mods, builderClassName, allTypeParams.toList(), extending, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
	}
	
	public JavacNode makeBuilderImplClass(JavacNode source, JavacNode tdParent, String builderImplClass, String builderClassName, List<JCTypeParameter> typeParams, JCAnnotation ast) {
		JavacTreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.PRIVATE | Flags.FINAL);
		
		JCExpression extending = null;
		if (builderClassName != null) {
			extending = maker.Ident(tdParent.toName(builderClassName));

			// Add any type params of the annotated class.
			ListBuffer<JCTypeParameter> allTypeParams = new ListBuffer<JCTypeParameter>();
			allTypeParams.addAll(copyTypeParams(source, typeParams));
			// Add builder-specific type params required for inheritable builders.
			// 1. The return type for the build() method (named "C" in the abstract builder), which is the annotated class.
			JCIdent annotatedClassIdent = maker.Ident(tdParent.toName(tdParent.getName()));
			// 2. The return type for all setter methods (named "B" in the abstract builder), which is this builder class.
			JCIdent builderImplClassIdent = maker.Ident(tdParent.toName(builderImplClass));
			extending = maker.TypeApply(extending, List.<JCExpression>of(annotatedClassIdent, builderImplClassIdent));
		}
		
		JCClassDecl builder = maker.ClassDef(mods, tdParent.toName(builderImplClass), copyTypeParams(source, typeParams), extending, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
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
			if (node.get() instanceof JCVariableDecl) {
				type = ((JCVariableDecl) node.get()).vartype;
			}
			
			String name = null;
			List<JCExpression> typeArgs = List.nil();
			if (type instanceof JCTypeApply) {
				typeArgs = ((JCTypeApply) type).arguments;
				type = ((JCTypeApply) type).clazz;
			}
			
			name = type.toString();
			
			String targetFqn = JavacSingularsRecipes.get().toQualified(name);
			JavacSingularizer singularizer = JavacSingularsRecipes.get().getSingularizer(targetFqn);
			if (singularizer == null) {
				node.addError("Lombok does not know how to create the singular-form builder methods for type '" + name + "'; they won't be generated.");
				return null;
			}
			
			return new SingularData(child, singularName, pluralName, typeArgs, targetFqn, singularizer);
		}
		
		return null;
	}
}
