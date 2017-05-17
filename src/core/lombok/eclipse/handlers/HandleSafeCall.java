/*
 * Copyright (C) 2010-2016 The Project Lombok Authors.
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

import lombok.core.HandlerPriority;
import lombok.core.handlers.SafeCallIllegalUsingException;
import lombok.core.handlers.SafeCallIllegalUsingException.Place;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseASTAdapter;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseNode;
import lombok.experimental.SafeCall;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.mangosdk.spi.ProviderFor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.STATIC;
import static java.util.Arrays.asList;
import static lombok.core.handlers.SafeCallIllegalUsingException.Place.*;
import static lombok.core.handlers.SafeCallIllegalUsingException.unsupportedPlaceMessage;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.getParent;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.insertBlockAfterVariable;
import static lombok.eclipse.Eclipse.getEcjCompilerVersion;
import static lombok.eclipse.EclipseAugments.ASTNode_parentNode;
import static lombok.eclipse.handlers.EclipseHandlerUtil.copySourcePosition;
import static lombok.eclipse.handlers.EclipseHandlerUtil.typeMatches;


@ProviderFor(EclipseASTVisitor.class)
@DeferUntilPostDiet
@HandlerPriority(65536 + 1)
public class HandleSafeCall extends EclipseASTAdapter {

	public static void registerBlock(AbstractVariableDeclaration var, Block block) {
		ASTNode_parentNode.set(block, var);
	}

	public static void elivisation(AbstractVariableDeclaration local, EclipseNode variable) {
		Place illegalPlace = checkIllegalUsing(local, variable);
		if (illegalPlace != null) {
			addIllegalPlaceError(variable, illegalPlace);
			return;
		}

		if (local.initialization == null) {
			return;
		}

		Block initBlock = newStubBlock(local);
		if (initBlock == null) return;


		ASTNode parentNode;
		EclipseNode parent = variable.up();
		try {
			parentNode = getParentASTNode(local, parent);
		} catch (SafeCallIllegalUsingException e) {
			addIllegalPlaceError(variable, e.getPlace());
			return;
		} catch (SafeCallUnexpectedStateException e) {
			String message = e.getMessage();
			variable.addError(message);
			return;
		}

		insertBlockAfterVariable(local, initBlock, parentNode);

		registerBlock(local, initBlock);
		parent.rebuild();
	}

	private static Place checkIllegalUsing(AbstractVariableDeclaration local, EclipseNode variable) {
		EclipseNode parentENode = variable.up();
		ASTNode parent = parentENode.get();
		Place illegalPlace = null;
		if (parent instanceof ForStatement && asList(((ForStatement) parent).initializations).contains(local)) {
			illegalPlace = forLoopInitializer;
		} else if (parent instanceof ForeachStatement && ((ForeachStatement) parent).elementVariable == local) {
			illegalPlace = forLoopVariable;
		} else if (parent instanceof TryStatement) {
			List<LocalDeclaration> resources = asList(getResources((TryStatement) parent));
			if (resources.contains(local)) illegalPlace = tryResource;
		}
		return illegalPlace;
	}

	private static void addIllegalPlaceError(EclipseNode variable, Place place) {
		variable.addError(unsupportedPlaceMessage(place));
	}

	private static ASTNode getParentASTNode(
			AbstractVariableDeclaration local, EclipseNode root
	) throws SafeCallIllegalUsingException {
		ASTNode parentASTNode = getParent(local, root.get());
		if (parentASTNode == null) throw new IllegalStateException("cannot find parent block for variable " + local);
		return parentASTNode;
	}

	private static ASTNode getParent(
			AbstractVariableDeclaration variable, ASTNode root
	) throws SafeCallIllegalUsingException {
		if (root instanceof Block) {
			return getParent(variable, root, ((Block) root).statements);
		} else if (root instanceof AbstractMethodDeclaration) {
			return getParent(variable, root, ((AbstractMethodDeclaration) root).statements);
		} else if (root instanceof TypeDeclaration) {
			return root;
		} else if (root instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) root;
			return getParent(variable, root, ifStatement.thenStatement, ifStatement.elseStatement);
		} else if (root instanceof ForStatement) {
			ForStatement forStatement = (ForStatement) root;

			ASTNode parent = getParent(variable, root, forStatement.initializations);
			if (parent != null) throw new SafeCallIllegalUsingException(forLoopInitializer, variable);

			return getParent(variable, root, forStatement.action);
		} else if (root instanceof ForeachStatement) {
			ForeachStatement forStatement = (ForeachStatement) root;

			ASTNode parent = getParent(variable, root, forStatement.elementVariable);
			if (parent != null) throw new SafeCallIllegalUsingException(forLoopVariable, variable);

			return getParent(variable, root, forStatement.action);
		} else if (root instanceof WhileStatement) {
			return getParent(variable, root, ((WhileStatement) root).action);
		} else if (root instanceof DoStatement) {
			return getParent(variable, root, ((DoStatement) root).action);
		} else if (root instanceof TryStatement) {
			TryStatement tryStatement = (TryStatement) root;
			LocalDeclaration[] resources = getResources(tryStatement);
			for (LocalDeclaration resource : resources) {
				ASTNode parent = getParent(variable, root, resource);
				if (parent != null) throw new SafeCallIllegalUsingException(tryResource, variable);

			}
			ASTNode parent = getParent(variable, root, tryStatement.tryBlock);
			Block[] catchBlocks = tryStatement.catchBlocks;
			if (parent == null && catchBlocks != null) {
				parent = getParent(variable, root, catchBlocks);
			}
			Block finallyBlock = tryStatement.finallyBlock;
			if (parent == null && finallyBlock != null) {
				parent = getParent(variable, root, finallyBlock);
			}
			return parent;
		} else if (root instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement) root;

			ASTNode parent = getParent(variable, root, switchStatement.statements);
			if (parent != null) {
				return root;
			} else {
				return null;
			}
		} else if (root instanceof CaseStatement) {
			return null;
		} else if (root instanceof LocalDeclaration) {
			return root == variable ? root : null;
		} else if (isLambda(root)) {
			return getParent(variable, getLambdaBody(root));
		} else if (root instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) root;
			return getParent(variable, synchronizedStatement.block);
		} else {
			throw new SafeCallUnexpectedStateException(getParent, variable, root.getClass());
		}
	}

	private static ASTNode getLambdaBody(ASTNode root) {
		Method body;
		try {
			body = root.getClass().getMethod("body");
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
		try {
			return (ASTNode) body.invoke(root);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean isLambda(ASTNode expr) {
		return expr != null && expr.getClass().getSimpleName().equals("LambdaExpression");
	}

	private static LocalDeclaration[] getResources(TryStatement tryStatement) {
		LocalDeclaration[] resources;
		if (getEcjCompilerVersion() > 6) {
			Field getResources;
			try {
				getResources = TryStatement.class.getField("resources");
			} catch (NoSuchFieldException e) {
				throw new IllegalStateException(e);
			}
			try {
				resources = (LocalDeclaration[]) getResources.get(tryStatement);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		} else resources = new LocalDeclaration[0];
		return resources;
	}


	private static ASTNode getParent(
			AbstractVariableDeclaration local,
			ASTNode parent, Statement... statements
	) throws SafeCallIllegalUsingException {
		List<Statement> childBlocks = new ArrayList<Statement>();
		for (Statement statement : statements) {
			if (statement == local) {
				return parent;
			} else if (!(statement instanceof AbstractVariableDeclaration)) childBlocks.add(statement);
		}
		for (Statement statement : childBlocks) {
			ASTNode possibleParent = getParent(local, statement);
			if (possibleParent != null) {
				return possibleParent;
			}
		}
		return null;
	}

	private static void insertBlockAfterVariable(
			AbstractVariableDeclaration local, Block initBlock, ASTNode parentNode
	) {
		if (parentNode instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration md = (AbstractMethodDeclaration) parentNode;
			md.statements = insertInitBlockAfterVariable(initBlock, local, md.statements);
		} else if (parentNode instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) parentNode;
			FieldDeclaration[] fields = td.fields;
			FieldDeclaration[] newFields = new FieldDeclaration[fields.length + 1];
			for (int i = 0, j = 0; i < fields.length; i++, j++) {
				FieldDeclaration field = fields[i];
				newFields[j] = field;
				if (local == field) {
					boolean isStatic = (field.modifiers & STATIC) != 0;
					newFields[++j] = new Initializer(initBlock, isStatic ? STATIC : 0);
				}
			}
			td.fields = newFields;
		} else if (parentNode instanceof Block) {
			Block block = (Block) parentNode;
			block.statements = insertInitBlockAfterVariable(initBlock, local, block.statements);
		} else if (parentNode instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement) parentNode;
			switchStatement.statements = insertInitBlockAfterVariable(initBlock, local, switchStatement.statements);
		} else {
			throw new SafeCallUnexpectedStateException(insertBlockAfterVariable, local, parentNode.getClass());
		}
	}

	private static Statement[] insertInitBlockAfterVariable(
			Block initBlock, AbstractVariableDeclaration local, Statement[] statements) {
		Statement[] newStatements = new Statement[statements.length + 1];
		for (int i = 0, j = 0; i < statements.length; i++, j++) {
			Statement st = statements[i];
			newStatements[j] = st;
			if (local == st) {
				newStatements[++j] = initBlock;
			}
		}
		return newStatements;
	}

	public static Block newStubBlock(AbstractVariableDeclaration varDecl) {
		Expression expr = varDecl.initialization;
		if (expr == null) return null;

		Block block = new Block(1);
		copySourcePosition(varDecl, block);
		block.statements = new Statement[0];
		return block;
	}

	static boolean isSafe(EclipseNode fieldNode, AbstractVariableDeclaration field) {
		Annotation[] annotations = field.annotations;
		if (annotations != null) for (Annotation annotation : annotations) {
			if (typeMatches(SafeCall.class, fieldNode, annotation.type)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void visitLocal(EclipseNode localNode, LocalDeclaration local) {
		visit(localNode, local);
	}

	@Override
	public void visitField(EclipseNode fieldNode, FieldDeclaration field) {
		visit(fieldNode, field);
	}

	public void visit(EclipseNode fieldNode, AbstractVariableDeclaration field) {
		if (isSafe(fieldNode, field)) elivisation(field, fieldNode);
	}
}
