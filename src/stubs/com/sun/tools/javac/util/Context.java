/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.util;

public class Context {
	public static class Key<T> {
	}
	
	public interface Factory<T> {
		T make(Context c);
		T make();
	}
	
	public <T> void put(Key<T> key, Factory<T> fac) {
	}
	
	public <T> void put(Key<T> key, T data) {
	}
	
	public <T> void put(Class<T> clazz, T data) {
	}
	
	public <T> T get(Key<T> key) {
		return null;
	}
	
	public <T> T get(Class<T> clazz) {
		return null;
	}
}
