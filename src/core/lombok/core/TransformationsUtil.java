/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

/**
 * Container for static utility methods useful for some of the standard lombok transformations, regardless of
 * target platform (e.g. useful for both javac and Eclipse lombok implementations).
 */
public class TransformationsUtil {
	private TransformationsUtil() {
		//Prevent instantiation
	}
	
	@SuppressWarnings({"all", "unchecked", "deprecation"})
	public static final List<Class<? extends java.lang.annotation.Annotation>> INVALID_ON_BUILDERS = Collections.unmodifiableList(
			Arrays.<Class<? extends java.lang.annotation.Annotation>>asList(
			Getter.class, Setter.class, Wither.class, ToString.class, EqualsAndHashCode.class, 
			RequiredArgsConstructor.class, AllArgsConstructor.class, NoArgsConstructor.class, 
			Data.class, Value.class, lombok.experimental.Value.class, FieldDefaults.class));
	
	/**
	 * Given the name of a field, return the 'base name' of that field. For example, {@code fFoobar} becomes {@code foobar} if {@code f} is in the prefix list.
	 * For prefixes that end in a letter character, the next character must be a non-lowercase character (i.e. {@code hashCode} is not {@code ashCode} even if
	 * {@code h} is in the prefix list, but {@code hAshcode} would become {@code ashCode}). The first prefix that matches is used. If the prefix list is empty,
	 * or the empty string is in the prefix list and no prefix before it matches, the fieldName will be returned verbatim.
	 * 
	 * If no prefix matches and the empty string is not in the prefix list and the prefix list is not empty, {@code null} is returned.
	 * 
	 * @param fieldName The full name of a field.
	 * @param prefixes A list of prefixes, usually provided by the {@code Accessors} settings annotation, listing field prefixes.
	 * @return The base name of the field.
	 */
	private static CharSequence removePrefix(CharSequence fieldName, String[] prefixes) {
		if (prefixes == null || prefixes.length == 0) return fieldName;
		
		outer:
		for (String prefix : prefixes) {
			if (prefix.length() == 0) return fieldName;
			if (fieldName.length() <= prefix.length()) continue outer;
			for (int i = 0; i < prefix.length(); i++) {
				if (fieldName.charAt(i) != prefix.charAt(i)) continue outer;
			}
			char followupChar = fieldName.charAt(prefix.length());
			// if prefix is a letter then follow up letter needs to not be lowercase, i.e. 'foo' is not a match
			// as field named 'oo' with prefix 'f', but 'fOo' would be.
			if (Character.isLetter(prefix.charAt(prefix.length() - 1)) &&
					Character.isLowerCase(followupChar)) continue outer;
			return "" + Character.toLowerCase(followupChar) + fieldName.subSequence(prefix.length() + 1, fieldName.length());
		}
		
		return null;
	}
	
	/* NB: 'notnull' is not part of the pattern because there are lots of @NotNull annotations out there that are crappily named and actually mean
	        something else, such as 'this field must not be null _when saved to the db_ but its perfectly okay to start out as such, and a no-args
	        constructor and the implied starts-out-as-null state that goes with it is in fact mandatory' which happens with javax.validation.constraints.NotNull.
	        Various problems with spring have also been reported. See issue #287, issue #271, and issue #43. */
	
	/** Matches the simple part of any annotation that lombok considers as indicative of NonNull status. */
	public static final Pattern NON_NULL_PATTERN = Pattern.compile("^(?:nonnull)$", Pattern.CASE_INSENSITIVE);
	
	/** Matches the simple part of any annotation that lombok considers as indicative of Nullable status. */
	public static final Pattern NULLABLE_PATTERN = Pattern.compile("^(?:nullable|checkfornull)$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Generates a getter name from a given field name.
	 * 
	 * Strategy:
	 * <ul>
	 * <li>Reduce the field's name to its base name by stripping off any prefix (from {@code Accessors}). If the field name does not fit
	 * the prefix list, this method immediately returns {@code null}.</li>
	 * <li>If {@code Accessors} has {@code fluent=true}, then return the basename.</li>
	 * <li>Pick a prefix. 'get' normally, but 'is' if {@code isBoolean} is true.</li>
	 * <li>Only if {@code isBoolean} is true: Check if the field starts with {@code is} followed by a non-lowercase character. If so, return the field name verbatim.</li> 
	 * <li>Check if the first character of the field is lowercase. If so, check if the second character
	 * exists and is title or upper case. If so, uppercase the first character. If not, titlecase the first character.</li>
	 * <li>Return the prefix plus the possibly title/uppercased first character, and the rest of the field name.</li>
	 * </ul>
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type {@code java.lang.Boolean}, you should provide {@code false}.
	 * @return The getter name for this field, or {@code null} if this field does not fit expected patterns and therefore cannot be turned into a getter name.
	 */
	public static String toGetterName(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(accessors, fieldName, isBoolean, "is", "get", true);
	}
	
	/**
	 * Generates a setter name from a given field name.
	 * 
	 * Strategy:
	 * <ul>
	 * <li>Reduce the field's name to its base name by stripping off any prefix (from {@code Accessors}). If the field name does not fit
	 * the prefix list, this method immediately returns {@code null}.</li>
	 * <li>If {@code Accessors} has {@code fluent=true}, then return the basename.</li>
	 * <li>Only if {@code isBoolean} is true: Check if the field starts with {@code is} followed by a non-lowercase character.
	 * If so, replace {@code is} with {@code set} and return that.</li> 
	 * <li>Check if the first character of the field is lowercase. If so, check if the second character
	 * exists and is title or upper case. If so, uppercase the first character. If not, titlecase the first character.</li>
	 * <li>Return {@code "set"} plus the possibly title/uppercased first character, and the rest of the field name.</li>
	 * </ul>
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type {@code java.lang.Boolean}, you should provide {@code false}.
	 * @return The setter name for this field, or {@code null} if this field does not fit expected patterns and therefore cannot be turned into a getter name.
	 */
	public static String toSetterName(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(accessors, fieldName, isBoolean, "set", "set", true);
	}
	
	/**
	 * Generates a wither name from a given field name.
	 * 
	 * Strategy:
	 * <ul>
	 * <li>Reduce the field's name to its base name by stripping off any prefix (from {@code Accessors}). If the field name does not fit
	 * the prefix list, this method immediately returns {@code null}.</li>
	 * <li>Only if {@code isBoolean} is true: Check if the field starts with {@code is} followed by a non-lowercase character.
	 * If so, replace {@code is} with {@code with} and return that.</li> 
	 * <li>Check if the first character of the field is lowercase. If so, check if the second character
	 * exists and is title or upper case. If so, uppercase the first character. If not, titlecase the first character.</li>
	 * <li>Return {@code "with"} plus the possibly title/uppercased first character, and the rest of the field name.</li>
	 * </ul>
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type {@code java.lang.Boolean}, you should provide {@code false}.
	 * @return The wither name for this field, or {@code null} if this field does not fit expected patterns and therefore cannot be turned into a getter name.
	 */
	public static String toWitherName(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(accessors, fieldName, isBoolean, "with", "with", false);
	}
	
	private static String toAccessorName(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean,
			String booleanPrefix, String normalPrefix, boolean adhereToFluent) {
		
		if (fieldName.length() == 0) return null;
		
		Accessors ac = accessors == null ? null : accessors.getInstance();
		fieldName = removePrefix(fieldName, ac == null ? new String[0] : ac.prefix());
		if (fieldName == null) return null;
		
		String fName = fieldName.toString();
		if (adhereToFluent && ac != null && ac.fluent()) return fName;
		
		if (isBoolean && fName.startsWith("is") && fieldName.length() > 2 && !Character.isLowerCase(fieldName.charAt(2))) {
			// The field is for example named 'isRunning'.
			return booleanPrefix + fName.substring(2);
		}
		
		return buildName(isBoolean ? booleanPrefix : normalPrefix, fName);
	}
	
	/**
	 * Returns all names of methods that would represent the getter for a field with the provided name.
	 * 
	 * For example if {@code isBoolean} is true, then a field named {@code isRunning} would produce:<br />
	 * {@code [isRunning, getRunning, isIsRunning, getIsRunning]}
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide {@code false}.
	 */
	public static List<String> toAllGetterNames(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(accessors, fieldName, isBoolean, "is", "get", true);
	}
	
	/**
	 * Returns all names of methods that would represent the setter for a field with the provided name.
	 * 
	 * For example if {@code isBoolean} is true, then a field named {@code isRunning} would produce:<br />
	 * {@code [setRunning, setIsRunning]}
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide {@code false}.
	 */
	public static List<String> toAllSetterNames(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(accessors, fieldName, isBoolean, "set", "set", true);
	}
	
	/**
	 * Returns all names of methods that would represent the wither for a field with the provided name.
	 * 
	 * For example if {@code isBoolean} is true, then a field named {@code isRunning} would produce:<br />
	 * {@code [withRunning, withIsRunning]}
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide {@code false}.
	 */
	public static List<String> toAllWitherNames(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(accessors, fieldName, isBoolean, "with", "with", false);
	}
	
	private static List<String> toAllAccessorNames(AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean,
			String booleanPrefix, String normalPrefix, boolean adhereToFluent) {
		
		if (!isBoolean) {
			String accessorName = toAccessorName(accessors, fieldName, false, booleanPrefix, normalPrefix, adhereToFluent);
			return (accessorName == null) ? Collections.<String>emptyList() : Collections.singletonList(accessorName);
		}
		
		Accessors acc = accessors.getInstance();
		fieldName = removePrefix(fieldName, acc.prefix());
		if (fieldName == null) return Collections.emptyList();
		
		List<String> baseNames = toBaseNames(fieldName, isBoolean, acc.fluent());
		
		Set<String> names = new HashSet<String>();
		for (String baseName : baseNames) {
			if (adhereToFluent && acc.fluent()) {
				names.add(baseName);
			} else {
				names.add(buildName(normalPrefix, baseName));
				if (!normalPrefix.equals(booleanPrefix)) names.add(buildName(booleanPrefix, baseName));
			}
		}
		
		return new ArrayList<String>(names);
		
	}
	
	private static List<String> toBaseNames(CharSequence fieldName, boolean isBoolean, boolean fluent) {
		List<String> baseNames = new ArrayList<String>();
		baseNames.add(fieldName.toString());
		
		// isPrefix = field is called something like 'isRunning', so 'running' could also be the fieldname.
		String fName = fieldName.toString();
		if (fName.startsWith("is") && fName.length() > 2 && !Character.isLowerCase(fName.charAt(2))) {
			String baseName = fName.substring(2);
			if (fluent) {
				baseNames.add("" + Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1));
			} else {
				baseNames.add(baseName);
			}
		}
		
		return baseNames;
	}
	
	/**
	 * @param prefix Something like {@code get} or {@code set} or {@code is}.
	 * @param suffix Something like {@code running}.
	 * @return prefix + smartly title-cased suffix. For example, {@code setRunning}.
	 */
	private static String buildName(String prefix, String suffix) {
		if (suffix.length() == 0) return prefix;
		if (prefix.length() == 0) return suffix;
		
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
}
