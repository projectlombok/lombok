package lombok.javac;

import lombok.javac.JavacAST.Node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public interface JavacASTVisitor {
	/**
	 * Called at the very beginning and end.
	 */
	void visitCompilationUnit(Node top, JCCompilationUnit unit);
	void endVisitCompilationUnit(Node top, JCCompilationUnit unit);
	
	/**
	 * Called when visiting a type (a class, interface, annotation, enum, etcetera).
	 */
	void visitType(Node typeNode, JCClassDecl type);
	void visitAnnotationOnType(JCClassDecl type, Node annotationNode, JCAnnotation annotation);
	void endVisitType(Node typeNode, JCClassDecl type);
	
	/**
	 * Called when visiting a field of a class.
	 */
	void visitField(Node fieldNode, JCVariableDecl field);
	void visitAnnotationOnField(JCVariableDecl field, Node annotationNode, JCAnnotation annotation);
	void endVisitField(Node fieldNode, JCVariableDecl field);
	
	/**
	 * Called for static and instance initializers. You can tell the difference via the isStatic() method.
	 */
	void visitInitializer(Node initializerNode, JCBlock initializer);
	void endVisitInitializer(Node initializerNode, JCBlock initializer);
	
	/**
	 * Called for both methods and constructors.
	 */
	void visitMethod(Node methodNode, JCMethodDecl method);
	void visitAnnotationOnMethod(JCMethodDecl method, Node annotationNode, JCAnnotation annotation);
	void endVisitMethod(Node methodNode, JCMethodDecl method);
	
	/**
	 * Visits a method argument.
	 */
	void visitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method);
	void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, Node annotationNode, JCAnnotation annotation);
	void endVisitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method);
	
	/**
	 * Visits a local declaration - that is, something like 'int x = 10;' on the method level. Also called
	 * for method parameters.
	 */
	void visitLocal(Node localNode, JCVariableDecl local);
	void visitAnnotationOnLocal(JCVariableDecl local, Node annotationNode, JCAnnotation annotation);
	void endVisitLocal(Node localNode, JCVariableDecl local);
	
	/**
	 * Visits a statement that isn't any of the other visit methods (e.g. JCClassDecl).
	 * The statement object is guaranteed to be either a JCStatement or a JCExpression.
	 */
	void visitStatement(Node statementNode, JCTree statement);
	void endVisitStatement(Node statementNode, JCTree statement);
	
	public static class JavacASTPrinter implements JavacASTVisitor {
		int indent = 0;
		private void print(String text, Object... params) {
			StringBuilder sb = new StringBuilder();
			for ( int i = 0 ; i < indent ; i++ ) sb.append("  ");
			System.out.printf(sb.append(text).append('\n').toString(), params);
		}
		
		@Override public void visitCompilationUnit(Node Node, JCCompilationUnit unit) {
			System.out.println("---------------------------------------------------------");
			
			print("<CU %s>", Node.getFileName());
			indent++;
		}
		
		@Override public void endVisitCompilationUnit(Node node, JCCompilationUnit unit) {
			indent--;
			print("</CUD>");
		}
		
		@Override public void visitType(Node node, JCClassDecl type) {
			print("<TYPE %s>", type.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnType(JCClassDecl type, Node node, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitType(Node node, JCClassDecl type) {
			indent--;
			print("</TYPE %s>", type.name);
		}
		
		@Override public void visitInitializer(Node node, JCBlock initializer) {
			print("<%s INITIALIZER>",
					initializer.isStatic() ? "static" : "instance");
			indent++;
		}
		
		@Override public void endVisitInitializer(Node node, JCBlock initializer) {
			indent--;
			print("</%s INITIALIZER>", initializer.isStatic() ? "static" : "instance");
		}
		
		@Override public void visitField(Node node, JCVariableDecl field) {
			print("<FIELD %s %s>", field.vartype, field.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, Node node, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitField(Node node, JCVariableDecl field) {
			indent--;
			print("</FIELD %s %s>", field.vartype, field.name);
		}
		
		@Override public void visitMethod(Node node, JCMethodDecl method) {
			String type = method.name.contentEquals("<init>") ? "CONSTRUCTOR" : "METHOD";
			print("<%s %s>", type, method.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, Node node, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethod(Node node, JCMethodDecl method) {
			String type = method.name.contentEquals("<init>") ? "CONSTRUCTOR" : "METHOD";
			indent--;
			print("</%s %s>", type, method.name);
		}
		
		@Override public void visitMethodArgument(Node node, JCVariableDecl arg, JCMethodDecl method) {
			print("<METHODARG %s %s>", arg.vartype, arg.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl arg, JCMethodDecl method, Node nodeAnnotation, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethodArgument(Node node, JCVariableDecl arg, JCMethodDecl method) {
			indent--;
			print("</METHODARG %s %s>", arg.vartype, arg.name);
		}
		
		@Override public void visitLocal(Node node, JCVariableDecl local) {
			print("<LOCAL %s %s>", local.vartype, local.name);
			indent++;
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, Node node, JCAnnotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitLocal(Node node, JCVariableDecl local) {
			indent--;
			print("</LOCAL %s %s>", local.vartype, local.name);
		}
		
		@Override public void visitStatement(Node node, JCTree statement) {
			print("<%s>", statement.getClass());
			indent++;
		}
		
		@Override public void endVisitStatement(Node node, JCTree statement) {
			indent--;
			print("</%s>", statement.getClass());
		}
	}
}
