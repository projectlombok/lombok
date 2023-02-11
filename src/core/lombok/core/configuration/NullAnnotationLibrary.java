/*
 * Copyright (C) 2020-2023 The Project Lombok Authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NullAnnotationLibrary implements ConfigurationValueType {
	private final String key;
	private final String nonNullAnnotation;
	private final String nullableAnnotation;
	private final boolean typeUse;
	
	private NullAnnotationLibrary(String key, String nonNullAnnotation, String nullableAnnotation, boolean typeUse) {
		this.key = key;
		this.nonNullAnnotation = nonNullAnnotation;
		this.nullableAnnotation = nullableAnnotation;
		this.typeUse = typeUse;
	}
	
	/**
	 * Returns the fully qualified annotation name to apply to non-null elements. If {@code null} is returned, apply no annotation.
	 */
	public String getNonNullAnnotation() {
		return nonNullAnnotation;
	}
	
	/**
	 * Returns the fully qualified annotation name to apply to nullable elements. If {@code null} is returned, apply no annotation.
	 */
	public String getNullableAnnotation() {
		return nullableAnnotation;
	}
	
	/**
	 * If {@code true}, the annotation can only be used in TYPE_USE form, otherwise, prefer to annotate the parameter, not the type of the parameter (or the method, not the return type, etc).
	 */
	public boolean isTypeUse() {
		return typeUse;
	}
	
	public static final NullAnnotationLibrary NONE = new NullAnnotationLibrary("none", null, null, false);
	public static final NullAnnotationLibrary JAVAX = new NullAnnotationLibrary("javax", "javax.annotation.Nonnull", "javax.annotation.Nullable", false);
	public static final NullAnnotationLibrary JAKARTA = new NullAnnotationLibrary("jakarta", "jakarta.annotation.Nonnull", "jakarta.annotation.Nullable", false);
	public static final NullAnnotationLibrary ECLIPSE = new NullAnnotationLibrary("eclipse", "org.eclipse.jdt.annotation.NonNull", "org.eclipse.jdt.annotation.Nullable", true);
	public static final NullAnnotationLibrary JETBRAINS = new NullAnnotationLibrary("jetbrains", "org.jetbrains.annotations.NotNull", "org.jetbrains.annotations.Nullable", false);
	public static final NullAnnotationLibrary NETBEANS = new NullAnnotationLibrary("netbeans", "org.netbeans.api.annotations.common.NonNull", "org.netbeans.api.annotations.common.NullAllowed", false);
	public static final NullAnnotationLibrary ANDROIDX = new NullAnnotationLibrary("androidx", "androidx.annotation.NonNull", "androidx.annotation.Nullable", false);
	public static final NullAnnotationLibrary ANDROID_SUPPORT = new NullAnnotationLibrary("android.support", "android.support.annotation.NonNull", "android.support.annotation.Nullable", false);
	public static final NullAnnotationLibrary CHECKERFRAMEWORK = new NullAnnotationLibrary("checkerframework", "org.checkerframework.checker.nullness.qual.NonNull", "org.checkerframework.checker.nullness.qual.Nullable", true);
	public static final NullAnnotationLibrary FINDBUGS = new NullAnnotationLibrary("findbugs", "edu.umd.cs.findbugs.annotations.NonNull", "edu.umd.cs.findbugs.annotations.Nullable", false);
	public static final NullAnnotationLibrary SPRING = new NullAnnotationLibrary("spring", "org.springframework.lang.NonNull", "org.springframework.lang.Nullable", false);
	public static final NullAnnotationLibrary JML = new NullAnnotationLibrary("jml", "org.jmlspecs.annotation.NonNull", "org.jmlspecs.annotation.Nullable", false);
	
	private static final List<NullAnnotationLibrary> ALL_AVAILABLE;
	private static final String EXAMPLE_VALUE;
	
	static {
		ArrayList<NullAnnotationLibrary> out = new ArrayList<NullAnnotationLibrary>();
		StringBuilder example = new StringBuilder();
		for (Field f : NullAnnotationLibrary.class.getDeclaredFields()) {
			if (f.getType() != NullAnnotationLibrary.class || !Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) continue;
			try {
				NullAnnotationLibrary nal = (NullAnnotationLibrary) f.get(null);
				out.add(nal);
				example.append(nal.key).append(" | ");
			} catch (IllegalAccessException e) {
				continue;
			}
		}
		out.trimToSize();
		example.append("CUSTOM:com.foo.my.nonnull.annotation:com.foo.my.nullable.annotation");
		ALL_AVAILABLE = Collections.unmodifiableList(out);
		EXAMPLE_VALUE = example.toString();
	}
	
	public static NullAnnotationLibrary custom(String nonNullAnnotation, String nullableAnnotation, boolean typeUse) {
		if (nonNullAnnotation == null && nullableAnnotation == null) return NONE;
		String typeUseStr = typeUse ? "TYPE_USE:" : "";
		if (nullableAnnotation == null) return new NullAnnotationLibrary("custom:" + typeUseStr + nonNullAnnotation, nonNullAnnotation, null, typeUse);
		if (nonNullAnnotation == null) return new NullAnnotationLibrary("custom::" + typeUseStr + nullableAnnotation, null, nullableAnnotation, typeUse);
		return new NullAnnotationLibrary("custom:" + typeUseStr + nonNullAnnotation + ":" + nullableAnnotation, nonNullAnnotation, nullableAnnotation, typeUse);
	}
	
	public static String description() {
		return "nullity-annotation-library";
	}
	
	public static String exampleValue() {
		return EXAMPLE_VALUE;
	}
	
	public static NullAnnotationLibrary valueOf(String in) {
		String ci = in == null ? "" : in.toLowerCase();
		if (ci.length() == 0) return NONE;
		for (NullAnnotationLibrary nal : ALL_AVAILABLE) if (nal.key.equals(ci)) return nal;
		if (!ci.startsWith("custom:")) {
			StringBuilder out = new StringBuilder("Invalid null annotation library. Valid options: ");
			for (NullAnnotationLibrary nal : ALL_AVAILABLE) out.append(nal.key).append(", ");
			out.setLength(out.length() - 2);
			out.append(" or CUSTOM:[TYPE_USE:]nonnull.annotation.type:nullable.annotation.type");
			throw new IllegalArgumentException(out.toString());
		}
		boolean typeUse = ci.startsWith("custom:type_use:");
		int start = typeUse ? 16 : 7;
		int split = ci.indexOf(':', start);
		if (split == -1) {
			String nonNullAnnotation = in.substring(start);
			return custom(verifyTypeName(nonNullAnnotation), null, typeUse);
		}
		String nonNullAnnotation = in.substring(start, split);
		String nullableAnnotation = in.substring(split + 1);
		return custom(verifyTypeName(nonNullAnnotation), verifyTypeName(nullableAnnotation), typeUse);
	}
	
	private static final String MSG = "Not an identifier (provide a fully qualified type for custom: nullity annotations): ";
	private static String verifyTypeName(String fqn) {
		boolean atStart = true;
		for (int i = 0; i < fqn.length(); i++) {
			char c = fqn.charAt(i);
			if (Character.isJavaIdentifierStart(c)) {
				atStart = false;
				continue;
			}
			if (atStart) throw new IllegalArgumentException(MSG + fqn);
			if (c == '.') {
				atStart = true;
				continue;
			}
			if (Character.isJavaIdentifierPart(c)) continue;
			throw new IllegalArgumentException(MSG + fqn);
		}
		if (atStart) throw new IllegalArgumentException(MSG + fqn);
		return fqn;
	}
}
