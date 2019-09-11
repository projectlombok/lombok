/*
 * Copyright (C) 2013-2019 The Project Lombok Authors.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LombokInternalAliasing {
	/** Maps a package name to a space separated list of packages. If the key package is star-imported, assume all packages in the 'value' part of the MapEntry are too. */
	public static final Map<String, Collection<String>> IMPLIED_EXTRA_STAR_IMPORTS;
	public static final Map<String, String> ALIASES;
	public static final Map<String, Collection<String>> REVERSE_ALIASES;
	
	/**
	 * Provide a fully qualified name (FQN), and the canonical version of this is returned.
	 */
	public static String processAliases(String in) {
		if (in == null) return null;
		String ret = ALIASES.get(in);
		return ret == null ? in : ret;
	}
	
	static {
		Map<String, Collection<String>> m1 = new HashMap<String, Collection<String>>();
		m1.put("lombok.experimental", Collections.singleton("lombok"));
		m1.put("lombok", Collections.singleton("lombok.experimental"));
		IMPLIED_EXTRA_STAR_IMPORTS = Collections.unmodifiableMap(m1);
		
		Map<String, String> m2 = new HashMap<String, String>();
		m2.put("lombok.experimental.Value", "lombok.Value");
		m2.put("lombok.experimental.Builder", "lombok.Builder");
		m2.put("lombok.experimental.var", "lombok.var");
		m2.put("lombok.Delegate", "lombok.experimental.Delegate");
		m2.put("lombok.experimental.Wither", "lombok.With");
		ALIASES = Collections.unmodifiableMap(m2);
		
		Map<String, Collection<String>> m3 = new HashMap<String, Collection<String>>();
		for (Map.Entry<String, String> e : m2.entrySet()) {
			Collection<String> c = m3.get(e.getValue());
			if (c == null) {
				m3.put(e.getValue(), Collections.singleton(e.getKey()));
			} else if (c.size() == 1) {
				Collection<String> newC = new ArrayList<String>(2);
				newC.addAll(c);
				m3.put(e.getValue(), c);
			} else {
				c.add(e.getKey());
			}
		}
		for (Map.Entry<String, Collection<String>> e : m3.entrySet()) {
			Collection<String> c = e.getValue();
			if (c.size() > 1) e.setValue(Collections.unmodifiableList((ArrayList<String>) c));
		}
		REVERSE_ALIASES = Collections.unmodifiableMap(m3);
	}
}
