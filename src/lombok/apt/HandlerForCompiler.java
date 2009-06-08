package lombok.apt;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public abstract class HandlerForCompiler<T extends Annotation> {
	protected ProcessingEnvironment processEnv;
	protected RoundEnvironment roundEnv;
	
	public void init() throws Exception {}
	
	public abstract void handle(Element element, T annotation) throws Exception;
}
