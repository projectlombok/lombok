/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Library of types, which can be used to look up potential matching types.
 * 
 * For example, if you put 'foo.Spork' and 'bar.Spork' into the library, and then ask for
 * all compatible types given the type 'Spork', you'll get both of them, but you'll only
 * get the one if you ask for compatible types given 'foo.Spork'.
 * 
 * Useful to 'guess' if a given annotation AST node matches an annotation handler's target annotation.
 */
public class TypeLibrary {
	private final Map<String, Set<String>> simpleToQualifiedMap = new HashMap<String, Set<String>>();
	
	/**
	 * Add a type to the library.
	 * 
	 * @param fullyQualifiedTypeName the FQN type name, such as 'java.lang.String'.
	 */
	public void addType(String fullyQualifiedTypeName) {
		int idx = fullyQualifiedTypeName.lastIndexOf('.');
		if ( idx == -1 ) throw new IllegalArgumentException(
				"Only fully qualified types are allowed (and stuff in the default package is not palatable to us either!)");
		
		final String simpleName = fullyQualifiedTypeName.substring(idx +1);
		final String packageName = fullyQualifiedTypeName.substring(0, idx);
		
		if ( simpleToQualifiedMap.put(fullyQualifiedTypeName, Collections.singleton(fullyQualifiedTypeName)) != null ) return;
		
		addToMap(simpleName, fullyQualifiedTypeName);
		addToMap(packageName + ".*", fullyQualifiedTypeName);
	}
	
	private TypeLibrary addToMap(String keyName, String fullyQualifiedTypeName) {
		Set<String> existing = simpleToQualifiedMap.get(keyName);
		Set<String> set = (existing == null) ? new HashSet<String>() : new HashSet<String>(existing);
		set.add(fullyQualifiedTypeName);
		simpleToQualifiedMap.put(keyName, Collections.unmodifiableSet(set));
		return this;
	}
	
	/**
	 * Returns all items in the type library that may be a match to the provided type.
	 * 
	 * @param typeReference something like 'String' or even 'java.lang.String'.
	 */
	public Collection<String> findCompatible(String typeReference) {
		Set<String> result = simpleToQualifiedMap.get(typeReference);
		return result == null ? Collections.<String>emptySet() : result;
	}
}
