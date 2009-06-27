package lombok.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

//TODO expand javadoc
public interface EclipseASTVisitor {
	/**
	 * Called at the very beginning and end.
	 */
	void visitCompilationUnit(Node top, CompilationUnitDeclaration unit);
	void endVisitCompilationUnit(Node top, CompilationUnitDeclaration unit);
	
	/**
	 * Called when visiting a type (a class, interface, annotation, enum, etcetera).
	 */
	void visitType(Node typeNode, TypeDeclaration type);
	void visitAnnotationOnType(TypeDeclaration type, Node annotationNode, Annotation annotation);
	void endVisitType(Node typeNode, TypeDeclaration type);
	
	/**
	 * Called when visiting a field of a class.
	 * Even though in eclipse initializers (both instance and static) are represented as Initializer objects,
	 * which are a subclass of FieldDeclaration, those do NOT result in a call to this method. They result
	 * in a call to the visitInitializer method.
	 */
	void visitField(Node fieldNode, FieldDeclaration field);
	void visitAnnotationOnField(FieldDeclaration field, Node annotationNode, Annotation annotation);
	void endVisitField(Node fieldNode, FieldDeclaration field);
	
	/**
	 * Called for static and instance initializers. You can tell the difference via the modifier flag on the
	 * ASTNode (8 for static, 0 for not static). The content is in the 'block', not in the 'initialization',
	 * which would always be null for an initializer instance.
	 */
	void visitInitializer(Node initializerNode, Initializer initializer);
	void endVisitInitializer(Node initializerNode, Initializer initializer);
	
	/**
	 * Called for both methods (MethodDeclaration) and constructors (ConstructorDeclaration), but not for
	 * Clinit objects, which are a vestigial eclipse thing that never contain anything. Static initializers
	 * show up as 'Initializer', in the visitInitializer method, with modifier bit STATIC set.
	 */
	void visitMethod(Node methodNode, AbstractMethodDeclaration method);
	void visitAnnotationOnMethod(AbstractMethodDeclaration method, Node annotationNode, Annotation annotation);
	void endVisitMethod(Node methodNode, AbstractMethodDeclaration method);
	
	/**
	 * Visits a method argument
	 */
	void visitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method);
	void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, Node annotationNode, Annotation annotation);
	void endVisitMethodArgument(Node argNode, Argument arg, AbstractMethodDeclaration method);
	
	/**
	 * Visits a local declaration - that is, something like 'int x = 10;' on the method level.
	 */
	void visitLocal(Node localNode, LocalDeclaration local);
	void visitAnnotationOnLocal(LocalDeclaration local, Node annotationNode, Annotation annotation);
	void endVisitLocal(Node localNode, LocalDeclaration local);
	
	/**
	 * Visits a statement that isn't any of the other visit methods (e.g. TypeDeclaration).
	 */
	void visitStatement(Node statementNode, Statement statement);
	void endVisitStatement(Node statementNode, Statement statement);
	
	public static class Printer implements EclipseASTVisitor {
		private final PrintStream out;
		private final boolean printContent;
		private int disablePrinting = 0;
		private int indent = 0;
		
		public Printer(boolean printContent) {
			this(printContent, System.out);
		}
		
		public Printer(boolean printContent, File file) throws FileNotFoundException {
			this(printContent, new PrintStream(file));
		}
		
		public Printer(boolean printContent, PrintStream out) {
			this.printContent = printContent;
			this.out = out;
		}
		
		private void forcePrint(String text, Object... params) {
			StringBuilder sb = new StringBuilder();
			for ( int i = 0 ; i < indent ; i++ ) sb.append("  ");
			out.printf(sb.append(text).append('\n').toString(), params);
			out.flush();
		}
		
		private void print(String text, Object... params) {
			if ( disablePrinting == 0 ) forcePrint(text, params);
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
			out.println("---------------------------------------------------------");
			out.println(node.isCompleteParse() ? "COMPLETE" : "incomplete");
			
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
		
		@Override public void visitAnnotationOnType(TypeDeclaration type, Node node, Annotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitType(Node node, TypeDeclaration type) {
			indent--;
			print("</TYPE %s>", str(type.name));
		}
		
		@Override public void visitInitializer(Node node, Initializer initializer) {
			Block block = initializer.block;
			boolean s = (block != null && block.statements != null);
			print("<%s INITIALIZER: %s>",
					(initializer.modifiers & Modifier.STATIC) != 0 ? "static" : "instance",
							s ? "filled" : "blank");
			indent++;
			if ( printContent ) {
				if ( initializer.block != null ) print("%s", initializer.block);
				disablePrinting++;
			}
		}
		
		@Override public void endVisitInitializer(Node node, Initializer initializer) {
			if ( printContent ) disablePrinting--;
			indent--;
			print("</%s INITIALIZER>", (initializer.modifiers & Modifier.STATIC) != 0 ? "static" : "instance");
		}
		
		@Override public void visitField(Node node, FieldDeclaration field) {
			print("<FIELD %s %s = %s>", str(field.type), str(field.name), field.initialization);
			indent++;
			if ( printContent ) {
				if ( field.initialization != null ) print("%s", field.initialization);
				disablePrinting++;
			}
		}
		
		@Override public void visitAnnotationOnField(FieldDeclaration field, Node node, Annotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitField(Node node, FieldDeclaration field) {
			if ( printContent ) disablePrinting--;
			indent--;
			print("</FIELD %s %s>", str(field.type), str(field.name));
		}
		
		@Override public void visitMethod(Node node, AbstractMethodDeclaration method) {
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			print("<%s %s: %s>", type, str(method.selector), method.statements != null ? "filled" : "blank");
			indent++;
			if ( printContent ) {
				if ( method.statements != null ) print("%s", method);
				disablePrinting++;
			}
		}
		
		@Override public void visitAnnotationOnMethod(AbstractMethodDeclaration method, Node node, Annotation annotation) {
			forcePrint("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethod(Node node, AbstractMethodDeclaration method) {
			if ( printContent ) disablePrinting--;
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			indent--;
			print("</%s %s>", type, str(method.selector));
		}
		
		@Override public void visitMethodArgument(Node node, Argument arg, AbstractMethodDeclaration method) {
			print("<METHODARG %s %s = %s>", str(arg.type), str(arg.name), arg.initialization);
			indent++;
		}
		
		@Override public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, Node node, Annotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitMethodArgument(Node node, Argument arg, AbstractMethodDeclaration method) {
			indent--;
			print("</METHODARG %s %s>", str(arg.type), str(arg.name));
		}
		
		@Override public void visitLocal(Node node, LocalDeclaration local) {
			print("<LOCAL %s %s = %s>", str(local.type), str(local.name), local.initialization);
			indent++;
		}
		
		@Override public void visitAnnotationOnLocal(LocalDeclaration local, Node node, Annotation annotation) {
			print("<ANNOTATION: %s />", annotation);
		}
		
		@Override public void endVisitLocal(Node node, LocalDeclaration local) {
			indent--;
			print("</LOCAL %s %s>", str(local.type), str(local.name));
		}
		
		@Override public void visitStatement(Node node, Statement statement) {
			print("<%s>", statement.getClass());
			indent++;
			print("%s", statement);
		}
		
		@Override public void endVisitStatement(Node node, Statement statement) {
			indent--;
			print("</%s>", statement.getClass());
		}
	}
}
