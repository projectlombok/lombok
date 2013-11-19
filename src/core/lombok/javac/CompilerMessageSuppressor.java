/*
 * Copyright (C) 2011-2013 The Project Lombok Authors.
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
package lombok.javac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;

/**
 * During resolution, the resolver will emit resolution errors, but without appropriate file names and line numbers. If these resolution errors stick around
 * then they will be generated AGAIN, this time with proper names and line numbers, at the end. Therefore, we want to suppress the logger.
 */
public final class CompilerMessageSuppressor {
	private final Log log;
	private static final Field errWriterField, warnWriterField, noticeWriterField, dumpOnErrorField, promptOnErrorField, diagnosticListenerField;
	private static final Field deferDiagnosticsField, deferredDiagnosticsField, diagnosticHandlerField;
	private static final ConcurrentMap<Class<?>, Field> handlerDeferredFields = new ConcurrentHashMap<Class<?>, Field>();
	private static final Field NULL_FIELD;
	private PrintWriter errWriter, warnWriter, noticeWriter;
	private Boolean dumpOnError, promptOnError;
	private DiagnosticListener<?> contextDiagnosticListener, logDiagnosticListener;
	private final Context context;
	
	// If this is true, the fields changed. Better to print weird error messages than to fail outright.
	private static final boolean dontBother;
	
	private static final ThreadLocal<Queue<?>> queueCache = new ThreadLocal<Queue<?>>();
	
	static {
		errWriterField = getDeclaredField(Log.class, "errWriter");
		warnWriterField = getDeclaredField(Log.class, "warnWriter");
		noticeWriterField = getDeclaredField(Log.class, "noticeWriter");
		dumpOnErrorField = getDeclaredField(Log.class, "dumpOnError");
		promptOnErrorField = getDeclaredField(Log.class, "promptOnError");
		diagnosticListenerField = getDeclaredField(Log.class, "diagListener");
		
		dontBother = 
					errWriterField == null || 
					warnWriterField == null || 
					noticeWriterField == null || 
					dumpOnErrorField == null || 
					promptOnErrorField == null || 
					diagnosticListenerField == null;
		
		
		deferDiagnosticsField = getDeclaredField(Log.class, "deferDiagnostics");
		deferredDiagnosticsField = getDeclaredField(Log.class, "deferredDiagnostics");
		
		// javac8
		diagnosticHandlerField = getDeclaredField(Log.class, "diagnosticHandler");
		
		NULL_FIELD = getDeclaredField(JavacResolution.class, "NULL_FIELD");
	}
	
	static Field getDeclaredField(Class<?> c, String fieldName) {
		try {
			Field field = c.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		}
		catch (Throwable t) {
			return null;
		}
	}
	
	public CompilerMessageSuppressor(Context context) {
		this.log = Log.instance(context);
		this.context = context;
	}
	
	public boolean disableLoggers() {
		contextDiagnosticListener = context.get(DiagnosticListener.class);
		context.put(DiagnosticListener.class, (DiagnosticListener<?>) null);
		if (dontBother) return false;
		boolean dontBotherInstance = false;
		
		PrintWriter dummyWriter = new PrintWriter(new OutputStream() {
			@Override public void write(int b) throws IOException {
				// Do nothing on purpose
			}
		});
		
		if (deferDiagnosticsField != null) try {
			if (Boolean.TRUE.equals(deferDiagnosticsField.get(log))) {
				queueCache.set((Queue<?>) deferredDiagnosticsField.get(log));
				Queue<?> empty = new LinkedList<Object>();
				deferredDiagnosticsField.set(log, empty);
			}
		} catch (Exception e) {}
		
		if (diagnosticHandlerField != null) try {
			Object handler = diagnosticHandlerField.get(log);
			Field field = getDeferredField(handler);
			if (field != null) {
				queueCache.set((Queue<?>) field.get(handler));
				Queue<?> empty = new LinkedList<Object>();
				field.set(handler, empty);
			}
		} catch (Exception e) {}
		
		if (!dontBotherInstance) try {
			errWriter = (PrintWriter) errWriterField.get(log);
			errWriterField.set(log, dummyWriter);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (!dontBotherInstance) try {
			warnWriter = (PrintWriter) warnWriterField.get(log);
			warnWriterField.set(log, dummyWriter);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (!dontBotherInstance) try {
			noticeWriter = (PrintWriter) noticeWriterField.get(log);
			noticeWriterField.set(log, dummyWriter);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (!dontBotherInstance) try {
			dumpOnError = (Boolean) dumpOnErrorField.get(log);
			dumpOnErrorField.set(log, false);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (!dontBotherInstance) try {
			promptOnError = (Boolean) promptOnErrorField.get(log);
			promptOnErrorField.set(log, false);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (!dontBotherInstance) try {
			logDiagnosticListener = (DiagnosticListener<?>) diagnosticListenerField.get(log);
			diagnosticListenerField.set(log, null);
		} catch (Exception e) {
			dontBotherInstance = true;
		}
		
		if (dontBotherInstance) enableLoggers();
		return !dontBotherInstance;
	}
	
	private static Field getDeferredField(Object handler) {
		Class<? extends Object> key = handler.getClass();
		Field field = handlerDeferredFields.get(key);
		if (field != null) {
			return field == NULL_FIELD ? null : field;
		}
		Field value = getDeclaredField(key, "deferred");
		handlerDeferredFields.put(key, value == null ? NULL_FIELD : value);
		return getDeferredField(handler);
	}

	public void enableLoggers() {
		if (contextDiagnosticListener != null) {
			context.put(DiagnosticListener.class, contextDiagnosticListener);
			contextDiagnosticListener = null;
		}
		
		if (errWriter != null) try {
			errWriterField.set(log, errWriter);
			errWriter = null;
		} catch (Exception e) {}
		
		if (warnWriter != null) try {
			warnWriterField.set(log, warnWriter);
			warnWriter = null;
		} catch (Exception e) {}
		
		if (noticeWriter != null) try {
			noticeWriterField.set(log, noticeWriter);
			noticeWriter = null;
		} catch (Exception e) {}
		
		if (dumpOnError != null) try {
			dumpOnErrorField.set(log, dumpOnError);
			dumpOnError = null;
		} catch (Exception e) {}
		
		if (promptOnError != null) try {
			promptOnErrorField.set(log, promptOnError);
			promptOnError = null;
		} catch (Exception e) {}
		
		if (logDiagnosticListener != null) try {
			diagnosticListenerField.set(log, logDiagnosticListener);
			logDiagnosticListener = null;
		} catch (Exception e) {}
		
		if (diagnosticHandlerField != null && queueCache.get() != null) try {
			Object handler = diagnosticHandlerField.get(log);
			Field field = getDeferredField(handler);
			if (field != null) {
				field.set(handler, queueCache.get());
				queueCache.set(null);
			}
		} catch (Exception e) {}
		
		if (deferDiagnosticsField != null && queueCache.get() != null) try {
			deferredDiagnosticsField.set(log, queueCache.get());
			queueCache.set(null);
		} catch (Exception e) {}
	}
	
	public void removeAllBetween(JavaFileObject sourcefile, int startPos, int endPos) {
		DiagnosticListener<?> listener = context.get(DiagnosticListener.class);
		if (listener instanceof CapturingDiagnosticListener) {
			((CapturingDiagnosticListener) listener).suppress(startPos, endPos);
		}
		
		Field field = null;
		Object receiver = null;
		if (deferDiagnosticsField != null) try {
			if (Boolean.TRUE.equals(deferDiagnosticsField.get(log))) {
				field = deferredDiagnosticsField;
				receiver = log;
			}
		} catch (Exception e) {}
		
		if (diagnosticHandlerField != null) try {
			Object handler = diagnosticHandlerField.get(log);
			field = getDeferredField(handler);
			receiver = handler;
		} catch (Exception e) {}
		
		if (field == null || receiver == null) return;
		
		try {
			ListBuffer<?> deferredDiagnostics = (ListBuffer<?>) field.get(receiver);
			ListBuffer<Object> newDeferredDiagnostics = new ListBuffer<Object>();
			for (Object diag_ : deferredDiagnostics) {
				if (!(diag_ instanceof JCDiagnostic)) {
					newDeferredDiagnostics.add(diag_);
					continue;
				}
				JCDiagnostic diag = (JCDiagnostic) diag_;
				long here = diag.getStartPosition();
				if (here >= startPos && here < endPos && diag.getSource() == sourcefile) {
					// We eliminate it
				} else {
					newDeferredDiagnostics.add(diag);
				}
			}
			field.set(receiver, newDeferredDiagnostics);
		} catch (Exception e) {
			// We do not expect failure here; if failure does occur, the best course of action is to silently continue; the result will be that the error output of
			// javac will contain rather a lot of messages, but this is a lot better than just crashing during compilation!
		}
	}
}