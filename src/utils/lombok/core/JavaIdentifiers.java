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
package lombok.core;

/**
 * Utility functions for validating potential java verifiers.
 */
public class JavaIdentifiers {
	private JavaIdentifiers() {}
	
	private static final LombokImmutableList<String> KEYWORDS = LombokImmutableList.of(
			"public", "private", "protected",
			"default", "switch", "case",
			"for", "do", "goto", "const", "strictfp", "while", "if", "else",
			"byte", "short", "int", "long", "float", "double", "void", "boolean", "char", 
			"null", "false", "true",
			"continue", "break", "return", "instanceof",
			"synchronized", "volatile", "transient", "final", "static",
			"interface", "class", "extends", "implements", "throws",
			"throw", "catch", "try", "finally", "abstract", "assert",
			"enum", "import", "package", "native", "new", "super", "this");
	
	public static boolean isValidJavaIdentifier(String identifier) {
		if (identifier == null) return false;
		if (identifier.isEmpty()) return false;
		
		if (!Character.isJavaIdentifierStart(identifier.charAt(0))) return false;
		for (int i = 1; i < identifier.length(); i++) {
			if (!Character.isJavaIdentifierPart(identifier.charAt(i))) return false;
		}
		
		return !isKeyword(identifier);
	}
	
	public static boolean isKeyword(String keyword) {
		return KEYWORDS.contains(keyword);
	}
}
