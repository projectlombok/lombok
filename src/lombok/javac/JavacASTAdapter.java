package lombok.javac;

import lombok.javac.JavacAST.Node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class JavacASTAdapter implements JavacASTVisitor {
	@Override public void visitCompilationUnit(Node top, JCCompilationUnit unit) {}
	@Override public void endVisitCompilationUnit(Node top, JCCompilationUnit unit) {}
	@Override public void visitType(Node typeNode, JCClassDecl type) {}
	@Override public void visitAnnotationOnType(JCClassDecl type, Node annotationNode, JCAnnotation annotation) {}
	@Override public void endVisitType(Node typeNode, JCClassDecl type) {}
	@Override public void visitField(Node fieldNode, JCVariableDecl field) {}
	@Override public void visitAnnotationOnField(JCVariableDecl field, Node annotationNode, JCAnnotation annotation) {}
	@Override public void endVisitField(Node fieldNode, JCVariableDecl field) {}
	@Override public void visitInitializer(Node initializerNode, JCBlock initializer) {}
	@Override public void endVisitInitializer(Node initializerNode, JCBlock initializer) {}
	@Override public void visitMethod(Node methodNode, JCMethodDecl method) {}
	@Override public void visitAnnotationOnMethod(JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {}
	@Override public void endVisitMethod(Node methodNode, JCMethodDecl method) {}
	@Override public void visitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method) {}
	@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {}
	@Override public void endVisitMethodArgument(Node argumentNode, JCVariableDecl argument, JCMethodDecl method) {}
	@Override public void visitLocal(Node localNode, JCVariableDecl local) {}
	@Override public void visitAnnotationOnLocal(JCVariableDecl local, Node annotationNode, JCAnnotation annotation) {}
	@Override public void endVisitLocal(Node localNode, JCVariableDecl local) {}
	@Override public void visitStatement(Node statementNode, JCTree statement) {}
	@Override public void endVisitStatement(Node statementNode, JCTree statement) {}
}
