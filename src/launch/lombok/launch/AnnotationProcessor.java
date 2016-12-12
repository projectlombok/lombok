/*
 * Copyright (C) 2014-2016 The Project Lombok Authors.
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
package lombok.launch;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

class AnnotationProcessorHider {
	public static class AnnotationProcessor extends AbstractProcessor {
		private static final long START = System.currentTimeMillis();
		
		private void log(String txt) {
			System.out.printf("***[%3d]: %s\n", System.currentTimeMillis() - START, txt);
		}
		private final AbstractProcessor instance = createWrappedInstance();
		
		@Override public Set<String> getSupportedOptions() {
			return instance.getSupportedOptions();
		}
		
		@Override public Set<String> getSupportedAnnotationTypes() {
			return instance.getSupportedAnnotationTypes();
		}
		
		@Override public SourceVersion getSupportedSourceVersion() {
			return instance.getSupportedSourceVersion();
		}
		
		@Override public void init(ProcessingEnvironment processingEnv) {
			log("Lombok in init");
			instance.init(processingEnv);
			super.init(processingEnv);
		}
		
		private int roundCounter = 0;
		@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			roundCounter++;
			log("Lombok in round " + roundCounter);
			return instance.process(annotations, roundEnv);
		}
		
		@Override public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
			return instance.getCompletions(element, annotation, member, userText);
		}
		
		private static AbstractProcessor createWrappedInstance() {
			ClassLoader cl = Main.createShadowClassLoader();
			try {
				Class<?> mc = cl.loadClass("lombok.core.AnnotationProcessor");
				return (AbstractProcessor) mc.newInstance();
			} catch (Throwable t) {
				if (t instanceof Error) throw (Error) t;
				if (t instanceof RuntimeException) throw (RuntimeException) t;
				throw new RuntimeException(t);
			}
		}
	}
	
	@SupportedAnnotationTypes("lombok.*")
	public static class ClaimingProcessor extends AbstractProcessor {
		@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return true;
		}
		
		@Override public SourceVersion getSupportedSourceVersion() {
			return SourceVersion.latest();
		}
	}
}
