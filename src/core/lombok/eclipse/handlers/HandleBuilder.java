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
package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.*;
import static lombok.core.handlers.HandlerUtil.*;
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
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
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
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.experimental.NonFinal;

@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleBuilder extends EclipseAnnotationHandler<Builder> {
	private HandleConstructor handleConstructor = new HandleConstructor();
	
	private static final char[] CLEAN_FIELD_NAME = "$lombokUnclean".toCharArray();
	private static final char[] CLEAN_METHOD_NAME = "$lombokClean".toCharArray();
	
	private static final boolean toBoolean(Object expr, boolean defaultValue) {
		if (expr == null) return defaultValue;
		if (expr instanceof FalseLiteral) return false;
		if (expr instanceof TrueLiteral) return true;
		return ((Boolean) expr).booleanValue();
	}
	
	static class BuilderFieldData {
		Annotation[] annotations;
		TypeReference type;
		char[] rawName;
		char[] name;
		char[] builderFieldName;
		char[] nameOfDefaultProvider;
		char[] nameOfSetFlag;
		SingularData singularData;
		ObtainVia obtainVia;
		EclipseNode obtainViaNode;
		EclipseNode originalFieldNode;
		
		List<EclipseNode> createdFields = new ArrayList<EclipseNode>();
	}
	
	private static boolean equals(String a, char[] b) {
		if (a.length() != b.length) return false;
		for (int i = 0; i < b.length; i++) {
			if (a.charAt(i) != b[i]) return false;
		}
		return true;
	}
	
	private static boolean equals(String a, char[][] b) {
		if (a == null || a.isEmpty()) return b.length == 0;
		String[] aParts = a.split("\\.");
		if (aParts.length != b.length) return false;
		for (int i = 0; i < b.length; i++) {
			if (!equals(aParts[i], b[i])) return false;
		}
		return true;
	}
	
	private static final char[] DEFAULT_PREFIX = {'$', 'd', 'e', 'f', 'a', 'u', 'l', 't', '$'};
	private static final char[] SET_PREFIX = {'$', 's', 'e', 't'};
	private static final char[] VALUE_PREFIX = {'$', 'v', 'a', 'l', 'u', 'e'};
	
	private static final char[] prefixWith(char[] prefix, char[] name) {
		char[] out = new char[prefix.length + name.length];
		System.arraycopy(prefix, 0, out, 0, prefix.length);
		System.arraycopy(name, 0, out, prefix.length, name.length);
		return out;
	}
	
	@Override public void handle(AnnotationValues<Builder> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.BUILDER_FLAG_USAGE, "@Builder");
		CheckerFrameworkVersion cfv = getCheckerFrameworkVersion(annotationNode);
		
		long p = (long) ast.sourceStart << 32 | ast.sourceEnd;
		
		Builder builderInstance = annotation.getInstance();
		AccessLevel accessForOuters = builderInstance.access();
		if (accessForOuters == null) accessForOuters = AccessLevel.PUBLIC;
		if (builderInstance.access() == AccessLevel.NONE) {
			annotationNode.addError("AccessLevel.NONE is not valid here");
			accessForOuters = AccessLevel.PUBLIC;
		}
		AccessLevel accessForInners = accessForOuters == AccessLevel.PROTECTED ? AccessLevel.PUBLIC : accessForOuters;
		
		// These exist just to support the 'old' lombok.experimental.Builder, which had these properties. lombok.Builder no longer has them.
		boolean fluent = toBoolean(annotation.getActualExpression("fluent"), true);
		boolean chain = toBoolean(annotation.getActualExpression("chain"), true);
		
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		String builderClassName = builderInstance.builderClassName();
		String toBuilderMethodName = "toBuilder";
		boolean toBuilder = builderInstance.toBuilder();
		List<char[]> typeArgsForToBuilder = null;
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		if (builderClassName == null) builderClassName = "";
		
		boolean generateBuilderMethod;
		if (builderMethodName.isEmpty()) {
			generateBuilderMethod = false;
		} else if (!checkName("builderMethodName", builderMethodName, annotationNode)) {
			return;
		} else {
			generateBuilderMethod = true;
		}
		
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		if (!builderClassName.isEmpty()) {
			if (!checkName("builderClassName", builderClassName, annotationNode)) return;
		}
		
		EclipseNode parent = annotationNode.up();
		
		List<BuilderFieldData> builderFields = new ArrayList<BuilderFieldData>();
		TypeReference returnType;
		TypeParameter[] typeParams;
		TypeReference[] thrownExceptions;
		char[] nameOfStaticBuilderMethod;
		EclipseNode tdParent;
		
		EclipseNode fillParametersFrom = parent.get() instanceof AbstractMethodDeclaration ? parent : null;
		boolean addCleaning = false;
		boolean isStatic = true;
		
		List<EclipseNode> nonFinalNonDefaultedFields = null;
		
		if (builderClassName.isEmpty()) builderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.BUILDER_CLASS_NAME);
		if (builderClassName == null || builderClassName.isEmpty()) builderClassName = "*Builder";
		boolean replaceNameInBuilderClassName = builderClassName.contains("*");
		
		if (parent.get() instanceof TypeDeclaration) {
			tdParent = parent;
			TypeDeclaration td = (TypeDeclaration) tdParent.get();
			
			List<EclipseNode> allFields = new ArrayList<EclipseNode>();
			boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation("lombok.experimental.Value", parent));
			for (EclipseNode fieldNode : HandleConstructor.findAllFields(tdParent, true)) {
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
				bfd.singularData = getSingularData(fieldNode, ast, builderInstance.setterPrefix());
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
					
					MethodDeclaration md = generateDefaultProvider(bfd.nameOfDefaultProvider, td.typeParameters, fieldNode, ast);
					if (md != null) injectMethod(tdParent, md);
				}
				addObtainVia(bfd, fieldNode);
				builderFields.add(bfd);
				allFields.add(fieldNode);
			}
			
			handleConstructor.generateConstructor(tdParent, AccessLevel.PACKAGE, allFields, false, null, SkipIfConstructorExists.I_AM_BUILDER,
				Collections.<Annotation>emptyList(), annotationNode);
			
			returnType = namePlusTypeParamsToTypeReference(tdParent, td.typeParameters, p);
			typeParams = td.typeParameters;
			thrownExceptions = null;
			nameOfStaticBuilderMethod = null;
			if (replaceNameInBuilderClassName) builderClassName = builderClassName.replace("*", new String(td.name));
			replaceNameInBuilderClassName = false;
		} else if (parent.get() instanceof ConstructorDeclaration) {
			ConstructorDeclaration cd = (ConstructorDeclaration) parent.get();
			if (cd.typeParameters != null && cd.typeParameters.length > 0) {
				annotationNode.addError("@Builder is not supported on constructors with constructor type parameters.");
				return;
			}
			
			tdParent = parent.up();
			TypeDeclaration td = (TypeDeclaration) tdParent.get();
			returnType = namePlusTypeParamsToTypeReference(tdParent, td.typeParameters, p);
			typeParams = td.typeParameters;
			thrownExceptions = cd.thrownExceptions;
			nameOfStaticBuilderMethod = null;
			if (replaceNameInBuilderClassName) builderClassName = builderClassName.replace("*", new String(cd.selector));
			replaceNameInBuilderClassName = false;
		} else if (parent.get() instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration) parent.get();
			tdParent = parent.up();
			isStatic = md.isStatic();
			
			if (toBuilder) {
				final String TO_BUILDER_NOT_SUPPORTED = "@Builder(toBuilder=true) is only supported if you return your own type.";
				char[] token;
				char[][] pkg = null;
				if (md.returnType.dimensions() > 0) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				if (md.returnType instanceof SingleTypeReference) {
					token = ((SingleTypeReference) md.returnType).token;
				} else if (md.returnType instanceof QualifiedTypeReference) {
					pkg = ((QualifiedTypeReference) md.returnType).tokens;
					token = pkg[pkg.length];
					char[][] pkg_ = new char[pkg.length - 1][];
					System.arraycopy(pkg, 0, pkg_, 0, pkg_.length);
					pkg = pkg_;
				} else {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				if (pkg != null && !equals(parent.getPackageDeclaration(), pkg)) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				if (tdParent == null || !equals(tdParent.getName(), token)) {
					annotationNode.addError(TO_BUILDER_NOT_SUPPORTED);
					return;
				}
				
				TypeParameter[] tpOnType = ((TypeDeclaration) tdParent.get()).typeParameters;
				TypeParameter[] tpOnMethod = md.typeParameters;
				TypeReference[][] tpOnRet_ = null;
				if (md.returnType instanceof ParameterizedSingleTypeReference) {
					tpOnRet_ = new TypeReference[1][];
					tpOnRet_[0] = ((ParameterizedSingleTypeReference) md.returnType).typeArguments;
				} else if (md.returnType instanceof ParameterizedQualifiedTypeReference) {
					tpOnRet_ = ((ParameterizedQualifiedTypeReference) md.returnType).typeArguments;
				}
				
				if (tpOnRet_ != null) for (int i = 0; i < tpOnRet_.length - 1; i++) {
					if (tpOnRet_[i] != null && tpOnRet_[i].length > 0) {
						annotationNode.addError("@Builder(toBuilder=true) is not supported if returning a type with generics applied to an intermediate.");
						return;
					}
				}
				TypeReference[] tpOnRet = tpOnRet_ == null ? null : tpOnRet_[tpOnRet_.length - 1];
				typeArgsForToBuilder = new ArrayList<char[]>();
				
				// Every typearg on this method needs to be found in the return type, but the reverse is not true.
				// We also need to 'map' them.
				
				
				if (tpOnMethod != null) for (TypeParameter onMethod : tpOnMethod) {
					int pos = -1;
					if (tpOnRet != null) for (int i = 0; i < tpOnRet.length; i++) {
						if (tpOnRet[i].getClass() != SingleTypeReference.class) continue;
						if (!Arrays.equals(((SingleTypeReference) tpOnRet[i]).token, onMethod.name)) continue;
						pos = i;
					}
					if (pos == -1 || tpOnType == null || tpOnType.length <= pos) {
						annotationNode.addError("@Builder(toBuilder=true) requires that each type parameter on the static method is part of the typeargs of the return value. Type parameter " + new String(onMethod.name) + " is not part of the return type.");
						return;
					}
					
					typeArgsForToBuilder.add(tpOnType[pos].name);
				}
			}
			
			returnType = copyType(md.returnType, ast);
			typeParams = md.typeParameters;
			thrownExceptions = md.thrownExceptions;
			nameOfStaticBuilderMethod = md.selector;
			if (replaceNameInBuilderClassName) {
				char[] token;
				if (md.returnType instanceof QualifiedTypeReference) {
					char[][] tokens = ((QualifiedTypeReference) md.returnType).tokens;
					token = tokens[tokens.length - 1];
				} else if (md.returnType instanceof SingleTypeReference) {
					token = ((SingleTypeReference) md.returnType).token;
					if (!(md.returnType instanceof ParameterizedSingleTypeReference) && typeParams != null) {
						for (TypeParameter tp : typeParams) {
							if (Arrays.equals(tp.name, token)) {
								annotationNode.addError("@Builder requires specifying 'builderClassName' if used on methods with a type parameter as return type.");
								return;
							}
						}
					}
				} else {
					annotationNode.addError("Unexpected kind of return type on annotated method. Specify 'builderClassName' to solve this problem.");
					return;
				}
				
				if (Character.isLowerCase(token[0])) {
					char[] newToken = new char[token.length];
					System.arraycopy(token, 1, newToken, 1, token.length - 1);
					newToken[0] = Character.toTitleCase(token[0]);
					token = newToken;
				}
				
				builderClassName = builderClassName.replace("*", new String(token));
			}
		} else {
			annotationNode.addError("@Builder is only supported on types, constructors, and methods.");
			return;
		}
		
		if (fillParametersFrom != null) {
			for (EclipseNode param : fillParametersFrom.down()) {
				if (param.getKind() != Kind.ARGUMENT) continue;
				BuilderFieldData bfd = new BuilderFieldData();
				Argument arg = (Argument) param.get();
				
				Annotation[] copyableAnnotations = findCopyableAnnotations(param);
				
				bfd.rawName = arg.name;
				bfd.name = arg.name;
				bfd.builderFieldName = bfd.name;
				bfd.annotations = copyAnnotations(arg, copyableAnnotations);
				bfd.type = arg.type;
				bfd.singularData = getSingularData(param, ast, builderInstance.setterPrefix());
				bfd.originalFieldNode = param;
				addObtainVia(bfd, param);
				builderFields.add(bfd);
			}
		}
		
		EclipseNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = makeBuilderClass(isStatic, tdParent, builderClassName, typeParams, ast, accessForOuters);
		} else {
			TypeDeclaration builderTypeDeclaration = (TypeDeclaration) builderType.get();
			if (isStatic && (builderTypeDeclaration.modifiers & ClassFileConstants.AccStatic) == 0) {
				annotationNode.addError("Existing Builder must be a static inner class.");
				return;
			} else if (!isStatic && (builderTypeDeclaration.modifiers & ClassFileConstants.AccStatic) != 0) {
				annotationNode.addError("Existing Builder must be a non-static inner class.");
				return;
			}
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderType, annotationNode);
			/* generate errors for @Singular BFDs that have one already defined node. */ {
				for (BuilderFieldData bfd : builderFields) {
					SingularData sd = bfd.singularData;
					if (sd == null) continue;
					EclipseSingularizer singularizer = sd.getSingularizer();
					if (singularizer == null) continue;
					if (singularizer.checkForAlreadyExistingNodesAndGenerateError(builderType, sd)) {
						bfd.singularData = null;
					}
				}
			}
		}
		
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
		
		generateBuilderFields(builderType, builderFields, ast);
		if (addCleaning) {
			FieldDeclaration cleanDecl = new FieldDeclaration(CLEAN_FIELD_NAME, 0, -1);
			cleanDecl.declarationSourceEnd = -1;
			cleanDecl.modifiers = ClassFileConstants.AccPrivate;
			cleanDecl.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
			cleanDecl.traverse(new SetGeneratedByVisitor(ast), (MethodScope) null);
			injectFieldAndMarkGenerated(builderType, cleanDecl);
		}
		
		if (constructorExists(builderType) == MemberExistsResult.NOT_EXISTS) {
			ConstructorDeclaration cd = HandleConstructor.createConstructor(
				AccessLevel.PACKAGE, builderType, Collections.<EclipseNode>emptyList(), false,
				annotationNode, Collections.<Annotation>emptyList());
			if (cd != null) injectMethod(builderType, cd);
		}
		
		for (BuilderFieldData bfd : builderFields) {
			makePrefixedSetterMethodsForBuilder(cfv, builderType, bfd, annotationNode, fluent, chain, accessForInners, bfd.originalFieldNode, builderInstance.setterPrefix());
		}
		
		{
			MemberExistsResult methodExists = methodExists(buildMethodName, builderType, -1);
			if (methodExists == MemberExistsResult.EXISTS_BY_LOMBOK) methodExists = methodExists(buildMethodName, builderType, 0);
			if (methodExists == MemberExistsResult.NOT_EXISTS) {
				MethodDeclaration md = generateBuildMethod(cfv, tdParent, isStatic, buildMethodName, nameOfStaticBuilderMethod, returnType, builderFields, builderType, thrownExceptions, addCleaning, ast, accessForInners);
				if (md != null) injectMethod(builderType, md);
			}
		}
		
		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			List<Included<EclipseNode, ToString.Include>> fieldNodes = new ArrayList<Included<EclipseNode, ToString.Include>>();
			for (BuilderFieldData bfd : builderFields) {
				for (EclipseNode f : bfd.createdFields) {
					fieldNodes.add(new Included<EclipseNode, ToString.Include>(f, null, true));
				}
			}
			MethodDeclaration md = HandleToString.createToString(builderType, fieldNodes, true, false, ast, FieldAccess.ALWAYS_FIELD);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (addCleaning) {
			MethodDeclaration cleanMethod = generateCleanMethod(builderFields, builderType, ast);
			if (cleanMethod != null) injectMethod(builderType, cleanMethod);
		}
		
		if (generateBuilderMethod && methodExists(builderMethodName, tdParent, -1) != MemberExistsResult.NOT_EXISTS) generateBuilderMethod = false;
		if (generateBuilderMethod) {
			MethodDeclaration md = generateBuilderMethod(cfv, isStatic, builderMethodName, builderClassName, tdParent, typeParams, ast, accessForOuters);
			if (md != null) injectMethod(tdParent, md);
		}
		
		if (toBuilder) switch (methodExists(toBuilderMethodName, tdParent, 0)) {
		case EXISTS_BY_USER:
			annotationNode.addWarning("Not generating toBuilder() as it already exists.");
			break;
		case NOT_EXISTS:
			TypeParameter[] tps = typeParams;
			if (typeArgsForToBuilder != null) {
				tps = new TypeParameter[typeArgsForToBuilder.size()];
				for (int i = 0; i < tps.length; i++) {
					tps[i] = new TypeParameter();
					tps[i].name = typeArgsForToBuilder.get(i);
				}
			}
			MethodDeclaration md = generateToBuilderMethod(cfv, isStatic, toBuilderMethodName, builderClassName, tdParent, tps, builderFields, fluent, ast, accessForOuters, builderInstance.setterPrefix());
			
			if (md != null) injectMethod(tdParent, md);
		}
		
		if (nonFinalNonDefaultedFields != null && generateBuilderMethod) {
			for (EclipseNode fieldNode : nonFinalNonDefaultedFields) {
				fieldNode.addWarning("@Builder will ignore the initializing expression entirely. If you want the initializing expression to serve as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.");
			}
		}
	}
	
	private static final char[] BUILDER_TEMP_VAR = {'b', 'u', 'i', 'l', 'd', 'e', 'r'};
	private MethodDeclaration generateToBuilderMethod(CheckerFrameworkVersion cfv, boolean isStatic, String methodName, String builderClassName, EclipseNode type, TypeParameter[] typeParams, List<BuilderFieldData> builderFields, boolean fluent, ASTNode source, AccessLevel access, String prefix) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		out.selector = methodName.toCharArray();
		out.modifiers = toEclipseModifier(access);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = namePlusTypeParamsToTypeReference(type, builderClassName.toCharArray(), !isStatic, typeParams, p);
		AllocationExpression invoke = new AllocationExpression();
		invoke.type = namePlusTypeParamsToTypeReference(type, builderClassName.toCharArray(), !isStatic, typeParams, p);
		
		Expression receiver = invoke;
		List<Statement> preStatements = null;
		List<Statement> postStatements = null;
		
		for (BuilderFieldData bfd : builderFields) {
			String setterName = new String(bfd.name);
			String setterPrefix = !prefix.isEmpty() ? prefix : fluent ? "" : "set";
			if (!setterPrefix.isEmpty()) setterName = HandlerUtil.buildAccessorName(setterPrefix, setterName);
			
			MessageSend ms = new MessageSend();
			Expression[] tgt = new Expression[bfd.singularData == null ? 1 : 2];
			
			if (bfd.obtainVia == null || !bfd.obtainVia.field().isEmpty()) {
				char[] fieldName = bfd.obtainVia == null ? bfd.rawName : bfd.obtainVia.field().toCharArray();
				for (int i = 0; i < tgt.length; i++) {
					FieldReference fr = new FieldReference(fieldName, 0);
					fr.receiver = new ThisReference(0, 0);
					tgt[i] = fr;
				}
			} else {
				String obtainName = bfd.obtainVia.method();
				boolean obtainIsStatic = bfd.obtainVia.isStatic();
				MessageSend obtainExpr = new MessageSend();
				if (obtainIsStatic) {
					if (typeParams != null && typeParams.length > 0) {
						obtainExpr.typeArguments = new TypeReference[typeParams.length];
						for (int j = 0; j<typeParams.length; j++) {
							obtainExpr.typeArguments[j] = new SingleTypeReference(typeParams[j].name, 0);
						}
					}
					obtainExpr.receiver = generateNameReference(type, 0);
				} else {
					obtainExpr.receiver = new ThisReference(0, 0);
				}
				obtainExpr.selector = obtainName.toCharArray();
				if (obtainIsStatic) obtainExpr.arguments = new Expression[] {new ThisReference(0, 0)};
				for (int i = 0; i < tgt.length; i++) tgt[i] = new SingleNameReference(bfd.name, 0L);
				
				// javac appears to cache the type of JCMethodInvocation expressions based on position, meaning, if you have 2 ObtainVia-based method invokes on different types, you get bizarre type mismatch errors.
				// going via a local variable declaration solves the problem. We copy this behaviour
				// for ecj so we match what javac's handler does.
				LocalDeclaration ld = new LocalDeclaration(bfd.name, 0, 0);
				ld.modifiers = ClassFileConstants.AccFinal;
				ld.type = EclipseHandlerUtil.copyType(bfd.type, source);
				ld.initialization = obtainExpr;
				if (preStatements == null) preStatements = new ArrayList<Statement>();
				preStatements.add(ld);
			}
			
			ms.selector = setterName.toCharArray();
			if (bfd.singularData == null) {
				ms.arguments = tgt;
				ms.receiver = receiver;
				receiver = ms;
			} else {
				ms.arguments = new Expression[] {tgt[1]};
				ms.receiver = new SingleNameReference(BUILDER_TEMP_VAR, p);
				EqualExpression isNotNull = new EqualExpression(tgt[0], new NullLiteral(pS, pE), OperatorIds.NOT_EQUAL);
				if (postStatements == null) postStatements = new ArrayList<Statement>();
				postStatements.add(new IfStatement(isNotNull, ms, pS, pE));
			}
		}
		
		int preSs = preStatements == null ? 0 : preStatements.size();
		int postSs = postStatements == null ? 0 : postStatements.size();
		if (postSs > 0) {
			out.statements = new Statement[preSs + postSs + 2];
			for (int i = 0; i < preSs; i++) out.statements[i] = preStatements.get(i);
			for (int i = 0; i < postSs; i++) out.statements[preSs + 1 + i] = postStatements.get(i);
			LocalDeclaration b = new LocalDeclaration(BUILDER_TEMP_VAR, pS, pE);
			out.statements[preSs] = b;
			b.modifiers |= ClassFileConstants.AccFinal;
			b.type = namePlusTypeParamsToTypeReference(type, builderClassName.toCharArray(), !isStatic, typeParams, p);
			b.type.sourceStart = pS; b.type.sourceEnd = pE;
			b.initialization = receiver;
			out.statements[preSs + postSs + 1] = new ReturnStatement(new SingleNameReference(BUILDER_TEMP_VAR, p), pS, pE);
		} else {
			out.statements = new Statement[preSs + 1];
			for (int i = 0; i < preSs; i++) out.statements[i] = preStatements.get(i);
			out.statements[preSs] = new ReturnStatement(receiver, pS, pE);
		}
		
		if (cfv.generateUnique()) {
			out.annotations = new Annotation[] {generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__UNIQUE)};
		}
		
		createRelevantNonNullAnnotation(type, out);
		out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) type.get()).scope);
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
	
	static Argument[] generateBuildArgs(CheckerFrameworkVersion cfv, EclipseNode type, List<BuilderFieldData> builderFields, ASTNode source) {
		if (!cfv.generateCalledMethods()) return null;
		
		List<char[]> mandatories = new ArrayList<char[]>();
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData == null && bfd.nameOfSetFlag == null) mandatories.add(bfd.name);
		}
		
		if (mandatories.size() == 0) return null;
		char[][] nameCalled = fromQualifiedName(CheckerFrameworkVersion.NAME__CALLED);
		SingleMemberAnnotation ann = new SingleMemberAnnotation(new QualifiedTypeReference(nameCalled, poss(source, nameCalled.length)), source.sourceStart);
		if (mandatories.size() == 1) {
			ann.memberValue = new StringLiteral(mandatories.get(0), 0, 0, 0);
		} else {
			ArrayInitializer arr = new ArrayInitializer();
			arr.sourceStart = source.sourceStart;
			arr.sourceEnd = source.sourceEnd;
			arr.expressions = new Expression[mandatories.size()];
			for (int i = 0; i < arr.expressions.length; i++) {
				arr.expressions[i] = new StringLiteral(mandatories.get(i), source.sourceStart, source.sourceEnd, 0);
			}
			ann.memberValue = arr;
		}
		Argument arg = new Argument(new char[] { 't', 'h', 'i', 's' }, 0, generateTypeReference(type, source.sourceStart), Modifier.FINAL);
		arg.annotations = new Annotation[] {ann};
		return new Argument[] {arg};
	}
	
	public MethodDeclaration generateBuildMethod(CheckerFrameworkVersion cfv, EclipseNode tdParent, boolean isStatic, String name, char[] staticName, TypeReference returnType, List<BuilderFieldData> builderFields, EclipseNode type, TypeReference[] thrownExceptions, boolean addCleaning, ASTNode source, AccessLevel access) {
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		List<Statement> statements = new ArrayList<Statement>();
		
		if (addCleaning) {
			FieldReference thisUnclean = new FieldReference(CLEAN_FIELD_NAME, 0);
			thisUnclean.receiver = new ThisReference(0, 0);
			Expression notClean = new UnaryExpression(thisUnclean, OperatorIds.NOT);
			MessageSend invokeClean = new MessageSend();
			invokeClean.selector = CLEAN_METHOD_NAME;
			statements.add(new IfStatement(notClean, invokeClean, 0, 0));
		}
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, type, statements, bfd.builderFieldName, "this");
			}
		}
		
		List<Expression> args = new ArrayList<Expression>();
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.nameOfSetFlag != null) {
				LocalDeclaration ld = new LocalDeclaration(bfd.builderFieldName, 0, 0);
				ld.type = copyType(bfd.type);
				FieldReference builderAssign = new FieldReference(bfd.builderFieldName, 0);
				builderAssign.receiver = new ThisReference(0, 0);
				ld.initialization = builderAssign;
				statements.add(ld);
				
				MessageSend inv = new MessageSend();
				inv.sourceStart = source.sourceStart;
				inv.sourceEnd = source.sourceEnd;
				inv.receiver = new SingleNameReference(((TypeDeclaration) tdParent.get()).name, 0L);
				inv.selector = bfd.nameOfDefaultProvider;
				inv.typeArguments = typeParameterNames(((TypeDeclaration) type.get()).typeParameters);
				
				Assignment defaultAssign = new Assignment(new SingleNameReference(bfd.builderFieldName, 0L), inv, 0);
				FieldReference thisSet = new FieldReference(bfd.nameOfSetFlag, 0L);
				thisSet.receiver = new ThisReference(0, 0);
				Expression thisNotSet = new UnaryExpression(thisSet, OperatorIds.NOT);
				statements.add(new IfStatement(thisNotSet, defaultAssign, 0, 0));
			}
			
			if (bfd.nameOfSetFlag != null || (bfd.singularData != null && bfd.singularData.getSingularizer().shadowedDuringBuild())) {
				args.add(new SingleNameReference(bfd.builderFieldName, 0L));
			} else {
				FieldReference fr = new FieldReference(bfd.builderFieldName, 0L);
				fr.receiver = new ThisReference(0, 0);
				args.add(fr);
			}
		}
		
		if (addCleaning) {
			FieldReference thisUnclean = new FieldReference(CLEAN_FIELD_NAME, 0);
			thisUnclean.receiver = new ThisReference(0, 0);
			statements.add(new Assignment(thisUnclean, new TrueLiteral(0, 0), 0));
		}
		
		out.modifiers = toEclipseModifier(access);
		out.selector = name.toCharArray();
		out.thrownExceptions = copyTypes(thrownExceptions);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = returnType;
		
		if (staticName == null) {
			AllocationExpression allocationStatement = new AllocationExpression();
			allocationStatement.type = copyType(out.returnType);
			allocationStatement.arguments = args.isEmpty() ? null : args.toArray(new Expression[0]);
			statements.add(new ReturnStatement(allocationStatement, 0, 0));
		} else {
			MessageSend invoke = new MessageSend();
			invoke.selector = staticName;
			if (isStatic) {
				invoke.receiver = new SingleNameReference(type.up().getName().toCharArray(), 0);
			} else {
				invoke.receiver = new QualifiedThisReference(generateTypeReference(type.up(), 0) , 0, 0);
			}
			
			invoke.typeArguments = typeParameterNames(((TypeDeclaration) type.get()).typeParameters);
			invoke.arguments = args.isEmpty() ? null : args.toArray(new Expression[0]);
			if (returnType instanceof SingleTypeReference && Arrays.equals(TypeConstants.VOID, ((SingleTypeReference) returnType).token)) {
				statements.add(invoke);
			} else {
				statements.add(new ReturnStatement(invoke, 0, 0));
			}
		}
		out.statements = statements.isEmpty() ? null : statements.toArray(new Statement[0]);
		if (cfv.generateSideEffectFree()) {
			out.annotations = new Annotation[] {generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE)};
		}
		out.arguments = generateBuildArgs(cfv, type, builderFields, source);
		if (staticName == null) createRelevantNonNullAnnotation(type, out);
		out.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return out;
	}
	
	private TypeReference[] typeParameterNames(TypeParameter[] typeParameters) {
		if (typeParameters == null) return null;
		
		TypeReference[] trs = new TypeReference[typeParameters.length];
		for (int i = 0; i < trs.length; i++) {
			trs[i] = new SingleTypeReference(typeParameters[i].name, 0);
		}
		return trs;
	}
	
	public static MethodDeclaration generateDefaultProvider(char[] methodName, TypeParameter[] typeParameters, EclipseNode fieldNode, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) fieldNode.top().get()).compilationResult);
		out.typeParameters = copyTypeParams(typeParameters, source);
		out.selector = methodName;
		out.modifiers = ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
		out.returnType = copyType(fd.type, source);
		out.statements = new Statement[] {new ReturnStatement(fd.initialization, pS, pE)};
		fd.initialization = null;
		
		out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) fieldNode.up().get()).scope);
		return out;
	}
	
	public MethodDeclaration generateBuilderMethod(CheckerFrameworkVersion cfv, boolean isStatic, String builderMethodName, String builderClassName, EclipseNode type, TypeParameter[] typeParams, ASTNode source, AccessLevel access) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		char[] builderClassName_ = builderClassName.toCharArray();
		
		MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		out.selector = builderMethodName.toCharArray();
		out.modifiers = toEclipseModifier(access);
		if (isStatic) out.modifiers |= ClassFileConstants.AccStatic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = namePlusTypeParamsToTypeReference(type, builderClassName_, !isStatic, typeParams, p);
		out.typeParameters = copyTypeParams(typeParams, source);
		AllocationExpression invoke = new AllocationExpression();
		if (isStatic) {
			invoke.type = namePlusTypeParamsToTypeReference(type, builderClassName_, false, typeParams, p);
			out.statements = new Statement[] {new ReturnStatement(invoke, pS, pE)};
		} else {
			// return this.new Builder();
			QualifiedAllocationExpression qualifiedInvoke = new QualifiedAllocationExpression();
			qualifiedInvoke.enclosingInstance = new ThisReference(pS, pE);
			if (typeParams == null || typeParams.length == 0) {
				qualifiedInvoke.type = new SingleTypeReference(builderClassName_, p);
			} else {
				qualifiedInvoke.type = namePlusTypeParamsToTypeReference(null, builderClassName_, false, typeParams, p);
			}
			
			out.statements = new Statement[] {new ReturnStatement(qualifiedInvoke, pS, pE)};
		}
		Annotation uniqueAnn = cfv.generateUnique() ? generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__UNIQUE) : null;
		Annotation sefAnn = cfv.generateSideEffectFree() ? generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE) : null;
		if (uniqueAnn != null && sefAnn != null) {
			out.annotations = new Annotation[] {uniqueAnn, sefAnn};
		} else if (uniqueAnn != null) {
			out.annotations = new Annotation[] {uniqueAnn};
		} else if (sefAnn != null) {
			out.annotations = new Annotation[] {sefAnn};
		}
		createRelevantNonNullAnnotation(type, out);
		out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) type.get()).scope);
		return out;
	}
	
	public void generateBuilderFields(EclipseNode builderType, List<BuilderFieldData> builderFields, ASTNode source) {
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
					if (Arrays.equals(n, bfd.builderFieldName)) field = exists;
					if (bfd.nameOfSetFlag != null && Arrays.equals(n, bfd.nameOfSetFlag)) setFlag = exists;
				}
				
				if (field == null) {
					FieldDeclaration fd = new FieldDeclaration(bfd.builderFieldName, 0, 0);
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
	
	private static final AbstractMethodDeclaration[] EMPTY = {};
	
	public void makePrefixedSetterMethodsForBuilder(CheckerFrameworkVersion cfv, EclipseNode builderType, BuilderFieldData bfd, EclipseNode sourceNode, boolean fluent, boolean chain, AccessLevel access, EclipseNode originalFieldNode, String prefix) {
		boolean deprecate = isFieldDeprecated(bfd.originalFieldNode);
		if (bfd.singularData == null || bfd.singularData.getSingularizer() == null) {
			makePrefixedSetterMethodForBuilder(cfv, builderType, deprecate, bfd.createdFields.get(0), bfd.name, bfd.nameOfSetFlag, sourceNode, fluent, chain, bfd.annotations, access, originalFieldNode, prefix);
		} else {
			bfd.singularData.getSingularizer().generateMethods(cfv, bfd.singularData, deprecate, builderType, fluent, chain, access);
		}
	}
	
	private void makePrefixedSetterMethodForBuilder(CheckerFrameworkVersion cfv, EclipseNode builderType, boolean deprecate, EclipseNode fieldNode, char[] paramName, char[] nameOfSetFlag, EclipseNode sourceNode, boolean fluent, boolean chain, Annotation[] annotations, AccessLevel access, EclipseNode originalFieldNode, String prefix) {
		TypeDeclaration td = (TypeDeclaration) builderType.get();
		AbstractMethodDeclaration[] existing = td.methods;
		if (existing == null) existing = EMPTY;
		int len = existing.length;
		
		String setterPrefix = prefix.isEmpty() ? "set" : prefix;
		String setterName;
		if(fluent) {
			setterName = prefix.isEmpty() ? new String(paramName) : HandlerUtil.buildAccessorName(setterPrefix, new String(paramName));
		} else {
			setterName = HandlerUtil.buildAccessorName(setterPrefix, new String(paramName));
		}
		
		for (int i = 0; i < len; i++) {
			if (!(existing[i] instanceof MethodDeclaration)) continue;
			char[] existingName = existing[i].selector;
			if (Arrays.equals(setterName.toCharArray(), existingName) && !isTolerate(fieldNode, existing[i])) return;
		}
		
		List<Annotation> methodAnnsList = Collections.<Annotation>emptyList();
		Annotation[] methodAnns = EclipseHandlerUtil.findCopyableToSetterAnnotations(originalFieldNode);
		if (methodAnns != null && methodAnns.length > 0) methodAnnsList = Arrays.asList(methodAnns);
		ASTNode source = sourceNode.get();
		MethodDeclaration setter = HandleSetter.createSetter(td, deprecate, fieldNode, setterName, paramName, nameOfSetFlag, chain, toEclipseModifier(access),
			sourceNode, methodAnnsList, annotations != null ? Arrays.asList(copyAnnotations(source, annotations)) : Collections.<Annotation>emptyList());
		if (cfv.generateCalledMethods()) {
			Argument[] arr = setter.arguments == null ? new Argument[0] : setter.arguments;
			Argument[] newArr = new Argument[arr.length + 1];
			System.arraycopy(arr, 0, newArr, 1, arr.length);
			newArr[0] = new Argument(new char[] { 't', 'h', 'i', 's' }, 0, generateTypeReference(builderType, 0), Modifier.FINAL);
			char[][] nameNotCalled = fromQualifiedName(CheckerFrameworkVersion.NAME__NOT_CALLED);
			SingleMemberAnnotation ann = new SingleMemberAnnotation(new QualifiedTypeReference(nameNotCalled, poss(
					source, nameNotCalled.length)), source.sourceStart);
			ann.memberValue = new StringLiteral(setterName.toCharArray(), 0, 0, 0);
			newArr[0].annotations = new Annotation[] {ann};
			setter.arguments = newArr;
		}
		injectMethod(builderType, setter);
	}
	
	public EclipseNode makeBuilderClass(boolean isStatic, EclipseNode tdParent, String builderClassName, TypeParameter[] typeParams, ASTNode source, AccessLevel access) {
		TypeDeclaration parent = (TypeDeclaration) tdParent.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= toEclipseModifier(access);
		if (isStatic) builder.modifiers |= ClassFileConstants.AccStatic;
		builder.typeParameters = copyTypeParams(typeParams, source);
		builder.name = builderClassName.toCharArray();
		builder.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return injectType(tdParent, builder);
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
	 * @param setterPrefix Explicitly requested setter prefix.
	 */
	private SingularData getSingularData(EclipseNode node, ASTNode source, final String setterPrefix) {
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
}
