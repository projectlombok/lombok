/*
 * Copyright (C) 2010 The Project Lombok Authors.
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
package lombok.javac;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Name;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;

import com.sun.tools.javac.code.Type;

/**
 * scanner (i.e. visits child nodes all the way to the leaves) that accumulates type variables. Call {@code visit} on any {@code TypeMirror} object with an instance
 * to add all used type variable names such as {@code T} or {@code E} to the set that is returned by the {@link #getTypeVariables} method.
 */
public class FindTypeVarScanner extends AbstractTypeVisitor6<Void, Void> {
	private Set<String> typeVariables = new HashSet<String>();
	
	public Set<String> getTypeVariables() {
		return typeVariables;
	}
	
	private Void subVisit(TypeMirror mirror) {
		if (mirror == null) return null;
		return mirror.accept(this, null);
	}
	
	@Override public Void visitPrimitive(PrimitiveType t, Void p) {
		return null;
	}
	
	@Override public Void visitNull(NullType t, Void p) {
		return null;
	}
	
	
	@Override public Void visitNoType(NoType t, Void p) {
		return null;
	}
	
	@Override public Void visitUnknown(TypeMirror t, Void p) {
		return null;
	}
	
	@Override public Void visitError(ErrorType t, Void p) {
		return null;
	}
	
	@Override public Void visitArray(ArrayType t, Void p) {
		return subVisit(t.getComponentType());
	}
	
	@Override public Void visitDeclared(DeclaredType t, Void p) {
		for (TypeMirror subT : t.getTypeArguments()) subVisit(subT);
		return null;
	}
	
	@Override public Void visitTypeVariable(TypeVariable t, Void p) {
		Name name = null;
		try {
			name = ((Type) t).tsym.name;
		} catch (NullPointerException e) {}
		if (name != null) typeVariables.add(name.toString());
		subVisit(t.getLowerBound());
		subVisit(t.getUpperBound());
		return null;
	}
	
	@Override public Void visitWildcard(WildcardType t, Void p) {
		subVisit(t.getSuperBound());
		subVisit(t.getExtendsBound());
		return null;
	}
	
	@Override public Void visitExecutable(ExecutableType t, Void p) {
		subVisit(t.getReturnType());
		for (TypeMirror subT : t.getParameterTypes()) subVisit(subT);
		for (TypeMirror subT : t.getThrownTypes()) subVisit(subT);
		for (TypeVariable subT : t.getTypeVariables()) subVisit(subT);
		return null;
	}
}
