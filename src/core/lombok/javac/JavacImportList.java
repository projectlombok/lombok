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
package lombok.javac;

import java.util.ArrayList;
import java.util.Collection;

import lombok.core.ImportList;
import lombok.core.LombokInternalAliasing;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.util.List;

public class JavacImportList implements ImportList {
	private final String pkgStr;
	private final List<JCTree> defs;
	
	public JavacImportList(JCCompilationUnit cud) {
		this.pkgStr = PackageName.getPackageName(cud);
		this.defs = cud.defs;
	}
	
	@Override public String getFullyQualifiedNameForSimpleName(String unqualified) {
		String q = getFullyQualifiedNameForSimpleNameNoAliasing(unqualified);
		return q == null ? null : LombokInternalAliasing.processAliases(q);
	}
	
	@Override public String getFullyQualifiedNameForSimpleNameNoAliasing(String unqualified) {
		for (JCTree def : defs) {
			if (!(def instanceof JCImport)) continue;
			JCTree qual = Javac.getQualid((JCImport) def);
			if (!(qual instanceof JCFieldAccess)) continue;
			String simpleName = ((JCFieldAccess) qual).name.toString();
			if (simpleName.equals(unqualified)) {
				return qual.toString();
			}
		}
		
		return null;
	}
	
	@Override public boolean hasStarImport(String packageName) {
		if (pkgStr != null && pkgStr.equals(packageName)) return true;
		if ("java.lang".equals(packageName)) return true;
		
		for (JCTree def : defs) {
			if (!(def instanceof JCImport)) continue;
			if (((JCImport) def).staticImport) continue;
			JCTree qual = Javac.getQualid((JCImport) def);
			if (!(qual instanceof JCFieldAccess)) continue;
			String simpleName = ((JCFieldAccess) qual).name.toString();
			if (!"*".equals(simpleName)) continue;
			String starImport = ((JCFieldAccess) qual).selected.toString();
			if (packageName.equals(starImport)) return true;
		}
		
		return false;
	}
	
	@Override public Collection<String> applyNameToStarImports(String startsWith, String name) {
		ArrayList<String> out = new ArrayList<String>();
		
		if (pkgStr != null && topLevelName(pkgStr).equals(startsWith)) out.add(pkgStr + "." + name);
		
		for (JCTree def : defs) {
			if (!(def instanceof JCImport)) continue;
			if (((JCImport) def).staticImport) continue;
			JCTree qual = Javac.getQualid((JCImport) def);
			if (!(qual instanceof JCFieldAccess)) continue;
			String simpleName = ((JCFieldAccess) qual).name.toString();
			if (!"*".equals(simpleName)) continue;
			
			String topLevelName = topLevelName(qual);
			if (topLevelName.equals(startsWith)) {
				out.add(((JCFieldAccess) qual).selected.toString() + "." + name);
			}
		}
		
		return out;
	}
	
	private String topLevelName(JCTree tree) {
		while (tree instanceof JCFieldAccess) tree = ((JCFieldAccess) tree).selected;
		return tree.toString();
	}
	
	private String topLevelName(String packageName) {
		int idx = packageName.indexOf(".");
		if (idx == -1) return packageName;
		return packageName.substring(0, idx);
	}
	
	@Override public String applyUnqualifiedNameToPackage(String unqualified) {
		if (pkgStr == null) return unqualified;
		return pkgStr + "." + unqualified;
	}
}
