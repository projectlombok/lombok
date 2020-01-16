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

public abstract class ConfigurationFile {
	private static final ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
		protected byte[] initialValue() {
			return new byte[65536];
		}
	};
		
	private final String identifier;
	
	public static ConfigurationFile fromFile(File file) {
		return new RegularConfigurationFile(file);
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
	
	final String description() {
		return identifier;
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof ConfigurationFile)) return false;
		return identifier.equals(((ConfigurationFile)obj).identifier);
	}
	
	@Override public int hashCode() {
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
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (true) {
				int r = is.read(b);
				if (r == -1) break;
				out.write(b, 0, r);
			}
			return new String(out.toByteArray(), "UTF-8");
		} finally {
			is.close();
		}
	}
	
	private static class RegularConfigurationFile extends ConfigurationFile {
		private final File file;
		
		private RegularConfigurationFile(File file) {
			super(file.getPath());
			this.file = file;
		}
		
		@Override boolean exists() {
			return fileExists(file);
		}
		
		public ConfigurationFile resolve(String path) {
			File file = resolveFile(path);
			return file == null ? null : fromFile(file);
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
			return read(new FileInputStream(file));
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
	}
}