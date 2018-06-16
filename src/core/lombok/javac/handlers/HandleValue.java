/*
 * Copyright (C) 2012-2018 The Project Lombok Authors.
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
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.NonFinal;
import lombok.Value;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.Value} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-512) //-2^9; to ensure @EqualsAndHashCode and such pick up on this handler making the class final and messing with the fields' access levels, run earlier.
public class HandleValue extends JavacAnnotationHandler<Value> {
	private HandleFieldDefaults handleFieldDefaults = new HandleFieldDefaults();
	private HandleConstructor handleConstructor = new HandleConstructor();
	private HandleGetter handleGetter = new HandleGetter();
	private HandleEqualsAndHashCode handleEqualsAndHashCode = new HandleEqualsAndHashCode();
	private HandleToString handleToString = new HandleToString();
	
	@Override public void handle(AnnotationValues<Value> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.VALUE_FLAG_USAGE, "@Value");
		
		deleteAnnotationIfNeccessary(annotationNode, Value.class, "lombok.experimental.Value");
		JavacNode typeNode = annotationNode.up();
		boolean notAClass = !isClass(typeNode);
		
		if (notAClass) {
			annotationNode.addError("@Value is only supported on a class.");
			return;
		}
		
		String staticConstructorName = annotation.getInstance().staticConstructor();
		
		if (!hasAnnotationAndDeleteIfNeccessary(NonFinal.class, typeNode)) {
			JCModifiers jcm = ((JCClassDecl) typeNode.get()).mods;
			if ((jcm.flags & Flags.FINAL) == 0) {
				jcm.flags |= Flags.FINAL;
				typeNode.rebuild();
			}
		}
		handleFieldDefaults.generateFieldDefaultsForType(typeNode, annotationNode, AccessLevel.PRIVATE, true, true);
		handleConstructor.generateAllArgsConstructor(typeNode, AccessLevel.PUBLIC, staticConstructorName, SkipIfConstructorExists.YES, annotationNode);
		handleConstructor.generateExtraNoArgsConstructor(typeNode, annotationNode);
		handleGetter.generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, List.<JCAnnotation>nil());
		handleEqualsAndHashCode.generateEqualsAndHashCodeForType(typeNode, annotationNode);
		handleToString.generateToStringForType(typeNode, annotationNode);
	}
}
