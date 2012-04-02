/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library of types, which can be used to look up potential matching types.
 * 
 * For example, if you put {@code foo.Spork} and {@code bar.Spork} into the library, and then ask for
 * all compatible types given the type {@code Spork}, you'll get both of them, but you'll only
 * get the one if you ask for compatible types given {@code foo.Spork}.
 * <p>
 * When adding {@code foo.Spork}, that FQN (Fully Qualified Name) will be returned as an option for any of these queries:
 * <ul><li>foo.Spork</li><li>Spork</li><li>foo.*</li></ul>
 */
public class TypeLibrary {
	private final Map<String, List<String>> keyToFqnMap;
	private final String singletonValue;
	private final List<String> singletonKeys;
	
	public TypeLibrary() {
		keyToFqnMap = new HashMap<String, List<String>>();
		singletonKeys = null;
		singletonValue = null;
	}
	
	private TypeLibrary(String fqnSingleton) {
		keyToFqnMap = null;
		singletonValue = fqnSingleton;
		int idx = fqnSingleton.lastIndexOf('.');
		if (idx == -1) {
			singletonKeys = Collections.singletonList(fqnSingleton);
		} else {
			singletonKeys = Arrays.asList(fqnSingleton, fqnSingleton.substring(idx + 1), fqnSingleton.substring(0, idx) + ".*");
		}
	}
	
	public static TypeLibrary createLibraryForSingleType(String fqnSingleton) {
		return new TypeLibrary(fqnSingleton);
	}
	
	/**
	 * Add a type to the library.
	 * 
	 * @param fullyQualifiedTypeName the FQN type name, such as 'java.lang.String'.
	 */
	public void addType(String fullyQualifiedTypeName) {
		if (keyToFqnMap == null) throw new IllegalStateException("SingleType library");
		int idx = fullyQualifiedTypeName.lastIndexOf('.');
		if (idx == -1) throw new IllegalArgumentException(
				"Only fully qualified types are allowed (and stuff in the default package is not palatable to us either!)");
		
		fullyQualifiedTypeName = fullyQualifiedTypeName.replace("$", ".");
		final String simpleName = fullyQualifiedTypeName.substring(idx +1);
		final String packageName = fullyQualifiedTypeName.substring(0, idx);
		
		if (keyToFqnMap.put(fullyQualifiedTypeName, Collections.singletonList(fullyQualifiedTypeName)) != null) return;
		
		addToMap(simpleName, fullyQualifiedTypeName);
		addToMap(packageName + ".*", fullyQualifiedTypeName);
	}
	
	private TypeLibrary addToMap(String keyName, String fullyQualifiedTypeName) {
		List<String> list = keyToFqnMap.get(keyName);
		if (list == null) {
			list = new ArrayList<String>();
			keyToFqnMap.put(keyName, list);
		}
		
		list.add(fullyQualifiedTypeName);
		return this;
	}
	
	/**
	 * Returns all items in the type library that may be a match to the provided type.
	 * 
	 * @param typeReference something like 'String', 'java.lang.String', or 'java.lang.*'.
	 * @return A list of Fully Qualified Names for all types in the library that fit the reference.
	 */
	public Collection<String> findCompatible(String typeReference) {
		if (singletonKeys != null) return singletonKeys.contains(typeReference) ? Collections.singletonList(singletonValue) : Collections.<String>emptyList();
		
		List<String> result = keyToFqnMap.get(typeReference);
		return result == null ? Collections.<String>emptyList() : Collections.unmodifiableList(result);
	}
}
