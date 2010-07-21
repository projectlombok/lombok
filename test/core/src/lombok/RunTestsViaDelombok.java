/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import lombok.delombok.Delombok;

public class RunTestsViaDelombok extends AbstractRunTests {
	private Delombok delombok = new Delombok();
	
	@Override
	public void transformCode(final StringBuilder messages, StringWriter result, File file) throws Throwable {
		delombok.setVerbose(false);
		delombok.setForceProcess(true);
		delombok.setCharset("UTF-8");
		
		delombok.setDiagnosticsListener(new DiagnosticListener<JavaFileObject>() {
			@Override public void report(Diagnostic<? extends JavaFileObject> d) {
				messages.append(String.format("%d:%d %s %s\n", d.getLineNumber(), d.getColumnNumber(), d.getKind(), d.getMessage(Locale.ENGLISH)));
			}
		});
		
		delombok.delombok(file.getAbsolutePath(), result);
	}
}
