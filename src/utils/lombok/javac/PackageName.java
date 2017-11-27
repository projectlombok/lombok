/*
 * Copyright (C) 2017 The Project Lombok Authors.
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

import java.lang.reflect.Method;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

// Supports JDK6-9
public class PackageName {
	private static final Method packageNameMethod = getPackageNameMethod();
	
	private static Method getPackageNameMethod() {
		try {
			return JCCompilationUnit.class.getDeclaredMethod("getPackageName");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getPackageName(JCCompilationUnit cu) {
		JCTree t = getPackageNode(cu);
		return t != null ? t.toString() : null;
	}
	
	public static JCTree getPackageNode(JCCompilationUnit cu) {
		if (packageNameMethod != null) try {
			Object pkg = packageNameMethod.invoke(cu);
			return (pkg instanceof JCFieldAccess || pkg instanceof JCIdent) ? (JCTree) pkg : null;
		} catch (Exception e) {}
		return cu.pid instanceof JCFieldAccess || cu.pid instanceof JCIdent ? cu.pid : null;
	}
}
