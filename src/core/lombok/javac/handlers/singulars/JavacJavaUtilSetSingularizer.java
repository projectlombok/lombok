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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import lombok.core.LombokImmutableList;
import lombok.core.handlers.HandlerUtil;
import static lombok.javac.Javac.*;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacSingularizer.class)
public class JavacJavaUtilSetSingularizer extends JavacJavaUtilSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Set", "java.util.SortedSet", "java.util.NavigableSet");
	}
	
	@Override public JavacNode generateFields(SingularData data, JavacNode builderType, JCTree source) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression type = JavacHandlerUtil.chainDots(builderType, "java", "util", "ArrayList");
		type = addTypeArgs(1, false, builderType, type, data.getTypeArgs(), source);
		
		JCVariableDecl buildField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), data.getPluralName(), type, null);
		return injectField(builderType, buildField);
	}
	
	@Override public void generateMethods(SingularData data, JavacNode builderType, JCTree source, boolean fluent, boolean chain) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression returnType = chain ? cloneSelfType(builderType) : maker.Type(createVoidType(maker, CTC_VOID));
		JCStatement returnStatement = chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
		
		generateSingularMethod(maker, returnType, returnStatement, data, builderType, source, fluent);
		generatePluralMethod(maker, returnType, returnStatement, data, builderType, source, fluent);
	}
	
	private void generateSingularMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), "add");
		JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, List.<JCExpression>of(maker.Ident(data.getSingularName())));
		statements.append(maker.Exec(invokeAdd));
		if (returnStatement != null) statements.append(returnStatement);
		JCBlock body = maker.Block(0, statements.toList());
		Name name = data.getSingularName();
		long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
		if (!fluent) name = builderType.toName(HandlerUtil.buildAccessorName("add", name.toString()));
		JCExpression paramType; {
			if (data.getTypeArgs() == null || data.getTypeArgs().isEmpty()) {
				paramType = chainDots(builderType, "java", "lang", "Object");
			} else {
				JCExpression originalType = data.getTypeArgs().head;
				if (originalType.getKind() == Kind.UNBOUNDED_WILDCARD || originalType.getKind() == Kind.SUPER_WILDCARD) {
					paramType = chainDots(builderType, "java", "lang", "Object");
				} else if (originalType.getKind() == Kind.EXTENDS_WILDCARD) {
					try {
						paramType = cloneType(maker, (JCExpression) ((JCWildcard) originalType).inner, source, builderType.getContext());
					} catch (Exception e) {
						paramType = chainDots(builderType, "java", "lang", "Object");
					}
				} else {
					paramType = cloneType(maker, originalType, source, builderType.getContext());
				}
			}
		}
		JCVariableDecl param = maker.VarDef(maker.Modifiers(paramFlags), data.getSingularName(), paramType, null);
		JCMethodDecl method = maker.MethodDef(mods, name, returnType, typeParams, List.of(param), thrown, body, null);
		injectMethod(builderType, method);
	}
	
	private void generatePluralMethod(JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent) {
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrown = List.nil();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
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
	
	private JCExpression getSize(JavacTreeMaker maker, JavacNode builderType, Name name) {
		JCExpression fn = maker.Select(maker.Select(maker.Ident(builderType.toName("this")), name), builderType.toName("size"));
		return maker.Apply(List.<JCExpression>nil(), fn, List.<JCExpression>nil());
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		JCExpression localShadowerType = chainDotsString(builderType, data.getTargetFqn());
		localShadowerType = addTypeArgs(1, false, builderType, localShadowerType, data.getTypeArgs(), source);
		JCExpression constructTargetType; {
			if (data.getTargetFqn().equals("java.util.Set")) {
				JCExpression internalType = chainDots(builderType, "java", "util", "LinkedHashSet");
				internalType = addTypeArgs(1, false, builderType, internalType, data.getTypeArgs(), source);
				JCExpression loadFactor = maker.Literal(CTC_FLOAT, 0.75f);
				JCExpression lessThanCutoff = maker.Binary(CTC_LESS_THAN, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 0x40000000));
				JCExpression maxInt = chainDots(builderType, "java", "lang", "Integer", "MAX_VALUE");
				JCExpression belowThree = maker.Binary(CTC_LESS_THAN, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 3));
				JCExpression sizePlusOne = maker.Binary(CTC_PLUS, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 1));
				JCExpression sizeDivThree = maker.Binary(CTC_DIV, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 3));
				JCExpression sizePlusSizeDivThree = maker.Binary(CTC_PLUS, getSize(maker, builderType, data.getPluralName()), sizeDivThree);
				JCExpression rest = maker.Conditional(belowThree, sizePlusOne, sizePlusSizeDivThree);
				JCExpression initialCapacity = maker.Conditional(lessThanCutoff, rest, maxInt);
				constructTargetType = maker.NewClass(null, List.<JCExpression>nil(), internalType, List.<JCExpression>of(initialCapacity, loadFactor), null);
			} else {
				JCExpression internalType = chainDots(builderType, "java", "util", "TreeSet");
				internalType = addTypeArgs(1, false, builderType, internalType, data.getTypeArgs(), source);
				constructTargetType = maker.NewClass(null, List.<JCExpression>nil(), internalType, List.<JCExpression>nil(), null);
			}
		}
		JCVariableDecl varDef = maker.VarDef(maker.Modifiers(0), data.getPluralName(), localShadowerType, constructTargetType);
		statements.append(varDef);
		JCFieldAccess varDotAddAll = maker.Select(maker.Ident(data.getPluralName()), builderType.toName("addAll"));
		JCExpression thisDotFieldName = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		statements.append(maker.Exec(maker.Apply(List.<JCExpression>nil(), varDotAddAll, List.of(thisDotFieldName))));
		String singletonMaker = "unmodifiable" + data.getTargetFqn().substring(data.getTargetFqn().lastIndexOf(".") + 1);
		JCExpression javaUtilCollectionsInvoke = maker.Apply(List.<JCExpression>nil(), chainDots(builderType, "java", "util", "Collections", singletonMaker), List.<JCExpression>of(maker.Ident(data.getPluralName())));
		statements.append(maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), javaUtilCollectionsInvoke)));
	}
}
