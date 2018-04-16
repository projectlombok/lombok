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

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

// Can't use SimpleJavaFileObject so we copy/paste most of its content here, because javac doesn't follow the interface,
// and casts to its own BaseFileObject type. D'oh!
class EmptyLombokFileObject implements LombokFileObject {
	private final String name;
	private final Kind kind;
	
	public EmptyLombokFileObject(String name, Kind kind) {
		this.name = name;
		this.kind = kind;
	}
	
	@Override public boolean isNameCompatible(String simpleName, Kind kind) {
		String baseName = simpleName + kind.extension;
		return kind.equals(getKind())
		&& (baseName.equals(toUri().getPath())
				|| toUri().getPath().endsWith("/" + baseName));
	}
	
	@Override public URI toUri() {
		return URI.create("file:///" + (name.startsWith("/") ? name.substring(1) : name));
	}
	
	@Override public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return "";
	}
	
	@Override public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(new byte[0]);
	}
	
	@Override public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new StringReader("");
	}
	
	@Override public Writer openWriter() throws IOException {
		return new OutputStreamWriter(openOutputStream());
	}
	
	@Override public OutputStream openOutputStream() throws IOException {
		return new ByteArrayOutputStream();
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
	
	@Override public CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		CodingErrorAction action = ignoreEncodingErrors ? CodingErrorAction.REPLACE : CodingErrorAction.REPORT;
		return decoder.onMalformedInput(action).onUnmappableCharacter(action);
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof EmptyLombokFileObject)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		EmptyLombokFileObject other = (EmptyLombokFileObject) obj;
		return name.equals(other.name) && kind.equals(other.kind);
	}
	
	@Override public int hashCode() {
		return name.hashCode() ^ kind.hashCode();
	}
}