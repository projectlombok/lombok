package lombok.eclipse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import lombok.eclipse.EclipseAST.Node;
import lombok.transformations.TypeLibrary;
import lombok.transformations.TypeResolver;

import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class HandlerLibrary {
	private TypeLibrary typeLibrary = new TypeLibrary();
	
	private static class HandlerContainer<T extends Annotation> {
		private EclipseAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		HandlerContainer(EclipseAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		@SuppressWarnings("unchecked")
		public void handle(Object annInstance,
				org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				Node node) {
			handler.handle((T) annInstance, annotation, node);
		}
	}
	
	private Map<String, HandlerContainer<?>> handlers = new HashMap<String, HandlerContainer<?>>();
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A createAnnotation(Class<A> target,
			CompilationUnitDeclaration ast,
			org.eclipse.jdt.internal.compiler.ast.Annotation node) throws EnumDecodeFail {
		final Map<String, Object> values = new HashMap<String, Object>();
		
		final MemberValuePair[] pairs = node.memberValuePairs();
		
		for ( Method m : target.getMethods() ) {
			String name = m.getName();
			Object value = m.getDefaultValue();
			for ( MemberValuePair pair : pairs ) {
				if ( name.equals(new String(pair.name)) ) {
					value = calculateValue(ast, m.getReturnType(), pair.value);
					break;
				}
			}
			values.put(name, value);
		}
		
		InvocationHandler invocations = new InvocationHandler() {
			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return values.get(method.getName());
			}
		};
		
		return (A) Proxy.newProxyInstance(target.getClassLoader(), new Class[] { target }, invocations);
	}
	
	private Object calculateValue(CompilationUnitDeclaration ast, Class<?> type, Expression e) throws EnumDecodeFail {
		if ( e instanceof Literal ) {
			((Literal)e).computeConstant();
			return convertConstant(type, e.constant);
		} else if ( e instanceof ArrayInitializer ) {
			if ( !type.isArray() ) throw new EnumDecodeFail("Did not expect an array here.");
			
			Class<?> component = type.getComponentType();
			Expression[] expressions = ((ArrayInitializer)e).expressions;
			int length = expressions == null ? 0 : expressions.length;
			Object[] values = new Object[length];
			for (int i = 0; i < length; i++) {
				values[i] = calculateValue(ast, component, expressions[i]);
			}
			return values;
		} else if ( e instanceof ClassLiteralAccess ) {
			if ( type == Class.class ) return toClass(ast, str(((ClassLiteralAccess)e).type.getTypeName()));
			else throw new EnumDecodeFail("Expected a " + type + " literal.");
		} else if ( e instanceof NameReference ) {
			String s = null;
			if ( e instanceof SingleNameReference ) s = new String(((SingleNameReference)e).token);
			else if ( e instanceof QualifiedNameReference ) s = str(((QualifiedNameReference)e).tokens);
			if ( Enum.class.isAssignableFrom(type) ) toEnum(type, s);
			throw new EnumDecodeFail("Lombok annotations must contain literals only.");
		} else {
			throw new EnumDecodeFail("Lombok could not decode this annotation parameter.");
		}
	}
	
	private Enum<?> toEnum(Class<?> enumType, String ref) throws EnumDecodeFail {
		int idx = ref.indexOf('.');
		if ( idx > -1 ) ref = ref.substring(idx +1);
		Object[] enumConstants = enumType.getEnumConstants();
		for ( Object constant : enumConstants ) {
			String target = ((Enum<?>)constant).name();
			if ( target.equals(ref) ) return (Enum<?>) constant;
		}
		throw new EnumDecodeFail("I can't figure out which enum constant you mean.");
	}
	
	private Class<?> toClass(CompilationUnitDeclaration ast, String typeName) throws EnumDecodeFail {
		Class<?> c;
		boolean fqn = typeName.indexOf('.') > -1;
		
		if ( fqn ) {
			c = tryClass(typeName);
			if ( c != null ) return c;
		}
		
		for ( ImportReference ref : ast.imports ) {
			String im = str(ref.tokens);
			int idx = im.lastIndexOf('.');
			String simple = im;
			if ( idx > -1 ) simple = im.substring(idx+1);
			if ( simple.equals(typeName) ) {
				c = tryClass(im);
				if ( c != null ) return c;
			}
		}
		
		if ( ast.currentPackage != null && ast.currentPackage.tokens != null ) {
			String pkg = str(ast.currentPackage.tokens);
			c = tryClass(pkg + "." + typeName);
			if ( c != null ) return c;
		}
		
		c = tryClass("java.lang." + typeName);
		if ( c != null ) return c;
		
		throw new EnumDecodeFail("I can't find this class. Try using the fully qualified name.");
	}
	
	private Class<?> tryClass(String name) {
		try {
			return Class.forName(name);
		} catch ( ClassNotFoundException e ) {
			return null;
		}
	}
	
	private Object convertConstant(Class<?> type, Constant constant) throws EnumDecodeFail {
		int targetTypeID;
		boolean array = type.isArray();
		if ( array ) type = type.getComponentType();
		
		if ( type == int.class ) targetTypeID = TypeIds.T_int;
		else if ( type == long.class ) targetTypeID = TypeIds.T_long;
		else if ( type == short.class ) targetTypeID = TypeIds.T_short;
		else if ( type == byte.class ) targetTypeID = TypeIds.T_byte;
		else if ( type == double.class ) targetTypeID = TypeIds.T_double;
		else if ( type == float.class ) targetTypeID = TypeIds.T_float;
		else if ( type == String.class ) targetTypeID = TypeIds.T_JavaLangString;
		else if ( type == char.class ) targetTypeID = TypeIds.T_char;
		else if ( type == boolean.class ) targetTypeID = TypeIds.T_boolean;
		else {
			//Enum or Class, so a constant isn't going to be very useful.
			throw new EnumDecodeFail("Expected a constant of some sort here (a number or a string)");
		}
		if ( !Expression.isConstantValueRepresentable(constant, constant.typeID(), targetTypeID) ) {
			throw new EnumDecodeFail("I can't turn this literal into a " + type);
		}
		
		Object o = null;
		
		if ( type == int.class ) o = constant.intValue();
		else if ( type == long.class ) o = constant.longValue();
		else if ( type == short.class ) o = constant.shortValue();
		else if ( type == byte.class ) o = constant.byteValue();
		else if ( type == double.class ) o = constant.doubleValue();
		else if ( type == float.class ) o = constant.floatValue();
		else if ( type == String.class ) o = constant.stringValue();
		else if ( type == char.class ) o = constant.charValue();
		else if ( type == boolean.class ) o = constant.booleanValue();
		
		if ( array ) {
			Object a = Array.newInstance(type, 1);
			Array.set(a, 0, o);
			return a;
		}
		
		return o;
	}
	
	private static class EnumDecodeFail extends Exception {
		private static final long serialVersionUID = 1L;
		
		EnumDecodeFail(String msg) {
			super(msg);
		}
	}
	
	private static String str(char[][] c) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for ( char[] part : c ) {
			sb.append(first ? "" : ".").append(part);
			first = false;
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static HandlerLibrary load() {
		HandlerLibrary lib = new HandlerLibrary();
		for ( EclipseAnnotationHandler<?> handler : ServiceLoader.load(EclipseAnnotationHandler.class) ) {
			Class<? extends Annotation> annotationClass = lib.findAnnotationClass(handler.getClass());
			HandlerContainer<?> container = new HandlerContainer(handler, annotationClass);
			lib.handlers.put(container.annotationClass.getName(), container);
			lib.typeLibrary.addType(container.annotationClass.getName());
		}
		
		return lib;
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Annotation> findAnnotationClass(Class<?> c) {
		if ( c == Object.class || c == null ) return null;
		for ( Type iface : c.getGenericInterfaces() ) {
			if ( iface instanceof ParameterizedType ) {
				ParameterizedType p = (ParameterizedType)iface;
				if ( !EclipseAnnotationHandler.class.equals(p.getRawType()) ) continue;
				Type target = p.getActualTypeArguments()[0];
				if ( target instanceof Class<?> ) {
					if ( Annotation.class.isAssignableFrom((Class<?>) target) ) {
						return (Class<? extends Annotation>) target;
					}
				}
				
				throw new ClassCastException("Not an annotation type: " + target);
			}
		}
		
		Class<? extends Annotation> potential = findAnnotationClass(c.getSuperclass());
		if ( potential != null ) return potential;
		for ( Class<?> iface : c.getInterfaces() ) {
			potential = findAnnotationClass(iface);
			if ( potential != null ) return potential;
		}
		
		return null;
	}
	
	public void handle(CompilationUnitDeclaration ast, EclipseAST.Node node,
			org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		TypeResolver resolver = new TypeResolver(typeLibrary, node.top());
		TypeReference rawType = annotation.type;
		if ( rawType == null ) return;
		for ( String fqn : resolver.findTypeMatches(node, annotation.type) ) {
			HandlerContainer<?> container = handlers.get(fqn);
			if ( container == null ) continue;
			try {
				Object annInstance = createAnnotation(container.annotationClass, ast, annotation);
				container.handle(annInstance, annotation, node);
			} catch (EnumDecodeFail e) {
				e.printStackTrace();
				//TODO: Add to problems array in ast.
			}
		}
	}
}
