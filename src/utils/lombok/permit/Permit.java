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
			f = AccessibleObject.class.getDeclaredField("override");
			g = UNSAFE.objectFieldOffset(f);
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
