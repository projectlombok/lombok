/*
 * Copyright © 2009-2011 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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
package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import lombok.Cleanup;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.Cleanup} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleCleanup implements JavacAnnotationHandler<Cleanup> {
	@Override public void handle(AnnotationValues<Cleanup> annotation, JCAnnotation ast, JavacNode annotationNode) {
		deleteAnnotationIfNeccessary(annotationNode, Cleanup.class);
		String cleanupName = annotation.getInstance().value();
		if (cleanupName.length() == 0) {
			annotationNode.addError("cleanupName cannot be the empty string.");
			return;
		}
		
		if (annotationNode.up().getKind() != Kind.LOCAL) {
			annotationNode.addError("@Cleanup is legal only on local variable declarations.");
			return;
		}
		
		JCVariableDecl decl = (JCVariableDecl)annotationNode.up().get();
		
		if (decl.init == null) {
			annotationNode.addError("@Cleanup variable declarations need to be initialized.");
			return;
		}
		
		JavacNode ancestor = annotationNode.up().directUp();
		JCTree blockNode = ancestor.get();
		
		final List<JCStatement> statements;
		if (blockNode instanceof JCBlock) {
			statements = ((JCBlock)blockNode).stats;
		} else if (blockNode instanceof JCCase) {
			statements = ((JCCase)blockNode).stats;
		} else if (blockNode instanceof JCMethodDecl) {
			statements = ((JCMethodDecl)blockNode).body.stats;
		} else {
			annotationNode.addError("@Cleanup is legal only on a local variable declaration inside a block.");
			return;
		}
		
		boolean seenDeclaration = false;
		ListBuffer<JCStatement> newStatements = ListBuffer.lb();
		ListBuffer<JCStatement> tryBlock = ListBuffer.lb();
		for (JCStatement statement : statements) {
			if (!seenDeclaration) {
				if (statement == decl) seenDeclaration = true;
				newStatements.append(statement);
			} else {
				tryBlock.append(statement);
			}
		}
		
		if (!seenDeclaration) {
			annotationNode.addError("LOMBOK BUG: Can't find this local variable declaration inside its parent.");
			return;
		}
		doAssignmentCheck(annotationNode, tryBlock.toList(), decl.name);
		
		TreeMaker maker = annotationNode.getTreeMaker();
		JCFieldAccess cleanupMethod = maker.Select(maker.Ident(decl.name), annotationNode.toName(cleanupName));
		List<JCStatement> cleanupCall = List.<JCStatement>of(maker.Exec(
				maker.Apply(List.<JCExpression>nil(), cleanupMethod, List.<JCExpression>nil())));
		
		JCMethodInvocation preventNullAnalysis = preventNullAnalysis(maker, annotationNode, maker.Ident(decl.name));
		JCBinary isNull = maker.Binary(Javac.getCTCint(JCTree.class, "NE"), preventNullAnalysis, maker.Literal(Javac.getCTCint(TypeTags.class, "BOT"), null));
		
		JCIf ifNotNullCleanup = maker.If(isNull, maker.Block(0, cleanupCall), null);
		
		JCBlock finalizer = Javac.recursiveSetGeneratedBy(maker.Block(0, List.<JCStatement>of(ifNotNullCleanup)), ast);
		
		newStatements.append(Javac.setGeneratedBy(maker.Try(Javac.setGeneratedBy(maker.Block(0, tryBlock.toList()), ast), List.<JCCatch>nil(), finalizer), ast));
		
		if (blockNode instanceof JCBlock) {
			((JCBlock)blockNode).stats = newStatements.toList();
		} else if (blockNode instanceof JCCase) {
			((JCCase)blockNode).stats = newStatements.toList();
		} else if (blockNode instanceof JCMethodDecl) {
			((JCMethodDecl)blockNode).body.stats = newStatements.toList();
		} else throw new AssertionError("Should not get here");
		
		ancestor.rebuild();
	}
	
	private JCMethodInvocation preventNullAnalysis(TreeMaker maker, JavacNode node, JCExpression expression) {
		JCMethodInvocation singletonList = maker.Apply(List.<JCExpression>nil(), JavacHandlerUtil.chainDotsString(maker, node, "java.util.Collections.singletonList"), List.of(expression));
		JCMethodInvocation cleanedExpr = maker.Apply(List.<JCExpression>nil(), maker.Select(singletonList, node.toName("get")) , List.<JCExpression>of(maker.Literal(TypeTags.INT, 0)));
		return cleanedExpr;
	}
	
	private void doAssignmentCheck(JavacNode node, List<JCStatement> statements, Name name) {
		for (JCStatement statement : statements) doAssignmentCheck0(node, statement, name);
	}
	
	private void doAssignmentCheck0(JavacNode node, JCTree statement, Name name) {
		if (statement instanceof JCAssign) doAssignmentCheck0(node, ((JCAssign)statement).rhs, name);
		if (statement instanceof JCExpressionStatement) doAssignmentCheck0(node,
				((JCExpressionStatement)statement).expr, name);
		if (statement instanceof JCVariableDecl) doAssignmentCheck0(node, ((JCVariableDecl)statement).init, name);
		if (statement instanceof JCTypeCast) doAssignmentCheck0(node, ((JCTypeCast)statement).expr, name);
		if (statement instanceof JCIdent) {
			if (((JCIdent)statement).name.contentEquals(name)) {
				JavacNode problemNode = node.getNodeFor(statement);
				if (problemNode != null) problemNode.addWarning(
				"You're assigning an auto-cleanup variable to something else. This is a bad idea.");
			}
		}
	}
	
	@Override public boolean isResolutionBased() {
		return false;
	}
}
