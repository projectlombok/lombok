package lombok.eclipse;

import lombok.core.AnnotationValues;

public interface EclipseAnnotationHandler<T extends java.lang.annotation.Annotation> {
	boolean handle(AnnotationValues<T> annotation, org.eclipse.jdt.internal.compiler.ast.Annotation ast, EclipseAST.Node annotationNode);
}
