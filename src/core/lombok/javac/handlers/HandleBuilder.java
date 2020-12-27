/*
 * Copyright (C) 2013-2020 The Project Lombok Authors.
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
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
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
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleBuilder extends JavacAnnotationHandler<Builder> {
	private HandleConstructor handleConstructor = new HandleConstructor();
	
	static final String CLEAN_FIELD_NAME = "$lombokUnclean";
	static final String CLEAN_METHOD_NAME = "$lombokClean";
	static final String TO_BUILDER_METHOD_NAME = "toBuilder";
	static final String DEFAULT_PREFIX = "$default$";
	static final String SET_PREFIX = "$set";
	static final String VALUE_PREFIX = "$value";
	static final String BUILDER_TEMP_VAR = "builder";
	static final String TO_BUILDER_NOT_SUPPORTED = "@Builder(toBuilder=true) is only supported if you return your own type.";
	
	private static final boolean toBoolean(Object expr, boolean defaultValue) {
		if (expr == null) return defaultValue;
		if (expr instanceof JCLiteral) return ((Integer) ((JCLiteral) expr).value) != 0;
		return ((Boolean) expr).booleanValue();
	}
	
	static class BuilderJob {
		CheckerFrameworkVersion checkerFramework;
		JavacNode parentType;
		String builderMethodName, buildMethodName;
		boolean isStatic;
		List<JCTypeParameter> typeParams;
		List<JCTypeParameter> builderTypeParams;
		JCTree source;
		JavacNode sourceNode;
		java.util.List<BuilderFieldData> builderFields;
		AccessLevel accessInners, accessOuters;
		boolean oldFluent, oldChain, toBuilder;
		
		JavacNode builderType;
		String builderClassName;
		
		void init(AnnotationValues<Builder> annValues, Builder ann, JavacNode node) {
			accessOuters = ann.access();
			if (accessOuters == null) accessOuters = AccessLevel.PUBLIC;
			if (accessOuters == AccessLevel.NONE) {
				sourceNode.addError("AccessLevel.NONE is not valid here");
				accessOuters = AccessLevel.PUBLIC;
			}
			accessInners = accessOuters == AccessLevel.PROTECTED ? AccessLevel.PUBLIC : accessOuters;
			
			oldFluent = toBoolean(annValues.getActualExpression("fluent"), true);
			oldChain = toBoolean(annValues.getActualExpression("chain"), true);
			
			builderMethodName = ann.builderMethodName();
			buildMethodName = ann.buildMethodName();
			builderClassName = fixBuilderClassName(node, ann.builderClassName());
			toBuilder = ann.toBuilder();
			
			if (builderMethodName == null) builderMethodName = "builder";
			if (buildMethodName == null) buildMethodName = "build";
			if (builderClassName == null) builderClassName = "";
		}
		
		static String fixBuilderClassName(JavacNode node, String override) {
			if (override != null && !override.isEmpty()) return override;
			override = node.getAst().readConfiguration(ConfigurationKeys.BUILDER_CLASS_NAME);
			if (override != null && !override.isEmpty()) return override;
			return "*Builder";
		}
		
		String replaceBuilderClassName(Name name) {
			if (builderClassName.indexOf('*') == -1) return builderClassName;
			return builderClassName.replace("*", name.toString());
		}
		
		JCExpression createBuilderParentTypeReference() {
			return namePlusTypeParamsToTypeReference(parentType.getTreeMaker(), parentType, typeParams);
		}
		
		Name getBuilderClassName() {
			return parentType.toName(builderClassName);
		}
		
		List<JCTypeParameter> copyTypeParams() {
			return JavacHandlerUtil.copyTypeParams(sourceNode, typeParams);
		}
		
		Name toName(String name) {
			return parentType.toName(name);
		}
		
		Context getContext() {
			return parentType.getContext();
		}
		
		JavacTreeMaker getTreeMaker() {
			return parentType.getTreeMaker();
		}
	}
	
	static class BuilderFieldData {
		List<JCAnnotation> annotations;
		JCExpression type;
		Name rawName;
		Name name;
		Name builderFieldName;
		Name nameOfDefaultProvider;
		Name nameOfSetFlag;
		SingularData singularData;
		ObtainVia obtainVia;
		JavacNode obtainViaNode;
		JavacNode originalFieldNode;
		
		java.util.List<JavacNode> createdFields = new ArrayList<JavacNode>();
	}
	
	@Override public void handle(AnnotationValues<Builder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.BUILDER_FLAG_USAGE, "@Builder");
		BuilderJob job = new BuilderJob();
		job.sourceNode = annotationNode;
		job.source = ast;
		job.checkerFramework = getCheckerFrameworkVersion(annotationNode);
		job.isStatic = true;
		
		Builder annInstance = annotation.getInstance();
		job.init(annotation, annInstance, annotationNode);
		java.util.List<Name> typeArgsForToBuilder = null;
		
		boolean generateBuilderMethod;
		if (job.builderMethodName.isEmpty()) {
			generateBuilderMethod = false;
		} else if (!checkName("builderMethodName", job.builderMethodName, annotationNode)) {
			return;
		} else {
			generateBuilderMethod = true;
		}
		
		if (!checkName("buildMethodName", job.buildMethodName, annotationNode)) return;
		
		// Do not delete the Builder annotation yet, we need it for @Jacksonized.
		
		JavacNode parent = annotationNode.up();
		
		job.builderFields = new ArrayList<BuilderFieldData>();
		JCExpression buildMethodReturnType;
		job.typeParams = List.nil();
		List<JCExpression> buildMethodThrownExceptions;
		Name nameOfBuilderMethod;
		
		JavacNode fillParametersFrom = parent.get() instanceof JCMethodDecl ? parent : null;
		boolean addCleaning = false;
		
		ArrayList<JavacNode> nonFinalNonDefaultedFields = null;
		
		if (parent.get() instanceof JCClassDecl) {
			job.parentType = parent;
			JCClassDecl td = (JCClassDecl) parent.get();
			
			ListBuffer<JavacNode> allFields = new ListBuffer<JavacNode>();
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
					JCMethodDecl md = generateDefaultProvider(bfd.nameOfDefaultProvider, fieldNode, td.typarams);
					recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
					if (md != null) injectMethod(parent, md);
				}
				addObtainVia(bfd, fieldNode);
				job.builderFields.add(bfd);
				allFields.append(fieldNode);
			}
			
			handleConstructor.generateConstructor(parent, AccessLevel.PACKAGE, List.<JCAnnotation>nil(), allFields.toList(), false, null, SkipIfConstructorExists.I_AM_BUILDER, annotationNode);
			
			buildMethodReturnType = namePlusTypeParamsToTypeReference(parent.getTreeMaker(), parent, td.typarams);
			job.typeParams = job.builderTypeParams = td.typarams;
			buildMethodThrownExceptions = List.nil();
			nameOfBuilderMethod = null;
			job.builderClassName = job.replaceBuilderClassName(td.name);
			if (!checkName("builderClassName", job.builderClassName, annotationNode)) return;
		} else if (fillParametersFrom != null && fillParametersFrom.getName().toString().equals("<init>")) {
			JCMethodDecl jmd = (JCMethodDecl) fillParametersFrom.get();
			if (!jmd.typarams.isEmpty()) {
				annotationNode.addError("@Builder is not supported on constructors with constructor type parameters.");
				return;
			}
			
			job.parentType = parent.up();
			JCClassDecl td = (JCClassDecl) job.parentType.get();
			job.typeParams = job.builderTypeParams = td.typarams;
			buildMethodReturnType = job.createBuilderParentTypeReference();
			buildMethodThrownExceptions = jmd.thrown;
			nameOfBuilderMethod = null;
			job.builderClassName = job.replaceBuilderClassName(td.name);
			if (!checkName("builderClassName", job.builderClassName, annotationNode)) return;
		} else if (fillParametersFrom != null) {
			job.parentType = parent.up();
			JCClassDecl td = (JCClassDecl) job.parentType.get();
			JCMethodDecl jmd = (JCMethodDecl) fillParametersFrom.get();
			job.isStatic = (jmd.mods.flags & Flags.STATIC) != 0;
			
			JCExpression fullReturnType = jmd.restype;
			buildMethodReturnType = fullReturnType;
			job.typeParams = job.builderTypeParams = jmd.typarams;
			buildMethodThrownExceptions = jmd.thrown;
			nameOfBuilderMethod = jmd.name;
			if (buildMethodReturnType instanceof JCTypeApply) {
				buildMethodReturnType = cloneType(job.getTreeMaker(), buildMethodReturnType, ast, annotationNode.getContext());
			}
			if (job.builderClassName.indexOf('*') > -1) {
				String replStr = returnTypeToBuilderClassName(annotationNode, td, buildMethodReturnType, job.typeParams);
				if (replStr == null) return; // shuold not happen
				job.builderClassName = job.builderClassName.replace("*", replStr);
			}
			if (job.toBuilder) {
				if (fullReturnType instanceof JCArrayTypeTree) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				Name simpleName;
				String pkg;
				List<JCExpression> tpOnRet = List.nil();
				
				if (fullReturnType instanceof JCTypeApply) {
					tpOnRet = ((JCTypeApply) fullReturnType).arguments;
				}
				
				JCExpression namingType = fullReturnType;
				if (buildMethodReturnType instanceof JCTypeApply) namingType = ((JCTypeApply) buildMethodReturnType).clazz;
				
				if (namingType instanceof JCIdent) {
					simpleName = ((JCIdent) namingType).name;
					pkg = null;
				} else if (namingType instanceof JCFieldAccess) {
					JCFieldAccess jcfa = (JCFieldAccess) namingType;
					simpleName = jcfa.name;
					pkg = unpack(jcfa.selected);
					if (pkg.startsWith("ERR:")) {
						String err = pkg.substring(4, pkg.indexOf("__ERR__"));
						annotationNode.addError(err);
						return;
					}
				} else {
					annotationNode.addError("Expected a (parameterized) type here instead of a " + namingType.getClass().getName());
					return;
				}
				
				if (pkg != null && !parent.getPackageDeclaration().equals(pkg)) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				if (!job.parentType.getName().contentEquals(simpleName)) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				List<JCTypeParameter> tpOnMethod = jmd.typarams;
				List<JCTypeParameter> tpOnType = ((JCClassDecl) job.parentType.get()).typarams;
				typeArgsForToBuilder = new ArrayList<Name>();
				
				for (JCTypeParameter tp : tpOnMethod) {
					int pos = -1;
					int idx = -1;
					for (JCExpression tOnRet : tpOnRet) {
						idx++;
						if (!(tOnRet instanceof JCIdent)) continue;
						if (((JCIdent) tOnRet).name != tp.name) continue;
						pos = idx;
					}
					
					if (pos == -1 || tpOnType.size() <= pos) {
						annotationNode.addError("@Builder(toBuilder=true) requires that each type parameter on the static method is part of the typeargs of the return value. Type parameter " + tp.name + " is not part of the return type.");
						return;
					}
					typeArgsForToBuilder.add(tpOnType.get(pos).name);
				}
			}
		} else {
			annotationNode.addError("@Builder is only supported on types, constructors, and methods.");
			return;
		}
		
		if (fillParametersFrom != null) {
			for (JavacNode param : fillParametersFrom.down()) {
				if (param.getKind() != Kind.ARGUMENT) continue;
				BuilderFieldData bfd = new BuilderFieldData();
				
				JCVariableDecl raw = (JCVariableDecl) param.get();
				bfd.name = raw.name;
				bfd.builderFieldName = bfd.name;
				bfd.rawName = raw.name;
				bfd.annotations = findCopyableAnnotations(param);
				bfd.type = raw.vartype;
				bfd.singularData = getSingularData(param, annInstance.setterPrefix());
				bfd.originalFieldNode = param;
				addObtainVia(bfd, param);
				job.builderFields.add(bfd);
			}
		}
		
		job.builderType = findInnerClass(job.parentType, job.builderClassName);
		if (job.builderType == null) {
			job.builderType = makeBuilderClass(job);
			recursiveSetGeneratedBy(job.builderType.get(), ast, annotationNode.getContext());
		} else {
			JCClassDecl builderTypeDeclaration = (JCClassDecl) job.builderType.get();
			if (job.isStatic && !builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)) {
				annotationNode.addError("Existing Builder must be a static inner class.");
				return;
			} else if (!job.isStatic && builderTypeDeclaration.getModifiers().getFlags().contains(Modifier.STATIC)) {
				annotationNode.addError("Existing Builder must be a non-static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(job.builderType, annotationNode);
			/* generate errors for @Singular BFDs that have one already defined node. */ {
				for (BuilderFieldData bfd : job.builderFields) {
					SingularData sd = bfd.singularData;
					if (sd == null) continue;
					JavacSingularizer singularizer = sd.getSingularizer();
					if (singularizer == null) continue;
					if (singularizer.checkForAlreadyExistingNodesAndGenerateError(job.builderType, sd)) {
						bfd.singularData = null;
					}
				}
			}
		}
		
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
		
		generateBuilderFields(job);
		if (addCleaning) {
			JavacTreeMaker maker = job.getTreeMaker();
			JCVariableDecl uncleanField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), job.builderType.toName(CLEAN_FIELD_NAME), maker.TypeIdent(CTC_BOOLEAN), null);
			injectFieldAndMarkGenerated(job.builderType, uncleanField);
			recursiveSetGeneratedBy(uncleanField, ast, annotationNode.getContext());
		}
		
		if (constructorExists(job.builderType) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PACKAGE, List.<JCAnnotation>nil(), job.builderType, List.<JavacNode>nil(), false, annotationNode);
			if (cd != null) injectMethod(job.builderType, cd);
		}
		
		for (BuilderFieldData bfd : job.builderFields) {
			makePrefixedSetterMethodsForBuilder(job, bfd, annInstance.setterPrefix());
		}
		
		{
			MemberExistsResult methodExists = methodExists(job.buildMethodName, job.builderType, -1);
			if (methodExists == MemberExistsResult.EXISTS_BY_LOMBOK) methodExists = methodExists(job.buildMethodName, job.builderType, 0);
			if (methodExists == MemberExistsResult.NOT_EXISTS) {
				JCMethodDecl md = generateBuildMethod(job, nameOfBuilderMethod, buildMethodReturnType, buildMethodThrownExceptions, addCleaning);
				if (md != null) {
					injectMethod(job.builderType, md);
					recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
				}
			}
		}
		
		if (methodExists("toString", job.builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			java.util.List<Included<JavacNode, ToString.Include>> fieldNodes = new ArrayList<Included<JavacNode, ToString.Include>>();
			for (BuilderFieldData bfd : job.builderFields) {
				for (JavacNode f : bfd.createdFields) {
					fieldNodes.add(new Included<JavacNode, ToString.Include>(f, null, true, false));
				}
			}
			
			JCMethodDecl md = HandleToString.createToString(job.builderType, fieldNodes, true, false, FieldAccess.ALWAYS_FIELD, ast);
			if (md != null) injectMethod(job.builderType, md);
		}
		
		if (addCleaning) injectMethod(job.builderType, generateCleanMethod(job));
		
		if (generateBuilderMethod && methodExists(job.builderMethodName, job.parentType, -1) != MemberExistsResult.NOT_EXISTS) generateBuilderMethod = false;
		if (generateBuilderMethod) {
			JCMethodDecl md = generateBuilderMethod(job);
			recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
			if (md != null) injectMethod(job.parentType, md);
		}
		
		if (job.toBuilder) {
			switch (methodExists(TO_BUILDER_METHOD_NAME, job.parentType, 0)) {
			case EXISTS_BY_USER:
				annotationNode.addWarning("Not generating toBuilder() as it already exists.");
				return;
			case NOT_EXISTS:
				List<JCTypeParameter> tps = job.typeParams;
				if (typeArgsForToBuilder != null) {
					ListBuffer<JCTypeParameter> lb = new ListBuffer<JCTypeParameter>();
					JavacTreeMaker maker = job.getTreeMaker();
					for (Name n : typeArgsForToBuilder) {
						lb.append(maker.TypeParameter(n, List.<JCExpression>nil()));
					}
					tps = lb.toList();
				}
				JCMethodDecl md = generateToBuilderMethod(job, tps, annInstance.setterPrefix());
				if (md != null) {
					recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
					injectMethod(job.parentType, md);
				}
			}
		}
		
		if (nonFinalNonDefaultedFields != null && generateBuilderMethod) {
			for (JavacNode fieldNode : nonFinalNonDefaultedFields) {
				fieldNode.addWarning("@Builder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
		}
	}

	static String returnTypeToBuilderClassName(JavacNode annotationNode, JCClassDecl td, JCExpression returnType, List<JCTypeParameter> typeParams) {
		String replStr = null;
		if (returnType instanceof JCFieldAccess) {
			replStr = ((JCFieldAccess) returnType).name.toString();
		} else if (returnType instanceof JCIdent) {
			Name n = ((JCIdent) returnType).name;
			
			for (JCTypeParameter tp : typeParams) {
				if (tp.name.equals(n)) {
					annotationNode.addError("@Builder requires specifying 'builderClassName' if used on methods with a type parameter as return type.");
					return null;
				}
			}
			replStr = n.toString();
		} else if (returnType instanceof JCPrimitiveTypeTree) {
			replStr = returnType.toString();
			if (Character.isLowerCase(replStr.charAt(0))) {
				replStr = Character.toTitleCase(replStr.charAt(0)) + replStr.substring(1);
			}
		} else if (returnType instanceof JCTypeApply) {
			JCExpression clazz = ((JCTypeApply) returnType).clazz;
			if (clazz instanceof JCFieldAccess) {
				replStr = ((JCFieldAccess) clazz).name.toString();
			} else if (clazz instanceof JCIdent) {
				replStr = ((JCIdent) clazz).name.toString();
			}
		}
		
		if (replStr == null || replStr.isEmpty()) {
			// This shouldn't happen.
			System.err.println("Lombok bug ID#20140614-1651: javac HandleBuilder: return type to name conversion failed: " + returnType.getClass());
			replStr = td.name.toString();
		}
		return replStr;
	}
	
	private static String unpack(JCExpression expr) {
		StringBuilder sb = new StringBuilder();
		unpack(sb, expr);
		return sb.toString();
	}
	
	private static void unpack(StringBuilder sb, JCExpression expr) {
		if (expr instanceof JCIdent) {
			sb.append(((JCIdent) expr).name.toString());
			return;
		}
		
		if (expr instanceof JCFieldAccess) {
			JCFieldAccess jcfa = (JCFieldAccess) expr;
			unpack(sb, jcfa.selected);
			sb.append(".").append(jcfa.name.toString());
			return;
		}
		
		if (expr instanceof JCTypeApply) {
			sb.setLength(0);
			sb.append("ERR:");
			sb.append("@Builder(toBuilder=true) is not supported if returning a type with generics applied to an intermediate.");
			sb.append("__ERR__");
			return;
		}
		
		sb.setLength(0);
		sb.append("ERR:");
		sb.append("Expected a type of some sort, not a " + expr.getClass().getName());
		sb.append("__ERR__");
	}
	
	private JCMethodDecl generateToBuilderMethod(BuilderJob job, List<JCTypeParameter> typeParameters, String prefix) {
		// return new ThingieBuilder<A, B>().setA(this.a).setB(this.b);
		JavacTreeMaker maker = job.getTreeMaker();
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : typeParameters) {
			typeArgs.append(maker.Ident(typeParam.name));
		}
		
		JCExpression call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderClassName), !job.isStatic, job.builderTypeParams), List.<JCExpression>nil(), null);
		JCExpression invoke = call;
		ListBuffer<JCStatement> preStatements = null;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		for (BuilderFieldData bfd : job.builderFields) {
			String setterPrefix = !prefix.isEmpty() ? prefix : job.oldFluent ? "" : "set";
			String prefixedSetterName = bfd.name.toString();
			if (!setterPrefix.isEmpty()) prefixedSetterName = HandlerUtil.buildAccessorName(setterPrefix, prefixedSetterName);
			
			Name setterName = job.toName(prefixedSetterName);
			JCExpression[] tgt = new JCExpression[bfd.singularData == null ? 1 : 2];
			if (bfd.obtainVia == null || !bfd.obtainVia.field().isEmpty()) {
				for (int i = 0; i < tgt.length; i++) {
					tgt[i] = maker.Select(maker.Ident(job.toName("this")), bfd.obtainVia == null ? bfd.rawName : job.toName(bfd.obtainVia.field()));
				}
			} else {
				String name = bfd.obtainVia.method();
				JCMethodInvocation inv;
				if (bfd.obtainVia.isStatic()) {
					JCExpression c = maker.Select(maker.Ident(job.toName(job.parentType.getName())), job.toName(name));
					inv = maker.Apply(typeParameterNames(maker, typeParameters), c, List.<JCExpression>of(maker.Ident(job.toName("this"))));
				} else {
					JCExpression c = maker.Select(maker.Ident(job.toName("this")), job.toName(name));
					inv = maker.Apply(List.<JCExpression>nil(), c, List.<JCExpression>nil());
				}
				for (int i = 0; i < tgt.length; i++) tgt[i] = maker.Ident(bfd.name);
				
				// javac appears to cache the type of JCMethodInvocation expressions based on position, meaning, if you have 2 ObtainVia-based method invokes on different types, you get bizarre type mismatch errors.
				// going via a local variable declaration solves the problem.
				JCExpression varType = JavacHandlerUtil.cloneType(maker, bfd.type, job.source, job.getContext());
				if (preStatements == null) preStatements = new ListBuffer<JCStatement>();
				preStatements.append(maker.VarDef(maker.Modifiers(Flags.FINAL), bfd.name, varType, inv));
			}
			
			JCExpression arg;
			if (bfd.singularData == null) {
				arg = tgt[0];
				invoke = maker.Apply(List.<JCExpression>nil(), maker.Select(invoke, setterName), List.of(arg));
			} else {
				JCExpression isNotNull = maker.Binary(CTC_NOT_EQUAL, tgt[0], maker.Literal(CTC_BOT, null));
				JCExpression invokeBuilder = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(job.toName(BUILDER_TEMP_VAR)), setterName), List.<JCExpression>of(tgt[1]));
				statements.append(maker.If(isNotNull, maker.Exec(invokeBuilder), null));
			}
		}
		
		if (!statements.isEmpty()) {
			JCExpression tempVarType = namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), !job.isStatic, typeParameters);
			statements.prepend(maker.VarDef(maker.Modifiers(Flags.FINAL), job.toName(BUILDER_TEMP_VAR), tempVarType, invoke));
			statements.append(maker.Return(maker.Ident(job.toName(BUILDER_TEMP_VAR))));
		} else {
			statements.append(maker.Return(invoke));
		}
		
		if (preStatements != null) {
			preStatements.appendList(statements);
			statements = preStatements;
		}
		JCBlock body = maker.Block(0, statements.toList());
		List<JCAnnotation> annsOnParamType = List.nil();
		if (job.checkerFramework.generateUnique()) annsOnParamType = List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__UNIQUE), List.<JCExpression>nil()));
		JCMethodDecl methodDef = maker.MethodDef(maker.Modifiers(toJavacModifier(job.accessOuters)), job.toName(TO_BUILDER_METHOD_NAME), namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), !job.isStatic, typeParameters, annsOnParamType), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		createRelevantNonNullAnnotation(job.parentType, methodDef);
		return methodDef;
	}
	
	private JCMethodDecl generateCleanMethod(BuilderJob job) {
		JavacTreeMaker maker = job.getTreeMaker();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendCleaningCode(bfd.singularData, job.builderType, job.source, statements);
			}
		}
		
		statements.append(maker.Exec(maker.Assign(maker.Select(maker.Ident(job.toName("this")), job.toName(CLEAN_FIELD_NAME)), maker.Literal(CTC_BOOLEAN, 0))));
		JCBlock body = maker.Block(0, statements.toList());
		JCMethodDecl method = maker.MethodDef(maker.Modifiers(toJavacModifier(AccessLevel.PRIVATE)), job.toName(CLEAN_METHOD_NAME), maker.Type(Javac.createVoidType(job.builderType.getSymbolTable(), CTC_VOID)), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		recursiveSetGeneratedBy(method, job.source, job.getContext());
		return method;
	}
	
	static JCVariableDecl generateReceiver(BuilderJob job) {
		if (!job.checkerFramework.generateCalledMethods()) return null;
		
		ArrayList<String> mandatories = new ArrayList<String>();
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.singularData == null && bfd.nameOfSetFlag == null) mandatories.add(bfd.name.toString());
		}
		
		JCExpression arg;
		JavacTreeMaker maker = job.getTreeMaker();
		if (mandatories.size() == 0) return null;
		if (mandatories.size() == 1) arg = maker.Literal(mandatories.get(0));
		else {
			List<JCExpression> elems = List.nil();
			for (int i = mandatories.size() - 1; i >= 0; i--) elems = elems.prepend(maker.Literal(mandatories.get(i)));
			arg = maker.NewArray(null, List.<JCExpression>nil(), elems);
		}
		JCAnnotation recvAnno = maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__CALLED), List.of(arg));
		JCClassDecl builderTypeNode = (JCClassDecl) job.builderType.get();
		JCVariableDecl recv = maker.VarDef(maker.Modifiers(Flags.PARAMETER, List.<JCAnnotation>nil()), job.toName("this"), namePlusTypeParamsToTypeReference(maker, job.builderType, builderTypeNode.typarams, List.<JCAnnotation>of(recvAnno)), null);
		return recv;
	}
	
	private JCMethodDecl generateBuildMethod(BuilderJob job, Name staticName, JCExpression returnType, List<JCExpression> thrownExceptions, boolean addCleaning) {
		JavacTreeMaker maker = job.getTreeMaker();
		
		JCExpression call;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		if (addCleaning) {
			JCExpression notClean = maker.Unary(CTC_NOT, maker.Select(maker.Ident(job.toName("this")), job.toName(CLEAN_FIELD_NAME)));
			JCStatement invokeClean = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(job.toName(CLEAN_METHOD_NAME)), List.<JCExpression>nil()));
			JCIf ifUnclean = maker.If(notClean, invokeClean, null);
			statements.append(ifUnclean);
		}
		
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, job.builderType, job.source, statements, bfd.builderFieldName, "this");
			}
		}
		
		ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
		Name thisName = job.toName("this");
		for (BuilderFieldData bfd : job.builderFields) {
			if (bfd.nameOfSetFlag != null) {
				statements.append(maker.VarDef(maker.Modifiers(0L), bfd.builderFieldName, cloneType(maker, bfd.type, job.source, job.getContext()), maker.Select(maker.Ident(thisName), bfd.builderFieldName)));
				statements.append(maker.If(maker.Unary(CTC_NOT, maker.Select(maker.Ident(thisName), bfd.nameOfSetFlag)), maker.Exec(maker.Assign(maker.Ident(bfd.builderFieldName), maker.Apply(typeParameterNames(maker, ((JCClassDecl) job.parentType.get()).typarams), maker.Select(maker.Ident(((JCClassDecl) job.parentType.get()).name), bfd.nameOfDefaultProvider), List.<JCExpression>nil()))), null));
			}
			if (bfd.nameOfSetFlag != null || (bfd.singularData != null && bfd.singularData.getSingularizer().shadowedDuringBuild())) {
				args.append(maker.Ident(bfd.builderFieldName));
			} else {
				args.append(maker.Select(maker.Ident(thisName), bfd.builderFieldName));
			}
		}
		
		if (addCleaning) {
			statements.append(maker.Exec(maker.Assign(maker.Select(maker.Ident(job.toName("this")), job.toName(CLEAN_FIELD_NAME)), maker.Literal(CTC_BOOLEAN, 1))));
		}
		
		if (staticName == null) {
			call = maker.NewClass(null, List.<JCExpression>nil(), returnType, args.toList(), null);
			statements.append(maker.Return(call));
		} else {
			ListBuffer<JCExpression> typeParams = new ListBuffer<JCExpression>();
			for (JCTypeParameter tp : ((JCClassDecl) job.builderType.get()).typarams) {
				typeParams.append(maker.Ident(tp.name));
			}
			JCExpression callee = maker.Ident(((JCClassDecl) job.parentType.get()).name);
			if (!job.isStatic) callee = maker.Select(callee, job.toName("this"));
			JCExpression fn = maker.Select(callee, staticName);
			call = maker.Apply(typeParams.toList(), fn, args.toList());
			if (returnType instanceof JCPrimitiveTypeTree && CTC_VOID.equals(typeTag(returnType))) {
				statements.append(maker.Exec(call));
			} else {
				statements.append(maker.Return(call));
			}
		}
		
		JCBlock body = maker.Block(0, statements.toList());
		
		List<JCAnnotation> annsOnMethod = job.checkerFramework.generateSideEffectFree() ? List.of(maker.Annotation(genTypeRef(job.builderType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil())) : List.<JCAnnotation>nil();
		JCVariableDecl recv = generateReceiver(job);
		JCMethodDecl methodDef;
		if (recv != null && maker.hasMethodDefWithRecvParam()) {
			methodDef = maker.MethodDefWithRecvParam(maker.Modifiers(toJavacModifier(job.accessInners), annsOnMethod), job.toName(job.buildMethodName), returnType, List.<JCTypeParameter>nil(), recv, List.<JCVariableDecl>nil(), thrownExceptions, body, null);
		} else {
			methodDef = maker.MethodDef(maker.Modifiers(toJavacModifier(job.accessInners), annsOnMethod), job.toName(job.buildMethodName), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
		}
		if (staticName == null) createRelevantNonNullAnnotation(job.builderType, methodDef);
		return methodDef;
	}
	
	public static JCMethodDecl generateDefaultProvider(Name methodName, JavacNode fieldNode, List<JCTypeParameter> params) {
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		
		JCStatement statement = maker.Return(field.init);
		field.init = null;
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = Flags.PRIVATE | Flags.STATIC;
		return maker.MethodDef(maker.Modifiers(modifiers), methodName, cloneType(maker, field.vartype, field, fieldNode.getContext()), copyTypeParams(fieldNode, params), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	public JCMethodDecl generateBuilderMethod(BuilderJob job) {
		//String builderClassName, JavacNode source, JavacNode type, List<JCTypeParameter> typeParams, AccessLevel access) {
		//builderClassName, annotationNode, tdParent, typeParams, accessForOuters);

		JavacTreeMaker maker = job.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : job.typeParams) {
			typeArgs.append(maker.Ident(typeParam.name));
		}
		
		JCExpression call;
		if (job.isStatic) {
			call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, job.parentType, job.toName(job.builderClassName), false, job.typeParams), List.<JCExpression>nil(), null);
		} else {
			call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, null, job.toName(job.builderClassName), false, job.typeParams), List.<JCExpression>nil(), null);
			((JCNewClass) call).encl = maker.Ident(job.toName("this"));
			
		}
		JCStatement statement = maker.Return(call);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		int modifiers = toJavacModifier(job.accessOuters);
		if (job.isStatic) modifiers |= Flags.STATIC;
		List<JCAnnotation> annsOnMethod = List.nil();
		if (job.checkerFramework.generateSideEffectFree()) annsOnMethod = List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		List<JCAnnotation> annsOnParamType = List.nil();
		if (job.checkerFramework.generateUnique()) annsOnParamType = List.of(maker.Annotation(genTypeRef(job.parentType, CheckerFrameworkVersion.NAME__UNIQUE), List.<JCExpression>nil()));
		
		JCExpression returnType = namePlusTypeParamsToTypeReference(maker, job.parentType, job.getBuilderClassName(), !job.isStatic, job.builderTypeParams, annsOnParamType);
		JCMethodDecl methodDef = maker.MethodDef(maker.Modifiers(modifiers, annsOnMethod), job.toName(job.builderMethodName), returnType, job.copyTypeParams(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		createRelevantNonNullAnnotation(job.parentType, methodDef);
		return methodDef;
	}
	
	public void generateBuilderFields(BuilderJob job) {
		int len = job.builderFields.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : job.builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		java.util.List<JCVariableDecl> generated = new ArrayList<JCVariableDecl>();
		
		for (int i = len - 1; i >= 0; i--) {
			BuilderFieldData bfd = job.builderFields.get(i);
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.createdFields.addAll(bfd.singularData.getSingularizer().generateFields(bfd.singularData, job.builderType, job.source));
			} else {
				JavacNode field = null, setFlag = null;
				for (JavacNode exists : existing) {
					Name n = ((JCVariableDecl) exists.get()).name;
					if (n.equals(bfd.builderFieldName)) field = exists;
					if (n.equals(bfd.nameOfSetFlag)) setFlag = exists;
				}
				JavacTreeMaker maker = job.getTreeMaker();
				if (field == null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.builderFieldName, cloneType(maker, bfd.type, job.source, job.getContext()), null);
					field = injectFieldAndMarkGenerated(job.builderType, newField);
					generated.add(newField);
				}
				if (setFlag == null && bfd.nameOfSetFlag != null) {
					JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
					JCVariableDecl newField = maker.VarDef(mods, bfd.nameOfSetFlag, maker.TypeIdent(CTC_BOOLEAN), null);
					injectFieldAndMarkGenerated(job.builderType, newField);
					generated.add(newField);
				}
				bfd.createdFields.add(field);
			}
		}
		for (JCVariableDecl gen : generated)  recursiveSetGeneratedBy(gen, job.source, job.getContext());
	}
	
	public void makePrefixedSetterMethodsForBuilder(BuilderJob job, BuilderFieldData bfd, String prefix) {
		boolean deprecate = isFieldDeprecated(bfd.originalFieldNode);
		if (bfd.singularData == null || bfd.singularData.getSingularizer() == null) {
			makePrefixedSetterMethodForBuilder(job, bfd, deprecate, prefix);
		} else {
			bfd.singularData.getSingularizer().generateMethods(job, bfd.singularData, deprecate);
		}
	}
	
	private void makePrefixedSetterMethodForBuilder(BuilderJob job, BuilderFieldData bfd, boolean deprecate, String prefix) {
		JavacNode fieldNode = bfd.createdFields.get(0);
		String setterPrefix = !prefix.isEmpty() ? prefix : job.oldFluent ? "" : "set";
		String setterName = HandlerUtil.buildAccessorName(setterPrefix, bfd.name.toString());
		Name setterName_ = job.builderType.toName(setterName);
		
		for (JavacNode child : job.builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			JCMethodDecl methodDecl = (JCMethodDecl) child.get();
			Name existingName = methodDecl.name;
			if (existingName.equals(setterName_) && !isTolerate(fieldNode, methodDecl)) return;
		}
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		List<JCAnnotation> methodAnns = JavacHandlerUtil.findCopyableToSetterAnnotations(bfd.originalFieldNode);
		JCMethodDecl newMethod = null;
		if (job.checkerFramework.generateCalledMethods() && maker.hasMethodDefWithRecvParam()) {
			JCAnnotation ncAnno = maker.Annotation(genTypeRef(job.sourceNode, CheckerFrameworkVersion.NAME__NOT_CALLED), List.<JCExpression>of(maker.Literal(setterName.toString())));
			JCClassDecl builderTypeNode = (JCClassDecl) job.builderType.get();
			JCExpression selfType = namePlusTypeParamsToTypeReference(maker, job.builderType, builderTypeNode.typarams, List.<JCAnnotation>of(ncAnno));
			JCVariableDecl recv = maker.VarDef(maker.Modifiers(Flags.PARAMETER, List.<JCAnnotation>nil()), job.builderType.toName("this"), selfType, null);
			newMethod = HandleSetter.createSetterWithRecv(toJavacModifier(job.accessInners), deprecate, fieldNode, maker, setterName, bfd.name, bfd.nameOfSetFlag, job.oldChain, job.sourceNode, methodAnns, bfd.annotations, recv);
		}
		if (newMethod == null) newMethod = HandleSetter.createSetter(toJavacModifier(job.accessInners), deprecate, fieldNode, maker, setterName, bfd.name, bfd.nameOfSetFlag, job.oldChain, job.sourceNode, methodAnns, bfd.annotations);
		recursiveSetGeneratedBy(newMethod, job.source, job.getContext());
		if (job.sourceNode.up().getKind() == Kind.METHOD) {
			copyJavadocFromParam(bfd.originalFieldNode.up(), newMethod, bfd.name.toString());
		} else {
			copyJavadoc(bfd.originalFieldNode, newMethod, CopyJavadoc.SETTER, true);
		}
		
		injectMethod(job.builderType, newMethod);
	}
	
	private void copyJavadocFromParam(JavacNode from, JCMethodDecl to, String param) {
		try {
			JCCompilationUnit cu = ((JCCompilationUnit) from.top().get());
			String methodComment = Javac.getDocComment(cu, from.get());
			String newJavadoc = addReturnsThisIfNeeded(getParamJavadoc(methodComment, param));
			Javac.setDocComment(cu, to, newJavadoc);
		} catch (Exception ignore) {}
	}	
	
	public JavacNode makeBuilderClass(BuilderJob job) {
		//boolean isStatic, JavacNode source, JavacNode tdParent, String builderClassName, List<JCTypeParameter> typeParams, JCAnnotation ast, AccessLevel access) {
		//isStatic, annotationNode, tdParent, builderClassName, typeParams, ast, accessForOuters
		JavacTreeMaker maker = job.getTreeMaker();
		int modifiers = toJavacModifier(job.accessOuters);
		if (job.isStatic) modifiers |= Flags.STATIC;
		JCModifiers mods = maker.Modifiers(modifiers);
		JCClassDecl builder = maker.ClassDef(mods, job.getBuilderClassName(), job.copyTypeParams(), null, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(job.parentType, builder);
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
	 * @param setterPrefix Explicitly requested setter prefix.
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
			JavacSingularizer singularizer = JavacSingularsRecipes.get().getSingularizer(targetFqn, node);
			if (singularizer == null) {
				node.addError("Lombok does not know how to create the singular-form builder methods for type '" + name + "'; they won't be generated.");
				return null;
			}
			
			return new SingularData(child, singularName, pluralName, typeArgs, targetFqn, singularizer, singularInstance.ignoreNullCollections(), setterPrefix);
		}
		
		return null;
	}
}
