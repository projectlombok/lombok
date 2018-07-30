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
package lombok.core;

import java.net.URI;
import java.util.Collections;

import lombok.core.configuration.BubblingConfigurationResolver;
import lombok.core.configuration.ConfigurationKey;
import lombok.core.configuration.ConfigurationProblemReporter;
import lombok.core.configuration.ConfigurationResolver;
import lombok.core.configuration.ConfigurationResolverFactory;
import lombok.core.configuration.FileSystemSourceCache;

public class LombokConfiguration {
	private static final ConfigurationResolver NULL_RESOLVER = new ConfigurationResolver() {
		@SuppressWarnings("unchecked") @Override public <T> T resolve(ConfigurationKey<T> key) {
			if (key.getType().isList()) return (T) Collections.emptyList();
			return null;
		}
	};
	
	private static FileSystemSourceCache cache = new FileSystemSourceCache();
	private static ConfigurationResolverFactory configurationResolverFactory;
	
	static {
		if (System.getProperty("lombok.disableConfig") != null) {
			configurationResolverFactory = new ConfigurationResolverFactory() {
				@Override public ConfigurationResolver createResolver(URI sourceLocation) {
					return NULL_RESOLVER;
				}
			};
		}
		else {
			configurationResolverFactory = createFileSystemBubblingResolverFactory();
		}
	}
	
	private LombokConfiguration() {
		// prevent instantiation
	}
	
	public static void overrideConfigurationResolverFactory(ConfigurationResolverFactory crf) {
		configurationResolverFactory = crf == null ? createFileSystemBubblingResolverFactory() : crf;
	}
	
	static <T> T read(ConfigurationKey<T> key, AST<?, ?, ?> ast) {
		return configurationResolverFactory.createResolver(ast.getAbsoluteFileLocation()).resolve(key);
	}
	
	public static <T> T read(ConfigurationKey<T> key, URI sourceLocation) {
		return configurationResolverFactory.createResolver(sourceLocation).resolve(key);
	}
	
	private static ConfigurationResolverFactory createFileSystemBubblingResolverFactory() {
		return new ConfigurationResolverFactory() {
			@Override public ConfigurationResolver createResolver(URI sourceLocation) {
				return new BubblingConfigurationResolver(cache.sourcesForJavaFile(sourceLocation, ConfigurationProblemReporter.CONSOLE));
			}
		};
	}
}
