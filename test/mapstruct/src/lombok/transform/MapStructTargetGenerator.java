/*
 * Copyright (C) 2026 The Project Lombok Authors.
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
package lombok.transform;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;

/**
 * Generates a lombok annotated type during the first round, so that lombok and mapstruct only get to see it in a later one.
 * Only fixtures in package {@code multiround} get it; the other fixtures must not depend on a generated file to trigger a second round.
 * 
 * Also points the filer at a directory under {@code build}, because {@code Delombok} sets no output locations and javac would
 * otherwise write everything generated here or by mapstruct into the working directory. This must therefore be the first
 * processor of the test that touches the filer.
 */
@SupportedAnnotationTypes("*")
public class MapStructTargetGenerator extends AbstractProcessor {
	private static final File GENERATED_SOURCES = new File("build/mapstruct-test-generated");
	private boolean generated;
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	@Override public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		// Lombok wraps javac's file manager, so the option route is the one that reaches it. Class output matters too: mapstruct writes a
		// resource there for mappers with a custom implementation name, and javac would put it into the working directory otherwise.
		JavaFileManager fileManager = ((JavacProcessingEnvironment) processingEnv).getContext().get(JavaFileManager.class);
		GENERATED_SOURCES.mkdirs();
		for (String option : new String[] {"-s", "-d"}) {
			if (!fileManager.handleOption(option, Collections.singleton(GENERATED_SOURCES.getPath()).iterator())) throw new IllegalStateException("file manager rejected " + option);
		}
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (generated || !inMultiRoundPackage(roundEnv)) return false;
		generated = true;
		try {
			Writer out = processingEnv.getFiler().createSourceFile("multiround.MapStructGeneratedTarget").openWriter();
			try {
				out.write("package multiround;\n@lombok.Value @lombok.experimental.SuperBuilder class MapStructGeneratedTarget {\n\tString title;\n}\n");
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	
	private boolean inMultiRoundPackage(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getRootElements()) {
			if (processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().contentEquals("multiround")) return true;
		}
		return false;
	}
}
