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
package lombok.eclipse;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

/**
 * Standard adapter for the {@link EclipseASTVisitor} interface. Every method on that interface
 * has been implemented with an empty body. Override whichever methods you need.
 */
public abstract class EclipseASTAdapter implements EclipseASTVisitor {
	
	private final boolean deferUntilPostDiet = getClass().isAnnotationPresent(DeferUntilPostDiet.class);

	/** {@inheritDoc} */
	public void visitCompilationUnit(EclipseNode top, CompilationUnitDeclaration unit) {}
	
	/** {@inheritDoc} */
	public void endVisitCompilationUnit(EclipseNode top, CompilationUnitDeclaration unit) {}
	
	/** {@inheritDoc} */
	public void visitType(EclipseNode typeNode, TypeDeclaration type) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnType(TypeDeclaration type, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitType(EclipseNode typeNode, TypeDeclaration type) {}
	
	/** {@inheritDoc} */
	public void visitInitializer(EclipseNode initializerNode, Initializer initializer) {}
	
	/** {@inheritDoc} */
	public void endVisitInitializer(EclipseNode initializerNode, Initializer initializer) {}
	
	/** {@inheritDoc} */
	public void visitField(EclipseNode fieldNode, FieldDeclaration field) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnField(FieldDeclaration field, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitField(EclipseNode fieldNode, FieldDeclaration field) {}
	
	/** {@inheritDoc} */
	public void visitMethod(EclipseNode methodNode, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnMethod(AbstractMethodDeclaration method, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitMethod(EclipseNode methodNode, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitMethodArgument(EclipseNode argNode, Argument arg, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitMethodArgument(EclipseNode argNode, Argument arg, AbstractMethodDeclaration method) {}
	
	/** {@inheritDoc} */
	public void visitLocal(EclipseNode localNode, LocalDeclaration local) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnLocal(LocalDeclaration local, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	public void endVisitLocal(EclipseNode localNode, LocalDeclaration local) {}
	
	/** {@inheritDoc} */
	@Override public void visitTypeUse(EclipseNode typeUseNode, TypeReference typeUse) {}
	
	/** {@inheritDoc} */
	public void visitAnnotationOnTypeUse(TypeReference typeUse, EclipseNode annotationNode, Annotation annotation) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitTypeUse(EclipseNode typeUseNode, TypeReference typeUse) {}
	
	/** {@inheritDoc} */
	public void visitStatement(EclipseNode statementNode, Statement statement) {}
	
	/** {@inheritDoc} */
	public void endVisitStatement(EclipseNode statementNode, Statement statement) {}
	
	public boolean isDeferUntilPostDiet() {
		return deferUntilPostDiet ;
	}
}
