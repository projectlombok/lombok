/*
 * Copyright (C) 2026 The Project Lombok Authors.
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

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import lombok.ConfigurationKeys;
import lombok.Locked;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import static lombok.core.handlers.HandlerUtil.handleFlagUsage;

/** Handles {@code lombok.Locked.Policy} for javac. */
@Provides
@HandlerPriority(65536)
public class HandleLockedPolicy extends JavacAnnotationHandler<Locked.Policy> {
	@Override public void handle(AnnotationValues<Locked.Policy> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.LOCKED_FLAG_USAGE, "@Locked.Policy");
		deleteAnnotationIfNeccessary(annotationNode, Locked.Policy.class);
	}
}
