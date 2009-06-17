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

public abstract class EclipseASTAdapter implements EclipseASTVisitor {
	@Override public void visitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	@Override public void endVisitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	@Override public void visitType(Node typeNode, TypeDeclaration type) {}
	@Override public void visitAnnotationOnType(TypeDeclaration type, Node annotationNode, Annotation annotation) {}
	@Override public void endVisitType(Node typeNode, TypeDeclaration type) {}
	@Override public void visitInitializer(Node initializerNode, Initializer initializer) {}
	@Override public void endVisitInitializer(Node initializerNode, Initializer initializer) {}
	@Override public void visitField(Node fieldNode, FieldDeclaration field) {}
	@Override public void visitAnnotationOnField(FieldDeclaration field, Node annotationNode, Annotation annotation) {}
	@Override public void endVisitField(Node fieldNode, FieldDeclaration field) {}
	@Override public void visitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	@Override public void visitAnnotationOnMethod(AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	@Override public void endVisitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	@Override public void visitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	@Override public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	@Override public void endVisitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	@Override public void visitLocal(Node localNode, LocalDeclaration local) {}
	@Override public void visitAnnotationOnLocal(LocalDeclaration local, Node annotationNode, Annotation annotation) {}
	@Override public void endVisitLocal(Node localNode, LocalDeclaration local) {}
	@Override public void visitStatement(Node statementNode, Statement statement) {}
	@Override public void endVisitStatement(Node statementNode, Statement statement) {}
}
