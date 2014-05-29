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

public class FileSystemSourceCache {
	private static String LOMBOK_CONFIG_FILENAME = "lombok.config";
	private static final long RECHECK_FILESYSTEM = TimeUnit.SECONDS.toMillis(2);
	private static final long MISSING = -1; 
	
	private final ConcurrentMap<File, Content> cache = new ConcurrentHashMap<File, Content>();
	
	public Iterable<ConfigurationSource> sourcesForJavaFile(URI javaFile, ConfigurationProblemReporter reporter) {
		if (javaFile == null) return Collections.emptyList();
		URI uri = javaFile.normalize();
		if (!uri.isAbsolute()) {
			uri = new File(".").toURI().resolve(uri);
		}
		
		return sourcesForDirectory(new File(uri).getParentFile(), reporter);
	}
	
	public Iterable<ConfigurationSource> sourcesForDirectory(URI directory, ConfigurationProblemReporter reporter) {
		if (directory == null) return Collections.emptyList();
		return sourcesForDirectory(new File(directory.normalize()), reporter);
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
		if (!directory.exists() && !directory.isDirectory()) throw new IllegalArgumentException("Not a directory: " + directory);
		long now = System.currentTimeMillis();
		File configFile = new File(directory, LOMBOK_CONFIG_FILENAME);
		
		Content content = ensureContent(directory);
		synchronized (content) {
			if (content.lastChecked != MISSING && now - content.lastChecked < RECHECK_FILESYSTEM && getLastModified(configFile) == content.lastModified) {
				return content.source;
			}
			content.lastChecked = now;
			long previouslyModified = content.lastModified;
			content.lastModified = getLastModified(configFile);
			if (content.lastModified != previouslyModified) content.source = content.lastModified == MISSING ? null : parse(configFile, reporter);
			return content.source;
		}
	}
	
	private Content ensureContent(File directory) {
		Content content = cache.get(directory);
		if (content != null) {
			return content;
		}
		cache.putIfAbsent(directory, Content.empty());
		return cache.get(directory);
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
	
	private static final long getLastModified(File file) {
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
			return new Content(null, MISSING, MISSING);
		}
	}
}