package lombok.apt;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class HandleANY_ecj extends HandlerForCompiler<Annotation> {
	@Override public void handle(Element element, Annotation annotation) throws Exception {
		//TODO: We should find eclipse's eclipse.ini file and patch us in as a javaagent and bootclasspath/a.
		//Though, we should probably use reflection to find eclipse's SWT system and generate a popup dialog for
		//confirmation.
		
		String msg = "You'll need to install the eclipse patch. See http://lombok.github.org/ for more info.";
		processEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, element);
	}
}
