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

public class AnnotationValues<A extends Annotation> {
	private final Class<A> type;
	private final Map<String, AnnotationValue> values;
	private final AST<?>.Node ast;

	public static class AnnotationValue {
		public final List<String> raws;
		public final List<Object> valueGuesses;
		private final AST<?>.Node node;
		
		/**
		 * 'raw' should be the exact expression, for example '5+7', 'AccessLevel.PUBLIC', or 'int.class'.
		 * 'valueGuess' should be a likely guess at the real value intended.
		 * 
		 * For classes, supply the class name (qualified or not) as a string.<br />
		 * For enums, supply the simple name part (everything after the last dot) as a string.<br />
		 */
		public AnnotationValue(AST<?>.Node node, String raw, Object valueGuess) {
			this.node = node;
			this.raws = Collections.singletonList(raw);
			this.valueGuesses = Collections.singletonList(valueGuess);
		}
		
		/** When the value is an array type. */
		public AnnotationValue(AST<?>.Node node, List<String> raws, List<Object> valueGuesses) {
			this.node = node;
			this.raws = raws;
			this.valueGuesses = valueGuesses;
		}
		
		/**
		 *  Override this if you want more specific behaviour (e.g. get the source position just right).
		 * 
		 * @param message English message with the problem.
		 * @param valueIdx The index into the values for this annotation key that caused the problem.
		 *   -1 for a problem that applies to all values, otherwise the 0-based index into an array of values.
		 *   If there is no array for this value (e.g. value=1 instead of value={1,2}), then always -1 or 0.
		 */
		public void setError(String message, int valueIdx) {
			node.addError(message);
		}
	}
	
	public AnnotationValues(Class<A> type, Map<String, AnnotationValue> values, AST<?>.Node ast) {
		this.type = type;
		this.values = values;
		this.ast = ast;
	}
	
	public static class AnnotationValueDecodeFail extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public final int idx;
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
	
	@SuppressWarnings("unchecked")
	public A getInstance() throws AnnotationValueDecodeFail {
		InvocationHandler invocations = new InvocationHandler() {
			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				AnnotationValue v = values.get(method.getName());
				if ( v == null ) {
					Object defaultValue = method.getDefaultValue();
					if ( defaultValue != null ) return defaultValue;
					throw makeNoDefaultFail(v, method);
				}
				
				boolean isArray = false;
				Class<?> expected = method.getReturnType();
				Object array = null;
				if ( expected.isArray() ) {
					isArray = true;
					expected = expected.getComponentType();
					array = Array.newInstance(expected, 1);
				}
				
				if ( !isArray && v.valueGuesses.size() > 1 ) {
					throw new AnnotationValueDecodeFail(v, 
							"Expected a single value, but " + method.getName() + " has an array of values", -1);
				}
				
				if ( v.valueGuesses.size() == 0 && !isArray ) {
					Object defaultValue = method.getDefaultValue();
					if ( defaultValue == null ) throw makeNoDefaultFail(v, method);
					return defaultValue;
				}
				
				int idx = 0;
				for ( Object guess : v.valueGuesses ) {
					Object result = guess == null ? null : guessToType(guess, expected, v, idx);
					if ( !isArray ) {
						if ( result == null ) {
							Object defaultValue = method.getDefaultValue();
							if ( defaultValue == null ) throw makeNoDefaultFail(v, method);
							return defaultValue;
						} else return result;
					} else {
						if ( result == null ) {
							if ( v.valueGuesses.size() == 1 ) {
								Object defaultValue = method.getDefaultValue();
								if ( defaultValue == null ) throw makeNoDefaultFail(v, method);
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
		
		return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, invocations);
	}
	
	private Object guessToType(Object guess, Class<?> expected, AnnotationValue v, int pos) {
		if ( expected == int.class ) {
			if ( guess instanceof Integer || guess instanceof Short || guess instanceof Byte ) {
				return ((Number)guess).intValue();
			}
		}
		
		if ( expected == long.class ) {
			if ( guess instanceof Long || guess instanceof Integer || guess instanceof Short || guess instanceof Byte ) {
				return ((Number)guess).longValue();
			}
		}
		
		if ( expected == short.class ) {
			if ( guess instanceof Integer || guess instanceof Short || guess instanceof Byte ) {
				int intVal = ((Number)guess).intValue();
				int shortVal = ((Number)guess).shortValue();
				if ( shortVal == intVal ) return shortVal;
			}
		}
		
		if ( expected == byte.class ) {
			if ( guess instanceof Integer || guess instanceof Short || guess instanceof Byte ) {
				int intVal = ((Number)guess).intValue();
				int byteVal = ((Number)guess).byteValue();
				if ( byteVal == intVal ) return byteVal;
			}
		}
		
		if ( expected == double.class ) {
			if ( guess instanceof Number ) return ((Number)guess).doubleValue();
		}
		
		if ( expected == float.class ) {
			if ( guess instanceof Number ) return ((Number)guess).floatValue();
		}
		
		if ( expected == boolean.class ) {
			if ( guess instanceof Boolean ) return ((Boolean)guess).booleanValue();
		}
		
		if ( expected == char.class ) {
			if ( guess instanceof Character ) return ((Character)guess).charValue();
		}
		
		if ( expected == String.class ) {
			if ( guess instanceof String ) return expected;
		}
		
		if ( Enum.class.isAssignableFrom(expected) ) {
			if ( guess instanceof String ) {
				for ( Object enumConstant : expected.getEnumConstants() ) {
					String target = ((Enum<?>)enumConstant).name();
					if ( target.equals(guess) ) return enumConstant;
				}
				throw new AnnotationValueDecodeFail(v,
						"Can't translate " + guess + " to an enum of type " + expected, pos);
			}
		}
		
		if ( Class.class == expected ) {
			if ( guess instanceof String ) try {
				return Class.forName(toFQ((String)guess));
			} catch ( ClassNotFoundException e ) {
				throw new AnnotationValueDecodeFail(v,
						"Can't translate " + guess + " to a class object.", pos);
			}
		}
		
		throw new AnnotationValueDecodeFail(v,
				"Can't translate a " + guess.getClass() + " to the expected " + expected, pos);
	}
	
	public List<String> getRawExpressions(String annotationMethodName) {
		AnnotationValue v = values.get(annotationMethodName);
		return v == null ? Collections.<String>emptyList() : v.raws;
	}
	
	public String getRawExpression(String annotationMethodName) {
		List<String> l = getRawExpressions(annotationMethodName);
		return l.isEmpty() ? null : l.get(0);
	}
	
	public List<String> getProbableFQTypes(String annotationMethodName) {
		List<String> result = new ArrayList<String>();
		AnnotationValue v = values.get(annotationMethodName);
		if ( v == null ) return Collections.emptyList();
		
		for ( Object o : v.valueGuesses ) result.add(o == null ? null : toFQ(o.toString()));
		return result;
	}
	
	private String toFQ(String typeName) {
		Class<?> c;
		boolean fqn = typeName.indexOf('.') > -1;
		String prefix = fqn ? typeName.substring(0, typeName.indexOf('.')) : typeName;
		
		for ( String im : ast.getImportStatements() ) {
			int idx = im.lastIndexOf('.');
			String simple = im;
			if ( idx > -1 ) simple = im.substring(idx+1);
			if ( simple.equals(prefix) ) {
				return im + typeName.substring(prefix.length());
			}
		}
		
		c = tryClass(typeName);
		if ( c != null ) return c.getName();
		
		c = tryClass("java.lang." + typeName);
		if ( c != null ) return c.getName();
		
		//Try star imports
		for ( String im : ast.getImportStatements() ) {
			if ( im.endsWith(".*") ) {
				c = tryClass(im.substring(0, im.length() -1) + typeName);
				if ( c != null ) return c.getName();
			}
		}
		
		if ( !fqn ) {
			String pkg = ast.getPackageDeclaration();
			if ( pkg != null ) return pkg + "." + typeName;
		}
		
		return null;
	}
	
	private Class<?> tryClass(String name) {
		try {
			return Class.forName(name);
		} catch ( ClassNotFoundException e ) {
			return null;
		}
	}
	
	public String getProbableFQType(String annotationMethodName) {
		List<String> l = getProbableFQTypes(annotationMethodName);
		return l.isEmpty() ? null : l.get(0);
	}
}
