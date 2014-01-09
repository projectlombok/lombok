/*
 * Copyright (C) 2014 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringConfigurationSource implements ConfigurationSource {
	
	private static final Pattern LINE = Pattern.compile("(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*?))");
	
	private final Map<String, Result<?>> values;
	
	public static ConfigurationSource forString(String content) {
		return forString(content, ConfigurationErrorReporter.CONSOLE);
	}
	
	public static ConfigurationSource forString(String content, ConfigurationErrorReporter reporter) {
		if (reporter == null) throw new NullPointerException("reporter");
		
		Map<String, Result<?>> values = new TreeMap<String, Result<?>>(String.CASE_INSENSITIVE_ORDER);
		
		Map<String, ConfigurationDataType> registeredKeys = ConfigurationKey.registeredKeys();
		for (String line : content.trim().split("\\s*\\n\\s*")) {
			if (line.isEmpty() || line.startsWith("#")) continue;
			Matcher matcher = LINE.matcher(line);
			
			String operator = null;
			String keyName = null;
			String value;
			
			if (matcher.matches()) {
				if (matcher.group(1) == null) {
					keyName = matcher.group(2);
					operator = matcher.group(3);
					value = matcher.group(4);
				} else {
					keyName = matcher.group(1);
					operator = "clear";
					value = null;
				}
				ConfigurationDataType type = registeredKeys.get(keyName);
				if (type == null) {
					reporter.report("Unknown key '" + keyName + "' on line: " + line);
				} else {
					boolean listOperator = operator.equals("+=") || operator.equals("-=");
					if (listOperator && !type.isList()) {
						reporter.report("'" + keyName + "' is not a list and doesn't support " + operator + " (only = and clear): " + line);
					} else if (operator.equals("=") && type.isList()) {
						reporter.report("'" + keyName + "' is a list and cannot be assigned to (use +=, -= and clear instead): " + line);
					} else {
						processResult(values, keyName, operator, value, type, reporter);
					}
				}
			} else {
				reporter.report("No valid line: " + line);
			}
		}
		
		return new StringConfigurationSource(values);
	}
	
	private StringConfigurationSource(Map<String, Result<?>> values) {
		this.values = new TreeMap<String, Result<?>>(String.CASE_INSENSITIVE_ORDER);
		for (Entry<String, Result<?>> entry : values.entrySet()) {
			Result<?> result = entry.getValue();
			if (result.getValue() instanceof List<?>) {
				this.values.put(entry.getKey(), new Result<List<?>>(Collections.unmodifiableList((List<?>) result.getValue()), result.isAuthoritative()));
			} else {
				this.values.put(entry.getKey(), result);
			}
		}
	}
	
	private static void processResult(Map<String, Result<?>> values, String keyName, String operator, String value, ConfigurationDataType type, ConfigurationErrorReporter reporter) {
		Object element = null;
		if (value != null) try {
			element = type.getParser().parse(value);
		} catch (Exception e) {
			reporter.report("Error while parsing the value for '" + keyName + "' value '" + value + "' (should be a " + type.getParser().description() + ")");
			return;
		}
		
		if (operator.equals("clear") || operator.equals("=")) {
			if (element == null && type.isList()) {
				element = new ArrayList<ListModification>();
			}
			values.put(keyName, new Result<Object>(element, true));
		} else {
			Result<?> result = values.get(keyName);
			@SuppressWarnings("unchecked")
			List<ListModification> list = result == null ? new ArrayList<ListModification>() : (List<ListModification>) result.getValue();
			if (result == null) values.put(keyName, new Result<Object>(list, false));
			list.add(new ListModification(element, operator.equals("+=")));
		}
	}
	
	@SuppressWarnings("unchecked") 
	@Override 
	public <T> Result<T> resolve(ConfigurationKey<T> key) {
		return (Result<T>) values.get(key.getKeyName());
	}
}
