package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.After;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

@Provides
@HandlerPriority(Integer.MAX_VALUE) // If we want this annotation to work on Lombok generated methods, this handler must run last.
public class HandleAfter extends JavacAnnotationHandler<After> {
	@Override 
	public void handle(AnnotationValues<After> annotation, JCAnnotation ast, JavacNode annotationNode) {
		// TODO Auto-generated method stub
	}
}
