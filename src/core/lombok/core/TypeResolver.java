/*
 * Copyright (C) 2009 The Project Lombok Authors.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.core.AST.Kind;

/**
 * Capable of resolving a simple type name such as 'String' into 'java.lang.String'.
 */
public class TypeResolver {
	private final TypeLibrary library;
	private Collection<String> imports;
	
	/**
	 * Creates a new TypeResolver that can be used to resolve types in a given library, encountered in
	 * a source file with the provided package and import statements.
	 */
	public TypeResolver(TypeLibrary library, String packageString, Collection<String> importStrings) {
		this.library = library;
		this.imports = makeImportList(packageString, importStrings);
	}
	
	private static Collection<String> makeImportList(String packageString, Collection<String> importStrings) {
		Set<String> imports = new HashSet<String>();
		if (packageString != null) imports.add(packageString + ".*");
		imports.addAll(importStrings == null ? Collections.<String>emptySet() : importStrings);
		return imports;
	}
	
	/**
	 * Finds type matches for the stated type reference. The provided context is scanned for local type names
	 * that shadow type names listed in import statements. If such a shadowing occurs, no matches are returned
	 * for any shadowed types, as you would expect.
	 */
	public Collection<String> findTypeMatches(LombokNode<?, ?, ?> context, String typeRef) {
		Collection<String> potentialMatches = library.findCompatible(typeRef);
		if (potentialMatches.isEmpty()) return Collections.emptyList();
		
		int idx = typeRef.indexOf('.');
		if (idx > -1) return potentialMatches;
		String simpleName = typeRef.substring(idx+1);
		
		//If there's an import statement that explicitly imports a 'Getter' that isn't any of our potentials, return no matches.
		if (nameConflictInImportList(simpleName, potentialMatches)) return Collections.emptyList();
		
		//Check if any of our potentials is even imported in the first place. If not: no matches.
		potentialMatches = eliminateImpossibleMatches(potentialMatches);
		if (potentialMatches.isEmpty()) return Collections.emptyList();
		
		//Find a lexically accessible type of the same simple name in the same Compilation Unit. If it exists: no matches.
		LombokNode<?, ?, ?> n = context;
		while (n != null) {
			if (n.getKind() == Kind.TYPE) {
				String name = n.getName();
				if (name != null && name.equals(simpleName)) return Collections.emptyList();
			}
			n = n.up();
		}
		
		// The potential matches we found by comparing the import statements is our matching set. Return it.
		return potentialMatches;
	}
	
	private Collection<String> eliminateImpossibleMatches(Collection<String> potentialMatches) {
		Set<String> results = new HashSet<String>();
		
		for (String importedType : imports) {
			Collection<String> reduced = new HashSet<String>(library.findCompatible(importedType));
			reduced.retainAll(potentialMatches);
			results.addAll(reduced);
		}
		
		return results;
	}
	
	private boolean nameConflictInImportList(String simpleName, Collection<String> potentialMatches) {
		for (String importedType : imports) {
			if (!toSimpleName(importedType).equals(simpleName)) continue;
			if (potentialMatches.contains(importedType)) continue;
			return true;
		}
		
		return false;
	}
	
	private static String toSimpleName(String typeName) {
		int idx = typeName.lastIndexOf('.');
		return idx == -1 ? typeName : typeName.substring(idx+1);
	}
}
