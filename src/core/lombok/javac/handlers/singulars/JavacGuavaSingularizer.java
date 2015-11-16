/*
 * Copyright (C) 2015 The Project Lombok Authors.
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
package lombok.javac.handlers.singulars;

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collections;

import lombok.core.GuavaTypeMap;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

abstract class JavacGuavaSingularizer extends JavacSingularizer {
	protected String getSimpleTargetTypeName(SingularData data) {
		return GuavaTypeMap.getGuavaTypeName(data.getTargetFqn());
	}

	protected String getBuilderMethodName(SingularData data) {
		String simpleTypeName = getSimpleTargetTypeName(data);
		if ("ImmutableSortedSet".equals(simpleTypeName) || "ImmutableSortedMap".equals(simpleTypeName)) return "naturalOrder";
		return "builder";
	}

	protected abstract boolean isMap();

	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		String simpleTypeName = getSimpleTargetTypeName(data);
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "com", "google", "common", "collect", simpleTypeName, "Builder");
		type = addTypeArgs(getTypeArgumentsCount(isMap(), simpleTypeName), false, builderType, type, data.getTypeArgs(), source);

		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}

	@Override public void generateMethods(SingularData data, JavacNode builderType, JCTree source, boolean fluent, boolean chain) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		JCStatement returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generateSingularMethod(maker, returnType, returnStatement, data, builderType, source, fluent);

		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generatePluralMethod(maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generateClearMethod(maker, returnType, returnStatement, data, builderType, source);
	}
	
	private void generateClearMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source) {
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		List<JCVariableDecl> params = List.nil();
		
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCStatement clearField = maker.Exec(maker.Assign(thisDotField, maker.Literal(CTC_BOT, null)));
		List<JCStatement> statements = returnStatement != null ? List.of(clearField, returnStatement) : List.of(clearField);
		
		JCBlock body = maker.Block(0, statements);
		Name methodName = builderType.toName(HandlerUtil.buildAccessorName("clear", data.getPluralName().toString()));
		JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, params, thrown, body, null);
		injectMethod(builderType, method);
	}

	void generateSingularMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		boolean mapMode = isMap();

		Name keyName = !mapMode ? data.getSingularName() : builderType.toName(data.getSingularName() + "$key");
		Name valueName = !mapMode ? null : builderType.toName(data.getSingularName() + "$value");

		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, mapMode, source));
		JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), shouldUsePut(data, mapMode) ? "put" : "add");
		List<JCExpression> invokeAddExpr;
		if (mapMode) {
			invokeAddExpr = List.<JCExpression>of(maker.Ident(keyName), maker.Ident(valueName));
		} else {
			invokeAddExpr = List.<JCExpression>of(maker.Ident(keyName));
		}
		JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, invokeAddExpr);
		statements.append(maker.Exec(invokeAdd));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name methodName = data.getSingularName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) methodName = builderType.toName(HandlerUtil.buildAccessorName(shouldUsePut(data, mapMode) ? "put" : "add", methodName.toString()));
		List<JCVariableDecl> params;
		if (mapMode) {
			JCExpression keyType = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
			JCExpression valueType = cloneParamType(1, maker, data.getTypeArgs(), builderType, source);
			JCVariableDecl paramKey = maker.VarDef(maker.Modifiers(paramFlags), keyName, keyType, null);
			JCVariableDecl paramValue = maker.VarDef(maker.Modifiers(paramFlags), valueName, valueType, null);
			params = List.of(paramKey, paramValue);
		} else {
			final JCExpression paramType;

			if (isSpecialTypeOfListSet(data)) {
				JCExpression cellType = JavacHandlerUtil.chainDots(builderType, "com", "google", "common", "collect", "Table", "Cell");
				paramType = addTypeArgs(3, false, builderType, cellType, data.getTypeArgs(), source);
			} else {
				paramType = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
			}
			params = List.of(maker.VarDef(maker.Modifiers(paramFlags), data.getSingularName(), paramType, null));
		}
		JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, params, thrown, body, null);
		injectMethod(builderType, method);
	}

	protected void generatePluralMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		boolean mapMode = isMap();

		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, mapMode, source));
		JCExpression thisDotFieldDotAddAll = chainDots(builderType, "this", data.getPluralName().toString(), shouldUsePut(data, mapMode) ? "putAll" : "addAll");
		JCExpression invokeAddAll = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAddAll, List.<JCExpression>of(maker.Ident(data.getPluralName())));
		statements.append(maker.Exec(invokeAddAll));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name methodName = data.getPluralName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) methodName = builderType.toName(HandlerUtil.buildAccessorName(shouldUsePut(data, mapMode) ? "putAll" : "addAll", methodName.toString()));
		JCExpression paramType;
		if (mapMode) {
			paramType = chainDots(builderType, "java", "util", "Map");
		} else {
			if (isSpecialTypeOfListSet(data)) {
				paramType = chainDots(builderType, "com", "google", "common", "collect", "Table");
			} else {
				paramType = genJavaLangTypeRef(builderType, "Iterable");
			}
		}
		String simpleTypeName = getSimpleTargetTypeName(data);
		paramType = addTypeArgs(getTypeArgumentsCount(mapMode, simpleTypeName), true, builderType, paramType, data.getTypeArgs(), source);
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getPluralName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, List.of(param), thrown, body, null);
		injectMethod(builderType, method);
	}

	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		List<JCExpression> jceBlank = List.nil();
		boolean mapMode = isMap();

		String simpleTypeName = getSimpleTargetTypeName(data);
		JCExpression varType = chainDotsString(builderType, data.getTargetFqn());
		int agrumentsCount = getTypeArgumentsCount(mapMode, simpleTypeName);
		varType = addTypeArgs(agrumentsCount, false, builderType, varType, data.getTypeArgs(), source);

		JCExpression empty; {
			//ImmutableX.of()
			JCExpression emptyMethod = chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), "of");
			List<JCExpression> invokeTypeArgs = createTypeArgs(agrumentsCount, false, builderType, data.getTypeArgs(), source);
			empty = maker.Apply(invokeTypeArgs, emptyMethod, jceBlank);
		}

		JCExpression invokeBuild; {
			//this.pluralName.build();
			invokeBuild = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName().toString(), "build"), jceBlank);
		}

		JCExpression isNull; {
			//this.pluralName == null
			isNull = maker.Binary(CTC_EQUAL, maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName()), maker.Literal(CTC_BOT, null));
		}

		JCExpression init = maker.Conditional(isNull, empty, invokeBuild); // this.pluralName == null ? ImmutableX.of() : this.pluralName.build()

		JCStatement jcs = maker.VarDef(maker.Modifiers(0), data.getPluralName(), varType, init);
		statements.append(jcs);
	}

	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, boolean mapMode, JCTree source) {
		List<JCExpression> jceBlank = List.nil();

		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression thisDotField2 = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression cond = maker.Binary(CTC_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));

		JCExpression create = maker.Apply(jceBlank, chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), getBuilderMethodName(data)), jceBlank);
		JCStatement thenPart = maker.Exec(maker.Assign(thisDotField2, create));

		return maker.If(cond, thenPart, null);
	}

	private int getTypeArgumentsCount(boolean isMap, String simpleTypeName) {
		return isMap ? 2 : getListSetTypeArgumentsCount(simpleTypeName);
	}

	private int getListSetTypeArgumentsCount(String simpleTypeName) {
		return "ImmutableTable".equals(simpleTypeName) ? 3 : 1;
	}

	private boolean shouldUsePut(SingularData data, boolean mapMode) {
		return mapMode || isSpecialTypeOfListSet(data);
	}

	private boolean isSpecialTypeOfListSet(SingularData data) {
		return "ImmutableTable".equals(getSimpleTargetTypeName(data));
	}
}
