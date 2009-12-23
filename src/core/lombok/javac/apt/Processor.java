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
package lombok.javac.apt;

import java.util.IdentityHashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import lombok.javac.JavacTransformer;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;


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
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	private JavacProcessingEnvironment processingEnv;
	private JavacTransformer transformer;
	private Trees trees;
	
	/** {@inheritDoc} */
	@Override public void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.processingEnv = (JavacProcessingEnvironment) procEnv;
		transformer = new JavacTransformer(procEnv.getMessager());
		trees = Trees.instance(procEnv);
	}
	
	/** {@inheritDoc} */
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		IdentityHashMap<JCCompilationUnit, Void> units = new IdentityHashMap<JCCompilationUnit, Void>();
		for (Element element : roundEnv.getRootElements()) {
			JCCompilationUnit unit = toUnit(element);
			if (unit != null) units.put(unit, null);
		}
		
		transformer.transform(processingEnv.getContext(), units.keySet());
		
		return false;
	}
	
	private JCCompilationUnit toUnit(Element element) {
		TreePath path = trees == null ? null : trees.getPath(element);
		if (path == null) return null;
		
		return (JCCompilationUnit) path.getCompilationUnit();
	}
}
