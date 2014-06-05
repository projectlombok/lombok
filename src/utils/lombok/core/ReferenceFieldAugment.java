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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class ReferenceFieldAugment<T, F> {
	
	/**
	 * Augments a instance of a type with a reference field.
	 * <p>
	 * If the type already declares an instance field with the given name and field type, that field might be used. Otherwise the field will be augmented.
	 * <p>
	 * This code assumes that for any combination of {@code type} and {@code name} this method is only called once.
	 * Otherwise, whether state is shared is undefined.
	 * 
	 * @param type to augment
	 * @param fieldType type of the field
	 * @param name of the field
	 * @throws NullPointerException if {@code type}, {@code fieldType} or {@code name} is {@code null}
	 */
	public static <T, F> ReferenceFieldAugment<T, F> augment(Class<T> type, Class<? super F> fieldType, String name) {
		ReferenceFieldAugment<T, F> ret = tryCreateReflectionAugment(type, fieldType, name);
		return ret != null ? ret : new MapFieldAugment<T, F>();
	}
	
	/**
	 * Augments a instance of a type with a weak reference field.
	 * <p>
	 * If the type already declares an instance field with the given name and field type, that field might be used. Otherwise the field will be augmented.
	 * <p>
	 * This code assumes that for any combination of {@code type} and {@code name} this method is only called once.
	 * Otherwise, whether state is shared is undefined.
	 * 
	 * @param type to augment
	 * @param fieldType type of the field
	 * @param name of the field
	 * @throws NullPointerException if {@code type}, {@code fieldType} or {@code name} is {@code null}
	 */
	public static <T, F> ReferenceFieldAugment<T, F> augmentWeakField(Class<T> type, Class<? super F> fieldType, String name) {
		ReferenceFieldAugment<T, F> ret = tryCreateReflectionAugment(type, fieldType, name);
		return ret != null ? ret : new MapWeakFieldAugment<T, F>();
	}
	
	/**
	 * Creates a reflection-based augment which will directly access the listed field name. If this field does not exist or the field
	 * is not capable of storing the requested type, {@code null} is returned instead.
	 */
	private static <T, F> ReferenceFieldAugment<T, F> tryCreateReflectionAugment(Class<T> type, Class<? super F> fieldType, String name) {
		Field f = findField(type, name);
		if (f != null && typeIsAssignmentCompatible(f.getType(), fieldType)) return new ReflectionFieldAugment<T, F>(f, fieldType);
		return null;
	}
	
	/**
	 * Finds the named <em>instance</em> field in the type, or in any of its supertypes, regardless of its access modifier. It's set as accessible and returned if found.
	 */
	private static Field findField(Class<?> type, String name) {
		while (type != null) {
			try {
				Field f = type.getDeclaredField(name);
				if (!Modifier.isStatic(f.getModifiers())) {
					f.setAccessible(true);
					return f;
				}
			} catch (NoSuchFieldException fallthrough) {}
			type = type.getSuperclass();
		}
		
		return null;
	}
	
	private static boolean typeIsAssignmentCompatible(Class<?> fieldType, Class<?> wantedType) {
		if (Modifier.isFinal(fieldType.getModifiers())) return false;
		if (Modifier.isStatic(fieldType.getModifiers())) return false;
		
		if (fieldType == java.lang.Object.class) return true;
		if (fieldType == wantedType) return true;
		
		if (fieldType.isPrimitive()) return fieldType == wantedType;
		if (wantedType == int.class && (fieldType == Number.class || fieldType == Integer.class)) return true;
		if (wantedType == long.class && (fieldType == Number.class || fieldType == Long.class)) return true;
		if (wantedType == short.class && (fieldType == Number.class || fieldType == Short.class)) return true;
		if (wantedType == byte.class && (fieldType == Number.class || fieldType == Byte.class)) return true;
		if (wantedType == char.class && (fieldType == Number.class || fieldType == Character.class)) return true;
		if (wantedType == float.class && (fieldType == Number.class || fieldType == Float.class)) return true;
		if (wantedType == double.class && (fieldType == Number.class || fieldType == Double.class)) return true;
		if (wantedType == boolean.class && fieldType == Boolean.class) return true;
		
		return fieldType.isAssignableFrom(wantedType);
	}
	
	private ReferenceFieldAugment() {
		// prevent external instantiation
	}
	
	/**
	 * @throws NullPointerException if {@code object} is {@code null}
	 */
	public abstract F get(T object);
	
	/**
	 * @throws NullPointerException if {@code object} or {@code value} is {@code null}
	 */
	public final void set(T object, F value) {
		getAndSet(object, value);
	}
	
	/**
	 * @return the value of the field <strong>before</strong> the operation.
	 * @throws NullPointerException if {@code object} or {@code value} is {@code null}.
	 */
	public abstract F getAndSet(T object, F value);
	
	/**
	 * @return the value of the field <strong>before</strong> the operation.
	 * @throws NullPointerException if {@code object} is {@code null}
	 */
	public abstract F clear(T object);
	
	/**
	 * @return the value of the field <strong>after</strong> the operation. If the value was equal to {@code expected} or already cleared {@code null}, otherwise the current value.
	 * @throws NullPointerException if {@code object} or {@code expected} is {@code null}
	 */
	public abstract F compareAndClear(T object, F expected);
	
	/**
	 * @return the value of the field <strong>after</strong> the operation.
	 * @throws NullPointerException if {@code object} or {@code value} is {@code null}
	 */
	public abstract F setIfAbsent(T object, F value);
	
	/**
	 * @return the value of the field <strong>after</strong> the operation.
	 * @throws NullPointerException if {@code object}, {@code expected} or {@code value} is {@code null}
	 */
	public abstract F compareAndSet(T object, F expected, F value);
	
	private static class ReflectionFieldAugment<T, F> extends ReferenceFieldAugment<T, F> {
		private final Field field;
		private final Class<F> targetType;
		
		@SuppressWarnings("unchecked")
		ReflectionFieldAugment(Field field, Class<? super F> targetType) {
			this.field = field;
			this.targetType = (Class<F>) targetType;
		}
		
		@Override public F get(T object) {
			checkNotNull(object, "object");
			try {
				return targetType.cast(field.get(object));
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F getAndSet(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			try {
				F oldValue = targetType.cast(field.get(object));
				field.set(object, value);
				return oldValue;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F clear(T object) {
			checkNotNull(object, "object");
			try {
				F oldValue = targetType.cast(field.get(object));
				field.set(object, null);
				return oldValue;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F compareAndClear(T object, F expected) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			try {
				F result = targetType.cast(field.get(object));
				if (result == null) return null;
				if (!expected.equals(result)) return result;
				field.set(object, null);
				return null;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F setIfAbsent(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			try {
				F result = targetType.cast(field.get(object));
				if (result != null) return result;
				field.set(object, value);
				return value;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F compareAndSet(T object, F expected, F value) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			checkNotNull(value, "value");
			try {
				F result = targetType.cast(field.get(object));
				if (!expected.equals(result)) return result;
				field.set(object, value);
				return value;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	private static class MapFieldAugment<T, F> extends ReferenceFieldAugment<T, F> {
		final Map<T, Object> values = new WeakHashMap<T, Object>();
		
		@Override
		public F get(T object) {
			checkNotNull(object, "object");
			synchronized (values) {
				return read(object);
			}
		}
		
		@Override
		public F getAndSet(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			synchronized (values) {
				F result = read(object);
				write(object, value);
				return result;
			}
		}
		
		@Override
		public F clear(T object) {
			checkNotNull(object, "object");
			synchronized (values) {
				F result = read(object);
				values.remove(object);
				return result;
			}
		}
		
		@Override
		public F compareAndClear(T object, F expected) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			synchronized (values) {
				F result = read(object);
				if (result == null) {
					return null;
				}
				if (!expected.equals(result)) {
					return result;
				}
				values.remove(object);
				return null;
			}
		}
		
		@Override
		public F setIfAbsent(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			synchronized (values) {
				F result = read(object);
				if (result != null) {
					return result;
				}
				write(object, value);
				return value;
			}
		}
		
		@Override
		public F compareAndSet(T object, F expected, F value) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			checkNotNull(value, "value");
			synchronized (values) {
				F result = read(object);
				if (!expected.equals(result)) {
					return result;
				}
				write(object, value);
				return value;
			}
		}
		
		@SuppressWarnings("unchecked")
		F read(T object) {
			return (F)values.get(object);
		}
		
		void write(T object, F value) {
			values.put(object, value);
		}
	}
	
	static class MapWeakFieldAugment<T, F> extends MapFieldAugment<T, F> {
		
		@SuppressWarnings("unchecked")
		F read(T object) {
			WeakReference<F> read = (WeakReference<F>)values.get(object);
			if (read == null) return null;
			F result = read.get();
			if (result == null) values.remove(object);
			return result;
		}
		
		void write(T object, F value) {
			values.put(object, new WeakReference<F>(value));
		}
	}
	
	private static <T> T checkNotNull(T object, String name) {
		if (object == null) throw new NullPointerException(name);
		return object;
	}
}
