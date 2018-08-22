/*
 * Copyright (C) 2013-2018 The Project Lombok Authors.
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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Describes a configuration key and its type.
 * <p>
 * The recommended usage is to create a type token:
 * <pre>
 *    private static ConfigurationKey&lt;String> KEY = new ConfigurationKey&lt;String>("keyName", "description") {};
 * </pre>
 */
public abstract class ConfigurationKey<T> {
	private static final Pattern VALID_NAMES = Pattern.compile("[-_a-zA-Z][-.\\w]*(?<![-.])");
	
	private static final TreeMap<String, ConfigurationKey<?>> registeredKeys = new TreeMap<String, ConfigurationKey<?>>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, ConfigurationKey<?>> copy;
	
	private final String keyName;
	private final String description;
	private final ConfigurationDataType type;
	private final boolean hidden;
	
	public ConfigurationKey(String keyName, String description) {
		this(keyName, description, false);
	}
	
	public ConfigurationKey(String keyName, String description, boolean hidden) {
		this.keyName = checkName(keyName);
		@SuppressWarnings("unchecked")
		ConfigurationDataType type = ConfigurationDataType.toDataType((Class<? extends ConfigurationKey<?>>)getClass());
		this.type = type;
		this.description = description;
		this.hidden = hidden;
		registerKey(keyName, this);
	}
	
	public final String getKeyName() {
		return keyName;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final ConfigurationDataType getType() {
		return type;
	}
	
	public final boolean isHidden() {
		return hidden;
	}
	
	@Override public String toString() {
		return keyName + " (" + type + "): " + description;
	}
	
	private static String checkName(String keyName) {
		if (keyName == null) throw new NullPointerException("keyName");
		if (!VALID_NAMES.matcher(keyName).matches()) throw new IllegalArgumentException("Invalid keyName: " + keyName);
		return keyName;
	}
	
	/** 
	 * Returns a copy of the currently registered keys.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ConfigurationKey<?>> registeredKeys() {
		synchronized (registeredKeys) {
			if (copy == null) copy = Collections.unmodifiableMap((Map<String, ConfigurationKey<?>>) registeredKeys.clone());
			return copy;
		}
	}
	
	private static void registerKey(String keyName, ConfigurationKey<?> key) {
		synchronized (registeredKeys) {
			if (registeredKeys.containsKey(keyName)) throw new IllegalArgumentException("Key '" + keyName + "' already registered");
			registeredKeys.put(keyName, key);
			copy = null;
		}
	}
}