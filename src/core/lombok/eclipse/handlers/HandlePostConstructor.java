/*
 * Copyright (C) 2022 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.experimental.PostConstructor;
import lombok.experimental.PostConstructor.InvokePostConstructors;
import lombok.experimental.PostConstructor.SkipPostConstructors;
import lombok.spi.Provides;

/**
 * Handles the {@code lombok.experimental.PostConstructor} annotation for eclipse.
 */
@DeferUntilPostDiet
@Provides
@HandlerPriority(value = 1024)
public class HandlePostConstructor extends EclipseAnnotationHandler<PostConstructor> {
	private HandleConstructor handleConstructor = new HandleConstructor();
	
	@Override public void preHandle(AnnotationValues<PostConstructor> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode typeNode = upToTypeNode(annotationNode);
		handleConstructor.generateConstructor(typeNode, AccessLevel.PUBLIC, Collections.<EclipseNode>emptyList(), false, null, SkipIfConstructorExists.YES, Collections.<Annotation>emptyList(), annotationNode);
	}
	
	@Override public void handle(AnnotationValues<PostConstructor> annotation, Annotation source, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.POST_CONSTRUCTOR_FLAG_USAGE, "@PostConstructor");
		
		EclipseNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration)) {
			annotationNode.addError("@PostConstructor is legal only on methods.");
			return;
		}
		
		MethodDeclaration method = (MethodDeclaration) methodNode.get();
		if (method.isAbstract()) {
			annotationNode.addError("@PostConstructor is legal only on concrete methods.");
			return;
		}
		
		if (method.isStatic()) {
			annotationNode.addError("@PostConstructor is legal only on instance methods.");
			return;
		}
		
		if (method.arguments != null) {
			annotationNode.addError("@PostConstructor is legal only on methods without parameters.");
			return;
		}
		
		EclipseNode typeNode = upToTypeNode(annotationNode);
		
		handleConstructor.generateConstructor(typeNode, AccessLevel.PUBLIC, Collections.<EclipseNode>emptyList(), false, null, SkipIfConstructorExists.YES, Collections.<Annotation>emptyList(), annotationNode);
		
		List<EclipseNode> constructors = findConstructors(typeNode);
		for (EclipseNode constructorNode : constructors) {
			ConstructorDeclaration constructor = (ConstructorDeclaration) constructorNode.get();
			
			boolean hasSkip = hasAnnotation(SkipPostConstructors.class, constructorNode);
			boolean hasInvoke = hasAnnotation(InvokePostConstructors.class, constructorNode);
			boolean hasThisCall = constructor.constructorCall != null && constructor.constructorCall.accessMode == ExplicitConstructorCall.This;
			
			if (hasSkip) {
				if (hasInvoke) {
					constructorNode.addError("@InvokePostConstructors and @SkipPostConstructors are mutually exclusive.");
				}
				if (hasThisCall) {
					constructorNode.addWarning("@SkipPostConstructors is not needed; constructors calling this(...) are skipped anyway.");
				}
				continue;
			}
			
			if (hasThisCall && !hasInvoke) {
				continue;
			}
			
			if (!isGenerated(constructor) && !hasInvoke) {
				constructorNode.addError("Constructor needs to be annotated with @PostConstruct.(Invoke|Skip)PostConstructors or needs to start with a this(...) call.");
				continue;
			};
			
			MessageSend call = new MessageSend();
			call.selector = method.selector;
			call.receiver = ThisReference.implicitThis();
			call.traverse(new SetGeneratedByVisitor(source), (BlockScope) null);
			
			Block block = getOrCreatePostConstructorBlock(constructor, source);
			if (block != null) {
				block.statements = concat(block.statements, new Statement[] {call}, Statement.class);
			} else {
				constructor.statements = concat(constructor.statements, new Statement[] {call}, Statement.class);
			}
			
			constructor.thrownExceptions = concat(constructor.thrownExceptions, method.thrownExceptions, TypeReference.class);
			
			constructorNode.rebuild();
		}
	}
	
	private List<EclipseNode> findConstructors(EclipseNode typeNode) {
		List<EclipseNode> constructors = new ArrayList<EclipseNode>();
		
		for (EclipseNode eclipseNode : typeNode.down()) {
			if (eclipseNode.getKind() != Kind.METHOD) continue;
			if (!(eclipseNode.get() instanceof ConstructorDeclaration)) continue;
			constructors.add(eclipseNode);
		}
		
		return constructors;
	}
	
	private Block getOrCreatePostConstructorBlock(ConstructorDeclaration constructor, ASTNode source) {
		// Generated constructors and methods without return cannot exit early
		if (isGenerated(constructor) || !containsReturn(constructor)) {
			return null;
		}
		
		// Search existing try ... catch ... finally and return 
		Statement[] statements = constructor.statements;
		if (statements.length > 0 && statements[0] instanceof LocalDeclaration && isGenerated(statements[0])) {
			LocalDeclaration localDeclaration = (LocalDeclaration) statements[0];
			if (Arrays.equals("$callPostConstructor".toCharArray(), localDeclaration.name)) {
				return (Block) ((IfStatement)((TryStatement) statements[1]).finallyBlock.statements[0]).thenStatement;
			}
		}
		
		// Not found, create it ...
		// boolean $callPostConstructor = true
		LocalDeclaration callPostConstructorVar = new LocalDeclaration("$callPostConstructor".toCharArray(), 0, 0);
		callPostConstructorVar.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		callPostConstructorVar.initialization = new TrueLiteral(0, 0);
		callPostConstructorVar.traverse(new SetGeneratedByVisitor(source), null);
		
		// Throwable $ex
		Argument catchArg = new Argument("$ex".toCharArray(), 0, createTypeReference("java.lang.Throwable", source), Modifier.FINAL);
		
		// { $callPostConstructor = false; throw $ex; }
		Block catchBlock = new Block(0);
		Assignment assignFalseStatement = new Assignment(new SingleNameReference("$callPostConstructor".toCharArray(), 0), new FalseLiteral(0, 0), 0);
		ThrowStatement throwStatement = new ThrowStatement(new SingleNameReference("$ex".toCharArray(), 0), 0, 0);
		catchBlock.statements = new Statement[] { assignFalseStatement, throwStatement };
		
		// if ($callPostConstructor) { ... }
		Block finallyBlock = new Block(0);
		Block postConstructorBlock = new Block(0);
		IfStatement callPostConstructorIf = new IfStatement(new SingleNameReference("$callPostConstructor".toCharArray(), 0), postConstructorBlock, 0, 0);
		finallyBlock.statements = new Statement[] {callPostConstructorIf};
		
		// try { ... } catch { ... } finally { ... }
		TryStatement tryStatement = new TryStatement();
		tryStatement.tryBlock = new Block(0);
		tryStatement.catchArguments = new Argument[] { catchArg };
		tryStatement.catchBlocks = new Block[] { catchBlock };
		tryStatement.finallyBlock = finallyBlock;
		tryStatement.traverse(new SetGeneratedByVisitor(source), null);
		tryStatement.tryBlock.statements = constructor.statements;
		
		constructor.statements = new Statement[] { callPostConstructorVar, tryStatement };
		
		return postConstructorBlock;
	}
	private boolean containsReturn(ConstructorDeclaration method) {
		ReturnVisitor returnVisitor = new ReturnVisitor();
		method.traverse(returnVisitor, (ClassScope) null);
		return returnVisitor.found;
	}
	
	private static class ReturnVisitor extends ASTVisitor {
		private boolean found = false;
		
		@Override
		public boolean visit(TypeDeclaration type, BlockScope scope) {
			return false;
		}
		@Override
		public boolean visit(TypeDeclaration type, ClassScope scope) {
			return false;
		}
		
		@Override 
		public boolean visit(LambdaExpression lambdaExpression, BlockScope scope) {
			return false;
		}
		
		@Override public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
			found = true;
			return false;
		}
	}
}
