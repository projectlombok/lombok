package com.sun.tools.javac.code;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

public abstract class Attribute {
	public static class Compound extends Attribute implements AnnotationMirror {
		public DeclaredType getAnnotationType() { return null; }
		public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() { return null; }
	}
}