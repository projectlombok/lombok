/*
 * Copyright (C) 2009-2015 The Project Lombok Authors.
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

import java.util.HashMap;
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
	private final Map<String, String> unqualifiedToQualifiedMap;
	private final String unqualified, qualified;
	private boolean locked;
	
	public TypeLibrary() {
		unqualifiedToQualifiedMap = new HashMap<String, String>();
		unqualified = null;
		qualified = null;
	}
	
	public TypeLibrary(TypeLibrary parent) {
		unqualifiedToQualifiedMap = new HashMap<String, String>();
		unqualified = null;
		qualified = null;
	}
	
	public void lock() {
		this.locked = true;
	}
	
	private TypeLibrary(String fqnSingleton) {
		if (fqnSingleton.indexOf("$") != -1) {
			unqualifiedToQualifiedMap = new HashMap<String, String>();
			unqualified = null;
			qualified = null;
			addType(fqnSingleton);
		} else {
			unqualifiedToQualifiedMap = null;
			qualified = fqnSingleton;
			int idx = fqnSingleton.lastIndexOf('.');
			if (idx == -1) {
				unqualified = fqnSingleton;
			} else {
				unqualified = fqnSingleton.substring(idx + 1);
			}
		}
		locked = true;
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
		String dotBased = fullyQualifiedTypeName.replace("$", ".");
		
		if (locked) throw new IllegalStateException("locked");
		int idx = fullyQualifiedTypeName.lastIndexOf('.');
		if (idx == -1) throw new IllegalArgumentException(
				"Only fully qualified types are allowed (and stuff in the default package is not palatable to us either!)");
		String unqualified = fullyQualifiedTypeName.substring(idx + 1);
		if (unqualifiedToQualifiedMap == null) throw new IllegalStateException("SingleType library");
		
		unqualifiedToQualifiedMap.put(unqualified.replace("$", "."), dotBased);
		unqualifiedToQualifiedMap.put(unqualified, dotBased);
		unqualifiedToQualifiedMap.put(fullyQualifiedTypeName, dotBased);
		unqualifiedToQualifiedMap.put(dotBased, dotBased);
		for (Map.Entry<String, String> e : LombokInternalAliasing.ALIASES.entrySet()) {
			if (fullyQualifiedTypeName.equals(e.getValue())) unqualifiedToQualifiedMap.put(e.getKey(), dotBased);
		}
		
		int idx2 = fullyQualifiedTypeName.indexOf('$', idx + 1);
		while (idx2 != -1) {
			String unq = fullyQualifiedTypeName.substring(idx2 + 1);
			unqualifiedToQualifiedMap.put(unq.replace("$", "."), dotBased);
			unqualifiedToQualifiedMap.put(unq, dotBased);
			idx2 = fullyQualifiedTypeName.indexOf('$', idx2 + 1);
		}
	}
	
	/**
	 * Translates an unqualified name such as 'String' to 'java.lang.String', _if_ you added 'java.lang.String' to the library via the {@code addType} method.
	 * Also returns the input if it is equal to a fully qualified name added to this type library.
	 * 
	 * Returns null if it does not match any type in this type library.
	 */
	public String toQualified(String typeReference) {
		if (unqualifiedToQualifiedMap == null) {
			if (typeReference.equals(unqualified) || typeReference.equals(qualified)) return qualified;
			for (Map.Entry<String, String> e : LombokInternalAliasing.ALIASES.entrySet()) {
				if (e.getKey().equals(typeReference)) return e.getValue();
			}
			return null;
		}
		return unqualifiedToQualifiedMap.get(typeReference);
	}
}
