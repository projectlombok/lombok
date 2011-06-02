/*
 * Copyright © 2009-2011 Reinier Zwitserloot and Roel Spilker.
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.HandleSneakyThrows} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSneakyThrows implements EclipseAnnotationHandler<SneakyThrows> {
	private static class DeclaredException {
		final String exceptionName;
		final ASTNode node;
		
		DeclaredException(String exceptionName, ASTNode node) {
			this.exceptionName = exceptionName;
			this.node = node;
		}
		
		public long getPos() {
			return (long)node.sourceStart << 32 | node.sourceEnd;
		}
	}
	
	@Override public boolean deferUntilPostDiet() {
		return true;
	}
	
	@Override public void handle(AnnotationValues<SneakyThrows> annotation, Annotation source, EclipseNode annotationNode) {
		List<String> exceptionNames = annotation.getRawExpressions("value");
		List<DeclaredException> exceptions = new ArrayList<DeclaredException>();
		
		MemberValuePair[] memberValuePairs = source.memberValuePairs();
		if (memberValuePairs == null || memberValuePairs.length == 0) {
			exceptions.add(new DeclaredException("java.lang.Throwable", source));
		} else {
			Expression arrayOrSingle = memberValuePairs[0].value;
			final Expression[] exceptionNameNodes;
			if (arrayOrSingle instanceof ArrayInitializer) {
				exceptionNameNodes = ((ArrayInitializer)arrayOrSingle).expressions;
			} else exceptionNameNodes = new Expression[] { arrayOrSingle };
			
			if (exceptionNames.size() != exceptionNameNodes.length) {
				annotationNode.addError(
						"LOMBOK BUG: The number of exception classes in the annotation isn't the same pre- and post- guessing.");
			}
			
			int idx = 0;
			for (String exceptionName : exceptionNames) {
				if (exceptionName.endsWith(".class")) exceptionName = exceptionName.substring(0, exceptionName.length() - 6);
				exceptions.add(new DeclaredException(exceptionName, exceptionNameNodes[idx++]));
			}
		}
		
		
		EclipseNode owner = annotationNode.up();
		switch (owner.getKind()) {
//		case FIELD:
//			return handleField(annotationNode, (FieldDeclaration)owner.get(), exceptions);
		case METHOD:
			handleMethod(annotationNode, (AbstractMethodDeclaration)owner.get(), exceptions);
			break;
		default:
			annotationNode.addError("@SneakyThrows is legal only on methods and constructors.");
		}
	}
	
//	private boolean handleField(Node annotation, FieldDeclaration field, List<DeclaredException> exceptions) {
//		if (field.initialization == null) {
//			annotation.addError("@SneakyThrows can only be used on fields with an initialization statement.");
//			return true;
//		}
//		
//		Expression expression = field.initialization;
//		Statement[] content = new Statement[] {new Assignment(
//				new SingleNameReference(field.name, 0), expression, 0)};
//		field.initialization = null;
//		
//		for (DeclaredException exception : exceptions) {
//			content = new Statement[] { buildTryCatchBlock(content, exception) };
//		}
//		
//		Block block = new Block(0);
//		block.statements = content;
//		
//		Node typeNode = annotation.up().up();
//		
//		Initializer initializer = new Initializer(block, field.modifiers & Modifier.STATIC);
//		initializer.sourceStart = expression.sourceStart;
//		initializer.sourceEnd = expression.sourceEnd;
//		initializer.declarationSourceStart = expression.sourceStart;
//		initializer.declarationSourceEnd = expression.sourceEnd;
//		injectField(typeNode, initializer);
//		
//		typeNode.rebuild();
//		
//		return true;
//	}
	
	private void handleMethod(EclipseNode annotation, AbstractMethodDeclaration method, List<DeclaredException> exceptions) {
		if (method.isAbstract()) {
			annotation.addError("@SneakyThrows can only be used on concrete methods.");
			return;
		}
		
		if (method.statements == null) return;
		
		Statement[] contents = method.statements;
		
		for (DeclaredException exception : exceptions) {
			contents = new Statement[] { buildTryCatchBlock(contents, exception, exception.node) };
		}
		
		method.statements = contents;
		annotation.up().rebuild();
	}
	
	private Statement buildTryCatchBlock(Statement[] contents, DeclaredException exception, ASTNode source) {
		long p = exception.getPos();
		int pS = (int)(p >> 32), pE = (int)p;
		
		TryStatement tryStatement = new TryStatement();
		Eclipse.setGeneratedBy(tryStatement, source);
		tryStatement.tryBlock = new Block(0);
		tryStatement.tryBlock.sourceStart = pS; tryStatement.tryBlock.sourceEnd = pE;
		Eclipse.setGeneratedBy(tryStatement.tryBlock, source);
		tryStatement.tryBlock.statements = contents;
		TypeReference typeReference;
		if (exception.exceptionName.indexOf('.') == -1) {
			typeReference = new SingleTypeReference(exception.exceptionName.toCharArray(), p);
			typeReference.statementEnd = pE;
		} else {
			String[] x = exception.exceptionName.split("\\.");
			char[][] elems = new char[x.length][];
			long[] poss = new long[x.length];
			int start = pS;
			for (int i = 0; i < x.length; i++) {
				elems[i] = x[i].trim().toCharArray();
				int end = start + x[i].length();
				poss[i] = (long)start << 32 | end;
				start = end + 1;
			}
			typeReference = new QualifiedTypeReference(elems, poss);
		}
		Eclipse.setGeneratedBy(typeReference, source);
		
		Argument catchArg = new Argument("$ex".toCharArray(), p, typeReference, Modifier.FINAL);
		Eclipse.setGeneratedBy(catchArg, source);
		catchArg.declarationSourceEnd = catchArg.declarationEnd = catchArg.sourceEnd = pE;
		catchArg.declarationSourceStart = catchArg.modifiersSourceStart = catchArg.sourceStart = pS;
		
		tryStatement.catchArguments = new Argument[] { catchArg };
		
		MessageSend sneakyThrowStatement = new MessageSend();
		Eclipse.setGeneratedBy(sneakyThrowStatement, source);
		sneakyThrowStatement.receiver = new QualifiedNameReference(new char[][] { "lombok".toCharArray(), "Lombok".toCharArray() }, new long[] { p, p }, pS, pE);
		Eclipse.setGeneratedBy(sneakyThrowStatement.receiver, source);
		sneakyThrowStatement.receiver.statementEnd = pE;
		sneakyThrowStatement.selector = "sneakyThrow".toCharArray();
		SingleNameReference exRef = new SingleNameReference("$ex".toCharArray(), p);
		Eclipse.setGeneratedBy(exRef, source);
		exRef.statementEnd = pE;
		sneakyThrowStatement.arguments = new Expression[] { exRef };
		sneakyThrowStatement.nameSourcePosition = p;
		sneakyThrowStatement.sourceStart = pS;
		sneakyThrowStatement.sourceEnd = sneakyThrowStatement.statementEnd = pE;
		Statement rethrowStatement = new ThrowStatement(sneakyThrowStatement, pS, pE);
		Eclipse.setGeneratedBy(rethrowStatement, source);
		Block block = new Block(0);
		block.sourceStart = pS;
		block.sourceEnd = pE;
		Eclipse.setGeneratedBy(block, source);
		block.statements = new Statement[] { rethrowStatement };
		tryStatement.catchBlocks = new Block[] { block };
		tryStatement.sourceStart = pS;
		tryStatement.sourceEnd = pE;
		return tryStatement;
	}
}
