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

import static org.junit.Assert.fail;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.mapstruct.Mapper;

/**
 * Fails the test if mapstruct generated no implementation for a {@code @Mapper} type without reporting an error.
 * 
 * Comparing diagnostics alone is not enough: mapstruct 1.3.1 does nothing at all in the final round, so a mapper that
 * was deferred until then (because the binding kept reporting its types incomplete) is dropped silently, and a test
 * that only expects zero diagnostics would pass. Errors mapstruct did report are left to the diagnostics comparison,
 * which is the more useful failure.
 */
@SupportedAnnotationTypes("*")
public class ValidateMapStructImplProcessor extends AbstractProcessor {
	private final Set<String> expectedImpls = new TreeSet<String>();
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.processingOver()) {
			for (Element mapper : roundEnv.getElementsAnnotatedWith(Mapper.class)) {
				// mapstruct's default implementation name; nested mappers are not supported here.
				expectedImpls.add(((TypeElement) mapper).getQualifiedName() + "Impl");
			}
		} else if (!roundEnv.errorRaised()) {
			for (String impl : expectedImpls) {
				if (processingEnv.getElementUtils().getTypeElement(impl) == null) fail("mapstruct generated no " + impl + " and reported no error");
			}
		}
		return false;
	}
}
