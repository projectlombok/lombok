/*
 * Copyright (C) 2015 The Project Lombok Authors.
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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.core.Version;

/**
 * We make a number of assumptions in lombok code, and if these assumptions fail, we try to fall back to a 'least bad' scenario. However, we would prefer to
 * just know about these cases, without confronting our users with error messages. The 'fix' is to log such assertion failures to this logger, which promptly
 * ignores them, _unless_ you specifically enable logging them to a file. If you'd like to help out or want to assist in debugging, turn this on.
 */
public class AssertionLogger {
	private static final String LOG_PATH;
	
	static {
		String log = System.getProperty("lombok.assertion.log", null);
		if (log != null) {
			LOG_PATH = log.isEmpty() ? null : log;
		} else {
			try {
				log = System.getenv("LOMBOK_ASSERTION_LOG");
			} catch (Exception e) {
				log = null;
			}
			LOG_PATH = (log == null || log.isEmpty()) ? null : log;
		}
	}
	
	private static final AtomicBoolean loggedIntro = new AtomicBoolean(false);
	private static final String PROCESS_ID = generateProcessId();
	
	private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	private static String generateProcessId() {
		char[] ID = new char[4];
		Random r = new Random();
		for (int i = 0; i < ID.length; i++) ID[i] = ID_CHARS.charAt(r.nextInt(ID_CHARS.length()));
		return new String(ID);
	}
	
	private static synchronized void logToFile(String msg) {
		if (msg == null) return;
		
		try {
			OutputStream out = new FileOutputStream(LOG_PATH, true);
			out.write(msg.getBytes("UTF-8"));
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("assertion logging can't write to log file", e);
		}
	}
	
	private static void logIntro() {
		if (loggedIntro.getAndSet(true)) return;
		
		String version;
		try {
			version = Version.getFullVersion();
		} catch (Exception e) {
			version = Version.getVersion();
		}
		
		logToFile(String.format("{%s} [%s -- START %s]\n", PROCESS_ID, new Date(), version));
	}
	
	public static <T extends Throwable> T assertLog(String message, T throwable) {
		if (LOG_PATH == null) return throwable;
		
		logIntro();
		
		if (message == null) message = "(No message)";
		String stackMsg = "";
		if (throwable != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			pw.close();
			stackMsg = "\n  " + sw.toString().replace("\r", "").replace("\n", "\n  ").trim();
		}
		logToFile(String.format("{%s} [%ty%<tm%<tdT%<tH%<tM%<tS.%<tL] %s%s\n", PROCESS_ID, new Date(), message, stackMsg));
		return throwable;
	}
	
	public static void assertLog(String message) {
		if (LOG_PATH == null) return;
		assertLog(message, null);
	}
}
