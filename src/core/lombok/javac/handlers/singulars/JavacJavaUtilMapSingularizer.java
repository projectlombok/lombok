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

import java.util.Arrays;

import lombok.core.LombokImmutableList;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import org.mangosdk.spi.ProviderFor;

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

@ProviderFor(JavacSingularizer.class)
public class JavacJavaUtilMapSingularizer extends JavacJavaUtilSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Map", "java.util.SortedMap", "java.util.NavigableMap");
	}
	
	@Override public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.listFieldsToBeGenerated(data, builderType);
		}
		
		String p = data.getPluralName().toString();
		return Arrays.asList(builderType.toName(p + "$key"), builderType.toName(p + "$value"));
	}
	
	@Override public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.listMethodsToBeGenerated(data, builderType);
		}
		
		return super.listMethodsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.generateFields(data, builderType, source);
		}
		
		JavacTreeMaker maker = builderType.getTreeMaker();
		
		JCVariableDecl buildKeyField; {
			JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
			type = addTypeArgs(1, false, builderType, type, data.getTypeArgs(), source);
			buildKeyField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName(data.getPluralName() + "$key"), type, null);
		}
		
		JCVariableDecl buildValueField; {
			JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
			List<JCExpression> tArgs = data.getTypeArgs();
			if (tArgs != null && tArgs.size() > 1) tArgs = tArgs.tail;
			else tArgs = List.nil();
			type = addTypeArgs(1, false, builderType, type, tArgs, source);
			buildValueField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName(data.getPluralName() + "$value"), type, null);
		}
		
		JavacNode valueFieldNode = injectFieldAndMarkGenerated(builderType, buildValueField);
		JavacNode keyFieldNode = injectFieldAndMarkGenerated(builderType, buildKeyField);
		
		return Arrays.asList(keyFieldNode, valueFieldNode);
	}
	
	@Override public void generateMethods(SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, boolean chain) {
		if (useGuavaInstead(builderType)) {
			guavaMapSingularizer.generateMethods(data, deprecate, builderType, source, fluent, chain);
			return;
		}
		
		JavacTreeMaker maker = builderType.getTreeMaker();
		Symtab symbolTable = builderType.getSymbolTable();
		
		JCExpression returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		JCStatement returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generateSingularMethod(deprecate, maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generatePluralMethod(deprecate, maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(symbolTable, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generateClearMethod(deprecate, maker, returnType, returnStatement, data, builderType, source);
	}
	
	private void generateClearMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source) {
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		List<JCVariableDecl> params = List.nil();
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression thisDotKeyField = chainDots(builderType, "this", data.getPluralName() + "$key");
		JCExpression thisDotKeyFieldDotClear = chainDots(builderType, "this", data.getPluralName() + "$key", "clear");
		JCExpression thisDotValueFieldDotClear = chainDots(builderType, "this", data.getPluralName() + "$value", "clear");
		JCStatement clearKeyCall = maker.Exec(maker.Apply(jceBlank, thisDotKeyFieldDotClear, jceBlank));
		JCStatement clearValueCall = maker.Exec(maker.Apply(jceBlank, thisDotValueFieldDotClear, jceBlank));
		JCExpression cond = maker.Binary(CTC_NOT_EQUAL, thisDotKeyField, maker.Literal(CTC_BOT, null));
		JCBlock clearCalls = maker.Block(0, List.of(clearKeyCall, clearValueCall));
		JCStatement ifSetCallClear = maker.If(cond, clearCalls, null);
		List<JCStatement> statements = returnStatement != null ? List.of(ifSetCallClear, returnStatement) : List.of(ifSetCallClear);
		
		JCBlock body = maker.Block(0, statements);
		Name methodName = builderType.toName(HandlerUtil.buildAccessorName("clear", data.getPluralName().toString()));
		JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, params, thrown, body, null);
		injectMethod(builderType, method);
	}
	
	private void generateSingularMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, true, source));
		Name keyName = builderType.toName(data.getSingularName().toString() + "Key");
		Name valueName = builderType.toName(data.getSingularName().toString() + "Value");
		/* this.pluralname$key.add(singularnameKey); */ {
			JCExpression thisDotKeyFieldDotAdd = chainDots(builderType, "this", data.getPluralName() + "$key", "add");
			JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotKeyFieldDotAdd, List.<JCExpression>of(maker.Ident(keyName)));
			statements.append(maker.Exec(invokeAdd));
		}
		/* this.pluralname$value.add(singularnameValue); */ {
			JCExpression thisDotValueFieldDotAdd = chainDots(builderType, "this", data.getPluralName() + "$value", "add");
			JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotValueFieldDotAdd, List.<JCExpression>of(maker.Ident(valueName)));
			statements.append(maker.Exec(invokeAdd));
		}
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		
		Name name = data.getSingularName();
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("put", name.toString()));
		JCExpression paramTypeKey = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
		JCExpression paramTypeValue = cloneParamType(1, maker, data.getTypeArgs(), builderType, source);
		JCVariableDecl paramKey = maker.VarDef(maker.Modifiers(paramFlags), keyName, paramTypeKey, null);
		JCVariableDecl paramValue = maker.VarDef(maker.Modifiers(paramFlags), valueName, paramTypeValue, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(paramKey, paramValue), thrown, body, null);
		injectMethod(builderType, method);
	}

	private void generatePluralMethod(boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> jceBlank = List.nil();
		JCModifiers mods = makeMods(maker, builderType, deprecate);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, true, source));
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		long baseFlags = JavacHandlerUtil.addFinalIfNeeded(0, builderType.getContext());
		Name entryName = builderType.toName("$lombokEntry");
		
		JCExpression forEachType = chainDots(builderType, "java", "util", "Map", "Entry");
		forEachType = addTypeArgs(2, true, builderType, forEachType, data.getTypeArgs(), source);
		JCExpression keyArg = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(entryName), builderType.toName("getKey")), List.<JCExpression>nil());
		JCExpression valueArg = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(entryName), builderType.toName("getValue")), List.<JCExpression>nil());
		JCExpression addKey = maker.Apply(List.<JCExpression>nil(), chainDots(builderType, "this", data.getPluralName() + "$key", "add"), List.of(keyArg));
		JCExpression addValue = maker.Apply(List.<JCExpression>nil(), chainDots(builderType, "this", data.getPluralName() + "$value", "add"), List.of(valueArg));
		JCBlock forEachBody = maker.Block(0, List.<JCStatement>of(maker.Exec(addKey), maker.Exec(addValue)));
		JCExpression entrySetInvocation = maker.Apply(jceBlank, maker.Select(maker.Ident(data.getPluralName()), builderType.toName("entrySet")), jceBlank);
		JCStatement forEach = maker.ForeachLoop(maker.VarDef(maker.Modifiers(baseFlags), entryName, forEachType, null), entrySetInvocation, forEachBody);
		statements.append(forEach);
		
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getPluralName();
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("putAll", name.toString()));
		JCExpression paramType = chainDots(builderType, "java", "util", "Map");
		paramType = addTypeArgs(2, true, builderType, paramType, data.getTypeArgs(), source);
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getPluralName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(param), jceBlank, body, null);
		injectMethod(builderType, method);
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName) {
		if (useGuavaInstead(builderType)) {
			guavaMapSingularizer.appendBuildCode(data, builderType, source, statements, targetVariableName);
			return;
		}
		
		JavacTreeMaker maker = builderType.getTreeMaker();
		
		if (data.getTargetFqn().equals("java.util.Map")) {
			statements.appendList(createJavaUtilSetMapInitialCapacitySwitchStatements(maker, data, builderType, true, "emptyMap", "singletonMap", "LinkedHashMap", source));
		} else {
			statements.appendList(createJavaUtilSimpleCreationAndFillStatements(maker, data, builderType, true, true, false, true, "TreeMap", source));
		}
	}
}
