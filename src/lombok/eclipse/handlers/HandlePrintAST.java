package lombok.eclipse.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandlePrintAST implements EclipseAnnotationHandler<PrintAST> {
	@Override public boolean handle(AnnotationValues<PrintAST> annotation, Annotation ast, Node annotationNode) {
		if ( !annotationNode.isCompleteParse() ) return false;
		
		PrintStream stream = System.out;
		String fileName = annotation.getInstance().outfile();
		if ( fileName.length() > 0 ) try {
			stream = new PrintStream(new File(fileName));
		} catch ( FileNotFoundException e ) {
			Lombok.sneakyThrow(e);
		}
		
		annotationNode.up().traverse(new EclipseASTVisitor.Printer(stream));
		return true;
	}
}
