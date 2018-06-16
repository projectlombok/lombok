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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Collections;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.experimental.NonFinal;
import lombok.Value;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Value} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-512) //-2^9; to ensure @EqualsAndHashCode and such pick up on this handler making the class final and messing with the fields' access levels, run earlier.
public class HandleValue extends EclipseAnnotationHandler<Value> {
	private HandleFieldDefaults handleFieldDefaults = new HandleFieldDefaults();
	private HandleGetter handleGetter = new HandleGetter();
	private HandleEqualsAndHashCode handleEqualsAndHashCode = new HandleEqualsAndHashCode();
	private HandleToString handleToString = new HandleToString();
	private HandleConstructor handleConstructor = new HandleConstructor();
	
	public void handle(AnnotationValues<Value> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.VALUE_FLAG_USAGE, "@Value");
		
		Value ann = annotation.getInstance();
		EclipseNode typeNode = annotationNode.up();
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			annotationNode.addError("@Value is only supported on a class.");
			return;
		}
		
		// Make class final.
		if (!hasAnnotation(NonFinal.class, typeNode)) {
			if ((typeDecl.modifiers & ClassFileConstants.AccFinal) == 0) {
				typeDecl.modifiers |= ClassFileConstants.AccFinal;
				typeNode.rebuild();
			}
		}
		
		handleFieldDefaults.generateFieldDefaultsForType(typeNode, annotationNode, AccessLevel.PRIVATE, true, true);
		
		//Careful: Generate the public static constructor (if there is one) LAST, so that any attempt to
		//'find callers' on the annotation node will find callers of the constructor, which is by far the
		//most useful of the many methods built by @Value. This trick won't work for the non-static constructor,
		//for whatever reason, though you can find callers of that one by focusing on the class name itself
		//and hitting 'find callers'.
		
		handleGetter.generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, Collections.<Annotation>emptyList());
		handleEqualsAndHashCode.generateEqualsAndHashCodeForType(typeNode, annotationNode);
		handleToString.generateToStringForType(typeNode, annotationNode);
		handleConstructor.generateAllArgsConstructor(typeNode, AccessLevel.PUBLIC, ann.staticConstructor(), SkipIfConstructorExists.YES,
				Collections.<Annotation>emptyList(), annotationNode);
		handleConstructor.generateExtraNoArgsConstructor(typeNode, annotationNode);
	}
}
