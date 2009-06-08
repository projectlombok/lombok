package lombok.apt;

import static lombok.apt.PKG.CURRENT_SUPPORT;
import static lombok.apt.PKG.isInstanceOf;
import static lombok.apt.PKG.readResource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Responsible for redirecting the need to handle an annotation to a class that knows how to handle a given annotation type in a given compiler environment.
 * Will dynamically locate a class in this package using the naming pattern: "HandleFoo_compilerType", e.g. "HandleGetter_ecj".
 * Responsible for injecting the proper class into the right classloader so that it has open access to the classes required to inspect the live AST and
 * modify it so that annotations can cause changes to the live AST.
 * 
 * @author rzwitserloot
 * 
 * @param <T> The annotation class that this transponder should handle (example: Getter.class).
 */
public class AnnotationTransponder<T extends Annotation> {
	private HandlerForCompiler<T> impl;
	private final ProcessingEnvironment processEnv;
	private final RoundEnvironment roundEnv;
	private String error;
	
	@SuppressWarnings("unchecked")
	private void createInstance(Class<T> annotation, ClassLoader loader, String compilerType) {
		try {
			if ( loader == null ) loader = AnnotationTransponder.class.getClassLoader();
			Class<?> implClass;
			try {
				implClass = loader.loadClass(String.format(
						"org.javanext.apt.Handle%s_%s", annotation.getSimpleName(), compilerType));
			} catch ( ClassNotFoundException e ) {
				implClass = loader.loadClass(String.format("lombok.apt.HandleANY_%s", compilerType));
			}
			
			Constructor<?> constructor;
			
			constructor = implClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			impl = (HandlerForCompiler<T>)constructor.newInstance();
			impl.processEnv = processEnv;
			impl.roundEnv = roundEnv;
			try {
				impl.init();
			} catch ( Exception e ) {
				error = "Exception initializing handler: " + e;
				impl = null;
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			error = "You are using " + compilerType + " but a version that's changed the compiler internals. I can't work with it.";
		}
	}
	
	public AnnotationTransponder(Class<T> annotation, RoundEnvironment roundEnv, ProcessingEnvironment processEnv) {
		this.processEnv = processEnv;
		this.roundEnv = roundEnv;
		if ( isInstanceOf(processEnv, "com.sun.tools.javac.processing.JavacProcessingEnvironment") ) {
			createInstance(annotation, null, "javac");
		} else if ( isInstanceOf(processEnv, "org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeBuildProcessingEnvImpl") ) {
			final ClassLoader[] parentLoaders =
				new ClassLoader[] { processEnv.getClass().getClassLoader(), AnnotationTransponder.class.getClassLoader() };
			
			ClassLoader loader = new ClassLoader() {
				@Override public Class<?> findClass(String name) throws ClassNotFoundException {
					if ( name.equals(HandlerForCompiler.class.getName()) ) return HandlerForCompiler.class;
					if ( name.startsWith(AnnotationTransponder.class.getPackage().getName()) ) {
						byte[] data = readResource(name.replace(".", "/") + ".class");
						return defineClass(name, data, 0, data.length);
					}
					for ( int i = 0 ; i < parentLoaders.length ; i++ ) {
						try {
							return parentLoaders[i].loadClass(name);
						} catch ( ClassNotFoundException e ) {
							if ( i == parentLoaders.length -1 ) throw e;
						}
					}
					
					return null;
				}
			};
			
			createInstance(annotation, loader, "ecj");
		} else {
			impl = null;
			this.error = "I cannot work with your compiler. I currently only support " + CURRENT_SUPPORT + ".\n" +
					"This is a: " + processEnv.getClass();
		}
	}
	
	
	public void handle(Element element, T annotation) {
		if ( impl == null ) {
			processEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, error, element);
		} else {
			try {
				impl.handle(element, annotation);
			} catch ( Exception e ) {
				e.printStackTrace();
				processEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Exception in JavaNext: " + e, element);
			}
		}
	}
}
