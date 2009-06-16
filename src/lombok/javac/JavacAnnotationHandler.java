package lombok.javac;

import java.lang.annotation.Annotation;

public interface JavacAnnotationHandler<T extends Annotation> {
	void handle(JavacNode annotedElement, T annotation);
}
