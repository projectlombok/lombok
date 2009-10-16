/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.disableCheckedExceptions;

import java.lang.instrument.Instrumentation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.inject.LiveInjector;
import lombok.patcher.scripts.ScriptBuilder;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DisableCheckedExceptionsAgent extends AbstractProcessor {
	/** Inject an agent if we're on a sun-esque JVM. */
	@Override public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		if (!(processingEnv instanceof JavacProcessingEnvironment)) {
			processingEnv.getMessager().printMessage(Kind.WARNING, "You aren't using a compiler based around javac v1.6, so lombok will not work properly.");
			this.processingEnv = null;
		}
		
		new LiveInjector().injectSelf();
	}
	
	/** Does nothing - we just wanted the init method so we can inject an agent. */
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		return false;
	}
	
	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
		registerPatchScripts(instrumentation, true);
	}
	
	public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
		registerPatchScripts(instrumentation, false);
	}
	
	private static void registerPatchScripts(Instrumentation instrumentation, boolean reloadExistingClasses) {
		ScriptManager sm = new ScriptManager();
		sm.registerTransformer(instrumentation);
		
		patchExceptions(sm);
		
		if (reloadExistingClasses) sm.reloadClasses(instrumentation);
	}
	
	private static void patchExceptions(ScriptManager sm) {
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("com.sun.tools.javac.comp.Check", "isUnchecked",
						"boolean", "com.sun.tools.javac.code.Symbol$ClassSymbol"))
				.transplant()
				.decisionMethod(new Hook("lombok/javac/disableCheckedExceptions/DisableCheckedExceptionsAgent", "retTrue", "()Z"))
				.valueMethod(new Hook("lombok/javac/disableCheckedExceptions/DisableCheckedExceptionsAgent", "retTrue", "()Z"))
				.build());
	}
	
	public static boolean retTrue() {
		return true;
	}
}
