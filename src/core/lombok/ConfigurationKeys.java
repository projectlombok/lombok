/*
 * Copyright (C) 2013-2014 The Project Lombok Authors.
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

import lombok.core.FlagUsageType;
import lombok.core.configuration.ConfigurationKey;

/**
 * A container class containing all lombok configuration keys that do not belong to a specific annotation.
 */
public class ConfigurationKeys {
	private ConfigurationKeys() {}
	
	// ##### main package features #####
	
	// ----- *ArgsConstructor -----
	
	/**
	 * lombok configuration: {@code lombok.AnyConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @AllArgsConstructor}, {@code @RequiredArgsConstructor}, or {@code @NoArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ANY_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.AnyConstructor.flagUsage", "Emit a warning or error if any of the XxxArgsConstructor annotations are used.") {};
	
	/**
	 * lombok configuration: {@code lombok.AllArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @AllArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ALL_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.AllArgsConstructor.flagUsage", "Emit a warning or error if @AllArgsConstructor is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.NoArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @NoArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> NO_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.NoArgsConstructor.flagUsage", "Emit a warning or error if @NoArgsConstructor is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.RequiredArgsConstructor.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @RequiredArgsConstructor} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.RequiredArgsConstructor.flagUsage", "Emit a warning or error if @RequiredArgsConstructor is used.") {};
	
	// ##### Beanies #####
	
	// ----- Data -----
	
	/**
	 * lombok configuration: {@code lombok.Data.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Data} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> DATA_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Data.flagUsage", "Emit a warning or error if @Data is used.") {};
	
	// ----- Value -----
	
	/**
	 * lombok configuration: {@code lombok.Value.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Value} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> VALUE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Value.flagUsage", "Emit a warning or error if @Value is used.") {};
	
	// ----- Getter -----
	
	/**
	 * lombok configuration: {@code lombok.Getter.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Getter} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> GETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Getter.flagUsage", "Emit a warning or error if @Getter is used.") {};
	
	// ----- Setter -----
	
	/**
	 * lombok configuration: {@code lombok.Setter.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Setter} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Setter.flagUsage", "Emit a warning or error if @Setter is used.") {};
	
	// ----- EqualsAndHashCode -----
	
	/**
	 * lombok configuration: {@code lombok.EqualsAndHashCode.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @EqualsAndHashCode} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> EQUALS_AND_HASH_CODE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.EqualsAndHashCode.flagUsage", "Emit a warning or error if @EqualsAndHashCode is used.") {};
	
	// ----- ToString -----
	/**
	 * lombok configuration: {@code lombok.ToString.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @ToString} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> TO_STRING_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.ToString.flagUsage", "Emit a warning or error if @ToString is used.") {};
	
	// ##### Standalones #####
	
	// ----- Cleanup -----
	
	/**
	 * lombok configuration: {@code lombok.Cleanup.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Cleanup} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> CLEANUP_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Cleanup.flagUsage", "Emit a warning or error if @Cleanup is used.") {};
	
	// ----- Delegate -----
	
	/**
	 * lombok configuration: {@code lombok.Delegate.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Delegate} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> DELEGATE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Delegate.flagUsage", "Emit a warning or error if @Delegate is used.") {};
	
	// ----- NonNull -----
	
	/**
	 * lombok configuration: {@code lombok.NonNull.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * <em>Implementation note: This field is supposed to be lombok.NonNull itself, but jdk6 and 7 have bugs where fields in annotations don't work well.</em>
	 * 
	 * If set, <em>any</em> usage of {@code @NonNull} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> NON_NULL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.NonNull.flagUsage", "Emit a warning or error if @NonNull is used.") {};
	
	// ----- SneakyThrows -----
	
	/**
	 * lombok configuration: {@code lombok.SneakyThrows.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @SneakyThrows} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SNEAKY_THROWS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.SneakyThrows.flagUsage", "Emit a warning or error if @SneakyThrows is used.") {};
	
	// ----- Synchronized -----
	
	/**
	 * lombok configuration: {@code lombok.Synchronized.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Synchronized} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> SYNCHRONIZED_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Synchronized.flagUsage", "Emit a warning or error if @Synchronized is used.") {};
	
	// ----- val -----
	
	/**
	 * lombok configuration: {@code lombok.val.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code val} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> VAL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.val.flagUsage", "Emit a warning or error if 'val' is used.") {};
	
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
	 * lombok configuration: {@code lombok.log.fieldName} = "aJavaIdentifier".
	 * 
	 * If set the various log annotations (which make a log field) will use the stated identifier instead of {@code log} as a name.
	 */
	public static final ConfigurationKey<String> LOG_ANY_FIELD_NAME = new ConfigurationKey<String>("lombok.log.fieldName", "Use this name for the generated logger fields (default: 'log')") {};
	
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
	 * lombok configuration: {@code lombok.Accessors.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Accessors} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> ACCESSORS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Accessors.flagUsage", "Emit a warning or error if @Accessors is used.") {};
	
	/**
	 * lombok configuration: {@code lombok.Accessors.prefix} += &lt;String: prefix&gt;.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code prefix} option, this list of prefixes is used.
	 */
	public static final ConfigurationKey<List<String>> ACCESSORS_PREFIX = new ConfigurationKey<List<String>>("lombok.Accessors.prefix", "Strip this field prefix, like 'f' or 'm_', from the names of generated getters and setters.") {};
	
	/**
	 * lombok configuration: {@code lombok.Accessors.chain} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code chain} option, this value is used.
	 */
	public static final ConfigurationKey<Boolean> ACCESSORS_CHAIN = new ConfigurationKey<Boolean>("lombok.Accessors.chain", "Generate setters that return 'this' instead of 'void'.") {};
	
	/**
	 * lombok configuration: {@code lombok.Accessors.fluent} = {@code true} | {@code false}.
	 * 
	 * For any class without an {@code @Accessors} that explicitly defines the {@code fluent} option, this value is used.
	 */
	public static final ConfigurationKey<Boolean> ACCESSORS_FLUENT = new ConfigurationKey<Boolean>("lombok.Accessors.fluent", "Generate getters and setters using only the field name (no get/set prefix).") {};
	
	// ----- Builder -----
	
	/**
	 * lombok configuration: {@code lombok.Builder.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Builder} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> BUILDER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Builder.flagUsage", "Emit a warning or error if @Builder is used.") {};
	
	// ----- ExtensionMethod -----
	
	/**
	 * lombok configuration: {@code lombok.ExtensionMethod.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @ExtensionMethod} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> EXTENSION_METHOD_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.ExtensionMethod.flagUsage", "Emit a warning or error if @ExtensionMethod is used.") {};
	
	// ----- FieldDefaults -----
	
	/**
	 * lombok configuration: {@code lombok.FieldDefaults.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @FieldDefaults} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> FIELD_DEFAULTS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.FieldDefaults.flagUsage", "Emit a warning or error if @FieldDefaults is used.") {};
	
	// ----- Wither -----
	
	/**
	 * lombok configuration: {@code lombok.Wither.flagUsage} = {@code WARNING} | {@code ERROR}.
	 * 
	 * If set, <em>any</em> usage of {@code @Value} results in a warning / error.
	 */
	public static final ConfigurationKey<FlagUsageType> WITHER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.Wither.flagUsage", "Emit a warning or error if @Wither is used.") {};
	
	
	// ----- Configuration System -----
	
	/**
	 * lombok configuration: {@code stop-bubbling} = {@code true} | {@code false}.
	 * 
	 * If not set, or set to {@code false}, the configuration system will look for {@code lombok.config} files in the parent directory.
	 * 
	 * If set to {@code true}, no futher {@code lombok.config} files will be checked.
	 */
	public static final ConfigurationKey<Boolean> STOP_BUBBLING = new ConfigurationKey<Boolean>("stop-bubbling", "Tell the configuration system it should stop looking for other configuration files (default: false).") {};
}
