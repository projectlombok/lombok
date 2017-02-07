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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

/**
 * Handles the {@code lombok.Data} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleData extends JavacAnnotationHandler<Data> {
	@Override public void handle(AnnotationValues<Data> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.DATA_FLAG_USAGE, "@Data");
		
		deleteAnnotationIfNeccessary(annotationNode, Data.class);
		JavacNode typeNode = annotationNode.up();
		boolean notAClass = !isClass(typeNode);
		
		if (notAClass) {
			annotationNode.addError("@Data is only supported on a class.");
			return;
		}

		Data ann = annotation.getInstance();
		String staticConstructorName = ann.staticConstructor();
		Boolean callSuper = ann.callSuper();
		if (!annotation.isExplicit("callSuper")) callSuper = null;

		// TODO move this to the end OR move it to the top in eclipse.
		new HandleConstructor().generateRequiredArgsConstructor(typeNode, AccessLevel.PUBLIC, staticConstructorName, SkipIfConstructorExists.YES, annotationNode);
		new HandleGetter().generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true);
		new HandleSetter().generateSetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true);
		new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode, callSuper);
		new HandleToString().generateToStringForType(typeNode, annotationNode, callSuper);
	}
}
