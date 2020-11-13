/*
 * Copyright (C) 2010-2011 The Project Lombok Authors.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.CharsetDecoder;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import lombok.Lombok;
import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;
import lombok.permit.Permit;

final class InterceptingJavaFileObject implements LombokFileObject {
	private final JavaFileObject delegate;
	private final String fileName;
	private final DiagnosticsReceiver diagnostics;
	private final Method decoderMethod;
	
	public InterceptingJavaFileObject(JavaFileObject original, String fileName, DiagnosticsReceiver diagnostics, Method decoderMethod) {
		this.delegate = original;
		this.fileName = fileName;
		this.diagnostics = diagnostics;
		this.decoderMethod = decoderMethod;
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException {
		return PostCompiler.wrapOutputStream(delegate.openOutputStream(), fileName, diagnostics);
	}
	
	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException("Can't use a write for class files");
	}
	
	@Override public CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
		if (decoderMethod == null) throw new UnsupportedOperationException();
		return (CharsetDecoder) Permit.invokeSneaky(decoderMethod, delegate, ignoreEncodingErrors);
	}	
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof InterceptingJavaFileObject)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		InterceptingJavaFileObject other = (InterceptingJavaFileObject) obj;
		return fileName.equals(other.fileName) && delegate.equals(other.delegate);
	}
	
	@Override public int hashCode() {
		return fileName.hashCode() ^ delegate.hashCode();
	}
	
	
/////////////////////// NOTHING CHANGED BELOW //////////////////////////////////////
	
	@Override
	public boolean delete() {
		return delegate.delete();
	}
	
	@Override
	public Modifier getAccessLevel() {
		return delegate.getAccessLevel();
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return delegate.getCharContent(ignoreEncodingErrors);
	}
	
	@Override
	public Kind getKind() {
		return delegate.getKind();
	}
	
	@Override
	public long getLastModified() {
		return delegate.getLastModified();
	}
	
	@Override
	@SuppressWarnings("all")
	public String getName() {
		return delegate.getName();
	}
	
	@Override
	public NestingKind getNestingKind() {
		return delegate.getNestingKind();
	}
	
	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return delegate.isNameCompatible(simpleName, kind);
	}
	
	@Override
	public InputStream openInputStream() throws IOException {
		return delegate.openInputStream();
	}
	
	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return delegate.openReader(ignoreEncodingErrors);
	}
	
	@Override
	public URI toUri() {
		return delegate.toUri();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
}
