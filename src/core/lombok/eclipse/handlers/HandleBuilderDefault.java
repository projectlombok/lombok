/*
 * Copyright (C) 2017-2018 The Project Lombok Authors.
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
package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.Builder;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.SuperBuilder;

@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-1025) //HandleBuilder's level, minus one.
public class HandleBuilderDefault extends EclipseAnnotationHandler<Builder.Default> {
	@Override public void handle(AnnotationValues<Builder.Default> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode annotatedField = annotationNode.up();
		if (annotatedField.getKind() != Kind.FIELD) return;
		EclipseNode classWithAnnotatedField = annotatedField.up();
		if (!hasAnnotation(Builder.class, classWithAnnotatedField) && !hasAnnotation("lombok.experimental.Builder", classWithAnnotatedField)
				&& !hasAnnotation(SuperBuilder.class, classWithAnnotatedField)) {
			annotationNode.addWarning("@Builder.Default requires @Builder or @SuperBuilder on the class for it to mean anything.");
		}
	}
}
