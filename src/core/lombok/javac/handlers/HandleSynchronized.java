/*
 * Copyright (C) 2009-2023 The Project Lombok Authors.
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
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.ConfigurationKeys;
import lombok.Synchronized;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.Synchronized} annotation for javac.
 */
@Provides
@HandlerPriority(value = 1024) // 2^10; @NonNull must have run first, so that we wrap around the statements generated by it.
public class HandleSynchronized extends JavacAnnotationHandler<Synchronized> {
	private static final String INSTANCE_LOCK_NAME = "$lock";
	private static final String STATIC_LOCK_NAME = "$LOCK";
	
	@Override public void handle(AnnotationValues<Synchronized> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.SYNCHRONIZED_FLAG_USAGE, "@Synchronized");
		
		if (inNetbeansEditor(annotationNode)) return;
		deleteAnnotationIfNeccessary(annotationNode, Synchronized.class);
		
		JavacNode methodNode = annotationNode.up();
		
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof JCMethodDecl)) {
			annotationNode.addError("@Synchronized is legal only on methods.");
			return;
		}
		
		JCMethodDecl method = (JCMethodDecl)methodNode.get();
		
		if ((method.mods.flags & Flags.ABSTRACT) != 0) {
			annotationNode.addError("@Synchronized is legal only on concrete methods.");
			return;
		}
		
		JavacNode typeNode = upToTypeNode(annotationNode);
		if (!isClassOrEnum(typeNode)) {
			annotationNode.addError("@Synchronized is legal only on methods in classes and enums.");
			return;
		}
		
		boolean isStatic = (method.mods.flags & Flags.STATIC) != 0;
		String lockName = annotation.getInstance().value();
		boolean autoMake = false;
		if (lockName.length() == 0) {
			autoMake = true;
			lockName = isStatic ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		}
		
		JavacTreeMaker maker = methodNode.getTreeMaker().at(ast.pos);
		
		MemberExistsResult exists = MemberExistsResult.NOT_EXISTS;
		
		if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl) typeNode.get()).defs) {
				if (def instanceof JCVariableDecl) {
					if (((JCVariableDecl) def).name.contentEquals(lockName)) {
						exists = getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
						boolean st = ((((JCVariableDecl) def).mods.flags) & Flags.STATIC) != 0;
						if (isStatic && !st) {
							annotationNode.addError("The field " + lockName + " is non-static and this cannot be used on this static method");
							return;
						}
						isStatic = st;
					}
				}
			}
		}
		
		if (exists == MemberExistsResult.NOT_EXISTS) {
			if (!autoMake) {
				annotationNode.addError("The field " + lockName + " does not exist.");
				return;
			}
			JCExpression objectType = genJavaLangTypeRef(methodNode, ast.pos, "Object");
			//We use 'new Object[0];' because unlike 'new Object();', empty arrays *ARE* serializable!
			JCNewArray newObjectArray = maker.NewArray(genJavaLangTypeRef(methodNode, ast.pos, "Object"),
				List.<JCExpression>of(maker.Literal(CTC_INT, 0)), null);
			JCVariableDecl fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
				maker.Modifiers(Flags.PRIVATE | Flags.FINAL | (isStatic ? Flags.STATIC : 0)),
				methodNode.toName(lockName), objectType, newObjectArray), annotationNode);
			injectFieldAndMarkGenerated(methodNode.up(), fieldDecl);
		}
		
		if (method.body == null) return;
		
		JCExpression lockNode;
		if (isStatic) {
			lockNode = namePlusTypeParamsToTypeReference(maker, typeNode, methodNode.toName(lockName), false, List.<JCTypeParameter>nil());
		} else {
			lockNode = maker.Select(maker.Ident(methodNode.toName("this")), methodNode.toName(lockName));
		}
		
		recursiveSetGeneratedBy(lockNode, annotationNode);
		method.body = setGeneratedBy(maker.Block(0, List.<JCStatement>of(setGeneratedBy(maker.Synchronized(lockNode, method.body), annotationNode))), annotationNode);
		
		methodNode.rebuild();
	}
}
