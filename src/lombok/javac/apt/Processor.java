package lombok.javac.apt;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import lombok.javac.HandlerLibrary;
import lombok.javac.JavacNode;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;


@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	private JavacProcessingEnvironment processingEnv;
	private HandlerLibrary handlers;
	private Trees trees;
	
	@Override public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		if ( !(processingEnv instanceof JavacProcessingEnvironment) ) this.processingEnv = null;
		else {
			this.processingEnv = (JavacProcessingEnvironment) processingEnv;
			handlers = HandlerLibrary.load(processingEnv.getMessager());
			trees = Trees.instance(processingEnv);
		}
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if ( processingEnv == null ) return false;
		
		
		for ( TypeElement annotationType : annotations ) {
			if ( !handlers.hasHandlerFor(annotationType) ) continue;
			for ( Element element : roundEnv.getElementsAnnotatedWith(annotationType) ) {
				handlers.handleAnnotation(createNode(element), annotationType);
			}
		}
		
		for ( Element element : roundEnv.getRootElements() ) {
			if ( element instanceof TypeElement ) {
				handlers.handleType((TypeElement)element);
			}
		}
		
		return false;
	}
	
	private JavacNode createNode(Element element) {
		return new JavacNode(trees, processingEnv, element);
	}
}
