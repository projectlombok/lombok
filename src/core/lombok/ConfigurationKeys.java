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
package lombok;

import java.util.List;

import lombok.core.configuration.CallSuperType;
import lombok.core.configuration.CapitalizationStrategy;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.configuration.ConfigurationKey;
import lombok.core.configuration.FlagUsageType;
import lombok.core.configuration.IdentifierName;
import lombok.core.configuration.LogDeclaration;
import lombok.core.configuration.NullAnnotationLibrary;
import lombok.core.configuration.NullCheckExceptionType;
import lombok.core.configuration.TypeName;

/**
 * A container class containing all lombok configuration keys that do not belong to a specific annotation.
 */
public class ConfigurationKeys {
	private ConfigurationKeys() {}
	
	// ##### main package features #####
	
	// ----- global -----
	
	/**
	 * lombok configuration: {@code dangerousconfig.lombok.disable} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok is disabled entirely.
	 */
	public static final ConfigurationKey<Boolean> LOMBOK_DISABLE = new ConfigurationKey<Boolean>("dangerousconfig.lombok.disable", "Disables lombok transformers. It does not flag any lombok mentions (so, @Cleanup silently does nothing), and does not disable patched operations in eclipse either. Don't use this unless you know what you're doing. (default: false).", true) {};
	
	/**
	 * lombok configuration: {@code lombok.addGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @javax.annotation.Generated("lombok")} on all fields, methods, and types that are generated, unless {@code lombok.addJavaxGeneratedAnnotation} is set.
	 * <br>
	 * <em>BREAKING CHANGE</em>: Starting with lombok v1.16.20, defaults to {@code false} instead of {@code true}, as this annotation is broken in JDK9.
	 * 
	 * @see ConfigurationKeys#ADD_JAVAX_GENERATED_ANNOTATIONS
	 * @see ConfigurationKeys#ADD_JAKARTA_GENERATED_ANNOTATIONS
	 * @see ConfigurationKeys#ADD_LOMBOK_GENERATED_ANNOTATIONS
	 * @deprecated Since version 1.16.14, use {@link #ADD_JAVAX_GENERATED_ANNOTATIONS} instead.
	 */
	@Deprecated
	public static final ConfigurationKey<Boolean> ADD_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: false). Deprecated, use 'lombok.addJavaxGeneratedAnnotation' instead.") {};
	
	/**
	 * lombok configuration: {@code lombok.addJavaxGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @javax.annotation.Generated("lombok")} on all fields, methods, and types that are generated.
	 * <br>
	 * <em>BREAKING CHANGE</em>: Starting with lombok v1.16.20, defaults to {@code false} instead of {@code true}, as this annotation is broken in JDK9.
	 */
	public static final ConfigurationKey<Boolean> ADD_JAVAX_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addJavaxGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: follow lombok.addGeneratedAnnotation).") {};
	
	/**
	 * lombok configuration: {@code lombok.addJakartaGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @jakarta.annotation.Generated("lombok")} on all fields, methods, and types that are generated.
	 */
	public static final ConfigurationKey<Boolean> ADD_JAKARTA_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addJakartaGeneratedAnnotation", "Generate @jakarta.annotation.Generated on all generated code (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.addLombokGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @lombok.Generated} on all fields, methods, and types that are generated.
	 */
	public static final ConfigurationKey<Boolean> ADD_LOMBOK_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addLombokGeneratedAnnotation", "Generate @lombok.Generated on all generated code (default: true).") {};
	
	/**
	 * lombok configuration: {@code lombok.extern.findbugs.addSuppressFBWarnings} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code edu.umd.cs.findbugs.annotations.SuppressFBWarnings} on all fields, methods, and types that are generated.
	 * 
	 * NB: If you enable this option, findbugs must be on the source or classpath, or you'll get errors that the type {@code SuppressFBWarnings} cannot be found.
	 */
	public static final ConfigurationKey<Boolean> ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.extern.findbugs.addSuppressFBWarnings", "Generate @edu.umd.cs.findbugs.annotations.SuppressFBWarnings on all generated code (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.addSuppressWarnings} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @java.lang.SuppressWarnings("all")} on all fields, methods, and types that are generated.
	 */
	public static final ConfigurationKey<Boolean> ADD_SUPPRESSWARNINGS_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addSuppressWarnings", "Generate @java.lang.SuppressWarnings(\"all\") on all generated code (default: true).") {};
	
	/**
	 * lombok configuration: {@code lombok.addNullAnnotations = }one of: [{@code none}, {@code javax}, {@code eclipse}, {@code jetbrains}, {@code netbeans}, {@code androidx}, {@code android.support}, {@code checkerframework}, {@code findbugs}, {@code spring}, {@code JML}, {@code jspecify} or a custom set of fully qualified annotation types].
	 * 
	 * Lombok generally copies relevant nullity annotations from your source code to the right places. However, sometimes lombok generates code where the nullability of some node is not dependent on something in your source code. You can configure lombok to add an appropriate nullity annotation in this case.<ul>
	 * <li>{@code none} (the default) - no annotations are added.</li>
	 * <li>{@code javax} - The annotations {@code javax.annotation.NonNull} and {@code javax.annotation.Nullable} are used.</li>
	 * <li>{@code jakarta} - The annotations {@code jakarta.annotation.NonNull} and {@code jakarta.annotation.Nullable} are used.</li>
	 * <li>{@code eclipse} - The annotations {@code org.eclipse.jdt.annotation.NonNull} and {@code org.eclipse.jdt.annotation.Nullable} are used.</li>
	 * <li>{@code jetbrains} - The annotations {@code org.jetbrains.annotations.NotNull} and {@code org.jetbrains.annotations.Nullable} are used.</li>
	 * <li>{@code netbeans} - The annotations {@code org.netbeans.api.annotations.common.NonNull} and {@code org.netbeans.api.annotations.common.NullAllowed} are used.</li>
	 * <li>{@code androidx} - The annotations {@code androidx.annotation.NonNull} and {@code androidx.annotation.Nullable} are used.</li>
	 * <li>{@code android.support} - The annotations {@code android.support.annotation.NonNull} and {@code android.support.annotation.Nullable} are used.</li>
	 * <li>{@code checkerframework} - The annotations {@code org.checkerframework.checker.nullness.qual.NonNull} and {@code org.checkerframework.checker.nullness.qual.Nullable} are used.</li>
	 * <li>{@code findbugs} - The annotations {@code edu.umd.cs.findbugs.annotations.NonNull} and {@code edu.umd.cs.findbugs.annotations.Nullable} are used.</li>
	 * <li>{@code spring} - The annotations {@code org.springframework.lang.NonNull} and {@code org.springframework.lang.Nullable} are used.</li>
	 * <li>{@code jml} - The annotations {@code org.jmlspecs.annotation.NonNull} and {@code org.jmlspecs.annotation.Nullable} are used.</li>
	 * <li>{@code jspecify} - The annotations {@code org.jspecify.annotations.NonNull} and {@code org.jspecify.annotations.Nullable} are used.</li>
	 * <li><code>CUSTOM:<em>fully.qualified.nonnull.annotation</em>:<em>fully.qualified.nullable.annotation</em></code> to configure your own types; the nullable annotation (and the colon) are optional.</li>
	 * </ul>
	 * <p>
	 * Lombok will not put these annotations on the classpath for you; your project must be set up such that these annotations are available.
	 * <p>
	 * Current features which use this configuration:<ul>
	 * <li>{@code @Builder.Singular} makes methods that accept a collection, all of which must be added. The parameter to this 'plural form' method is annotated.</li>
	 * </ul>
	 */
	public static final ConfigurationKey<NullAnnotationLibrary> ADD_NULL_ANNOTATIONS = new ConfigurationKey<NullAnnotationLibrary>("lombok.addNullAnnotations", "Generate some style of null annotation for generated code where this is relevant. (default: none).") {};
	
	// ----- *ArgsConstructor -----
	
	/**
	 * lombok configuration: {@code lombok.anyConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @AllArgsConstructor}, {@code @RequiredArgsConstructor}, or {@code @NoArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ANY_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.anyConstructor.flagUsage", "Emit a warning or error if any of the XxxArgsConstructor annotations are used.") {};
	
	/**
	 * lombok configuration: {@code lombok.anyConstructor.suppressConstructorProperties} = {@code true} | {@code false}.
	 * 
	 * If {@code false} or this configuration is omitted, all generated constructors with at least 1 argument get a {@code @ConstructorProperties}.
	 * To suppress the generation of it, set this configuration to {@code true}.
	 * 
	 * NB: GWT projects, and probably android projects, should explicitly set this key to {@code true} for the entire project.
	 * 
	 * <br>
	 * <em>BREAKING CHANGE</em>: Starting with lombok v1.16.20, defaults to {@code true} instead of {@code false}, as {@code @ConstructorProperties} requires extra modules in JDK9.
	 * 
	 * @see ConfigurationKeys#ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES
	 * @deprecated Since version 2.0, use {@link #ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES} instead.
	 */
	@Deprecated
	public static final ConfigurationKey<Boolean> ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES = new ConfigurationKey<Boolean>("lombok.anyConstructor.suppressConstructorProperties", "Suppress the generation of @ConstructorProperties for generated constructors (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.anyConstructor.addConstructorProperties} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, all generated constructors with at least 1 argument get a {@code @ConstructorProperties}.
	 * 
	 */
	public static final ConfigurationKey<Boolean> ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES = new ConfigurationKey<Boolean>("lombok.anyConstructor.addConstructorProperties", "Generate @ConstructorProperties for generated constructors (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.allArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @AllArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ALL_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.allArgsConstructor.flagUsage", "Emit a warning or error if @AllArgsConstructor is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.noArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @NoArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> NO_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.noArgsConstructor.flagUsage", "Emit a warning or error if @NoArgsConstructor is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.noArgsConstructor.extraPrivate} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, @Data and @Value will also generate a private no-args constructor, if there isn't already one, setting all fields to their default values.
	 */
	public static final ConfigurationKey<Boolean> NO_ARGS_CONSTRUCTOR_EXTRA_PRIVATE = new ConfigurationKey<Boolean>("lombok.noArgsConstructor.extraPrivate", "Generate a private no-args constructor for @Data and @Value (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.requiredArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @RequiredArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.requiredArgsConstructor.flagUsage", "Emit a warning or error if @RequiredArgsConstructor is used.") {};
	
	// ##### Beanies #####
	
	// ----- Data -----
	
	/**
	 * lombok configuration: {@code lombok.data.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Data} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> DATA_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.data.flagUsage", "Emit a warning or error if @Data is used.") {};
	
	// ----- Value -----
	
	/**
	 * lombok configuration: {@code lombok.value.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Value} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> VALUE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.value.flagUsage", "Emit a warning or error if @Value is used.") {};
	
	// ----- Getter -----
	
	/**
	 * lombok configuration: {@code lombok.getter.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Getter} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> GETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.getter.flagUsage", "Emit a warning or error if @Getter is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.getter.lazy.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Getter(lazy=true)} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> GETTER_LAZY_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.getter.lazy.flagUsage", "Emit a warning or error if @Getter(lazy=true) is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.getter.noIsPrefix} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, booleans getters are both referred to, and generated as {@code getFieldName()}. If {@code false} (the default), the javabean-standard {@code isFieldName()} is generated / used instead.
	 *
	 */
	public static final ConfigurationKey<Boolean> GETTER_CONSEQUENT_BOOLEAN = new ConfigurationKey<Boolean>("lombok.getter.noIsPrefix", "If true, generate and use getFieldName() for boolean getters instead of isFieldName().") {};
	
	// ----- Setter -----
	
	/**
	 * lombok configuration: {@code lombok.setter.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Setter} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.setter.flagUsage", "Emit a warning or error if @Setter is used.") {};
	
	// ----- EqualsAndHashCode -----
	
	/**
	 * lombok configuration: {@code lombok.equalsAndHashCode.doNotUseGetters} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @EqualsAndHashCode} that explicitly defines the {@code doNotUseGetters} option, this value is used (default = false).
	 */
	public static final ConfigurationKey<Boolean> EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS = new ConfigurationKey<Boolean>("lombok.equalsAndHashCode.doNotUseGetters", "Don't call the getters but use the fields directly in the generated equals and hashCode method (default = false).") {};
	
	/**
	 * lombok configuration: {@code lombok.equalsAndHashCode.callSuper} = {@code call} | {@code ignore} | {@code warn}.
	 * 
	 * For any class with an {@code @EqualsAndHashCode} annotation which extends a class other than {@code java.lang.Object}, should a call to superclass's implementation of {@code equals} and {@code hashCode} be included in the generated methods? (Default = warn)
	 */
	public static final ConfigurationKey<CallSuperType> EQUALS_AND_HASH_CODE_CALL_SUPER = new ConfigurationKey<CallSuperType>("lombok.equalsAndHashCode.callSuper", "When generating equals and hashCode for classes that extend something (other than Object), either automatically take into account superclass implementation (call), or don't (skip), or warn and don't (warn). (default = warn).") {};
	
	/**
	 * lombok configuration: {@code lombok.equalsAndHashCode.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @EqualsAndHashCode} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> EQUALS_AND_HASH_CODE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.equalsAndHashCode.flagUsage", "Emit a warning or error if @EqualsAndHashCode is used.") {};
	
	// ----- ToString -----
	
	/**
	 * lombok configuration: {@code lombok.toString.doNotUseGetters} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @ToString} that explicitly defines the {@code doNotUseGetters} option, this value is used  (default = false).
	 */
	public static final ConfigurationKey<Boolean> TO_STRING_DO_NOT_USE_GETTERS = new ConfigurationKey<Boolean>("lombok.toString.doNotUseGetters", "Don't call the getters but use the fields directly in the generated toString method (default = false).") {};
	
	/**
	 * lombok configuration: {@code lombok.toString.callSuper} = {@code call} | {@code ignore} | {@code warn}.
	 * 
	 * For any class with an {@code @ToString} annotation which extends a class other than {@code java.lang.Object}, should a call to superclass's implementation of {@code toString} be included in the generated method? (Default = skip)
	 */
	public static final ConfigurationKey<CallSuperType> TO_STRING_CALL_SUPER = new ConfigurationKey<CallSuperType>("lombok.toString.callSuper", "When generating toString for classes that extend something (other than Object), either automatically take into account superclass implementation (call), or don't (skip), or warn and don't (warn). (default = skip).") {};
	
	/**
	 * lombok configuration: {@code lombok.toString.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @ToString} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> TO_STRING_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.toString.flagUsage", "Emit a warning or error if @ToString is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.toString.includeFieldNames} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @ToString} that explicitly defines the {@code includeFieldNames} option, this value is used  (default = true).
	 */
	public static final ConfigurationKey<Boolean> TO_STRING_INCLUDE_FIELD_NAMES = new ConfigurationKey<Boolean>("lombok.toString.includeFieldNames", "Include the field names in the generated toString method (default = true).") {};
	
	/**
	 * lombok configuration: {@code lombok.toString.onlyExplicitlyIncluded} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, require a {@code @ToString.Include} annotation on any fields/no-args methods you want to include in lombok's generated `@ToString` method. Otherwise, every (non-static, non-dollar-named) field is included by default  (default = false).
	 */
	public static final ConfigurationKey<Boolean> TO_STRING_ONLY_EXPLICITLY_INCLUDED = new ConfigurationKey<Boolean>("lombok.toString.onlyExplicitlyIncluded", "Include only fields/methods explicitly marked with @ToString.Include. Otherwise, include all non-static, non-dollar-named fields (default = false).") {};
	
	// ----- Builder -----
	
	/**
	 * lombok configuration: {@code lombok.builder.classNames} = &lt;String: aJavaIdentifier (optionally with a star as placeholder for the type name)&gt; (Default: {@code *Builder}).
	 * 
	 * For any usage of the {@code @Builder} annotation without an explicit {@code builderClassName} parameter, this value is used to determine the name of the builder class to generate (or to adapt if such an inner class already exists).
	 */
	public static final ConfigurationKey<String> BUILDER_CLASS_NAME = new ConfigurationKey<String>("lombok.builder.className", "Default name of the generated builder class. A * is replaced with the name of the relevant type (default = *Builder).") {};
	
	/**
	 * lombok configuration: {@code lombok.builder.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Builder} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> BUILDER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.builder.flagUsage", "Emit a warning or error if @Builder is used.") {};
	
	// ----- Singular -----
	
	/**
	 * lombok configuration: {@code lombok.singular.useGuava} = {@code true} | {@code false}.
	 * 
	 * If explicitly set to {@code true}, guava's {@code ImmutableList} etc are used to implement the immutable collection datatypes generated by @Singular @Builder for fields/parameters of type {@code java.util.List} and such.
	 * By default, unmodifiable-wrapped versions of {@code java.util} types are used.
	 */
	public static final ConfigurationKey<Boolean> SINGULAR_USE_GUAVA = new ConfigurationKey<Boolean>("lombok.singular.useGuava", "Generate backing immutable implementations for @Singular on java.util.* types by using guava's ImmutableList, etc. Normally java.util's mutable types are used and wrapped to make them immutable.") {}; 
	
	/**
	 * lombok configuration: {@code lombok.singular.auto} = {@code true} | {@code false}.
	 * 
	 * By default or if explicitly set to {@code true}, lombok will attempt to automatically singularize the name of your variable/parameter when using {@code @Singular}; the name is assumed to be written in english, and a plural. If explicitly to {@code false}, you must always specify the singular form; this is especially useful if your identifiers are in a foreign language.
	 */
	public static final ConfigurationKey<Boolean> SINGULAR_AUTO = new ConfigurationKey<Boolean>("lombok.singular.auto", "If true (default): Automatically singularize the assumed-to-be-plural name of your variable/parameter when using @Singular.") {}; 
	
	// ##### Standalones #####
	
	// ----- Cleanup -----
	
	/**
	 * lombok configuration: {@code lombok.cleanup.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Cleanup} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> CLEANUP_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.cleanup.flagUsage", "Emit a warning or error if @Cleanup is used.") {};
	
	// ----- Delegate -----
	
	/**
	 * lombok configuration: {@code lombok.delegate.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Delegate} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> DELEGATE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.delegate.flagUsage", "Emit a warning or error if @Delegate is used.") {};
	
	// ----- NonNull -----
	
	/**
	 * lombok configuration: {@code lombok.nonNull.exceptionType} = one of: [{@code IllegalArgumentException}, {@code NullPointerException}, {@code JDK}, {@code Guava}, or {@code Assertion}].
	 * 
	 * Sets the behavior of the generated nullcheck if {@code @NonNull} is applied to a method parameter, and a caller passes in {@code null}.<ul>
	 * <li>If the chosen configuration is {@code NullPointerException} (the default), or {@code IllegalArgumentException}, that exception type is a thrown, with as message <code><em>field-name</em> is marked non-null but is null</code>.</li>
	 * <li>If the chosen configuration is {@code Assert}, then an {@code assert} statement is generated. This means an {@code AssertionError} will be thrown if assertions are on (VM started with {@code -ea} parameter), and nothing happens if not.</li>
	 * <li>If the chosen configuration is {@code JDK}, a call to {@code java.util.Objects.requireNonNull} is generated with the fieldname passed along (which throws {@code NullPointerException}).</li>
	 * <li>If the chosen configuration is {@code Guava}, a call to {@code com.google.common.base.Preconditions.checkNotNull} is generated with the fieldname passed along (which throws {@code NullPointerException}).</li>
	 * </ul>
	 * NB: The chosen nullcheck style is also used by {@code @Builder}'s {@code @Singular} annotation to check any collections passed to a plural-form method.
	 */
	public static final ConfigurationKey<NullCheckExceptionType> NON_NULL_EXCEPTION_TYPE = new ConfigurationKey<NullCheckExceptionType>("lombok.nonNull.exceptionType", "The type of the exception to throw if a passed-in argument is null (Default: NullPointerException).") {};
	
	/**
	 * lombok configuration: {@code lombok.nonNull.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @NonNull} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> NON_NULL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.nonNull.flagUsage", "Emit a warning or error if @NonNull is used.") {};
	
	// ----- SneakyThrows -----
	
	/**
	 * lombok configuration: {@code lombok.sneakyThrows.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @SneakyThrows} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SNEAKY_THROWS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.sneakyThrows.flagUsage", "Emit a warning or error if @SneakyThrows is used.") {};
	
	// ----- Synchronized -----
	
	/**
	 * lombok configuration: {@code lombok.synchronized.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Synchronized} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SYNCHRONIZED_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.synchronized.flagUsage", "Emit a warning or error if @Synchronized is used.") {};
	
	// ----- val -----
	
	/**
	 * lombok configuration: {@code lombok.val.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code val} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> VAL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.val.flagUsage", "Emit a warning or error if 'val' is used.") {};
	public static final ConfigurationKey<FlagUsageType> VAR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.var.flagUsage", "Emit a warning or error if 'var' is used.") {};

	// ----- With -----
	
	/**
	 * lombok configuration: {@code lombok.with.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @With} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> WITH_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.with.flagUsage", "Emit a warning or error if @With is used.") {};
	
	// ##### Extern #####
	
	// ----- Logging -----
	/**
	 * lombok configuration: {@code lombok.log.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of any of the log annotations in {@code lombok.extern}{@code @Slf4j} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_ANY_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.flagUsage", "Emit a warning or error if any of the log annotations is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.apacheCommons.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @CommonsLog} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_COMMONS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.apacheCommons.flagUsage", "Emit a warning or error if @CommonsLog is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.javaUtilLogging.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Log} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_JUL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.javaUtilLogging.flagUsage", "Emit a warning or error if @Log is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.log4j.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Log4j} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_LOG4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.log4j.flagUsage", "Emit a warning or error if @Log4j is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.log4j2.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Log4j2} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_LOG4J2_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.log4j2.flagUsage", "Emit a warning or error if @Log4j2 is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.slf4j.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Slf4j} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_SLF4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.slf4j.flagUsage", "Emit a warning or error if @Slf4j is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.xslf4j.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @XSlf4j} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_XSLF4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.xslf4j.flagUsage", "Emit a warning or error if @XSlf4j is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.jbosslog.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @JBossLog} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_JBOSSLOG_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.jbosslog.flagUsage", "Emit a warning or error if @JBossLog is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.flogger.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Flogger} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_FLOGGER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.flogger.flagUsage", "Emit a warning or error if @Flogger is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.fieldName} = &lt;String: aJavaIdentifier&gt; (Default: {@code log}).
	 * 
	 * If set the various log annotations (which make a log field) will use the stated identifier instead of {@code log} as a name.
	 */
	public static final ConfigurationKey<IdentifierName> LOG_ANY_FIELD_NAME = new ConfigurationKey<IdentifierName>("lombok.log.fieldName", "Use this name for the generated logger fields (default: 'log').") {};
	
	/**
	 * lombok configuration: {@code lombok.log.fieldIsStatic} = {@code true} | {@code false}.
	 * 
	 * If not set, or set to {@code true}, the log field generated by the various log annotations will be {@code static}.
	 * 
	 * If set to {@code false}, these will be generated as instance fields instead.
	 */
	public static final ConfigurationKey<Boolean> LOG_ANY_FIELD_IS_STATIC = new ConfigurationKey<Boolean>("lombok.log.fieldIsStatic", "Make the generated logger fields static (default: true).") {};
	
	// ----- Custom Logging -----
	
	/**
	 * lombok configuration: {@code lombok.log.custom.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @CustomLog} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOG_CUSTOM_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.custom.flagUsage", "Emit a warning or error if @CustomLog is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.log.custom.declaration} = &lt;logDeclaration string&gt;.
	 * 
	 * The log declaration must follow the pattern: 
	 * <br>
	 * {@code [LoggerType ]LoggerFactoryType.loggerFactoryMethod(loggerFactoryMethodParams)[(loggerFactoryMethodParams)]}
	 * <br>
	 * It consists of:
	 * <ul>
	 * <li>Optional fully qualified logger type, e.g. {@code my.cool.Logger}, followed by space. If not specified, it defaults to the <em>LoggerFactoryType</em>.
	 * <li>Fully qualified logger factory type, e.g. {@code my.cool.LoggerFactory}, followed by dot.
	 * <li>Factory method, e.g. {@code createLogger}. This must be a {@code public static} method in the <em>LoggerFactoryType</em>.
	 * <li>At least one definition of factory method parameters, e.g. {@code ()} or {@code (TOPIC,TYPE)}. The format inside the parentheses is a comma-separated list of parameter kinds.<br>
	 * The allowed parameters are: {@code TYPE} | {@code NAME} | {@code TOPIC} | {@code NULL}.<br>
	 * There can be at most one parameter definition with {@code TOPIC} and at most one without {@code TOPIC}. You can specify both.
	 * </ul>
	 * 
	 * An example: {@code my.cool.Logger my.cool.LoggerFactory.createLogger(TYPE)(TYPE,TOPIC)}<br>
	 * If no topic is provided in the usage of {@code @CustomLog}, the above will invoke {@code LoggerFactory}'s {@code createLogger} method, passing in the type as a {@code java.lang.Class} variable.<br>
	 * If a topic is provided, the overload of that method is invoked with 2 parameters: First the type (as {@code Class}), then the topic (as {@code String}).
	 * <p>
	 * If this configuration key is not set, any usage of {@code @CustomLog} will result in an error.
	 */
	public static final ConfigurationKey<LogDeclaration> LOG_CUSTOM_DECLARATION = new ConfigurationKey<LogDeclaration>("lombok.log.custom.declaration", "Define the generated custom logger field.") {};

	// ##### Experimental #####
	
	/**
	 * lombok configuration: {@code lombok.experimental.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of any experimental features (from package {@code lombok.experimental}) that haven't been
	 * promoted to a main feature results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> EXPERIMENTAL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.experimental.flagUsage", "Emit a warning or error if an experimental feature is used.") {};
	
	// ----- Accessors -----
	
	/**
	 * lombok configuration: {@code lombok.accessors.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Accessors} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ACCESSORS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.accessors.flagUsage", "Emit a warning or error if @Accessors is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.accessors.prefix} += &lt;String: prefix&gt;.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code prefix} option, this list of prefixes is used.
	 */
	public static final ConfigurationKey<List<String>> ACCESSORS_PREFIX = new ConfigurationKey<List<String>>("lombok.accessors.prefix", "Strip this field prefix, like 'f' or 'm_', from the names of generated getters, setters, and with-ers.") {};
	
	/**
	 * lombok configuration: {@code lombok.accessors.chain} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code chain} option, this value is used (default = false).
	 */
	public static final ConfigurationKey<Boolean> ACCESSORS_CHAIN = new ConfigurationKey<Boolean>("lombok.accessors.chain", "Generate setters that return 'this' instead of 'void' (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.accessors.fluent} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code fluent} option, this value is used (default = false).
	 */
	public static final ConfigurationKey<Boolean> ACCESSORS_FLUENT = new ConfigurationKey<Boolean>("lombok.accessors.fluent", "Generate getters and setters using only the field name (no get/set prefix) (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.accessors.makeFinal} = {@code true} | {@code false}.
	 * 
	 * Unless an explicit {@code @Accessors} that explicitly defines the {@code makeFinal} option, this value is used (default = false).
	 */
	public static final ConfigurationKey<Boolean> ACCESSORS_MAKE_FINAL = new ConfigurationKey<Boolean>("lombok.accessors.makeFinal", "Generate getters, setters and with-ers with the 'final' modifier (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.accessors.capitalization} = {@code basic} | {@code beanspec}.
	 * 
	 * Which capitalization rule is used to turn field names into getter/setter/with names and vice versa for field names that start with 1 lowercase letter, then 1 uppercase letter.
	 * basic = {@code uShape} becomes {@code getUShape}, beanspec = {@code uShape} becomes {@code getuShape} (default = basic).
	 */
	public static final ConfigurationKey<CapitalizationStrategy> ACCESSORS_JAVA_BEANS_SPEC_CAPITALIZATION = new ConfigurationKey<CapitalizationStrategy>("lombok.accessors.capitalization", "Which capitalization strategy to use when converting field names to accessor names and vice versa (default: basic).") {};
	
	
	// ----- ExtensionMethod -----
	
	/**
	 * lombok configuration: {@code lombok.extensionMethod.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @ExtensionMethod} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> EXTENSION_METHOD_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.extensionMethod.flagUsage", "Emit a warning or error if @ExtensionMethod is used.") {};
	
	// ----- FieldDefaults -----
	
	/**
	 * lombok configuration: {@code lombok.fieldDefaults.defaultPrivate} = {@code true} | {@code false}.
	 * 
	 * If set to {@code true} <em>any</em> field without an access modifier or {@code @PackagePrivate} is marked as {@code private} by lombok, in all source files compiled.
	 */
	public static final ConfigurationKey<Boolean> FIELD_DEFAULTS_PRIVATE_EVERYWHERE = new ConfigurationKey<Boolean>("lombok.fieldDefaults.defaultPrivate", "If true, fields without any access modifier, in any file (lombok annotated or not) are marked as private. Use @PackagePrivate or an explicit modifier to override this.") {};
	
	/**
	 * lombok configuration: {@code lombok.fieldDefaults.defaultFinal} = {@code true} | {@code false}.
	 * 
	 * If set to {@code true} <em>any</em> field without {@code @NonFinal} is marked as {@code final} by lombok, in all source files compiled.
	 */
	public static final ConfigurationKey<Boolean> FIELD_DEFAULTS_FINAL_EVERYWHERE = new ConfigurationKey<Boolean>("lombok.fieldDefaults.defaultFinal", "If true, fields, in any file (lombok annotated or not) are marked as final. Use @NonFinal to override this.") {};
	
	/**
	 * lombok configuration: {@code lombok.fieldDefaults.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @FieldDefaults} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> FIELD_DEFAULTS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.fieldDefaults.flagUsage", "Emit a warning or error if @FieldDefaults is used.") {};
	
	// ----- Helper -----
	
	/**
	 * lombok configuration: {@code lombok.helper.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Helper} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> HELPER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.helper.flagUsage", "Emit a warning or error if @Helper is used.") {};
	
	// ----- LOCKED -----
	
	/**
	 * lombok configuration: {@code lombok.locked.flagUsage} = {@code WARNING} | {@code ERROR}.
	 *
	 * If set, <em>any</em> usage of {@code @Locked} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> LOCKED_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.locked.flagUsage", "Emit a warning or error if @Locked is used.") {};
	
	// ----- onX -----
	
	/**
	 * lombok configuration: {@code lombok.onX.flagUsage} = {@code WARNING} | {@code ERROR}.
	 *
	 * If set, <em>any</em> usage of {@code onX} results in a warning / error.
	 * <br>
	 * Specifically, this flags usage of {@code @Getter(onMethod=...)}, {@code @Setter(onParam=...)}, {@code @Setter(onMethod=...)}, {@code @XArgsConstructor(onConstructor=...)}.
	 */
	public static final ConfigurationKey<FlagUsageType> ON_X_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.onX.flagUsage", "Emit a warning or error if onX is used.") {};
	
	// ----- UtilityClass -----
	
	/**
	 * lombok configuration: {@code lombok.utilityClass.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @UtilityClass} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> UTILITY_CLASS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.utilityClass.flagUsage", "Emit a warning or error if @UtilityClass is used.") {};
	
	// ----- FieldNameConstants -----
	/**
	 * lombok configuration: {@code lombok.fieldNameConstants.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @FieldNameConstants} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> FIELD_NAME_CONSTANTS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.fieldNameConstants.flagUsage", "Emit a warning or error if @FieldNameConstants is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.fieldNameConstants.innerTypeName} = &lt;String: AValidJavaTypeName&gt; (Default: {@code Fields}).
	 * 
	 * The names of the constants generated by {@code @FieldNameConstants} will be prefixed with this value.
	 */
	public static final ConfigurationKey<IdentifierName> FIELD_NAME_CONSTANTS_INNER_TYPE_NAME = new ConfigurationKey<IdentifierName>("lombok.fieldNameConstants.innerTypeName", "The default name of the inner type generated by @FieldNameConstants. (default: 'Fields').") {};
	
	/**
	 * lombok configuration: {@code lombok.fieldNameConstants.uppercase} = {@code true} | {@code false}.
	 * 
	 * If true, names of constants generated by {@code @FieldNameConstants} will be UPPER_CASED_LIKE_A_CONSTANT. (Default: {@code false}).
	 */
	public static final ConfigurationKey<Boolean> FIELD_NAME_CONSTANTS_UPPERCASE = new ConfigurationKey<Boolean>("lombok.fieldNameConstants.uppercase", "The default name of the constants inside the inner type generated by @FieldNameConstants follow the variable name precisely. If this config key is true, lombok will uppercase them as best it can. (default: false).") {};
	
	// ----- SuperBuilder -----
	
	/**
	 * lombok configuration: {@code lombok.superBuilder.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @SuperBuilder} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SUPERBUILDER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.superBuilder.flagUsage", "Emit a warning or error if @SuperBuilder is used.") {};

	// ----- WithBy -----
	
	/**
	 * lombok configuration: {@code lombok.withBy.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @WithBy} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> WITHBY_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.withBy.flagUsage", "Emit a warning or error if @WithBy is used.") {};

	// ----- Jacksonized -----
	
	/**
	 * lombok configuration: {@code lombok.jacksonized.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Jacksonized} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> JACKSONIZED_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.jacksonized.flagUsage", "Emit a warning or error if @Jacksonized is used.") {};
	
	// ----- Configuration System -----
	
	/**
	 * lombok configuration: {@code config.stopBubbling} = {@code true} | {@code false}.
	 * 
	 * If not set, or set to {@code false}, the configuration system will look for {@code lombok.config} files in the parent directory.
	 * 
	 * If set to {@code true}, no further {@code lombok.config} files will be checked.
	 */
	public static final ConfigurationKey<Boolean> STOP_BUBBLING = new ConfigurationKey<Boolean>("config.stopBubbling", "Tell the configuration system it should stop looking for other configuration files (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.copyableAnnotations} += &lt;TypeName: fully-qualified annotation class name&gt;.
	 *
	 * Copy these annotations to getters, setters, with methods, builder-setters, etc.
	 */
	public static final ConfigurationKey<List<TypeName>> COPYABLE_ANNOTATIONS = new ConfigurationKey<List<TypeName>>("lombok.copyableAnnotations", "Copy these annotations to getters, setters, with methods, builder-setters, etc.") {};
	
	/**
	 * lombok configuration: {@code checkerframework} = {@code true} | {@code false} | &lt;String: MajorVer.MinorVer&gt; (Default: false).
	 * 
	 * If set, lombok will generate appropriate annotations from checkerframework.org on generated code. If set to {@code true}, all relevant annotations from the most recent version of
	 * checkerframework.org that lombok supports will be generated. If set to a specific major/minor version number, only checkerframework annotations introduced on or before the stated
	 * checkerframework.org version will be generated.
	 */
	public static final ConfigurationKey<CheckerFrameworkVersion> CHECKER_FRAMEWORK = new ConfigurationKey<CheckerFrameworkVersion>("checkerframework", "If set with the version of checkerframework.org (in major.minor, or just 'true' for the latest supported version), create relevant checkerframework.org annotations for code lombok generates (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.standardException.flagUsage} = {@code WARNING} | {@code ERROR}.
	 *
	 * If set, <em>any</em> usage of {@code @StandardException} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> STANDARD_EXCEPTION_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.standardException.flagUsage", "Emit a warning or error if @StandardException is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.copyJacksonAnnotationsToAccessors} = {@code true} | {@code false}.
	 *
	 * If {@code true}, copy certain Jackson annotations from a field to its corresponding accessors (getter/setters). This was the behavior from lombok 1.18.16 to 1.18.38.
	 * However, it turned out to be harmful in certain situations. Thus, the default is now {@code false}.
	 */
	public static final ConfigurationKey<Boolean> COPY_JACKSON_ANNOTATIONS_TO_ACCESSORS = new ConfigurationKey<Boolean>("lombok.copyJacksonAnnotationsToAccessors", "Copy Jackson annotations from fields to the corresponding getters and setters.") {};
}
