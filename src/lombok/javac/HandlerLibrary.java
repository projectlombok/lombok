package lombok.javac;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import lombok.core.SpiLoadUtil;


public class HandlerLibrary {
	private final Map<String, AnnotationHandlerContainer<?>> annotationHandlers = new HashMap<String, AnnotationHandlerContainer<?>>();
//	private final Collection<JavacASTVisitor> visitorHandlers = new ArrayList<JavacASTVisitor>();
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private JavacAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		AnnotationHandlerContainer(JavacAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		@SuppressWarnings("unchecked")
		public void handle(JavacNode node, Object annInstance) {
			handler.handle(node, (T) annInstance);
		}
	}
	
	public static HandlerLibrary load(Messager messager) {
		HandlerLibrary library = new HandlerLibrary();
		loadAnnotationHandlers(messager, library);
		return library;
	}
	
	@SuppressWarnings("unchecked")
	private static void loadAnnotationHandlers(Messager messager, HandlerLibrary lib) {
		//No, that seemingly superfluous reference to JavacAnnotationHandler's classloader is not in fact superfluous!
		Iterator<JavacAnnotationHandler> it = ServiceLoader.load(JavacAnnotationHandler.class,
				JavacAnnotationHandler.class.getClassLoader()).iterator();
		while ( it.hasNext() ) {
			try {
				JavacAnnotationHandler<?> handler = it.next();
				Class<? extends Annotation> annotationClass =
					SpiLoadUtil.findAnnotationClass(handler.getClass(), JavacAnnotationHandler.class);
				AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
				if ( lib.annotationHandlers.put(container.annotationClass.getName(), container) != null ) {
					messager.printMessage(Diagnostic.Kind.WARNING,
							"Duplicate handlers for annotation type: " + container.annotationClass.getName());
				}
			} catch ( ServiceConfigurationError e ) {
				messager.printMessage(Diagnostic.Kind.WARNING,
						"Can't load Lombok annotation handler for javac: " + e);
			}
		}
	}
	
	public void handleAnnotation(JavacNode node, TypeElement annotationType) {
		AnnotationHandlerContainer<?> container = annotationHandlers.get(annotationType.getQualifiedName().toString());
		if ( container == null ) return;
		try {
			container.handle(node, createAnnotation(container.annotationClass, annotationType.getQualifiedName(), node));
		} catch ( AnnotationValueDecodeFail e ) {
			node.addError(e.getMessage(), e.mirror, e.value);
		}
	}
	
	private Object createAnnotation(Class<? extends Annotation> target, Name annotationName, JavacNode node)
			throws AnnotationValueDecodeFail {
		AnnotationMirror mirror = fetchMirror(annotationName, node);
		if ( mirror == null ) throw new AssertionError("This can't be.");
		
		InvocationHandler invocations = new AnnotationMirrorInvocationHandler(target, mirror);
		return Proxy.newProxyInstance(target.getClassLoader(), new Class[] { target }, invocations);
	}
	
	private static class AnnotationValueDecodeFail extends Exception {
		private static final long serialVersionUID = 1L;
		
		AnnotationMirror mirror;
		AnnotationValue value;
		
		AnnotationValueDecodeFail(String msg, AnnotationMirror mirror, AnnotationValue value) {
			super(msg);
			this.mirror = mirror;
			this.value = value;
		}
	}
	
	private static class AnnotationMirrorInvocationHandler implements InvocationHandler {
		private final AnnotationMirror mirror;
		private final Map<String, Object> values = new HashMap<String, Object>();
		
		AnnotationMirrorInvocationHandler(Class<?> target, AnnotationMirror mirror) throws AnnotationValueDecodeFail {
			this.mirror = mirror;
			
			for ( Method m : target.getDeclaredMethods() ) {
				if ( !Modifier.isPublic(m.getModifiers()) ) continue;
				values.put(m.getName(), decode(m));
			}
		}
		
		private Object decode(Method m) throws AnnotationValueDecodeFail {
			for ( Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
				mirror.getElementValues().entrySet() ) {
				
				if ( entry.getKey().getSimpleName().contentEquals(m.getName()) ) {
					AnnotationValue value = entry.getValue();
					return convert(m.getReturnType(), mirror, value, value.getValue());
				}
			}
			
			return m.getDefaultValue();
		}
		
		@Override public Object invoke(Object proxy, Method method, Object[] args) {
			return values.get(method.getName());
		}
		
		private Object convert(Class<?> expected, AnnotationMirror mirror, AnnotationValue value, Object v) throws AnnotationValueDecodeFail {
			if ( expected == int.class ) {
				if ( v instanceof Number ) return ((Number)v).intValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == long.class ) {
				if ( v instanceof Number ) return ((Number)v).longValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == short.class ) {
				if ( v instanceof Number ) return ((Number)v).shortValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == byte.class ) {
				if ( v instanceof Number ) return ((Number)v).byteValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == double.class ) {
				if ( v instanceof Number ) return ((Number)v).doubleValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == float.class ) {
				if ( v instanceof Number ) return ((Number)v).floatValue();
				else throw new AnnotationValueDecodeFail("Expected a numeric value here", mirror, value);
			} else if ( expected == char.class ) {
				if ( v instanceof Character ) return v;
				else throw new AnnotationValueDecodeFail("Expected a character here", mirror, value);
			} else if ( expected == boolean.class ) {
				if ( v instanceof Boolean ) return v;
				else throw new AnnotationValueDecodeFail("Expected a boolean here", mirror, value);
			} else if ( expected == String.class ) {
				if ( v instanceof String ) return v;
				else throw new AnnotationValueDecodeFail("Expected a String here", mirror, value);
			} else if ( expected == Class.class ) {
				if ( v instanceof TypeMirror ) {
					try {
						return Class.forName(v.toString());
					} catch ( ClassNotFoundException e ) {
						throw new AnnotationValueDecodeFail(
								"I can't find this class. Lombok only works well with types in the core java libraries.",
								mirror, value);
					}
				} else throw new AnnotationValueDecodeFail("Expected a class literal here", mirror, value);
			} else if ( Enum.class.isAssignableFrom(expected) ) {
				if ( v instanceof VariableElement ) {
					String n = ((VariableElement)v).getSimpleName().toString();
					@SuppressWarnings("unchecked")
					Object enumVal = Enum.valueOf((Class<? extends Enum>)expected, n);
					return enumVal;
				} else throw new AnnotationValueDecodeFail("Expected an enum value here", mirror, value);
			} else if ( expected.isArray() ) {
				if ( v instanceof Collection<?> ) {
					List<Object> convertedValues = new ArrayList<Object>();
					Class<?> componentType = expected.getComponentType();
					for ( Object innerV : (Collection<?>)v ) {
						convertedValues.add(convert(componentType, mirror, value, innerV));
					}
					
					Object array = Array.newInstance(componentType, convertedValues.size());
					int pos = 0;
					for ( Object converted : convertedValues ) Array.set(array, pos++, converted);
					return array;
				} else throw new AnnotationValueDecodeFail("Expected an array value here", mirror, value);
//				Collection<AnnotationValue> result = (Collection<AnnotationValue>)entry.getValue().getValue();
//				return result;
			} else {
				throw new AssertionError("We didn't know this is even a legal annotation type: " + expected);
			}
		}
	}
	
	private AnnotationMirror fetchMirror(Name lookingFor, JavacNode node) {
		for ( AnnotationMirror mirror : node.getJavacAST().getAnnotationMirrors() ) {
			if ( !lookingFor.contentEquals(
					((TypeElement)(mirror.getAnnotationType()).asElement()).getQualifiedName()) ) continue;
			return mirror;
		}
		return null;
	}

	public void handleType(TypeElement typeElement) {
		//Later!
	}
	
	public boolean hasHandlerFor(TypeElement annotationType) {
		return annotationHandlers.containsKey(annotationType.getQualifiedName().toString());
	}
}
