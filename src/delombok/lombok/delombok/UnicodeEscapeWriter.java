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
		writer.write("\\u" + Integer.toHexString(c));
	}
}