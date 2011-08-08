/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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
package lombok.javac.apt;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import lombok.Lombok;
import lombok.core.DiagnosticsReceiver;
import lombok.javac.JavacTransformer;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * This Annotation Processor is the standard injection mechanism for lombok-enabling the javac compiler.
 * 
 * Due to lots of changes in the core javac code, as well as lombok's heavy usage of non-public API, this
 * code only works for the javac v1.6 compiler; it definitely won't work for javac v1.5, and it probably
 * won't work for javac v1.7 without modifications.
 * 
 * To actually enable lombok in a javac compilation run, this class should be in the classpath when
 * running javac; that's the only requirement.
 */
public class Processor extends AbstractProcessor {

	private JavacProcessingEnvironment processingEnv;
	private JavacTransformer transformer;
	private Trees trees;
	
	/** {@inheritDoc} */
	@Override public void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.processingEnv = (JavacProcessingEnvironment) procEnv;
		placePostCompileAndDontMakeForceRoundDummiesHook();
		transformer = new JavacTransformer(procEnv.getMessager());
		trees = Trees.instance(procEnv);
	}
	
	private void placePostCompileAndDontMakeForceRoundDummiesHook() {
		stopJavacProcessingEnvironmentFromClosingOurClassloader();
		
		forceMultipleRoundsInNetBeansEditor();
		Context context = processingEnv.getContext();
		disablePartialReparseInNetBeansEditor(context);
		try {
			Method keyMethod = Context.class.getDeclaredMethod("key", Class.class);
			keyMethod.setAccessible(true);
			Object key = keyMethod.invoke(context, JavaFileManager.class);
			Field htField = Context.class.getDeclaredField("ht");
			htField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<Object,Object> ht = (Map<Object,Object>) htField.get(context);
			final JavaFileManager originalFiler = (JavaFileManager) ht.get(key);
			
			if (!(originalFiler instanceof InterceptingJavaFileManager)) {
				final Messager messager = processingEnv.getMessager();
				DiagnosticsReceiver receiver = new MessagerDiagnosticsReceiver(messager);
				
				JavaFileManager newFiler = new InterceptingJavaFileManager(originalFiler, receiver);
				ht.put(key, newFiler);
				Field filerFileManagerField = JavacFiler.class.getDeclaredField("fileManager");
				filerFileManagerField.setAccessible(true);
				filerFileManagerField.set(processingEnv.getFiler(), newFiler);
			}
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	private void forceMultipleRoundsInNetBeansEditor() {
		try {
			Field f = JavacProcessingEnvironment.class.getDeclaredField("isBackgroundCompilation");
			f.setAccessible(true);
			f.set(processingEnv, true);
		} catch (NoSuchFieldException e) {
			// only NetBeans has it
		} catch (Throwable t) {
			throw Lombok.sneakyThrow(t);
		}
	}
	
	private void disablePartialReparseInNetBeansEditor(Context context) {
		try {
			Class<?> cancelServiceClass = Class.forName("com.sun.tools.javac.util.CancelService");
			Method cancelServiceInstace = cancelServiceClass.getDeclaredMethod("instance", Context.class);
			Object cancelService = cancelServiceInstace.invoke(null, context);
			if (cancelService == null) return;
			Field parserField = cancelService.getClass().getDeclaredField("parser");
			parserField.setAccessible(true);
			Object parser = parserField.get(cancelService);
			Field supportsReparseField = parser.getClass().getDeclaredField("supportsReparse");
			supportsReparseField.setAccessible(true);
			supportsReparseField.set(parser, false);
		} catch (ClassNotFoundException e) {
			// only NetBeans has it
		} catch (NoSuchFieldException e) {
			// only NetBeans has it
		} catch (Throwable t) {
			throw Lombok.sneakyThrow(t);
		}
	}
	
	private static class WrappingClassLoader extends ClassLoader {
		private final ClassLoader parent;
		
		public WrappingClassLoader(ClassLoader parent) {
			this.parent = parent;
		}
		
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return parent.loadClass(name);
		}
		
		public String toString() {
			return parent.toString();
		}
		
		public URL getResource(String name) {
			return parent.getResource(name);
		}
		
		public Enumeration<URL> getResources(String name) throws IOException {
			return parent.getResources(name);
		}
		
		public InputStream getResourceAsStream(String name) {
			return parent.getResourceAsStream(name);
		}
		
		public void setDefaultAssertionStatus(boolean enabled) {
			parent.setDefaultAssertionStatus(enabled);
		}
		
		public void setPackageAssertionStatus(String packageName, boolean enabled) {
			parent.setPackageAssertionStatus(packageName, enabled);
		}
		
		public void setClassAssertionStatus(String className, boolean enabled) {
			parent.setClassAssertionStatus(className, enabled);
		}
		
		public void clearAssertionStatus() {
			parent.clearAssertionStatus();
		}
	}
	
	private void stopJavacProcessingEnvironmentFromClosingOurClassloader() {
		try {
			Field f = JavacProcessingEnvironment.class.getDeclaredField("processorClassLoader");
			f.setAccessible(true);
			ClassLoader unwrapped = (ClassLoader) f.get(processingEnv);
			ClassLoader wrapped = new WrappingClassLoader(unwrapped);
			f.set(processingEnv, wrapped);
		} catch (NoSuchFieldException e) {
			// Some versions of javac have this (and call close on it), some don't. I guess this one doesn't have it.
		} catch (Throwable t) {
			throw Lombok.sneakyThrow(t);
		}
	}
	
	private final IdentityHashMap<JCCompilationUnit, Void> rootsAtPhase0 = new IdentityHashMap<JCCompilationUnit, Void>();
	private final IdentityHashMap<JCCompilationUnit, Void> rootsAtPhase1 = new IdentityHashMap<JCCompilationUnit, Void>();
	private int dummyCount = 0;
	
	/** {@inheritDoc} */
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) return false;
		
		if (!rootsAtPhase0.isEmpty()) {
			ArrayList<JCCompilationUnit> cus = new ArrayList<JCCompilationUnit>(rootsAtPhase0.keySet());
			transformer.transform(true, processingEnv.getContext(), cus);
			rootsAtPhase1.putAll(rootsAtPhase0);
			rootsAtPhase0.clear();
		}
		
		for (Element element : roundEnv.getRootElements()) {
			JCCompilationUnit unit = toUnit(element);
			if (unit != null) {
				if (!rootsAtPhase1.containsKey(unit)) rootsAtPhase0.put(unit, null);
			}
		}
		
		if (!rootsAtPhase0.isEmpty()) {
			ArrayList<JCCompilationUnit> cus = new ArrayList<JCCompilationUnit>(rootsAtPhase0.keySet());
			transformer.transform(false, processingEnv.getContext(), cus);
			JavacFiler filer = (JavacFiler) processingEnv.getFiler();
			if (!filer.newFiles()) {
				try {
					JavaFileObject dummy = filer.createSourceFile("lombok.dummy.ForceNewRound" + (dummyCount++));
					Writer w = dummy.openWriter();
					w.close();
				} catch (Exception e) {
					processingEnv.getMessager().printMessage(Kind.WARNING,
							"Can't force a new processing round. Lombok features that require resolution won't work.");
				}
			}
		}
		
		return false;
	}
	
	private JCCompilationUnit toUnit(Element element) {
		TreePath path = trees == null ? null : trees.getPath(element);
		if (path == null) return null;
		
		return (JCCompilationUnit) path.getCompilationUnit();
	}
}
