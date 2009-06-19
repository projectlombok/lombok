package lombok.eclipse;

import static lombok.eclipse.Eclipse.toQualifiedName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import lombok.core.AnnotationValues;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class HandlerLibrary {
	private TypeLibrary typeLibrary = new TypeLibrary();
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private EclipseAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		AnnotationHandlerContainer(EclipseAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		public boolean handle(org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				final Node annotationNode) {
			AnnotationValues<T> annValues = Eclipse.createAnnotation(annotationClass, annotationNode);
			return handler.handle(annValues, annotation, annotationNode);
		}
	}
	
	private Map<String, AnnotationHandlerContainer<?>> annotationHandlers =
		new HashMap<String, AnnotationHandlerContainer<?>>();
	
	private Collection<EclipseASTVisitor> visitorHandlers = new ArrayList<EclipseASTVisitor>();
	
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
	
	public boolean handle(CompilationUnitDeclaration ast, EclipseAST.Node annotationNode,
			org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		String pkgName = annotationNode.getPackageDeclaration();
		Collection<String> imports = annotationNode.getImportStatements();
		
		TypeResolver resolver = new TypeResolver(typeLibrary, pkgName, imports);
		TypeReference rawType = annotation.type;
		if ( rawType == null ) return false;
		boolean handled = false;
		for ( String fqn : resolver.findTypeMatches(annotationNode, toQualifiedName(annotation.type.getTypeName())) ) {
			AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
			if ( container == null ) continue;
			
			try {
				handled |= container.handle(annotation, annotationNode);
			} catch ( AnnotationValueDecodeFail fail ) {
				fail.owner.setError(fail.getMessage(), fail.idx);
			} catch ( Throwable t ) {
				Eclipse.error(String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
			}
		}
		
		return handled;
	}
	
	public void callASTVisitors(EclipseAST ast) {
		for ( EclipseASTVisitor visitor : visitorHandlers ) try {
			ast.traverse(visitor);
		} catch ( Throwable t ) {
			Eclipse.error(String.format("Lombok visitor handler %s failed", visitor.getClass()), t);
		}
	}
}
