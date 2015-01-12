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

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

abstract class JavacGuavaSingularizer extends JavacSingularizer {
	protected String getSimpleTargetTypeName(SingularData data) {
		String simpleTypeName = data.getTargetSimpleType();
		if ("ImmutableCollection".equals(simpleTypeName)) return "ImmutableList";
		return simpleTypeName;
	}
	
	protected String getBuilderMethodName(SingularData data) {
		String simpleTypeName = data.getTargetSimpleType();
		if ("ImmutableSortedSet".equals(simpleTypeName) || "ImmutableSortedMap".equals(simpleTypeName)) return "naturalOrder";
		return "builder";
	}
	
	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, boolean mapMode, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression thisDotField2 = maker.Select(maker.Ident(builderType.toName("this")), data.getPluralName());
		JCExpression cond = maker.Binary(CTC_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));
		
		JCExpression create = maker.Apply(jceBlank, chainDots(builderType, "com", "google", "common", "collect", getSimpleTargetTypeName(data), getBuilderMethodName(data)), jceBlank);
		JCStatement thenPart = maker.Exec(maker.Assign(thisDotField2, create));
		
		return maker.If(cond, thenPart, null);
	}
}
