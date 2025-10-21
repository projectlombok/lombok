/*
 * Copyright (C) 2015-2025 The Project Lombok Authors.
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
package lombok.eclipse.handlers.singulars;

import java.util.List;

import lombok.core.LombokImmutableList;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.spi.Provides;

import org.eclipse.jdt.internal.compiler.ast.Statement;

@Provides(EclipseSingularizer.class)
public class EclipseJavaUtilSetSingularizer extends EclipseJavaUtilListSetSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Set", "java.util.SortedSet", "java.util.NavigableSet", "java.util.SequencedSet");
	}
	
	private static final char[] EMPTY_SORTED_SET = {'e', 'm', 'p', 't', 'y', 'S', 'o', 'r', 't', 'e', 'd', 'S', 'e', 't'};
	private static final char[] EMPTY_NAVIGABLE_SET = {'e', 'm', 'p', 't', 'y', 'N', 'a', 'v', 'i', 'g', 'a', 'b', 'l', 'e', 'S', 'e', 't'};
	private static final char[] EMPTY_SET = {'e', 'm', 'p', 't', 'y', 'S', 'e', 't'};
	
	@Override protected char[][] getEmptyMakerReceiver(String targetFqn) {
		return JAVA_UTIL_COLLECTIONS;
	}
	
	@Override protected char[] getEmptyMakerSelector(String targetFqn) {
		if (targetFqn.endsWith("SortedSet") || targetFqn.endsWith("SequencedSet")) return EMPTY_SORTED_SET;
		if (targetFqn.endsWith("NavigableSet")) return EMPTY_NAVIGABLE_SET;
		return EMPTY_SET;
	}
	
	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName, String builderVariable) {
		if (useGuavaInstead(builderType)) {
			guavaListSetSingularizer.appendBuildCode(data, builderType, statements, targetVariableName, builderVariable);
			return;
		}
		
		if (data.getTargetFqn().equals("java.util.Set")) {
			statements.addAll(createJavaUtilSetMapInitialCapacitySwitchStatements(data, builderType, false, "emptySet", "singleton", "LinkedHashSet", builderVariable));
		} else if (data.getTargetFqn().equals("java.util.SequencedSet")) {
			statements.addAll(createJavaUtilSimpleCreationAndFillStatements(data, builderType, false, true, false, true, "LinkedHashSet", builderVariable));
		} else {
			statements.addAll(createJavaUtilSimpleCreationAndFillStatements(data, builderType, false, true, false, true, "TreeSet", builderVariable));
		}
	}
}
