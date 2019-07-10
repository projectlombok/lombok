/*
 * Copyright (C) 2018-20199 The Project Lombok Authors.
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
package lombok.permit;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;

// sunapi suppresses javac's warning about using Unsafe; 'all' suppresses eclipse's warning about the unspecified 'sunapi' key. Leave them both.
// Yes, javac's definition of the word 'all' is quite contrary to what the dictionary says it means. 'all' does NOT include 'sunapi' according to javac.
@SuppressWarnings({"sunapi", "all"})
public class Permit {
	private Permit() {}
	
	
	private static final long ACCESSIBLE_OVERRIDE_FIELD_OFFSET;
	private static final IllegalAccessException INIT_ERROR;
	private static final sun.misc.Unsafe UNSAFE = (sun.misc.Unsafe) reflectiveStaticFieldAccess(sun.misc.Unsafe.class, "theUnsafe");
	
	static {
		Field f;
		long g;
		Throwable ex;
		
		try {
			g = getOverrideFieldOffset();
			ex = null;
		} catch (Throwable t) {
			f = null;
			g = -1L;
			ex = t;
		}
		
		ACCESSIBLE_OVERRIDE_FIELD_OFFSET = g;
		if (ex == null) INIT_ERROR = null;
		else if (ex instanceof IllegalAccessException) INIT_ERROR = (IllegalAccessException) ex;
		else {
			INIT_ERROR = new IllegalAccessException("Cannot initialize Unsafe-based permit");
			INIT_ERROR.initCause(ex);
		}
	}
	
	public static <T extends AccessibleObject> T setAccessible(T accessor) {
		if (INIT_ERROR == null) {
			UNSAFE.putBoolean(accessor, ACCESSIBLE_OVERRIDE_FIELD_OFFSET, true);
		} else {
			accessor.setAccessible(true);
		}
		
		return accessor;
	}
	
	private static long getOverrideFieldOffset() throws Throwable {
		Field f = null;
		Throwable saved = null;
		try {
			f = AccessibleObject.class.getDeclaredField("override");
		} catch (Throwable t) {
			saved = t;
		}
		
		if (f != null) {
			return UNSAFE.objectFieldOffset(f);
		}
		// The below seems very risky, but for all AccessibleObjects in java today it does work, and starting with JDK12, making the field accessible is no longer possible.
		try {
			return UNSAFE.objectFieldOffset(Fake.class.getDeclaredField("override"));
		} catch (Throwable t) {
			throw saved;
		}
	}
	
	static class Fake {
		boolean override;
	}
	
	public static Method getMethod(Class<?> c, String mName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Method m = null;
		Class<?> oc = c;
		while (c != null) {
			try {
				m = c.getDeclaredMethod(mName, parameterTypes);
				break;
			} catch (NoSuchMethodException e) {}
			c = c.getSuperclass();
		}
		
		if (m == null) throw new NoSuchMethodException(oc.getName() + " :: " + mName + "(args)");
		return setAccessible(m);
	}
	
	public static Field getField(Class<?> c, String fName) throws NoSuchFieldException {
		Field f = null;
		Class<?> oc = c;
		while (c != null) {
			try {
				f = c.getDeclaredField(fName);
				break;
			} catch (NoSuchFieldException e) {}
			c = c.getSuperclass();
		}
		
		if (f == null) throw new NoSuchFieldException(oc.getName() + " :: " + fName);
		
		return setAccessible(f);
	}
	
	public static Field permissiveGetField(Class<?> c, String fName) {
		try {
			return getField(c, fName);
		} catch (Exception ignore) {
			return null;
		}
	}
	
	public static <T> T permissiveReadField(Class<T> type, Field f, Object instance) {
		try {
			return type.cast(f.get(instance));
		} catch (Exception ignore) {
			return null;
		}
	}
	
	public static <T> Constructor<T> getConstructor(Class<T> c, Class<?>... parameterTypes) throws NoSuchMethodException {
		return setAccessible(c.getDeclaredConstructor(parameterTypes));
	}
	
	private static Object reflectiveStaticFieldAccess(Class<?> c, String fName) {
		try {
			Field f = c.getDeclaredField(fName);
			f.setAccessible(true);
			return f.get(null);
		} catch (Exception e) {
			return null;
		}
	}
}
