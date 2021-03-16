/*
 * Copyright (C) 2015-2021 The Project Lombok Authors.
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

import lombok.core.LombokImmutableList;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import lombok.spi.Provides;

import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

@Provides(JavacSingularizer.class)
public class JavacJavaUtilSetSingularizer extends JavacJavaUtilListSetSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Set", "java.util.SortedSet", "java.util.NavigableSet");
	}
	
	@Override protected String getEmptyMaker(String target) {
		if (target.endsWith("SortedSet")) return "java.util.Collections.emptySortedSet";
		if (target.endsWith("NavigableSet")) return "java.util.Collections.emptyNavigableSet";
		return "java.util.Collections.emptySet";
	}
	
	@Override public void appendBuildCode(SingularData data, JavacNode builderType, JavacNode source, ListBuffer<JCStatement> statements, Name targetVariableName, String builderVariable) {
		JavacTreeMaker maker = builderType.getTreeMaker();
		
		if (data.getTargetFqn().equals("java.util.Set")) {
			statements.appendList(createJavaUtilSetMapInitialCapacitySwitchStatements(maker, data, builderType, false, "emptySet", "singleton", "LinkedHashSet", source, builderVariable));
		} else {
			statements.appendList(createJavaUtilSimpleCreationAndFillStatements(maker, data, builderType, false, true, false, true, "TreeSet", source, builderVariable));
		}
	}
}
