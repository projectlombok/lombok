/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static lombok.eclipse.EclipseAugments.ASTNode_handled;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;
import lombok.core.configuration.ConfigurationKeysLoader;
import lombok.core.HandlerPriority;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
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
	 * Creates a new HandlerLibrary. Errors will be reported to the Eclipse Error log.
	 * You probably want to use {@link #load()} instead.
	 */
	public HandlerLibrary() {
		ConfigurationKeysLoader.LoaderLoader.loadAllConfigurationKeys();
	}
	
	private TypeLibrary typeLibrary = new TypeLibrary();
	
	private static class VisitorContainer {
		private final EclipseASTVisitor visitor;
		private final long priority;
		private final boolean deferUntilPostDiet;
		
		VisitorContainer(EclipseASTVisitor visitor) {
			this.visitor = visitor;
			this.deferUntilPostDiet = visitor.getClass().isAnnotationPresent(DeferUntilPostDiet.class);
			HandlerPriority hp = visitor.getClass().getAnnotation(HandlerPriority.class);
			this.priority = hp == null ? 0L : (((long)hp.value()) << 32) + hp.subValue();
		}
		
		public boolean deferUntilPostDiet() {
			return deferUntilPostDiet;
		}
		
		public long getPriority() {
			return priority;
		}
	}
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private final EclipseAnnotationHandler<T> handler;
		private final Class<T> annotationClass;
		private final long priority;
		private final boolean deferUntilPostDiet;
		
		AnnotationHandlerContainer(EclipseAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
			this.deferUntilPostDiet = handler.getClass().isAnnotationPresent(DeferUntilPostDiet.class);
			HandlerPriority hp = handler.getClass().getAnnotation(HandlerPriority.class);
			this.priority = hp == null ? 0L : (((long)hp.value()) << 32) + hp.subValue();
		}
		
		public void handle(org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				final EclipseNode annotationNode) {
			AnnotationValues<T> annValues = createAnnotation(annotationClass, annotationNode);
			handler.handle(annValues, annotation, annotationNode);
		}
		
		public void preHandle(org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				final EclipseNode annotationNode) {
			AnnotationValues<T> annValues = createAnnotation(annotationClass, annotationNode);
			handler.preHandle(annValues, annotation, annotationNode);
		}
		
		public boolean deferUntilPostDiet() {
			return deferUntilPostDiet;
		}
		
		public long getPriority() {
			return priority;
		}
	}
	
	private Map<String, AnnotationHandlerContainer<?>> annotationHandlers =
		new HashMap<String, AnnotationHandlerContainer<?>>();
	
	private Collection<VisitorContainer> visitorHandlers = new ArrayList<VisitorContainer>();

	/**
	 * Creates a new HandlerLibrary.  Errors will be reported to the Eclipse Error log.
	 * then uses SPI discovery to load all annotation and visitor based handlers so that future calls
	 * to the handle methods will defer to these handlers.
	 */
	public static HandlerLibrary load() {
		HandlerLibrary lib = new HandlerLibrary();
		
		loadAnnotationHandlers(lib);
		loadVisitorHandlers(lib);
		
		lib.calculatePriorities();
		
		return lib;
	}
	
	private SortedSet<Long> priorities;
	
	public SortedSet<Long> getPriorities() {
		return priorities;
	}
	
	private void calculatePriorities() {
		SortedSet<Long> set = new TreeSet<Long>();
		for (AnnotationHandlerContainer<?> container : annotationHandlers.values()) set.add(container.getPriority());
		for (VisitorContainer container : visitorHandlers) set.add(container.getPriority());
		this.priorities = Collections.unmodifiableSortedSet(set);
	}
	
	/** Uses SPI Discovery to find implementations of {@link EclipseAnnotationHandler}. */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void loadAnnotationHandlers(HandlerLibrary lib) {
		try {
			for (EclipseAnnotationHandler<?> handler : SpiLoadUtil.findServices(EclipseAnnotationHandler.class, EclipseAnnotationHandler.class.getClassLoader())) {
				try {
					Class<? extends Annotation> annotationClass = handler.getAnnotationHandledByThisHandler();
					AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
					String annotationClassName = container.annotationClass.getName().replace("$", ".");
					if (lib.annotationHandlers.put(annotationClassName, container) != null) {
						error(null, "Duplicate handlers for annotation type: " + annotationClassName, null);
					}
					lib.typeLibrary.addType(container.annotationClass.getName());
				} catch (Throwable t) {
					error(null, "Can't load Lombok annotation handler for Eclipse: ", t);
				}
			}
		} catch (IOException e) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	/** Uses SPI Discovery to find implementations of {@link EclipseASTVisitor}. */
	private static void loadVisitorHandlers(HandlerLibrary lib) {
		try {
			for (EclipseASTVisitor visitor : SpiLoadUtil.findServices(EclipseASTVisitor.class, EclipseASTVisitor.class.getClassLoader())) {
				lib.visitorHandlers.add(new VisitorContainer(visitor));
			}
		} catch (Throwable t) {
			throw Lombok.sneakyThrow(t);
		}
	}
	
	private boolean checkAndSetHandled(ASTNode node) {
		return !ASTNode_handled.getAndSet(node, true);
	}
	
	private boolean needsHandling(ASTNode node) {
		return !ASTNode_handled.get(node);
	}
	
	/**
	 * Handles the provided annotation node by first finding a qualifying instance of
	 * {@link EclipseAnnotationHandler} and if one exists, calling it with a freshly cooked up
	 * instance of {@link AnnotationValues}.
	 * 
	 * Note that depending on the printASTOnly flag, the {@link lombok.core.PrintAST} annotation
	 * will either be silently skipped, or everything that isn't {@code PrintAST} will be skipped.
	 * 
	 * The HandlerLibrary will attempt to guess if the given annotation node represents a lombok annotation.
	 * For example, if {@code lombok.*} is in the import list, then this method will guess that
	 * {@code Getter} refers to {@code lombok.Getter}, presuming that {@link lombok.eclipse.handlers.HandleGetter}
	 * has been loaded.
	 * 
	 * @param ast The Compilation Unit that contains the Annotation AST Node.
	 * @param annotationNode The Lombok AST Node representing the Annotation AST Node.
	 * @param annotation 'node.get()' - convenience parameter.
	 */
	public void handleAnnotation(CompilationUnitDeclaration ast, EclipseNode annotationNode, org.eclipse.jdt.internal.compiler.ast.Annotation annotation, long priority) {
		TypeResolver resolver = new TypeResolver(annotationNode.getImportList());
		TypeReference rawType = annotation.type;
		if (rawType == null) return;
		
		String fqn = resolver.typeRefToFullyQualifiedName(annotationNode, typeLibrary, toQualifiedName(annotation.type.getTypeName()));
		if (fqn == null) return;
		AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
		if (container == null) return;
		if (priority != container.getPriority()) return;
		
		if (!annotationNode.isCompleteParse() && container.deferUntilPostDiet()) {
			if (needsHandling(annotation)) container.preHandle(annotation, annotationNode);
			return;
		}
		
		try {
			if (checkAndSetHandled(annotation)) container.handle(annotation, annotationNode);
		} catch (AnnotationValueDecodeFail fail) {
			fail.owner.setError(fail.getMessage(), fail.idx);
		} catch (Throwable t) {
			error(ast, String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
		}
	}
	
	/**
	 * Will call all registered {@link EclipseASTVisitor} instances.
	 */
	public void callASTVisitors(EclipseAST ast, long priority, boolean isCompleteParse) {
		for (VisitorContainer container : visitorHandlers) {
			if (!isCompleteParse && container.deferUntilPostDiet()) continue;
			if (priority != container.getPriority()) continue;
			try {
				ast.traverse(container.visitor);
			} catch (Throwable t) {
				error((CompilationUnitDeclaration) ast.top().get(),
						String.format("Lombok visitor handler %s failed", container.visitor.getClass()), t);
			}
		}
	}
}
