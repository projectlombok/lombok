package lombok.eclipse.handlers;

import lombok.Builder;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.SuperBuilder;
import lombok.spi.Provides;
import org.eclipse.jdt.internal.compiler.ast.Annotation;

@Provides
@HandlerPriority(-1025) //HandleBuilder's level, minus one.
public class HandleBuilderExclude extends EclipseAnnotationHandler<Builder.Exclude> {
    @Override
    public void handle(AnnotationValues<Builder.Exclude> annotation, Annotation ast, EclipseNode annotationNode) {
        EclipseNode annotatedField = annotationNode.up();
        if (annotatedField.getKind() != AST.Kind.FIELD) return;
        EclipseNode classWithAnnotatedField = annotatedField.up();
        if (!EclipseHandlerUtil.hasAnnotation(Builder.class, classWithAnnotatedField) && !EclipseHandlerUtil.hasAnnotation("lombok.experimental.Builder", classWithAnnotatedField)
                && !EclipseHandlerUtil.hasAnnotation(SuperBuilder.class, classWithAnnotatedField)) {
            annotationNode.addWarning("@Builder.Exclude requires @Builder or @SuperBuilder on the class for it to mean anything.");
        }
    }
}
