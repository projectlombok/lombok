/*
 * Copyright (C) 2014-2018 The Project Lombok Authors.
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

import java.lang.reflect.Field;
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

import sun.misc.Unsafe;

class AnnotationProcessorHider {

	public static class AstModificationNotifierData {
		public volatile static boolean lombokInvoked = false;
	}
	
	public static class AnnotationProcessor extends AbstractProcessor {
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
			disableJava9SillyWarning();
			AstModificationNotifierData.lombokInvoked = true;
			instance.init(processingEnv);
			super.init(processingEnv);
		}
		
		// sunapi suppresses javac's warning about using Unsafe; 'all' suppresses eclipse's warning about the unspecified 'sunapi' key. Leave them both.
		// Yes, javac's definition of the word 'all' is quite contrary to what the dictionary says it means. 'all' does NOT include 'sunapi' according to javac.
		@SuppressWarnings({"sunapi", "all"})
		private void disableJava9SillyWarning() {
			// JVM9 complains about using reflection to access packages from a module that aren't exported. This makes no sense; the whole point of reflection
			// is to get past such issues. The only comment from the jigsaw team lead on this was some unspecified mumbling about security which makes no sense,
			// as the SecurityManager is invoked to check such things. Therefore this warning is a bug, so we shall patch java to fix it.
			
			try {
				Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				Unsafe u = (Unsafe) theUnsafe.get(null);
				
				Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
				Field logger = cls.getDeclaredField("logger");
				u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
			} catch (Throwable t) {
				// We shall ignore it; the effect of this code failing is that the user gets to see a warning they remove with various --add-opens magic.
			}
		}
		
		@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			return instance.process(annotations, roundEnv);
		}
		
		@Override public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
			return instance.getCompletions(element, annotation, member, userText);
		}
		
		private static AbstractProcessor createWrappedInstance() {
			ClassLoader cl = Main.getShadowClassLoader();
			try {
				Class<?> mc = cl.loadClass("lombok.core.AnnotationProcessor");
				return (AbstractProcessor) mc.getDeclaredConstructor().newInstance();
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
