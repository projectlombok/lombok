/*
 * Copyright (C) 2014-2020 The Project Lombok Authors.
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

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationParser {
	private static final Pattern LINE = Pattern.compile("(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*?))");
	private static final Pattern NEWLINE_FINDER = Pattern.compile("^[\t ]*(.*?)[\t\r ]*$", Pattern.MULTILINE);
	private static final Pattern IMPORT = Pattern.compile("import\\s+(.+?)");
	
	private ConfigurationProblemReporter reporter;
	
	public ConfigurationParser(ConfigurationProblemReporter reporter) {
		if (reporter == null) throw new NullPointerException("reporter");
		this.reporter = reporter;
	}
	
	public void parse(ConfigurationFile context, Collector collector) {
		CharSequence contents = contents(context);
		if (contents == null) {
			return;
		}
		Map<String, ConfigurationKey<?>> registeredKeys = ConfigurationKey.registeredKeys();
		int lineNumber = 0;
		Matcher lineMatcher = NEWLINE_FINDER.matcher(contents);
		boolean importsAllowed = true;
		while (lineMatcher.find()) {
			CharSequence line = contents.subSequence(lineMatcher.start(1), lineMatcher.end(1));
			lineNumber++;
			if (line.length() == 0 || line.charAt(0) == '#') continue;
			
			Matcher importMatcher = IMPORT.matcher(line);
			if (importMatcher.matches()) {
				if (!importsAllowed) {
					reporter.report(context.description(), "Imports are only allowed in the top of the file", lineNumber, line);
					continue;
				}
				String imported = importMatcher.group(1);
				ConfigurationFile importFile = context.resolve(imported);
				if (importFile == null) {
					reporter.report(context.description(), "Import is not valid", lineNumber, line);
					continue;
				}
				if (!importFile.exists()) {
					reporter.report(context.description(), "Imported file does not exist", lineNumber, line);
					continue;
				}
				collector.addImport(importFile, context, lineNumber);
				continue;
			}
			
			Matcher matcher = LINE.matcher(line);
			if (!matcher.matches()) {
				reporter.report(context.description(), "Invalid line", lineNumber, line);
				continue;
			}
			importsAllowed = false;
			
			String operator = null;
			String keyName = null;
			String stringValue;
			if (matcher.group(1) == null) {
				keyName = matcher.group(2);
				operator = matcher.group(3);
				stringValue = matcher.group(4);
			} else {
				keyName = matcher.group(1);
				operator = "clear";
				stringValue = null;
			}
			ConfigurationKey<?> key = registeredKeys.get(keyName);
			if (key == null) {
				reporter.report(context.description(), "Unknown key '" + keyName + "'", lineNumber, line);
				continue;
			}
			
			ConfigurationDataType type = key.getType();
			boolean listOperator = operator.equals("+=") || operator.equals("-=");
			if (listOperator && !type.isList()) {
				reporter.report(context.description(), "'" + keyName + "' is not a list and doesn't support " + operator + " (only = and clear)", lineNumber, line);
				continue;
			}
			if (operator.equals("=") && type.isList()) {
				reporter.report(context.description(), "'" + keyName + "' is a list and cannot be assigned to (use +=, -= and clear instead)", lineNumber, line);
				continue;
			}
			
			Object value = null;
			if (stringValue != null) try {
				value = type.getParser().parse(stringValue);
			} catch (Exception e) {
				reporter.report(context.description(), "Error while parsing the value for '" + keyName + "' value '" + stringValue + "' (should be " + type.getParser().exampleValue() + ")", lineNumber, line);
				continue;
			}
			
			if (operator.equals("clear")) {
				collector.clear(key, context, lineNumber);
			} else if (operator.equals("=")) {
				collector.set(key, value, context, lineNumber);
			} else if (operator.equals("+=")) {
				collector.add(key, value, context, lineNumber);
			} else {
				collector.remove(key, value, context, lineNumber);
			}
		}
	}
	
	private CharSequence contents(ConfigurationFile context) {
		try {
			return context.contents();
		} catch (IOException e) {
			reporter.report(context.description(), "Exception while reading file: " + e.getMessage(), 0, null);
		}
		return null;
	}
	
	public interface Collector {
		void addImport(ConfigurationFile importFile, ConfigurationFile context, int lineNumber);
		void clear(ConfigurationKey<?> key, ConfigurationFile context, int lineNumber);
		void set(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber);
		void add(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber);
		void remove(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber);
	}
}
