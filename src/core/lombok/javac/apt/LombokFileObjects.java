/*
 * Copyright (C) 2010-2020 The Project Lombok Authors.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import lombok.core.DiagnosticsReceiver;
import lombok.permit.Permit;

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
		try {
			return Permit.getMethod(Class.forName(className), "getDecoder", boolean.class);
		} catch (NoSuchMethodException e) {
			// Intentional fallthrough - getDecoder(boolean) is not always present.
		} catch (ClassNotFoundException e) {
			// Intentional fallthrough - getDecoder(boolean) is not always present.
		}
		return null;
	}
	
	private LombokFileObjects() {}
	
	private static final List<String> KNOWN_JAVA9_FILE_MANAGERS = Arrays.asList(
		"com.google.errorprone.MaskedClassLoader$MaskedFileManager",
		"com.google.devtools.build.buildjar.javac.BlazeJavacMain$ClassloaderMaskingFileManager",
		"com.google.devtools.build.java.turbine.javac.JavacTurbineCompiler$ClassloaderMaskingFileManager",
		"org.netbeans.modules.java.source.parsing.ProxyFileManager",
		"com.sun.tools.javac.api.ClientCodeWrapper$WrappedStandardJavaFileManager",
		"com.sun.tools.javac.main.DelegatingJavaFileManager$DelegatingSJFM" // IntelliJ + JDK10
	);
	
	static Compiler getCompiler(JavaFileManager jfm) {
		String jfmClassName = jfm != null ? jfm.getClass().getName() : "null";
		if (jfmClassName.equals("com.sun.tools.javac.util.DefaultFileManager")) return Compiler.JAVAC6;
		if (jfmClassName.equals("com.sun.tools.javac.util.JavacFileManager")) return Compiler.JAVAC6;
		if (jfmClassName.equals("com.sun.tools.javac.file.JavacFileManager")) {
			try {
				Class<?> superType = Class.forName("com.sun.tools.javac.file.BaseFileManager");
				if (superType.isInstance(jfm)) return java9Compiler(jfm);
			}
			catch (Throwable e) {}
			return Compiler.JAVAC7;
		}
		if (KNOWN_JAVA9_FILE_MANAGERS.contains(jfmClassName)) {
			try {
				return java9Compiler(jfm);
			}
			catch (Throwable e) {}
		}
		try {
			if (Class.forName("com.sun.tools.javac.file.PathFileObject") == null) throw new NullPointerException();
			return java9Compiler(jfm);
		} catch (Throwable e) {}
		try {
			if (Class.forName("com.sun.tools.javac.file.BaseFileObject") == null) throw new NullPointerException();
			return Compiler.JAVAC7;
		} catch (Throwable e) {}
		try {
			if (Class.forName("com.sun.tools.javac.util.BaseFileObject") == null) throw new NullPointerException();
			return Compiler.JAVAC6;
		} catch (Throwable e) {}
		
		StringBuilder sb = new StringBuilder(jfmClassName);
		if (jfm != null) {
			sb.append(" extends ").append(jfm.getClass().getSuperclass().getName());
			for (Class<?> cls : jfm.getClass().getInterfaces()) {
				sb.append(" implements ").append(cls.getName());
			}
		}
		throw new IllegalArgumentException(sb.toString());
	}
	
	static JavaFileObject createIntercepting(Compiler compiler, JavaFileObject delegate, String fileName, DiagnosticsReceiver diagnostics) {
		return compiler.wrap(new InterceptingJavaFileObject(delegate, fileName, diagnostics, compiler.getDecoderMethod()));
	}
	
	private static Constructor<?> j9CompilerConstructor = null;
	private static Compiler java9Compiler(JavaFileManager jfm) {
		try {
			if (j9CompilerConstructor == null) j9CompilerConstructor = Class.forName("lombok.javac.apt.Java9Compiler").getConstructor(JavaFileManager.class);
			return (Compiler) j9CompilerConstructor.newInstance(jfm);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof RuntimeException) throw (RuntimeException) t;
			if (t instanceof Error) throw (Error) t;
			throw new RuntimeException(t);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
