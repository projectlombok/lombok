/*
 * Copyright (C) 2013-2015 The Project Lombok Authors.
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
	private final Boolean filledEmpties;
	private final boolean generateFinalParams;
	private final boolean generateConstructorProperties;
	private final boolean generateSuppressWarnings, danceAroundIdeChecks, generateDelombokComment, javaLangAsFqn;
	final Map<String, String> rawMap;
	private final boolean generateGenerated;
	
	static final Map<String, String> KEYS;
	
	static {
		Map<String, String> keys = new LinkedHashMap<String, String>();
		keys.put("indent", "The indent to use. 'tab' can be used to represent 1 tab. A number means that many spaces. Default: 'tab'");
		keys.put("emptyLines", "Either 'indent' or 'blank'. indent means: Indent an empty line to the right level. Default: 'blank'");
		keys.put("finalParams", "Either 'generate' or 'skip'. generate means: All lombok-generated methods set all parameters to final. Default: 'generate'");
		keys.put("constructorProperties", "Either 'generate' or 'skip'. generate means: All lombok-generated constructors with 1 or more arguments get an @ConstructorProperties annotation. Default: 'generate'");
		keys.put("suppressWarnings", "Either 'generate' or 'skip'. generate means: All lombok-generated methods, types, and fields get a @SuppressWarnings annotation. Default: 'generate'");
		keys.put("generated", "Either 'generate' or 'skip'. generate means: All lombok-generated methods, types, and fields get a @javax.annotation.Generated(\"lombok\") annotation. Default: 'generate'");
		keys.put("danceAroundIdeChecks", "Either 'generate' or 'skip'. generate means: Lombok will intentionally obfuscate some generated code to avoid IDE warnings. Default: 'generate'");
		keys.put("generateDelombokComment", "Either 'generate' or 'skip'. generate means: Any file modified by delombok will have a comment stating this at the top. Default: 'generate'");
		keys.put("javaLangAsFQN", "Either 'generate' or 'skip'. generate means: Any generated reference to java.lang classes are prefixed with `java.lang.`. Default: 'generate'");
		KEYS = Collections.unmodifiableMap(keys);
	}
	
	public FormatPreferences(Map<String, String> preferences) {
		this(preferences, null, null);
	}
	
	public FormatPreferences(Map<String, String> preferences, String indent, Boolean filledEmpties) {
		this.rawMap = preferences;
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
		String empties_ = preferences.get("emptyLines".toLowerCase());
		if ("indent".equalsIgnoreCase(empties_)) filledEmpties = true;
		else if ("blank".equalsIgnoreCase(empties_)) filledEmpties = false;
		else if (empties_ != null && !"scan".equalsIgnoreCase(empties_)) {
			throw new IllegalArgumentException("Legal values for 'emptyLines' are 'scan', 'indent', or 'blank'.");
		}
		
		this.indent = indent;
		this.filledEmpties = filledEmpties;
		
		this.generateFinalParams = unrollBoolean(preferences, "finalParams", "generate", "skip", true);
		this.generateConstructorProperties = unrollBoolean(preferences, "constructorProperties", "generate", "skip", true);
		this.generateSuppressWarnings = unrollBoolean(preferences, "suppressWarnings", "generate", "skip", true);
		this.generateGenerated = unrollBoolean(preferences, "generated", "generate", "skip", true);
		this.danceAroundIdeChecks = unrollBoolean(preferences, "danceAroundIdeChecks", "generate", "skip", true);
		this.generateDelombokComment = unrollBoolean(preferences, "generateDelombokComment", "generate", "skip", true);
		this.javaLangAsFqn = unrollBoolean(preferences, "javaLangAsFQN", "generate", "skip", true);
	}
	
	private static boolean unrollBoolean(Map<String, String> preferences, String name, String trueStr, String falseStr, boolean defaultVal) {
		String v_ = preferences.get(name.toLowerCase());
		if (v_ == null) return defaultVal;
		if (trueStr.equalsIgnoreCase(v_)) return true;
		if (falseStr.equalsIgnoreCase(v_)) return false;
		throw new IllegalArgumentException("Legal values for '" + name + "' are '" + trueStr + "', or '" + falseStr + "'.");
	}
	
	public static Map<String, String> getKeysAndDescriptions() {
		return KEYS;
	}
	
	/** If true, empty lines should still be appropriately indented. If false, empty lines should be completely blank. */
	public boolean fillEmpties() {
		return filledEmpties == null ? false : filledEmpties;
	}
	
	public String indent() {
		return indent == null ? "\t" : indent;
	}
	
	public boolean generateSuppressWarnings() {
		return generateSuppressWarnings;
	}
	
	public boolean generateGenerated() {
		return generateGenerated;
	}
	
	public boolean generateFinalParams() {
		return generateFinalParams;
	}
	
	public boolean danceAroundIdeChecks() {
		return danceAroundIdeChecks;
	}
	
	public boolean generateDelombokComment() {
		return generateDelombokComment;
	}
	
	public boolean javaLangAsFqn() {
		return javaLangAsFqn;
	}
	
	public boolean generateConstructorProperties() {
		return generateConstructorProperties;
	}
	
	@Override public String toString() {
		return rawMap.toString();
	}
}
