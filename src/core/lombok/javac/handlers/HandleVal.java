/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import lombok.val;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

@ProviderFor(JavacASTVisitor.class)
public class HandleVal extends JavacASTAdapter {
	@Override public boolean isResolutionBased() {
		return true;
	}
	
	@Override public void visitLocal(JavacNode localNode, JCVariableDecl local) {
		if (local.vartype == null || (!local.vartype.toString().equals("val") && !local.vartype.toString().equals("lombok.val"))) return;
		
		JCTree source = local.vartype;
		
		if (!typeMatches(val.class, localNode, local.vartype)) return;
		
		JCExpression rhsOfEnhancedForLoop = null;
		if (local.init == null) {
			JCTree parentRaw = localNode.directUp().get();
			if (parentRaw instanceof JCEnhancedForLoop) {
				JCEnhancedForLoop efl = (JCEnhancedForLoop) parentRaw;
				if (efl.var == local) rhsOfEnhancedForLoop = efl.expr;
			}
		}
		
		if (rhsOfEnhancedForLoop == null && local.init == null) {
			localNode.addError("'val' on a local variable requires an initializer expression");
			return;
		}
		
		if (local.init instanceof JCNewArray && ((JCNewArray)local.init).elemtype == null) {
			localNode.addError("'val' is not compatible with array initializer expressions. Use the full form (new int[] { ... } instead of just { ... })");
			return;
		}
		
		if (localNode.shouldDeleteLombokAnnotations()) JavacHandlerUtil.deleteImportFromCompilationUnit(localNode, "lombok.val");
		
		local.mods.flags |= Flags.FINAL;
		
		if (!localNode.shouldDeleteLombokAnnotations()) {
			JCAnnotation valAnnotation = recursiveSetGeneratedBy(localNode.getTreeMaker().Annotation(local.vartype, List.<JCExpression>nil()), source);
			local.mods.annotations = local.mods.annotations == null ? List.of(valAnnotation) : local.mods.annotations.append(valAnnotation);
		}
		
		local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
		
		Type type;
		try {
			if (rhsOfEnhancedForLoop == null) {
				if (local.init.type == null) {
					JavacResolution resolver = new JavacResolution(localNode.getContext());
					type = ((JCExpression) resolver.resolveMethodMember(localNode).get(local.init)).type;
				} else {
					type = local.init.type;
				}
			} else {
				if (rhsOfEnhancedForLoop.type == null) {
					JavacResolution resolver = new JavacResolution(localNode.getContext());
					type = ((JCExpression) resolver.resolveMethodMember(localNode.directUp()).get(rhsOfEnhancedForLoop)).type;
				} else {
					type = rhsOfEnhancedForLoop.type;
				}
			}
			
			try {
				JCExpression replacement;
				
				if (rhsOfEnhancedForLoop != null) {
					Type componentType = JavacResolution.ifTypeIsIterableToComponent(type, localNode.getAst());
					if (componentType == null) replacement = JavacResolution.createJavaLangObject(localNode.getAst());
					else replacement = JavacResolution.typeToJCTree(componentType, localNode.getAst(), false);
				} else {
					replacement = JavacResolution.typeToJCTree(type, localNode.getAst(), false);
				}
				
				if (replacement != null) {
					local.vartype = replacement;
				} else {
					local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
				}
				localNode.getAst().setChanged();
			} catch (JavacResolution.TypeNotConvertibleException e) {
				localNode.addError("Cannot use 'val' here because initializer expression does not have a representable type: " + e.getMessage());
				local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
			}
		} catch (RuntimeException e) {
			local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
			throw e;
		} finally {
			recursiveSetGeneratedBy(local.vartype, source);
		}
	}
}
