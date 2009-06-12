package lombok.eclipse;

public interface EclipseAnnotationHandler<T extends java.lang.annotation.Annotation> {
	void handle(T annotation, org.eclipse.jdt.internal.compiler.ast.Annotation ast, EclipseAST.Node node);
}
