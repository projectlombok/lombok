/*
 * Copyright (C) 2013-2017 The Project Lombok Authors.
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
import lombok.core.configuration.ConfigurationKey;
import lombok.core.configuration.FlagUsageType;
import lombok.core.configuration.NullCheckExceptionType;

/**
 * A container class containing all lombok configuration keys that do not belong to a specific annotation.
 */
public class ConfigurationKeys {
	private ConfigurationKeys() {}
	
	// ##### main package features #####
	
	// ----- global -----
	
	/**
	 * lombok configuration: {@code lombok.addGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If unset or {@code true}, lombok generates {@code @javax.annotation.Generated("lombok")} on all fields, methods, and types that are generated, unless {@code lombok.addJavaxGeneratedAnnotation} is set.
	 * 
	 * @see ConfigurationKeys#ADD_JAVAX_GENERATED_ANNOTATIONS
	 * @see ConfigurationKeys#ADD_LOMBOK_GENERATED_ANNOTATIONS
	 * @deprecated Since version 1.16.14, use {@link #ADD_JAVAX_GENERATED_ANNOTATIONS} instead.
	 */
	@Deprecated
	public static final ConfigurationKey<Boolean> ADD_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: true). Deprecated, use 'lombok.addJavaxGeneratedAnnotation' instead.") {};
	
	/**
	 * lombok configuration: {@code lombok.addJavaxGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If unset or {@code true}, lombok generates {@code @javax.annotation.Generated("lombok")} on all fields, methods, and types that are generated, unless {@code lombok.addGeneratedAnnotation} is set to {@code false}.
	 */
	public static final ConfigurationKey<Boolean> ADD_JAVAX_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addJavaxGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: follow lombok.addGeneratedAnnotation).") {};
	
	/**
	 * lombok configuration: {@code lombok.addLombokGeneratedAnnotation} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code @lombok.Generated} on all fields, methods, and types that are generated.
	 */
	public static final ConfigurationKey<Boolean> ADD_LOMBOK_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addLombokGeneratedAnnotation", "Generate @lombok.Generated on all generated code (default: false).") {};
	
	/**
	 * lombok configuration: {@code lombok.extern.findbugs.addSuppressFBWarnings} = {@code true} | {@code false}.
	 * 
	 * If {@code true}, lombok generates {@code edu.umd.cs.findbugs.annotations.SuppressFBWarnings} on all fields, methods, and types that are generated.
	 * 
	 * NB: If you enable this option, findbugs must be on the source or classpath, or you'll get errors that the type {@code SuppressFBWarnings} cannot be found.
	 */
	public static final ConfigurationKey<Boolean> ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.extern.findbugs.addSuppressFBWarnings", "Generate @edu.umd.cs.findbugs.annotations.SuppressFBWArnings on all generated code (default: false).") {};
	
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
	 */
	public static final ConfigurationKey<Boolean> ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES = new ConfigurationKey<Boolean>("lombok.anyConstructor.suppressConstructorProperties", "Suppress the generation of @ConstructorProperties for generated constructors (default: false).") {};
	
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
	public static final ConfigurationKey<CallSuperType> EQUALS_AND_HASH_CODE_CALL_SUPER = new ConfigurationKey<CallSuperType>("lombok.equalsAndHashCode.callSuper", "When generating equals and hashCode for classes that don't extend Object, either automatically take into account superclass implementation (call), or don't (skip), or warn and don't (warn). (default = warn).") {};
	
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
	
	// ----- Builder -----
	
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
	public static final ConfigurationKey<Boolean> SINGULAR_AUTO = new ConfigurationKey<Boolean>("lombok.singular.auto", "If true (default): Automatically singularize the assumed-to-be-plural name of your variable/parameter when using {@code @Singular}.") {}; 
	
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
	 * lombok configuration: {@code lombok.nonNull.exceptionType} = &lt;String: <em>a java exception type</em>; either [{@code IllegalArgumentException} or: {@code NullPointerException}].
	 * 
	 * Sets the exception to throw if {@code @NonNull} is applied to a method parameter, and a caller passes in {@code null}.
	 */
	public static final ConfigurationKey<NullCheckExceptionType> NON_NULL_EXCEPTION_TYPE = new ConfigurationKey<NullCheckExceptionType>("lombok.nonNull.exceptionType", "The type of the exception to throw if a passed-in argument is null (Default: NullPointerException).") {};
	
	/**
	 * lombok configuration: {@code lombok.nonNull.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * <em>Implementation note: This field is supposed to be lombok.NonNull itself, but jdk6 and 7 have bugs where fields in annotations don't work well.</em>
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
	 * lombok configuration: {@code lombok.log.fieldName} = &lt;String: aJavaIdentifier&gt; (Default: {@code log}).
	 * 
	 * If set the various log annotations (which make a log field) will use the stated identifier instead of {@code log} as a name.
	 */
	public static final ConfigurationKey<String> LOG_ANY_FIELD_NAME = new ConfigurationKey<String>("lombok.log.fieldName", "Use this name for the generated logger fields (default: 'log').") {};
	
	/**
	 * lombok configuration: {@code lombok.log.fieldIsStatic} = {@code true} | {@code false}.
	 * 
	 * If not set, or set to {@code true}, the log field generated by the various log annotations will be {@code static}.
	 * 
	 * If set to {@code false}, these will be generated as instance fields instead.
	 */
	public static final ConfigurationKey<Boolean> LOG_ANY_FIELD_IS_STATIC = new ConfigurationKey<Boolean>("lombok.log.fieldIsStatic", "Make the generated logger fields static (default: true).") {};
	
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
	public static final ConfigurationKey<List<String>> ACCESSORS_PREFIX = new ConfigurationKey<List<String>>("lombok.accessors.prefix", "Strip this field prefix, like 'f' or 'm_', from the names of generated getters and setters.") {};
	
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
	 * If set to <code>true</code> <em>any</em> field without an access modifier or {@code @PackagePrivate} is marked as {@code private} by lombok, in all source files compiled.
	 */
	public static final ConfigurationKey<Boolean> FIELD_DEFAULTS_PRIVATE_EVERYWHERE = new ConfigurationKey<Boolean>("lombok.fieldDefaults.defaultPrivate", "If true, fields without any access modifier, in any file (lombok annotated or not) are marked as private. Use @PackagePrivate or an explicit modifier to override this.") {};
	
	/**
	 * lombok configuration: {@code lombok.fieldDefaults.defaultFinal} = {@code true} | {@code false}.
	 * 
	 * If set to <code>true</code> <em>any</em> field without {@code @NonFinal} is marked as {@code final} by lombok, in all source files compiled.
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
	
	// ----- Wither -----
	
	/**
	 * lombok configuration: {@code lombok.wither.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Wither} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> WITHER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.wither.flagUsage", "Emit a warning or error if @Wither is used.") {};
	
	// ----- Configuration System -----
	
	/**
	 * lombok configuration: {@code config.stopBubbling} = {@code true} | {@code false}.
	 * 
	 * If not set, or set to {@code false}, the configuration system will look for {@code lombok.config} files in the parent directory.
	 * 
	 * If set to {@code true}, no further {@code lombok.config} files will be checked.
	 */
	public static final ConfigurationKey<Boolean> STOP_BUBBLING = new ConfigurationKey<Boolean>("config.stopBubbling", "Tell the configuration system it should stop looking for other configuration files (default: false).") {};
}
