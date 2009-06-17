package lombok.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.core.AST.Kind;

public class TypeResolver {
	private final TypeLibrary library;
	private Collection<String> imports;
	
	public TypeResolver(TypeLibrary library, String packageString, Collection<String> importStrings) {
		this.library = library;
		this.imports = makeImportList(packageString, importStrings);
	}
	
	private static Collection<String> makeImportList(String packageString, Collection<String> importStrings) {
		Set<String> imports = new HashSet<String>();
		if ( packageString != null ) imports.add(packageString + ".*");
		imports.addAll(importStrings == null ? Collections.<String>emptySet() : importStrings);
		return imports;
	}
	
	public Collection<String> findTypeMatches(AST<?>.Node context, String typeRef) {
		Collection<String> potentialMatches = library.findCompatible(typeRef);
		if ( potentialMatches.isEmpty() ) return Collections.emptyList();
		
		int idx = typeRef.indexOf('.');
		if ( idx > -1 ) return potentialMatches;
		String simpleName = typeRef.substring(idx+1);
		
		//If there's an import statement that explicitly imports a 'Getter' that isn't any of our potentials, return no matches.
		if ( nameConflictInImportList(simpleName, potentialMatches) ) return Collections.emptyList();
		
		//Check if any of our potentials is even imported in the first place. If not: no matches.
		potentialMatches = eliminateImpossibleMatches(potentialMatches);
		if ( potentialMatches.isEmpty() ) return Collections.emptyList();
		
		//Find a lexically accessible type of the same simple name in the same Compilation Unit. If it exists: no matches.
		AST<?>.Node n = context;
		while ( n != null ) {
			if ( n.getKind() == Kind.TYPE ) {
				String name = n.getName();
				if ( name != null && name.equals(simpleName) ) return Collections.emptyList();
			}
			n = n.up();
		}
		
		// The potential matches we found by comparing the import statements is our matching set. Return it.
		return potentialMatches;
	}
	
	private Collection<String> eliminateImpossibleMatches(Collection<String> potentialMatches) {
		Set<String> results = new HashSet<String>();
		
		for ( String importedType : imports ) {
			Collection<String> reduced = new HashSet<String>(library.findCompatible(importedType));
			reduced.retainAll(potentialMatches);
			results.addAll(reduced);
		}
		
		return results;
	}
	
	private boolean nameConflictInImportList(String simpleName, Collection<String> potentialMatches) {
		for ( String importedType : imports ) {
			if ( !toSimpleName(importedType).equals(simpleName) ) continue;
			if ( potentialMatches.contains(importedType) ) continue;
			return true;
		}
		
		return false;
	}
	
	private static String toSimpleName(String typeName) {
		int idx = typeName.lastIndexOf('.');
		return idx == -1 ? typeName : typeName.substring(idx+1);
	}
}
