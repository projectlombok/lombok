package lombok.eclipse.handlers;

import org.eclipse.jdt.internal.compiler.ast.Annotation;

import lombok.Onstruct;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

@HandlerPriority(65536) // same as HandleValue // TODO
public class HandleOnstruct extends EclipseAnnotationHandler<Onstruct> {
	
	public static final HandleOnstruct INSTANCE = new HandleOnstruct();
	
	@Override public void handle(AnnotationValues<Onstruct> annotation, Annotation ast, EclipseNode annotationNode) {
		// TODO Auto-generated method stub

	}
	
	
}
