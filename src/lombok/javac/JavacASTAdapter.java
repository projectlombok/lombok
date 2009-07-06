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
package lombok.javac;

import lombok.javac.JavacAST.Node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Standard adapter for the {@link JavacASTVisitor} interface. Every method on that interface
 * has been implemented with an empty body. Override whichever methods you need.
 */
public class JavacASTAdapter implements JavacASTVisitor {
	/** {@inheritDoc} */
	@Override public void visitCompilationUnit(Node top, JCCompilationUnit unit) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitCompilationUnit(Node top, JCCompilationUnit unit) {}
	
	/** {@inheritDoc} */
	@Override public void visitType(Node typeNode, JCClassDecl type) {}
	
	/** {@inheritDoc} */
	@Override public void visitAnnotationOnType(JCClassDecl type, Node annotationNode, JCAnnotation annotation) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitType(Node typeNode, JCClassDecl type) {}
	
	/** {@inheritDoc} */
	@Override public void visitField(Node fieldNode, JCVariableDecl field) {}
	
	/** {@inheritDoc} */
	@Override public void visitAnnotationOnField(JCVariableDecl field, Node annotationNode, JCAnnotation annotation) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitField(Node fieldNode, JCVariableDecl field) {}
	
	/** {@inheritDoc} */
	@Override public void visitInitializer(Node initializerNode, JCBlock initializer) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitInitializer(Node initializerNode, JCBlock initializer) {}
	
	/** {@inheritDoc} */
	@Override public void visitMethod(Node methodNode, JCMethodDecl method) {}
	
	/** {@inheritDoc} */
	@Override public void visitAnnotationOnMethod(JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitMethod(Node methodNode, JCMethodDecl method) {}
	
	/** {@inheritDoc} */
	@Override public void visitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method) {}
	
	/** {@inheritDoc} */
	@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {}
	/** {@inheritDoc} */
	@Override public void endVisitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method) {}
	
	/** {@inheritDoc} */
	@Override public void visitLocal(Node localNode, JCVariableDecl local) {}
	
	/** {@inheritDoc} */
	@Override public void visitAnnotationOnLocal(JCVariableDecl local, Node annotationNode, JCAnnotation annotation) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitLocal(Node localNode, JCVariableDecl local) {}
	
	/** {@inheritDoc} */
	@Override public void visitStatement(Node statementNode, JCTree statement) {}
	
	/** {@inheritDoc} */
	@Override public void endVisitStatement(Node statementNode, JCTree statement) {}
}
