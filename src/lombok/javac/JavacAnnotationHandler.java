package lombok.javac;

import java.lang.annotation.Annotation;

import lombok.core.AnnotationValues;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

public interface JavacAnnotationHandler<T extends Annotation> {
	boolean handle(AnnotationValues<T> annotation, JCAnnotation ast, JavacAST.Node annotationNode);
}
