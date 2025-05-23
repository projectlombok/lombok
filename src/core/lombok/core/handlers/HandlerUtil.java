/*
 * Copyright (C) 2013-2025 The Project Lombok Authors.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import lombok.core.configuration.CapitalizationStrategy;
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
	
	public static final List<String> NONNULL_ANNOTATIONS, BASE_COPYABLE_ANNOTATIONS, JACKSON_COPY_TO_GETTER_ANNOTATIONS, JACKSON_COPY_TO_SETTER_ANNOTATIONS, JACKSON_COPY_TO_BUILDER_SINGULAR_SETTER_ANNOTATIONS, JACKSON_COPY_TO_BUILDER_ANNOTATIONS;
	static {
		// This is a list of annotations with a __highly specific meaning__: All annotations in this list indicate that passing null for the relevant item is __never__ acceptable, regardless of settings or circumstance.
		// In other words, things like 'this models a database table, and the db table column has a nonnull constraint', or 'this represents a web form, and if this is null, the form is invalid' __do not count__ and should not be in this list;
		// after all, you should be able to model invalid rows, or invalid forms.
		
		// In addition, the intent for these annotations is that they can be used 'in public' - it's not for internal-only usage annotations.
		
		// Presence of these annotations mean that lombok will generate null checks in any created setters and constructors.
		NONNULL_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"android.annotation.NonNull",
			"android.support.annotation.NonNull",
			"android.support.annotation.RecentlyNonNull",
			"androidx.annotation.NonNull",
			"androidx.annotation.RecentlyNonNull",
			"com.android.annotations.NonNull",
			"com.google.firebase.database.annotations.NotNull", // Even though it's in a database package, it does mean semantically: "Check if never null at the language level", and not 'db column cannot be null'.
			"com.mongodb.lang.NonNull", // Even though mongo is a DB engine, this semantically refers to language, not DB table designs (mongo is a document DB engine, so this isn't surprising perhaps).
			"com.sun.istack.NotNull",
			"com.unboundid.util.NotNull",
			"edu.umd.cs.findbugs.annotations.NonNull",
			"io.micrometer.core.lang.NonNull",
			"io.reactivex.annotations.NonNull",
			"io.reactivex.rxjava3.annotations.NonNull",
			"jakarta.annotation.Nonnull",
			"javax.annotation.Nonnull",
			// "javax.validation.constraints.NotNull", // The field might contain a null value until it is persisted.
			"libcore.util.NonNull",
			"lombok.NonNull",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.checkerframework.checker.nullness.compatqual.NonNullDecl",
			"org.checkerframework.checker.nullness.compatqual.NonNullType",
			"org.codehaus.commons.nullanalysis.NotNull",
			"org.eclipse.jdt.annotation.NonNull",
			"org.jetbrains.annotations.NotNull",
			"org.jmlspecs.annotation.NonNull",
			"org.jspecify.annotations.NonNull",
			"org.netbeans.api.annotations.common.NonNull",
			"org.springframework.lang.NonNull",
			"reactor.util.annotation.NonNull",
		}));
		
		// This is a list of annotations that lombok will automatically 'copy' - be it to the method (when generating a getter for a field annotated with one of these), or to a parameter (generating a setter, with-er, or builder 'setter').
		// You can't disable this behaviour, so the list should only contain annotations where 'copy it!' is the desired behaviour in at least 95%, preferably 98%, of all non-buggy usages.
		// As a general rule, lombok takes on maintenance of adding all nullity-related annotations here, _if_ they fit the definition of language-level nullity as per {@see #NONNULL_ANNOTATIONS}. As a consequence, everything from the NONNULL list should probably
		// also be in this list, and any nullity-related annotation in this list implies the non-null variant should be in the NONNULL_ANNOTATIONS list, unless there is no such annotation.
		
		// NB: Intent is that we get rid of a lot of this list and instead move to a system whereby lombok users explicitly opt in to the desired behaviour per 'library' (e.g per "Jackson annotations", "Checker framework annotations", etc.
		// - the problem is, how do we know that the owners of a certain annotation intend for it to be copied in this fashion? What to do if a bug report is filed that we should not always copy it? Hence, care should be taken when editing this list.
		// When in doubt, leave it out - this list can be added to dynamically by {See lombok.ConfigurationKeys#COPYABLE_ANNOTATIONS}.
		BASE_COPYABLE_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"android.annotation.NonNull",
			"android.annotation.Nullable",
			"android.support.annotation.NonNull",
			"android.support.annotation.Nullable",
			"android.support.annotation.RecentlyNonNull",
			"android.support.annotation.RecentlyNullable",
			"androidx.annotation.NonNull",
			"androidx.annotation.Nullable",
			"androidx.annotation.RecentlyNonNull",
			"androidx.annotation.RecentlyNullable",
			"com.android.annotations.NonNull",
			"com.android.annotations.Nullable",
			// "com.google.api.server.spi.config.Nullable", - let's think about this one a little, as it is targeted solely at parameters, so you can't even put it on fields. If we choose to support it, we should REMOVE it from the field, then - that's not something we currently support.
			"com.google.firebase.database.annotations.NotNull",
			"com.google.firebase.database.annotations.Nullable",
			"com.mongodb.lang.NonNull",
			"com.mongodb.lang.Nullable",
			"com.sun.istack.NotNull",
			"com.sun.istack.Nullable",
			"com.unboundid.util.NotNull",
			"com.unboundid.util.Nullable",
			"edu.umd.cs.findbugs.annotations.CheckForNull",
			"edu.umd.cs.findbugs.annotations.NonNull",
			"edu.umd.cs.findbugs.annotations.Nullable",
			"edu.umd.cs.findbugs.annotations.PossiblyNull",
			"edu.umd.cs.findbugs.annotations.UnknownNullness",
			"io.micrometer.core.lang.NonNull",
			"io.micrometer.core.lang.Nullable",
			"io.reactivex.annotations.NonNull",
			"io.reactivex.annotations.Nullable",
			"io.reactivex.rxjava3.annotations.NonNull",
			"io.reactivex.rxjava3.annotations.Nullable",
			"jakarta.annotation.Nonnull",
			"jakarta.annotation.Nullable",
			"javax.annotation.CheckForNull",
			"javax.annotation.Nonnull",
			"javax.annotation.Nullable",
//			"javax.validation.constraints.NotNull", // - this should definitely not be included; validation is not about language-level nullity, therefore should not be in this core list.
			"libcore.util.NonNull",
			"libcore.util.Nullable",
			"lombok.NonNull",
			"org.checkerframework.checker.nullness.compatqual.NonNullDecl",
			"org.checkerframework.checker.nullness.compatqual.NonNullType",
			"org.checkerframework.checker.nullness.compatqual.NullableDecl",
			"org.checkerframework.checker.nullness.compatqual.NullableType",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.checkerframework.checker.nullness.qual.Nullable",
			"org.codehaus.commons.nullanalysis.NotNull",
			"org.codehaus.commons.nullanalysis.Nullable",
			"org.eclipse.jdt.annotation.NonNull",
			"org.eclipse.jdt.annotation.Nullable",
			"org.jetbrains.annotations.NotNull",
			"org.jetbrains.annotations.Nullable",
			"org.jetbrains.annotations.UnknownNullability",
			"org.jmlspecs.annotation.NonNull",
			"org.jmlspecs.annotation.Nullable",
			"org.jspecify.annotations.Nullable",
			"org.jspecify.annotations.NonNull",
			"org.netbeans.api.annotations.common.CheckForNull",
			"org.netbeans.api.annotations.common.NonNull",
			"org.netbeans.api.annotations.common.NullAllowed",
			"org.netbeans.api.annotations.common.NullUnknown",
			"org.springframework.lang.NonNull",
			"org.springframework.lang.Nullable",
			"reactor.util.annotation.NonNull",
			"reactor.util.annotation.Nullable",
			
			// Checker Framework annotations.
			// To update Checker Framework annotations, run:
			// grep --recursive --files-with-matches -e '^@Target\b.*TYPE_USE' $CHECKERFRAMEWORK/checker/src/main/java $CHECKERFRAMEWORK/checker-qual/src/main/java $CHECKERFRAMEWORK/checker-util/src/main/java $CHECKERFRAMEWORK/framework/src/main/java | grep '\.java$' | sed 's/.*\/java\//\t\t\t"/' | sed 's/\.java$/",/' | sed 's/\//./g' | sort
			// Only add new annotations, do not remove annotations that have been removed from the latest version of the Checker Framework.
			"org.checkerframework.checker.builder.qual.CalledMethods",
			"org.checkerframework.checker.builder.qual.NotCalledMethods",
			"org.checkerframework.checker.calledmethods.qual.CalledMethods",
			"org.checkerframework.checker.calledmethods.qual.CalledMethodsBottom",
			"org.checkerframework.checker.calledmethods.qual.CalledMethodsPredicate",
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
			"org.checkerframework.checker.formatter.qual.UnknownFormat",
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
			"org.checkerframework.checker.index.qual.UpperBoundLiteral",
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
			"org.checkerframework.checker.lock.qual.NewObject",
			"org.checkerframework.checker.mustcall.qual.MustCall",
			"org.checkerframework.checker.mustcall.qual.MustCallAlias",
			"org.checkerframework.checker.mustcall.qual.MustCallUnknown",
			"org.checkerframework.checker.mustcall.qual.PolyMustCall",
			"org.checkerframework.checker.nullness.qual.KeyFor",
			"org.checkerframework.checker.nullness.qual.KeyForBottom",
			"org.checkerframework.checker.nullness.qual.MonotonicNonNull",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.checkerframework.checker.nullness.qual.Nullable",
			"org.checkerframework.checker.nullness.qual.PolyKeyFor",
			"org.checkerframework.checker.nullness.qual.PolyNull",
			"org.checkerframework.checker.nullness.qual.UnknownKeyFor",
			"org.checkerframework.checker.optional.qual.MaybePresent",
			"org.checkerframework.checker.optional.qual.OptionalBottom",
			"org.checkerframework.checker.optional.qual.PolyPresent",
			"org.checkerframework.checker.optional.qual.Present",
			"org.checkerframework.checker.propkey.qual.PropertyKey",
			"org.checkerframework.checker.propkey.qual.PropertyKeyBottom",
			"org.checkerframework.checker.propkey.qual.UnknownPropertyKey",
			"org.checkerframework.checker.regex.qual.PolyRegex",
			"org.checkerframework.checker.regex.qual.Regex",
			"org.checkerframework.checker.regex.qual.RegexBottom",
			"org.checkerframework.checker.regex.qual.UnknownRegex",
			"org.checkerframework.checker.signature.qual.ArrayWithoutPackage",
			"org.checkerframework.checker.signature.qual.BinaryName",
			"org.checkerframework.checker.signature.qual.BinaryNameOrPrimitiveType",
			"org.checkerframework.checker.signature.qual.BinaryNameWithoutPackage",
			"org.checkerframework.checker.signature.qual.CanonicalName",
			"org.checkerframework.checker.signature.qual.CanonicalNameAndBinaryName",
			"org.checkerframework.checker.signature.qual.CanonicalNameOrEmpty",
			"org.checkerframework.checker.signature.qual.CanonicalNameOrPrimitiveType",
			"org.checkerframework.checker.signature.qual.ClassGetName",
			"org.checkerframework.checker.signature.qual.ClassGetSimpleName",
			"org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers",
			"org.checkerframework.checker.signature.qual.DotSeparatedIdentifiersOrPrimitiveType",
			"org.checkerframework.checker.signature.qual.FieldDescriptor",
			"org.checkerframework.checker.signature.qual.FieldDescriptorForPrimitive",
			"org.checkerframework.checker.signature.qual.FieldDescriptorWithoutPackage",
			"org.checkerframework.checker.signature.qual.FqBinaryName",
			"org.checkerframework.checker.signature.qual.FullyQualifiedName",
			"org.checkerframework.checker.signature.qual.Identifier",
			"org.checkerframework.checker.signature.qual.IdentifierOrPrimitiveType",
			"org.checkerframework.checker.signature.qual.InternalForm",
			"org.checkerframework.checker.signature.qual.MethodDescriptor",
			"org.checkerframework.checker.signature.qual.PolySignature",
			"org.checkerframework.checker.signature.qual.PrimitiveType",
			"org.checkerframework.checker.signature.qual.SignatureBottom",
			"org.checkerframework.checker.signedness.qual.PolySigned",
			"org.checkerframework.checker.signedness.qual.Signed",
			"org.checkerframework.checker.signedness.qual.SignednessBottom",
			"org.checkerframework.checker.signedness.qual.SignednessGlb",
			"org.checkerframework.checker.signedness.qual.SignedPositive",
			"org.checkerframework.checker.signedness.qual.SignedPositiveFromUnsigned",
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
			"org.checkerframework.checker.units.qual.Force",
			"org.checkerframework.checker.units.qual.g",
			"org.checkerframework.checker.units.qual.h",
			"org.checkerframework.checker.units.qual.K",
			"org.checkerframework.checker.units.qual.kg",
			"org.checkerframework.checker.units.qual.km",
			"org.checkerframework.checker.units.qual.km2",
			"org.checkerframework.checker.units.qual.km3",
			"org.checkerframework.checker.units.qual.kmPERh",
			"org.checkerframework.checker.units.qual.kN",
			"org.checkerframework.checker.units.qual.Length",
			"org.checkerframework.checker.units.qual.Luminance",
			"org.checkerframework.checker.units.qual.m",
			"org.checkerframework.checker.units.qual.m2",
			"org.checkerframework.checker.units.qual.m3",
			"org.checkerframework.checker.units.qual.Mass",
			"org.checkerframework.checker.units.qual.min",
			"org.checkerframework.checker.units.qual.mm",
			"org.checkerframework.checker.units.qual.mm2",
			"org.checkerframework.checker.units.qual.mm3",
			"org.checkerframework.checker.units.qual.mol",
			"org.checkerframework.checker.units.qual.mPERs",
			"org.checkerframework.checker.units.qual.mPERs2",
			"org.checkerframework.checker.units.qual.N",
			"org.checkerframework.checker.units.qual.PolyUnit",
			"org.checkerframework.checker.units.qual.radians",
			"org.checkerframework.checker.units.qual.s",
			"org.checkerframework.checker.units.qual.Speed",
			"org.checkerframework.checker.units.qual.Substance",
			"org.checkerframework.checker.units.qual.t",
			"org.checkerframework.checker.units.qual.Temperature",
			"org.checkerframework.checker.units.qual.Time",
			"org.checkerframework.checker.units.qual.UnitsBottom",
			"org.checkerframework.checker.units.qual.UnknownUnits",
			"org.checkerframework.checker.units.qual.Volume",
			"org.checkerframework.common.aliasing.qual.LeakedToResult",
			"org.checkerframework.common.aliasing.qual.MaybeAliased",
			"org.checkerframework.common.aliasing.qual.NonLeaked",
			"org.checkerframework.common.aliasing.qual.Unique",
			"org.checkerframework.common.initializedfields.qual.InitializedFields",
			"org.checkerframework.common.initializedfields.qual.InitializedFieldsBottom",
			"org.checkerframework.common.initializedfields.qual.PolyInitializedFields",
			"org.checkerframework.common.reflection.qual.ClassBound",
			"org.checkerframework.common.reflection.qual.ClassVal",
			"org.checkerframework.common.reflection.qual.ClassValBottom",
			"org.checkerframework.common.reflection.qual.MethodVal",
			"org.checkerframework.common.reflection.qual.MethodValBottom",
			"org.checkerframework.common.reflection.qual.UnknownClass",
			"org.checkerframework.common.reflection.qual.UnknownMethod",
			"org.checkerframework.common.returnsreceiver.qual.BottomThis",
			"org.checkerframework.common.returnsreceiver.qual.This",
			"org.checkerframework.common.returnsreceiver.qual.UnknownThis",
			"org.checkerframework.common.subtyping.qual.Bottom",
			"org.checkerframework.common.util.report.qual.ReportUnqualified",
			"org.checkerframework.common.value.qual.ArrayLen",
			"org.checkerframework.common.value.qual.ArrayLenRange",
			"org.checkerframework.common.value.qual.BoolVal",
			"org.checkerframework.common.value.qual.BottomVal",
			"org.checkerframework.common.value.qual.DoubleVal",
			"org.checkerframework.common.value.qual.EnumVal",
			"org.checkerframework.common.value.qual.IntRange",
			"org.checkerframework.common.value.qual.IntVal",
			"org.checkerframework.common.value.qual.MatchesRegex",
			"org.checkerframework.common.value.qual.MinLen",
			"org.checkerframework.common.value.qual.PolyValue",
			"org.checkerframework.common.value.qual.StringVal",
			"org.checkerframework.common.value.qual.UnknownVal",
			"org.checkerframework.framework.qual.PurityUnqualified",
		}));
		
		// The following two lists contain all annotations that can be copied from the field to the getter or setter. 
		// Right now, it only contains Jackson annotations.
		// Jackson's annotation processing roughly works as follows: To calculate the annotation for a JSON property, Jackson 
		// builds a triple of the Java field and the corresponding setter and getter methods. It is sufficient for an annotation
		// to be present on one of those to become effective. E.g., a @JsonIgnore on a setter completely ignores the JSON property, 
		// not only during deserialization, but also when serializing. Therefore, in most cases it is _not_ necessary to copy the 
		// annotations. It may even harm, as Jackson considers some annotations inheritable, and this "virtual inheritance" only
		// affects annotations on setter/getter, but not on private fields. 
		// However, there are two exceptions where we have to copy the annotations:
		// 1. When using a builder to deserialize, Jackson does _not_ "propagate" the annotations to the setter methods of the 
		//    builder, i.e. annotations like @JsonIgnore on the field will not be respected when deserializing with a builder.
		//    Thus, those annotations should be copied to the builder's setters.
		// 2. If the getter/setter methods do not follow the exact beanspec naming strategy, Jackson will not correctly detect the 
		//    field-getter-setter triple, and annotations may not work as intended.
		//    However, we cannot always know what the user's intention is. Thus, lombok should only fix those cases where it is 
		//    obvious what the user wants. That is the case for a `@Jacksonized @Accessors(fluent=true)`.
		JACKSON_COPY_TO_GETTER_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"com.fasterxml.jackson.annotation.JsonFormat",
			"com.fasterxml.jackson.annotation.JsonIgnore",
			"com.fasterxml.jackson.annotation.JsonIgnoreProperties",
			"com.fasterxml.jackson.annotation.JsonProperty",
			"com.fasterxml.jackson.annotation.JsonSubTypes",
			"com.fasterxml.jackson.annotation.JsonTypeInfo",
			"com.fasterxml.jackson.annotation.JsonUnwrapped",
			"com.fasterxml.jackson.annotation.JsonView",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText",
		}));
		JACKSON_COPY_TO_SETTER_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"com.fasterxml.jackson.annotation.JacksonInject",
			"com.fasterxml.jackson.annotation.JsonAlias",
			"com.fasterxml.jackson.annotation.JsonFormat",
			"com.fasterxml.jackson.annotation.JsonIgnore",
			"com.fasterxml.jackson.annotation.JsonIgnoreProperties",
			"com.fasterxml.jackson.annotation.JsonProperty",
			"com.fasterxml.jackson.annotation.JsonSetter",
			"com.fasterxml.jackson.annotation.JsonSubTypes",
			"com.fasterxml.jackson.annotation.JsonTypeInfo",
			"com.fasterxml.jackson.annotation.JsonUnwrapped",
			"com.fasterxml.jackson.annotation.JsonView",
			"com.fasterxml.jackson.databind.annotation.JsonDeserialize",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty",
			"com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText",
		}));
		JACKSON_COPY_TO_BUILDER_SINGULAR_SETTER_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"com.fasterxml.jackson.annotation.JsonAnySetter",
		}));
		// In order to let Jackson recognize certain configuration annotations when deserializing using a builder, those must
		// be copied to the generated builder class.
		JACKSON_COPY_TO_BUILDER_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
			"com.fasterxml.jackson.annotation.JsonAutoDetect",
			"com.fasterxml.jackson.annotation.JsonFormat",
			"com.fasterxml.jackson.annotation.JsonIgnoreProperties",
			"com.fasterxml.jackson.annotation.JsonIgnoreType",
			"com.fasterxml.jackson.annotation.JsonPropertyOrder",
			"com.fasterxml.jackson.annotation.JsonRootName",
			"com.fasterxml.jackson.annotation.JsonSubTypes",
			"com.fasterxml.jackson.annotation.JsonTypeInfo",
			"com.fasterxml.jackson.annotation.JsonTypeName",
			"com.fasterxml.jackson.annotation.JsonView",
			"com.fasterxml.jackson.databind.annotation.JsonNaming",
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
	
	public static boolean shouldMakeFinal0(AnnotationValues<Accessors> accessors, AST<?, ?, ?> ast) {
		boolean isExplicit = accessors.isExplicit("makeFinal");
		if (isExplicit) return accessors.getAsBoolean("makeFinal");
		Boolean config = ast.readConfiguration(ConfigurationKeys.ACCESSORS_MAKE_FINAL);
		if (config != null) return config.booleanValue();
		return false;
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
	
	/**
	 * Generates a withBy name from a given field name.
	 * 
	 * Strategy: The same as the {@code toWithName} strategy, but then append {@code "By"} at the end.
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type {@code java.lang.Boolean}, you should provide {@code false}.
	 * @return The with name for this field, or {@code null} if this field does not fit expected patterns and therefore cannot be turned into a getter name.
	 */
	public static String toWithByName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		return toAccessorName(ast, accessors, fieldName, isBoolean, "with", "with", false) + "By";
	}
	
	private static String toAccessorName(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean,
			String booleanPrefix, String normalPrefix, boolean adhereToFluent) {
		
		fieldName = fieldName.toString();
		if (fieldName.length() == 0) return null;
		
		if (Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.GETTER_CONSEQUENT_BOOLEAN))) isBoolean = false;
		boolean explicitPrefix = accessors != null && accessors.isExplicit("prefix");
		boolean explicitFluent = accessors != null && accessors.isExplicit("fluent");
		boolean explicitJavaBeansSpecCapitalization = accessors != null && accessors.isExplicit("javaBeansSpecCapitalization");
		
		Accessors ac = (explicitPrefix || explicitFluent || explicitJavaBeansSpecCapitalization) ? accessors.getInstance() : null;
		
		List<String> prefix = explicitPrefix ? Arrays.asList(ac.prefix()) : ast.readConfiguration(ConfigurationKeys.ACCESSORS_PREFIX);
		boolean fluent = explicitFluent ? ac.fluent() : Boolean.TRUE.equals(ast.readConfiguration(ConfigurationKeys.ACCESSORS_FLUENT));
		CapitalizationStrategy capitalizationStrategy = ast.readConfigurationOr(ConfigurationKeys.ACCESSORS_JAVA_BEANS_SPEC_CAPITALIZATION, CapitalizationStrategy.defaultValue());
		
		fieldName = removePrefix(fieldName, prefix);
		if (fieldName == null) return null;
		
		String fName = fieldName.toString();
		if (adhereToFluent && fluent) return fName;
		
		if (isBoolean && fName.startsWith("is") && fieldName.length() > 2 && !Character.isLowerCase(fieldName.charAt(2))) {
			// The field is for example named 'isRunning'.
			return booleanPrefix + fName.substring(2);
		}
		
		return buildAccessorName(isBoolean ? booleanPrefix : normalPrefix, fName, capitalizationStrategy);
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
	
	/**
	 * Returns all names of methods that would represent the withBy for a field with the provided name.
	 * 
	 * For example if {@code isBoolean} is true, then a field named {@code isRunning} would produce:<br />
	 * {@code [withRunningBy, withIsRunningBy]}
	 * 
	 * @param accessors Accessors configuration.
	 * @param fieldName the name of the field.
	 * @param isBoolean if the field is of type 'boolean'. For fields of type 'java.lang.Boolean', you should provide {@code false}.
	 */
	public static List<String> toAllWithByNames(AST<?, ?, ?> ast, AnnotationValues<Accessors> accessors, CharSequence fieldName, boolean isBoolean) {
		List<String> in = toAllAccessorNames(ast, accessors, fieldName, isBoolean, "with", "with", false);
		if (!(in instanceof ArrayList)) in = new ArrayList<String>(in);
		for (int i = 0; i < in.size(); i++) in.set(i, in.get(i) + "By");
		return in;
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
		CapitalizationStrategy capitalizationStrategy = ast.readConfigurationOr(ConfigurationKeys.ACCESSORS_JAVA_BEANS_SPEC_CAPITALIZATION, CapitalizationStrategy.defaultValue());
		
		fieldName = removePrefix(fieldName, prefix);
		if (fieldName == null) return Collections.emptyList();
		
		List<String> baseNames = toBaseNames(fieldName, isBoolean, fluent);
		
		Set<String> names = new HashSet<String>();
		for (String baseName : baseNames) {
			if (adhereToFluent && fluent) {
				names.add(baseName);
			} else {
				names.add(buildAccessorName(normalPrefix, baseName, capitalizationStrategy));
				if (!normalPrefix.equals(booleanPrefix)) names.add(buildAccessorName(booleanPrefix, baseName, capitalizationStrategy));
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
	 * @param node Any node (used to fetch config of capitalization strategy).
	 * @param prefix Something like {@code get} or {@code set} or {@code is}.
	 * @param suffix Something like {@code running}.
	 * @return prefix + smartly title-cased suffix. For example, {@code setRunning}.
	 */
	public static String buildAccessorName(AST<?, ?, ?> ast, String prefix, String suffix) {
		CapitalizationStrategy capitalizationStrategy = ast.readConfigurationOr(ConfigurationKeys.ACCESSORS_JAVA_BEANS_SPEC_CAPITALIZATION, CapitalizationStrategy.defaultValue());
		return buildAccessorName(prefix, suffix, capitalizationStrategy);
	}
	
	/**
	 * @param node Any node (used to fetch config of capitalization strategy).
	 * @param prefix Something like {@code get} or {@code set} or {@code is}.
	 * @param suffix Something like {@code running}.
	 * @return prefix + smartly title-cased suffix. For example, {@code setRunning}.
	 */
	public static String buildAccessorName(LombokNode<?, ?, ?> node, String prefix, String suffix) {
		CapitalizationStrategy capitalizationStrategy = node.getAst().readConfigurationOr(ConfigurationKeys.ACCESSORS_JAVA_BEANS_SPEC_CAPITALIZATION, CapitalizationStrategy.defaultValue());
		return buildAccessorName(prefix, suffix, capitalizationStrategy);
	}
	
	/**
	 * @param prefix Something like {@code get} or {@code set} or {@code is}.
	 * @param suffix Something like {@code running}.
	 * @param capitalizationStrategy Which strategy to use to capitalize the name part.
	 */
	private static String buildAccessorName(String prefix, String suffix, CapitalizationStrategy capitalizationStrategy) {
		if (suffix.length() == 0) return prefix;
		if (prefix.length() == 0) return suffix;
		return prefix + capitalizationStrategy.capitalize(suffix);
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
	
	/** Matches any of the 8 primitive wrapper names, such as {@code Boolean}. */
	private static final Pattern PRIMITIVE_WRAPPER_TYPE_NAME_PATTERN = Pattern.compile("^(?:java\\.lang\\.)?(?:Boolean|Byte|Short|Integer|Long|Float|Double|Character)$");

	public static int defaultEqualsAndHashcodeIncludeRank(String typeName) {
		// Modification in this code should be documented
		// 1. In the changelog this should be marked as an INPROBABLE BREAKING CHANGE, since the hashcode will change
		// 2. In the javadoc of EqualsAndHashcode.Include#rank
		if (JavaIdentifiers.isPrimitive(typeName)) return 1000;
		if (PRIMITIVE_WRAPPER_TYPE_NAME_PATTERN.matcher(typeName).matches()) return 800;
		return 0;
	}
	
	private static final Pattern SECTION_FINDER = Pattern.compile("^\\s*\\**\\s*[-*][-*]+\\s*([GS]ETTER|WITH(?:ER)?)\\s*[-*][-*]+\\s*\\**\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static final Pattern LINE_BREAK_FINDER = Pattern.compile("(\\r?\\n)?");
	
	public enum JavadocTag {
		PARAM("@param(?:eter)?"),
		RETURN("@returns?");
		
		private Pattern pattern;
		
		JavadocTag(String regexpFragment) {
			pattern = Pattern.compile("\\s?^[ \\t]*\\**[ \\t]*" + regexpFragment + ".*?(?=(\\s^\\s*\\**\\s*@|\\Z))", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
	}
	
	public static String stripLinesWithTagFromJavadoc(String javadoc, JavadocTag... tags) {
		if (javadoc == null || javadoc.isEmpty()) return javadoc;
		String result = javadoc;
		for (JavadocTag tag : tags) {
			result = tag.pattern.matcher(result).replaceAll("").trim();
		}
		return result;
	}
	
	public static String stripSectionsFromJavadoc(String javadoc) {
		if (javadoc == null || javadoc.isEmpty()) return javadoc;
		Matcher sectionMatcher = SECTION_FINDER.matcher(javadoc);
		if (!sectionMatcher.find()) return javadoc;
		
		return javadoc.substring(0, sectionMatcher.start());
	}
	
	public static String getJavadocSection(String javadoc, String sectionNameSpec) {
		if (javadoc == null || javadoc.isEmpty()) return null;
		String[] sectionNames = sectionNameSpec.split("\\|");
		Matcher sectionMatcher = SECTION_FINDER.matcher(javadoc);
		Matcher lineBreakMatcher = LINE_BREAK_FINDER.matcher(javadoc);
		int sectionStart = -1;
		int sectionEnd = -1;
		while (sectionMatcher.find()) {
			boolean found = false;
			for (String sectionName : sectionNames) if (sectionMatcher.group(1).equalsIgnoreCase(sectionName)) {
				found = true;
				break;
			}
			if (found) {
				lineBreakMatcher.find(sectionMatcher.end());
				sectionStart = lineBreakMatcher.end();
			} else if (sectionStart != -1) {
				sectionEnd = sectionMatcher.start();
			}
		}
		
		if (sectionStart != -1) {
			if (sectionEnd != -1) return javadoc.substring(sectionStart, sectionEnd);
			return javadoc.substring(sectionStart);
		}
		
		return null;
	}
	
	private static final Pattern FIND_RETURN = Pattern.compile("^\\s*\\**\\s*@returns?\\s+.*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	
	public static String addReturnsThisIfNeeded(String in) {
		if (in != null && FIND_RETURN.matcher(in).find()) return in;
		
		return addJavadocLine(in, "@return {@code this}.");
	}
	
	public static String addReturnsUpdatedSelfIfNeeded(String in) {
		if (in != null && FIND_RETURN.matcher(in).find()) return in;
		
		return addJavadocLine(in, "@return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).");
	}
	
	public static String addJavadocLine(String in, String line) {
		if (in == null) return line;
		if (in.endsWith("\n")) return in + line;
		return in + "\n" + line;
	}

	public static String getParamJavadoc(String methodComment, String param) {
		if (methodComment == null || methodComment.isEmpty()) return methodComment;
		Pattern pattern = Pattern.compile("@param " + param + " .+?(?=^ ?@|\\z)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(methodComment);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
	public static String getConstructorJavadocHeader(String typeName) {
		return "Creates a new {@code " + typeName + "} instance.\n\n";
	}
	
	public static String getConstructorParameterJavadoc(String paramName, String fieldJavadoc) {
		String fieldBaseJavadoc = stripSectionsFromJavadoc(fieldJavadoc);
		
		String paramJavadoc = getParamJavadoc(fieldBaseJavadoc, paramName);
		if (paramJavadoc != null) {
			return paramJavadoc;
		}
		
		String javadocWithoutTags = stripLinesWithTagFromJavadoc(fieldBaseJavadoc, JavadocTag.PARAM, JavadocTag.RETURN);
		if (javadocWithoutTags != null) {
			return "@param " + paramName + " " + javadocWithoutTags;
		}
		return null;
	}
}
