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
	public void visitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	public void endVisitCompilationUnit(Node top, CompilationUnitDeclaration unit) {}
	public void visitType(Node typeNode, TypeDeclaration type) {}
	public void visitAnnotationOnType(TypeDeclaration type, Node annotationNode, Annotation annotation) {}
	public void endVisitType(Node typeNode, TypeDeclaration type) {}
	public void visitInitializer(Node initializerNode, Initializer initializer) {}
	public void endVisitInitializer(Node initializerNode, Initializer initializer) {}
	public void visitField(Node fieldNode, FieldDeclaration field) {}
	public void visitAnnotationOnField(FieldDeclaration field, Node annotationNode, Annotation annotation) {}
	public void endVisitField(Node fieldNode, FieldDeclaration field) {}
	public void visitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	public void visitAnnotationOnMethod(AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	public void endVisitMethod(Node methodNode, AbstractMethodDeclaration method) {}
	public void visitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, Node annotationNode, Annotation annotation) {}
	public void endVisitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method) {}
	public void visitLocal(Node localNode, LocalDeclaration local) {}
	public void visitAnnotationOnLocal(LocalDeclaration local, Node annotationNode, Annotation annotation) {}
	public void endVisitLocal(Node localNode, LocalDeclaration local) {}
	public void visitStatement(Node statementNode, Statement statement) {}
	public void endVisitStatement(Node statementNode, Statement statement) {}
}
