/*
 * Copyright (C) 2015-2017 The Project Lombok Authors.
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

import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symtab;
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

abstract class JavacJavaUtilListSetSingularizer extends JavacJavaUtilSingularizer {
	@Override public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.listFieldsToBeGenerated(data, builderType);
		}
		
		return super.listFieldsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.listMethodsToBeGenerated(data, builderType);
		}
		
		return super.listMethodsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.generateFields(data, builderType, source);
		}
		
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
		type = addTypeArgs(1, false, builderType, type, data.getTypeArgs(), source);
		
		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}
	
	@Override public void generateMethods(SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, boolean chain) {
		if (useGuavaInstead(builderType)) {
			guavaListSetSingularizer.generateMethods(data, deprecate, builderType, source, fluent, chain);
			return;
		}
		
		JavacTreeMaker maker = builderType.getTreeMaker();
		Symtab symbolTable = builderType.getSymbolTable();
		Name thisName = builderType.toName("this");
		
		JCExpression returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		JCStatement returnStatement = chain ? maker.Return(maker.Ident(thisName)) : null;
		generateSingularMethod(deprecate, maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(thisName)) : null;
		generatePluralMethod(deprecate, maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(thisName)) : null;
		generateClearMethod(deprecate, maker, returnType, returnStatement, data, builderType, source);
	}
	
	private void generateClearMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source) {
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		List<JCVariableDecl> params = List.nil();
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression thisDotFieldDotClear = maker.Select(maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName()), builderType.toName("clear"));
		JCStatement clearCall = maker.Exec(maker.Apply(jceBlank, thisDotFieldDotClear, jceBlank));
		JCExpression cond = maker.Binary(CTC_NOT_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));
		JCStatement ifSetCallClear = maker.If(cond, clearCall, null);
		List<JCStatement> statements = returnStatement != null ? List.of(ifSetCallClear, returnStatement) : List.of(ifSetCallClear);
		
		JCBlock body = maker.Block(0, statements);
		Name methodName = builderType.toName(HandlerUtil.buildAccessorName("clear", data.getPluralName().toString()));
		JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, params, thrown, body, null);
		injectMethod(builderType, method);
	}
	
	void generateSingularMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, false, source));
		JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), "add");
		JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, List.<JCExpression>of(maker.Ident(data.getSingularName())));
		statements.append(maker.Exec(invokeAdd));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getSingularName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("add", name.toString()));
		JCExpression paramType = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getSingularName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(param), thrown, body, null);
		injectMethod(builderType, method);
	}
	
	void generatePluralMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, false, source));
		JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), "addAll");
		JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, List.<JCExpression>of(maker.Ident(data.getPluralName())));
		statements.append(maker.Exec(invokeAdd));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getPluralName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("addAll", name.toString()));
		JCExpression paramType = chainDots(builderType, "java", "util", "Collection");
		paramType = addTypeArgs(1, true, builderType, paramType, data.getTypeArgs(), source);
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getPluralName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(param), thrown, body, null);
		injectMethod(builderType, method);
	}
}
