/*
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;

import java.util.Collections;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.DataVersionable;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;

/**
 * Handles the {@code lombok.DataVersionable} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleDataVersionable extends EclipseAnnotationHandler<DataVersionable> {
    @Override
    public void handle(AnnotationValues<DataVersionable> annotation, Annotation ast, EclipseNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.DATA_VERSIONABLE_FLAG_USAGE, "@DataVersionable");

        DataVersionable annData = annotation.getInstance();
        EclipseNode typeNode = annotationNode.up();

        TypeDeclaration typeDecl = null;
        if (typeNode.get() instanceof TypeDeclaration)
            typeDecl = (TypeDeclaration) typeNode.get();
        int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
        boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;

        if (typeDecl == null || notAClass) {
            annotationNode.addError("@DataVersionable is only supported on a class.");
            return;
        }

        //Careful: Generate the public static constructor (if there is one) LAST, so that any attempt to
        //'find callers' on the annotation node will find callers of the constructor, which is by far the
        //most useful of the many methods built by @DataVersionable. This trick won't work for the non-static constructor,
        //for whatever reason, though you can find callers of that one by focusing on the class name itself
        //and hitting 'find callers'

        new HandleGetter().generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true);
        new HandleSetterVersionable().generateSetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, annData);
        new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode);
        new HandleToString().generateToStringForType(typeNode, annotationNode);
        new HandleConstructor().generateRequiredArgsConstructor(typeNode, AccessLevel.PUBLIC, annData.staticConstructor(), SkipIfConstructorExists.YES, Collections.<Annotation> emptyList(), annotationNode);
    }
}
