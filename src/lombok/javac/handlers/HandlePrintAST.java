package lombok.javac.handlers;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

@ProviderFor(JavacAnnotationHandler.class)
public class HandlePrintAST implements JavacAnnotationHandler<PrintAST> {
	@Override public boolean handle(AnnotationValues<PrintAST> annotation, JCAnnotation ast, Node annotationNode) {
		annotationNode.up().traverse(new JavacASTVisitor.JavacASTPrinter());
		return true;
	}
}
