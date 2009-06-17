package lombok.javac;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import lombok.core.AnnotationValues;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewArray;


public class HandlerLibrary {
	private final TypeLibrary typeLibrary = new TypeLibrary();
	private final Map<String, AnnotationHandlerContainer<?>> annotationHandlers = new HashMap<String, AnnotationHandlerContainer<?>>();
//	private final Collection<JavacASTVisitor> visitorHandlers = new ArrayList<JavacASTVisitor>();
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private JavacAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		AnnotationHandlerContainer(JavacAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		private Object calculateGuess(JCExpression expr) {
			if ( expr instanceof JCLiteral ) {
				return ((JCLiteral)expr).value;
			} else if ( expr instanceof JCIdent || expr instanceof JCFieldAccess ) {
				String x = expr.toString();
				if ( x.endsWith(".class") ) x = x.substring(0, x.length() - 6);
				else {
					int idx = x.lastIndexOf('.');
					if ( idx > -1 ) x = x.substring(idx + 1);
				}
				return x;
			} else return null;
		}
		
		public void handle(JavacAST.Node node) {
			Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
			JCAnnotation anno = (JCAnnotation) node.get();
			List<JCExpression> arguments = anno.getArguments();
			for ( Method m : annotationClass.getDeclaredMethods() ) {
				if ( !Modifier.isPublic(m.getModifiers()) ) continue;
				String name = m.getName();
				List<String> raws = new ArrayList<String>();
				List<Object> guesses = new ArrayList<Object>();
				
				for ( JCExpression arg : arguments ) {
					JCAssign assign = (JCAssign) arg;
					String mName = assign.lhs.toString();
					if ( !mName.equals(name) ) continue;
					JCExpression rhs = assign.rhs;
					if ( rhs instanceof JCNewArray ) {
						List<JCExpression> elems = ((JCNewArray)rhs).elems;
						for  ( JCExpression inner : elems ) {
							raws.add(inner.toString());
							guesses.add(calculateGuess(inner));
						}
					} else {
						raws.add(rhs.toString());
						guesses.add(calculateGuess(rhs));
					}
				}
				
				values.put(name, new AnnotationValue(node, raws, guesses) {
					@Override public void setError(String message, int valueIdx) {
						//TODO
						super.setError(message, valueIdx);
					}
				});
			}
			
			handler.handle(new AnnotationValues<T>(annotationClass, values, node), (JCAnnotation)node.get(), node);
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
				lib.typeLibrary.addType(container.annotationClass.getName());
			} catch ( ServiceConfigurationError e ) {
				messager.printMessage(Diagnostic.Kind.WARNING,
						"Can't load Lombok annotation handler for javac: " + e);
			}
		}
	}
	
	public void handleAnnotation(JCCompilationUnit unit, JavacAST.Node node, JCAnnotation annotation) {
		TypeResolver resolver = new TypeResolver(typeLibrary, node.getPackageDeclaration(), node.getImportStatements());
		String rawType = annotation.annotationType.toString();
		for ( String fqn : resolver.findTypeMatches(node, rawType) ) {
			AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
			if ( container == null ) continue;
			
			try {
				container.handle(node);
			} catch ( AnnotationValueDecodeFail fail ) {
				fail.owner.setError(fail.getMessage(), fail.idx);
			} catch ( Throwable t ) {
				t.printStackTrace();
//				Eclipse.error(String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
				//TODO
			}
		}
	}
	
	public void handleAST(JavacAST ast) {
		//Later!
	}
	
	public boolean hasHandlerFor(TypeElement annotationType) {
		return annotationHandlers.containsKey(annotationType.getQualifiedName().toString());
	}
}
