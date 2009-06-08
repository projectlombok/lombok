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

import java.util.Map;
import java.util.Stack;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

import com.hanhuy.panno.Property;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;

/**
 * Old attempt to get the annotation working, have a better approach now.
 */
@Deprecated
class ASTTreeToSource extends TreeScanner<Void, StringBuilder> {
	public final static String PROPERTY_ANNOTATION_FULL = "com.hanhuy.panno.Property";
	public final static String PROPERTY_ANNOTATION = "Property";
	
	private final Stack<String> currentClassNameStack = new Stack<String>();
	private final Messager messager;
	private final Map<String, Property> fieldMap;
	private String currentClassName;
	private String currentField;
	private String currentType;
	
	ASTTreeToSource(Map<String, Property> fieldMap, Messager messager) {
		this.fieldMap = fieldMap;
		this.messager = messager;
	}
	
	/**
	 * Handle initializer blocks, both static and non.
	 */
	@Override
	public Void visitBlock(BlockTree node, StringBuilder p) {
		p.append("\n");
		if ( node.isStatic() ) p.append("static ");
		p.append("{\n");
		for ( StatementTree st : node.getStatements() ) {
			p.append(st);
			p.append(";");
		}
		p.append("\n}\n");
		return null;
	}
	
	/**
	 * Detect our annotation and add accessors if necessary.
	 */
	@Override
	public Void visitAnnotation(AnnotationTree node, StringBuilder p) {
		String anno = ((IdentifierTree)node.getAnnotationType()).getName().toString();
		if ( anno != null && PROPERTY_ANNOTATION.equals(anno.trim()) ) {
			Property pAnno = fieldMap.get(currentClassName + "." + currentField);
			
			String propName = currentField;
			if ( !"".equals(pAnno.name()) ) propName = pAnno.name();
			propName = PropertyProcessor.ucfirst(propName);
			
			if ( pAnno.writeOnly() && pAnno.readOnly() ) {
				messager.printMessage(Diagnostic.Kind.ERROR, "@Property's writeOnly and readOnly may not both be true");
			}
			
			if ( !"boolean".equals(currentType) && pAnno.useGet() ) {
				messager.printMessage(Diagnostic.Kind.WARNING, "@Property's useGet is only for booleans");
			}
			
			if ( !pAnno.writeOnly() ) {
				// generate a getter
				p.append("\n");
				p.append("public ");
				p.append(currentType);
				if ( !"boolean".equals(currentType) || pAnno.useGet() ) p.append(" get");
				else p.append(" is");
				p.append(propName);
				p.append("() { return ");
				p.append(currentField);
				p.append("; }");
			}
			if ( !pAnno.readOnly() ) {
				// generate a setter
				p.append("\n");
				p.append("public void set");
				p.append(propName);
				p.append("(");
				p.append(currentType);
				p.append(" ");
				p.append(currentField);
				p.append(") { this.");
				p.append(currentField);
				p.append(" = ");
				p.append(currentField);
				p.append("; }");
			}
		}
		return null;
	}
	
	/**
	 * Gather file information. Then continue to visit classes.
	 */
	@Override
	public Void visitCompilationUnit(CompilationUnitTree node, StringBuilder p) {
		new Exception("Visit CUT: " + node.getSourceFile()).printStackTrace();
		p.append("// Generated from: ");
		p.append(node.getSourceFile());
		p.append("\n\npackage ");
		p.append(node.getPackageName());
		p.append(";\n\n");
		
		for ( ImportTree it : node.getImports() ) {
			if ( PROPERTY_ANNOTATION_FULL.equals(it.getQualifiedIdentifier().toString().trim()) ) {
				continue;
			}
			p.append("import ");
			if ( it.isStatic() ) p.append("static ");
			p.append(it.getQualifiedIdentifier());
			p.append(";\n");
		}
		super.visitCompilationUnit(node, p);
		return null;
	}
	
	private void pushClass(String className) {
		currentClassName = className;
		currentClassNameStack.push(currentClassName);
	}
	
	private void popClass() {
		currentClassNameStack.pop();
		if ( currentClassNameStack.size() > 0 ) currentClassName = currentClassNameStack.peek();
	}
	
	/**
	 * Reproduce class declarations, and go on to handle methods, blocks and
	 * variables.
	 */
	@Override
	public Void visitClass(ClassTree node, StringBuilder p) {
		pushClass(node.getSimpleName().toString());
		
		// Check to see if this is an enum
		// I have no idea what will happen if we encounter an AST
		// implementation that is not Sun's (or if this has changed in java7)
		if ( node instanceof com.sun.tools.javac.tree.JCTree.JCClassDecl ) {
			com.sun.tools.javac.tree.JCTree.JCClassDecl jcd = (com.sun.tools.javac.tree.JCTree.JCClassDecl)node;
			if ( (jcd.mods.flags & 16384L) != 0L ) {
				p.append(node.getModifiers());
				p.append(" enum ");
				p.append(currentClassName);
				appendEnumBody(jcd.defs, p);
				
				popClass();
				
				return null;
			}
		}
		
		p.append("\n");
		p.append(node.getModifiers());
		p.append(" class ");
		p.append(currentClassName);
		if ( node.getTypeParameters().size() > 0 ) {
			p.append("<");
			p.append(node.getTypeParameters());
			p.append(">");
		}
		if ( node.getExtendsClause() != null ) {
			p.append("\nextends ");
			p.append(node.getExtendsClause());
		}
		if ( node.getImplementsClause().size() > 0 ) {
			p.append("\nimplements ");
			p.append(node.getImplementsClause());
		}
		
		p.append("\n{");
		super.visitClass(node, p);
		p.append("\n}\n");
		
		popClass();
		
		return null;
	}
	
	/**
	 * Enums appear to be a special case. We must reconstitute its source in a
	 * different manner from the rest. Need to remove any 'super()' statements
	 * and print the lines appropriately.
	 */
	public void appendEnumBody(com.sun.tools.javac.util.List<com.sun.tools.javac.tree.JCTree> list, StringBuilder p) {
		p.append("{\n");
		boolean flag = true;
		
		for ( com.sun.tools.javac.util.List<com.sun.tools.javac.tree.JCTree> list1 = list; list1.nonEmpty(); list1 = list1.tail ) {
			
			if ( !isEnumerator(list1.head) ) continue;
			
			if ( !flag ) p.append(",\n");
			
			p.append(list1.head);
			flag = false;
		}
		
		p.append(";\n");
		for ( com.sun.tools.javac.util.List<com.sun.tools.javac.tree.JCTree> list2 = list; list2.nonEmpty(); list2 = list2.tail ) {
			if ( !isEnumerator(list2.head) ) {
				if ( list2.head instanceof MethodTree ) visitMethod((MethodTree)list2.head, p);
				else p.append(list2.head);
				p.append("\n");
			}
		}
		
		p.append("}");
	}
	
	private boolean isEnumerator(com.sun.tools.javac.tree.JCTree jctree) {
		return jctree.tag == 5 && (((com.sun.tools.javac.tree.JCTree.JCVariableDecl)jctree).mods.flags & 16384L) != 0L;
	}
	
	/**
	 * Reconstitute methods. We could just do node.toString() here, but
	 * constructors need to be renamed from &lt;init&gt; to the class name, so
	 * handle all methods.
	 */
	@Override
	public Void visitMethod(MethodTree node, StringBuilder p) {
		p.append("\n");
		p.append(node.getModifiers());
		p.append(" ");
		if ( node.getReturnType() != null ) p.append(node.getReturnType());
		if ( node.getTypeParameters().size() > 0 ) {
			p.append(" <");
			p.append(node.getTypeParameters());
			p.append(">");
		}
		p.append(" ");
		if ( node.getName() != null && "<init>".equals(node.getName().toString().trim()) ) p.append(currentClassName);
		else p.append(node.getName());
		
		p.append("(");
		p.append(node.getParameters());
		p.append(")");
		if ( node.getThrows().size() > 0 ) {
			p.append("\nthrows ");
			p.append(node.getThrows());
		}
		
		p.append(" {\n");
		// this needs to be done for enums, otherwise we'll get a compile error
		// if we didn't need this case, we'd just do p.append(node.getBody())
		for ( StatementTree st : node.getBody().getStatements() ) {
			if ( "super()".equals(st.toString().trim()) ) continue;
			p.append(st);
			if ( p.charAt(p.length() - 1) != ';' ) p.append(";\n");
		}
		p.append("\n}\n");
		return null;
	}
	
	/**
	 * Reconstitute class fields. While doing so, search for our annotation to
	 * perform processing.
	 */
	@Override
	public Void visitVariable(VariableTree node, StringBuilder p) {
		currentField = node.getName().toString();
		currentType = node.getType().toString();
		p.append("\n");
		
		for ( AnnotationTree a : node.getModifiers().getAnnotations() ) {
			String anno = ((IdentifierTree)a.getAnnotationType()).getName().toString();
			if ( !PROPERTY_ANNOTATION.equals(anno) ) {
				p.append(a);
				p.append("\n");
			}
		}
		
		for ( Modifier m : node.getModifiers().getFlags() ) {
			p.append(m.toString());
			p.append(" ");
		}
		p.append(" ");
		p.append(currentType);
		p.append(" ");
		p.append(currentField);
		
		if ( node.getInitializer() != null ) {
			p.append(" = ");
			p.append(node.getInitializer());
		}
		p.append(";");
		super.visitVariable(node, p);
		return null;
	}
}
