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
package lombok.eclipse.agent;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Contains all the code to be transplanted into eclipse.
 * 
 * Do not use:
 * 
 * * Annotations
 * * Generics
 * * Varargs
 * * Auto (un)boxing
 * * class literals
 * 
 * The above because this code is compiled with -source 1.4, and is transplanted.
 * 
 * NB: The suppress warnings will be stripped out before compilation.
 */
@SuppressWarnings("all")
public class EclipseLoaderPatcherTransplants {
	public static boolean overrideLoadDecide(ClassLoader original, String name, boolean resolve) {
		return name.startsWith("lombok.");
	}
	
	public static Class overrideLoadResult(ClassLoader original, String name, boolean resolve) throws ClassNotFoundException {
		try {
			Field shadowLoaderField = original.getClass().getField("lombok$shadowLoader");
			ClassLoader shadowLoader = (ClassLoader) shadowLoaderField.get(original);
			if (shadowLoader == null) {
				synchronized ("lombok$shadowLoader$globalLock".intern()) {
					shadowLoader = (ClassLoader) shadowLoaderField.get(original);
					if (shadowLoader == null) {
						Class shadowClassLoaderClass = (Class) original.getClass().getField("lombok$shadowLoaderClass").get(null);
						Class classLoaderClass = Class.forName("java.lang.ClassLoader");
						String jarLoc = (String) original.getClass().getField("lombok$location").get(null);
						if (shadowClassLoaderClass == null) {
							JarFile jf = new JarFile(jarLoc);
							InputStream in = null;
							try {
								ZipEntry entry = jf.getEntry("lombok/launch/ShadowClassLoader.class");
								in = jf.getInputStream(entry);
								byte[] bytes = new byte[65536];
								int len = 0;
								while (true) {
									int r = in.read(bytes, len, bytes.length - len);
									if (r == -1) break;
									len += r;
									if (len == bytes.length) throw new IllegalStateException("lombok.launch.ShadowClassLoader too large.");
								}
								in.close();
								
								try {
									/* Since Java 16 reflective access to ClassLoader.defineClass is no longer permitted. The recommended solution 
									 * is to use MethodHandles.lookup().defineClass which is useless here because it is limited to classes in the
									 * same package. Fortunately this code gets transplanted into a ClassLoader and we can call the parent method
									 * using a MethodHandle. To support old Java versions we use a reflective version of the code snippet below.
									 * 
									 * Lookup lookup = MethodHandles.lookup();
									 * MethodType type = MethodType.methodType(Class.class, new Class[] {String.class, byte[].class, int.class, int.class});
									 * MethodHandle method = lookup.findVirtual(original.getClass(), "defineClass", type);
									 * shadowClassLoaderClass = (Class) method.invokeWithArguments(original, "lombok.launch.ShadowClassLoader", bytes, new Integer(0), new Integer(len)}) 
									 */
									Class methodHandles = Class.forName("java.lang.invoke.MethodHandles");
									Class methodHandle = Class.forName("java.lang.invoke.MethodHandle");
									Class methodType = Class.forName("java.lang.invoke.MethodType");
									Class methodHandlesLookup = Class.forName("java.lang.invoke.MethodHandles$Lookup");
									Method lookupMethod = methodHandles.getDeclaredMethod("lookup", null);
									Method methodTypeMethod = methodType.getDeclaredMethod("methodType", new Class[] {Class.class, Class[].class});
									Method findVirtualMethod = methodHandlesLookup.getDeclaredMethod("findVirtual", new Class[] {Class.class, String.class, methodType});
									Method invokeMethod = methodHandle.getDeclaredMethod("invokeWithArguments", new Class[] {Object[].class});
									
									Object lookup = lookupMethod.invoke(null, null);
									Object type = methodTypeMethod.invoke(null, new Object[] {Class.class, new Class[] {String.class, byte[].class, int.class, int.class}});
									Object method = findVirtualMethod.invoke(lookup, new Object[] {original.getClass(), "defineClass", type});
									shadowClassLoaderClass = (Class) invokeMethod.invoke(method, new Object[] {new Object[] {original, "lombok.launch.ShadowClassLoader", bytes, new Integer(0), new Integer(len)}});
								} catch (ClassNotFoundException e) {
									// Ignore, old Java
								}
								if (shadowClassLoaderClass == null)
								{
									Class[] paramTypes = new Class[4];
									paramTypes[0] = "".getClass();
									paramTypes[1] = new byte[0].getClass();
									paramTypes[2] = Integer.TYPE;
									paramTypes[3] = paramTypes[2];
									Method defineClassMethod = classLoaderClass.getDeclaredMethod("defineClass", paramTypes);
									defineClassMethod.setAccessible(true);
									shadowClassLoaderClass = (Class) defineClassMethod.invoke(original, new Object[] {"lombok.launch.ShadowClassLoader", bytes, new Integer(0), new Integer(len)});
								}
								original.getClass().getField("lombok$shadowLoaderClass").set(null, shadowClassLoaderClass);
							} finally {
								if (in != null) in.close();
								jf.close();
							}
						}
						Class[] paramTypes = new Class[5];
						paramTypes[0] = classLoaderClass;
						paramTypes[1] = "".getClass();
						paramTypes[2] = paramTypes[1];
						paramTypes[3] = Class.forName("java.util.List");
						paramTypes[4] = paramTypes[3];
						Constructor constructor = shadowClassLoaderClass.getDeclaredConstructor(paramTypes);
						constructor.setAccessible(true);
						shadowLoader = (ClassLoader) constructor.newInstance(new Object[] {original, "lombok", jarLoc, Arrays.asList(new Object[] {"lombok."}), Arrays.asList(new Object[] {"lombok.patcher.Symbols"})});
						shadowLoaderField.set(original, shadowLoader);
					}
				}
			}
			
			if (resolve) {
				Class[] paramTypes = new Class[2];
				paramTypes[0] = "".getClass();
				paramTypes[1] = Boolean.TYPE;
				Method m = shadowLoader.getClass().getDeclaredMethod("loadClass", new Class[] {String.class, boolean.class});
				m.setAccessible(true);
				return (Class) m.invoke(shadowLoader, new Object[] {name, Boolean.TRUE});
			} else {
				return shadowLoader.loadClass(name);
			}
		} catch (Exception ex) {
			Throwable t = ex;
			if (t instanceof InvocationTargetException) t = t.getCause();
			if (t instanceof RuntimeException) throw (RuntimeException) t;
			if (t instanceof Error) throw (Error) t;
			throw new RuntimeException(t);
		}
	}
}
