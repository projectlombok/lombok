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
import java.util.Map;
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
	private static final WriterField errWriterField, warnWriterField, noticeWriterField;
	private static final Field dumpOnErrorField, promptOnErrorField, diagnosticListenerField;
	private static final Field deferDiagnosticsField, deferredDiagnosticsField, diagnosticHandlerField;
	private static final ConcurrentMap<Class<?>, Field> handlerDeferredFields = new ConcurrentHashMap<Class<?>, Field>();
	private static final Field NULL_FIELD;
	private Boolean dumpOnError, promptOnError;
	private DiagnosticListener<?> contextDiagnosticListener, logDiagnosticListener;
	private final Context context;
	
	private static final ThreadLocal<Queue<?>> queueCache = new ThreadLocal<Queue<?>>();
	
	enum Writers {
		ERROR("errWriter", "ERROR"),
		WARNING("warnWriter", "WARNING"),
		NOTICE("noticeWriter", "NOTICE");
		
		final String fieldName;
		final String keyName;
		
		Writers(String fieldName, String keyName) {
			this.fieldName = fieldName;
			this.keyName = keyName;
		}
	}
	
	static {
		errWriterField = createWriterField(Writers.ERROR);
		warnWriterField = createWriterField(Writers.WARNING);
		noticeWriterField = createWriterField(Writers.NOTICE);
		dumpOnErrorField = getDeclaredField(Log.class, "dumpOnError");
		promptOnErrorField = getDeclaredField(Log.class, "promptOnError");
		diagnosticListenerField = getDeclaredField(Log.class, "diagListener");
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
	
	public void disableLoggers() {
		contextDiagnosticListener = context.get(DiagnosticListener.class);
		context.put(DiagnosticListener.class, (DiagnosticListener<?>) null);

		errWriterField.pauze(log);
		warnWriterField.pauze(log);
		noticeWriterField.pauze(log);
		
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
		
		if (dumpOnErrorField != null) try {
			dumpOnError = (Boolean) dumpOnErrorField.get(log);
			dumpOnErrorField.set(log, false);
		} catch (Exception e) {
		}
		
		if (promptOnErrorField != null) try {
			promptOnError = (Boolean) promptOnErrorField.get(log);
			promptOnErrorField.set(log, false);
		} catch (Exception e) {
		}
		
		if (diagnosticListenerField != null) try {
			logDiagnosticListener = (DiagnosticListener<?>) diagnosticListenerField.get(log);
			diagnosticListenerField.set(log, null);
		} catch (Exception e) {
		}
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
		
		errWriterField.resume(log);
		warnWriterField.resume(log);
		noticeWriterField.resume(log);
		
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
	
	private static WriterField createWriterField(Writers w) {
		// jdk9
		try {
			Field writers = getDeclaredField(Log.class, "writer");
			if (writers != null) {
				Class<?> kindsClass = Class.forName("com.sun.tools.javac.util.Log$WriterKind");
				for (Object enumConstant : kindsClass.getEnumConstants()) {
					if (enumConstant.toString().equals(w.keyName)) {
						return new Java9WriterField(writers, enumConstant);
					}
				}
				return WriterField.NONE;
			}
		} catch (Exception e) {
		}
		
		// jdk8
		Field writerField = getDeclaredField(Log.class, w.fieldName);
		if (writerField != null) return new Java8WriterField(writerField);
		
		// other jdk
		return WriterField.NONE;
	}
	
	interface WriterField {
		final PrintWriter NO_WRITER = new PrintWriter(new OutputStream() {
			@Override public void write(int b) throws IOException {
				// Do nothing on purpose
			}
		});

		final WriterField NONE = new WriterField() {
			@Override public void pauze(Log log) {
				// do nothing
			}
			@Override public void resume(Log log) {
				// no nothing
			}
		};
		
		void pauze(Log log);
		void resume(Log log);
	}
	
	static class Java8WriterField implements WriterField {
		private final Field field;
		private PrintWriter writer;
		
		public Java8WriterField(Field field) {
			this.field = field;
		}

		@Override public void pauze(Log log) {
			try {
				writer = (PrintWriter) field.get(log);
				field.set(log, NO_WRITER);
			} catch (Exception e) {
			}
		}

		@Override public void resume(Log log) {
			if (writer != null) {
				try {
					field.set(log, writer);
				} catch (Exception e) {
				}
			}
			writer = null;
		}
	}
	
	
	static class Java9WriterField implements WriterField {
		private final Field field;
		private final Object key;
		private PrintWriter writer;
		
		public Java9WriterField(Field field, Object key) {
			this.field = field;
			this.key = key;
		}
		
		@Override public void pauze(Log log) {
			try {
				@SuppressWarnings("unchecked") Map<Object,PrintWriter> map = (Map<Object,PrintWriter>)field.get(log);
				writer = map.get(key);
				map.put(key, NO_WRITER);
			} catch (Exception e) {
			}
		}
		
		@Override public void resume(Log log) {
			if (writer != null) {
				try {
					@SuppressWarnings("unchecked") Map<Object,PrintWriter> map = (Map<Object,PrintWriter>)field.get(log);
					map.put(key, writer);
				} catch (Exception e) {
				}
			}
			writer = null;
		}
	}
}