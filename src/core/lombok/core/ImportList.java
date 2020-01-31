/*
 * Copyright (C) 2013-2020 The Project Lombok Authors.
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
package lombok.core;

import java.util.Collection;

public interface ImportList {
	/**
	 * If there is an explicit import of the stated unqualified type name, return that. Otherwise, return null.
	 */
	String getFullyQualifiedNameForSimpleName(String unqualified);
	
	/**
	 * If there is an explicit import of the stated unqualified type name, return that. Otherwise, return null.
	 * Do not translate the produced fully qualified name to the alias.
	 */
	String getFullyQualifiedNameForSimpleNameNoAliasing(String unqualified);
	
	/**
	 * Returns true if the package name is explicitly star-imported, OR the packageName refers to this source file's own package name, OR packageName is 'java.lang'.
	 */
	boolean hasStarImport(String packageName);
	
	/**
	 * Takes all explicit non-static star imports whose first element is equal to {@code startsWith}, replaces the star with {@code unqualified}, and returns these.
	 */
	Collection<String> applyNameToStarImports(String startsWith, String unqualified);
	
	String applyUnqualifiedNameToPackage(String unqualified);
}
