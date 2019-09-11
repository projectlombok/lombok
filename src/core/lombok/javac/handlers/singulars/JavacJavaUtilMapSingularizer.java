/*
 * Copyright (C) 2015-2019 The Project Lombok Authors.
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

import lombok.AccessLevel;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.ExpressionMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.javac.handlers.JavacSingularsRecipes.StatementMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacSingularizer.class)
public class JavacJavaUtilMapSingularizer extends JavacJavaUtilSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Map", "java.util.SortedMap", "java.util.NavigableMap");
	}
	
	@Override protected String getEmptyMaker(String target) {
		if (target.endsWith("NavigableMap")) return "java.util.Collections.emptyNavigableMap";
		if (target.endsWith("SortedMap")) return "java.util.Collections.emptySortedMap";
		return "java.util.Collections.emptyMap";
	}
	
	@Override protected JavacSingularizer getGuavaInstead(JavacNode node) {
		return new JavacGuavaMapSingularizer();
	}
	
	@Override public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
		String p = data.getPluralName().toString();
		return Arrays.asList(builderType.toName(p + "$key"), builderType.toName(p + "$value"));
	}
	
	@Override public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
		return super.listMethodsToBeGenerated(data, builderType);
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
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
	
	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		doGenerateMethods(cfv, data, deprecate, builderType, source, fluent, returnTypeMaker, returnStatementMaker, access);
	}
	
	@Override
	protected JCStatement generateClearStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType) {
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression thisDotKeyField = chainDots(builderType, "this", data.getPluralName() + "$key");
		JCExpression thisDotKeyFieldDotClear = chainDots(builderType, "this", data.getPluralName() + "$key", "clear");
		JCExpression thisDotValueFieldDotClear = chainDots(builderType, "this", data.getPluralName() + "$value", "clear");
		JCStatement clearKeyCall = maker.Exec(maker.Apply(jceBlank, thisDotKeyFieldDotClear, jceBlank));
		JCStatement clearValueCall = maker.Exec(maker.Apply(jceBlank, thisDotValueFieldDotClear, jceBlank));
		JCExpression cond = maker.Binary(CTC_NOT_EQUAL, thisDotKeyField, maker.Literal(CTC_BOT, null));
		JCBlock clearCalls = maker.Block(0, List.of(clearKeyCall, clearValueCall));
		return maker.If(cond, clearCalls, null);
	}
	
	@Override
	protected ListBuffer<JCStatement> generateSingularMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		Name keyName = builderType.toName(data.getSingularName().toString() + "Key");
		Name valueName = builderType.toName(data.getSingularName().toString() + "Value");
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		/* Generates: this.pluralname$key.add(singularnameKey); */
		statements.append(generateSingularMethodAddStatement(maker, builderType, keyName, data.getPluralName() + "$key"));
		/* Generates: this.pluralname$value.add(singularnameValue); */
		statements.append(generateSingularMethodAddStatement(maker, builderType, valueName, data.getPluralName() + "$value"));
		return statements;
	}
	
	@Override
	protected List<JCVariableDecl> generateSingularMethodParameters(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		Name keyName = builderType.toName(data.getSingularName().toString() + "Key");
		Name valueName = builderType.toName(data.getSingularName().toString() + "Value");
		JCVariableDecl paramKey = generateSingularMethodParameter(0, maker, data, builderType, source, keyName);
		JCVariableDecl paramValue = generateSingularMethodParameter(1, maker, data, builderType, source, valueName);
		return List.of(paramKey, paramValue);
	}
	
	@Override
	protected ListBuffer<JCStatement> generatePluralMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
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
		return statements;
	}
	
	@Override
	protected JCExpression getPluralMethodParamType(JavacNode builderType) {
		return chainDots(builderType, "java", "util", "Map");
	}
	
	@Override
	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		return createConstructBuilderVarIfNeeded(maker, data, builderType, true, source);
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName, String builderVariable) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		
		if (data.getTargetFqn().equals("java.util.Map")) {
			statements.appendList(createJavaUtilSetMapInitialCapacitySwitchStatements(maker, data, builderType, true, "emptyMap", "singletonMap", "LinkedHashMap", source, builderVariable));
		} else {
			statements.appendList(createJavaUtilSimpleCreationAndFillStatements(maker, data, builderType, true, true, false, true, "TreeMap", source, builderVariable));
		}
	}
	
	@Override
	protected String getAddMethodName() {
		return "put";
	}
	
	@Override
	protected int getTypeArgumentsCount() {
		return 2;
	}
}
