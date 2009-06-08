package lombok.apt;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import lombok.Getter;


@SupportedAnnotationTypes("lombok.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for ( TypeElement typeElement : annotations ) {
			if ( typeElement.getQualifiedName().contentEquals(Getter.class.getName()) )
				return handle(roundEnv, Getter.class, typeElement);
		}
		
		return false;
	}
	
	private <T extends Annotation> boolean handle(RoundEnvironment roundEnv, Class<T> annotation, TypeElement typeElement) {
		for ( Element element : roundEnv.getElementsAnnotatedWith(typeElement) ) {
			new AnnotationTransponder<T>(annotation, roundEnv, processingEnv).handle(element, element.getAnnotation(annotation));
		}
		return true;
	}
}
