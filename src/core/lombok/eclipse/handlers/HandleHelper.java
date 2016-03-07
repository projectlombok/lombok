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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.mangosdk.spi.ProviderFor;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Helper;

/**
 * Handles the {@code lombok.Cleanup} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleHelper extends EclipseAnnotationHandler<Helper> {
	private Statement[] getStatementsFromAstNode(ASTNode node) {
		if (node instanceof Block) return ((Block) node).statements;
		if (node instanceof AbstractMethodDeclaration) return ((AbstractMethodDeclaration) node).statements;
		if (node instanceof SwitchStatement) return ((SwitchStatement) node).statements;
		return null;
	}
	
	private void setStatementsOfAstNode(ASTNode node, Statement[] statements) {
		if (node instanceof Block) ((Block) node).statements = statements;
		else if (node instanceof AbstractMethodDeclaration) ((AbstractMethodDeclaration) node).statements = statements;
		else if (node instanceof SwitchStatement) ((SwitchStatement) node).statements = statements;
		else throw new IllegalArgumentException("Can't set statements on node type: " + node.getClass());
	}
	
	@Override public void handle(AnnotationValues<Helper> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.HELPER_FLAG_USAGE, "@Helper");
		
		EclipseNode annotatedType = annotationNode.up();
		EclipseNode containingBlock = annotatedType == null ? null : annotatedType.directUp();
		Statement[] origStatements = getStatementsFromAstNode(containingBlock == null ? null : containingBlock.get());
		
		if (annotatedType == null || annotatedType.getKind() != Kind.TYPE || origStatements == null) {
			annotationNode.addError("@Helper is legal only on method-local classes.");
			return;
		}
		
		TypeDeclaration annotatedType_ = (TypeDeclaration) annotatedType.get();
		int indexOfType = -1;
		for (int i = 0; i < origStatements.length; i++) {
			if (origStatements[i] == annotatedType_) {
				indexOfType = i;
				break;
			}
		}
		
		final List<String> knownMethodNames = new ArrayList<String>();
		
		for (AbstractMethodDeclaration methodOfHelper : annotatedType_.methods) {
			if (!(methodOfHelper instanceof MethodDeclaration)) continue;
			char[] name = methodOfHelper.selector;
			if (name != null && name.length > 0 && name[0] != '<') knownMethodNames.add(new String(name));
		}
		
		Collections.sort(knownMethodNames);
		final String[] knownMethodNames_ = knownMethodNames.toArray(new String[knownMethodNames.size()]);
		
		final char[] helperName = new char[annotatedType_.name.length + 1];
		final boolean[] helperUsed = new boolean[1];
		helperName[0] = '$';
		System.arraycopy(annotatedType_.name, 0, helperName, 1, helperName.length - 1);
		
		ASTVisitor visitor = new ASTVisitor() {
			@Override public boolean visit(MessageSend messageSend, BlockScope scope) {
				if (messageSend.receiver instanceof ThisReference) {
					if ((((ThisReference) messageSend.receiver).bits & ASTNode.IsImplicitThis) == 0) return true;
				} else if (messageSend.receiver != null) return true;
				
				char[] name = messageSend.selector;
				if (name == null || name.length == 0 || name[0] == '<') return true;
				String n = new String(name);
				if (Arrays.binarySearch(knownMethodNames_, n) < 0) return true;
				messageSend.receiver = new SingleNameReference(helperName, messageSend.nameSourcePosition);
				helperUsed[0] = true;
				return true;
			}
		};
		
		for (int i = indexOfType + 1; i < origStatements.length; i++) {
			origStatements[i].traverse(visitor, null);
		}
		
		if (!helperUsed[0]) {
			annotationNode.addWarning("No methods of this helper class are ever used.");
			return;
		}
		
		Statement[] newStatements = new Statement[origStatements.length + 1];
		System.arraycopy(origStatements, 0, newStatements, 0, indexOfType + 1);
		System.arraycopy(origStatements, indexOfType + 1, newStatements, indexOfType + 2, origStatements.length - indexOfType - 1);
		LocalDeclaration decl = new LocalDeclaration(helperName, 0, 0);
		decl.modifiers |= ClassFileConstants.AccFinal;
		AllocationExpression alloc = new AllocationExpression();
		alloc.type = new SingleTypeReference(annotatedType_.name, 0L);
		decl.initialization = alloc;
		decl.type = new SingleTypeReference(annotatedType_.name, 0L);
		SetGeneratedByVisitor sgbvVisitor = new SetGeneratedByVisitor(annotationNode.get());
		decl.traverse(sgbvVisitor, null);
		newStatements[indexOfType + 1] = decl;
		setStatementsOfAstNode(containingBlock.get(), newStatements);
	}
}
