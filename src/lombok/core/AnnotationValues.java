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
package lombok.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a single annotation in a source file and can be used to query the parameters present on it.
 */
public class AnnotationValues<A extends Annotation> {
	private final Class<A> type;
	private final Map<String, AnnotationValue> values;
	private final LombokNode<?, ?, ?> ast;
	
	/**
	 * Represents a single method on the annotation class. For example, the value() method on the Getter annotation.
	 */
	public static class AnnotationValue {
		/** A list of the raw expressions. List is size 1 unless an array is provided. */
		public final List<String> raws;
		
		/** Guesses for each raw expression. If the raw expression is a literal expression, the guess will
		 * likely be right. If not, it'll be wrong. */
		public final List<Object> valueGuesses;
		private final LombokNode<?, ?, ?> node;
		private final boolean isExplicit;
		
		/**
		 * 'raw' should be the exact expression, for example '5+7', 'AccessLevel.PUBLIC', or 'int.class'.
		 * 'valueGuess' should be a likely guess at the real value intended.
		 * 
		 * For classes, supply the class name (qualified or not) as a string.<br />
		 * For enums, supply the simple name part (everything after the last dot) as a string.<br />
		 */
		public AnnotationValue(LombokNode<?, ?, ?> node, String raw, Object valueGuess, boolean isExplicit) {
			this.node = node;
			this.raws = Collections.singletonList(raw);
			this.valueGuesses = Collections.singletonList(valueGuess);
			this.isExplicit = isExplicit;
		}
		
		/**
		 * Like the other constructor, but used for when the annotation method is initialized with an array value.
		 */
		public AnnotationValue(LombokNode<?, ?, ?> node, List<String> raws, List<Object> valueGuesses, boolean isExplicit) {
			this.node = node;
			this.raws = raws;
			this.valueGuesses = valueGuesses;
			this.isExplicit = isExplicit;
		}
		
		/**
		 *  Override this if you want more specific behaviour (to get the source position just right).
		 * 
		 * @param message English message with the problem.
		 * @param valueIdx The index into the values for this annotation key that caused the problem.
		 *   -1 for a problem that applies to all values, otherwise the 0-based index into an array of values.
		 *   If there is no array for this value (e.g. value=1 instead of value={1,2}), then always -1 or 0.
		 */
		public void setError(String message, int valueIdx) {
			node.addError(message);
		}
		
		/**
		 *  Override this if you want more specific behaviour (to get the source position just right).
		 * 
		 * @param message English message with the problem.
		 * @param valueIdx The index into the values for this annotation key that caused the problem.
		 *   -1 for a problem that applies to all values, otherwise the 0-based index into an array of values.
		 *   If there is no array for this value (e.g. value=1 instead of value={1,2}), then always -1 or 0.
		 */
		public void setWarning(String message, int valueIdx) {
			node.addError(message);
		}
		
		/** {@inheritDoc} */
		@Override public String toString() {
			return "raws: " + raws + " valueGuesses: " + valueGuesses;
		}
		
		public boolean isExplicit() {
			return isExplicit;
		}
	}
	
	/**
	 * Creates a new AnnotationValues.
	 * 
	 * @param type The annotation type. For example, "Getter.class"
	 * @param values a Map of method names to AnnotationValue instances, for example 'value -> annotationValue instance'.
	 * @param ast The Annotation node.
	 */
	public AnnotationValues(Class<A> type, Map<String, AnnotationValue> values, LombokNode<?, ?, ?> ast) {
		this.type = type;
		this.values = values;
		this.ast = ast;
	}
	
	/**
	 * Thrown on the fly if an actual annotation instance procured via the {@link #getInstance()} method is queried
	 * for a method for which this AnnotationValues instance either doesn't have a guess or can't manage to fit
	 * the guess into the required data type.
	 */
	public static class AnnotationValueDecodeFail extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		/** The index into an array initializer (e.g. if the second value in an array initializer is
		 * an integer constant expression like '5+SomeOtherClass.CONSTANT', this exception will be thrown,
		 * and you'll get a '1' for idx. */
		public final int idx;
		
		/** The AnnotationValue object that goes with the annotation method for which the failure occurred. */
		public final AnnotationValue owner;
		
		public AnnotationValueDecodeFail(AnnotationValue owner, String msg, int idx) {
			super(msg);
			this.idx = idx;
			this.owner = owner;
		}
	}
	
	private static AnnotationValueDecodeFail makeNoDefaultFail(AnnotationValue owner, Method method) {
		return new AnnotationValueDecodeFail(owner, 
				"No value supplied but " + method.getName() + " has no default either.", -1);
	}
	
	private A cachedInstance = null;
	
	/**
	 * Creates an actual annotation instance. You can use this to query any annotation methods, except for
	 * those annotation methods with class literals, as those can most likely not be turned into Class objects.
	 * 
	 * If some of the methods cannot be implemented, this method still works; it's only when you call a method
	 * that has a problematic value that an AnnotationValueDecodeFail exception occurs.
	 */
	@SuppressWarnings("unchecked")
	public A getInstance() {
		if (cachedInstance != null) return cachedInstance;
		InvocationHandler invocations = new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				AnnotationValue v = values.get(method.getName());
				if (v == null) {
					Object defaultValue = method.getDefaultValue();
					if (defaultValue != null) return defaultValue;
					throw makeNoDefaultFail(v, method);
				}
				
				boolean isArray = false;
				Class<?> expected = method.getReturnType();
				Object array = null;
				if (expected.isArray()) {
					isArray = true;
					expected = expected.getComponentType();
					array = Array.newInstance(expected, v.valueGuesses.size());
				}
				
				if (!isArray && v.valueGuesses.size() > 1) {
					throw new AnnotationValueDecodeFail(v, 
							"Expected a single value, but " + method.getName() + " has an array of values", -1);
				}
				
				if (v.valueGuesses.size() == 0 && !isArray) {
					Object defaultValue = method.getDefaultValue();
					if (defaultValue == null) throw makeNoDefaultFail(v, method);
					return defaultValue;
				}
				
				int idx = 0;
				for (Object guess : v.valueGuesses) {
					Object result = guess == null ? null : guessToType(guess, expected, v, idx);
					if (!isArray) {
						if (result == null) {
							Object defaultValue = method.getDefaultValue();
							if (defaultValue == null) throw makeNoDefaultFail(v, method);
							return defaultValue;
						} else return result;
					} else {
						if (result == null) {
							if (v.valueGuesses.size() == 1) {
								Object defaultValue = method.getDefaultValue();
								if (defaultValue == null) throw makeNoDefaultFail(v, method);
								return defaultValue;
							} else throw new AnnotationValueDecodeFail(v, 
									"I can't make sense of this annotation value. Try using a fully qualified literal.", idx);
						}
						Array.set(array, idx++, result);
					}
				}
				
				return array;
			}
		};
		
		return cachedInstance = (A) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, invocations);
	}
	
	private Object guessToType(Object guess, Class<?> expected, AnnotationValue v, int pos) {
		if (expected == int.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				return ((Number)guess).intValue();
			}
		}
		
		if (expected == long.class) {
			if (guess instanceof Long || guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				return ((Number)guess).longValue();
			}
		}
		
		if (expected == short.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				int intVal = ((Number)guess).intValue();
				int shortVal = ((Number)guess).shortValue();
				if (shortVal == intVal) return shortVal;
			}
		}
		
		if (expected == byte.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				int intVal = ((Number)guess).intValue();
				int byteVal = ((Number)guess).byteValue();
				if (byteVal == intVal) return byteVal;
			}
		}
		
		if (expected == double.class) {
			if (guess instanceof Number) return ((Number)guess).doubleValue();
		}
		
		if (expected == float.class) {
			if (guess instanceof Number) return ((Number)guess).floatValue();
		}
		
		if (expected == boolean.class) {
			if (guess instanceof Boolean) return ((Boolean)guess).booleanValue();
		}
		
		if (expected == char.class) {
			if (guess instanceof Character) return ((Character)guess).charValue();
		}
		
		if (expected == String.class) {
			if (guess instanceof String) return guess;
		}
		
		if (Enum.class.isAssignableFrom(expected) ) {
			if (guess instanceof String) {
				for (Object enumConstant : expected.getEnumConstants()) {
					String target = ((Enum<?>)enumConstant).name();
					if (target.equals(guess)) return enumConstant;
				}
				throw new AnnotationValueDecodeFail(v,
						"Can't translate " + guess + " to an enum of type " + expected, pos);
			}
		}
		
		if (Class.class == expected) {
			if (guess instanceof String) try {
				return Class.forName(toFQ((String)guess));
			} catch (ClassNotFoundException e) {
				throw new AnnotationValueDecodeFail(v,
						"Can't translate " + guess + " to a class object.", pos);
			}
		}
		
		throw new AnnotationValueDecodeFail(v,
				"Can't translate a " + guess.getClass() + " to the expected " + expected, pos);
	}
	
	/**
	 * Returns the raw expressions used for the provided annotationMethodName.
	 * 
	 * You should use this method for annotation methods that return Class objects. Remember that
	 * class literals end in ".class" which you probably want to strip off.
	 */
	public List<String> getRawExpressions(String annotationMethodName) {
		AnnotationValue v = values.get(annotationMethodName);
		return v == null ? Collections.<String>emptyList() : v.raws;
	}
	
	public boolean isExplicit(String annotationMethodName) {
		AnnotationValue annotationValue = values.get(annotationMethodName);
		return annotationValue != null && annotationValue.isExplicit();
	}
	
	/**
	 * Convenience method to return the first result in a {@link #getRawExpressions(String)} call.
	 * 
	 * You should use this method if the annotation method is not an array type.
	 */
	public String getRawExpression(String annotationMethodName) {
		List<String> l = getRawExpressions(annotationMethodName);
		return l.isEmpty() ? null : l.get(0);
	}
	
	/** Generates an error message on the stated annotation value (you should only call this method if you know it's there!) */
	public void setError(String annotationMethodName, String message) {
		setError(annotationMethodName, message, -1);
	}
	
	/** Generates a warning message on the stated annotation value (you should only call this method if you know it's there!) */
	public void setWarning(String annotationMethodName, String message) {
		setWarning(annotationMethodName, message, -1);
	}
	
	/** Generates an error message on the stated annotation value, which must have an array initializer.
	 * The index-th item in the initializer will carry the error (you should only call this method if you know it's there!) */
	public void setError(String annotationMethodName, String message, int index) {
		AnnotationValue v = values.get(annotationMethodName);
		if (v == null) return;
		v.setError(message, index);
	}
	
	/** Generates a warning message on the stated annotation value, which must have an array initializer.
	 * The index-th item in the initializer will carry the error (you should only call this method if you know it's there!) */
	public void setWarning(String annotationMethodName, String message, int index) {
		AnnotationValue v = values.get(annotationMethodName);
		if (v == null) return;
		v.setWarning(message, index);
	}
	
	/**
	 * Attempts to translate class literals to their fully qualified names, such as 'Throwable.class' to 'java.lang.Throwable'.
	 * 
	 * This process is at best a guess, but it will take into account import statements.
	 */
	public List<String> getProbableFQTypes(String annotationMethodName) {
		List<String> result = new ArrayList<String>();
		AnnotationValue v = values.get(annotationMethodName);
		if (v == null) return Collections.emptyList();
		
		for (Object o : v.valueGuesses) result.add(o == null ? null : toFQ(o.toString()));
		return result;
	}
	
	/**
	 * Convenience method to return the first result in a {@link #getProbableFQType(String)} call.
	 * 
	 * You should use this method if the annotation method is not an array type.
	 */
	public String getProbableFQType(String annotationMethodName) {
		List<String> l = getProbableFQTypes(annotationMethodName);
		return l.isEmpty() ? null : l.get(0);
	}
	
	private String toFQ(String typeName) {
		Class<?> c;
		boolean fqn = typeName.indexOf('.') > -1;
		String prefix = fqn ? typeName.substring(0, typeName.indexOf('.')) : typeName;
		
		for (String im : ast.getImportStatements()) {
			int idx = im.lastIndexOf('.');
			String simple = im;
			if (idx > -1) simple = im.substring(idx+1);
			if (simple.equals(prefix)) {
				return im + typeName.substring(prefix.length());
			}
		}
		
		c = tryClass(typeName);
		if (c != null) return c.getName();
		
		c = tryClass("java.lang." + typeName);
		if (c != null) return c.getName();
		
		//Try star imports
		for (String im : ast.getImportStatements()) {
			if (im.endsWith(".*")) {
				c = tryClass(im.substring(0, im.length() -1) + typeName);
				if (c != null) return c.getName();
			}
		}
		
		if (!fqn) {
			String pkg = ast.getPackageDeclaration();
			if (pkg != null) return pkg + "." + typeName;
		}
		
		return null;
	}
	
	private Class<?> tryClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
