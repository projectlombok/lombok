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

import org.mangosdk.spi.ProviderFor;

import lombok.core.LombokImmutableList;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
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
public class JavacJavaUtilListSingularizer extends JavacJavaUtilSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.List", "java.util.Collection", "java.util.Iterable");
	}
	
	@Override public java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
		type = addTypeArgs(1, false, builderType, type, data.getTypeArgs(), source);
		
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
		
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
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
	
	private void generatePluralMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
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
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		List<JCExpression> jceBlank = List.nil();
		ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
		
		/* case 0: (empty); break; */ {
			JCStatement assignStat; {
				// pluralName = java.util.Collections.emptyList();
				JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", "emptyList"), jceBlank);
				assignStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
			}
			JCStatement breakStat = maker.Break(null);
			JCCase emptyCase = maker.Case(maker.Literal(CTC_INT, 0), List.of(assignStat, breakStat));
			cases.append(emptyCase);
		}
		
		/* case 1: (singletonList); break; */ {
			JCStatement assignStat; {
				// pluralName = java.util.Collections.singletonList(this.pluralName.get(0));
				JCExpression zeroLiteral = maker.Literal(CTC_INT, 0);
				JCExpression arg = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName().toString(), "get"), List.of(zeroLiteral));
				List<JCExpression> args = List.of(arg);
				JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", "singletonList"), args);
				assignStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
			}
			JCStatement breakStat = maker.Break(null);
			JCCase singletonCase = maker.Case(maker.Literal(CTC_INT, 1), List.of(assignStat, breakStat));
			cases.append(singletonCase);
		}
		
		/* default: Create with right size, then addAll */ {
			List<JCStatement> defStats = createListCopy(maker, data, builderType, source);
			JCCase defaultCase = maker.Case(null, defStats);
			cases.append(defaultCase);
		}
		
		JCStatement switchStat = maker.Switch(getSize(maker,  builderType, data.getPluralName(), true), cases.toList());
		JCExpression localShadowerType = chainDots(builderType, "java", "util", "List");
		localShadowerType = addTypeArgs(1, false, builderType, localShadowerType, data.getTypeArgs(), source);
		JCStatement varDefStat = maker.VarDef(maker.Modifiers(0), data.getPluralName(), localShadowerType, null);
		statements.append(varDefStat);
		statements.append(switchStat);
	}
	
	private List<JCStatement> createListCopy(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		Name thisName = builderType.toName("this");
		
		JCStatement createStat; {
			 // pluralName = new java.util.ArrayList<Generics>(this.pluralName.size());
			List<JCExpression> constructorArgs = List.nil();
			constructorArgs = List.<JCExpression>of(getSize(maker, builderType, data.getPluralName(), false));
			JCExpression targetTypeExpr = chainDots(builderType, "java", "util", "ArrayList");
			targetTypeExpr = addTypeArgs(1, false, builderType, targetTypeExpr, data.getTypeArgs(), source);
			JCExpression constructorCall = maker.NewClass(null, jceBlank, targetTypeExpr, constructorArgs, null);
			createStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), constructorCall));
		}
		
		JCStatement fillStat; {
			// pluralname.addAll(this.pluralname);
			JCExpression thisDotPluralName = maker.Select(maker.Ident(thisName), data.getPluralName());
			fillStat = maker.Exec(maker.Apply(jceBlank, maker.Select(maker.Ident(data.getPluralName()), builderType.toName("addAll")), List.of(thisDotPluralName)));
		}
		
		JCStatement unmodifiableStat; {
			// pluralname = Collections.unmodifiableInterfaceType(pluralname);
			JCExpression arg = maker.Ident(data.getPluralName());
			JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", "unmodifiableList"), List.of(arg));
			unmodifiableStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
		}
		
		return List.of(createStat, fillStat, unmodifiableStat);
	}
}
