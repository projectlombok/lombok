/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.Accessors;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(65536)
public class HandleAccessors extends JavacAnnotationHandler<Accessors> {
	@Override public void handle(AnnotationValues<Accessors> annotation, JCAnnotation ast, JavacNode annotationNode) {
		// Accessors itself is handled by HandleGetter/Setter; this is just to ensure that the annotation is removed
		// from the AST when delomboking.
		
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.ACCESSORS_FLAG_USAGE, "@Accessors");
		
		deleteAnnotationIfNeccessary(annotationNode, Accessors.class);
	}
}
