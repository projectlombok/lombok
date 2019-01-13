/*
 * Copyright (C) 2015-2018 The Project Lombok Authors.
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

abstract class JavacJavaUtilListSetSingularizer extends JavacJavaUtilSingularizer {

	@Override protected JavacSingularizer getGuavaInstead(JavacNode node) {
		return new JavacGuavaSetListSingularizer();
	}

	@Override public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
		return super.listFieldsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
		return super.listMethodsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
		type = addTypeArgs(1, false, builderType, type, data.getTypeArgs(), source);
		
		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}
	
	@Override public void generateMethods(SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker) {
		doGenerateMethods(data, deprecate, builderType, source, fluent, returnTypeMaker, returnStatementMaker);
	}

	@Override
	protected JCStatement generateClearStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType) {
		List<JCExpression> jceBlank = List.nil();
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression thisDotFieldDotClear = maker.Select(maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName()), builderType.toName("clear"));

		JCStatement clearCall = maker.Exec(maker.Apply(jceBlank, thisDotFieldDotClear, jceBlank));
		JCExpression cond = maker.Binary(CTC_NOT_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));

		return maker.If(cond, clearCall, null);
	}

	@Override
	protected ListBuffer<JCStatement> generateSingularMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		return new ListBuffer<JCStatement>()
			.append(generateSingularMethodAddStatement(maker, builderType, data.getSingularName(), data.getPluralName().toString()));
	}

	@Override
	protected List<JCVariableDecl> generateSingularMethodParameters(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		JCVariableDecl param = generateSingularMethodParameter(0, maker, data, builderType, source, data.getSingularName());
		return List.of(param);
	}

	@Override
	protected JCExpression getPluralMethodParamType(JavacNode builderType) {
		return chainDots(builderType, "java", "util", "Collection");
	}

	@Override
	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		return createConstructBuilderVarIfNeeded(maker, data, builderType, false, source);
	}

	@Override
	protected String getAddMethodName() {
		return "add";
	}

	@Override
	protected int getTypeArgumentsCount() {
		return 1;
	}
}
