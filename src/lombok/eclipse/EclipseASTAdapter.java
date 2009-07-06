/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

/**
 * Standard adapter for the {@link EclipseASTVisitor} interface. Every method on that interface
 * has been implemented with an empty body. Override whichever methods you need.
 */
public abstract class EclipseASTAdapter implements EclipseASTVisitor {
	/** {@inheritDoc} */
	public void visitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	
	/** {@inheritDoc} */
	public void endVisitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	
	/** {@inheritDoc} */
	public void visitType(Node typeNode, TypeDeclaration type) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnType(TypeDeclaration type, Node annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitType(Node typeNode, TypeDeclaration type) {}
	
	/** {@inheritDoc} */
	public void visitInitializer(Node initializerNode, Initializer initializer) {}
	
	/** {@inheritDoc} */
	public void endVisitInitializer(Node initializerNode, Initializer initializer) {}
	
	/** {@inheritDoc} */
	public void visitField(Node fieldNode, FieldDeclaration field) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnField(FieldDeclaration field, Node annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitField(Node fieldNode, FieldDeclaration field) {}
	
	/** {@inheritDoc} */
	public void visitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnMethod(AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitLocal(Node localNode, LocalDeclaration local) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnLocal(LocalDeclaration local, Node annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitLocal(Node localNode, LocalDeclaration local) {}
	
	/** {@inheritDoc} */
	public void visitStatement(Node statementNode, Statement statement) {}
	
	/** {@inheritDoc} */
	public void endVisitStatement(Node statementNode, Statement statement) {}
}
