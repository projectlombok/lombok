/*
 * Copyright (C) 2015-2016 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.mangosdk.spi.ProviderFor;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.Helper;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleHelper extends JavacAnnotationHandler<Helper> {
	private List<JCStatement> getStatementsFromJcNode(JCTree tree) {
		if (tree instanceof JCBlock) return ((JCBlock) tree).stats;
		if (tree instanceof JCCase) return ((JCCase) tree).stats;
		return null;
	}
	
	private void setStatementsOfJcNode(JCTree tree, List<JCStatement> statements) {
		if (tree instanceof JCBlock) ((JCBlock) tree).stats = statements;
		else if (tree instanceof JCCase) ((JCCase) tree).stats = statements;
		else throw new IllegalArgumentException("Can't set statements on node type: " + tree.getClass());
	}
	
	@Override public void handle(AnnotationValues<Helper> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.HELPER_FLAG_USAGE, "@Helper");
		
		deleteAnnotationIfNeccessary(annotationNode, Helper.class);
		JavacNode annotatedType = annotationNode.up();
		JavacNode containingBlock = annotatedType == null ? null : annotatedType.directUp();
		List<JCStatement> origStatements = getStatementsFromJcNode(containingBlock == null ? null : containingBlock.get());
		
		if (annotatedType == null || annotatedType.getKind() != Kind.TYPE || origStatements == null) {
			annotationNode.addError("@Helper is legal only on method-local classes.");
			return;
		}
		
		JCClassDecl annotatedType_ = (JCClassDecl) annotatedType.get();
		Iterator<JCStatement> it = origStatements.iterator();
		while (it.hasNext()) {
			if (it.next() == annotatedType_) {
				break;
			}
		}
		
		java.util.List<String> knownMethodNames = new ArrayList<String>();
		
		for (JavacNode ch : annotatedType.down()) {
			if (ch.getKind() != Kind.METHOD) continue;
			String n = ch.getName();
			if (n == null || n.isEmpty() || n.charAt(0) == '<') continue;
			knownMethodNames.add(n);
		}
		
		Collections.sort(knownMethodNames);
		final String[] knownMethodNames_ = knownMethodNames.toArray(new String[0]);
		
		final Name helperName = annotationNode.toName("$" + annotatedType_.name);
		final boolean[] helperUsed = new boolean[1];
		final JavacTreeMaker maker = annotationNode.getTreeMaker();
		
		TreeVisitor<Void, Void> visitor = new TreeScanner<Void, Void>() {
			@Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
				JCMethodInvocation jcmi = (JCMethodInvocation) node;
				apply(jcmi);
				return super.visitMethodInvocation(node, p);
			}
			
			private void apply(JCMethodInvocation jcmi) {
				if (!(jcmi.meth instanceof JCIdent)) return;
				JCIdent jci = (JCIdent) jcmi.meth;
				if (Arrays.binarySearch(knownMethodNames_, jci.name.toString()) < 0) return;
				jcmi.meth = maker.Select(maker.Ident(helperName), jci.name);
				helperUsed[0] = true;
			}
		};
		
		while (it.hasNext()) {
			JCStatement stat = it.next();
			stat.accept(visitor, null);
		}
		
		if (!helperUsed[0]) {
			annotationNode.addWarning("No methods of this helper class are ever used.");
			return;
		}
		
		ListBuffer<JCStatement> newStatements = new ListBuffer<JCStatement>();
		
		boolean mark = false;
		for (JCStatement stat : origStatements) {
			newStatements.append(stat);
			if (mark || stat != annotatedType_) continue;
			mark = true;
			JCExpression init = maker.NewClass(null, List.<JCExpression>nil(), maker.Ident(annotatedType_.name), List.<JCExpression>nil(), null);
			JCExpression varType = maker.Ident(annotatedType_.name);
			JCVariableDecl decl = maker.VarDef(maker.Modifiers(Flags.FINAL), helperName, varType, init);
			newStatements.append(decl);
		}
		setStatementsOfJcNode(containingBlock.get(), newStatements.toList());
	}
}
