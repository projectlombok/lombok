/*
 * Copyright (C) 2014 The Project Lombok Authors.
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
package lombok.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Augments a instance of a type with a boolean field.
 * <p>
 * If the type already declares a boolean field, that field is used. Otherwise the field will be augmented.
 * 
 * @param <T> the type to augment.
 */
public abstract class BooleanFieldAugment<T> {
	
	/**
	 * Augments a instance of a type with a boolean field.
	 * <p>
	 * If the type already declares a boolean instance field, that field might be used. Otherwise the field will be augmented.
	 * <p>
	 * This code assumes that for any combination of {@code type} and {@code name} this method is only called once.
	 * Otherwise, whether state is shared is undefined.
	 * 
	 * @param type to augment
	 * @param name of the field
	 * @throws NullPointerException if {@code type} or {@code name} is {@code null}
	 */
	public static <T> BooleanFieldAugment<T> augment(Class<T> type, String name) {
		checkNotNull(type, "type");
		checkNotNull(name, "name");
		Field booleanField = getBooleanField(type, name);
		if (booleanField == null) {
			return new MapFieldAugment<T>();
		}
		return new ExistingFieldAugment<T>(booleanField);
	}
	
	private BooleanFieldAugment() {
		// prevent external instantiation
	}
	
	private static Field getBooleanField(Class<?> type, String name) {
		try {
			Field result = type.getDeclaredField(name);
			if (Modifier.isStatic(result.getModifiers()) || result.getType() != boolean.class) {
				return null;
			}
			result.setAccessible(true);
			return result;
		} catch (Throwable t) {
			return null;
		}
	}
	
	/**
	 * Sets the field to {@code true}.
	 * @returns the previous value
	 * @throws NullPointerException if {@code object} is {@code null}
	 */
	public abstract boolean set(T object);
	
	/**
	 * Sets the field to {@code false}.
	 * @returns the previous value
	 * @throws NullPointerException if {@code object} is {@code null}
	 */
	public abstract boolean clear(T object);
	
	/**
	 * @eturn {code true} if the field is set, otherwise {@code false}.
	 * @throws NullPointerException if {@code object} is {@code null}
	 */
	public abstract boolean get(T object);
	
	private static class MapFieldAugment<T> extends BooleanFieldAugment<T> {
		private static final Object MARKER = new Object();
		
		private final Map<T, Object> values = new WeakHashMap<T,Object>();
		
		public boolean set(T object) {
			checkNotNull(object, "object");
			synchronized (values) {
				return values.put(object, MARKER) != null;
			}
		}
		
		public boolean clear(T object) {
			checkNotNull(object, "object");
			synchronized (values) {
				return values.remove(object) != null;
			}
		}
		
		public boolean get(T object) {
			checkNotNull(object, "object");
			synchronized (values) {
				return values.get(object) != null;
			}
		}
	}
	
	private static class ExistingFieldAugment<T> extends BooleanFieldAugment<T> {
		private final Object lock = new Object();
		private final Field booleanField;
		
		private ExistingFieldAugment(Field booleanField) {
			this.booleanField = booleanField;
		}
		
		@Override public boolean set(T object) {
			checkNotNull(object, "object");
			try {
				synchronized (lock) {
					boolean result = booleanField.getBoolean(object);
					booleanField.setBoolean(object, true);
					return result;
				}
			} catch (IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}
		
		@Override public boolean clear(T object) {
			checkNotNull(object, "object");
			try {
				synchronized (lock) {
					boolean result = booleanField.getBoolean(object);
					booleanField.setBoolean(object, false);
					return result;
				}
			} catch (IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}
		
		@Override public boolean get(T object) {
			checkNotNull(object, "object");
			try {
				synchronized (lock) {
					return booleanField.getBoolean(object);
				}
			} catch (IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}
	}
	
	private static <T> T checkNotNull(T object, String name) {
		if (object == null) throw new NullPointerException(name);
		return object;
	}
	
	private static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) throw new NullPointerException("t");
		BooleanFieldAugment.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
}