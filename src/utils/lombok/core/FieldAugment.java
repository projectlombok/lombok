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
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.permit.Permit;

public abstract class FieldAugment<T, F> {
	private static Object getDefaultValue(Class<?> type) {
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		if (!type.isPrimitive()) return null;
		
		if (type == long.class) return 0L;
		if (type == short.class) return (short) 0;
		if (type == byte.class) return (byte) 0;
		if (type == char.class) return '\0';
		if (type == float.class) return 0.0F;
		if (type == double.class) return 0.0D;
		
		// We can't get here unless java added some primitive types, but, hey.
		return null;
	}
	
	/**
	 * (Virtually) adds a field to an existing type and returns an object that can be used to read and write this field.
	 * <p>
	 * If the type already declares a non-final instance field with the given name and a compatible field type, that field will be used.
	 * Otherwise the field will be provided virtually.
	 * <p>
	 * <em>WARNING</em>: The values put into the augment should NOT reference in any way the object you've added the augment to, or memory leaks may occur.
	 * If you do need to add such references, use {@link #circularSafeAugment(Class, Class, String, Object)} instead.
	 * <p>
	 * This code assumes that for any combination of {@code type} and {@code name} this method is only called once.
	 * Otherwise, whether state is shared is undefined.
	 * 
	 * @param type to augment
	 * @param fieldType type of the field
	 * @param name of the field
	 * @param defaultValue the value of the augment if it hasn't been set yet.
	 * @throws NullPointerException if {@code type}, {@code fieldType} or {@code name} is {@code null}
	 */
	public static <T, F> FieldAugment<T, F> augment(Class<T> type, Class<? super F> fieldType, String name) {
		checkNotNull(type, "type");
		checkNotNull(fieldType, "fieldType");
		checkNotNull(name, "name");
		
		if (type.isInterface()) {
			return new InterfaceFieldAugment<T, F>(name, fieldType);
		}
		
		@SuppressWarnings("unchecked")
		F defaultValue = (F) getDefaultValue(fieldType);
		FieldAugment<T, F> ret = tryCreateReflectionAugment(type, fieldType, name, defaultValue);
		return ret != null ? ret : new MapFieldAugment<T, F>(defaultValue);
	}
	
	/**
	 * (Virtually) adds a field to an existing type and returns an object that can be used to read and write this field.
	 * <p>
	 * This method does the same as {@link #augment(Class, Class, String, Object)}, except it is safe to set values that reference back to their containing object.
	 */
	public static <T, F> FieldAugment<T, F> circularSafeAugment(Class<T> type, Class<? super F> fieldType, String name) {
		checkNotNull(type, "type");
		checkNotNull(fieldType, "fieldType");
		checkNotNull(name, "name");
		
		@SuppressWarnings("unchecked")
		F defaultValue = (F) getDefaultValue(fieldType);
		FieldAugment<T, F> ret = tryCreateReflectionAugment(type, fieldType, name, defaultValue);
		return ret != null ? ret : new MapWeakFieldAugment<T, F>(defaultValue);
	}
	
	/**
	 * Creates a reflection-based augment which will directly access the listed field name. If this field does not exist or the field
	 * is not capable of storing the requested type, {@code null} is returned instead.
	 */
	private static <T, F> FieldAugment<T, F> tryCreateReflectionAugment(Class<T> type, Class<? super F> fieldType, String name, F defaultValue) {
		Field f = findField(type, fieldType, name);
		if (f != null && typeIsAssignmentCompatible(f.getType(), fieldType)) return new ReflectionFieldAugment<T, F>(f, fieldType, defaultValue);
		return null;
	}
	
	private static Field findField(Class<?> type, Class<?> wantedType, String name) {
		try {
			Field f = Permit.getField(type, name);
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) return null;
			if (!typeIsAssignmentCompatible(f.getType(), wantedType)) return null;
			return f;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static boolean typeIsAssignmentCompatible(Class<?> fieldType, Class<?> wantedType) {
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
	
	private FieldAugment() {
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
	
	private static final class InterfaceFieldAugment<T, F> extends FieldAugment<T, F> {
		private final String name;
		private final Class<? super F> fieldType;
		
		private Map<Class<T>, FieldAugment<T, F>> map = new HashMap<Class<T>, FieldAugment<T,F>>();
		
		private InterfaceFieldAugment(String name, Class<? super F> fieldType) {
			this.name = name;
			this.fieldType = fieldType;
		}
		
		private synchronized FieldAugment<T, F> getDelegate(T object) {
			@SuppressWarnings("unchecked")
			Class<T> c = (Class<T>) object.getClass();
			
			FieldAugment<T,F> fieldAugment = map.get(c);
			if (fieldAugment == null) {
				fieldAugment = augment(c, fieldType, name);
				map.put(c, fieldAugment);
			}
			return fieldAugment;
		}
		
		@Override public F get(T object) {
			return getDelegate(object).get(object);
		}
		
		@Override public F getAndSet(T object, F value) {
			return getDelegate(object).getAndSet(object, value);
		}
		
		@Override public F clear(T object) {
			return getDelegate(object).clear(object);
		}
		
		@Override public F compareAndClear(T object, F expected) {
			return getDelegate(object).compareAndClear(object, expected);
		}
		
		@Override public F setIfAbsent(T object, F value) {
			return getDelegate(object).setIfAbsent(object, value);
		}
		
		@Override public F compareAndSet(T object, F expected, F value) {
			return getDelegate(object).compareAndSet(object, expected, value);
		}
	}

	private static class ReflectionFieldAugment<T, F> extends FieldAugment<T, F> {
		private final Object lock = new Object();
		private final Field field;
		private final Class<F> targetType;
		private final F defaultValue;
		
		@SuppressWarnings("unchecked")
		ReflectionFieldAugment(Field field, Class<? super F> targetType, F defaultValue) {
			this.field = field;
			this.targetType = (Class<F>) targetType;
			this.defaultValue = defaultValue;
		}
		
		@Override public F get(T object) {
			checkNotNull(object, "object");
			try {
				F value;
				synchronized (lock) {
					value = targetType.cast(field.get(object));
				}
				return value == null ? defaultValue : value;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F getAndSet(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			try {
				F oldValue;
				synchronized (lock) {
					oldValue = targetType.cast(field.get(object));
					field.set(object, value);
				}
				return oldValue == null ? defaultValue : oldValue;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F clear(T object) {
			checkNotNull(object, "object");
			try {
				F oldValue;
				synchronized (lock) {
					oldValue = targetType.cast(field.get(object));
					field.set(object, defaultValue);
				}
				return oldValue == null ? defaultValue : oldValue;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F compareAndClear(T object, F expected) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			try {
				F oldValue;
				synchronized (lock) {
					oldValue = targetType.cast(field.get(object));
					if (expected.equals(oldValue)) {
						field.set(object, defaultValue);
						return defaultValue;
					}
				}
				return oldValue;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F setIfAbsent(T object, F value) {
			checkNotNull(object, "object");
			checkNotNull(value, "value");
			try {
				synchronized (lock) {
					F oldValue = targetType.cast(field.get(object));
					if (oldValue != null && !oldValue.equals(defaultValue)) return oldValue;
					field.set(object, value);
					return value;
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override public F compareAndSet(T object, F expected, F value) {
			checkNotNull(object, "object");
			checkNotNull(expected, "expected");
			checkNotNull(value, "value");
			try {
				synchronized (lock) {
					F oldValue = targetType.cast(field.get(object));
					if (!expected.equals(oldValue)) return oldValue == null ? defaultValue : oldValue;
					field.set(object, value);
					return value;
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	private static class MapFieldAugment<T, F> extends FieldAugment<T, F> {
		final Map<T, Object> values = new WeakHashMap<T, Object>();
		final F defaultValue;
		
		MapFieldAugment(F defaultValue) {
			this.defaultValue = defaultValue;
		}
		
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
			F value = (F) values.get(object);
			return value == null ? defaultValue : value;
		}
		
		void write(T object, F value) {
			values.put(object, value);
		}
	}
	
	static class MapWeakFieldAugment<T, F> extends MapFieldAugment<T, F> {
		MapWeakFieldAugment(F defaultValue) {
			super(defaultValue);
		}
		
		@SuppressWarnings("unchecked")
		F read(T object) {
			WeakReference<F> read = (WeakReference<F>)values.get(object);
			if (read == null) return defaultValue;
			F result = read.get();
			if (result == null) values.remove(object);
			return result == null ? defaultValue : result;
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
