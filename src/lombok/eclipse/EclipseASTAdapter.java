package lombok.eclipse;

import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public abstract class EclipseASTAdapter implements EclipseASTVisitor {
	@Override public void visitCompilationUnit(Node node, CompilationUnitDeclaration unit) {}
	@Override public void endVisitCompilationUnit(Node node, CompilationUnitDeclaration unit) {}
	@Override public void visitType(Node node, TypeDeclaration type) {}
	@Override public void endVisitType(Node node, TypeDeclaration type) {}
	@Override public void visitInitializer(Node node, Initializer initializer) {}
	@Override public void endVisitInitializer(Node node, Initializer initializer) {}
	@Override public void visitField(Node node, FieldDeclaration field) {}
	@Override public void endVisitField(Node node, FieldDeclaration field) {}
	@Override public void visitMethod(Node node, AbstractMethodDeclaration declaration) {}
	@Override public void endVisitMethod(Node node, AbstractMethodDeclaration declaration) {}
	@Override public void visitLocal(Node node, LocalDeclaration declaration) {}
	@Override public void endVisitLocal(Node node, LocalDeclaration declaration) {}
}
