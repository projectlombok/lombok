package lombok.eclipse;

import java.lang.reflect.Modifier;

import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public interface EclipseASTVisitor {
	/**
	 * Called at the very beginning and end.
	 */
	void visitCompilationUnit(Node node, CompilationUnitDeclaration unit);
	void endVisitCompilationUnit(Node node, CompilationUnitDeclaration unit);
	
	/**
	 * Called when visiting a type (a class, interface, annotation, enum, etcetera).
	 */
	void visitType(Node node, TypeDeclaration type);
	void endVisitType(Node node, TypeDeclaration type);
	
	/**
	 * Called when visiting a field of a class.
	 * Even though in eclipse initializers (both instance and static) are represented as Initializer objects,
	 * which are a subclass of FieldDeclaration, those do NOT result in a call to this method. They result
	 * in a call to the visitInitializer method.
	 */
	void visitField(Node node, FieldDeclaration field);
	void endVisitField(Node node, FieldDeclaration field);
	
	/**
	 * Called for static and instance initializers. You can tell the difference via the modifier flag on the
	 * ASTNode (8 for static, 0 for not static). The content is in the 'block', not in the 'initialization',
	 * which would always be null for an initializer instance.
	 */
	void visitInitializer(Node node, Initializer initializer);
	void endVisitInitializer(Node node, Initializer initializer);
	
	/**
	 * Called for both methods (MethodDeclaration) and constructors (ConstructorDeclaration), but not for
	 * Clinit objects, which are a vestigial eclipse thing that never contain anything. Static initializers
	 * show up as 'Initializer', in the visitInitializer method, with modifier bit STATIC set.
	 */
	void visitMethod(Node node, AbstractMethodDeclaration declaration);
	void endVisitMethod(Node node, AbstractMethodDeclaration declaration);
	
	/**
	 * Visits a local declaration - that is, something like 'int x = 10;' on the method level. Also called
	 * for method parameter (those would be Arguments, a subclass of LocalDeclaration).
	 */
	void visitLocal(Node node, LocalDeclaration declaration);
	void endVisitLocal(Node node, LocalDeclaration declaration);
	
	public static class EclipseASTPrinter implements EclipseASTVisitor {
		int indent = 0;
		private void print(String text, Object... params) {
			StringBuilder sb = new StringBuilder();
			for ( int i = 0 ; i < indent ; i++ ) sb.append("  ");
			System.out.printf(sb.append(text).append('\n').toString(), params);
		}
		
		private String str(char[] c) {
			if ( c == null ) return "(NULL)";
			else return new String(c);
		}
		
		private String str(TypeReference type) {
			if ( type == null ) return "(NULL)";
			char[][] c = type.getTypeName();
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for ( char[] d : c ) {
				sb.append(first ? "" : ".").append(new String(d));
				first = false;
			}
			return sb.toString();
		}
		
		@Override public void visitCompilationUnit(Node node, CompilationUnitDeclaration unit) {
			System.out.println("---------------------------------------------------------");
			System.out.println(node.isCompleteParse() ? "COMPLETE" : "incomplete");
			
			print("<CUD %s>", node.getFileName());
			indent++;
		}
		
		@Override public void endVisitCompilationUnit(Node node, CompilationUnitDeclaration unit) {
			indent--;
			print("</CUD>");
		}
		
		@Override public void visitType(Node node, TypeDeclaration type) {
			print("<TYPE %s>", str(type.name));
			indent++;
		}
		
		@Override public void endVisitType(Node node, TypeDeclaration type) {
			indent--;
			print("</TYPE %s>", str(type.name));
		}
		
		@Override public void visitInitializer(Node node, Initializer initializer) {
			Block block = initializer.block;
			boolean s = (block != null && block.statements != null);
			print("<%s INITIALIZER: %s>",
					(initializer.modifiers & Modifier.STATIC) > 0 ? "static" : "instance",
							s ? "filled" : "blank");
			indent++;
		}
		
		@Override public void endVisitInitializer(Node node, Initializer initializer) {
			indent--;
			print("</%s INITIALIZER>", (initializer.modifiers & Modifier.STATIC) > 0 ? "static" : "instance");
		}
		
		@Override public void visitField(Node node, FieldDeclaration field) {
			print("<FIELD %s %s = %s>", str(field.type), str(field.name), field.initialization);
			indent++;
		}
		
		@Override public void endVisitField(Node node, FieldDeclaration field) {
			indent--;
			print("</FIELD %s %s>", str(field.type), str(field.name));
		}
		
		@Override public void visitMethod(Node node, AbstractMethodDeclaration method) {
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			print("<%s %s: %s>", type, str(method.selector), method.statements != null ? "filled" : "blank");
			indent++;
		}
		
		@Override public void endVisitMethod(Node node, AbstractMethodDeclaration method) {
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			indent--;
			print("</%s %s>", type, str(method.selector));
		}
		
		@Override public void visitLocal(Node node, LocalDeclaration local) {
			String type = local instanceof Argument ? "ARGUMENT" : "LOCAL";
			print("<%s %s %s = %s>", type, str(local.type), str(local.name), local.initialization);
			indent++;
		}
		
		@Override public void endVisitLocal(Node node, LocalDeclaration local) {
			String type = local instanceof Argument ? "ARGUMENT" : "LOCAL";
			indent--;
			print("</%s %s %s>", type, str(local.type), str(local.name));
		}
	}
}
