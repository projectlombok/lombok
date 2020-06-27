/*
 * Copyright (C) 2009-2013 The Project Lombok Authors.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.core.AST.Kind;
import lombok.permit.Permit;

/**
 * Represents a single annotation in a source file and can be used to query the parameters present on it.
 * 
 * @param A The annotation that this class represents, such as {@code lombok.Getter}
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
		
		/** Guesses for each raw expression. It's 'primitive' (String or primitive), an AV.ClassLiteral, an AV.FieldSelect, or an array of one of those. */
		public final List<Object> valueGuesses;
		
		/** A list of the actual expressions. List is size 1 unless an array is provided. */
		public final List<Object> expressions;
		
		private final LombokNode<?, ?, ?> node;
		private final boolean isExplicit;
		
		/**
		 * Like the other constructor, but used for when the annotation method is initialized with an array value.
		 */
		public AnnotationValue(LombokNode<?, ?, ?> node, List<String> raws, List<Object> expressions, List<Object> valueGuesses, boolean isExplicit) {
			this.node = node;
			this.raws = raws;
			this.expressions = expressions;
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
	
	public static <A extends Annotation> AnnotationValues<A> of(Class<A> type) {
		return new AnnotationValues<A>(type, Collections.<String, AnnotationValue>emptyMap(), null);
	}
	
	/**
	 * Creates a new annotation wrapper with all default values, and using the provided ast as lookup anchor for
	 * class literals.
	 */
	public static <A extends Annotation> AnnotationValues<A> of(Class<A> type, LombokNode<?, ?, ?> ast) {
		return new AnnotationValues<A>(type, Collections.<String, AnnotationValue>emptyMap(), ast);
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
	
	public List<String> getAsStringList(String methodName) {
		AnnotationValue v = values.get(methodName);
		
		if (v == null) {
			String[] s = getDefaultIf(methodName, String[].class, new String[0]);
			return Collections.unmodifiableList(Arrays.asList(s));
		}
		
		List<String> out = new ArrayList<String>(v.valueGuesses.size());
		int idx = 0;
		for (Object guess : v.valueGuesses) {
			Object result = guess == null ? null : guessToType(guess, String.class, v, idx);
			if (result == null) {
				if (v.valueGuesses.size() == 1) {
					String[] s = getDefaultIf(methodName, String[].class, new String[0]);
					return Collections.unmodifiableList(Arrays.asList(s));
				} 
				throw new AnnotationValueDecodeFail(v, 
					"I can't make sense of this annotation value. Try using a fully qualified literal.", idx);
			}
			out.add((String) result);
		}
		
		return Collections.unmodifiableList(out);
	}
	
	public String getAsString(String methodName) {
		AnnotationValue v = values.get(methodName);
		if (v == null || v.valueGuesses.size() != 1) {
			return getDefaultIf(methodName, String.class, "");
		}
		
		Object guess = guessToType(v.valueGuesses.get(0), String.class, v, 0);
		if (guess instanceof String) return (String) guess;
		return getDefaultIf(methodName, String.class, "");
	}
	
	public boolean getAsBoolean(String methodName) {
		AnnotationValue v = values.get(methodName);
		if (v == null || v.valueGuesses.size() != 1) {
			return getDefaultIf(methodName, boolean.class, false);
		}
		
		Object guess = guessToType(v.valueGuesses.get(0), boolean.class, v, 0);
		if (guess instanceof Boolean) return ((Boolean) guess).booleanValue();
		return getDefaultIf(methodName, boolean.class, false);
	}
	
	public <T> T getDefaultIf(String methodName, Class<T> type, T defaultValue) {
		try {
			return type.cast(Permit.getMethod(type, methodName).getDefaultValue());
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
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
						}
						return result;
					} 
					if (result == null) {
						if (v.valueGuesses.size() == 1) {
							Object defaultValue = method.getDefaultValue();
							if (defaultValue == null) throw makeNoDefaultFail(v, method);
							return defaultValue;
						} 
						throw new AnnotationValueDecodeFail(v, 
							"I can't make sense of this annotation value. Try using a fully qualified literal.", idx);
					}
					Array.set(array, idx++, result);
				}
				
				return array;
			}
		};
		
		return cachedInstance = (A) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, invocations);
	}
	
	private Object guessToType(Object guess, Class<?> expected, AnnotationValue v, int pos) {
		if (expected == int.class || expected == Integer.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				return ((Number) guess).intValue();
			}
		}
		
		if (expected == long.class || expected == Long.class) {
			if (guess instanceof Long || guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				return ((Number) guess).longValue();
			}
		}
		
		if (expected == short.class || expected == Short.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				int intVal = ((Number) guess).intValue();
				int shortVal = ((Number) guess).shortValue();
				if (shortVal == intVal) return shortVal;
			}
		}
		
		if (expected == byte.class || expected == Byte.class) {
			if (guess instanceof Integer || guess instanceof Short || guess instanceof Byte) {
				int intVal = ((Number) guess).intValue();
				int byteVal = ((Number) guess).byteValue();
				if (byteVal == intVal) return byteVal;
			}
		}
		
		if (expected == double.class || expected == Double.class) {
			if (guess instanceof Number) return ((Number) guess).doubleValue();
		}
		
		if (expected == float.class || expected == Float.class) {
			if (guess instanceof Number) return ((Number) guess).floatValue();
		}
		
		if (expected == boolean.class || expected == Boolean.class) {
			if (guess instanceof Boolean) return ((Boolean) guess).booleanValue();
		}
		
		if (expected == char.class || expected == Character.class) {
			if (guess instanceof Character) return ((Character) guess).charValue();
		}
		
		if (expected == String.class) {
			if (guess instanceof String) return guess;
		}
		
		if (Enum.class.isAssignableFrom(expected) ) {
			if (guess instanceof FieldSelect) {
				String fieldSel = ((FieldSelect) guess).getFinalPart();
				for (Object enumConstant : expected.getEnumConstants()) {
					String target = ((Enum<?>) enumConstant).name();
					if (target.equals(fieldSel)) return enumConstant;
				}
				throw new AnnotationValueDecodeFail(v,
					"Can't translate " + fieldSel + " to an enum of type " + expected, pos);
			}
		}
		
		if (expected == Class.class) {
			if (guess instanceof ClassLiteral) try {
				String classLit = ((ClassLiteral) guess).getClassName();
				return Class.forName(toFQ(classLit));
			} catch (ClassNotFoundException e) {
				throw new AnnotationValueDecodeFail(v,
					"Can't translate " + guess + " to a class object.", pos);
			}
		}
		
		if (guess instanceof AnnotationValues) {
			return ((AnnotationValues<?>) guess).getInstance();
		}
		
		if (guess instanceof FieldSelect) throw new AnnotationValueDecodeFail(v,
			"You must use constant literals in lombok annotations; they cannot be references to (static) fields.", pos);
		
		throw new AnnotationValueDecodeFail(v,
			"Can't translate a " + guess.getClass() + " to the expected " + expected, pos);
	}
	
	/**
	 * Returns the raw expressions used for the provided {@code annotationMethodName}.
	 * 
	 * You should use this method for annotation methods that return {@code Class} objects. Remember that
	 * class literals end in ".class" which you probably want to strip off.
	 */
	public List<String> getRawExpressions(String annotationMethodName) {
		AnnotationValue v = values.get(annotationMethodName);
		return v == null ? Collections.<String>emptyList() : v.raws;
	}
	
	/**
	 * Returns the actual expressions used for the provided {@code annotationMethodName}.
	 */
	public List<Object> getActualExpressions(String annotationMethodName) {
		AnnotationValue v = values.get(annotationMethodName);
		return v == null ? Collections.<Object>emptyList() : v.expressions;
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
	
	/**
	 * Convenience method to return the first result in a {@link #getActualExpressions(String)} call.
	 * 
	 * You should use this method if the annotation method is not an array type.
	 */
	public Object getActualExpression(String annotationMethodName) {
		List<Object> l = getActualExpressions(annotationMethodName);
		return l.isEmpty() ? null : l.get(0);
	}

	/**
	 * Returns the guessed value for the provided {@code annotationMethodName}.
	 */
	public Object getValueGuess(String annotationMethodName) {
		AnnotationValue v = values.get(annotationMethodName);
		return v == null || v.valueGuesses.isEmpty() ? null : v.valueGuesses.get(0);
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
	
	/*
	 * Credit goes to Petr Jiricka of Sun for highlighting the problems with the earlier version of this method.
	 */
	private String toFQ(String typeName) {
		String prefix = typeName.indexOf('.') > -1 ? typeName.substring(0, typeName.indexOf('.')) : typeName;
		
		/* 1. Walk through type names in this source file at this level. */ {
			LombokNode<?, ?, ?> n = ast;
			walkThroughCU:
			while (n != null) {
				if (n.getKind() == Kind.TYPE) {
					String simpleName = n.getName();
					if (prefix.equals(simpleName)) {
						//We found a matching type name in the local hierarchy!
						List<String> outerNames = new ArrayList<String>();
						while (true) {
							n = n.up();
							if (n == null || n.getKind() == Kind.COMPILATION_UNIT) break;
							if (n.getKind() == Kind.TYPE) outerNames.add(n.getName());
							//If our type has a parent that isn't either the CompilationUnit or another type, then we are
							//a method-local class or an anonymous inner class literal. These technically do have FQNs
							//and we may, with a lot of effort, figure out their name, but, that's some fairly horrible code
							//style and these methods have 'probable' in their name for a reason.
							break walkThroughCU;
						}
						StringBuilder result = new StringBuilder();
						if (ast.getPackageDeclaration() != null) result.append(ast.getPackageDeclaration());
						if (result.length() > 0) result.append('.');
						Collections.reverse(outerNames);
						for (String outerName : outerNames) result.append(outerName).append('.');
						result.append(typeName);
						return result.toString();
					}
				}
				n = n.up();
			}
		}
		
		/* 2. Walk through non-star imports and search for a match. */ {
			if (prefix.equals(typeName)) {
				String fqn = ast.getImportList().getFullyQualifiedNameForSimpleName(typeName);
				if (fqn != null) return fqn;
			}
		}
		
		/* 3. Walk through star imports and, if they start with "java.", use Class.forName based resolution. */ {
			for (String potential : ast.getImportList().applyNameToStarImports("java", typeName)) {
				try {
					Class<?> c = Class.forName(potential);
					if (c != null) return c.getName();
				} catch (Throwable t) {
					//Class.forName failed for whatever reason - it most likely does not exist, continue.
				}
			}
		}
		
		/* 4. If the type name is a simple name, then our last guess is that it's another class in this package. */ {
			if (typeName.indexOf('.') == -1) return inLocalPackage(ast, typeName);
		}
		
		/* 5. It's either an FQN or a nested class in another class in our package. Use code conventions to guess. */ {
			char firstChar = typeName.charAt(0);
			if (Character.isTitleCase(firstChar) || Character.isUpperCase(firstChar)) {
				//Class names start with uppercase letters, so presume it's a nested class in another class in our package.
				return inLocalPackage(ast, typeName);
			}
			
			//Presume it's fully qualified.
			return typeName;
		}
	}
	
	private static String inLocalPackage(LombokNode<?, ?, ?> node, String typeName) {
		StringBuilder result = new StringBuilder();
		if (node != null && node.getPackageDeclaration() != null) result.append(node.getPackageDeclaration());
		if (result.length() > 0) result.append('.');
		result.append(typeName);
		return result.toString();
	}
}
