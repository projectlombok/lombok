package lombok.eclipse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.eclipse.EclipseAST.Node;

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
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private EclipseAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		AnnotationHandlerContainer(EclipseAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		@SuppressWarnings("unchecked")
		public void handle(Object annInstance,
				org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				Node annotationNode) {
			handler.handle((T) annInstance, annotation, annotationNode);
		}
	}
	
	private Map<String, AnnotationHandlerContainer<?>> annotationHandlers =
		new HashMap<String, AnnotationHandlerContainer<?>>();
	
	private Collection<EclipseASTVisitor> visitorHandlers = new ArrayList<EclipseASTVisitor>();
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A createAnnotation(Class<A> target,
			CompilationUnitDeclaration ast,
			org.eclipse.jdt.internal.compiler.ast.Annotation node) throws AnnotationValueDecodeFail {
		final Map<String, Object> values = new HashMap<String, Object>();
		
		final MemberValuePair[] pairs = node.memberValuePairs();
		
		for ( Method m : target.getMethods() ) {
			String name = m.getName();
			Object value = m.getDefaultValue();
			for ( MemberValuePair pair : pairs ) {
				if ( name.equals(new String(pair.name)) ) {
					value = calculateValue(pair, ast, m.getReturnType(), pair.value);
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
	
	private Object calculateValue(MemberValuePair pair,
			CompilationUnitDeclaration ast, Class<?> type, Expression e) throws AnnotationValueDecodeFail {
		if ( e instanceof Literal ) {
			((Literal)e).computeConstant();
			return convertConstant(pair, type, e.constant);
		} else if ( e instanceof ArrayInitializer ) {
			if ( !type.isArray() ) throw new AnnotationValueDecodeFail(pair, "Did not expect an array here.");
			
			Class<?> component = type.getComponentType();
			Expression[] expressions = ((ArrayInitializer)e).expressions;
			int length = expressions == null ? 0 : expressions.length;
			Object[] values = new Object[length];
			for (int i = 0; i < length; i++) {
				values[i] = calculateValue(pair, ast, component, expressions[i]);
			}
			return values;
		} else if ( e instanceof ClassLiteralAccess ) {
			if ( type == Class.class ) return toClass(pair, ast, str(((ClassLiteralAccess)e).type.getTypeName()));
			else throw new AnnotationValueDecodeFail(pair, "Expected a " + type + " literal.");
		} else if ( e instanceof NameReference ) {
			String s = null;
			if ( e instanceof SingleNameReference ) s = new String(((SingleNameReference)e).token);
			else if ( e instanceof QualifiedNameReference ) s = str(((QualifiedNameReference)e).tokens);
			if ( Enum.class.isAssignableFrom(type) ) return toEnum(pair, type, s);
			throw new AnnotationValueDecodeFail(pair, "Lombok annotations must contain literals only.");
		} else {
			throw new AnnotationValueDecodeFail(pair, "Lombok could not decode this annotation parameter.");
		}
	}
	
	private Enum<?> toEnum(MemberValuePair pair, Class<?> enumType, String ref) throws AnnotationValueDecodeFail {
		int idx = ref.indexOf('.');
		if ( idx > -1 ) ref = ref.substring(idx +1);
		Object[] enumConstants = enumType.getEnumConstants();
		for ( Object constant : enumConstants ) {
			String target = ((Enum<?>)constant).name();
			if ( target.equals(ref) ) return (Enum<?>) constant;
		}
		throw new AnnotationValueDecodeFail(pair, "I can't figure out which enum constant you mean.");
	}
	
	private Class<?> toClass(MemberValuePair pair, CompilationUnitDeclaration ast, String typeName) throws AnnotationValueDecodeFail {
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
		
		if ( !fqn ) {
			c = tryClass(typeName);
			if ( c != null ) return c;
		}
		
		//Try star imports
		for ( ImportReference ref : ast.imports ) {
			String im = str(ref.tokens);
			if ( im.endsWith(".*") ) {
				c = tryClass(im.substring(0, im.length() -1) + typeName);
				if ( c != null ) return c;
			}
		}
		
		throw new AnnotationValueDecodeFail(pair, "I can't find this class. Try using the fully qualified name.");
	}
	
	private Class<?> tryClass(String name) {
		try {
			return Class.forName(name);
		} catch ( ClassNotFoundException e ) {
			return null;
		}
	}
	
	private Object convertConstant(MemberValuePair pair, Class<?> type, Constant constant) throws AnnotationValueDecodeFail {
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
			throw new AnnotationValueDecodeFail(pair, "Expected a constant of some sort here (a number or a string)");
		}
		if ( !Expression.isConstantValueRepresentable(constant, constant.typeID(), targetTypeID) ) {
			throw new AnnotationValueDecodeFail(pair, "I can't turn this literal into a " + type);
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
	
	private static class AnnotationValueDecodeFail extends Exception {
		private static final long serialVersionUID = 1L;
		
		MemberValuePair pair;
		
		AnnotationValueDecodeFail(MemberValuePair pair, String msg) {
			super(msg);
			this.pair = pair;
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
	
	public static HandlerLibrary load() {
		HandlerLibrary lib = new HandlerLibrary();
		
		loadAnnotationHandlers(lib);
		loadVisitorHandlers(lib);
		
		return lib;
	}
	
	@SuppressWarnings("unchecked") private static void loadAnnotationHandlers(HandlerLibrary lib) {
		Iterator<EclipseAnnotationHandler> it = ServiceLoader.load(EclipseAnnotationHandler.class).iterator();
		while ( it.hasNext() ) {
			try {
				EclipseAnnotationHandler<?> handler = it.next();
				Class<? extends Annotation> annotationClass =
					SpiLoadUtil.findAnnotationClass(handler.getClass(), EclipseAnnotationHandler.class);
				AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
				if ( lib.annotationHandlers.put(container.annotationClass.getName(), container) != null ) {
					Eclipse.error("Duplicate handlers for annotation type: " + container.annotationClass.getName());
				}
				lib.typeLibrary.addType(container.annotationClass.getName());
			} catch ( ServiceConfigurationError e ) {
				Eclipse.error("Can't load Lombok annotation handler for eclipse: ", e);
			}
		}
	}
	
	private static void loadVisitorHandlers(HandlerLibrary lib) {
		Iterator<EclipseASTVisitor> it = ServiceLoader.load(EclipseASTVisitor.class).iterator();
		while ( it.hasNext() ) {
			try {
				lib.visitorHandlers.add(it.next());
			} catch ( ServiceConfigurationError e ) {
				Eclipse.error("Can't load Lombok visitor handler for eclipse: ", e);
			}
		}
	}
	
	public void handle(CompilationUnitDeclaration ast, EclipseAST.Node annotationNode,
			org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		TypeResolver resolver = new TypeResolver(typeLibrary, annotationNode.top());
		TypeReference rawType = annotation.type;
		if ( rawType == null ) return;
		for ( String fqn : resolver.findTypeMatches(annotationNode, annotation.type) ) {
			AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
			if ( container == null ) continue;
			Object annInstance;
			try {
				annInstance = createAnnotation(container.annotationClass, ast, annotation);
			} catch ( AnnotationValueDecodeFail e ) {
				annotationNode.addError(e.getMessage(), e.pair.sourceStart, e.pair.sourceEnd);
				return;
			}
			
			try {
				container.handle(annInstance, annotation, annotationNode);
			} catch ( Throwable t ) {
				Eclipse.error(String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
			}
		}
	}
	
	public void callASTVisitors(EclipseAST ast) {
		for ( EclipseASTVisitor visitor : visitorHandlers ) try {
			ast.traverse(visitor);
		} catch ( Throwable t ) {
			Eclipse.error(String.format("Lombok visitor handler %s failed", visitor.getClass()), t);
		}
	}
}
