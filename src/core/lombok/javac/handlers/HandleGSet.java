/*
 * Copyright (C) 2009-2025 The Project Lombok Authors.
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
import lombok.GSet;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.GSet} annotation for javac.
 */
@Provides
public class HandleGSet extends JavacAnnotationHandler<Data>{

    private HandleGetter handleGetter = new HandleGetter();
    private HandleSetter handleSetter = new HandleSetter();


    @Override
    public void handle(AnnotationValues<GSet> annotation, JCAnnotation ast, JavacNode annotationNode){
        handleFlagUsage(annotationNode, ConfigurationKeys.GSET_FLAG_USAGE, "@GSet");

        deleteAnnotationIfNeccessary(annotationNode, GSet.class);
        JavacNode typeNode = annotationNode.up();

        if (!isClass(typeNode)) {
            annotationNode.addError("@GSet is only supported on a class.");
            return;
        }

        String staticConstructorName = annotation.getInstance().staticConstructor();

        handleGetter.generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, List.<JCAnnotation>nil());
        handleSetter.generateSetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
    }
}
