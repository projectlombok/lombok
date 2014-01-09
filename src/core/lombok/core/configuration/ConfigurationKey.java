/*
 * Copyright (C) 2013 The Project Lombok Authors.
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

/**
 * Describes a configuration key and its type.
 * <p>
 * The recommended usage is to create a type token:
 * <pre>
 *    private static ConfigurationKey&lt;String> KEY = new ConfigurationKey&lt;String>("keyName") {}; 
 * </pre>
 */
public abstract class ConfigurationKey<T> {
	private static final TreeMap<String, ConfigurationDataType> registeredKeys = new TreeMap<String, ConfigurationDataType>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, ConfigurationDataType> copy;
	
	private final String keyName;
	private final ConfigurationDataType type;
	
	public ConfigurationKey(String keyName) {
		this.keyName = checkName(keyName);
		@SuppressWarnings("unchecked")
		ConfigurationDataType type = ConfigurationDataType.toDataType((Class<? extends ConfigurationKey<?>>)getClass());
		this.type = type;
		
		registerKey(keyName, type);
	}

	public final String getKeyName() {
		return keyName;
	}
	
	public final ConfigurationDataType getType() {
		return type;
	}
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + keyName.hashCode();
		result = prime * result + type.hashCode();
		return result;
	}
	
	/**
	 * Two configuration are considered equal if and only if their {@code keyName} and {@code type} are equal.
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConfigurationKey)) return false;
		ConfigurationKey<?> other = (ConfigurationKey<?>) obj;
		return keyName.equals(other.keyName) && type.equals(other.type);
	}
	
	private static String checkName(String keyName) {
		if (keyName == null) throw new NullPointerException("keyName");
		if (keyName.contains("=")) throw new IllegalArgumentException("Invalid character '=' in keyName: " + keyName);
		return keyName;
	}
	
	/** 
	 * Returns a copy of the currently registered keys.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ConfigurationDataType> registeredKeys() {
		synchronized (registeredKeys) {
			if (copy == null) copy = Collections.unmodifiableMap((Map<String, ConfigurationDataType>) registeredKeys.clone());
			return copy;
		}
	}
	
	private static void registerKey(String keyName, ConfigurationDataType type) {
		synchronized (registeredKeys) {
			ConfigurationDataType existingType = registeredKeys.get(keyName);
			if (existingType == null) {
				registeredKeys.put(keyName, type);
				copy = null;
				return;
			}
			if (!existingType.equals(type)) {
				throw new IllegalArgumentException("Key '" + keyName + "' already registered with a different type, existing " + existingType + " != provided " + type);
			}
		}
	}
}