/*
 * Copyright (C) 2010-2013 The Project Lombok Authors.
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
package lombok.javac;

import java.util.HashSet;
import java.util.Set;

import lombok.delombok.FormatPreferences;
import lombok.delombok.LombokOptionsFactory;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public abstract class LombokOptions extends Options {
	private boolean deleteLombokAnnotations = false;
	private final Set<JCCompilationUnit> changed = new HashSet<JCCompilationUnit>();
	private FormatPreferences formatPreferences = new FormatPreferences(null);
	
	public boolean isChanged(JCCompilationUnit ast) {
		return changed.contains(ast);
	}
	
	public void setFormatPreferences(FormatPreferences formatPreferences) {
		this.formatPreferences = formatPreferences;
	}
	
	public FormatPreferences getFormatPreferences() {
		return this.formatPreferences;
	}
	
	public static void markChanged(Context context, JCCompilationUnit ast) {
		LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
		options.changed.add(ast);
	}
	
	public static boolean shouldDeleteLombokAnnotations(Context context) {
		LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
		return options.deleteLombokAnnotations;
	}
	
	protected LombokOptions(Context context) {
		super(context);
	}
	
	public abstract void putJavacOption(String optionName, String value);
	
	public void deleteLombokAnnotations() {
		this.deleteLombokAnnotations = true;
	}
}
