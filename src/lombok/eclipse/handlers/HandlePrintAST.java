package lombok.eclipse.handlers;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandlePrintAST implements EclipseAnnotationHandler<PrintAST> {
	@Override public void handle(AnnotationValues<PrintAST> annotation, Annotation ast, Node annotationNode) {
		annotationNode.up().traverse(new EclipseASTVisitor.EclipseASTPrinter());
	}
}
