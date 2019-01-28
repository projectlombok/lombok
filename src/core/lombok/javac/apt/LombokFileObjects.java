/*
 * Copyright (C) 2010-2019 The Project Lombok Authors.
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.javac.file.BaseFileManager;

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
				if (superType.isInstance(jfm)) {
					return new Java9Compiler(jfm);
				}
			}
			catch (Throwable e) {}
			return Compiler.JAVAC7;
		}
		if (KNOWN_JAVA9_FILE_MANAGERS.contains(jfmClassName)) {
			try {
				return new Java9Compiler(jfm);
			}
			catch (Throwable e) {}
		}
		try {
			if (Class.forName("com.sun.tools.javac.file.PathFileObject") == null) throw new NullPointerException();
			return new Java9Compiler(jfm);
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
	
	static class Java9Compiler implements Compiler {
		private final BaseFileManager fileManager;
		
		public Java9Compiler(JavaFileManager jfm) {
			fileManager = asBaseFileManager(jfm);
		}
		
		@Override public JavaFileObject wrap(LombokFileObject fileObject) {
			Path p; try {
				p = toPath(fileObject);
			} catch (Exception e) {
				p = null;
			}
			
			// J9BFOW extends javac's internal file base impl of javax.tools.JavaFileObject.
			// J9JFOW just straight implements it. Probably J9JFOW is fine, but we decided to extend java's internal impl possibly for a reason.
			// Some exotic build environments don't _have_ file objects and crash with FileNotFoundEx, so if that happens, let's try the alternative.
			if (p != null) return new Javac9BaseFileObjectWrapper(fileManager, p, fileObject);
			return new Javac9JavaFileObjectWrapper(fileObject);
		}
		
		@Override public Method getDecoderMethod() {
			return null;
		}
		
		private static Path toPath(LombokFileObject fileObject) {
			URI uri = fileObject.toUri();
			if (uri.getScheme() == null) {
				uri = URI.create("file:///" + uri);
			}
			try {
				return Paths.get(uri);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Problems in URI '" + uri + "' (" + fileObject.toUri() + ")", e);
			}
		}
		
		private static BaseFileManager asBaseFileManager(JavaFileManager jfm) {
			if (jfm instanceof BaseFileManager) {
				return (BaseFileManager) jfm;
			}
			return new FileManagerWrapper(jfm);
		}
		
		static class FileManagerWrapper extends BaseFileManager {
			JavaFileManager manager;
			
			public FileManagerWrapper(JavaFileManager manager) {
				super(null);
				this.manager = manager;
			}
			
			@Override
			public int isSupportedOption(String option) {
				return manager.isSupportedOption(option);
			}
			
			@Override
			public ClassLoader getClassLoader(Location location) {
				return manager.getClassLoader(location);
			}
			
			@Override
			public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
				return manager.list(location, packageName, kinds, recurse);
			}
			
			@Override
			public String inferBinaryName(Location location, JavaFileObject file) {
				return manager.inferBinaryName(location, file);
			}
			
			@Override
			public boolean isSameFile(FileObject a, FileObject b) {
				return manager.isSameFile(a, b);
			}
			
			@Override
			public boolean handleOption(String current, Iterator<String> remaining) {
				return manager.handleOption(current, remaining);
			}
			
			@Override
			public boolean hasLocation(Location location) {
				return manager.hasLocation(location);
			}
			
			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
				return manager.getJavaFileForInput(location, className, kind);
			}
			
			@Override
			public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
				return manager.getJavaFileForOutput(location, className, kind, sibling);
			}
			
			@Override
			public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
				return manager.getFileForInput(location, packageName, relativeName);
			}
			
			@Override
			public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
				return manager.getFileForOutput(location, packageName, relativeName, sibling);
			}
			
			@Override
			public void flush() throws IOException {
				manager.flush();
			}
			
			@Override
			public void close() throws IOException {
				manager.close();
			}
		}
	}
}
