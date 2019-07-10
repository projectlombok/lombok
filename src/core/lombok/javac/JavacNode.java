/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.handlers.JavacHandlerUtil;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Javac specific version of the LombokNode class.
 */
public class JavacNode extends lombok.core.LombokNode<JavacAST, JavacNode, JCTree> {
	private JavacAST ast;
	/**
	 * Passes through to the parent constructor.
	 */
	public JavacNode(JavacAST ast, JCTree node, List<JavacNode> children, Kind kind) {
		super(node, children, kind);
		this.ast = ast;
	}
	
	@Override 
	public JavacAST getAst() {
		return ast;
	}
	
	public Element getElement() {
		if (node instanceof JCClassDecl) return ((JCClassDecl) node).sym;
		if (node instanceof JCMethodDecl) return ((JCMethodDecl) node).sym;
		if (node instanceof JCVariableDecl) return ((JCVariableDecl) node).sym;
		return null;
	}
	
	public int getEndPosition(DiagnosticPosition pos) {
		JCCompilationUnit cu = (JCCompilationUnit) top().get();
		return Javac.getEndPosition(pos, cu);
	}
	
	public int getEndPosition() {
		return getEndPosition(node);
	}
	
	/**
	 * Visits this node and all child nodes depth-first, calling the provided visitor's visit methods.
	 */
	public void traverse(JavacASTVisitor visitor) {
		switch (this.getKind()) {
		case COMPILATION_UNIT:
			visitor.visitCompilationUnit(this, (JCCompilationUnit) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitCompilationUnit(this, (JCCompilationUnit) get());
			break;
		case TYPE:
			visitor.visitType(this, (JCClassDecl) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitType(this, (JCClassDecl) get());
			break;
		case FIELD:
			visitor.visitField(this, (JCVariableDecl) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitField(this, (JCVariableDecl) get());
			break;
		case METHOD:
			visitor.visitMethod(this, (JCMethodDecl) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethod(this, (JCMethodDecl) get());
			break;
		case INITIALIZER:
			visitor.visitInitializer(this, (JCBlock) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitInitializer(this, (JCBlock) get());
			break;
		case ARGUMENT:
			JCMethodDecl parentMethod = (JCMethodDecl) up().get();
			visitor.visitMethodArgument(this, (JCVariableDecl) get(), parentMethod);
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethodArgument(this, (JCVariableDecl) get(), parentMethod);
			break;
		case LOCAL:
			visitor.visitLocal(this, (JCVariableDecl) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitLocal(this, (JCVariableDecl) get());
			break;
		case STATEMENT:
			visitor.visitStatement(this, get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitStatement(this, get());
			break;
		case ANNOTATION:
			switch (up().getKind()) {
			case TYPE:
				visitor.visitAnnotationOnType((JCClassDecl) up().get(), this, (JCAnnotation) get());
				break;
			case FIELD:
				visitor.visitAnnotationOnField((JCVariableDecl) up().get(), this, (JCAnnotation) get());
				break;
			case METHOD:
				visitor.visitAnnotationOnMethod((JCMethodDecl) up().get(), this, (JCAnnotation) get());
				break;
			case ARGUMENT:
				JCVariableDecl argument = (JCVariableDecl) up().get();
				JCMethodDecl method = (JCMethodDecl) up().up().get();
				visitor.visitAnnotationOnMethodArgument(argument, method, this, (JCAnnotation) get());
				break;
			case LOCAL:
				visitor.visitAnnotationOnLocal((JCVariableDecl) up().get(), this, (JCAnnotation) get());
				break;
			case TYPE_USE:
				visitor.visitAnnotationOnTypeUse(up().get(), this, (JCAnnotation) get());
				break;
			default:
				throw new AssertionError("Annotion not expected as child of a " + up().getKind());
			}
			break;
		case TYPE_USE:
			visitor.visitTypeUse(this, get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitTypeUse(this, get());
			break;
		default:
			throw new AssertionError("Unexpected kind during node traversal: " + getKind());
		}
	}
	
	/** {@inheritDoc} */
	@Override public String getName() {
		final Name n;
		
		if (node instanceof JCClassDecl) n = ((JCClassDecl) node).name;
		else if (node instanceof JCMethodDecl) n = ((JCMethodDecl) node).name;
		else if (node instanceof JCVariableDecl) n = ((JCVariableDecl) node).name;
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
	public JavacTreeMaker getTreeMaker() {
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
		return LombokOptions.shouldDeleteLombokAnnotations(ast.getContext());
	}
	
	/**
	 * Convenient shortcut to the owning JavacAST object's toName method.
	 * 
	 * @see JavacAST#toName(String)
	 */
	public Name toName(String name) {
		return ast.toName(name);
	}
	
	public void removeDeferredErrors() {
		ast.removeDeferredErrors(this);
	}
	
	/**
	 * Generates an compiler error focused on the AST node represented by this node object.
	 */
	@Override public void addError(String message) {
		ast.printMessage(Diagnostic.Kind.ERROR, message, this, null, true);
	}
	
	/**
	 * Generates an compiler error focused on the AST node represented by this node object.
	 */
	public void addError(String message, DiagnosticPosition pos) {
		ast.printMessage(Diagnostic.Kind.ERROR, message, null, pos, true);
	}
	
	/**
	 * Generates a compiler warning focused on the AST node represented by this node object.
	 */
	@Override public void addWarning(String message) {
		ast.printMessage(Diagnostic.Kind.WARNING, message, this, null, false);
	}
	
	/**
	 * Generates a compiler warning focused on the AST node represented by this node object.
	 */
	public void addWarning(String message, DiagnosticPosition pos) {
		ast.printMessage(Diagnostic.Kind.WARNING, message, null, pos, false);
	}
	
	@Override public boolean hasAnnotation(Class<? extends Annotation> type) {
		return JavacHandlerUtil.hasAnnotationAndDeleteIfNeccessary(type, this);
	}
	
	@Override public <Z extends Annotation> AnnotationValues<Z> findAnnotation(Class<Z> type) {
		JavacNode annotation = JavacHandlerUtil.findAnnotation(type, this, true);
		if (annotation == null) return null;
		return JavacHandlerUtil.createAnnotation(type, annotation);
	}
	
	private JCModifiers getModifiers() {
		if (node instanceof JCClassDecl) return ((JCClassDecl) node).getModifiers();
		if (node instanceof JCMethodDecl) return ((JCMethodDecl) node).getModifiers();
		if (node instanceof JCVariableDecl) return ((JCVariableDecl) node).getModifiers();
		return null;
	}
	
	@Override public boolean isStatic() {
		if (node instanceof JCClassDecl) {
			JavacNode directUp = directUp();
			if (directUp == null || directUp.getKind() == Kind.COMPILATION_UNIT) return true;
			if (!(directUp.get() instanceof JCClassDecl)) return false;
			JCClassDecl p = (JCClassDecl) directUp.get();
			long f = p.mods.flags;
			if ((Flags.INTERFACE & f) != 0) return true;
			if ((Flags.ENUM & f) != 0) return true;
		}
		
		if (node instanceof JCVariableDecl) {
			JavacNode directUp = directUp();
			if (directUp != null && directUp.get() instanceof JCClassDecl) {
				JCClassDecl p = (JCClassDecl) directUp.get();
				long f = p.mods.flags;
				if ((Flags.INTERFACE & f) != 0) return true;
			}
		}
		
		JCModifiers mods = getModifiers();
		if (mods == null) return false;
		return (mods.flags & Flags.STATIC) != 0;
	}
	
	@Override public boolean isEnumMember() {
		if (getKind() != Kind.FIELD) return false;
		JCModifiers mods = getModifiers();
		return mods != null && (Flags.ENUM & mods.flags) != 0;
	}
	
	@Override public boolean isEnumType() {
		if (getKind() != Kind.TYPE) return false;
		JCModifiers mods = getModifiers();
		return mods != null && (Flags.ENUM & mods.flags) != 0;
	}
	
	@Override public boolean isTransient() {
		if (getKind() != Kind.FIELD) return false;
		JCModifiers mods = getModifiers();
		return mods != null && (Flags.TRANSIENT & mods.flags) != 0;
	}
	
	@Override public int countMethodParameters() {
		if (getKind() != Kind.METHOD) return 0;
		
		com.sun.tools.javac.util.List<JCVariableDecl> params = ((JCMethodDecl) node).params;
		if (params == null) return 0;
		return params.size();
	}
	
	@Override public int getStartPos() {
		return node.getPreferredPosition();
	}
}
