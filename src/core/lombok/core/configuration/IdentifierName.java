/*
 * Copyright (C) 2019 The Project Lombok Authors.
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

import lombok.core.JavaIdentifiers;

public final class IdentifierName implements ConfigurationValueType {
	private final String name;
	
	private IdentifierName(String name) {
		this.name = name;
	}
	
	public static IdentifierName valueOf(String name) {
		if (name == null || name.trim().isEmpty()) return null;
		
		String trimmedName = name.trim();
		if (!JavaIdentifiers.isValidJavaIdentifier(trimmedName)) throw new IllegalArgumentException("Invalid identifier " + trimmedName);
		return new IdentifierName(trimmedName);
	}
	
	public static String description() {
		return "identifier-name";
	}
	
	public static String exampleValue() {
		return "<javaIdentifier>";
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof IdentifierName)) return false;
		return name.equals(((IdentifierName) obj).name);
	}
	
	@Override public int hashCode() {
		return name.hashCode();
	}
	
	@Override public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public char[] getCharArray() {
		return name.toCharArray();
	}
}
