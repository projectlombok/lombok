/*
 * Copyright (C) 2014-2015 The Project Lombok Authors.
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
package lombok.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class Main {
	private static ShadowClassLoader classLoader;
	
	static synchronized ClassLoader getShadowClassLoader() {
		if (classLoader == null) {
			classLoader = new ShadowClassLoader(Main.class.getClassLoader(), "lombok", null, Arrays.<String>asList(), Arrays.asList("lombok.patcher.Symbols"));
		}
		return classLoader;
	}
	
	static synchronized void prependClassLoader(ClassLoader loader) {
		getShadowClassLoader();
		classLoader.prepend(loader);
	}
	
	public static void main(String[] args) throws Throwable {
		ClassLoader cl = getShadowClassLoader();
		Class<?> mc = cl.loadClass("lombok.core.Main");
		try {
			mc.getMethod("main", String[].class).invoke(null, new Object[] {args});
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
