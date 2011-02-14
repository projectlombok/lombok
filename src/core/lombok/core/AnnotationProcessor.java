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
package lombok.core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import lombok.patcher.inject.LiveInjector;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {
	private static String trace(Throwable t) {
		StringWriter w = new StringWriter();
		t.printStackTrace(new PrintWriter(w, true));
		return w.toString();
	}
	
	static abstract class ProcessorDescriptor {
		abstract boolean want(ProcessingEnvironment procEnv, List<String> delayedWarnings);
		abstract String getName();
		abstract boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
	}
	
	private final List<ProcessorDescriptor> registered = Arrays.asList(new JavacDescriptor(), new EcjDescriptor());
	private final List<ProcessorDescriptor> active = new ArrayList<ProcessorDescriptor>();
	private final List<String> delayedWarnings = new ArrayList<String>();
	
	private static final Map<ClassLoader, Boolean> lombokAlreadyAddedTo = new WeakHashMap<ClassLoader, Boolean>();
	
	static class JavacDescriptor extends ProcessorDescriptor {
		private Processor processor;
		
		@Override String getName() {
			return "sun/apple javac 1.6";
		}
		
		@Override boolean want(ProcessingEnvironment procEnv, List<String> delayedWarnings) {
			if (!procEnv.getClass().getName().equals("com.sun.tools.javac.processing.JavacProcessingEnvironment")) return false;
			
			try {
				ClassLoader toFix = procEnv.getClass().getClassLoader();
				if (toFix.getClass().getCanonicalName().equals("org.codehaus.plexus.compiler.javac.IsolatedClassLoader")) {
					if (lombokAlreadyAddedTo.put(toFix, true) == null) {
						Method m = toFix.getClass().getDeclaredMethod("addURL", URL.class);
						URL selfUrl = new File(LiveInjector.findPathJar(AnnotationProcessor.class)).toURI().toURL();
						m.invoke(toFix, selfUrl);
					}
				}
				processor = (Processor)Class.forName("lombok.javac.apt.Processor", false, procEnv.getClass().getClassLoader()).newInstance();
			} catch (Exception e) {
				delayedWarnings.add("You found a bug in lombok; lombok.javac.apt.Processor is not available. Lombok will not run during this compilation: " + trace(e));
				return false;
			} catch (NoClassDefFoundError e) {
				delayedWarnings.add("Can't load javac processor due to (most likely) a class loader problem: " + trace(e));
				return false;
			}
			try {
				processor.init(procEnv);
			} catch (Exception e) {
				delayedWarnings.add("lombok.javac.apt.Processor could not be initialized. Lombok will not run during this compilation: " + trace(e));
				return false;
			} catch (NoClassDefFoundError e) {
				delayedWarnings.add("Can't initialize javac processor due to (most likely) a class loader problem: " + trace(e));
				return false;
			}
			return true;
		}
		
		@Override boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return processor.process(annotations, roundEnv);
		}
	}
	
	static class EcjDescriptor extends ProcessorDescriptor {
		private Processor processor;
		
		@Override String getName() {
			return "ECJ";
		}
		
		@Override boolean want(ProcessingEnvironment procEnv, List<String> delayedWarnings) {
			if (!procEnv.getClass().getName().startsWith("org.eclipse.jdt.")) return false;
			boolean inEclipse;
			try {
				Class.forName("org.eclipse.core.runtime.Platform");	//if this works, we're in eclipse.
				inEclipse = true;
			} catch (ClassNotFoundException e) {
				inEclipse = false;	//We're in ecj.
			}
			
			if (inEclipse) {
				delayedWarnings.add("You should not install lombok.jar as an annotation processor in eclipse. Instead, run lombok.jar as a java application and follow the instructions.");
				return false;
			}
			
			try {
				processor = (Processor)Class.forName("lombok.eclipse.apt.Processor").newInstance();
			} catch (Exception e) {
				delayedWarnings.add("You found a bug in lombok; lombok.eclipse.apt.Processor is not available. Lombok will not run during this compilation: " + trace(e));
				return false;
			} catch (NoClassDefFoundError e) {
				delayedWarnings.add("Can't load eclipse processor due to (most likely) a class loader problem: " + trace(e));
				return false;
			}
			
			processor.init(procEnv);
			return true;
		}
		
		@Override boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return processor.process(annotations, roundEnv);
		}
	}
	
	@Override public void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		for (ProcessorDescriptor proc : registered) {
			if (proc.want(procEnv, delayedWarnings)) active.add(proc);
		}
		
		if (active.isEmpty() && delayedWarnings.isEmpty()) {
			StringBuilder supported = new StringBuilder();
			for (ProcessorDescriptor proc : registered) {
				if (supported.length() > 0) supported.append(", ");
				supported.append(proc.getName());
			}
			procEnv.getMessager().printMessage(Kind.WARNING, String.format("You aren't using a compiler supported by lombok, so lombok will not work and has been disabled.\n" +
					"Your processor is: %s\nLombok supports: %s", procEnv.getClass().getName(), supported));
		}
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!delayedWarnings.isEmpty()) {
			Set<? extends Element> rootElements = roundEnv.getRootElements();
			if (!rootElements.isEmpty()) {
				Element firstRoot = rootElements.iterator().next();
				for (String warning : delayedWarnings) processingEnv.getMessager().printMessage(Kind.WARNING, warning, firstRoot);
				delayedWarnings.clear();
			}
		}
		
		boolean handled = false;
		for (ProcessorDescriptor proc : active) {
			handled |= proc.process(annotations, roundEnv);
		}
		
		return handled;
	}
}
