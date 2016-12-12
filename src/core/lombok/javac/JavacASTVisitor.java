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
package lombok.javac;

import java.io.PrintStream;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Implement so you can ask any JavacAST.LombokNode to traverse depth-first through all children,
 * calling the appropriate visit and endVisit methods.
 */
public interface JavacASTVisitor {
	void setTrees(Trees trees);
	
	/**
	 * Called at the very beginning and end.
	 */
	void visitCompilationUnit(JavacNode top, JCCompilationUnit unit);
	void endVisitCompilationUnit(JavacNode top, JCCompilationUnit unit);
	
	/**
	 * Called when visiting a type (a class, interface, annotation, enum, etcetera).
	 */
	void visitType(JavacNode typeNode, JCClassDecl type);
	void visitAnnotationOnType(JCClassDecl type, JavacNode annotationNode, JCAnnotation annotation);
	void endVisitType(JavacNode typeNode, JCClassDecl type);
	
	/**
	 * Called when visiting a field of a class.
	 */
	void visitField(JavacNode fieldNode, JCVariableDecl field);
	void visitAnnotationOnField(JCVariableDecl field, JavacNode annotationNode, JCAnnotation annotation);
	void endVisitField(JavacNode fieldNode, JCVariableDecl field);
	
	/**
	 * Called for static and instance initializers. You can tell the difference via the isStatic() method.
	 */
	void visitInitializer(JavacNode initializerNode, JCBlock initializer);
	void endVisitInitializer(JavacNode initializerNode, JCBlock initializer);
	
	/**
	 * Called for both methods and constructors.
	 */
	void visitMethod(JavacNode methodNode, JCMethodDecl method);
	void visitAnnotationOnMethod(JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation);
	void endVisitMethod(JavacNode methodNode, JCMethodDecl method);
	
	/**
	 * Visits a method argument.
	 */
	void visitMethodArgument(JavacNode argumentNode, JCVariableDecl argument, JCMethodDecl method);
	void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation);
	void endVisitMethodArgument(JavacNode argumentNode, JCVariableDecl argument, JCMethodDecl method);
	
	/**
	 * Visits a local declaration - that is, something like 'int x = 10;' on the method level. Also called
	 * for method parameters.
	 */
	void visitLocal(JavacNode localNode, JCVariableDecl local);
	void visitAnnotationOnLocal(JCVariableDecl local, JavacNode annotationNode, JCAnnotation annotation);
	void endVisitLocal(JavacNode localNode, JCVariableDecl local);
	
	/**
	 * Visits a statement that isn't any of the other visit methods (e.g. JCClassDecl).
	 * The statement object is guaranteed to be either a JCStatement or a JCExpression.
	 */
	void visitStatement(JavacNode statementNode, JCTree statement);
	void endVisitStatement(JavacNode statementNode, JCTree statement);
	
	/**
	 * Prints the structure of an AST.
	 */
	public static class Printer implements JavacASTVisitor {
		private final PrintStream out;
		private final boolean printContent;
		private int disablePrinting = 0;
		private int indent = 0;
		
		/**
		 * @param printContent if true, bodies are printed directly, as java code,
		 * instead of a tree listing of every AST node inside it.
		 */
		public Printer(boolean printContent) {
			this(printContent, System.out);
		}
		
		/**
		 * @param printContent if true, bodies are printed directly, as java code,
		 * instead of a tree listing of every AST node inside it.
		 * @param out write output to this stream. You must close it yourself. flush() is called after every line.
		 * 
		 * @see java.io.PrintStream#flush()
		 */
		public Printer(boolean printContent, PrintStream out) {
			this.printContent = printContent;
			this.out = out;
		}
		
		@Override public void setTrees(Trees trees) {}
		
		private void forcePrint(String text, Object... params) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indent; i++) sb.append("  ");
			out.printf(sb.append(text).append('\n').toString(), params);
			out.flush();
		}
		
		private void print(String text, Object... params) {
			if (disablePrinting == 0) forcePrint(text, params);
		}
		
		@Override public void visitCompilationUnit(JavacNode LombokNode, JCCompilationUnit unit) {
			out.println("---------------------------------------------------------");
			
			print("<CU %s>", LombokNode.getFileName());
			indent++;
		}
		
		@Override public void endVisitCompilationUnit(JavacNode node, JCCompilationUnit unit) {
			indent--;
			print("</CUD>");
		}
		
		@Override public void visitType(JavacNode node, JCClassDecl type) {
			print("<TYPE %s>", type.name);
			indent++;
			if (printContent) {
				print("%s", type);
				disablePrinting++;
			}
		}
		
		@Override public void visitAnnotationOnType(JCClassDecl type, JavacNode node, JCAnnotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitType(JavacNode node, JCClassDecl type) {
			if (printContent) disablePrinting--;
			indent--;
			print("</TYPE %s>", type.name);
		}
		
		@Override public void visitInitializer(JavacNode node, JCBlock initializer) {
			print("<%s INITIALIZER>",
					initializer.isStatic() ? "static" : "instance");
			indent++;
			if (printContent) {
				print("%s", initializer);
				disablePrinting++;
			}
		}
		
		@Override public void endVisitInitializer(JavacNode node, JCBlock initializer) {
			if (printContent) disablePrinting--;
			indent--;
			print("</%s INITIALIZER>", initializer.isStatic() ? "static" : "instance");
		}
		
		@Override public void visitField(JavacNode node, JCVariableDecl field) {
			print("<FIELD %s %s>", field.vartype, field.name);
			indent++;
			if (printContent) {
				if (field.init != null) print("%s", field.init);
				disablePrinting++;
			}
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, JavacNode node, JCAnnotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitField(JavacNode node, JCVariableDecl field) {
			if (printContent) disablePrinting--;
			indent--;
			print("</FIELD %s %s>", field.vartype, field.name);
		}
		
		@Override public void visitMethod(JavacNode node, JCMethodDecl method) {
			final String type;
			if (method.name.contentEquals("<init>")) {
				if ((method.mods.flags & Flags.GENERATEDCONSTR) != 0) {
					type = "DEFAULTCONSTRUCTOR";
				} else type = "CONSTRUCTOR";
			} else type = "METHOD";
			print("<%s %s> returns: %s", type, method.name, method.restype);
			indent++;
			if (printContent) {
				if (method.body == null) print("(ABSTRACT)");
				else print("%s", method.body);
				disablePrinting++;
			}
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, JavacNode node, JCAnnotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethod(JavacNode node, JCMethodDecl method) {
			if (printContent) disablePrinting--;
			indent--;
			print("</%s %s>", "XMETHOD", method.name);
		}
		
		@Override public void visitMethodArgument(JavacNode node, JCVariableDecl arg, JCMethodDecl method) {
			print("<METHODARG %s %s>", arg.vartype, arg.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl arg, JCMethodDecl method, JavacNode nodeAnnotation, JCAnnotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethodArgument(JavacNode node, JCVariableDecl arg, JCMethodDecl method) {
			indent--;
			print("</METHODARG %s %s>", arg.vartype, arg.name);
		}
		
		@Override public void visitLocal(JavacNode node, JCVariableDecl local) {
			print("<LOCAL %s %s>", local.vartype, local.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, JavacNode node, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitLocal(JavacNode node, JCVariableDecl local) {
			indent--;
			print("</LOCAL %s %s>", local.vartype, local.name);
		}
		
		@Override public void visitStatement(JavacNode node, JCTree statement) {
			print("<%s>", statement.getClass());
			indent++;
			print("%s", statement);
		}
		
		@Override public void endVisitStatement(JavacNode node, JCTree statement) {
			indent--;
			print("</%s>", statement.getClass());
		}
	}
}
