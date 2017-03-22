package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.Builder;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-1025) //HandleBuilder's level, minus one.
public class HandleBuilderDefault extends EclipseAnnotationHandler<Builder.Default> {
	@Override public void handle(AnnotationValues<Builder.Default> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode annotatedField = annotationNode.up();
		if (annotatedField.getKind() != Kind.FIELD) return;
		EclipseNode classWithAnnotatedField = annotatedField.up();
		if (!hasAnnotation(Builder.class, classWithAnnotatedField)) {
			annotationNode.addWarning("@Builder.Default requires @Builder on the class for it to mean anything.");
		}
	}
}
