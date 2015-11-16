/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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
package lombok;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import lombok.delombok.Delombok;
import lombok.javac.CapturingDiagnosticListener;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class RunTestsViaDelombok extends AbstractRunTests {
	private Delombok delombok = new Delombok();
	
	@Override
	public boolean transformCode(Collection<CompilerMessage> messages, StringWriter result, final File file, String encoding, Map<String, String> formatPreferences) throws Throwable {
		delombok.setVerbose(true);
		ChangedChecker cc = new ChangedChecker();
		delombok.setFeedback(cc.feedback);
		delombok.setForceProcess(true);
		delombok.setCharset(encoding == null ? "UTF-8" : encoding);
		delombok.setFormatPreferences(formatPreferences);
		
		delombok.setDiagnosticsListener(new CapturingDiagnosticListener(file, messages));
		
		delombok.addFile(file.getAbsoluteFile().getParentFile(), file.getName());
		delombok.setSourcepath(file.getAbsoluteFile().getParent());
		String bcp = System.getProperty("delombok.bootclasspath");
		if (bcp != null) delombok.setBootclasspath(bcp);
		delombok.setWriter(result);
		Locale originalLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			delombok.delombok();
			return cc.isChanged();
		} finally {
			Locale.setDefault(originalLocale);
		}
	}
	
	static class ChangedChecker {
		private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		private final PrintStream feedback;
		
		ChangedChecker() throws UnsupportedEncodingException {
			feedback = new PrintStream(bytes, true, "UTF-8");
		}
		
		boolean isChanged() throws UnsupportedEncodingException {
			feedback.flush();
			return bytes.toString("UTF-8").endsWith("[delomboked]\n");
		}
	}
}
