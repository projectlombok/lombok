/*
 * Copyright (C) 2015 The Project Lombok Authors.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GuavaTypeMap {
	private static final Map<String, String> TYPE_TO_GUAVA_TYPE; static {
		Map<String, String> m = new HashMap<String, String>();
		
		m.put("java.util.NavigableSet", "ImmutableSortedSet");
		m.put("java.util.NavigableMap", "ImmutableSortedMap");
		m.put("java.util.SortedSet", "ImmutableSortedSet");
		m.put("java.util.SortedMap", "ImmutableSortedMap");
		m.put("java.util.Set", "ImmutableSet");
		m.put("java.util.Map", "ImmutableMap");
		m.put("java.util.Collection", "ImmutableList");
		m.put("java.util.List", "ImmutableList");
		
		m.put("com.google.common.collect.ImmutableSet", "ImmutableSet");
		m.put("com.google.common.collect.ImmutableSortedSet", "ImmutableSortedSet");
		m.put("com.google.common.collect.ImmutableMap", "ImmutableMap");
		m.put("com.google.common.collect.ImmutableBiMap", "ImmutableBiMap");
		m.put("com.google.common.collect.ImmutableSortedMap", "ImmutableSortedMap");
		m.put("com.google.common.collect.ImmutableList", "ImmutableList");
		m.put("com.google.common.collect.ImmutableCollection", "ImmutableList");
		m.put("com.google.common.collect.ImmutableTable", "ImmutableTable");
		
		TYPE_TO_GUAVA_TYPE = Collections.unmodifiableMap(m);
	}
	
	public static String getGuavaTypeName(String fqn) {
		String target = TYPE_TO_GUAVA_TYPE.get(fqn);
		return target != null ? target : "ImmutableList";
	}
	
	private GuavaTypeMap() {}
}
