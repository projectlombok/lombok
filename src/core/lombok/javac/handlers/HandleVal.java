/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.handlers.HandleDelegate.HANDLE_DELEGATE_PRIORITY;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.reflect.Field;

import lombok.ConfigurationKeys;
import lombok.val;
import lombok.var;
import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.ResolutionResetNeeded;
import lombok.permit.Permit;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

@Provides(JavacASTVisitor.class)
@HandlerPriority(HANDLE_DELEGATE_PRIORITY + 100) // run slightly after HandleDelegate; resolution needs to work, so if the RHS expression is i.e. a call to a generated getter, we have to run after that getter has been generated.
@ResolutionResetNeeded
public class HandleVal extends JavacASTAdapter {
	
	private static boolean eq(String typeTreeToString, String key) {
		return typeTreeToString.equals(key) || typeTreeToString.equals("lombok." + key) || typeTreeToString.equals("lombok.experimental." + key);
	}
	
	@SuppressWarnings("deprecation") @Override
	public void endVisitLocal(JavacNode localNode, JCVariableDecl local) {
		JCTree typeTree = local.vartype;
		if (typeTree == null) return;
		String typeTreeToString = typeTree.toString();
		JavacNode typeNode = localNode.getNodeFor(typeTree);
		
		if (!(eq(typeTreeToString, "val") || eq(typeTreeToString, "var"))) return;
		boolean isVal = typeMatches(val.class, localNode, typeTree);
		boolean isVar = typeMatches(var.class, localNode, typeTree);
		if (!(isVal || isVar)) return;
		
		if (isVal) handleFlagUsage(localNode, ConfigurationKeys.VAL_FLAG_USAGE, "val");
		if (isVar) handleFlagUsage(localNode, ConfigurationKeys.VAR_FLAG_USAGE, "var");
		
		JCTree parentRaw = localNode.directUp().get();
		if (isVal && parentRaw instanceof JCForLoop) {
			localNode.addError("'val' is not allowed in old-style for loops");
			return;
		}
		
		if (parentRaw instanceof JCForLoop && ((JCForLoop) parentRaw).getInitializer().size() > 1) {
			localNode.addError("'var' is not allowed in old-style for loops if there is more than 1 initializer");
			return;
		}
		
		JCExpression rhsOfEnhancedForLoop = null;
		if (local.init == null) {
			if (parentRaw instanceof JCEnhancedForLoop) {
				JCEnhancedForLoop efl = (JCEnhancedForLoop) parentRaw;
				JCTree var = EnhancedForLoopReflect.getVarOrRecordPattern(efl);
				if (var == local) rhsOfEnhancedForLoop = efl.expr;
			}
		}
		
		final String annotation = typeTreeToString;
		if (rhsOfEnhancedForLoop == null && local.init == null) {
			localNode.addError("'" + annotation + "' on a local variable requires an initializer expression");
			return;
		}
		
		if (local.init instanceof JCNewArray && ((JCNewArray)local.init).elemtype == null) {
			localNode.addError("'" + annotation + "' is not compatible with array initializer expressions. Use the full form (new int[] { ... } instead of just { ... })");
			return;
		}
		
		if (localNode.shouldDeleteLombokAnnotations()) {
			JavacHandlerUtil.deleteImportFromCompilationUnit(localNode, val.class.getName());
			JavacHandlerUtil.deleteImportFromCompilationUnit(localNode, lombok.experimental.var.class.getName());
			JavacHandlerUtil.deleteImportFromCompilationUnit(localNode, var.class.getName());
		}
		
		if (isVal) local.mods.flags |= Flags.FINAL;
		
		if (!localNode.shouldDeleteLombokAnnotations()) {
			JCAnnotation valAnnotation = recursiveSetGeneratedBy(localNode.getTreeMaker().Annotation(local.vartype, List.<JCExpression>nil()), typeNode);
			local.mods.annotations = local.mods.annotations == null ? List.of(valAnnotation) : local.mods.annotations.append(valAnnotation);
		}
		
		if (localNode.getSourceVersion() >= 10) {
			local.vartype = null;
			localNode.getAst().setChanged();
			return;
		}
		
		if (JavacResolution.platformHasTargetTyping()) {
			local.vartype = localNode.getAst().getTreeMaker().Ident(localNode.getAst().toName("___Lombok_VAL_Attrib__"));
		} else {
			local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
		}
		
		Type type;
		try {
			if (rhsOfEnhancedForLoop == null) {
				if (local.init.type == null) {
					if (isVar && local.init instanceof JCLiteral && ((JCLiteral) local.init).value == null) {
						localNode.addError("variable initializer is 'null'");
					}
					JavacResolution resolver = new JavacResolution(localNode.getContext());
					try {
						type = ((JCExpression) resolver.resolveMethodMember(localNode).get(local.init)).type;
					} catch (RuntimeException e) {
						System.err.println("Exception while resolving: " + localNode + "(" + localNode.getFileName() + ")");
						throw e;
					}
				} else {
					type = local.init.type;
					if (type.isErroneous()) {
						try {
							JavacResolution resolver = new JavacResolution(localNode.getContext());
							local.type = Symtab.instance(localNode.getContext()).unknownType;
							type = ((JCExpression) resolver.resolveMethodMember(localNode).get(local.init)).type;
						} catch (RuntimeException e) {
							System.err.println("Exception while resolving: " + localNode + "(" + localNode.getFileName() + ")");
							throw e;
						}
					}
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
				localNode.addError("Cannot use '" + annotation + "' here because initializer expression does not have a representable type: " + e.getMessage());
				local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
			}
		} catch (RuntimeException e) {
			local.vartype = JavacResolution.createJavaLangObject(localNode.getAst());
			throw e;
		} finally {
			recursiveSetGeneratedBy(local.vartype, typeNode);
		}
	}
	
	private static class EnhancedForLoopReflect {
		private static final Field varOrRecordPattern = Permit.permissiveGetField(JCEnhancedForLoop.class, "varOrRecordPattern");
		
		private static JCTree getVarOrRecordPattern(JCEnhancedForLoop loop) {
			if (varOrRecordPattern == null) {
				return loop.var;
			}
			
			try {
				return (JCTree) varOrRecordPattern.get(loop);
			} catch (Exception ignore) {}
			return null;
		}
	}
}
