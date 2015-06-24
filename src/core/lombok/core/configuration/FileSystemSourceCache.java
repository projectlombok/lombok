/*
 * Copyright (C) 2014-2015 The Project Lombok Authors.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import lombok.ConfigurationKeys;
import lombok.core.configuration.ConfigurationSource.Result;
import lombok.core.debug.ProblemReporter;

public class FileSystemSourceCache {
	private static final String LOMBOK_CONFIG_FILENAME = "lombok.config";
	private static final long FULL_CACHE_CLEAR_INTERVAL = TimeUnit.MINUTES.toMillis(30);
	private static final long RECHECK_FILESYSTEM = TimeUnit.SECONDS.toMillis(2);
	private static final long NEVER_CHECKED = -1;
	private static final long MISSING = -88; // Magic value; any lombok.config with this exact epochmillis last modified will never be read, so, let's ensure nobody accidentally has one with that exact last modified stamp.
	
	private final ConcurrentMap<File, Content> dirCache = new ConcurrentHashMap<File, Content>(); // caches files (representing dirs) to the content object that tracks content.
	private final ConcurrentMap<URI, File> uriCache = new ConcurrentHashMap<URI, File>(); // caches URIs of java source files to the dir that contains it.
	private volatile long lastCacheClear = System.currentTimeMillis();
	
	private void cacheClear() {
		// We never clear the caches, generally because it'd be weird if a compile run would continually create an endless stream of new java files.
		// Still, eventually that's going to cause a bit of a memory leak, so lets just completely clear them out every many minutes.
		long now = System.currentTimeMillis();
		long delta = now - lastCacheClear;
		if (delta > FULL_CACHE_CLEAR_INTERVAL) {
			lastCacheClear = now;
			dirCache.clear();
			uriCache.clear();
		}
	}
	
	public Iterable<ConfigurationSource> sourcesForJavaFile(URI javaFile, ConfigurationProblemReporter reporter) {
		if (javaFile == null) return Collections.emptyList();
		cacheClear();
		File dir = uriCache.get(javaFile);
		if (dir == null) {
			URI uri = javaFile.normalize();
			if (!uri.isAbsolute()) uri = URI.create("file:" + uri.toString());
			
			try {
				File file = new File(uri);
				if (!file.exists()) throw new IllegalArgumentException("File does not exist: " + uri);
				dir = file.isDirectory() ? file : file.getParentFile();
				if (dir != null) uriCache.put(javaFile, dir);
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
		
		if (dir != null) {
			try {
				return sourcesForDirectory(dir, reporter);
			} catch (Exception e) {
				// Especially for eclipse's sake, exceptions here make eclipse borderline unusable, so let's play nice.
				ProblemReporter.error("Can't resolve config stack for dir: " + dir.getAbsolutePath(), e);
			}
		}
		
		return Collections.emptyList();
	}
	
	public Iterable<ConfigurationSource> sourcesForDirectory(URI directory, ConfigurationProblemReporter reporter) {
		return sourcesForJavaFile(directory, reporter);
	}
	
	private Iterable<ConfigurationSource> sourcesForDirectory(final File directory, final ConfigurationProblemReporter reporter) {
		return new Iterable<ConfigurationSource>() {
			@Override 
			public Iterator<ConfigurationSource> iterator() {
				return new Iterator<ConfigurationSource>() {
					File currentDirectory = directory;
					ConfigurationSource next;
					boolean stopBubbling = false;
					
					@Override
					public boolean hasNext() {
						if (next != null) return true;
						if (stopBubbling) return false;
						next = findNext();
						return next != null;
					}
					
					@Override
					public ConfigurationSource next() {
						if (!hasNext()) throw new NoSuchElementException();
						ConfigurationSource result = next;
						next = null;
						return result;
					}
					
					private ConfigurationSource findNext() {
						while (currentDirectory != null && next == null) {
							next = getSourceForDirectory(currentDirectory, reporter);
							currentDirectory = currentDirectory.getParentFile();
						}
						if (next != null) {
							Result stop = next.resolve(ConfigurationKeys.STOP_BUBBLING);
							stopBubbling = (stop != null && Boolean.TRUE.equals(stop.getValue()));
						}
						return next;
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	ConfigurationSource getSourceForDirectory(File directory, ConfigurationProblemReporter reporter) {
		long now = System.currentTimeMillis();
		File configFile = new File(directory, LOMBOK_CONFIG_FILENAME);
		
		Content content = ensureContent(directory);
		synchronized (content) {
			if (content.lastChecked != NEVER_CHECKED && now - content.lastChecked < RECHECK_FILESYSTEM) {
				return content.source;
			}
			content.lastChecked = now;
			long previouslyModified = content.lastModified;
			content.lastModified = getLastModifiedOrMissing(configFile);
			if (content.lastModified != previouslyModified) content.source = content.lastModified == MISSING ? null : parse(configFile, reporter);
			return content.source;
		}
	}
	
	private Content ensureContent(File directory) {
		Content content = dirCache.get(directory);
		if (content != null) {
			return content;
		}
		dirCache.putIfAbsent(directory, Content.empty());
		return dirCache.get(directory);
	}
	
	private ConfigurationSource parse(File configFile, ConfigurationProblemReporter reporter) {
		String contentDescription = configFile.getAbsolutePath();
		try {
			return StringConfigurationSource.forString(fileToString(configFile), reporter, contentDescription);
		} catch (Exception e) {
			reporter.report(contentDescription, "Exception while reading file: " + e.getMessage(), 0, null);
			return null;
		}
	}
	
	private static final ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
		protected byte[] initialValue() {
			return new byte[65536];
		}
	};
	
	static String fileToString(File configFile) throws Exception {
		byte[] b = buffers.get();
		FileInputStream fis = new FileInputStream(configFile);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (true) {
				int r = fis.read(b);
				if (r == -1) break;
				out.write(b, 0, r);
			}
			return new String(out.toByteArray(), "UTF-8");
		} finally {
			fis.close();
		}
	}
	
	private static final long getLastModifiedOrMissing(File file) {
		if (!file.exists() || !file.isFile()) return MISSING;
		return file.lastModified();
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