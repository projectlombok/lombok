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
package lombok.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import lombok.core.configuration.BubblingConfigurationResolver;
import lombok.core.configuration.ConfigurationErrorReporter;
import lombok.core.configuration.ConfigurationErrorReporterFactory;
import lombok.core.configuration.ConfigurationKey;
import lombok.core.configuration.FileSystemSourceCache;

public class LombokConfiguration {
	
	private static FileSystemSourceCache cache = new FileSystemSourceCache();
	
	private LombokConfiguration() {
		// prevent instantiation
	}
	
	static <T> T read(ConfigurationKey<T> key, AST<?, ?, ?> ast) {
		return createResolver(ast, ConfigurationErrorReporterFactory.CONSOLE, cache).resolve(key);
	}
	
	public static void writeConfiguration(AST<?, ?, ?> ast, PrintStream stream) {
		final List<String> problems = new ArrayList<String>();
		ConfigurationErrorReporterFactory reporterFactory = new ConfigurationErrorReporterFactory() {
			@Override public ConfigurationErrorReporter createFor(final String description) {
				return new ConfigurationErrorReporter() {
					@Override
					public void report(String error, int lineNumber, String line) {
						problems.add(String.format("%s (%s:%d)", error, description, lineNumber));
					}
				};
			}
		};
		
		stream.printf("Combined lombok configuration for '%s'\n\n", ast.getAbsoluteFileLocation());
		// create a new empty 'cache' to make sure all problems are reported
		FileSystemSourceCache sourceCache = new FileSystemSourceCache();
		BubblingConfigurationResolver resolver = createResolver(ast, reporterFactory, sourceCache);
		for (ConfigurationKey<?> key : ConfigurationKey.registeredKeys()) {
			Object value = resolver.resolve(key);
			if (value == null || value instanceof List<?> && ((List<?>)value).isEmpty()) continue;
			stream.printf("%s: %s\n", key.getKeyName(), value);
		}
		
		if (!problems.isEmpty()) {
			stream.println();
			stream.printf("Problems encountered during parsing: %d\n", problems.size());
			int i = 1;
			for (String problem : problems) {
				stream.printf("%4d - %s\n", i, problem);
				i++;
			}
		}
	}
	
	private static BubblingConfigurationResolver createResolver(AST<?, ?, ?> ast, ConfigurationErrorReporterFactory reporterFactory, FileSystemSourceCache sourceCache) {
		return new BubblingConfigurationResolver(sourceCache.sourcesForJavaFile(ast.getAbsoluteFileLocation(), reporterFactory));
	}
	
	public static void main(String[] args) {
		System.out.println("  \n  \n ".trim().length());
	}
}
