/*
 * Copyright (C) 2009-2020 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.core.AST.Kind;

/**
 * Capable of resolving a simple type name such as 'String' into 'java.lang.String'.
 * <p>
 * NB: This resolver gives wrong answers when there's a class in the local package with the same name as a class in a star-import,
 *  and this importer also can't find inner types from superclasses/interfaces.
 */
public class TypeResolver {
	private final ImportList imports;
	
	/**
	 * Creates a new TypeResolver that can be used to resolve types in a source file with the given package and import statements.
	 */
	public TypeResolver(ImportList importList) {
		this.imports = importList;
	}
	
	public boolean typeMatches(LombokNode<?, ?, ?> context, String fqn, String typeRef) {
		return typeRefToFullyQualifiedName(context, TypeLibrary.createLibraryForSingleType(fqn), typeRef) != null;
	}
	
	public String typeRefToFullyQualifiedName(LombokNode<?, ?, ?> context, TypeLibrary library, String typeRef) {
		// When asking if 'Foo' could possibly be referring to 'bar.Baz', the answer is obviously no.
		List<String> qualifieds = library.toQualifieds(typeRef);
		if (qualifieds == null || qualifieds.isEmpty()) return null;
		
		// When asking if 'lombok.Getter' could possibly be referring to 'lombok.Getter', the answer is obviously yes.
		if (qualifieds.contains(typeRef)) return LombokInternalAliasing.processAliases(typeRef);
		
		// Types defined on the containing type, or on any of its parents in the source file, take precedence over any imports
		String nestedTypeFqn = new NestedTypeFinder(typeRef).findNestedType(context);
		if (nestedTypeFqn != null) {
			// we found a nestedType - check edge case where nestedType is in type library
			qualifieds = library.toQualifieds(nestedTypeFqn);
			return qualifieds == null || !qualifieds.contains(nestedTypeFqn) ? null : nestedTypeFqn;
		}

		// When asking if 'Getter' could possibly be referring to 'lombok.Getter' if 'import lombok.Getter;' is in the source file, the answer is yes.
		int firstDot = typeRef.indexOf('.');
		if (firstDot == -1) firstDot = typeRef.length();
		String firstTypeRef = typeRef.substring(0, firstDot);
		String fromExplicitImport = imports.getFullyQualifiedNameForSimpleNameNoAliasing(firstTypeRef);
		if (fromExplicitImport != null) {
			String fqn = fromExplicitImport + typeRef.substring(firstDot);
			if (qualifieds.contains(fqn)) return LombokInternalAliasing.processAliases(fqn);
			// ... and if 'import foobar.Getter;' is in the source file, the answer is no.
			return null;
		}
		
		// When asking if 'Getter' could possibly be referring to 'lombok.Getter' and 'import lombok.*; / package lombok;' isn't in the source file. the answer is no.
		for (String qualified : qualifieds) {
			String pkgName = qualified.substring(0, qualified.length() - typeRef.length() - 1);
			if (!imports.hasStarImport(pkgName)) continue;
			
			// Now the hard part: Given that there is a star import, 'Getter' most likely refers to 'lombok.Getter', but type shadowing may occur in which case it doesn't.
			LombokNode<?, ?, ?> n = context;
			
			mainLoop:
			while (n != null) {
				if (n.getKind() == Kind.TYPE && firstTypeRef.equals(n.getName())) {
					// Our own class or one of our outer classes is named 'typeRef' so that's what 'typeRef' is referring to, not one of our type library classes.
					return null;
				}
				
				if (n.getKind() == Kind.STATEMENT || n.getKind() == Kind.LOCAL) {
					LombokNode<?, ?, ?> newN = n.directUp();
					if (newN == null) break mainLoop;
					
					if (newN.getKind() == Kind.STATEMENT || newN.getKind() == Kind.INITIALIZER || newN.getKind() == Kind.METHOD) {
						for (LombokNode<?, ?, ?> child : newN.down()) {
							// We found a method local with the same name above our code. That's the one 'typeRef' is referring to, not
							// anything in the type library we're trying to find, so, no matches.
							if (child.getKind() == Kind.TYPE && firstTypeRef.equals(child.getName())) return null;
							if (child == n) break;
						}
					}
					n = newN;
					continue mainLoop;
				}
				
				// don't need to check for inner class shadowing, we already do that in NestedTypeFinder
				
				n = n.directUp();
			}
			
			// If no shadowing thing has been found, the star import 'wins', so, return that.
			return LombokInternalAliasing.processAliases(qualified);
		}
		
		// No star import matches either.
		return null;
	}

	/**
	 * Traverse up the containing types until we find a match, or hit the package. At each level,
	 * we check for a type with matching name (including traversing into child types if typeRef is
	 * not a simple name).
	 */
	private static class NestedTypeFinder {

		private final String typeRef;
		private final List<String> typeRefElements;

		public NestedTypeFinder(String typeRef) {
			this.typeRef = typeRef;
			this.typeRefElements = Arrays.asList(typeRef.split("\\.", -1));
		}

		/** Finds a matching nestedType and returns its FQN, or {@code null} if no match found. */
		public String findNestedType(LombokNode<?, ?, ?> context) {
			LombokNode<?, ?, ?> nearestType = traverseUpToNearestType(context);
			if (nearestType == null) {
				return null;
			}

			boolean found = findTypeRef(nearestType, 0);
			if (found) {
				// return FQN
				return getFoundFqn(nearestType);
			}

			return findNestedType(nearestType.up());
		}

		/** Traverse up to the nearest type or package (including {@code node} if it is a type). */
		private LombokNode<?, ?, ?> traverseUpToNearestType(LombokNode<?, ?, ?> node) {
			if (node == null) {
				return null; // parent is null once we hit the package
			}
			if (node.getKind() == Kind.COMPILATION_UNIT || node.getKind() == Kind.TYPE) {
				return node;
			}
			return traverseUpToNearestType(node.up());
		}

		/** Check whether {@code typeRef[nameIndex]} exists as a child of {@code typeNode}. */
		private boolean findTypeRef(LombokNode<?, ?, ?> typeNode, int nameIndex) {
			for (LombokNode<?, ?, ?> child : typeNode.down()) {
				if (child.getKind() == Kind.TYPE) {
					// check if this node matches the first element
					if (child.getName().equals(typeRefElements.get(nameIndex))) {
						if (nameIndex == typeRefElements.size() - 1) {
							// we've found a match as we've matched all elements of typeRef
							return true;
						}
						// otherwise, check match of remaining typeRef elements
						boolean found = findTypeRef(child, nameIndex + 1);
						if (found) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private String getFoundFqn(LombokNode<?, ?, ?> typeNode) {
			List<String> elements = new ArrayList<String>();
			while (typeNode.getKind() != Kind.COMPILATION_UNIT) {
				elements.add(typeNode.getName());
				typeNode = traverseUpToNearestType(typeNode.up());
			}

			String pkg = typeNode.getPackageDeclaration();
			StringBuilder fqn;
			if (pkg == null) { // pkg can be null e.g. if top-level type is in default package
				fqn = new StringBuilder(elements.size() * 10);
			} else {
				fqn = new StringBuilder(pkg.length() + elements.size() * 10);
				fqn.append(pkg).append('.');
			}
			for (int i = elements.size() - 1; i >= 0; i--) {
				fqn.append(elements.get(i)).append('.');
			}
			fqn.append(typeRef);
			return fqn.toString();
		}
	}
}
