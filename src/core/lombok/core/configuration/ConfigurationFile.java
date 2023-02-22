/*
 * Copyright (C) 2020 The Project Lombok Authors.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class ConfigurationFile {
	private static final Pattern VARIABLE = Pattern.compile("\\<(.+?)\\>");
	private static final String LOMBOK_CONFIG_FILENAME = "lombok.config";
	private static final Map<String, String> ENV = new HashMap<String, String>(System.getenv());
	
	private static final ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
		protected byte[] initialValue() {
			return new byte[65536];
		}
	};
	
	static void setEnvironment(String key, String value) {
		ENV.put(key, value);
	}
	
	private final String identifier;
	
	public static ConfigurationFile forFile(File file) {
		return new RegularConfigurationFile(file);
	}
	
	public static ConfigurationFile forDirectory(File directory) {
		return forFile(new File(directory, LOMBOK_CONFIG_FILENAME));
	}
	
	public static ConfigurationFile fromCharSequence(String identifier, CharSequence contents, long lastModified) {
		return new CharSequenceConfigurationFile(identifier, contents, lastModified);
	}
	
	private ConfigurationFile(String identifier) {
		this.identifier = identifier;
	}
	
	abstract long getLastModifiedOrMissing();
	abstract boolean exists();
	abstract CharSequence contents() throws IOException;
	public abstract ConfigurationFile resolve(String path);
	abstract ConfigurationFile parent();
	
	final String description() {
		return identifier;
	}
	
	@Override public final boolean equals(Object obj) {
		if (!(obj instanceof ConfigurationFile)) return false;
		return identifier.equals(((ConfigurationFile)obj).identifier);
	}
	
	@Override public final int hashCode() {
		return identifier.hashCode();
	}
	
	public static long getLastModifiedOrMissing(File file) {
		if (!fileExists(file)) return FileSystemSourceCache.MISSING;
		return file.lastModified();
	}
	
	private static boolean fileExists(File file) {
		return file.exists() && file.isFile();
	}
	
	private static String read(InputStream is) throws IOException {
		byte[] b = buffers.get();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (true) {
			int r = is.read(b);
			if (r == -1) break;
			out.write(b, 0, r);
		}
		return new String(out.toByteArray(), "UTF-8");
	}
	
	private static class RegularConfigurationFile extends ConfigurationFile {
		private final File file;
		private ConfigurationFile parent;

		private RegularConfigurationFile(File file) {
			super(file.getPath());
			this.file = file;
		}
		
		@Override boolean exists() {
			return fileExists(file);
		}
		
		public ConfigurationFile resolve(String path) {
			if (path.endsWith("!")) return null;
			
			String[] parts = path.split("!");
			if (parts.length > 2) return null;
			
			String realFileName = parts[0];
			File file = resolveFile(replaceEnvironmentVariables(realFileName));
			if (realFileName.endsWith(".zip") || realFileName.endsWith(".jar")) {
				try {
					return ArchivedConfigurationFile.create(file, URI.create(parts.length == 1 ? LOMBOK_CONFIG_FILENAME : parts[1]));
				} catch (Exception e) {
					return null;
				}
			}
			
			if (parts.length > 1) return null;
			return file == null ? null : forFile(file);
		}
		
		private File resolveFile(String path) {
			boolean absolute = false;
			int colon = path.indexOf(':');
			if (colon != -1) {
				if (colon != 1 || path.indexOf(':', colon + 1) != -1) return null;
				char firstCharacter = Character.toLowerCase(path.charAt(0));
				if (firstCharacter < 'a' || firstCharacter > 'z') return null;
				absolute = true;
			}
			if (path.charAt(0) == '/') absolute = true;
			try {
				return absolute ? new File(path) : new File(file.toURI().resolve(path));
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		long getLastModifiedOrMissing() {
			return getLastModifiedOrMissing(file);
		}
		
		@Override
		CharSequence contents() throws IOException {
			FileInputStream is = new FileInputStream(file);
			try {
				return read(is);
			} finally {
				is.close();
			}
		}

		@Override ConfigurationFile parent() {
			if (parent == null) {
				File parentFile = file.getParentFile().getParentFile();
				parent = parentFile == null ? null : forDirectory(parentFile);
			}
			return parent;
		}
		
		private static String replaceEnvironmentVariables(String fileName) {
			int start = 0;
			StringBuffer result = new StringBuffer();
			if (fileName.startsWith("~")) {
				start = 1;
				result.append(System.getProperty("user.home", "~"));
			}
			Matcher matcher = VARIABLE.matcher(fileName.substring(start));
			while (matcher.find()) {
				String key = matcher.group(1);
				String value = ENV.get(key);
				if (value == null) value = "<" + key + ">";
				matcher.appendReplacement(result, value);
			}
			matcher.appendTail(result);
			return result.toString();
		}
	}
	
	private static class ArchivedConfigurationFile extends ConfigurationFile {
		private static final URI ROOT1 = URI.create("http://x.y/a/");
		private static final URI ROOT2 = URI.create("ftp://y.x/b/");
		
		private static final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<String, Object>();
		
		private final File archive;
		private final URI file;
		private final Object lock;
		private long lastModified = -2;
		private String contents;
		
		public static ConfigurationFile create(File archive, URI file) {
			if (!isRelative(file)) return null;
			return new ArchivedConfigurationFile(archive, file, archive.getPath() + "!" + file.getPath());
		}
		
		static boolean isRelative(URI path) {
			try {
				return ROOT1.resolve(path).toString().startsWith(ROOT1.toString()) && ROOT2.resolve(path).toString().startsWith(ROOT2.toString());
			} catch (Exception e) {
				return false;
			}
		}
		
		ArchivedConfigurationFile(File archive, URI file, String description) {
			super(description);
			this.archive = archive;
			this.file = file;
			locks.putIfAbsent(archive.getPath(), new Object());
			this.lock = locks.get(archive.getPath());
		}
		
		@Override
		long getLastModifiedOrMissing() {
			return getLastModifiedOrMissing(archive);
		}
		
		@Override
		boolean exists() {
			if (!fileExists(archive)) return false;
			synchronized (lock) {
				try {
					readIfNeccesary();
					return contents != null;
				} catch (Exception e) {
					return false;
				}
			}
		}
		
		@Override
		CharSequence contents() throws IOException {
			synchronized (lock) {
				readIfNeccesary();
				return contents;
			}
		}
		
		void readIfNeccesary() throws IOException {
			long archiveModified = getLastModifiedOrMissing();
			if (archiveModified == lastModified) return;
			contents = null;
			lastModified = archiveModified;
			if (archiveModified == FileSystemSourceCache.MISSING) return;
			contents = read();
		}
		
		private String read() throws IOException {
			FileInputStream is = new FileInputStream(archive);
			try {
				ZipInputStream zip = new ZipInputStream(is);
				try {
					while (true) {
						ZipEntry entry = zip.getNextEntry();
						if (entry == null) return null;
						if (entry.getName().equals(file.getPath())) {
							return read(zip);
						}
					}
				} finally {
					zip.close();
				}
			} finally {
				is.close();
			}
		}
		
		@Override
		public ConfigurationFile resolve(String path) {
			try {
				URI resolved = file.resolve(path);
				if (!isRelative(resolved)) return null;
				return create(archive, resolved);
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		ConfigurationFile parent() {
			return null;
		}
	}
	
	private static class CharSequenceConfigurationFile extends ConfigurationFile {
		private final CharSequence contents;
		private final long lastModified;
		
		private CharSequenceConfigurationFile(String identifier, CharSequence contents, long lastModified) {
			super(identifier);
			this.contents = contents;
			this.lastModified = lastModified;
		}
		
		@Override long getLastModifiedOrMissing() {
			return lastModified;
		}
		
		@Override CharSequence contents() throws IOException {
			return contents;
		}

		@Override boolean exists() {
			return true;
		}

		@Override public ConfigurationFile resolve(String path) {
			return null;
		}

		@Override ConfigurationFile parent() {
			return null;
		}
	}
}
