/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

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
 * Handles the <code>lombok.HandleSneakyThrows</code> annotation for eclipse.
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
	
	@Override public boolean handle(AnnotationValues<SneakyThrows> annotation, Annotation ast, Node annotationNode) {
		List<String> exceptionNames = annotation.getRawExpressions("value");
		
		MemberValuePair[] memberValuePairs = ast.memberValuePairs();
		if ( memberValuePairs == null || memberValuePairs.length == 0 ) return false;
		
		Expression arrayOrSingle = memberValuePairs[0].value;
		final Expression[] exceptionNameNodes;
		if ( arrayOrSingle instanceof ArrayInitializer ) {
			exceptionNameNodes = ((ArrayInitializer)arrayOrSingle).expressions;
		} else exceptionNameNodes = new Expression[] { arrayOrSingle };
		
		if ( exceptionNames.size() != exceptionNameNodes.length ) {
			annotationNode.addError(
					"LOMBOK BUG: The number of exception classes in the annotation isn't the same pre- and post- guessing.");
		}
		
		List<DeclaredException> exceptions = new ArrayList<DeclaredException>();
		int idx = 0;
		for ( String exceptionName : exceptionNames ) {
			if ( exceptionName.endsWith(".class") ) exceptionName = exceptionName.substring(0, exceptionName.length() - 6);
			exceptions.add(new DeclaredException(exceptionName, exceptionNameNodes[idx++]));
		}
		
		Node owner = annotationNode.up();
		switch ( owner.getKind() ) {
//		case FIELD:
//			return handleField(annotationNode, (FieldDeclaration)owner.get(), exceptions);
		case METHOD:
			return handleMethod(annotationNode, (AbstractMethodDeclaration)owner.get(), exceptions);
		default:
			annotationNode.addError("@SneakyThrows is legal only on methods and constructors.");
			return true;
		}
	}
	
//	private boolean handleField(Node annotation, FieldDeclaration field, List<DeclaredException> exceptions) {
//		if ( field.initialization == null ) {
//			annotation.addError("@SneakyThrows can only be used on fields with an initialization statement.");
//			return true;
//		}
//		
//		Expression expression = field.initialization;
//		Statement[] content = new Statement[] {new Assignment(
//				new SingleNameReference(field.name, 0), expression, 0)};
//		field.initialization = null;
//		
//		for ( DeclaredException exception : exceptions ) {
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
	
	private boolean handleMethod(Node annotation, AbstractMethodDeclaration method, List<DeclaredException> exceptions) {
		if ( method.isAbstract() ) {
			annotation.addError("@SneakyThrows can only be used on concrete methods.");
			return true;
		}
		
		if ( method.statements == null ) return false;
		
		Statement[] contents = method.statements;
		
		for ( DeclaredException exception : exceptions ) {
			contents = new Statement[] { buildTryCatchBlock(contents, exception) };
		}
		
		method.statements = contents;
		annotation.up().rebuild();
		
		return true;
	}

	private Statement buildTryCatchBlock(Statement[] contents, DeclaredException exception) {
		TryStatement tryStatement = new TryStatement();
		tryStatement.tryBlock = new Block(0);
		tryStatement.tryBlock.statements = contents;
		TypeReference typeReference;
		if ( exception.exceptionName.indexOf('.') == -1 ) {
			typeReference = new SingleTypeReference(exception.exceptionName.toCharArray(), exception.getPos());
		} else {
			String[] x = exception.exceptionName.split("\\.");
			char[][] elems = new char[x.length][];
			long[] poss = new long[x.length];
			int start = (int)(exception.getPos() >> 32);
			for ( int i = 0 ; i < x.length ; i++ ) {
				elems[i] = x[i].trim().toCharArray();
				int end = start + x[i].length();
				poss[i] = (long)start << 32 | end;
				start = end + 1;
			}
			typeReference = new QualifiedTypeReference(elems, poss);
		}
		
		Argument catchArg = new Argument("$ex".toCharArray(), exception.getPos(), typeReference, 0);
		
		tryStatement.catchArguments = new Argument[] { catchArg };
		
		MessageSend sneakyThrowStatement = new MessageSend();
		sneakyThrowStatement.receiver = new QualifiedNameReference(new char[][] { "lombok".toCharArray(), "Lombok".toCharArray() }, new long[] { 0, 0 }, 0, 0);
		sneakyThrowStatement.selector = "sneakyThrow".toCharArray();
		sneakyThrowStatement.arguments = new Expression[] { new SingleNameReference("$ex".toCharArray(), 0) };
		Statement rethrowStatement = new ThrowStatement(sneakyThrowStatement, 0, 0);
		Block block = new Block(0);
		block.statements = new Statement[] { rethrowStatement };
		block.sourceStart = block.sourceEnd = -2;
		tryStatement.catchBlocks = new Block[] { block };
		return tryStatement;
	}
}
