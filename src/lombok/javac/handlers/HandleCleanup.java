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
package lombok.javac.handlers;

import lombok.Cleanup;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Handles the <code>lombok.Cleanup</code> annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleCleanup implements JavacAnnotationHandler<Cleanup> {
	@Override public boolean handle(AnnotationValues<Cleanup> annotation, JCAnnotation ast, Node annotationNode) {
		String cleanupName = annotation.getInstance().cleanupMethod();
		if ( cleanupName.length() == 0 ) {
			annotationNode.addError("cleanupName cannot be the empty string.");
			return true;
		}
		
		if ( annotationNode.up().getKind() != Kind.LOCAL ) {
			annotationNode.addError("@Cleanup is legal only on local variable declarations.");
			return true;
		}
		
		JCVariableDecl decl = (JCVariableDecl)annotationNode.up().get();
		
		if ( decl.init == null ) {
			annotationNode.addError("@Cleanup variable declarations need to be initialized.");
			return true;
		}
		
		Node ancestor = annotationNode.up().directUp();
		JCTree blockNode = ancestor.get();
		
		final List<JCStatement> statements;
		if ( blockNode instanceof JCBlock ) {
			statements = ((JCBlock)blockNode).stats;
		} else if ( blockNode instanceof JCCase ) {
			statements = ((JCCase)blockNode).stats;
		} else if ( blockNode instanceof JCMethodDecl ) {
			statements = ((JCMethodDecl)blockNode).body.stats;
		} else {
			annotationNode.addError("@Cleanup is legal only on a local variable declaration inside a block.");
			return true;
		}
		
		boolean seenDeclaration = false;
		List<JCStatement> tryBlock = List.nil();
		List<JCStatement> newStatements = List.nil();
		for ( JCStatement statement : statements ) {
			if ( !seenDeclaration ) {
				if ( statement == decl ) seenDeclaration = true;
				newStatements = newStatements.append(statement);
			} else tryBlock = tryBlock.append(statement);
		}
		
		if ( !seenDeclaration ) {
			annotationNode.addError("LOMBOK BUG: Can't find this local variable declaration inside its parent.");
			return true;
		}
		
		doAssignmentCheck(annotationNode, tryBlock, decl.name);
		
		TreeMaker maker = annotationNode.getTreeMaker();
		JCFieldAccess cleanupCall = maker.Select(maker.Ident(decl.name), annotationNode.toName(cleanupName));
		List<JCStatement> finalizerBlock = List.<JCStatement>of(maker.Exec(
				maker.Apply(List.<JCExpression>nil(), cleanupCall, List.<JCExpression>nil())));
		
		JCBlock finalizer = maker.Block(0, finalizerBlock);
		newStatements = newStatements.append(maker.Try(maker.Block(0, tryBlock), List.<JCCatch>nil(), finalizer));
		
		if ( blockNode instanceof JCBlock ) {
			((JCBlock)blockNode).stats = newStatements;
		} else if ( blockNode instanceof JCCase ) {
			((JCCase)blockNode).stats = newStatements;
		} else if ( blockNode instanceof JCMethodDecl ) {
			((JCMethodDecl)blockNode).body.stats = newStatements;
		} else throw new AssertionError("Should not get here");
		
		ancestor.rebuild();
		
		return true;
	}
	
	private void doAssignmentCheck(Node node, List<JCStatement> statements, Name name) {
		for ( JCStatement statement : statements ) doAssignmentCheck0(node, statement, name);
	}
	
	private void doAssignmentCheck0(Node node, JCTree statement, Name name) {
		if ( statement instanceof JCAssign ) doAssignmentCheck0(node, ((JCAssign)statement).rhs, name);
		if ( statement instanceof JCExpressionStatement ) doAssignmentCheck0(node,
				((JCExpressionStatement)statement).expr, name);
		if ( statement instanceof JCVariableDecl ) doAssignmentCheck0(node, ((JCVariableDecl)statement).init, name);
		if ( statement instanceof JCTypeCast ) doAssignmentCheck0(node, ((JCTypeCast)statement).expr, name);
		if ( statement instanceof JCIdent ) {
			if ( ((JCIdent)statement).name.contentEquals(name) ) {
				Node problemNode = node.getNodeFor(statement);
				if ( problemNode != null ) problemNode.addWarning(
				"You're assigning an auto-cleanup variable to something else. This is a bad idea.");
			}
		}
	}
}
