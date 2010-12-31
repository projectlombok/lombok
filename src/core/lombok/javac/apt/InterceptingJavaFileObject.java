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

import com.sun.tools.javac.util.BaseFileObject;

import lombok.Lombok;
import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;

final class InterceptingJavaFileObject extends BaseFileObject {
	private final JavaFileObject delegate;
	private final String fileName;
	private final DiagnosticsReceiver diagnostics;
	
	public InterceptingJavaFileObject(JavaFileObject original, String fileName, DiagnosticsReceiver diagnostics) {
		this.delegate = original;
		this.fileName = fileName;
		this.diagnostics = diagnostics;
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException {
		return PostCompiler.wrapOutputStream(delegate.openOutputStream(), fileName, diagnostics);
	}
	
	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException("Can't use a write for class files");
//		return original.openWriter();
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
	
	protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
		if (delegate instanceof BaseFileObject) {
			try {
				Method m = BaseFileObject.class.getDeclaredMethod("getDecoder", boolean.class);
				m.setAccessible(true);
				return (CharsetDecoder) m.invoke(delegate, ignoreEncodingErrors);
			} catch (NoSuchMethodException e) {
				Lombok.sneakyThrow(e);
			} catch (IllegalAccessException e) {
				Lombok.sneakyThrow(e);
			} catch (InvocationTargetException e) {
				Lombok.sneakyThrow(e);
			}
		}
		
		throw new UnsupportedOperationException();
	}
}
