/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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
package lombok.eclipse;

import java.util.List;

import lombok.core.AST.Kind;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

/**
 * Eclipse specific version of the LombokNode class.
 */
public class EclipseNode extends lombok.core.LombokNode<EclipseAST, EclipseNode, ASTNode> {
	/** {@inheritDoc} */
	EclipseNode(EclipseAST ast, ASTNode node, List<EclipseNode> children, Kind kind) {
		super(ast, node, children, kind);
	}
	
	/**
	 * Visits this node and all child nodes depth-first, calling the provided visitor's visit methods.
	 */
	public void traverse(EclipseASTVisitor visitor) {
		if (!this.isCompleteParse() && visitor.getClass().isAnnotationPresent(DeferUntilPostDiet.class)) return;
		
		switch (getKind()) {
		case COMPILATION_UNIT:
			visitor.visitCompilationUnit(this, (CompilationUnitDeclaration) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitCompilationUnit(this, (CompilationUnitDeclaration) get());
			break;
		case TYPE:
			visitor.visitType(this, (TypeDeclaration) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitType(this, (TypeDeclaration) get());
			break;
		case FIELD:
			visitor.visitField(this, (FieldDeclaration) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitField(this, (FieldDeclaration) get());
			break;
		case INITIALIZER:
			visitor.visitInitializer(this, (Initializer) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitInitializer(this, (Initializer) get());
			break;
		case METHOD:
			if (get() instanceof Clinit) return;
			visitor.visitMethod(this, (AbstractMethodDeclaration) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethod(this, (AbstractMethodDeclaration) get());
			break;
		case ARGUMENT:
			AbstractMethodDeclaration method = (AbstractMethodDeclaration) up().get();
			visitor.visitMethodArgument(this, (Argument) get(), method);
			ast.traverseChildren(visitor, this);
			visitor.endVisitMethodArgument(this, (Argument) get(), method);
			break;
		case LOCAL:
			visitor.visitLocal(this, (LocalDeclaration) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitLocal(this, (LocalDeclaration) get());
			break;
		case ANNOTATION:
			switch (up().getKind()) {
			case TYPE:
				visitor.visitAnnotationOnType((TypeDeclaration) up().get(), this, (Annotation) get());
				break;
			case FIELD:
				visitor.visitAnnotationOnField((FieldDeclaration) up().get(), this, (Annotation) get());
				break;
			case METHOD:
				visitor.visitAnnotationOnMethod((AbstractMethodDeclaration) up().get(), this, (Annotation) get());
				break;
			case ARGUMENT:
				visitor.visitAnnotationOnMethodArgument(
						(Argument) parent.get(),
						(AbstractMethodDeclaration) parent.directUp().get(),
						this, (Annotation) get());
				break;
			case LOCAL:
				visitor.visitAnnotationOnLocal((LocalDeclaration) parent.get(), this, (Annotation) get());
				break;
			default:
				throw new AssertionError("Annotation not expected as child of a " + up().getKind());
			}
			break;
		case STATEMENT:
			visitor.visitStatement(this, (Statement) get());
			ast.traverseChildren(visitor, this);
			visitor.endVisitStatement(this, (Statement) get());
			break;
		default:
			throw new AssertionError("Unexpected kind during node traversal: " + getKind());
		}
	}
	
	@Override protected boolean fieldContainsAnnotation(ASTNode field, ASTNode annotation) {
		if (!(field instanceof FieldDeclaration)) return false;
		FieldDeclaration f = (FieldDeclaration) field;
		if (f.annotations == null) return false;
		for (Annotation childAnnotation : f.annotations) {
			if (childAnnotation == annotation) return true;
		}
		return false;
	}
	
	/** {@inheritDoc} */
	@Override public String getName() {
		final char[] n;
		if (node instanceof TypeDeclaration) n = ((TypeDeclaration)node).name;
		else if (node instanceof FieldDeclaration) n = ((FieldDeclaration)node).name;
		else if (node instanceof AbstractMethodDeclaration) n = ((AbstractMethodDeclaration)node).selector;
		else if (node instanceof LocalDeclaration) n = ((LocalDeclaration)node).name;
		else n = null;
		
		return n == null ? null : new String(n);
	}
	
	/** {@inheritDoc} */
	@Override public void addError(String message) {
		this.addError(message, this.get().sourceStart, this.get().sourceEnd);
	}
	
	/** Generate a compiler error that shows the wavy underline from-to the stated character positions. */
	public void addError(String message, int sourceStart, int sourceEnd) {
		ast.addProblem(ast.new ParseProblem(false, message, sourceStart, sourceEnd));
	}
	
	/** {@inheritDoc} */
	@Override public void addWarning(String message) {
		this.addWarning(message, this.get().sourceStart, this.get().sourceEnd);
	}
	
	/** Generate a compiler warning that shows the wavy underline from-to the stated character positions. */
	public void addWarning(String message, int sourceStart, int sourceEnd) {
		ast.addProblem(ast.new ParseProblem(true, message, sourceStart, sourceEnd));
	}
	
	/** {@inheritDoc} */
	@Override protected boolean calculateIsStructurallySignificant(ASTNode parent) {
		if (node instanceof TypeDeclaration) return true;
		if (node instanceof AbstractMethodDeclaration) return true;
		if (node instanceof FieldDeclaration) return true;
		if (node instanceof LocalDeclaration) return true;
		if (node instanceof CompilationUnitDeclaration) return true;
		return false;
	}
	
	/**
	 * Convenient shortcut to the owning EclipseAST object's isCompleteParse method.
	 * 
	 * @see EclipseAST#isCompleteParse()
	 */
	public boolean isCompleteParse() {
		return ast.isCompleteParse();
	}
}
