/*
 * Copyright (C) 2010-2017 The Project Lombok Authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import lombok.core.DiagnosticsReceiver;

import com.sun.tools.javac.file.BaseFileManager;

//Can't use SimpleJavaFileObject so we copy/paste most of its content here, because javac doesn't follow the interface,
//and casts to its own BaseFileObject type. D'oh!
final class LombokFileObjects {
	
	interface Compiler {
		Compiler JAVAC6 = new Compiler() {
			private Method decoderMethod = null;
			private final AtomicBoolean decoderIsSet = new AtomicBoolean();
			
			@Override public JavaFileObject wrap(LombokFileObject fileObject) {
				return new Javac6BaseFileObjectWrapper(fileObject);
			}
			
			@Override public Method getDecoderMethod() {
				synchronized (decoderIsSet) {
					if (decoderIsSet.get()) return decoderMethod;
					decoderMethod = LombokFileObjects.getDecoderMethod("com.sun.tools.javac.util.BaseFileObject");
					decoderIsSet.set(true);
					return decoderMethod;
				}
			}
		};
		Compiler JAVAC7 = new Compiler() {
			private Method decoderMethod = null;
			private final AtomicBoolean decoderIsSet = new AtomicBoolean();
			
			@Override public JavaFileObject wrap(LombokFileObject fileObject) {
				return new Javac7BaseFileObjectWrapper(fileObject);
			}
			
			@Override public Method getDecoderMethod() {
				synchronized (decoderIsSet) {
					if (decoderIsSet.get()) return decoderMethod;
					decoderMethod = LombokFileObjects.getDecoderMethod("com.sun.tools.javac.file.BaseFileObject");
					decoderIsSet.set(true);
					return decoderMethod;
				}
			}
		};
		
		JavaFileObject wrap(LombokFileObject fileObject);
		Method getDecoderMethod();
	}
		
	static Method getDecoderMethod(String className) {
		Method m = null;
		try {
			m = Class.forName(className).getDeclaredMethod("getDecoder", boolean.class);
			m.setAccessible(true);
		} catch (NoSuchMethodException e) {
			// Intentional fallthrough - getDecoder(boolean) is not always present.
		} catch (ClassNotFoundException e) {
			// Intentional fallthrough - getDecoder(boolean) is not always present.
		}
		return m;
	}
	
	private LombokFileObjects() {}
	
	static Compiler getCompiler(JavaFileManager jfm) {
		String jfmClassName = jfm != null ? jfm.getClass().getName() : "null";
		if (jfmClassName.equals("com.sun.tools.javac.util.DefaultFileManager")) return Compiler.JAVAC6;
		if (jfmClassName.equals("com.sun.tools.javac.util.JavacFileManager")) return Compiler.JAVAC6;
		if (jfmClassName.equals("com.sun.tools.javac.file.JavacFileManager") ||
				jfmClassName.equals("com.google.errorprone.MaskedClassLoader$MaskedFileManager") ||
				jfmClassName.equals("com.google.devtools.build.buildjar.javac.BlazeJavacMain$ClassloaderMaskingFileManager")) {
			try {
				Class<?> superType = Class.forName("com.sun.tools.javac.file.BaseFileManager");
				if (superType.isInstance(jfm)) {
					return new Java9Compiler(jfm);
				}
			}
			catch (Exception e) {}
			return Compiler.JAVAC7;
		}
		if (jfmClassName.equals("com.sun.tools.javac.api.ClientCodeWrapper$WrappedStandardJavaFileManager")) {
			try {
				Field wrappedField = Class.forName("com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileManager").getDeclaredField("clientJavaFileManager");
				wrappedField.setAccessible(true);
				JavaFileManager wrappedManager = (JavaFileManager)wrappedField.get(jfm);
				Class<?> superType = Class.forName("com.sun.tools.javac.file.BaseFileManager");
				if (superType.isInstance(wrappedManager)) {
					return new Java9Compiler(wrappedManager);
				}
			}
			catch (Exception e) {}
		}
		try {
			if (Class.forName("com.sun.tools.javac.file.BaseFileObject") == null) throw new NullPointerException();
			return Compiler.JAVAC7;
		} catch (Exception e) {}
		try {
			if (Class.forName("com.sun.tools.javac.util.BaseFileObject") == null) throw new NullPointerException();
			return Compiler.JAVAC6;
		} catch (Exception e) {}
		
		StringBuilder sb = new StringBuilder(jfmClassName);
		if (jfm != null) {
			sb.append(" extends ").append(jfm.getClass().getSuperclass().getName());
			for (Class<?> cls : jfm.getClass().getInterfaces()) {
				sb.append(" implements ").append(cls.getName());
			}
		}
		throw new IllegalArgumentException(sb.toString());
	}
	
	static JavaFileObject createEmpty(Compiler compiler, String name, Kind kind) {
		return compiler.wrap(new EmptyLombokFileObject(name, kind));
	}
	
	static JavaFileObject createIntercepting(Compiler compiler, JavaFileObject delegate, String fileName, DiagnosticsReceiver diagnostics) {
		return compiler.wrap(new InterceptingJavaFileObject(delegate, fileName, diagnostics, compiler.getDecoderMethod()));
	}
	
	static class Java9Compiler implements Compiler {
		private final BaseFileManager fileManager;
		
		public Java9Compiler(JavaFileManager jfm) {
			fileManager = (BaseFileManager) jfm;
		}
		
		@Override public JavaFileObject wrap(LombokFileObject fileObject) {
			URI uri = fileObject.toUri();
			if (uri.getScheme() == null) {
				uri = URI.create("file:///" + uri);
			}
			Path path;
			try {
				path = Paths.get(uri);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Problems in URI '" + uri + "' (" + fileObject.toUri() + ")", e);
			}
			return new Javac9BaseFileObjectWrapper(fileManager, path, fileObject);
		}
		
		@Override public Method getDecoderMethod() {
			throw new UnsupportedOperationException();
		}
	}
}
