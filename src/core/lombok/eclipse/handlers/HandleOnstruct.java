package lombok.eclipse.handlers;

import java.io.PrintStream;

import org.eclipse.jdt.internal.compiler.ast.Annotation;

import lombok.Onstruct;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.spi.Provides;

@Provides
@DeferUntilPostDiet
@HandlerPriority(65536) // same as HandleValue // TODO
public class HandleOnstruct extends EclipseAnnotationHandler<Onstruct> {
	
	public static final HandleOnstruct INSTANCE = new HandleOnstruct();
	
	@Override public void handle(AnnotationValues<Onstruct> annotation, Annotation ast, EclipseNode annotationNode) {
		PrintStream stream = System.out;
		stream.println("got annotation on " + ast);
		annotationNode.up().traverse(new EclipseASTVisitor.Printer(true, stream, true));
	}
	
	
}
