/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
package lombok.delombok;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FormatPreferences {
	private final String indent;
	private final boolean filledEmpties;
	static final Map<String, String> KEYS;
	
	static {
		Map<String, String> keys = new LinkedHashMap<String, String>();
		keys.put("indent", "The indent to use. 'tab' can be used to represent 1 tab. A number means that many spaces. Default: 'tab'");
		keys.put("emptyLines", "Either 'indent' or 'blank'. indent means: Indent an empty line to the right level. Default: 'blank'");
		KEYS = Collections.unmodifiableMap(keys);
	}
	
	public FormatPreferences(Map<String, String> preferences, String indent, boolean filledEmpties) {
		if (preferences == null) preferences = Collections.emptyMap();
		
		String indent_ = preferences.get("indent");
		if (indent_ != null && !"scan".equalsIgnoreCase(indent_)) {
			try {
				int id = Integer.parseInt(indent_);
				if (id > 0 && id < 32) {
					char[] c = new char[id];
					Arrays.fill(c, ' ');
					indent_ = new String(c);
				}
			} catch (NumberFormatException ignore) {}
			indent = indent_.replace("\\t", "\t").replace("tab", "\t");
		}
		String empties_ = preferences.get("emptyLines");
		if ("indent".equalsIgnoreCase(empties_)) filledEmpties = true;
		else if ("blank".equalsIgnoreCase(empties_)) filledEmpties = false;
		else if (empties_ != null && !"scan".equalsIgnoreCase(empties_)) {
			throw new IllegalArgumentException("Legal values for 'emptyLines' is scan, indent, or blank.");
		}
		this.indent = indent;
		this.filledEmpties = filledEmpties;
	}
	public static Map<String, String> getKeysAndDescriptions() {
		return KEYS;
	}
	
	/** If true, empty lines should still be appropriately indented. If false, empty lines should be completely blank. */
	public boolean fillEmpties() {
		return filledEmpties;
	}
	
	public String indent() {
		return indent;
	}
}
