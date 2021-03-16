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
package lombok.eclipse.handlers.singulars;

import lombok.core.LombokImmutableList;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.spi.Provides;

@Provides(EclipseSingularizer.class)
public class EclipseGuavaSetListSingularizer extends EclipseGuavaSingularizer {
	// TODO com.google.common.collect.ImmutableRangeSet
	// TODO com.google.common.collect.ImmutableMultiset and com.google.common.collect.ImmutableSortedMultiset
	private static final LombokImmutableList<String> SUFFIXES = LombokImmutableList.of("");
	private static final LombokImmutableList<String> SUPPORTED_TYPES = LombokImmutableList.of(
		"com.google.common.collect.ImmutableCollection", 
		"com.google.common.collect.ImmutableList", 
		"com.google.common.collect.ImmutableSet", 
		"com.google.common.collect.ImmutableSortedSet"
	);
	
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return SUPPORTED_TYPES;
	}
	
	@Override protected LombokImmutableList<String> getArgumentSuffixes() {
		return SUFFIXES;
	}
	
	@Override protected String getAddMethodName() {
		return "add";
	}
	
	@Override protected String getAddAllTypeName() {
		return "java.lang.Iterable";
	}
}
