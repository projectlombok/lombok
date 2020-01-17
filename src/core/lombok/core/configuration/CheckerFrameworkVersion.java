/*
 * Copyright (C) 2019-2020 The Project Lombok Authors.
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
package lombok.core.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CheckerFrameworkVersion implements ConfigurationValueType {
	private final int version;
	private static final int MAX_SUPPORTED = 3000;
	
	public static final String NAME__SIDE_EFFECT_FREE = "org.checkerframework.dataflow.qual.SideEffectFree";
	public static final String NAME__UNIQUE = "org.checkerframework.common.aliasing.qual.Unique";
	public static final String NAME__RETURNS_RECEIVER = "org.checkerframework.checker.builder.qual.ReturnsReceiver";
	public static final String NAME__NOT_CALLED = "org.checkerframework.checker.builder.qual.NotCalledMethods";
	public static final String NAME__CALLED = "org.checkerframework.checker.builder.qual.CalledMethods";
	
	public static final CheckerFrameworkVersion NONE = new CheckerFrameworkVersion(0);
	
	private CheckerFrameworkVersion(int v) {
		this.version = v;
	}
	
	private static final Pattern VERSION = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.\\d+)*$");
	
	public boolean generateSideEffectFree() {
		return version > 0;
	}
	
	public boolean generateUnique() {
		return version > 0;
	}
	
	public boolean generateReturnsReceiver() {
		return version > 2999;
	}
	
	public boolean generateCalledMethods() {
		return version > 2999;
	}
	
	public static CheckerFrameworkVersion valueOf(String versionString) {
		if (versionString != null) versionString = versionString.trim();
		if (versionString == null || versionString.equalsIgnoreCase("false") || versionString.equals("0")) return new CheckerFrameworkVersion(0);
		if (versionString.equalsIgnoreCase("true")) return new CheckerFrameworkVersion(MAX_SUPPORTED);
		Matcher m = VERSION.matcher(versionString);
		if (!m.matches()) throw new IllegalArgumentException("Expected 'true' or 'false' or a major/minor version, such as '2.9'");
		int major = Integer.parseInt(m.group(1));
		int minor = (m.group(2) != null && !m.group(2).isEmpty()) ? Integer.parseInt(m.group(2)) : 0;
		if (minor > 999) throw new IllegalArgumentException("Minor version must be between 0 and 999");
		int v = major * 1000 + minor;
		if (v > MAX_SUPPORTED) {
			String s = (v / 1000) + "." + (v % 1000);
			throw new IllegalArgumentException("Lombok supports at most v" + s + "; reduce the value of key 'checkerframework' to " + s);
		}
		return new CheckerFrameworkVersion(v);
	}
	
	public static String description() {
		return "checkerframework-version";
	}
	
	public static String exampleValue() {
		String s = (MAX_SUPPORTED / 1000) + "." + (MAX_SUPPORTED % 1000);
		return "major.minor (example: 2.9 - and no higher than " + s + ") or true or false";
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof CheckerFrameworkVersion)) return false;
		return version == ((CheckerFrameworkVersion) obj).version;
	}
	
	@Override public int hashCode() {
		return version;
	}
}
