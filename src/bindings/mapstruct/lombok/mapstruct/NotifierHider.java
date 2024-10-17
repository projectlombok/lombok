package lombok.mapstruct;

import org.mapstruct.ap.spi.AstModifyingAnnotationProcessor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Report to MapStruct that a type is completed when there aren't any Lombok annotations left on it. Lombok annotations
 * are removed whenever a class is processed. This way, annotations which require multiple rounds to process are also
 * correctly handled, and MapStruct processing will be delayed until Lombok completely finishes processing required types.
 */
class NotifierHider {
	public static class AstModificationNotifier implements AstModifyingAnnotationProcessor {
		@Override
		public boolean isTypeComplete(TypeMirror typeMirror) {
			List<? extends AnnotationMirror> annotationMirrors = typeMirror.getAnnotationMirrors();
			if (annotationMirrors == null || annotationMirrors.isEmpty()) return true;
			
			for (AnnotationMirror annotationMirror : annotationMirrors) {
				String annotationName = String.valueOf(annotationMirror);
				if (annotationName.startsWith("@lombok.")) return false;
			}
			
			return true;
		}
	}
}
