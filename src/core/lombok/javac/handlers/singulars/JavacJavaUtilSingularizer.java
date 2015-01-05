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
import static lombok.javac.handlers.JavacHandlerUtil.chainDots;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

public abstract class JavacJavaUtilSingularizer extends JavacSingularizer {
	protected JCExpression createJavaUtilSetMapInitialCapacityExpression(JavacTreeMaker maker, SingularData data, JavacNode builderType) {
		JCExpression lessThanCutoff = maker.Binary(CTC_LESS_THAN, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 0x40000000));
		JCExpression maxInt = chainDots(builderType, "java", "lang", "Integer", "MAX_VALUE");
		JCExpression belowThree = maker.Binary(CTC_LESS_THAN, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 3));
		JCExpression sizePlusOne = maker.Binary(CTC_PLUS, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 1));
		JCExpression sizeDivThree = maker.Binary(CTC_DIV, getSize(maker, builderType, data.getPluralName()), maker.Literal(CTC_INT, 3));
		JCExpression sizePlusSizeDivThree = maker.Binary(CTC_PLUS, getSize(maker, builderType, data.getPluralName()), sizeDivThree);
		JCExpression rest = maker.Conditional(belowThree, sizePlusOne, sizePlusSizeDivThree);
		JCExpression initialCapacity = maker.Conditional(lessThanCutoff, rest, maxInt);
		return initialCapacity;
	}
	
	protected void stuffJavaUtilCollectionAndWrapWithUnmodifiable(SingularData data, JavacNode builderType, ListBuffer<JCStatement> statements, JavacTreeMaker maker, String addAllName) {
		JCFieldAccess varDotAddAll = maker.Select(maker.Ident(data.getPluralName()), builderType.toName(addAllName));
		JCExpression thisDotFieldName = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		statements.append(maker.Exec(maker.Apply(List.<JCExpression>nil(), varDotAddAll, List.of(thisDotFieldName))));
		String singletonMaker = "unmodifiable" + data.getTargetFqn().substring(data.getTargetFqn().lastIndexOf(".") + 1);
		JCExpression javaUtilCollectionsInvoke = maker.Apply(List.<JCExpression>nil(), chainDots(builderType, "java", "util", "Collections", singletonMaker), List.<JCExpression>of(maker.Ident(data.getPluralName())));
		statements.append(maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), javaUtilCollectionsInvoke)));
	}
}
