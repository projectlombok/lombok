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

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import lombok.core.DiagnosticsReceiver;

final class InterceptingJavaFileManager implements JavaFileManager {
	private final JavaFileManager delegate;
	private final DiagnosticsReceiver diagnostics;
	
	InterceptingJavaFileManager(JavaFileManager original, DiagnosticsReceiver diagnostics) {
		this.delegate = original;
		this.diagnostics = diagnostics;
	}
	
	@Override public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
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