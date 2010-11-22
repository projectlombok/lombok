/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import java.util.List;

import javax.tools.Diagnostic;

import lombok.core.AST.Kind;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Javac specific version of the LombokNode class.
 */
public class JavacNode extends lombok.core.LombokNode<JavacAST, JavacNode, JCTree> {
	/**
	 * Passes through to the parent constructor.
	 */
	public JavacNode(JavacAST ast, JCTree node, List<JavacNode> children, Kind kind) {
		super(ast, node, children, kind);
	}
	
	/**
	 * Visits this node and all child nodes depth-first, calling the provided visitor's visit methods.
	 */
	public void traverse(JavacASTVisitor visitor) {
		switch (this.getKind()) {
		case COMPILATION_UNIT:
			visitor.visitCompilationUnit(this, (JCCompilationUnit)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitCompilationUnit(this, (JCCompilationUnit)get());
			break;
		case TYPE:
			visitor.visitType(this, (JCClassDecl)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitType(this, (JCClassDecl)get());
			break;
		case FIELD:
			visitor.visitField(this, (JCVariableDecl)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitField(this, (JCVariableDecl)get());
			break;
		case METHOD:
			visitor.visitMethod(this, (JCMethodDecl)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethod(this, (JCMethodDecl)get());
			break;
		case INITIALIZER:
			visitor.visitInitializer(this, (JCBlock)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitInitializer(this, (JCBlock)get());
			break;
		case ARGUMENT:
			JCMethodDecl parentMethod = (JCMethodDecl) up().get();
			visitor.visitMethodArgument(this, (JCVariableDecl)get(), parentMethod);
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethodArgument(this, (JCVariableDecl)get(), parentMethod);
			break;
		case LOCAL:
			visitor.visitLocal(this, (JCVariableDecl)get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitLocal(this, (JCVariableDecl)get());
			break;
		case STATEMENT:
			visitor.visitStatement(this, get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitStatement(this, get());
			break;
		case ANNOTATION:
			switch (up().getKind()) {
			case TYPE:
				visitor.visitAnnotationOnType((JCClassDecl)up().get(), this, (JCAnnotation)get());
				break;
			case FIELD:
				visitor.visitAnnotationOnField((JCVariableDecl)up().get(), this, (JCAnnotation)get());
				break;
			case METHOD:
				visitor.visitAnnotationOnMethod((JCMethodDecl)up().get(), this, (JCAnnotation)get());
				break;
			case ARGUMENT:
				JCVariableDecl argument = (JCVariableDecl)up().get();
				JCMethodDecl method = (JCMethodDecl)up().up().get();
				visitor.visitAnnotationOnMethodArgument(argument, method, this, (JCAnnotation)get());
				break;
			case LOCAL:
				visitor.visitAnnotationOnLocal((JCVariableDecl)up().get(), this, (JCAnnotation)get());
				break;
			default:
				throw new AssertionError("Annotion not expected as child of a " + up().getKind());
			}
			break;
		default:
			throw new AssertionError("Unexpected kind during node traversal: " + getKind());
		}
	}
	
	/** {@inheritDoc} */
	@Override public String getName() {
		final Name n;
		
		if (node instanceof JCClassDecl) n = ((JCClassDecl)node).name;
		else if (node instanceof JCMethodDecl) n = ((JCMethodDecl)node).name;
		else if (node instanceof JCVariableDecl) n = ((JCVariableDecl)node).name;
		else n = null;
		
		return n == null ? null : n.toString();
	}
	
	/** {@inheritDoc} */
	@Override protected boolean calculateIsStructurallySignificant(JCTree parent) {
		if (node instanceof JCClassDecl) return true;
		if (node instanceof JCMethodDecl) return true;
		if (node instanceof JCVariableDecl) return true;
		if (node instanceof JCCompilationUnit) return true;
		//Static and instance initializers
		if (node instanceof JCBlock) return parent instanceof JCClassDecl;
		
		return false;
	}
	
	@Override protected boolean fieldContainsAnnotation(JCTree field, JCTree annotation) {
		if (!(field instanceof JCVariableDecl)) return false;
		JCVariableDecl f = (JCVariableDecl) field;
		if (f.mods.annotations == null) return false;
		for (JCAnnotation childAnnotation : f.mods.annotations) {
			if (childAnnotation == annotation) return true;
		}
		return false;
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's getTreeMaker method.
	 * 
	 * @see JavacAST#getTreeMaker()
	 */
	public TreeMaker getTreeMaker() {
		return ast.getTreeMaker();
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's getSymbolTable method.
	 * 
	 * @see JavacAST#getSymbolTable()
	 */
	public Symtab getSymbolTable() {
		return ast.getSymbolTable();
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's getTypesUtil method.
	 * 
	 * @see JavacAST#getTypesUtil()
	 */
	public JavacTypes getTypesUtil() {
		return ast.getTypesUtil();
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's getContext method.
	 * 
	 * @see JavacAST#getContext()
	 */
	public Context getContext() {
		return ast.getContext();
	}
	
	public boolean shouldDeleteLombokAnnotations() {
		DeleteLombokAnnotations dla = ast.getContext().get(DeleteLombokAnnotations.class);
		return dla != null && dla.isDeleteLombokAnnotations();
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's toName method.
	 * 
	 * @see JavacAST#toName(String)
	 */
	public Name toName(String name) {
		return ast.toName(name);
	}
	
	/**
	 * Generates an compiler error focused on the AST node represented by this node object.
	 */
	@Override public void addError(String message) {
		ast.printMessage(Diagnostic.Kind.ERROR, message, this, null);
	}
	
	/**
	 * Generates an compiler error focused on the AST node represented by this node object.
	 */
	public void addError(String message, DiagnosticPosition pos) {
		ast.printMessage(Diagnostic.Kind.ERROR, message, null, pos);
	}
	
	/**
	 * Generates a compiler warning focused on the AST node represented by this node object.
	 */
	@Override public void addWarning(String message) {
		ast.printMessage(Diagnostic.Kind.WARNING, message, this, null);
	}
	
	/**
	 * Generates a compiler warning focused on the AST node represented by this node object.
	 */
	public void addWarning(String message, DiagnosticPosition pos) {
		ast.printMessage(Diagnostic.Kind.WARNING, message, null, pos);
	}
}
