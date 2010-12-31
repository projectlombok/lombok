/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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
package lombok.javac.apt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Iterator;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.javac.util.BaseFileObject;

import lombok.core.DiagnosticsReceiver;

final class InterceptingJavaFileManager implements JavaFileManager {
	private final JavaFileManager delegate;
	private final DiagnosticsReceiver diagnostics;
	
	InterceptingJavaFileManager(JavaFileManager original, DiagnosticsReceiver diagnostics) {
		this.delegate = original;
		this.diagnostics = diagnostics;
	}
	
	@Override public JavaFileObject getJavaFileForOutput(Location location, String className, final Kind kind, FileObject sibling) throws IOException {
		if (className.startsWith("lombok.dummy.ForceNewRound")) {
			final String name = className.replace(".", "/") + kind.extension;
			// Can't use SimpleJavaFileObject so we copy/paste most of its content here, because javac doesn't follow the interface,
			// and casts to its own BaseFileObject type. D'oh!
			return new BaseFileObject() {
				@Override public boolean isNameCompatible(String simpleName, Kind kind) {
					String baseName = simpleName + kind.extension;
					return kind.equals(getKind())
					&& (baseName.equals(toUri().getPath())
							|| toUri().getPath().endsWith("/" + baseName));
				}
				
				@Override public URI toUri() {
					return URI.create(name);
				}
				
				@Override public InputStream openInputStream() throws IOException {
					return new ByteArrayInputStream(new byte[0]);
				}
				
				@Override public OutputStream openOutputStream() throws IOException {
					return new ByteArrayOutputStream();
				}
				
				@Override public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
					return "";
				}
				
				@Override public Writer openWriter() throws IOException {
					return new OutputStreamWriter(openOutputStream());
				}
				
				@Override public long getLastModified() {
					return 0L;
				}
				
				@Override public boolean delete() {
					return false;
				}
				
				@Override public Kind getKind() {
					return kind;
				}
				
				@SuppressWarnings("all")
				@Override public String getName() {
					return toUri().getPath();
				}
				
				@Override public NestingKind getNestingKind() {
					return null;
				}
				
				@Override public Modifier getAccessLevel() {
					return null;
				}
				
				@Override public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
					return new StringReader("");
				}
				
				protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
					CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
					CodingErrorAction action = ignoreEncodingErrors ? CodingErrorAction.REPLACE : CodingErrorAction.REPORT;
					return decoder.onMalformedInput(action).onUnmappableCharacter(action);
				}
			};
		}
		JavaFileObject fileObject = delegate.getJavaFileForOutput(location, className, kind, sibling);
		if (kind != Kind.CLASS) {
			return fileObject;
		}
		return new InterceptingJavaFileObject(fileObject, className, diagnostics);
	}
	
	
	
	
/////////////////////// NOTHING CHANGED BELOW //////////////////////////////////////
	
	@Override public void close() throws IOException {
		delegate.close();
	}
	
	@Override public void flush() throws IOException {
		delegate.flush();
	}
	
	@Override public ClassLoader getClassLoader(Location location) {
		return delegate.getClassLoader(location);
	}
	
	@Override public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		return delegate.getFileForInput(location, packageName, relativeName);
	}
	
	@Override public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
		return delegate.getFileForOutput(location, packageName, relativeName, sibling);
	}
	
	@Override public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		return delegate.getJavaFileForInput(location, className, kind);
	}
	
	@Override public boolean handleOption(String current, Iterator<String> remaining) {
		return delegate.handleOption(current, remaining);
	}
	
	@Override public boolean hasLocation(Location location) {
		return delegate.hasLocation(location);
	}
	
	@Override public String inferBinaryName(Location location, JavaFileObject file) {
		return delegate.inferBinaryName(location, file);
	}
	
	@Override public boolean isSameFile(FileObject a, FileObject b) {
		return delegate.isSameFile(a, b);
	}
	
	@Override public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		return delegate.list(location, packageName, kinds, recurse);
	}
	
	@Override public int isSupportedOption(String option) {
		return delegate.isSupportedOption(option);
	}
}