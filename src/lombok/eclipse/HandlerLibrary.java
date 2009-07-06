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
package lombok.eclipse;

import static lombok.eclipse.Eclipse.toQualifiedName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

/**
 * This class tracks 'handlers' and knows how to invoke them for any given AST node.
 * 
 * This class can find the handlers (via SPI discovery) and will set up the given AST node, such as
 * building an AnnotationValues instance.
 */
public class HandlerLibrary {
	/**
	 * Creates a new HandlerLibrary. Errors will be reported to the eclipse Error log.
	 * You probably want to use {@link #load()} instead.
	 */
	public HandlerLibrary() {}
	
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

	private boolean skipPrintAST;
	
	/**
	 * Creates a new HandlerLibrary.  Errors will be reported to the eclipse Error log.
	 * then uses SPI discovery to load all annotation and visitor based handlers so that future calls
	 * to the handle methods will defer to these handlers.
	 */
	public static HandlerLibrary load() {
		HandlerLibrary lib = new HandlerLibrary();
		
		loadAnnotationHandlers(lib);
		loadVisitorHandlers(lib);
		
		return lib;
	}
	
	/** Uses SPI Discovery to find implementations of {@link EclipseAnnotationHandler}. */
	@SuppressWarnings("unchecked") private static void loadAnnotationHandlers(HandlerLibrary lib) {
		Iterator<EclipseAnnotationHandler> it;
		try {
			it = SpiLoadUtil.findServices(EclipseAnnotationHandler.class);
		} catch ( Throwable t ) {
			throw Lombok.sneakyThrow(t);
		}
		
		while ( it.hasNext() ) {
			try {
				EclipseAnnotationHandler<?> handler = it.next();
				Class<? extends Annotation> annotationClass =
					SpiLoadUtil.findAnnotationClass(handler.getClass(), EclipseAnnotationHandler.class);
				AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
				if ( lib.annotationHandlers.put(container.annotationClass.getName(), container) != null ) {
					Eclipse.error(null, "Duplicate handlers for annotation type: " + container.annotationClass.getName());
				}
				lib.typeLibrary.addType(container.annotationClass.getName());
			} catch ( Throwable t ) {
				Eclipse.error(null, "Can't load Lombok annotation handler for eclipse: ", t);
			}
		}
	}
	
	/** Uses SPI Discovery to find implementations of {@link EclipseASTVisitor}. */
	private static void loadVisitorHandlers(HandlerLibrary lib) {
		Iterator<EclipseASTVisitor> it;
		try {
			it = SpiLoadUtil.findServices(EclipseASTVisitor.class);
		} catch ( Throwable t ) {
			throw Lombok.sneakyThrow(t);
		}
		while ( it.hasNext() ) {
			try {
				lib.visitorHandlers.add(it.next());
			} catch ( Throwable t ) {
				Eclipse.error(null, "Can't load Lombok visitor handler for eclipse: ", t);
			}
		}
	}
	
	/**
	 * Handles the provided annotation node by first finding a qualifying instance of
	 * {@link EclipseAnnotationHandler} and if one exists, calling it with a freshly cooked up
	 * instance of {@link AnnotationValues}.
	 * 
	 * Note that depending on the printASTOnly flag, the {@link lombok.core.PrintAST} annotation
	 * will either be silently skipped, or everything that isn't <code>PrintAST</code> will be skipped.
	 * 
	 * The HandlerLibrary will attempt to guess if the given annotation node represents a lombok annotation.
	 * For example, if <code>lombok.*</code> is in the import list, then this method will guess that
	 * <code>Getter</code> refers to <code>lombok.Getter</code>, presuming that {@link lombok.eclipse.handlers.HandleGetter}
	 * has been loaded.
	 * 
	 * @param ast The Compilation Unit that contains the Annotation AST Node.
	 * @param annotationNode The Lombok AST Node representing the Annotation AST Node.
	 * @param annotation 'node.get()' - convenience parameter.
	 */
	public boolean handle(CompilationUnitDeclaration ast, EclipseAST.Node annotationNode,
			org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		String pkgName = annotationNode.getPackageDeclaration();
		Collection<String> imports = annotationNode.getImportStatements();
		
		TypeResolver resolver = new TypeResolver(typeLibrary, pkgName, imports);
		TypeReference rawType = annotation.type;
		if ( rawType == null ) return false;
		boolean handled = false;
		for ( String fqn : resolver.findTypeMatches(annotationNode, toQualifiedName(annotation.type.getTypeName())) ) {
			boolean isPrintAST = fqn.equals(PrintAST.class.getName());
			if ( isPrintAST == skipPrintAST ) continue;
			AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
			
			if ( container == null ) continue;
			
			try {
				handled |= container.handle(annotation, annotationNode);
			} catch ( AnnotationValueDecodeFail fail ) {
				fail.owner.setError(fail.getMessage(), fail.idx);
			} catch ( Throwable t ) {
				Eclipse.error(ast, String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
			}
		}
		
		return handled;
	}
	
	/**
	 * Will call all registered {@link EclipseASTVisitor} instances.
	 */
	public void callASTVisitors(EclipseAST ast) {
		for ( EclipseASTVisitor visitor : visitorHandlers ) try {
			ast.traverse(visitor);
		} catch ( Throwable t ) {
			Eclipse.error((CompilationUnitDeclaration) ast.top().get(),
					String.format("Lombok visitor handler %s failed", visitor.getClass()), t);
		}
	}
	
	/**
	 * Lombok does not currently support triggering annotations in a specified order; the order is essentially
	 * random right now. This lack of order is particularly annoying for the <code>PrintAST</code> annotation,
	 * which is almost always intended to run last. Hence, this hack, which lets it in fact run last.
	 * 
	 * @see #skipAllButPrintAST()
	 */
	public void skipPrintAST() {
		skipPrintAST = true;
	}
	
	/** @see #skipPrintAST() */
	public void skipAllButPrintAST() {
		skipPrintAST = false;
	}
}
