/*
 * Copyright (C) 2013-2020 The Project Lombok Authors.
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
package lombok.core.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.JavaIdentifiers;
import lombok.core.LombokNode;
import lombok.core.configuration.AllowHelper;
import lombok.core.configuration.ConfigurationKey;
import lombok.core.configuration.FlagUsageType;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Container for static utility methods useful for some of the standard lombok handlers, regardless of
 * target platform (e.g. useful for both javac and Eclipse lombok implementations).
 */
public class HandlerUtil {
	private HandlerUtil() {}
	
	public enum FieldAccess {
		GETTER, PREFER_FIELD, ALWAYS_FIELD;
	}
	
	public static int primeForHashcode() {
		return 59;
	}
	
	public static int primeForTrue() {
		return 79;
	}
	
	public static int primeForFalse() {
		return 97;
	}
	
	public static int primeForNull() {
		return 43;
	}
	
	public static final List<String> NONNULL_ANNOTATIONS, BASE_COPYABLE_ANNOTATIONS, COPY_TO_SETTER_ANNOTATIONS;
	static {
		NONNULL_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"androidx.annotation.NonNull",
			"android.support.annotation.NonNull",
			"com.sun.istack.internal.NotNull",
			"edu.umd.cs.findbugs.annotations.NonNull",
			"javax.annotation.Nonnull",
			// "javax.validation.constraints.NotNull", // The field might contain a null value until it is persisted.
			"lombok.NonNull",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.eclipse.jdt.annotation.NonNull",
			"org.eclipse.jgit.annotations.NonNull",
			"org.jetbrains.annotations.NotNull",
			"org.jmlspecs.annotation.NonNull",
			"org.netbeans.api.annotations.common.NonNull",
			"org.springframework.lang.NonNull",
		}));
		BASE_COPYABLE_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"androidx.annotation.NonNull",
			"androidx.annotation.Nullable",
			"android.support.annotation.NonNull",
			"android.support.annotation.Nullable",
			"edu.umd.cs.findbugs.annotations.NonNull",
			"edu.umd.cs.findbugs.annotations.Nullable",
			"edu.umd.cs.findbugs.annotations.UnknownNullness",
			"javax.annotation.CheckForNull",
			"javax.annotation.Nonnull",
			"javax.annotation.Nullable",
			"lombok.NonNull",
			"org.jmlspecs.annotation.NonNull",
			"org.jmlspecs.annotation.Nullable",
			// To update Checker Framework annotations, run:
			// grep --recursive --files-with-matches -e '^@Target\b.*TYPE_USE' $CHECKERFRAMEWORK/checker/src/main/java  $CHECKERFRAMEWORK/framework/src/main/java | grep '\.java$' | sed 's/.*\/java\//\t\t\t"/' | sed 's/\.java$/",/' | sed 's/\//./g' | sort
			// Only add new annotations, do not remove annotations that have been removed from the lastest version of the Checker Framework.
			"org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey",
			"org.checkerframework.checker.compilermsgs.qual.CompilerMessageKeyBottom",
			"org.checkerframework.checker.compilermsgs.qual.UnknownCompilerMessageKey",
			"org.checkerframework.checker.fenum.qual.AwtAlphaCompositingRule",
			"org.checkerframework.checker.fenum.qual.AwtColorSpace",
			"org.checkerframework.checker.fenum.qual.AwtCursorType",
			"org.checkerframework.checker.fenum.qual.AwtFlowLayout",
			"org.checkerframework.checker.fenum.qual.Fenum",
			"org.checkerframework.checker.fenum.qual.FenumBottom",
			"org.checkerframework.checker.fenum.qual.FenumTop",
			"org.checkerframework.checker.fenum.qual.PolyFenum",
			"org.checkerframework.checker.fenum.qual.SwingBoxOrientation",
			"org.checkerframework.checker.fenum.qual.SwingCompassDirection",
			"org.checkerframework.checker.fenum.qual.SwingElementOrientation",
			"org.checkerframework.checker.fenum.qual.SwingHorizontalOrientation",
			"org.checkerframework.checker.fenum.qual.SwingSplitPaneOrientation",
			"org.checkerframework.checker.fenum.qual.SwingTextOrientation",
			"org.checkerframework.checker.fenum.qual.SwingTitleJustification",
			"org.checkerframework.checker.fenum.qual.SwingTitlePosition",
			"org.checkerframework.checker.fenum.qual.SwingVerticalOrientation",
			"org.checkerframework.checker.formatter.qual.Format",
			"org.checkerframework.checker.formatter.qual.FormatBottom",
			"org.checkerframework.checker.formatter.qual.InvalidFormat",
			"org.checkerframework.checker.guieffect.qual.AlwaysSafe",
			"org.checkerframework.checker.guieffect.qual.PolyUI",
			"org.checkerframework.checker.guieffect.qual.UI",
			"org.checkerframework.checker.i18nformatter.qual.I18nFormat",
			"org.checkerframework.checker.i18nformatter.qual.I18nFormatBottom",
			"org.checkerframework.checker.i18nformatter.qual.I18nFormatFor",
			"org.checkerframework.checker.i18nformatter.qual.I18nInvalidFormat",
			"org.checkerframework.checker.i18nformatter.qual.I18nUnknownFormat",
			"org.checkerframework.checker.i18n.qual.LocalizableKey",
			"org.checkerframework.checker.i18n.qual.LocalizableKeyBottom",
			"org.checkerframework.checker.i18n.qual.Localized",
			"org.checkerframework.checker.i18n.qual.UnknownLocalizableKey",
			"org.checkerframework.checker.i18n.qual.UnknownLocalized",
			"org.checkerframework.checker.index.qual.GTENegativeOne",
			"org.checkerframework.checker.index.qual.IndexFor",
			"org.checkerframework.checker.index.qual.IndexOrHigh",
			"org.checkerframework.checker.index.qual.IndexOrLow",
			"org.checkerframework.checker.index.qual.LengthOf",
			"org.checkerframework.checker.index.qual.LessThan",
			"org.checkerframework.checker.index.qual.LessThanBottom",
			"org.checkerframework.checker.index.qual.LessThanUnknown",
			"org.checkerframework.checker.index.qual.LowerBoundBottom",
			"org.checkerframework.checker.index.qual.LowerBoundUnknown",
			"org.checkerframework.checker.index.qual.LTEqLengthOf",
			"org.checkerframework.checker.index.qual.LTLengthOf",
			"org.checkerframework.checker.index.qual.LTOMLengthOf",
			"org.checkerframework.checker.index.qual.NegativeIndexFor",
			"org.checkerframework.checker.index.qual.NonNegative",
			"org.checkerframework.checker.index.qual.PolyIndex",
			"org.checkerframework.checker.index.qual.PolyLength",
			"org.checkerframework.checker.index.qual.PolyLowerBound",
			"org.checkerframework.checker.index.qual.PolySameLen",
			"org.checkerframework.checker.index.qual.PolyUpperBound",
			"org.checkerframework.checker.index.qual.Positive",
			"org.checkerframework.checker.index.qual.SameLen",
			"org.checkerframework.checker.index.qual.SameLenBottom",
			"org.checkerframework.checker.index.qual.SameLenUnknown",
			"org.checkerframework.checker.index.qual.SearchIndexBottom",
			"org.checkerframework.checker.index.qual.SearchIndexFor",
			"org.checkerframework.checker.index.qual.SearchIndexUnknown",
			"org.checkerframework.checker.index.qual.SubstringIndexBottom",
			"org.checkerframework.checker.index.qual.SubstringIndexFor",
			"org.checkerframework.checker.index.qual.SubstringIndexUnknown",
			"org.checkerframework.checker.index.qual.UpperBoundBottom",
			"org.checkerframework.checker.index.qual.UpperBoundUnknown",
			"org.checkerframework.checker.initialization.qual.FBCBottom",
			"org.checkerframework.checker.initialization.qual.Initialized",
			"org.checkerframework.checker.initialization.qual.UnderInitialization",
			"org.checkerframework.checker.initialization.qual.UnknownInitialization",
			"org.checkerframework.checker.interning.qual.Interned",
			"org.checkerframework.checker.interning.qual.InternedDistinct",
			"org.checkerframework.checker.interning.qual.PolyInterned",
			"org.checkerframework.checker.interning.qual.UnknownInterned",
			"org.checkerframework.checker.lock.qual.GuardedBy",
			"org.checkerframework.checker.lock.qual.GuardedByBottom",
			"org.checkerframework.checker.lock.qual.GuardedByUnknown",
			"org.checkerframework.checker.lock.qual.GuardSatisfied",
			"org.checkerframework.checker.nullness.qual.KeyFor",
			"org.checkerframework.checker.nullness.qual.KeyForBottom",
			"org.checkerframework.checker.nullness.qual.MonotonicNonNull",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.checkerframework.checker.nullness.qual.NonRaw",
			"org.checkerframework.checker.nullness.qual.Nullable",
			"org.checkerframework.checker.nullness.qual.PolyKeyFor",
			"org.checkerframework.checker.nullness.qual.PolyNull",
			"org.checkerframework.checker.nullness.qual.PolyRaw",
			"org.checkerframework.checker.nullness.qual.Raw",
			"org.checkerframework.checker.nullness.qual.UnknownKeyFor",
			"org.checkerframework.checker.optional.qual.MaybePresent",
			"org.checkerframework.checker.optional.qual.PolyPresent",
			"org.checkerframework.checker.optional.qual.Present",
			"org.checkerframework.checker.propkey.qual.PropertyKey",
			"org.checkerframework.checker.propkey.qual.PropertyKeyBottom",
			"org.checkerframework.checker.propkey.qual.UnknownPropertyKey",
			"org.checkerframework.checker.regex.qual.PolyRegex",
			"org.checkerframework.checker.regex.qual.Regex",
			"org.checkerframework.checker.regex.qual.RegexBottom",
			"org.checkerframework.checker.regex.qual.UnknownRegex",
			"org.checkerframework.checker.signature.qual.BinaryName",
			"org.checkerframework.checker.signature.qual.BinaryNameInUnnamedPackage",
			"org.checkerframework.checker.signature.qual.ClassGetName",
			"org.checkerframework.checker.signature.qual.ClassGetSimpleName",
			"org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers",
			"org.checkerframework.checker.signature.qual.FieldDescriptor",
			"org.checkerframework.checker.signature.qual.FieldDescriptorForPrimitive",
			"org.checkerframework.checker.signature.qual.FieldDescriptorForPrimitiveOrArrayInUnnamedPackage",
			"org.checkerframework.checker.signature.qual.FqBinaryName",
			"org.checkerframework.checker.signature.qual.FullyQualifiedName",
			"org.checkerframework.checker.signature.qual.Identifier",
			"org.checkerframework.checker.signature.qual.IdentifierOrArray",
			"org.checkerframework.checker.signature.qual.InternalForm",
			"org.checkerframework.checker.signature.qual.MethodDescriptor",
			"org.checkerframework.checker.signature.qual.PolySignature",
			"org.checkerframework.checker.signature.qual.SignatureBottom",
			"org.checkerframework.checker.signedness.qual.Constant",
			"org.checkerframework.checker.signedness.qual.PolySignedness",
			"org.checkerframework.checker.signedness.qual.PolySigned",
			"org.checkerframework.checker.signedness.qual.Signed",
			"org.checkerframework.checker.signedness.qual.SignednessBottom",
			"org.checkerframework.checker.signedness.qual.SignednessGlb",
			"org.checkerframework.checker.signedness.qual.SignedPositive",
			"org.checkerframework.checker.signedness.qual.UnknownSignedness",
			"org.checkerframework.checker.signedness.qual.Unsigned",
			"org.checkerframework.checker.tainting.qual.PolyTainted",
			"org.checkerframework.checker.tainting.qual.Tainted",
			"org.checkerframework.checker.tainting.qual.Untainted",
			"org.checkerframework.checker.units.qual.A",
			"org.checkerframework.checker.units.qual.Acceleration",
			"org.checkerframework.checker.units.qual.Angle",
			"org.checkerframework.checker.units.qual.Area",
			"org.checkerframework.checker.units.qual.C",
			"org.checkerframework.checker.units.qual.cd",
			"org.checkerframework.checker.units.qual.Current",
			"org.checkerframework.checker.units.qual.degrees",
			"org.checkerframework.checker.units.qual.g",
			"org.checkerframework.checker.units.qual.h",
			"org.checkerframework.checker.units.qual.K",
			"org.checkerframework.checker.units.qual.kg",
			"org.checkerframework.checker.units.qual.km",
			"org.checkerframework.checker.units.qual.km2",
			"org.checkerframework.checker.units.qual.kmPERh",
			"org.checkerframework.checker.units.qual.Length",
			"org.checkerframework.checker.units.qual.Luminance",
			"org.checkerframework.checker.units.qual.m",
			"org.checkerframework.checker.units.qual.m2",
			"org.checkerframework.checker.units.qual.Mass",
			"org.checkerframework.checker.units.qual.min",
			"org.checkerframework.checker.units.qual.mm",
			"org.checkerframework.checker.units.qual.mm2",
			"org.checkerframework.checker.units.qual.mol",
			"org.checkerframework.checker.units.qual.mPERs",
			"org.checkerframework.checker.units.qual.mPERs2",
			"org.checkerframework.checker.units.qual.PolyUnit",
			"org.checkerframework.checker.units.qual.radians",
			"org.checkerframework.checker.units.qual.s",
			"org.checkerframework.checker.units.qual.Speed",
			"org.checkerframework.checker.units.qual.Substance",
			"org.checkerframework.checker.units.qual.Temperature",
			"org.checkerframework.checker.units.qual.Time",
			"org.checkerframework.checker.units.qual.UnitsBottom",
			"org.checkerframework.checker.units.qual.UnknownUnits",
			"org.checkerframework.common.aliasing.qual.LeakedToResult",
			"org.checkerframework.common.aliasing.qual.MaybeAliased",
			"org.checkerframework.common.aliasing.qual.NonLeaked",
			"org.checkerframework.common.aliasing.qual.Unique",
			"org.checkerframework.common.reflection.qual.ClassBound",
			"org.checkerframework.common.reflection.qual.ClassVal",
			"org.checkerframework.common.reflection.qual.ClassValBottom",
			"org.checkerframework.common.reflection.qual.MethodVal",
			"org.checkerframework.common.reflection.qual.MethodValBottom",
			"org.checkerframework.common.reflection.qual.UnknownClass",
			"org.checkerframework.common.reflection.qual.UnknownMethod",
			"org.checkerframework.common.subtyping.qual.Bottom",
			"org.checkerframework.common.util.report.qual.ReportUnqualified",
			"org.checkerframework.common.value.qual.ArrayLen",
			"org.checkerframework.common.value.qual.ArrayLenRange",
			"org.checkerframework.common.value.qual.BoolVal",
			"org.checkerframework.common.value.qual.BottomVal",
			"org.checkerframework.common.value.qual.DoubleVal",
			"org.checkerframework.common.value.qual.IntRange",
			"org.checkerframework.common.value.qual.IntVal",
			"org.checkerframework.common.value.qual.MinLen",
			"org.checkerframework.common.value.qual.PolyValue",
			"org.checkerframework.common.value.qual.StringVal",
			"org.checkerframework.common.value.qual.UnknownVal",
			"org.checkerframework.framework.qual.PolyAll",
			"org.checkerframework.framework.util.PurityUnqualified",
			
			"org.eclipse.jdt.annotation.NonNull",
			"org.eclipse.jdt.annotation.Nullable",
			"org.jetbrains.annotations.NotNull",
			"org.jetbrains.annotations.Nullable",
			"org.springframework.lang.NonNull",
			"org.springframework.lang.Nullable",
			"org.netbeans.api.annotations.common.NonNull",
			"org.netbeans.api.annotations.common.NullAllowed",
		}));
		COPY_TO_SETTER_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"com.fasterxml.jackson.annotation.JsonProperty",
			"com.fasterxml.jackson.annotation.JsonSetter",
		}));
	}
	
	/** Checks if the given name is a valid identifier.
	 * 
	 * If it is, this returns {@code true} and does nothing else.
	 * If it isn't, this returns {@code false} and adds an error message to the supplied node.
	 */
	public static boolean checkName(String nameSpec, String identifier, LombokNode<?, ?, ?> errorNode) {
		if (identifier.isEmpty()) {
			errorNode.addError(nameSpec + " cannot be the empty string.");
			return false;
		}
		
		if (!JavaIdentifiers.isValidJavaIdentifier(identifier)) {
			errorNode.addError(nameSpec + " must be a valid java identifier.");
			return false;
		}
		
		return true;
	}
	
	public static String autoSingularize(String plural) {
		return Singulars.autoSingularize(plural);
	}
	public static void handleFlagUsage(LombokNode<?, ?, ?> node, ConfigurationKey<FlagUsageType> key, String featureName) {
		FlagUsageType fut = node.getAst().readConfiguration(key);
		
		if (fut == null && AllowHelper.isAllowable(key)) {
			node.addError("Use of " + featureName + " is disabled by default. Please add '" + key.getKeyName() + " = " + FlagUsageType.ALLOW + "' to 'lombok.config' if you want to enable is.");
		}
		
		if (fut != null) {
			String msg = "Use of " + featureName + " is flagged according to lombok configuration.";
			if (fut == FlagUsageType.WARNING) node.addWarning(msg);
			else if (fut == FlagUsageType.ERROR) node.addError(msg);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static boolean shouldAddGenerated(LombokNode<?, ?, ?> node) {
		Boolean add = node.getAst().readConfiguration(ConfigurationKeys.ADD_JAVAX_GENERATED_ANNOTATIONS);
		if (add != null) return add;
		return Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_GENERATED_ANNOTATIONS));
	}
	
	public static void handleExperimentalFlagUsage(LombokNode<?, ?, ?> node, ConfigurationKey<FlagUsageType> key, String featureName) {
		handleFlagUsage(node, key, featureName, ConfigurationKeys.EXPERIMENTAL_FLAG_USAGE, "any lombok.experimental feature");
	}
	
	public static void handleFlagUsage(LombokNode<?, ?, ?> node, ConfigurationKey<FlagUsageType> key1, String featureName1, ConfigurationKey<FlagUsageType> key2, String featureName2) {
		FlagUsageType fut1 = node.getAst().readConfiguration(key1);
		FlagUsageType fut2 = node.getAst().readConfiguration(key2);
		
		FlagUsageType fut = null;
		String featureName = null;
		if (fut1 == FlagUsageType.ERROR) {
			fut = fut1;
			featureName = featureName1;
		} else if (fut2 == FlagUsageType.ERROR) {
			fut = fut2;
			featureName = featureName2;
		} else if (fut1 == FlagUsageType.WARNING) {
			fut = fut1;
			featureName = featureName1;
		} else {
			fut = fut2;
			featureName = featureName2;
		}
		
		if (fut != null) {
			String msg = "Use of " + featureName + " is flagged according to lombok configuration.";
			if (fut == FlagUsageType.WARNING) node.addWarning(msg);
			else if (fut == FlagUsageType.ERROR) node.addError(msg);
		}
	}
	
	public static boolean shouldReturnThis0(AnnotationValues<Accessors> accessors, AST<?, ?, ?> ast) {
		boolean chainForced = accessors.isExplicit("chain");
		boolean fluentForced = accessors.isExplicit("fluent");
		Accessors instance = accessors.getInstance();
		
		boolean chain = instance.chain();
		boolean fluent = instance.fluent();
		
		if (chainForced) return chain;
		
		if (!chainForced) {
			Boolean chainConfig = ast.readConfiguration(ConfigurationKeys.ACCESSORS_CHAIN);
			if (chainConfig != null) return chainConfig;
		}
		
		if (!fluentForced) {
			Boolean fluentConfig = ast.readConfiguration(ConfigurationKeys.ACCESSORS_FLUENT);
			if (fluentConfig != null) fluent = fluentConfig;
		}
		
		return chain || fluent;
	}
	
	@SuppressWarnings({"all", "unchecked", "deprecation"})
	public static final List<String> INVALID_ON_BUILDERS = Collections.unmodifiableList(
			Arrays.<String>asList(
			Getter.class.getName(), Setter.class.getName(), With.class.getName(), "lombok.experimental.Wither",
			ToString.class.getName(), EqualsAndHashCode.class.getName(), 
			RequiredArgsConstructor.class.getName(), AllArgsConstructor.class.getName(), NoArgsConstructor.class.getName(), 
			Data.class.getName(), Value.class.getName(), "lombok.experimental.Value", FieldDefaults.class.getName()));
	
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
	public static CharSequence removePrefix(CharSequence fieldName, List<String> prefixes) {
		if (prefixes == null || prefixes.isEmpty()) return fieldName;
		
		fieldName = fieldName.toString();
		
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
	
	public static final String DEFAULT_EXCEPTION_FOR_NON_NULL = "java.lang.NullPointerException";
	
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
	public static String toGetterName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(ast, accessors, fieldName, isBoolean, "is", "get", true);
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
	public static String toSetterName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(ast, accessors, fieldName, isBoolean, "set", "set", true);
	}
	
	/**
	 * Generates a with name from a given field name.
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
	 * @return The with name for this field, or {@code null} if this field does not fit expected patterns and therefore cannot be turned into a getter name.
	 */
	public static String toWithName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(ast, accessors, fieldName, isBoolean, "with", "with", false);
	}
	
	private static String toAccessorName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean,
			String booleanPrefix, String normalPrefix, boolean adhereToFluent) {
		
		fieldName = fieldName.toString();
		if (fieldName.length() == 0) return null;
		
		if (Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.GETTER_CONSEQUENT_BOOLEAN))) isBoolean = false;
		boolean explicitPrefix = accessors != null && accessors.isExplicit("prefix");
		boolean explicitFluent = accessors != null && accessors.isExplicit("fluent");
		
		Accessors ac = (explicitPrefix || explicitFluent) ? accessors.getInstance() : null;
		
		List<String> prefix = explicitPrefix ? Arrays.asList(ac.prefix()) : ast.readConfiguration(ConfigurationKeys.ACCESSORS_PREFIX);
		boolean fluent = explicitFluent ? ac.fluent() : Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.ACCESSORS_FLUENT));
		
		fieldName = removePrefix(fieldName, prefix);
		if (fieldName == null) return null;
		
		String fName = fieldName.toString();
		if (adhereToFluent && fluent) return fName;
		
		if (isBoolean && fName.startsWith("is") && fieldName.length() > 2 && !Character.isLowerCase(fieldName.charAt(2))) {
			// The field is for example named 'isRunning'.
			return booleanPrefix + fName.substring(2);
		}
		
		return buildAccessorName(isBoolean ? booleanPrefix : normalPrefix, fName);
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
	public static List<String> toAllGetterNames(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(ast, accessors, fieldName, isBoolean, "is", "get", true);
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
	public static List<String> toAllSetterNames(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(ast, accessors, fieldName, isBoolean, "set", "set", true);
	}
	
	/**
	 * Returns all names of methods that would represent the with for a field with the provided name.
	 * 
	 * For example if {@code isBoolean} is true, then a field named {@code isRunning} would produce:<br />
	 * {@code [withRunning, withIsRunning]}
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide {@code false}.
	 */
	public static List<String> toAllWithNames(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAllAccessorNames(ast, accessors, fieldName, isBoolean, "with", "with", false);
	}
	
	private static List<String> toAllAccessorNames(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean,
			String booleanPrefix, String normalPrefix, boolean adhereToFluent) {
		
		if (Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.GETTER_CONSEQUENT_BOOLEAN))) isBoolean = false;
		if (!isBoolean) {
			String accessorName = toAccessorName(ast, accessors, fieldName, false, booleanPrefix, normalPrefix, adhereToFluent);
			return (accessorName == null) ? Collections.<String>emptyList() : Collections.singletonList(accessorName);
		}
		
		boolean explicitPrefix = accessors != null && accessors.isExplicit("prefix");
		boolean explicitFluent = accessors != null && accessors.isExplicit("fluent");
		
		Accessors ac = (explicitPrefix || explicitFluent) ? accessors.getInstance() : null;
		
		List<String> prefix = explicitPrefix ? Arrays.asList(ac.prefix()) : ast.readConfiguration(ConfigurationKeys.ACCESSORS_PREFIX);
		boolean fluent = explicitFluent ? ac.fluent() : Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.ACCESSORS_FLUENT));
		
		fieldName = removePrefix(fieldName, prefix);
		if (fieldName == null) return Collections.emptyList();
		
		List<String> baseNames = toBaseNames(fieldName, isBoolean, fluent);
		
		Set<String> names = new HashSet<String>();
		for (String baseName : baseNames) {
			if (adhereToFluent && fluent) {
				names.add(baseName);
			} else {
				names.add(buildAccessorName(normalPrefix, baseName));
				if (!normalPrefix.equals(booleanPrefix)) names.add(buildAccessorName(booleanPrefix, baseName));
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
	public static String buildAccessorName(String prefix, String suffix) {
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
	
	public static String camelCaseToConstant(String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) return "";
		StringBuilder b = new StringBuilder();
		b.append(Character.toUpperCase(fieldName.charAt(0)));
		for (int i = 1; i < fieldName.length(); i++) {
			char c = fieldName.charAt(i);
			if (Character.isUpperCase(c)) b.append('_');
			b.append(Character.toUpperCase(c));
		}
		return b.toString();
	}
}
