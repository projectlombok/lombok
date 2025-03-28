package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import lombok.Builder;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.SuperBuilder;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import static lombok.javac.handlers.JavacHandlerUtil.hasAnnotation;

@Provides
@HandlerPriority(-1026) //HandleBuilder's level, minus one.
public class HandleBuilderExclude extends JavacAnnotationHandler<Builder.Exclude> {
    @Override public void handle(AnnotationValues<Builder.Exclude> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        JavacNode annotatedField = annotationNode.up();
        if (annotatedField.getKind() != AST.Kind.FIELD) return;
        JavacNode classWithAnnotatedField = annotatedField.up();
        if (!hasAnnotation(Builder.class, classWithAnnotatedField) && !hasAnnotation("lombok.experimental.Builder", classWithAnnotatedField)
                && !hasAnnotation(SuperBuilder.class, classWithAnnotatedField)) {
            annotationNode.addWarning("@Builder.Exclude requires @Builder or @SuperBuilder on the class for it to mean anything.");
            deleteAnnotationIfNeccessary(annotationNode, Builder.Exclude.class);
        }
    }
}
