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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Container for static utility methods useful for some of the standard lombok transformations, regardless of
 * target platform (e.g. useful for both javac and Eclipse lombok implementations).
 */
public class TransformationsUtil {
	private TransformationsUtil() {
		//Prevent instantiation
	}
	
	private static final List<String> KNOWN_BOOLEAN_PREFIXES = Collections.unmodifiableList(Arrays.asList(
			"is", "has", "get"
			));
	
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
	 * Note that for boolean fields, if the field starts with 'has', 'get', or 'is', and the character after that is
	 * <b>not</b> a lowercase character, the field name is returned without changing any character's case and without
	 * any prefix.
	 * 
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide <code>false</code>.
	 */
	public static String toGetterName(CharSequence fieldName, boolean isBoolean) {
		final String prefix = isBoolean ? "is" : "get";
		
		if (fieldName.length() == 0) return prefix;
		
		for (String knownBooleanPrefix : KNOWN_BOOLEAN_PREFIXES) {
			if (!fieldName.toString().startsWith(knownBooleanPrefix)) continue;
			if (fieldName.length() > knownBooleanPrefix.length() && 
					!Character.isLowerCase(fieldName.charAt(knownBooleanPrefix.length()))) {
				//The field is called something like 'isFoo' or 'hasFoo' or 'getFoo', so we shouldn't
				//prefix with 'is' but instead just use the field name as is. The isLowerCase check is so we don't turn
				//hashCodeGenerated, which so happens to start with 'has', into hasHCodeGenerated instead of isHashCodeGenerated.
				return fieldName.toString();
			}
		}
		
		return buildName(prefix, fieldName.toString());
	}
	
	public static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile(
			"^(boolean|byte|short|int|long|float|double|char)$");
	
	public static final Pattern NON_NULL_PATTERN = Pattern.compile("^nonnull$", Pattern.CASE_INSENSITIVE);
	public static final Pattern NULLABLE_PATTERN = Pattern.compile("^nullable$", Pattern.CASE_INSENSITIVE);
	
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
		if (suffix.length() == 0) return prefix;
		
		char first = suffix.charAt(0);
		if (Character.isLowerCase(first)) {
			boolean useUpperCase = suffix.length() > 2 &&
				(Character.isTitleCase(suffix.charAt(1)) || Character.isUpperCase(suffix.charAt(1)));
			suffix = String.format("%s%s",
					useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first),
					suffix.subSequence(1, suffix.length()));
		}
		return String.format("%s%s", prefix, suffix);
	}
	
	public static List<String> toAllGetterNames(CharSequence fieldName, boolean isBoolean) {
		if (!isBoolean) return Collections.singletonList(toGetterName(fieldName, false));
		
		List<String> baseNames = new ArrayList<String>();
		baseNames.add(fieldName.toString());
		for (String knownBooleanPrefix : KNOWN_BOOLEAN_PREFIXES) {
			if (!fieldName.toString().startsWith(knownBooleanPrefix)) continue;
			if (fieldName.length() > knownBooleanPrefix.length() && 
					!Character.isLowerCase(fieldName.charAt(knownBooleanPrefix.length()))) {
				//The field is called something like 'isFoo' or 'hasFoo' or 'getFoo', so the practical fieldname
				//could also be 'foo'.
				baseNames.add(fieldName.toString().substring(knownBooleanPrefix.length()));
				//prefix with 'is' but instead just use the field name as is. The isLowerCase check is so we don't turn
				//hashCodeGenerated, which so happens to start with 'has', into hasHCodeGenerated instead of isHashCodeGenerated.
			}
		}
		
		Set<String> names = new HashSet<String>();
		for (String baseName : baseNames) {
			if (baseName.length() > 0 && Character.isLowerCase(baseName.charAt(0))) {
				baseName = Character.toTitleCase(baseName.charAt(0)) + baseName.substring(1);
			}
			
			for (String prefix : KNOWN_BOOLEAN_PREFIXES) {
				names.add(prefix + baseName);
			}
		}
		
		return new ArrayList<String>(names);
	}
}
