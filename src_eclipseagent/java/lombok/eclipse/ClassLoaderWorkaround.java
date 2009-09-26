/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
package java.lombok.eclipse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Allows you to inject the lombok classes into any classloader, even if that classloader does not
 * know how to find the lombok classes.
 * 
 * Example: Injecting lombok's Eclipse Parser patching code into eclipse's OSGi BundleLoader.
 * 
 * @author rzwitserloot
 */
public class ClassLoaderWorkaround {
	static RuntimeException sneakyThrow(Throwable t) {
		if ( t == null ) throw new NullPointerException("t");
		ClassLoaderWorkaround.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
	
	private static final Map<ClassLoader, Method> transform = new HashMap<ClassLoader, Method>();
	
	public static void transformCompilationUnitDeclarationSwapped(Object cud, Object parser) throws Exception {
		transformCompilationUnitDeclaration(parser, cud);
	}
	
	public static void transformCompilationUnitDeclaration(Object parser, Object cud) throws Exception {
		Method transformMethod = getTransformMethod(cud);
		try {
			transformMethod.invoke(null, parser, cud);
		} catch ( InvocationTargetException e ) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	private static Method getTransformMethod(Object cud) throws ClassNotFoundException {
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		synchronized ( transform ) {
			if ( !transform.containsKey(contextLoader)) {
				transform.put(contextLoader, findTransformMethod(cud));
			}
			
			Method m = transform.get(contextLoader);
			if ( m == null ) throw new ClassNotFoundException("lombok.eclipse.TransformEclipseAST");
			return m;
			
		}
	}

	private static Method findTransformMethod(Object cud) throws ClassNotFoundException {
		final ClassLoader parent = cud.getClass().getClassLoader();
		ClassLoader loader = new ClassLoader() {
			@Override public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				if ( name.startsWith("lombok.") ) {
					InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(name.replace(".", "/") + ".class");
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					
					byte[] b = new byte[65536];
					try {
						while ( true ) {
							int r = in.read(b);
							if ( r == -1 ) break;
							if ( r > 0 ) out.write(b, 0, r);
						}
						
						in.close();
						byte[] data = out.toByteArray();
						Class<?> result = defineClass(name, data, 0, data.length);
						if ( resolve ) resolveClass(result);
						return result;
					} catch ( IOException e ) {
						throw new ClassNotFoundException();
					}
				} else {
					try {
						Class<?> result = ClassLoader.getSystemClassLoader().loadClass(name);
						if ( resolve ) resolveClass(result);
						return result;
					} catch ( ClassNotFoundException e ) {
						Class<?> result = parent.loadClass(name);
						if ( resolve ) resolveClass(result);
						return result;
					}
				}
			}
		};
		
		Class<?> c = loader.loadClass("lombok.eclipse.TransformEclipseAST");
		for ( Method method : c.getMethods() ) {
			if ( method.getName().equals("transform") ) {
				Class<?>[] types = method.getParameterTypes();
				if ( types.length != 2 ) continue;
				if ( !types[0].getName().equals("org.eclipse.jdt.internal.compiler.parser.Parser") ) continue;
				if ( !types[1].getName().equals("org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration") ) continue;
				return method;
			}
		}
		
		throw new ClassNotFoundException("lombok.eclipse.TransformEclipseAST");
	}
}
