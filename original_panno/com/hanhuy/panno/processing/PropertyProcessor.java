/*
 * Copyright 2007 Perry Nguyen <pfnguyen@hanhuy.com> Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.hanhuy.panno.processing;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import com.hanhuy.panno.Property;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes( { "com.hanhuy.panno.Property" })
public class PropertyProcessor extends AbstractProcessor {
	
	private Messager messager;
	
	/**
	 * Annotation processing entry point. Currently, we only support javac 6
	 * from Sun's JDK. Other IDE support will have to follow.
	 */
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		com.sun.tools.javac.util.Context ctx = null;
		
		if ( processingEnv instanceof com.sun.tools.javac.processing.JavacProcessingEnvironment ) {
			ctx = ((com.sun.tools.javac.processing.JavacProcessingEnvironment)processingEnv).getContext();
		} else {
			messager.printMessage(Diagnostic.Kind.ERROR, "@Property processing is not supported on this compiler.");
		}
		
		Trees trees = Trees.instance(processingEnv);
		messager = processingEnv.getMessager();
		
		Set<? extends Element> annoElements = env.getElementsAnnotatedWith(Property.class);
		
		for ( Element e : annoElements ) {
			processAnnotation(trees, ctx, e, e.getAnnotation(Property.class));
		}
		return true;
	}
	
	private void processAnnotation(Trees trees, com.sun.tools.javac.util.Context ctx, Element e, Property p) {
		TypeElement type = (TypeElement)findEnclosingType(e);
		
		if ( type == null ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "@Property annotated element is not a member field.", e);
		}
		ClassTree cls = trees.getTree(type);
		com.sun.tools.javac.tree.JCTree.JCClassDecl cd = (com.sun.tools.javac.tree.JCTree.JCClassDecl)cls;
		Object t = trees.getTree(e);
		if ( !(t instanceof VariableTree) ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "@Property can only be annotated on a member field", e);
			return;
		}
		
		com.sun.tools.javac.util.Name.Table tab = com.sun.tools.javac.util.Name.Table.instance(ctx);
		
		com.sun.tools.javac.tree.TreeMaker maker = com.sun.tools.javac.tree.TreeMaker.instance(ctx);
		
		MethodTree m;
		String[] pcts = p.preCallThrows();
		String pcm = p.preCallMethod();
		if ( p.readOnly() && ((pcm != null && !"".equals(pcm.trim())) || (pcts != null && pcts.length > 0)) ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "preCallMethod and/or preCallThrows can"
					+ "not be used with readOnly", e, getAnnotationMirror(e));
		}
		if ( p.writeOnly() && p.useGet() ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "useGet is only for use with readable boolean properties", e,
					getAnnotationMirror(e));
		}
		if ( !p.readOnly() ) {
			m = createSetter(e, p, maker, tab, trees);
			cd.defs = cd.defs.append((com.sun.tools.javac.tree.JCTree)m);
		}
		if ( !p.writeOnly() ) {
			m = createGetter(e, p, maker, tab);
			cd.defs = cd.defs.append((com.sun.tools.javac.tree.JCTree)m);
		}
	}
	
	private String getPropertyName(Element e, Property p) {
		String pname = p.name();
		if ( pname == null || "".equals(p.name().trim()) ) pname = e.getSimpleName().toString();
		return pname;
	}
	
	@SuppressWarnings("unchecked")
	private MethodTree createGetter(Element e, Property p, com.sun.tools.javac.tree.TreeMaker maker,
			com.sun.tools.javac.util.Name.Table tab) {
		
		String pname = getPropertyName(e, p);
		
		com.sun.tools.javac.util.List nil = com.sun.tools.javac.util.List.nil();
		
		com.sun.tools.javac.util.List statements = com.sun.tools.javac.util.List.of(maker.Return(maker
				.Ident((com.sun.tools.javac.code.Symbol.VarSymbol)e)));
		
		String getterPrefix = "get";
		// check if boolean, use "is", otherwise "get" if useGet
		if ( e.asType().getKind() == TypeKind.BOOLEAN ) {
			if ( !p.useGet() ) getterPrefix = "is";
		} else {
			if ( p.useGet() ) messager.printMessage(Diagnostic.Kind.WARNING,
					"@Property(useGet=true) is for boolean properties", e);
		}
		
		com.sun.tools.javac.util.Name n = com.sun.tools.javac.util.Name.fromString(tab, getterPrefix + ucfirst(pname));
		
		MethodTree m = maker.MethodDef(maker.Modifiers(1L, nil), n, maker.Type((com.sun.tools.javac.code.Type)e
				.asType()), nil, nil, nil, maker.Block(0, statements), null);
		
		return m;
	}
	
	@SuppressWarnings("unchecked")
	private MethodTree createSetter(Element e, Property p, com.sun.tools.javac.tree.TreeMaker maker,
			com.sun.tools.javac.util.Name.Table tab, Trees trees) {
		
//		String pname = getPropertyName(e, p);
		int pos = getAnnotationPosition(e, trees);
		
		com.sun.tools.javac.util.List nil = com.sun.tools.javac.util.List.nil();
		
		com.sun.tools.javac.util.Name vn = com.sun.tools.javac.util.Name.fromString(tab, "__panno_Generated_"
				+ e.getSimpleName().toString());
		
		com.sun.tools.javac.tree.JCTree.JCVariableDecl param = maker.Param(vn, (com.sun.tools.javac.code.Type)e
				.asType(), null);
		
		com.sun.tools.javac.util.List<com.sun.tools.javac.tree.JCTree.JCStatement> statements = com.sun.tools.javac.util.List
				.of(maker.Assignment((com.sun.tools.javac.code.Symbol.VarSymbol)e, maker.Ident(param)));
		
//		com.sun.tools.javac.util.Name n = com.sun.tools.javac.util.Name.fromString(tab, "set" + ucfirst(pname));
		
		com.sun.tools.javac.util.List throwList = nil;
		MethodInvocationTree mit = createPreCall(e, p, param, maker, tab, pos);
		
		if ( mit != null ) {
			statements = statements.prepend(maker.Exec((com.sun.tools.javac.tree.JCTree.JCMethodInvocation)mit));
			if ( p.preCallThrows() != null && p.preCallThrows().length > 0 ) {
				for ( String thrown : p.preCallThrows() ) {
					com.sun.tools.javac.util.Name tn = com.sun.tools.javac.util.Name.fromString(tab, thrown);
					
					com.sun.tools.javac.code.Symbol.ClassSymbol clssym = new com.sun.tools.javac.code.Symbol.ClassSymbol(
							0, tn, null);
					throwList = throwList.append(maker.Type(clssym.type).setPos(pos));
					// setpos to munge line number
				}
			}
			
		} else if ( p.preCallThrows() != null && p.preCallThrows().length > 0 ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "preCallThrows is only to be used with preCallMethod", e,
					getAnnotationMirror(e));
		}
		
		com.sun.tools.javac.tree.JCTree.JCMethodDecl m = null/*maker.MethodDef(maker.Modifiers(1L, nil), n, maker
				.Type(com.sun.tools.javac.code.Symtab.voidType), nil, com.sun.tools.javac.util.List.of(param),
				throwList, maker.Block(0, statements), null)*/;
		
		// munge line numbers
//		m.mods.pos = pos;
//		m.pos = pos;
		return m;
	}
	
	private MethodInvocationTree createPreCall(Element e, Property p,
			com.sun.tools.javac.tree.JCTree.JCVariableDecl newParam, com.sun.tools.javac.tree.TreeMaker maker,
			com.sun.tools.javac.util.Name.Table tab, int pos) {
		String pcm = p.preCallMethod();
		String pname = getPropertyName(e, p);
		pname = lcfirst(pname);
		
		com.sun.tools.javac.tree.JCTree.JCMethodInvocation m = null;
		if ( pcm == null || "".equals(pcm) ) return m;
		String[] parts = pcm.split("\\.");
		com.sun.tools.javac.tree.JCTree.JCExpression expr = null;
		Element enclosing = findEnclosingType(e);
		
		String lastPart = null;
		if ( parts.length > 1 ) {
			// A simple object navigation path is named.
			// I'm too lazy to support/figure out method invocations here,
			// do plain member selection only
			for ( int i = 0, j = parts.length; i < j; i++ ) {
				if ( parts[i].endsWith(")") ) {
					messager.printMessage(Diagnostic.Kind.ERROR, "Cannot call a method in graph to preCallMethod", e,
							getAnnotationMirror(e));
				}
				
				com.sun.tools.javac.util.Name partName = com.sun.tools.javac.util.Name.fromString(tab, parts[i]);
				
				if ( lastPart != null ) {
					
					expr = maker.Select(expr, partName);
					
				} else {
					
					com.sun.tools.javac.tree.JCTree.JCVariableDecl param = maker.Param(partName, null, null);
					expr = maker.Ident(param);
				}
				// this is how we get the correct line number
				expr.pos = pos;
				
				lastPart = parts[i];
			}
		} else {
			// A method is named
//			com.sun.tools.javac.util.Name name = com.sun.tools.javac.util.Name.fromString(tab, parts[0]);
			
			com.sun.tools.javac.code.Symbol.MethodSymbol msym = null/*new com.sun.tools.javac.code.Symbol.MethodSymbol(0,
					name, com.sun.tools.javac.code.Symtab.voidType, (com.sun.tools.javac.code.Symbol)enclosing)*/;
			
			expr = maker.Ident(msym);
			// this is how we get the correct line number when there's an error.
			expr.pos = pos;
		}
		
		m = maker.Apply(null, expr, com.sun.tools.javac.util.List.of(maker
				.This((com.sun.tools.javac.code.Type)enclosing.asType()), maker.Literal(pname), maker
				.Ident((com.sun.tools.javac.code.Symbol)e), maker.Ident(newParam)));
		
		// this is required, I guess...
		m.setType(new com.sun.tools.javac.code.Type.MethodType(null, null, null, null));
		
		return m;
	}
	
	private int getAnnotationPosition(Element e, Trees trees) {
		return (int)trees.getSourcePositions().getStartPosition(trees.getPath(e).getCompilationUnit(),
				trees.getTree(e, getAnnotationMirror(e)));
	}
	
	/**
	 * Returns the enclosing type if found. (Should return a TypeElement object)
	 */
	static Element findEnclosingType(Element e) {
		return e == null || e instanceof TypeElement ? e : e.getEnclosingElement();
	}
	
	/**
	 * Uppercase the first character of the string. Named after the ucfirst
	 * function in Perl.
	 */
	static String ucfirst(String name) {
		String first = name.substring(0, 1).toUpperCase();
		return first + name.substring(1);
	}
	
	static String lcfirst(String name) {
		String first = name.substring(0, 1).toLowerCase();
		return first + name.substring(1);
	}
	
	/**
	 * Return the Property AnnotationMirror for this element.
	 */
	static AnnotationMirror getAnnotationMirror(Element e) {
		List<? extends AnnotationMirror> annos = e.getAnnotationMirrors();
		for ( AnnotationMirror am : annos ) {
			DeclaredType type = am.getAnnotationType();
			String name = type.asElement().getSimpleName().toString();
			if ( name != null && "Property".equals(name.trim()) ) return am;
		}
		throw new IllegalStateException("Element must have a @Property annotation");
	}
}
