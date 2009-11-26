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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;

public class JavacTransformer {
	private final HandlerLibrary handlers;
	private final Messager messager;
	
	public JavacTransformer(Messager messager) {
		this.messager = messager;
		this.handlers = HandlerLibrary.load(messager);
	}
	
	public void transform(Context context, Iterable<JCCompilationUnit> compilationUnits) {
		List<JavacAST> asts = new ArrayList<JavacAST>();
		
		for (JCCompilationUnit unit : compilationUnits) asts.add(new JavacAST(messager, context, unit));
		
		handlers.skipPrintAST();
		for (JavacAST ast : asts) {
			ast.traverse(new AnnotationVisitor());
			handlers.callASTVisitors(ast);
		}
		
		handlers.skipAllButPrintAST();
		for (JavacAST ast : asts) {
			ast.traverse(new AnnotationVisitor());
		}
	}
	
	private class AnnotationVisitor extends JavacASTAdapter {
		@Override public void visitAnnotationOnType(JCClassDecl type, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
	}
}
