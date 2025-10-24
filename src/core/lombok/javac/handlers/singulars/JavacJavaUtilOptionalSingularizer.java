/*
 * Copyright (C) 2015-2025 The Project Lombok Authors.
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

import lombok.AccessLevel;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacSingularsRecipes.ExpressionMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.javac.handlers.JavacSingularsRecipes.StatementMaker;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
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

@Provides(JavacSingularizer.class)
public class JavacJavaUtilOptionalSingularizer extends JavacSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Optional");
	}

	@Override public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
		Name valueFieldName = builderType.toName(data.getSingularName() + "$value");
		Name setFieldName = builderType.toName(data.getSingularName() + "$set");
		return java.util.Arrays.asList(valueFieldName, setFieldName);
	}

	@Override public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
		Name singularName = data.getSingularName();
		String singularStr = singularName.toString();
		String clearMethodName = "clear" + Character.toUpperCase(singularStr.charAt(0)) + singularStr.substring(1);
		Name clearName = builderType.toName(clearMethodName);
		return java.util.Arrays.asList(singularName, clearName);
	}

	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JavacNode source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);

		JCVariableDecl valueField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName(data.getSingularName() + "$value"), type, null);
		JavacNode valueFieldNode = injectFieldAndMarkGenerated(builderType, valueField);

		JCExpression boolType = maker.TypeIdent(CTC_BOOLEAN);
		JCVariableDecl setField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName(data.getSingularName() + "$set"), boolType, null);
		JavacNode setFieldNode = injectFieldAndMarkGenerated(builderType, setField);

		return java.util.Arrays.asList(valueFieldNode, setFieldNode);
	}

	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, JavacNode builderType, JavacNode source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression returnType = returnTypeMaker.make();
		JCStatement returnStatement = returnStatementMaker.make();

		generateSingularMethod(cfv, deprecate, maker, returnType, returnStatement, data, builderType, source, fluent, access);
		generateClearMethod(cfv, deprecate, maker, returnType, returnStatement, data, builderType, source, access);
	}

	private void generateSingularMethod(CheckerFrameworkVersion cfv, boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JavacNode source, boolean fluent, AccessLevel access) {
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();

		Name valueFieldName = builderType.toName(data.getSingularName() + "$value");
		Name setFieldName = builderType.toName(data.getSingularName() + "$set");
		Name paramName = data.getSingularName();

		JCExpression thisDotValueField = maker.Select(maker.Ident(builderType.toName("this")), valueFieldName);
		JCExpression thisDotSetField = maker.Select(maker.Ident(builderType.toName("this")), setFieldName);

		statements.append(maker.Exec(maker.Assign(thisDotValueField, maker.Ident(paramName))));
		statements.append(maker.Exec(maker.Assign(thisDotSetField, maker.Literal(CTC_BOOLEAN, 1))));

		if (returnStatement != null) statements.append(returnStatement);

		long flags = addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		JCExpression type = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
		List<JCAnnotation> typeUseAnns = getTypeUseAnnotations(type);
		type = removeTypeUseAnnotations(type);
		JCModifiers paramMods = typeUseAnns.isEmpty() ? maker.Modifiers(flags) : maker.Modifiers(flags, typeUseAnns);
		JCVariableDecl param = maker.VarDef(paramMods, paramName, type, null);

		JCBlock body = maker.Block(0, statements.toList());
		List<JCAnnotation> annsOnMethod = deprecate ? List.of(maker.Annotation(genJavaLangTypeRef(builderType, "Deprecated"), List.<JCExpression>nil())) : List.<JCAnnotation>nil();

		Name methodName = data.getSingularName();
		String setterPrefix = data.getSetterPrefix();
		if (!setterPrefix.isEmpty()) {
			String nameStr = methodName.toString();
			methodName = builderType.toName(setterPrefix + Character.toUpperCase(nameStr.charAt(0)) + nameStr.substring(1));
		}

		JCMethodDecl method = maker.MethodDef(maker.Modifiers(toJavacModifier(access), annsOnMethod), methodName, returnType, List.<JCTypeParameter>nil(), List.of(param), List.<JCExpression>nil(), body, null);
		if (returnStatement != null) createRelevantNonNullAnnotation(builderType, method);
		recursiveSetGeneratedBy(method, source);
		injectMethod(builderType, method);
	}

	private void generateClearMethod(CheckerFrameworkVersion cfv, boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JavacNode source, AccessLevel access) {
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();

		Name setFieldName = builderType.toName(data.getSingularName() + "$set");
		JCExpression thisDotSetField = maker.Select(maker.Ident(builderType.toName("this")), setFieldName);

		statements.append(maker.Exec(maker.Assign(thisDotSetField, maker.Literal(CTC_BOOLEAN, 0))));

		if (returnStatement != null) statements.append(returnStatement);

		String singularStr = data.getSingularName().toString();
		String clearMethodName = "clear" + Character.toUpperCase(singularStr.charAt(0)) + singularStr.substring(1);
		Name methodName = builderType.toName(clearMethodName);

		JCBlock body = maker.Block(0, statements.toList());
		List<JCAnnotation> annsOnMethod = deprecate ? List.of(maker.Annotation(genJavaLangTypeRef(builderType, "Deprecated"), List.<JCExpression>nil())) : List.<JCAnnotation>nil();

		JCMethodDecl method = maker.MethodDef(maker.Modifiers(toJavacModifier(access), annsOnMethod), methodName, returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		if (returnStatement != null) createRelevantNonNullAnnotation(builderType, method);
		recursiveSetGeneratedBy(method, source);
		injectMethod(builderType, method);
	}

	@Override protected JCStatement generateClearStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType) {
		Name setFieldName = builderType.toName(data.getSingularName() + "$set");
		JCExpression thisDotSetField = maker.Select(maker.Ident(builderType.toName("this")), setFieldName);
		return maker.Exec(maker.Assign(thisDotSetField, maker.Literal(CTC_BOOLEAN, 0)));
	}

	@Override protected ListBuffer<JCStatement> generateSingularMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JavacNode source) {
		throw new UnsupportedOperationException("generateSingularMethodStatements should not be called for Optional");
	}

	@Override protected List<JCVariableDecl> generateSingularMethodParameters(JavacTreeMaker maker, SingularData data, JavacNode builderType, JavacNode source) {
		throw new UnsupportedOperationException("generateSingularMethodParameters should not be called for Optional");
	}

	@Override protected JCExpression getPluralMethodParamType(JavacNode builderType) {
		throw new UnsupportedOperationException("getPluralMethodParamType should not be called for Optional");
	}

	@Override protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, JavacNode source) {
		return null;
	}

	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JavacNode source, ListBuffer<JCStatement> statements, Name targetVariableName, String builderVariable) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		List<JCExpression> jceBlank = List.nil();

		Name setFieldName = builderType.toName(data.getSingularName() + "$set");
		Name valueFieldName = builderType.toName(data.getSingularName() + "$value");

		JCExpression optionalType = chainDots(builderType, "java", "util", "Optional");
		optionalType = addTypeArgs(1, false, builderType, optionalType, data.getTypeArgs(), source);

		JCStatement varDefStat = maker.VarDef(maker.Modifiers(0L), data.getPluralName(), optionalType, null);
		statements.append(varDefStat);

		Name builderVarName = builderType.toName(builderVariable);
		JCExpression thisDotSetField = maker.Select(maker.Ident(builderVarName), setFieldName);
		JCExpression thisDotValueField = maker.Select(maker.Ident(builderVarName), valueFieldName);

		JCExpression emptyOptional = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Optional", "empty"), jceBlank);
		JCStatement assignEmpty = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), emptyOptional));

		JCExpression ofNullableCall = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Optional", "ofNullable"), List.of(thisDotValueField));
		JCStatement assignValue = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), ofNullableCall));

		JCStatement ifStat = maker.If(thisDotSetField, assignValue, assignEmpty);
		statements.append(ifStat);
	}

	@Override public boolean shadowedDuringBuild() {
		return true;
	}

	@Override protected String getAddMethodName() {
		return "add";
	}

	@Override protected int getTypeArgumentsCount() {
		return 1;
	}

	@Override protected String getEmptyMaker(String target) {
		return "java.util.Optional.empty";
	}
}
