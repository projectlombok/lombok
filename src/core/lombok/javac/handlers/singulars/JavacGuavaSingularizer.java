/*
 * Copyright (C) 2015-2020 The Project Lombok Authors.
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
import lombok.core.GuavaTypeMap;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.ExpressionMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.javac.handlers.JavacSingularsRecipes.StatementMaker;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

abstract class JavacGuavaSingularizer extends JavacSingularizer {
	protected String getSimpleTargetTypeName(SingularData data) {
		return GuavaTypeMap.getGuavaTypeName(data.getTargetFqn());
	}
	
	@Override protected String getEmptyMaker(String target) {
		return target + ".of";
	}
	
	protected String getBuilderMethodName(SingularData data) {
		String simpleTypeName = getSimpleTargetTypeName(data);
		if ("ImmutableSortedSet".equals(simpleTypeName) || "ImmutableSortedMap".equals(simpleTypeName)) return "naturalOrder";
		return "builder";
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		String simpleTypeName = getSimpleTargetTypeName(data);
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "com", "google", "common", "collect", simpleTypeName, "Builder");
		type = addTypeArgs(getTypeArgumentsCount(), false, builderType, type, data.getTypeArgs(), source);
		
		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}
	
	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		doGenerateMethods(cfv, data, deprecate, builderType, source, fluent, returnTypeMaker, returnStatementMaker, access);
	}
	
	@Override
	protected JCStatement generateClearStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType) {
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		return maker.Exec(maker.Assign(thisDotField, maker.Literal(CTC_BOT, null)));
	}
	
	@Override
	protected List<JCVariableDecl> generateSingularMethodParameters(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		Name[] names = generateSingularMethodParameterNames(data, builderType);
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		for (int i = 0; i < names.length; i++) {
			params.append(generateSingularMethodParameter(i, maker, data, builderType, source, names[i]));
		}
		return params.toList();
	}
	
	@Override
	protected ListBuffer<JCStatement> generateSingularMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		Name[] names = generateSingularMethodParameterNames(data, builderType);
		
		JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), getAddMethodName());
		ListBuffer<JCExpression> invokeAddExprBuilder = new ListBuffer<JCExpression>();
		for (Name name : names) {
			invokeAddExprBuilder.append(maker.Ident(name));
		}
		List<JCExpression> invokeAddExpr = invokeAddExprBuilder.toList();
		JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, invokeAddExpr);
		JCStatement st = maker.Exec(invokeAdd);
		
		return new ListBuffer<JCStatement>().append(st);
	}
	
	private Name[] generateSingularMethodParameterNames(SingularData data, JavacNode builderType) {
		LombokImmutableList<String> suffixes = getArgumentSuffixes();
		Name[] names = new Name[suffixes.size()];
		for (int i = 0; i < names.length; i++) {
			String s = suffixes.get(i);
			Name n = data.getSingularName();
			names[i] = s.isEmpty() ? n : builderType.toName(s);
		}
		return names;
	}
	
	@Override
	protected JCExpression getPluralMethodParamType(JavacNode builderType) {
		return genTypeRef(builderType, getAddAllTypeName());
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName, String builderVariable) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression varType = chainDotsString(builderType, data.getTargetFqn());
		int argumentsCount = getTypeArgumentsCount();
		varType = addTypeArgs(argumentsCount, false, builderType, varType, data.getTypeArgs(), source);
		
		JCExpression empty; {
			//ImmutableX.of()
			JCExpression emptyMethod = chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), "of");
			List<JCExpression> invokeTypeArgs = createTypeArgs(argumentsCount, false, builderType, data.getTypeArgs(), source);
			empty = maker.Apply(invokeTypeArgs, emptyMethod, jceBlank);
		}
		
		JCExpression invokeBuild; {
			//this.pluralName.build();
			invokeBuild = maker.Apply(jceBlank, chainDots(builderType, builderVariable, data.getPluralName().toString(), "build"), jceBlank);
		}
		
		JCExpression isNull; {
			//this.pluralName == null
			isNull = maker.Binary(CTC_EQUAL, maker.Select(maker.Ident(builderType.toName(builderVariable)), data.getPluralName()), maker.Literal(CTC_BOT, null));
		}
		
		JCExpression init = maker.Conditional(isNull, empty, invokeBuild); // this.pluralName == null ? ImmutableX.of() : this.pluralName.build()
		
		JCStatement jcs = maker.VarDef(maker.Modifiers(0), data.getPluralName(), varType, init);
		statements.append(jcs);
	}

	@Override
	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression thisDotField2 = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression cond = maker.Binary(CTC_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));
		
		JCExpression create = maker.Apply(jceBlank, chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), getBuilderMethodName(data)), jceBlank);
		JCStatement thenPart = maker.Exec(maker.Assign(thisDotField2, create));
		
		return maker.If(cond, thenPart, null);
	}
	
	protected abstract LombokImmutableList<String> getArgumentSuffixes();
	
	protected abstract String getAddAllTypeName();
	
	@Override
	protected int getTypeArgumentsCount() {
		return getArgumentSuffixes().size();
	}
}
