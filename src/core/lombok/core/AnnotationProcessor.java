/*
 * Copyright (C) 2009-2018 The Project Lombok Authors.
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

import static lombok.core.Augments.ClassLoader_lombokAlreadyAddedTo;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import lombok.patcher.ClassRootFinder;
import lombok.permit.Permit;

@SupportedAnnotationTypes("*")
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
	
	/**
	 * This method is a simplified version of {@link lombok.javac.apt.LombokProcessor.getJavacProcessingEnvironment}
	 * It simply returns the processing environment, but in case of gradle incremental compilation,
	 * the delegate ProcessingEnvironment of the gradle wrapper is returned.
	 */
	public static ProcessingEnvironment getJavacProcessingEnvironment(ProcessingEnvironment procEnv, List<String> delayedWarnings) {
		return tryRecursivelyObtainJavacProcessingEnvironment(procEnv);
	}
	
	private static ProcessingEnvironment tryRecursivelyObtainJavacProcessingEnvironment(ProcessingEnvironment procEnv) {
		if (procEnv.getClass().getName().equals("com.sun.tools.javac.processing.JavacProcessingEnvironment")) {
			return procEnv;
		}
		
		for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
			try {
				Field field = Permit.getField(procEnvClass, "delegate");
				Object delegate = field.get(procEnv);
				
				return tryRecursivelyObtainJavacProcessingEnvironment((ProcessingEnvironment) delegate);
			} catch (final Exception e) {
				// no valid delegate, try superclass
			}
		}
		
		return null;
	}
	
	static class JavacDescriptor extends ProcessorDescriptor {
		private Processor processor;
		
		@Override String getName() {
			return "OpenJDK javac";
		}
		
		@Override boolean want(ProcessingEnvironment procEnv, List<String> delayedWarnings) {
			// do not run on ECJ as it may print warnings
			if (procEnv.getClass().getName().startsWith("org.eclipse.jdt.")) return false;
			
			ProcessingEnvironment javacProcEnv = getJavacProcessingEnvironment(procEnv, delayedWarnings);
			
			if (javacProcEnv == null) return false;
			
			try {
				ClassLoader classLoader = findAndPatchClassLoader(javacProcEnv);
				processor = (Processor) Class.forName("lombok.javac.apt.LombokProcessor", false, classLoader).getConstructor().newInstance();
			} catch (Exception e) {
				delayedWarnings.add("You found a bug in lombok; lombok.javac.apt.LombokProcessor is not available. Lombok will not run during this compilation: " + trace(e));
				return false;
			} catch (NoClassDefFoundError e) {
				delayedWarnings.add("Can't load javac processor due to (most likely) a class loader problem: " + trace(e));
				return false;
			}
			try {
				processor.init(procEnv);
			} catch (Exception e) {
				delayedWarnings.add("lombok.javac.apt.LombokProcessor could not be initialized. Lombok will not run during this compilation: " + trace(e));
				return false;
			} catch (NoClassDefFoundError e) {
				delayedWarnings.add("Can't initialize javac processor due to (most likely) a class loader problem: " + trace(e));
				return false;
			}
			return true;
		}
		
		private ClassLoader findAndPatchClassLoader(ProcessingEnvironment procEnv) throws Exception {
			ClassLoader environmentClassLoader = procEnv.getClass().getClassLoader();
			if (environmentClassLoader != null && environmentClassLoader.getClass().getCanonicalName().equals("org.codehaus.plexus.compiler.javac.IsolatedClassLoader")) {
				if (!ClassLoader_lombokAlreadyAddedTo.getAndSet(environmentClassLoader, true)) {
					Method m = Permit.getMethod(environmentClassLoader.getClass(), "addURL", URL.class);
					URL selfUrl = new File(ClassRootFinder.findClassRootOfClass(AnnotationProcessor.class)).toURI().toURL();
					m.invoke(environmentClassLoader, selfUrl);
				}
			}
			
			ClassLoader ourClassLoader = JavacDescriptor.class.getClassLoader();
			if (ourClassLoader == null) return ClassLoader.getSystemClassLoader();
			return ourClassLoader;
		}
		
		@Override boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return processor.process(annotations, roundEnv);
		}
	}
	
	static class EcjDescriptor extends ProcessorDescriptor {
		@Override String getName() {
			return "ECJ";
		}
		
		@Override boolean want(ProcessingEnvironment procEnv, List<String> delayedWarnings) {
			if (!procEnv.getClass().getName().startsWith("org.eclipse.jdt.")) return false;
			
			// Lombok used to work as annotation processor to ecj but that never actually worked properly, so we disabled the feature in 0.10.0.
			// Because loading lombok as an agent in any ECJ-based non-interactive tool works just fine, we're not going to generate any warnings, as we'll
			// likely generate more false positives than be helpful.
			return true;
		}
		
		@Override boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return false;
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
		
		for (ProcessorDescriptor proc : active) proc.process(annotations, roundEnv);
		
		boolean onlyLombok = true;
		boolean zeroElems = true;
		for (TypeElement elem : annotations) {
			zeroElems = false;
			Name n = elem.getQualifiedName();
			if (n.toString().startsWith("lombok.")) continue;
			onlyLombok = false;
		}
		
		// Normally we rely on the claiming processor to claim away all lombok annotations.
		// One of the many Java9 oversights is that this 'process' API has not been fixed to address the point that 'files I want to look at' and 'annotations I want to claim' must be one and the same,
		// and yet in java9 you can no longer have 2 providers for the same service, thus, if you go by module path, lombok no longer loads the ClaimingProcessor.
		// This doesn't do as good a job, but it'll have to do. The only way to go from here, I think, is either 2 modules, or use reflection hackery to add ClaimingProcessor during our init.
		
		return onlyLombok && !zeroElems;
	}
	
	/**
	 * We just return the latest version of whatever JDK we run on. Stupid? Yeah, but it's either that or warnings on all versions but 1. Blame Joe.
	 */
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}
}
