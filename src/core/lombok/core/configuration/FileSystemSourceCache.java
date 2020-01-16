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

import java.io.File;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import lombok.core.debug.ProblemReporter;

public class FileSystemSourceCache {
	private static final long FULL_CACHE_CLEAR_INTERVAL = TimeUnit.MINUTES.toMillis(30);
	private static final long RECHECK_FILESYSTEM = TimeUnit.SECONDS.toMillis(2);
	private static final long NEVER_CHECKED = -1;
	static final long MISSING = -88; // Magic value; any lombok.config with this exact epochmillis last modified will never be read, so, let's ensure nobody accidentally has one with that exact last modified stamp.
	
	private final ConcurrentMap<ConfigurationFile, Content> fileCache = new ConcurrentHashMap<ConfigurationFile, Content>(); // caches files to the content object that tracks content.
	private final ConcurrentMap<URI, ConfigurationFile> uriCache = new ConcurrentHashMap<URI, ConfigurationFile>(); // caches URIs of java source files to the dir that contains it.
	private volatile long lastCacheClear = System.currentTimeMillis();
	
	private void cacheClear() {
		// We never clear the caches, generally because it'd be weird if a compile run would continually create an endless stream of new java files.
		// Still, eventually that's going to cause a bit of a memory leak, so lets just completely clear them out every many minutes.
		long now = System.currentTimeMillis();
		long delta = now - lastCacheClear;
		if (delta > FULL_CACHE_CLEAR_INTERVAL) {
			lastCacheClear = now;
			fileCache.clear();
			uriCache.clear();
		}
	}
	
	public ConfigurationFileToSource fileToSource(final ConfigurationParser parser) {
		return new ConfigurationFileToSource() {
			@Override public ConfigurationSource parsed(ConfigurationFile fileLocation) {
				return parseIfNeccesary(fileLocation, parser);
			}
		};
	}
	
	public ConfigurationFile forUri(URI javaFile) {
		if (javaFile == null) return null;
		cacheClear();
		ConfigurationFile result = uriCache.get(javaFile);
		if (result == null) {
			URI uri = javaFile.normalize();
			if (!uri.isAbsolute()) uri = URI.create("file:" + uri.toString());
			
			try {
				File file = new File(uri);
				if (!file.exists()) throw new IllegalArgumentException("File does not exist: " + uri);
				File directory = file.isDirectory() ? file : file.getParentFile();
				if (directory != null) result = ConfigurationFile.forDirectory(directory);
				uriCache.put(javaFile, result);
			} catch (IllegalArgumentException e) {
				// This means that the file as passed is not actually a file at all, and some exotic path system is involved.
				// examples: sourcecontrol://jazz stuff, or an actual relative path (uri.isAbsolute() is completely different, that checks presence of schema!),
				// or it's eclipse trying to parse a snippet, which has "/Foo.java" as uri.
				// At some point it might be worth investigating abstracting away the notion of "I can read lombok.config if present in
				// current context, and I can give you may parent context", using ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(javaFile) as basis.
				
				// For now, we just carry on as if there is no lombok.config. (intentional fallthrough)
			} catch (Exception e) {
				// Especially for eclipse's sake, exceptions here make eclipse borderline unusable, so let's play nice.
				ProblemReporter.error("Can't find absolute path of file being compiled: " + javaFile, e);
			}
		}
		return result;
	}

	private ConfigurationSource parseIfNeccesary(ConfigurationFile file, ConfigurationParser parser) {
		long now = System.currentTimeMillis();
		Content content = ensureContent(file);
		synchronized (content) {
			if (content.lastChecked != NEVER_CHECKED && now - content.lastChecked < RECHECK_FILESYSTEM) {
				return content.source;
			}
			content.lastChecked = now;
			long previouslyModified = content.lastModified;
			content.lastModified = file.getLastModifiedOrMissing();
			if (content.lastModified != previouslyModified) content.source = content.lastModified == MISSING ? null : SingleConfigurationSource.parse(file, parser);
			return content.source;
		}
	}
	
	private Content ensureContent(ConfigurationFile context) {
		Content content = fileCache.get(context);
		if (content != null) {
			return content;
		}
		fileCache.putIfAbsent(context, Content.empty());
		return fileCache.get(context);
	}
	
	private static class Content {
		ConfigurationSource source;
		long lastModified;
		long lastChecked;
		
		private Content(ConfigurationSource source, long lastModified, long lastChecked) {
			this.source = source;
			this.lastModified = lastModified;
			this.lastChecked = lastChecked;
		}
		
		static Content empty() {
			return new Content(null, MISSING, NEVER_CHECKED);
		}
	}
}