/*
 * Copyright (C) 2014 The Project Lombok Authors.
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class ProblemReporter {
	public static void info(String msg, Throwable ex) {
		init();
		try {
			logger.info(msg, ex);
		} catch (Throwable t) {
			logger = new TerminalLogger();
			logger.info(msg, ex);
		}
	}
	
	public static void warning(String msg, Throwable ex) {
		init();
		try {
			logger.warning(msg, ex);
		} catch (Throwable t) {
			logger = new TerminalLogger();
			logger.warning(msg, ex);
		}
	}
	
	public static void error(String msg, Throwable ex) {
		init();
		try {
			logger.error(msg, ex);
		} catch (Throwable t) {
			logger = new TerminalLogger();
			logger.error(msg, ex);
		}
	}
	
	private static void init() {
		if (logger != null) return;
		try {
			logger = new EclipseWorkspaceLogger();
		} catch (Throwable t) {
			logger = new TerminalLogger();
		}
	}
	
	private static ErrorLogger logger;
	
	private interface ErrorLogger {
		void info(String message, Throwable ex);
		void warning(String message, Throwable ex);
		void error(String message, Throwable ex);
	}
	
	private static class TerminalLogger implements ErrorLogger {
		@Override
		public void info(String message, Throwable ex) {
			System.err.println(message);
			if (ex != null) ex.printStackTrace();
		}
		
		@Override
		public void warning(String message, Throwable ex) {
			System.err.println(message);
			if (ex != null) ex.printStackTrace();
		}
		
		@Override
		public void error(String message, Throwable ex) {
			System.err.println(message);
			if (ex != null) ex.printStackTrace();
		}
	}
	
	private static class EclipseWorkspaceLogger implements ErrorLogger {
		private static final String DEFAULT_BUNDLE_NAME = "org.eclipse.jdt.core";
		private static final Bundle bundle;
		private static final int MAX_LOG = 200;
		private static final long SQUELCH_TIMEOUT = TimeUnit.HOURS.toMillis(1);
		private static final AtomicInteger counter = new AtomicInteger();
		private static volatile long squelchTimeout = 0L;
		
		
		static {
			bundle = Platform.getBundle(DEFAULT_BUNDLE_NAME);
			if (bundle == null) throw new NoClassDefFoundError(); // this means some weird RCP build or possible ecj. At any rate, we can't report this way so act as if this isn't an eclipse.
		}
		
		@Override
		public void info(String message, Throwable error) {
			msg(IStatus.INFO, message, error);
		}
		
		@Override
		public void warning(String message, Throwable error) {
			msg(IStatus.WARNING, message, error);
		}
		
		@Override
		public void error(String message, Throwable error) {
			msg(IStatus.ERROR, message, error);
		}
		
		private void msg(int msgType, String message, Throwable error) {
			int ct = squelchTimeout != 0L ? 0 : counter.incrementAndGet();
			boolean printSquelchWarning = false;
			if (squelchTimeout != 0L) {
				long now = System.currentTimeMillis();
				if (squelchTimeout > now) return;
				squelchTimeout = now + SQUELCH_TIMEOUT;
				printSquelchWarning = true;
			} else if (ct >= MAX_LOG) {
				squelchTimeout = System.currentTimeMillis() + SQUELCH_TIMEOUT;
				printSquelchWarning = true;
			}
			ILog log = Platform.getLog(bundle);
			log.log(new Status(msgType, DEFAULT_BUNDLE_NAME, message, error));
			if (printSquelchWarning) {
				log.log(new Status(IStatus.WARNING, DEFAULT_BUNDLE_NAME, "Lombok has logged too many messages; to avoid memory issues, further lombok logs will be squelched for a while. Restart eclipse to start over."));
			}
		}
	}
}
