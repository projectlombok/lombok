/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.code;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public abstract class Symbol implements Element {
	public Type type;
	public Name name;
	public Symbol owner;
	public Type erasure_field;

	public long flags() { return 0; }
	public boolean isStatic() { return false; }
	public boolean isConstructor() { return false; }
	public boolean isLocal() { return false; }
	public Name flatName() { return null; }
	public Name getQualifiedName() { return null; }
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annoType) { return null; }
	@Override public java.util.List<Attribute.Compound> getAnnotationMirrors() { return null; }
	@Override public TypeMirror asType() { return null; }
	public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) { return null; }
	@Override public Name getSimpleName() { return null; }
	@Override public java.util.List<Symbol> getEnclosedElements() { return null; }
	@Override public Element getEnclosingElement() { return null; }
	public void appendAttributes(List<Attribute.Compound> l) {
	}

	public static abstract class TypeSymbol extends Symbol {}
	
	public static class MethodSymbol extends Symbol implements ExecutableElement {
		public List<Symbol.VarSymbol> params = null;
		public MethodSymbol(long flags, Name name, Type type, Symbol owner) {}
		@Override public ElementKind getKind() { return null; }
		@Override public Set<Modifier> getModifiers() { return null; }
		@Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { return null; }
		@Override public java.util.List<? extends TypeParameterElement> getTypeParameters() { return null; }
		@Override public TypeMirror getReturnType() { return null; }
		@Override public java.util.List<? extends VariableElement> getParameters() { return null; }
		@Override public boolean isVarArgs() { return false; }
		@Override public java.util.List<? extends TypeMirror> getThrownTypes() { return null; }
		@Override public AnnotationValue getDefaultValue() { return null; }
		public TypeMirror getReceiverType() { return null; }
		public boolean isDefault() { return false; }
		public com.sun.tools.javac.util.List<VarSymbol> params() { return null; }
	}
	
	public static class VarSymbol extends Symbol implements VariableElement {
		public Type type;
		public int adr;
		public VarSymbol(long flags, Name name, Type type, Symbol owner) {
		}
		@Override public ElementKind getKind() { return null; }
		@Override public Set<Modifier> getModifiers() { return null; }
		@Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { return null; }
		@Override public Object getConstantValue() { return null; }
	}
	
	public static class ClassSymbol extends TypeSymbol implements TypeElement {
		@Override public Name getQualifiedName() { return null; }
		@Override public java.util.List<? extends TypeMirror> getInterfaces() { return null; }
		@Override public TypeMirror getSuperclass() { return null; }
		@Override public ElementKind getKind() { return null; }
		@Override public Set<Modifier> getModifiers() { return null; }
		@Override public NestingKind getNestingKind() { return null; }
		@Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { return null; }
		@Override public java.util.List<? extends TypeParameterElement> getTypeParameters() { return null; }
	}
	
	// JDK9
	public static class ModuleSymbol extends TypeSymbol {
		@Override public ElementKind getKind() { return null; }
		@Override public Set<Modifier> getModifiers() { return null; }
		@Override public <R, P> R accept(ElementVisitor<R, P> v, P p) { return null; }
	}
}
