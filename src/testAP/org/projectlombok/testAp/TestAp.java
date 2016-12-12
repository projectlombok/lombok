/*
 * Copyright (C) 2016 The Project Lombok Authors.
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
package org.projectlombok.testAp;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes("org.projectlombok.testAp.ExampleAnnotation")
public final class TestAp extends AbstractProcessor {
	private int roundCounter = 0;
	private static final long START = System.currentTimeMillis();
	
	private void log(String txt) {
		System.out.printf("***[%3d]: %s\n", System.currentTimeMillis() - START, txt);
	}
	
	@Override public void init(ProcessingEnvironment processingEnv) {
		log("TestAP in init");
		super.init(processingEnv);
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		roundCounter++;
		log("TestAP in round " + roundCounter);
		boolean foundGetTest = false;
		int annotatedElemCount = 0;
		for (Element annotated : roundEnv.getElementsAnnotatedWith(ExampleAnnotation.class)) {
			annotatedElemCount++;
			for (Element child : annotated.getEnclosedElements()) {
				if (child.getSimpleName().toString().equals("getTest") && child.getKind() == ElementKind.METHOD) foundGetTest = true;
				if (child instanceof ExecutableElement) {
					TypeMirror returnType = ((ExecutableElement) child).getReturnType();
					System.out.println("RETURN TYPE for " + child.getSimpleName() + ": " + returnType.getClass() + " -- " + returnType.toString());
				}
			}
		}
		
		if (foundGetTest) log("RESULT: POSITIVE -- found the getTest method");
		else if (annotatedElemCount > 0) log("RESULT: NEGATIVE -- found the example class but there's no getTest method in it according to the type mirror.");
		else log("RESULT: AMBIVALENT -- The example class is not provided by 'getElementsAnnotatedWith' in this round. Not an issue, unless previously you got a NEGATIVE result.");
		
		return false;
	}
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
