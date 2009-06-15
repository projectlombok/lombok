package lombok.eclipse;

import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public abstract class EclipseASTAdapter implements EclipseASTVisitor {
	@Override public void visitCompilationUnit(Node node, CompilationUnitDeclaration unit) {}
	@Override public void endVisitCompilationUnit(Node node, CompilationUnitDeclaration unit) {}
	@Override public void visitType(Node node, TypeDeclaration type) {}
	@Override public void visitAnnotationOnType(Node node, TypeDeclaration type, Annotation annotation) {}
	@Override public void endVisitType(Node node, TypeDeclaration type) {}
	@Override public void visitInitializer(Node node, Initializer initializer) {}
	@Override public void endVisitInitializer(Node node, Initializer initializer) {}
	@Override public void visitField(Node node, FieldDeclaration field) {}
	@Override public void visitAnnotationOnField(Node node, FieldDeclaration Field, Annotation annotation) {}
	@Override public void endVisitField(Node node, FieldDeclaration field) {}
	@Override public void visitMethod(Node node, AbstractMethodDeclaration method) {}
	@Override public void visitAnnotationOnMethod(Node node, AbstractMethodDeclaration method, Annotation annotation) {}
	@Override public void endVisitMethod(Node node, AbstractMethodDeclaration method) {}
	@Override public void visitLocal(Node node, LocalDeclaration local) {}
	@Override public void visitAnnotationOnLocal(Node node, LocalDeclaration local, Annotation annotation) {}
	@Override public void endVisitLocal(Node node, LocalDeclaration local) {}
	@Override public void visitStatement(Node node, Statement statement) {}
	@Override public void endVisitStatement(Node node, Statement statement) {}
}
