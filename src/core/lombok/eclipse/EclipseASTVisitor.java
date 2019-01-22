/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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
package lombok.eclipse;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.io.PrintStream;
import java.lang.reflect.Modifier;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Implement so you can ask any EclipseAST.Node to traverse depth-first through all children,
 * calling the appropriate visit and endVisit methods.
 */
public interface EclipseASTVisitor {
	/**
	 * Called at the very beginning and end.
	 */
	void visitCompilationUnit(EclipseNode top, CompilationUnitDeclaration unit);
	void endVisitCompilationUnit(EclipseNode top, CompilationUnitDeclaration unit);
	
	/**
	 * Called when visiting a type (a class, interface, annotation, enum, etcetera).
	 */
	void visitType(EclipseNode typeNode, TypeDeclaration type);
	void visitAnnotationOnType(TypeDeclaration type, EclipseNode annotationNode, Annotation annotation);
	void endVisitType(EclipseNode typeNode, TypeDeclaration type);
	
	/**
	 * Called when visiting a field of a class.
	 * Even though in Eclipse initializers (both instance and static) are represented as Initializer objects,
	 * which are a subclass of FieldDeclaration, those do NOT result in a call to this method. They result
	 * in a call to the visitInitializer method.
	 */
	void visitField(EclipseNode fieldNode, FieldDeclaration field);
	void visitAnnotationOnField(FieldDeclaration field, EclipseNode annotationNode, Annotation annotation);
	void endVisitField(EclipseNode fieldNode, FieldDeclaration field);
	
	/**
	 * Called for static and instance initializers. You can tell the difference via the modifier flag on the
	 * ASTNode (8 for static, 0 for not static). The content is in the 'block', not in the 'initialization',
	 * which would always be null for an initializer instance.
	 */
	void visitInitializer(EclipseNode initializerNode, Initializer initializer);
	void endVisitInitializer(EclipseNode initializerNode, Initializer initializer);
	
	/**
	 * Called for both methods (MethodDeclaration) and constructors (ConstructorDeclaration), but not for
	 * Clinit objects, which are a vestigial Eclipse thing that never contain anything. Static initializers
	 * show up as 'Initializer', in the visitInitializer method, with modifier bit STATIC set.
	 */
	void visitMethod(EclipseNode methodNode, AbstractMethodDeclaration method);
	void visitAnnotationOnMethod(AbstractMethodDeclaration method, EclipseNode annotationNode, Annotation annotation);
	void endVisitMethod(EclipseNode methodNode, AbstractMethodDeclaration method);
	
	/**
	 * Visits a method argument
	 */
	void visitMethodArgument(EclipseNode argNode, Argument arg, AbstractMethodDeclaration method);
	void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, EclipseNode annotationNode, Annotation annotation);
	void endVisitMethodArgument(EclipseNode argNode, Argument arg, AbstractMethodDeclaration method);
	
	/**
	 * Visits a local declaration - that is, something like 'int x = 10;' on the method level.
	 */
	void visitLocal(EclipseNode localNode, LocalDeclaration local);
	void visitAnnotationOnLocal(LocalDeclaration local, EclipseNode annotationNode, Annotation annotation);
	void endVisitLocal(EclipseNode localNode, LocalDeclaration local);
	
	/**
	 * Visits a node that represents a type reference. Anything from {@code int} to {@code T} to {@code foo,.pkg.Bar<T>.Baz<?> @Ann []}.
	 */
	void visitTypeUse(EclipseNode typeUseNode, TypeReference typeUse);
	void visitAnnotationOnTypeUse(TypeReference typeUse, EclipseNode annotationNode, Annotation annotation);
	void endVisitTypeUse(EclipseNode typeUseNode, TypeReference typeUse);
	
	/**
	 * Visits a statement that isn't any of the other visit methods (e.g. TypeDeclaration).
	 */
	void visitStatement(EclipseNode statementNode, Statement statement);
	void endVisitStatement(EclipseNode statementNode, Statement statement);
	
	/**
	 * Prints the structure of an AST.
	 */
	public static class Printer implements EclipseASTVisitor {
		private final PrintStream out;
		private final boolean printContent;
		private int disablePrinting = 0;
		private int indent = 0;
		private boolean printClassNames = false;
		private final boolean printPositions;
		
		public boolean deferUntilPostDiet() {
			return false;
		}
		
		/**
		 * @param printContent if true, bodies are printed directly, as java code,
		 * instead of a tree listing of every AST node inside it.
		 */
		public Printer(boolean printContent) {
			this(printContent, System.out, false);
		}
		
		/**
		 * @param printContent if true, bodies are printed directly, as java code,
		 * instead of a tree listing of every AST node inside it.
		 * @param out write output to this stream. You must close it yourself. flush() is called after every line.
		 * 
		 * @see java.io.PrintStream#flush()
		 */
		public Printer(boolean printContent, PrintStream out, boolean printPositions) {
			this.printContent = printContent;
			this.out = out;
			this.printPositions = printPositions;
		}
		
		private void forcePrint(String text, Object... params) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indent; i++) sb.append("  ");
			sb.append(text);
			Object[] t;
			if (printClassNames && params.length > 0) {
				sb.append(" [");
				for (int i = 0; i < params.length; i++) {
					if (i > 0) sb.append(", ");
					sb.append("%s");
				}
				sb.append("]");
				t = new Object[params.length + params.length];
				for (int i = 0; i < params.length; i++) {
					t[i] = params[i];
					t[i + params.length] = (params[i] == null) ? "NULL " : params[i].getClass();
				}
			} else {
				t = params;
			}
			sb.append("\n");
			out.printf(sb.toString(), t);
			out.flush();
		}
		
		private void print(String text, Object... params) {
			if (disablePrinting == 0) forcePrint(text, params);
		}
		
		private String str(char[] c) {
			if (c == null) return "(NULL)";
			return new String(c);
		}
		
		private String str(TypeReference type) {
			if (type == null) return "(NULL)";
			char[][] c = type.getTypeName();
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (char[] d : c) {
				sb.append(first ? "" : ".").append(new String(d));
				first = false;
			}
			return sb.toString();
		}
		
		public void visitCompilationUnit(EclipseNode node, CompilationUnitDeclaration unit) {
			out.println("---------------------------------------------------------");
			out.println(node.isCompleteParse() ? "COMPLETE" : "incomplete");
			
			print("<CUD %s%s%s>", node.getFileName(), isGenerated(unit) ? " (GENERATED)" : "", position(node));
			indent++;
		}
		
		public void endVisitCompilationUnit(EclipseNode node, CompilationUnitDeclaration unit) {
			indent--;
			print("</CUD>");
		}
		
		private String printFlags(int flags, ASTNode node) {
			StringBuilder out = new StringBuilder();
			if ((flags & ClassFileConstants.AccPublic) != 0) {
				flags &= ~ClassFileConstants.AccPublic;
				out.append("public ");
			}
			if ((flags & ClassFileConstants.AccPrivate) != 0) {
				flags &= ~ClassFileConstants.AccPrivate;
				out.append("private ");
			}
			if ((flags & ClassFileConstants.AccProtected) != 0) {
				flags &= ~ClassFileConstants.AccProtected;
				out.append("protected ");
			}
			if ((flags & ClassFileConstants.AccStatic) != 0) {
				flags &= ~ClassFileConstants.AccStatic;
				out.append("static ");
			}
			if ((flags & ClassFileConstants.AccFinal) != 0) {
				flags &= ~ClassFileConstants.AccFinal;
				out.append("final ");
			}
			if ((flags & ClassFileConstants.AccSynchronized) != 0) {
				flags &= ~ClassFileConstants.AccSynchronized;
				out.append("synchronized ");
			}
			if ((flags & ClassFileConstants.AccNative) != 0) {
				flags &= ~ClassFileConstants.AccNative;
				out.append("native ");
			}
			if ((flags & ClassFileConstants.AccInterface) != 0) {
				flags &= ~ClassFileConstants.AccInterface;
				out.append("interface ");
			}
			if ((flags & ClassFileConstants.AccAbstract) != 0) {
				flags &= ~ClassFileConstants.AccAbstract;
				out.append("abstract ");
			}
			if ((flags & ClassFileConstants.AccStrictfp) != 0) {
				flags &= ~ClassFileConstants.AccStrictfp;
				out.append("strictfp ");
			}
			if ((flags & ClassFileConstants.AccSynthetic) != 0) {
				flags &= ~ClassFileConstants.AccSynthetic;
				out.append("synthetic ");
			}
			if ((flags & ClassFileConstants.AccAnnotation) != 0) {
				flags &= ~ClassFileConstants.AccAnnotation;
				out.append("annotation ");
			}
			if ((flags & ClassFileConstants.AccEnum) != 0) {
				flags &= ~ClassFileConstants.AccEnum;
				out.append("enum ");
			}
			
			if ((flags & ClassFileConstants.AccVolatile) != 0) {
				flags &= ~ClassFileConstants.AccVolatile;
				if (node instanceof FieldDeclaration) out.append("volatile ");
				else out.append("volatile/bridge ");
			}
			if ((flags & ClassFileConstants.AccTransient) != 0) {
				flags &= ~ClassFileConstants.AccTransient;
				if (node instanceof Argument) out.append("varargs ");
				else if (node instanceof FieldDeclaration) out.append("transient ");
				else out.append("transient/varargs ");
			}
			
			if (flags != 0) {
				out.append(String.format(" 0x%08X ", flags));
			}
			
			return out.toString().trim();
		}
		
		public void visitType(EclipseNode node, TypeDeclaration type) {
			print("<TYPE %s%s%s> %s", str(type.name), isGenerated(type) ? " (GENERATED)" : "", position(node), printFlags(type.modifiers, type));
			indent++;
			if (printContent) {
				print("%s", type);
				disablePrinting++;
			}
		}
		
		public void visitAnnotationOnType(TypeDeclaration type, EclipseNode node, Annotation annotation) {
			forcePrint("<ANNOTATION%s: %s%s />", isGenerated(annotation) ? " (GENERATED)" : "", annotation, position(node));
		}
		
		public void endVisitType(EclipseNode node, TypeDeclaration type) {
			if (printContent) disablePrinting--;
			indent--;
			print("</TYPE %s>", str(type.name));
		}
		
		public void visitInitializer(EclipseNode node, Initializer initializer) {
			Block block = initializer.block;
			boolean s = (block != null && block.statements != null);
			print("<%s INITIALIZER: %s%s%s>",
					(initializer.modifiers & Modifier.STATIC) != 0 ? "static" : "instance",
							s ? "filled" : "blank",
							isGenerated(initializer) ? " (GENERATED)" : "", position(node));
			indent++;
			if (printContent) {
				if (initializer.block != null) print("%s", initializer.block);
				disablePrinting++;
			}
		}
		
		public void endVisitInitializer(EclipseNode node, Initializer initializer) {
			if (printContent) disablePrinting--;
			indent--;
			print("</%s INITIALIZER>", (initializer.modifiers & Modifier.STATIC) != 0 ? "static" : "instance");
		}
		
		public void visitField(EclipseNode node, FieldDeclaration field) {
			print("<FIELD%s %s %s = %s%s> %s", isGenerated(field) ? " (GENERATED)" : "",
					str(field.type), str(field.name), field.initialization, position(node), printFlags(field.modifiers, field));
			indent++;
			if (printContent) {
				if (field.initialization != null) print("%s", field.initialization);
				disablePrinting++;
			}
		}
		
		public void visitAnnotationOnField(FieldDeclaration field, EclipseNode node, Annotation annotation) {
			forcePrint("<ANNOTATION%s: %s%s />", isGenerated(annotation) ? " (GENERATED)" : "", annotation, position(node));
		}
		
		public void endVisitField(EclipseNode node, FieldDeclaration field) {
			if (printContent) disablePrinting--;
			indent--;
			print("</FIELD %s %s>", str(field.type), str(field.name));
		}
		
		public void visitMethod(EclipseNode node, AbstractMethodDeclaration method) {
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			print("<%s %s: %s%s%s> %s", type, str(method.selector), method.statements != null ? ("filled(" + method.statements.length + ")") : "blank",
					isGenerated(method) ? " (GENERATED)" : "", position(node), printFlags(method.modifiers, method));
			indent++;
			if (method instanceof ConstructorDeclaration) {
				ConstructorDeclaration cd = (ConstructorDeclaration) method;
				print("--> constructorCall: %s", cd.constructorCall == null ? "-NONE-" : cd.constructorCall);
			}
			if (printContent) {
				if (method.statements != null) print("%s", method);
				disablePrinting++;
			}
		}
		
		public void visitAnnotationOnMethod(AbstractMethodDeclaration method, EclipseNode node, Annotation annotation) {
			forcePrint("<ANNOTATION%s: %s%s>", isGenerated(method) ? " (GENERATED)" : "", annotation, position(node));
			if (annotation instanceof MarkerAnnotation || disablePrinting != 0) {
				forcePrint("<ANNOTATION%s: %s%s />", isGenerated(method) ? " (GENERATED)" : "", annotation, position(node));
			} else {
				forcePrint("<ANNOTATION%s: %s%s>", isGenerated(method) ? " (GENERATED)" : "", annotation, position(node));
				indent++;
				if (annotation instanceof SingleMemberAnnotation) {
					Expression expr = ((SingleMemberAnnotation) annotation).memberValue;
					print("<SINGLE-MEMBER-VALUE %s /> %s", expr.getClass(), expr);
				}
				if (annotation instanceof NormalAnnotation) {
					for (MemberValuePair mvp : ((NormalAnnotation) annotation).memberValuePairs) {
						print("<Member %s: %s /> %s", new String(mvp.name), mvp.value.getClass(), mvp.value);
					}
				}
				indent--;
			}
		}
		
		public void endVisitMethod(EclipseNode node, AbstractMethodDeclaration method) {
			if (printContent) disablePrinting--;
			String type = method instanceof ConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
			indent--;
			print("</%s %s>", type, str(method.selector));
		}
		
		public void visitMethodArgument(EclipseNode node, Argument arg, AbstractMethodDeclaration method) {
			print("<METHODARG%s %s %s = %s%s> %s", isGenerated(arg) ? " (GENERATED)" : "",
				str(arg.type), str(arg.name), arg.initialization, position(node), printFlags(arg.modifiers, arg));
			indent++;
		}
		
		public void visitAnnotationOnMethodArgument(Argument arg, AbstractMethodDeclaration method, EclipseNode node, Annotation annotation) {
			print("<ANNOTATION%s: %s%s />", isGenerated(annotation) ? " (GENERATED)" : "", annotation, position(node));
		}
		
		public void endVisitMethodArgument(EclipseNode node, Argument arg, AbstractMethodDeclaration method) {
			indent--;
			print("</METHODARG %s %s>", str(arg.type), str(arg.name));
		}
		
		public void visitLocal(EclipseNode node, LocalDeclaration local) {
			print("<LOCAL%s %s %s = %s%s> %s", isGenerated(local) ? " (GENERATED)" : "",
				str(local.type), str(local.name), local.initialization, position(node), printFlags(local.modifiers, local));
			indent++;
		}
		
		public void visitAnnotationOnLocal(LocalDeclaration local, EclipseNode node, Annotation annotation) {
			print("<ANNOTATION%s: %s />", isGenerated(annotation) ? " (GENERATED)" : "", annotation);
		}
		
		public void endVisitLocal(EclipseNode node, LocalDeclaration local) {
			indent--;
			print("</LOCAL %s %s>", str(local.type), str(local.name));
		}
		
		@Override public void visitTypeUse(EclipseNode typeUseNode, TypeReference typeUse) {
			print("<TYPE %s>", typeUse.getClass());
			indent++;
			print("%s", typeUse);
		}
		
		@Override public void visitAnnotationOnTypeUse(TypeReference typeUse, EclipseNode annotationNode, Annotation annotation) {
			print("<ANNOTATION%s: %s />", isGenerated(annotation) ? " (GENERATED)" : "", annotation);
		}
		
		@Override public void endVisitTypeUse(EclipseNode typeUseNode, TypeReference typeUse) {
			indent--;
			print("</TYPE %s>", typeUse.getClass());
		}
		
		public void visitStatement(EclipseNode node, Statement statement) {
			print("<%s%s%s>", statement.getClass(), isGenerated(statement) ? " (GENERATED)" : "", position(node));
			if (statement instanceof AllocationExpression) {
				AllocationExpression alloc = (AllocationExpression) statement;
				print(" --> arguments: %s", alloc.arguments == null ? "NULL" : alloc.arguments.length);
				print(" --> genericTypeArguments: %s", alloc.genericTypeArguments == null ? "NULL" : alloc.genericTypeArguments.length);
				print(" --> typeArguments: %s", alloc.typeArguments == null ? "NULL" : alloc.typeArguments.length);
				print(" --> enumConstant: %s", alloc.enumConstant);
				print(" --> inferredReturnType: %s", alloc.inferredReturnType);
			}
			indent++;
			print("%s", statement);
		}
		
		public void endVisitStatement(EclipseNode node, Statement statement) {
			indent--;
			print("</%s>", statement.getClass());
		}
		
		String position(EclipseNode node) {
			if (!printPositions) return "";
			int start = node.get().sourceStart();
			int end = node.get().sourceEnd();
			return String.format(" [%d, %d]", start, end);
		}

		public boolean isDeferUntilPostDiet() {
			return false;
		}
	}

	boolean isDeferUntilPostDiet();
}
