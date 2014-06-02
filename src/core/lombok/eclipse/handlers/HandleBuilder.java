/*
 * Copyright (C) 2013-2014 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.experimental.Builder;
import lombok.experimental.NonFinal;

@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleBuilder extends EclipseAnnotationHandler<Builder> {
	@Override public void handle(AnnotationValues<Builder> annotation, Annotation ast, EclipseNode annotationNode) {
		long p = (long) ast.sourceStart << 32 | ast.sourceEnd;

		Builder builderInstance = annotation.getInstance();
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		String builderClassName = builderInstance.builderClassName();

		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) builderMethodName = "build";
		if (builderClassName == null) builderClassName = "";

		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		if (!builderClassName.isEmpty()) {
			if (!checkName("builderClassName", builderClassName, annotationNode)) return;
		}

		EclipseNode parent = annotationNode.up();

		List<TypeReference> typesOfParameters = new ArrayList<TypeReference>();
		List<char[]> namesOfParameters = new ArrayList<char[]>();
		TypeReference returnType;
		TypeParameter[] typeParams;
		TypeReference[] thrownExceptions;
		char[] nameOfStaticBuilderMethod;
		EclipseNode tdParent;

		AbstractMethodDeclaration fillParametersFrom = null;

		if (parent.get() instanceof TypeDeclaration) {
			tdParent = parent;
			TypeDeclaration td = (TypeDeclaration) tdParent.get();

			List<EclipseNode> fields = new ArrayList<EclipseNode>();
			@SuppressWarnings("deprecation")
			boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation(lombok.experimental.Value.class, parent));
			for (EclipseNode fieldNode : HandleConstructor.findAllFields(tdParent)) {
				FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
				// final fields with an initializer cannot be written to, so they can't be 'builderized'. Unfortunately presence of @Value makes
				// non-final fields final, but @Value's handler hasn't done this yet, so we have to do this math ourselves.
				// Value will only skip making a field final if it has an explicit @NonFinal annotation, so we check for that.
				if (fd.initialization != null && valuePresent && !hasAnnotation(NonFinal.class, fieldNode)) continue;
				namesOfParameters.add(removePrefixFromField(fieldNode));
				typesOfParameters.add(fd.type);
				fields.add(fieldNode);
			}

			new HandleConstructor().generateConstructor(tdParent, AccessLevel.PACKAGE, fields, null, SkipIfConstructorExists.I_AM_BUILDER, true, Collections.<Annotation>emptyList(), ast);

			returnType = namePlusTypeParamsToTypeReference(td.name, td.typeParameters, p);
			typeParams = td.typeParameters;
			thrownExceptions = null;
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = new String(td.name) + "Builder";
		} else if (parent.get() instanceof ConstructorDeclaration) {
			ConstructorDeclaration cd = (ConstructorDeclaration) parent.get();
			if (cd.typeParameters != null && cd.typeParameters.length > 0) {
				annotationNode.addError("@Builder is not supported on constructors with constructor type parameters.");
				return;
			}

			tdParent = parent.up();
			TypeDeclaration td = (TypeDeclaration) tdParent.get();
			fillParametersFrom = cd;
			returnType = namePlusTypeParamsToTypeReference(td.name, td.typeParameters, p);
			typeParams = td.typeParameters;
			thrownExceptions = cd.thrownExceptions;
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = new String(cd.selector) + "Builder";
		} else if (parent.get() instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration) parent.get();
			tdParent = parent.up();
			if (!md.isStatic()) {
				annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
				return;
			}
			fillParametersFrom = md;
			returnType = copyType(md.returnType, ast);
			typeParams = md.typeParameters;
			thrownExceptions = md.thrownExceptions;
			nameOfStaticBuilderMethod = md.selector;
			if (builderClassName.isEmpty()) {
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

				builderClassName = new String(token) + "Builder";
			}
		} else {
			annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
			return;
		}

		if (fillParametersFrom != null) {
			if (fillParametersFrom.arguments != null) for (Argument a : fillParametersFrom.arguments) {
				namesOfParameters.add(a.name);
				typesOfParameters.add(a.type);
			}
		}

		EclipseNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = makeBuilderClass(tdParent, builderClassName, typeParams, ast);
		} else {
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderType, annotationNode);
		}
		List<EclipseNode> fieldNodes = addFieldsToBuilder(builderType, namesOfParameters, typesOfParameters, ast);
		List<AbstractMethodDeclaration> newMethods = new ArrayList<AbstractMethodDeclaration>();
		for (EclipseNode fieldNode : fieldNodes) {
			MethodDeclaration newMethod = makeSetterMethodForBuilder(builderType, fieldNode, ast, builderInstance.fluent(), builderInstance.chain());
			if (newMethod != null) newMethods.add(newMethod);
		}

		if (constructorExists(builderType) == MemberExistsResult.NOT_EXISTS) {
			ConstructorDeclaration cd = HandleConstructor.createConstructor(AccessLevel.PACKAGE, builderType, Collections.<EclipseNode>emptyList(), true, ast, Collections.<Annotation>emptyList());
			if (cd != null) injectMethod(builderType, cd);
		}

		for (AbstractMethodDeclaration newMethod : newMethods) injectMethod(builderType, newMethod);
		if (methodExists(buildMethodName, builderType, -1) == MemberExistsResult.NOT_EXISTS) {
			MethodDeclaration md = generateBuildMethod(buildMethodName, nameOfStaticBuilderMethod, returnType, namesOfParameters, builderType, ast, thrownExceptions);
			if (md != null) injectMethod(builderType, md);
		}

		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			MethodDeclaration md = HandleToString.createToString(builderType, fieldNodes, true, false, ast, FieldAccess.ALWAYS_FIELD);
			if (md != null) injectMethod(builderType, md);
		}

		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			MethodDeclaration md = generateBuilderMethod(builderMethodName, builderClassName, tdParent, typeParams, ast);
			if (md != null) injectMethod(tdParent, md);
		}
	}

	public MethodDeclaration generateBuilderMethod(String builderMethodName, String builderClassName, EclipseNode type, TypeParameter[] typeParams, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;

		MethodDeclaration out = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		out.selector = builderMethodName.toCharArray();
		out.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = namePlusTypeParamsToTypeReference(builderClassName.toCharArray(), typeParams, p);
		out.typeParameters = copyTypeParams(typeParams, source);
		AllocationExpression invoke = new AllocationExpression();
		invoke.type = namePlusTypeParamsToTypeReference(builderClassName.toCharArray(), typeParams, p);
		out.statements = new Statement[] {new ReturnStatement(invoke, pS, pE)};

		out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) type.get()).scope);
		return out;
	}

	public MethodDeclaration generateBuildMethod(String name, char[] staticName, TypeReference returnType, List<char[]> fieldNames, EclipseNode type, ASTNode source, TypeReference[] thrownExceptions) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;

		MethodDeclaration out = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);

		out.modifiers = ClassFileConstants.AccPublic;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		out.selector = name.toCharArray();
		out.thrownExceptions = copyTypes(thrownExceptions, source);
		out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		out.returnType = returnType;

		List<Expression> assigns = new ArrayList<Expression>();
		for (char[] fieldName : fieldNames) {
			SingleNameReference nameRef = new SingleNameReference(fieldName, p);
			assigns.add(nameRef);
		}

		Statement statement;

		if (staticName == null) {
			AllocationExpression allocationStatement = new AllocationExpression();
			allocationStatement.type = copyType(out.returnType, source);
			allocationStatement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
			statement = new ReturnStatement(allocationStatement, (int)(p >> 32), (int)p);
		} else {
			MessageSend invoke = new MessageSend();
			invoke.selector = staticName;
			invoke.receiver = new SingleNameReference(type.up().getName().toCharArray(), p);
			TypeParameter[] tps = ((TypeDeclaration) type.get()).typeParameters;
			if (tps != null) {
				TypeReference[] trs = new TypeReference[tps.length];
				for (int i = 0; i < trs.length; i++) {
					trs[i] = new SingleTypeReference(tps[i].name, p);
				}
				invoke.typeArguments = trs;
			}
			invoke.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
			if (returnType instanceof SingleTypeReference && Arrays.equals(TypeBinding.VOID.simpleName, ((SingleTypeReference) returnType).token)) {
				statement = invoke;
			} else {
				statement = new ReturnStatement(invoke, (int)(p >> 32), (int)p);
			}
		}

		out.statements = new Statement[] { statement };

		out.traverse(new SetGeneratedByVisitor(source), typeDecl.scope);
		return out;
	}

	public List<EclipseNode> addFieldsToBuilder(EclipseNode builderType, List<char[]> namesOfParameters, List<TypeReference> typesOfParameters, ASTNode source) {
		int len = namesOfParameters.size();
		TypeDeclaration td = (TypeDeclaration) builderType.get();
		FieldDeclaration[] existing = td.fields;
		if (existing == null) existing = new FieldDeclaration[0];

		List<EclipseNode> out = new ArrayList<EclipseNode>();

		top:
		for (int i = len - 1; i >= 0; i--) {
			char[] name = namesOfParameters.get(i);
			for (FieldDeclaration exists : existing) {
				if (Arrays.equals(exists.name, name)) {
					out.add(builderType.getNodeFor(exists));
					continue top;
				}
			}
			TypeReference fieldReference = copyType(typesOfParameters.get(i), source);
			FieldDeclaration newField = new FieldDeclaration(name, 0, 0);
			newField.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
			newField.modifiers = ClassFileConstants.AccPrivate;
			newField.type = fieldReference;
			out.add(injectField(builderType, newField));
		}

		Collections.reverse(out);

		return out;
	}

	private static final AbstractMethodDeclaration[] EMPTY = {};

	public MethodDeclaration makeSetterMethodForBuilder(EclipseNode builderType, EclipseNode fieldNode, ASTNode source, boolean fluent, boolean chain) {
		TypeDeclaration td = (TypeDeclaration) builderType.get();
		AbstractMethodDeclaration[] existing = td.methods;
		if (existing == null) existing = EMPTY;
		int len = existing.length;
		FieldDeclaration fd = (FieldDeclaration) fieldNode.get();
		char[] name = fd.name;

		for (int i = 0; i < len; i++) {
			if (!(existing[i] instanceof MethodDeclaration)) continue;
			char[] existingName = existing[i].selector;
			if (Arrays.equals(name, existingName)) return null;
		}

		boolean isBoolean = isBoolean(fd.type);
		String setterName = fluent ? fieldNode.getName() : TransformationsUtil.toSetterName(null, fieldNode.getName(), isBoolean);

		return HandleSetter.createSetter(td, fieldNode, setterName, chain, ClassFileConstants.AccPublic,
				source, Collections.<Annotation>emptyList(), Collections.<Annotation>emptyList(),false,false,null);
	}

	public EclipseNode findInnerClass(EclipseNode parent, String name) {
		char[] c = name.toCharArray();
		for (EclipseNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			TypeDeclaration td = (TypeDeclaration) child.get();
			if (Arrays.equals(td.name, c)) return child;
		}
		return null;
	}

	public EclipseNode makeBuilderClass(EclipseNode tdParent, String builderClassName, TypeParameter[] typeParams, ASTNode source) {
		TypeDeclaration parent = (TypeDeclaration) tdParent.get();
		TypeDeclaration builder = new TypeDeclaration(parent.compilationResult);
		builder.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		builder.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		builder.typeParameters = copyTypeParams(typeParams, source);
		builder.name = builderClassName.toCharArray();
		builder.traverse(new SetGeneratedByVisitor(source), (ClassScope) null);
		return injectType(tdParent, builder);
	}
}
