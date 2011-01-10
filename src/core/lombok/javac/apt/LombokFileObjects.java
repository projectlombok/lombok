/*
 * Copyright © 2010-2011 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import java.lang.reflect.Method;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import lombok.core.DiagnosticsReceiver;

//Can't use SimpleJavaFileObject so we copy/paste most of its content here, because javac doesn't follow the interface,
//and casts to its own BaseFileObject type. D'oh!
final class LombokFileObjects {
	
	private enum Compiler {
		JAVAC6 {
			@Override public JavaFileObject wrap(JavaFileManager fileManager, LombokFileObject fileObject) {
				return new Javac6BaseFileObjectWrapper(fileObject);
			}
		}, 
		JAVAC7 {
			@Override public JavaFileObject wrap(JavaFileManager fileManager, LombokFileObject fileObject) {
				return new Javac7BaseFileObjectWrapper((com.sun.tools.javac.file.JavacFileManager)fileManager, fileObject);
			}
		};
		
		abstract JavaFileObject wrap(JavaFileManager fileManager, LombokFileObject fileObject);
	}
	
	private static final Compiler compiler;
	private static final Method decoderMethod;
	
	static {
		Compiler c = null;
		Method m = null;
		try {
			// In javac6, the BaseFileObject is located in 
			// com.sun.tools.javac.util
			Class<?> clazz = Class.forName("com.sun.tools.javac.util.BaseFileObject");
			c = Compiler.JAVAC6;
			try {
				// The getDecoder method is not always present in javac6
				m = clazz.getDeclaredMethod("getDecoder", boolean.class);
			} catch (NoSuchMethodException e) {}
		} catch (ClassNotFoundException cnfe) {
			// In javac7, the BaseFileObject has been moved to the package 
			// com.sun.tools.javac.file
			try {
				Class<?> clazz = Class.forName("com.sun.tools.javac.file.BaseFileObject");
				c = Compiler.JAVAC7;
				try {
					m = clazz.getDeclaredMethod("getDecoder", boolean.class);
				} catch (NoSuchMethodException e) {}
			} catch (ClassNotFoundException cnfe2) {
			}
		}
		compiler = c;
		if (m != null) {
			m.setAccessible(true);
		}
		decoderMethod = m;
	}
	
	private LombokFileObjects() {}
	
	static JavaFileObject createEmpty(JavaFileManager fileManager, String name, Kind kind) {
		return compiler.wrap(fileManager, new EmptyLombokFileObject(name, kind));
	}
	
	static JavaFileObject createIntercepting(JavaFileManager fileManager, JavaFileObject delegate, String fileName, DiagnosticsReceiver diagnostics) {
		return compiler.wrap(fileManager, new InterceptingJavaFileObject(delegate, fileName, diagnostics, decoderMethod));
	}
}
