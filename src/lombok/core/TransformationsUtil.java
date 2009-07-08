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

/**
 * Container for static utility methods useful for some of the standard lombok transformations, regardless of
 * target platform (e.g. useful for both javac and Eclipse lombok implementations).
 */
public class TransformationsUtil {
	private TransformationsUtil() {
		//Prevent instantiation
	}
	
	/**
	 * Generates a getter name from a given field name.
	 * 
	 * Strategy:
	 * 
	 * First, pick a prefix. 'get' normally, but 'is' if <code>isBoolean</code> is true.
	 * 
	 * Then, check if the first character of the field is lowercase. If so, check if the second character
	 * exists and is title or upper case. If so, uppercase the first character. If not, titlecase the first character.
	 * 
	 * return the prefix plus the possibly title/uppercased first character, and the rest of the field name.
	 * 
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide <code>false</code>.
	 */
	public static String toGetterName(CharSequence fieldName, boolean isBoolean) {
		final String prefix = isBoolean ? "is" : "get";
		
		if ( fieldName.length() == 0 ) return prefix;
		
		return buildName(prefix, fieldName.toString());
	}
	
	/**
	 * Generates a getter name from a given field name.
	 * 
	 * Strategy:
	 * 
	 * Check if the first character of the field is lowercase. If so, check if the second character
	 * exists and is title or upper case. If so, uppercase the first character. If not, titlecase the first character.
	 * 
	 * return "set" plus the possibly title/uppercased first character, and the rest of the field name.
	 * 
	 * @param fieldName the name of the field.
	 */
	public static String toSetterName(CharSequence fieldName) {
		return buildName("set", fieldName.toString());
	}
	
	private static String buildName(String prefix, String suffix) {
		if ( suffix.length() == 0 ) return prefix;
		
		char first = suffix.charAt(0);
		if ( Character.isLowerCase(first) ) {
			boolean useUpperCase = suffix.length() > 2 &&
				(Character.isTitleCase(suffix.charAt(1)) || Character.isUpperCase(suffix.charAt(1)));
			suffix = String.format("%s%s",
					useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first),
					suffix.subSequence(1, suffix.length()));
		}
		return String.format("%s%s", prefix, suffix);
	}
}
