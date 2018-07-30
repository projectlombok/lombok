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
package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import lombok.Builder;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.SuperBuilder;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1025) //HandleBuilder's level, minus one.
public class HandleBuilderDefault extends JavacAnnotationHandler<Builder.Default> {
	@Override public void handle(AnnotationValues<Builder.Default> annotation, JCAnnotation ast, JavacNode annotationNode) {
		JavacNode annotatedField = annotationNode.up();
		if (annotatedField.getKind() != Kind.FIELD) return;
		JavacNode classWithAnnotatedField = annotatedField.up();
		if (!hasAnnotation(Builder.class, classWithAnnotatedField) && !hasAnnotation("lombok.experimental.Builder", classWithAnnotatedField)
				&& !hasAnnotation(SuperBuilder.class, classWithAnnotatedField)) {
			annotationNode.addWarning("@Builder.Default requires @Builder or @SuperBuilder on the class for it to mean anything.");
			deleteAnnotationIfNeccessary(annotationNode, Builder.Default.class);
		}
		
		/** HandleBuilder is going to wipe out the import, at which point '@Builder.Default' is no longer clear. */
		if (ast.annotationType instanceof JCFieldAccess) {
			JCFieldAccess jfa = (JCFieldAccess) ast.annotationType;
			if (jfa.selected instanceof JCIdent && ((JCIdent) jfa.selected).name.contentEquals("Builder") && jfa.name.contentEquals("Default")) {
				JCFieldAccess newJfaSel = annotationNode.getTreeMaker().Select(annotationNode.getTreeMaker().Ident(annotationNode.toName("lombok")), ((JCIdent) jfa.selected).name);
				jfa.selected = newJfaSel;
			}
		}
	}
}
