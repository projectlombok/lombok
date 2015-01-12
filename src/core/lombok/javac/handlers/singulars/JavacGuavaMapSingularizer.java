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

import lombok.core.LombokImmutableList;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import org.mangosdk.spi.ProviderFor;

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

@ProviderFor(JavacSingularizer.class)
public class JavacGuavaMapSingularizer extends JavacGuavaSingularizer {
	// TODO cgcc.ImmutableMultimap, cgcc.ImmutableListMultimap, cgcc.ImmutableSetMultimap
	// TODO cgcc.ImmutableClassToInstanceMap
	// TODO cgcc.ImmutableRangeMap
	
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of(
				"com.google.common.collect.ImmutableMap", 
				"com.google.common.collect.ImmutableBiMap", 
				"com.google.common.collect.ImmutableSortedMap");
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), "Builder");
		type = addTypeArgs(2, false, builderType, type, data.getTypeArgs(), source);
		
		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return Collections.singletonList(injectField(builderType, buildField));
	}
	
	@Override public void generateMethods(SingularData data, JavacNode builderType, JCTree source, boolean fluent, boolean chain) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		JCStatement returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generateSingularMethod(maker, returnType, returnStatement, data, builderType, source, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		generatePluralMethod(maker, returnType, returnStatement, data, builderType, source, fluent);
	}
	
	private void generateSingularMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		
		Name keyName = builderType.toName(data.getSingularName() + "$key");
		Name valueName = builderType.toName(data.getSingularName() + "$value");
		
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, true, source));
		JCExpression thisDotFieldDotPut = chainDots(builderType, "this", data.getPluralName().toString(), "put");
		JCExpression invokePut = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotPut, List.<JCExpression>of(maker.Ident(keyName), maker.Ident(valueName)));
		statements.append(maker.Exec(invokePut));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getSingularName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("put", name.toString()));
		JCExpression keyType = cloneParamType(0, maker, data.getTypeArgs(), builderType, source);
		JCExpression valueType = cloneParamType(1, maker, data.getTypeArgs(), builderType, source);
		JCVariableDecl paramKey = maker.VarDef(maker.Modifiers(paramFlags), keyName, keyType, null);
		JCVariableDecl paramValue = maker.VarDef(maker.Modifiers(paramFlags), valueName, valueType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(paramKey, paramValue), thrown, body, null);
		injectMethod(builderType, method);
	}
	
	private void generatePluralMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		statements.append(createConstructBuilderVarIfNeeded(maker, data, builderType, true, source));
		JCExpression thisDotFieldDotPutAll = chainDots(builderType, "this", data.getPluralName().toString(), "putAll");
		JCExpression invokePutAll = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotPutAll, List.<JCExpression>of(maker.Ident(data.getPluralName())));
		statements.append(maker.Exec(invokePutAll));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getPluralName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("putAll", name.toString()));
		JCExpression paramType = chainDots(builderType, "java", "util", "Map");
		paramType = addTypeArgs(2, true, builderType, paramType, data.getTypeArgs(), source);
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getPluralName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(param), thrown, body, null);
		injectMethod(builderType, method);
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression varType = chainDotsString(builderType, data.getTargetFqn());
		varType = addTypeArgs(2, false, builderType, varType, data.getTypeArgs(), source);
		
		JCExpression empty; {
			//ImmutableX.of()
			JCExpression emptyMethod = chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), "of");
			empty = maker.Apply(jceBlank, emptyMethod, jceBlank);
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
}
