/*
 * Copyright (C) 2009 The Project Lombok Authors.
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
package lombok.delombok;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class UnicodeEscapeWriter extends Writer {
	private final Writer writer;
	private CharsetEncoder encoder;

	public UnicodeEscapeWriter(Writer writer, Charset charset) {
		this.writer = writer;
		encoder = charset.newEncoder();
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public final void write(char[] cbuf, int off, int len) throws IOException {
		int start = off;
		int index = start;
		int end = off + len;
		while (index < end) {
			if (!encoder.canEncode(cbuf[index])) {
				writer.write(cbuf, start, index - start);
				writeUnicodeEscape(cbuf[index]);
				start = index + 1;
			}
			index++;
		}
		if (start < end) {
			writer.write(cbuf, start, end - start);
		}
	}
	
	protected void writeUnicodeEscape(char c) throws IOException {
		writer.write(String.format("\\u%04x", (int) c));
	}
}