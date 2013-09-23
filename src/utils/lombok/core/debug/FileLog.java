/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
package lombok.core.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class FileLog {
	private static FileOutputStream fos;
	
	public static void log(String message) {
		log(message, null);
	}
	public synchronized static void log(String message, Throwable t) {
		try {
			if (fos == null) {
				fos = new FileOutputStream(new File(System.getProperty("user.home"), "LOMBOK-DEBUG-OUT.txt"));
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							fos.close();
						} catch (Throwable ignore) {}
					}
				});
			}
			fos.write(message.getBytes("UTF-8"));
			fos.write('\n');
			if (t != null) {
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				fos.write(sw.toString().getBytes("UTF-8"));
				fos.write('\n');
			}
			fos.flush();
		} catch (IOException e) {
			throw new IllegalStateException("Internal lombok file-based debugging not possible", e);
		}
	}
}
