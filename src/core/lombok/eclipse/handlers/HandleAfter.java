package lombok.eclipse.handlers;

import org.eclipse.jdt.internal.compiler.ast.Annotation;

import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.After;
import lombok.spi.Provides;

@Provides
@HandlerPriority(Integer.MAX_VALUE) // If we want this annotation to work on Lombok generated methods, this handler must run last.
public class HandleAfter extends EclipseAnnotationHandler<After> {
	@Override 
	public void handle(AnnotationValues<After> annotation, Annotation ast, EclipseNode annotationNode) {
		// TODO Auto-generated method stub	
	}
}
