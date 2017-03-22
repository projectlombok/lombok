package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.Builder;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1025) //HandleBuilder's level, minus one.
public class HandleBuilderDefault extends JavacAnnotationHandler<Builder.Default> {
	@SuppressWarnings("deprecation")
	@Override public void handle(AnnotationValues<Builder.Default> annotation, JCAnnotation ast, JavacNode annotationNode) {
		JavacNode annotatedField = annotationNode.up();
		if (annotatedField.getKind() != Kind.FIELD) return;
		JavacNode classWithAnnotatedField = annotatedField.up();
		if (!hasAnnotation(Builder.class, classWithAnnotatedField) && !hasAnnotation(lombok.experimental.Builder.class, classWithAnnotatedField)) {
			annotationNode.addWarning("@Builder.Default requires @Builder on the class for it to mean anything.");
			deleteAnnotationIfNeccessary(annotationNode, Builder.Default.class);
		}
	}
}
