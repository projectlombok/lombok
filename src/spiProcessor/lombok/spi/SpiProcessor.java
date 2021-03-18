/*
 * Copyright (C) 2021 The Project Lombok Authors.
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
package lombok.spi;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("*")
public class SpiProcessor extends AbstractProcessor {
	private SpiProcessorCollector data;
	private SpiProcessorPersistence persistence;
	
	static String getRootPathOfServiceFiles() {
		return "META-INF/services/";
	}
	
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
	
	static String toErrorMsg(Exception e, String pathName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Exception applying SPI processor on ").append(pathName).append(": ").append(toStringLong(e));
		return sb.toString();
	}
	
	private static String toStringLong(Throwable t) {
		if (t == null) return "NULL";
		StringBuilder out = new StringBuilder();
		
		String msg = t.getMessage();
		out.append(t.getClass().getName());
		if (msg != null) out.append(": ").append(msg);
		String indent = "  ";
		
		StackTraceElement[] elems = t.getStackTrace();
		if (elems != null) for (int i = 0; i < elems.length; i++) {
			out.append("\n").append(indent).append(elems[i]);
		}
		Throwable cause = t.getCause();
		while (cause != null) {
			indent = indent + " ";
			out.append("\n").append(indent).append("Caused by: ").append(cause.getClass().getName());
			msg = cause.getMessage();
			if (msg != null) out.append(": ").append(msg);
			elems = cause.getStackTrace();
			indent = indent + " ";
			if (elems != null) for (int i = 0; i < elems.length; i++) {
				out.append("\n").append(indent).append(elems[i]);
			}
			cause = cause.getCause();
		}
		return out.toString();
	}
	
	@Override public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		persistence = new SpiProcessorPersistence("SpiProcessor", processingEnv.getFiler(), processingEnv.getMessager());
		data = new SpiProcessorCollector(processingEnv);
		for (String serviceName : persistence.tryFind()) data.getService(serviceName);
		data.stripProvidersWithoutSourceFile();
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		removeStaleData(roundEnv);
		handleAnnotations(roundEnv);
		if (roundEnv.processingOver()) writeData();
		
		return false;
	}
	
	private void writeData() {
		for (SpiProcessorService service : data.services()) {
			try {
				persistence.write(service.getName(), service.toProvidersListFormat());
			} 
			catch (IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
			}
		}
	}
	
	private void removeStaleData(RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getRootElements()) {
			if (e instanceof TypeElement) {
				TypeElement currentClass = (TypeElement) e;
				data.removeProvider(createProperQualifiedName(currentClass));
			}
		}
	}
	
	private void handleAnnotations(RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Provides.class);
		for (Element e : elements) handleProvidingElement(e);
	}
	
	private void handleProvidingElement(Element element) {
		if (element.getKind() != ElementKind.CLASS) {
			report(element, "is not a class definition");
			return;
		}
		
		TypeElement elem = (TypeElement) element;
		
		Element enclosing = elem.getEnclosingElement();
		if (enclosing != null && enclosing.getKind() == ElementKind.CLASS && !elem.getModifiers().contains(Modifier.STATIC)) {
			report(elem, "is a non-static inner class");
			return;
		}
		
		boolean hasConstructors = false;
		boolean hasNoArgsConstructor = false;
		for (Element child : elem.getEnclosedElements()) {
			if (child.getKind() != ElementKind.CONSTRUCTOR) continue;
			ExecutableElement ee = (ExecutableElement) child;
			hasConstructors = true;
			if (ee.getParameters().isEmpty()) {
				hasNoArgsConstructor = true;
				break;
			}
		}
		
		if (hasConstructors && !hasNoArgsConstructor) {
			report(elem, "has no no-args constructor");
			return;
		}
		
		List<TypeMirror> spiTypes = new ArrayList<TypeMirror>();
		
		for (AnnotationMirror annMirror : element.getAnnotationMirrors()) {
			if (!getQualifiedTypeName(annMirror).contentEquals(Provides.class.getName())) continue;
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annMirror.getElementValues().entrySet()) {
				if (!entry.getKey().getSimpleName().contentEquals("value")) continue;
				Object list = entry.getValue().getValue();
				if (list instanceof TypeMirror) spiTypes.add((TypeMirror) list);
				else if (list instanceof List<?>) {
					for (Object tm : (List<?>) list) {
						if (tm instanceof AnnotationValue) tm = ((AnnotationValue) tm).getValue();
						if (tm instanceof TypeMirror) {
							TypeMirror mirror = (TypeMirror) tm;
							mirror = processingEnv.getTypeUtils().erasure(mirror);
							spiTypes.add(mirror);
						}
					}
				}
			}
		}
		
		TypeMirror superclass = elem.getSuperclass();
		if (spiTypes.isEmpty()) {
			List<TypeMirror> qualifying = new ArrayList<TypeMirror>();
			qualifying.addAll(elem.getInterfaces());
			if (superclass != null && !toElement(superclass).getQualifiedName().contentEquals("java.lang.Object")) qualifying.add(superclass);
			
			if (qualifying.isEmpty()) {
				report(elem, "is marked @Provides but implements/extends nothing");
				return;
			}
			if (qualifying.size() > 1) {
				report(elem, "is marked @Provides but implements/extends multiple types; explicitly specify which interface(s) this provides for");
				return;
			}
			spiTypes.add(qualifying.get(0));
		} else {
			Deque<TypeMirror> parentage = new ArrayDeque<TypeMirror>();
			parentage.addAll(elem.getInterfaces());
			parentage.add(superclass);
			List<TypeMirror> needed = new ArrayList<TypeMirror>();
			needed.addAll(spiTypes);
			while (!parentage.isEmpty() && !spiTypes.isEmpty()) {
				TypeMirror parent = parentage.pollFirst();
				if (parent == null) continue;
				needed.remove(parent);
				parentage.addAll(processingEnv.getTypeUtils().directSupertypes(parent));
			}
			if (!needed.isEmpty()) {
				report(elem, "is marked as providing " + needed + " but does not implement it");
				return;
			}
		}
		
		for (TypeMirror spiType : spiTypes) {
			String spiTypeName = createProperQualifiedName(toElement(spiType));
			String createProperQualifiedName = createProperQualifiedName(elem);
			data.getService(spiTypeName).addProvider(createProperQualifiedName);
		}
	}
	
	private Name getQualifiedTypeName(AnnotationMirror mirror) {
		Element elem = mirror.getAnnotationType().asElement();
		if (!(elem instanceof TypeElement)) return null;
		return ((TypeElement) elem).getQualifiedName();
	}
	
	private TypeElement toElement(TypeMirror typeMirror) {
		if (typeMirror instanceof DeclaredType) {
			Element asElement = ((DeclaredType) typeMirror).asElement();
			if (asElement instanceof TypeElement) return (TypeElement) asElement;
		}
		return null;
	}
	
	private void report(Element elem, String message) {
		/* In eclipse, messages just seem to get ignored, so we throw instead. */
		if (Boolean.TRUE) throw new RuntimeException(elem.getSimpleName() + " " + message);
		processingEnv.getMessager().printMessage(Kind.ERROR, elem.getSimpleName() + " " + message, elem);
	}
	
	private String createProperQualifiedName(TypeElement provider) {
		return processingEnv.getElementUtils().getBinaryName(provider).toString();
	}
}
