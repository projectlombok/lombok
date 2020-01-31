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
package lombok.eclipse;

import static lombok.eclipse.Eclipse.toQualifiedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.core.ImportList;
import lombok.core.LombokInternalAliasing;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class EclipseImportList implements ImportList {
	private ImportReference[] imports;
	private ImportReference pkg;
	
	public EclipseImportList(CompilationUnitDeclaration cud) {
		this.pkg = cud.currentPackage;
		this.imports = cud.imports;
	}
	
	@Override public String getFullyQualifiedNameForSimpleName(String unqualified) {
		String q = getFullyQualifiedNameForSimpleNameNoAliasing(unqualified);
		return q == null ? null : LombokInternalAliasing.processAliases(q);
	}
	
	@Override public String getFullyQualifiedNameForSimpleNameNoAliasing(String unqualified) {
		if (imports != null) {
			outer:
			for (ImportReference imp : imports) {
				if ((imp.bits & ASTNode.OnDemand) != 0) continue;
				char[][] tokens = imp.tokens;
				char[] token = tokens.length == 0 ? new char[0] : tokens[tokens.length - 1];
				int len = token.length;
				if (len != unqualified.length()) continue;
				for (int i = 0; i < len; i++) if (token[i] != unqualified.charAt(i)) continue outer;
				return toQualifiedName(tokens);
			}
		}
		return null;
	}
	
	@Override public boolean hasStarImport(String packageName) {
		if (isEqual(packageName, pkg)) return true;
		if ("java.lang".equals(packageName)) return true;
		
		if (imports != null) for (ImportReference imp : imports) {
			if ((imp.bits & ASTNode.OnDemand) == 0) continue;
			if (imp.isStatic()) continue;
			if (isEqual(packageName, imp)) return true;
			
		}
		return false;
	}
	
	private static boolean isEqual(String packageName, ImportReference pkgOrStarImport) {
		if (pkgOrStarImport == null || pkgOrStarImport.tokens == null || pkgOrStarImport.tokens.length == 0) return packageName.isEmpty();
		int pos = 0;
		int len = packageName.length();
		for (int i = 0; i < pkgOrStarImport.tokens.length; i++) {
			if (i != 0) {
				if (pos >= len) return false;
				if (packageName.charAt(pos++) != '.') return false;
			}
			for (int j = 0; j < pkgOrStarImport.tokens[i].length; j++) {
				if (pos >= len) return false;
				if (packageName.charAt(pos++) != pkgOrStarImport.tokens[i][j]) return false;
			}
		}
		return true;
	}
	
	@Override public Collection<String> applyNameToStarImports(String startsWith, String name) {
		List<String> out = Collections.emptyList();
		
		if (pkg != null && pkg.tokens != null && pkg.tokens.length != 0) {
			char[] first = pkg.tokens[0];
			int len = first.length;
			boolean match = true;
			if (startsWith.length() == len) {
				for (int i = 0; match && i < len; i++) {
					if (startsWith.charAt(i) != first[i]) match = false;
				}
				if (match) out.add(toQualifiedName(pkg.tokens) + "." + name);
			}
		}
		
		if (imports != null) {
			outer:
			for (ImportReference imp : imports) {
				if ((imp.bits & ASTNode.OnDemand) == 0) continue;
				if (imp.isStatic()) continue;
				if (imp.tokens == null || imp.tokens.length == 0) continue;
				char[] firstToken = imp.tokens[0];
				if (firstToken.length != startsWith.length()) continue;
				for (int i = 0; i < firstToken.length; i++) if (startsWith.charAt(i) != firstToken[i]) continue outer;
				String fqn = toQualifiedName(imp.tokens) + "." + name;
				if (out.isEmpty()) out = Collections.singletonList(fqn);
				else if (out.size() == 1) {
					out = new ArrayList<String>(out);
					out.add(fqn);
				} else {
					out.add(fqn);
				}
			}
		}
		return out;
	}
	
	@Override public String applyUnqualifiedNameToPackage(String unqualified) {
		if (pkg == null || pkg.tokens == null || pkg.tokens.length == 0) return unqualified;
		return toQualifiedName(pkg.tokens) + "." + unqualified;
	}
}
