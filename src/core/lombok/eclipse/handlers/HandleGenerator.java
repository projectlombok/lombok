/*
 * Copyright (C) 2023 The Project Lombok Authors.
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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Generator;
import lombok.spi.Provides;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

@Provides
@DeferUntilPostDiet
public class HandleGenerator extends EclipseAnnotationHandler<Generator> {
	public void handle(AnnotationValues<Generator> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.GENERATOR_FLAG_USAGE, "@Generator");

        EclipseNode methodNode = annotationNode.up();
        if (methodNode.getKind() != Kind.METHOD) {
            annotationNode.addError("@Generator can only be applied to method");
            return;
        }

        intoGeneratorMethod(ast, methodNode);

		methodNode.rebuild();
	}

	private static TypeReference extractIteratorType(TypeReference typeReference) {
		TypeReference[][] typeArguments = typeReference.getTypeArguments();

		if (typeArguments.length > 0) {
			TypeReference[] lastArguments = typeArguments[typeArguments.length - 1];
			if (lastArguments.length > 0) {
				return lastArguments[0];
			}
		}

		return generateQualifiedTypeRef(typeReference, TypeConstants.JAVA_LANG_OBJECT);
	}

	private static void checkMethod(final EclipseNode node, final String innerClassName) {
		MethodDeclaration methodDecl = (MethodDeclaration) node.get();

        if (methodDecl.thrownExceptions != null && methodDecl.thrownExceptions.length > 0) {
            node.addWarning("throws in generator method does nothing");
        }

        if ((methodDecl.modifiers & ClassFileConstants.AccSynchronized) != 0) {
            node.addError("Generator method cannot be synchronized");
        }

		methodDecl.traverse(new ASTVisitor() {
			private int synchronizedDepth = 0;

			@Override public boolean visit(MessageSend messageSend, BlockScope scope) {
				String name = new String(messageSend.selector);
				if (
					synchronizedDepth > 0
					&& messageSend.receiverIsImplicitThis()
					&& ("yieldThis".equals(name) || "yieldAll".equals(name))
					&& messageSend.arguments != null
					&& messageSend.arguments.length == 1
				) {
					node.addError(
						"Cannot yield inside synchronized block",
						messageSend.sourceStart,
						messageSend.sourceEnd
					);
				}

				if (
					messageSend.receiverIsImplicitThis()
					&& "advance".equals(name)
					&& (messageSend.arguments == null || messageSend.arguments.length == 0)
				) {
					node.addError(
						"Cannot call advance method directly in generater method",
						messageSend.sourceStart,
						messageSend.sourceEnd
					);
				}

				return super.visit(messageSend, scope);
			}

			// Ignore local class
			@Override public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
				return false;
			}

			private void checkClassName(ASTNode targetNode, String token) {
				if (innerClassName.equals(token)) {
					node.addError(
						innerClassName + " is used internally in generater method",
						targetNode.sourceStart,
						targetNode.sourceEnd
					);
				}
			}

			@Override public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
				checkClassName(singleTypeReference, new String(singleTypeReference.token));

				return super.visit(singleTypeReference, scope);
			}

			@Override public boolean visit(SynchronizedStatement synchronizedStatement, BlockScope scope) {
				synchronizedDepth++;
				return super.visit(synchronizedStatement, scope);
			}

			@Override public void endVisit(SynchronizedStatement synchronizedStatement, BlockScope scope) {
				synchronizedDepth--;
				super.endVisit(synchronizedStatement, scope);
			}
		}, (ClassScope) null);
	}

	private static void intoGeneratorMethod(ASTNode ast, EclipseNode sourceMethod) {
		MethodDeclaration methodDecl = (MethodDeclaration) sourceMethod.get();
		TypeDeclaration typeDecl = (TypeDeclaration) sourceMethod.up().get();

		String className = "__Generator";

		checkMethod(sourceMethod, className);

		TypeDeclaration generatorClass = createGeneratorClass(
			ast,
			new SingleTypeReference(typeDecl.name, 0),
			methodDecl.compilationResult,
			className,
			extractIteratorType(methodDecl.returnType),
			methodDecl.statements
		);

		AllocationExpression newExpr = new AllocationExpression();
		newExpr.type = new SingleTypeReference(generatorClass.name, 0);

		ReturnStatement returnStatement = new ReturnStatement(newExpr, 0, 0);
		returnStatement.traverse(new SetGeneratedByVisitor(ast), null);

		methodDecl.statements = new Statement[] {
			generatorClass,
			returnStatement
		};
		methodDecl.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
	}

	private static TypeDeclaration createGeneratorClass(
		ASTNode ast,
		TypeReference classType,
		CompilationResult result,
		String name,
		TypeReference type,
		Statement[] statements
	) {
		TypeDeclaration innerClass = new TypeDeclaration(result);
		innerClass.name = name.toCharArray();

		innerClass.modifiers |= ClassFileConstants.AccFinal;
		innerClass.superclass = new ParameterizedQualifiedTypeReference(
			new char[][] { "lombok".toCharArray(), "Lombok".toCharArray(), "Generator".toCharArray() },
			new TypeReference[][] { null, null, new TypeReference[] { copyType(type) } },
			0,
			new long[3]
		);

		innerClass.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;

		MethodDeclaration advanceMethod = new MethodDeclaration(result);
		advanceMethod.modifiers = ClassFileConstants.AccProtected;
		advanceMethod.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		advanceMethod.annotations = null;
		advanceMethod.arguments = null;
		advanceMethod.selector = "advance".toCharArray();
		advanceMethod.binding = null;
		advanceMethod.thrownExceptions = null;
		advanceMethod.typeParameters = null;

		innerClass.methods = new AbstractMethodDeclaration[] {
			innerClass.createDefaultConstructor(false, false),
			advanceMethod
		};

		innerClass.traverse(new SetGeneratedByVisitor(ast), (ClassScope) null);

		remapThisSuper(classType, statements);
		advanceMethod.statements = statements;

		return innerClass;
	}

	private static void remapThisSuper(final TypeReference classType, Statement[] statements) {
		ASTVisitor remapper = new ASTVisitor() {
			private Expression updateReceiver(ASTNode node, Expression expression) {
				Class<?> clazz = expression.getClass();

				if (clazz == SuperReference.class) {
					return new QualifiedSuperReference(
						copyType(classType, node),
						node.sourceStart,
						node.sourceEnd
					);
				} else if (clazz == ThisReference.class && !expression.isImplicitThis()) {
					return new QualifiedThisReference(
						copyType(classType, node),
						node.sourceStart,
						node.sourceEnd
					);
				}

				return null;
			}

			@Override public boolean visit(MessageSend messageSend, BlockScope scope) {
				Expression updated = updateReceiver(messageSend, messageSend.receiver);
				if (updated != null) {
					messageSend.receiver = updated;
				}

				return super.visit(messageSend, scope);
			}

			@Override public boolean visit(FieldReference fieldReference, BlockScope scope) {
				Expression updated = updateReceiver(fieldReference, fieldReference.receiver);
				if (updated != null) {
					fieldReference.receiver = updated;
				}

				return super.visit(fieldReference, scope);
			}
		};

		for (Statement statement : statements) {
			statement.traverse(remapper, null);
		}
	}
}
