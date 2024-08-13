package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.Builder;
import lombok.Builder.Exclude;
import lombok.core.AlreadyHandledAnnotations;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

@Provides
@HandlerPriority(32768)
@AlreadyHandledAnnotations
public class HandleBuilderExcludeRemove extends JavacAnnotationHandler<Builder.Exclude> {
    @Override public void handle(AnnotationValues<Exclude> annotation, JCAnnotation ast, JavacNode annotationNode) {
        deleteAnnotationIfNeccessary(annotationNode, Builder.Exclude.class);
        deleteImportFromCompilationUnit(annotationNode, Builder.class.getName());
        deleteImportFromCompilationUnit(annotationNode, Builder.Exclude.class.getName());
    }
}