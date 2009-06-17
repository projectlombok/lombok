package lombok.javac.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

@ProviderFor(JavacAnnotationHandler.class)
public class HandlePrintAST implements JavacAnnotationHandler<PrintAST> {
	@Override public boolean handle(AnnotationValues<PrintAST> annotation, JCAnnotation ast, Node annotationNode) {
		PrintStream stream = System.out;
		String fileName = annotation.getInstance().outfile();
		if ( fileName.length() > 0 ) try {
			stream = new PrintStream(new File(fileName));
		} catch ( FileNotFoundException e ) {
			Lombok.sneakyThrow(e);
		}
		
		annotationNode.up().traverse(new JavacASTVisitor.Printer(stream));
		
		return true;
	}
}
