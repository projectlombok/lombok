/*
 * Copyright (C) 2009-2011 The Project Lombok Authors.
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
 * <p>
 * NB: This resolver gives wrong answers when there's a class in the local package with the same name as a class in a star-import,
 *  and this importer also can't find inner types from superclasses/interfaces.
 */
public class TypeResolver {
	private Collection<String> imports;
	
	/**
	 * Creates a new TypeResolver that can be used to resolve types in a source file with the given package and import statements.
	 */
	public TypeResolver(String packageString, Collection<String> importStrings) {
		this.imports = makeImportList(packageString, importStrings);
	}
	
	private static Collection<String> makeImportList(String packageString, Collection<String> importStrings) {
		Set<String> imports = new HashSet<String>();
		if (packageString != null) imports.add(packageString + ".*");
		imports.addAll(importStrings == null ? Collections.<String>emptySet() : importStrings);
		return imports;
	}
	
	public boolean typeMatches(LombokNode<?, ?, ?> context, String fqn, String typeRef) {
		return !findTypeMatches(context, TypeLibrary.createLibraryForSingleType(fqn), typeRef).isEmpty();
	}
	
	/**
	 * Finds type matches for the stated type reference. The provided context is scanned for local type names
	 * that shadow type names listed in import statements. If such a shadowing occurs, no matches are returned
	 * for any shadowed types, as you would expect.
	 */
	public Collection<String> findTypeMatches(LombokNode<?, ?, ?> context, TypeLibrary library, String typeRef) {
		// When asking if 'Foo' could possibly  be referring to 'bar.Baz', the answer is obviously no.
		Collection<String> potentialMatches = library.findCompatible(typeRef);
		if (potentialMatches.isEmpty()) return Collections.emptyList();
		
		// If input type appears to be fully qualified, we found a winner.
		int idx = typeRef.indexOf('.');
		if (idx > -1) return potentialMatches;
		
		// If there's an import statement that explicitly imports a 'Getter' that isn't any of our potentials, return no matches,
		// because if you want to know if 'Foo' could refer to 'bar.Foo' when 'baz.Foo' is explicitly imported, the answer is no.
		if (nameConflictInImportList(typeRef, potentialMatches)) return Collections.emptyList();
		
		// Check if any of our potentials are even imported in the first place. If not: no matches.
		// Note that (ourPackage.*) is added to the imports.
		potentialMatches = eliminateImpossibleMatches(potentialMatches, library);
		if (potentialMatches.isEmpty()) return Collections.emptyList();
		
		// Now the hard part - inner classes or method local classes in our own scope.
		// For method locals, this refers to any statements that are 'above' the type reference with the same name.
		// For inners, this refers to siblings of us or any parent node that are type declarations.
		LombokNode<?, ?, ?> n = context;
		
		mainLoop:
		while (n != null) {
			if (n.getKind() == Kind.TYPE && typeRef.equals(n.getName())) {
				// Our own class or one of our outer classes is named 'typeRef' so that's what 'typeRef' is referring to, not one of our type library classes.
				return Collections.emptyList();
			}
			
			if (n.getKind() == Kind.STATEMENT || n.getKind() == Kind.LOCAL) {
				LombokNode<?, ?, ?> newN = n.directUp();
				if (newN == null) break mainLoop;
				
				if (newN.getKind() == Kind.STATEMENT || newN.getKind() == Kind.INITIALIZER || newN.getKind() == Kind.METHOD) {
					for (LombokNode<?, ?, ?> child : newN.down()) {
						// We found a method local with the same name above our code. That's the one 'typeRef' is referring to, not
						// anything in the type library we're trying to find, so, no matches.
						if (child.getKind() == Kind.TYPE && typeRef.equals(child.getName())) return Collections.emptyList();
						if (child == n) break;
					}
				}
				n = newN;
				continue mainLoop;
			}
			
			if (n.getKind() == Kind.TYPE || n.getKind() == Kind.COMPILATION_UNIT) {
				for (LombokNode<?, ?, ?> child : n.down()) {
					// Inner class that's visible to us has 'typeRef' as name, so that's the one being referred to, not one of our type library classes.
					if (child.getKind() == Kind.TYPE && typeRef.equals(child.getName())) return Collections.emptyList();
				}
			}
			
			n = n.directUp();
		}
		
		// No class in this source file is a match, therefore the potential matches found via the import statements must be it. Return those.
		return potentialMatches;
	}
	
	private Collection<String> eliminateImpossibleMatches(Collection<String> potentialMatches, TypeLibrary library) {
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
