/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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
package lombok.javac;

import java.lang.annotation.Annotation;

import lombok.core.AnnotationValues;
import lombok.core.SpiLoadUtil;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;

/**
 * Implement this interface if you want to be triggered for a specific annotation.
 * 
 * You MUST replace 'T' with a specific annotation type, such as:
 * 
 * {@code public class HandleGetter extends JavacAnnotationHandler<Getter>}
 * 
 * Because this generics parameter is inspected to figure out which class you're interested in.
 * 
 * You also need to register yourself via SPI discovery as being an implementation of {@code JavacAnnotationHandler}.
 */
public abstract class JavacAnnotationHandler<T extends Annotation> {
	protected Trees trees;
	
	/**
	 * Called when an annotation is found that is likely to match the annotation you're interested in.
	 * 
	 * Be aware that you'll be called for ANY annotation node in the source that looks like a match. There is,
	 * for example, no guarantee that the annotation node belongs to a method, even if you set your
	 * TargetType in the annotation to methods only.
	 * 
	 * @param annotation The actual annotation - use this object to retrieve the annotation parameters.
	 * @param ast The javac AST node representing the annotation.
	 * @param annotationNode The Lombok AST wrapper around the 'ast' parameter. You can use this object
	 * to travel back up the chain (something javac AST can't do) to the parent of the annotation, as well
	 * as access useful methods such as generating warnings or errors focused on the annotation.
	 */
	public abstract void handle(AnnotationValues<T> annotation, JCAnnotation ast, JavacNode annotationNode);
	
	/**
	 * This handler is a handler for the given annotation; you don't normally need to override this class
	 * as the annotation type is extracted from your {@code extends EclipseAnnotationHandler<AnnotationTypeHere>}
	 * signature.
	 */
	@SuppressWarnings("unchecked") public Class<T> getAnnotationHandledByThisHandler() {
		return (Class<T>) SpiLoadUtil.findAnnotationClass(getClass(), JavacAnnotationHandler.class);
	}
	
	public void setTrees(Trees trees) {
		this.trees = trees;
	}
}
