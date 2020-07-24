/*
 * Copyright (C) 2020 The Project Lombok Authors.
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
import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.PostGeneratedConstructor;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

/**
 * Handles the {@code lombok.experimental.PostGeneratedConstructor} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(value = 1024)
public class HandlePostGeneratedConstructor extends JavacAnnotationHandler<PostGeneratedConstructor> {
	
	@Override public void handle(AnnotationValues<PostGeneratedConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.POST_GENERATED_CONSTRUCTOR_FLAG_USAGE, "@PostGeneratedConstructor");
		
		if (inNetbeansEditor(annotationNode)) return;
		
		deleteAnnotationIfNeccessary(annotationNode, PostGeneratedConstructor.class);
		JavacNode methodNode = annotationNode.up();
		
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof JCMethodDecl)) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on methods.");
			return;
		}
		
		JCMethodDecl method = (JCMethodDecl)methodNode.get();
		
		if ((method.mods.flags & Flags.ABSTRACT) != 0) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on concrete methods.");
			return;
		}
		
		if ((method.mods.flags & Flags.STATIC) != 0) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on instance methods.");
			return;
		}
		
		JavacTreeMaker maker = methodNode.getTreeMaker();
		Context context = methodNode.getContext();
		
		JavacNode typeNode = upToTypeNode(annotationNode);
		
		List<JCMethodDecl> generatedConstructors = findGeneratedConstructors(typeNode);
		if (generatedConstructors.isEmpty()) {
			annotationNode.addError("Cannot find a generated constructor.");
			return;
		}
		
		for (JCMethodDecl constructor : generatedConstructors) {
			JCStatement statement = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(method.name), List.<JCExpression>nil()));
			recursiveSetGeneratedBy(statement, ast, context);
			
			constructor.body.stats = appendToList(constructor.body.stats, statement);
			constructor.thrown = appendToList(constructor.thrown, method.thrown);
			
			typeNode.getAst().get(constructor).rebuild();
		}
	}
	
	private List<JCMethodDecl> findGeneratedConstructors(JavacNode typeNode) {
		ListBuffer<JCMethodDecl> constructors = new ListBuffer<JCMethodDecl>();
		if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
			JCClassDecl type = (JCClassDecl) typeNode.get();
			for (JCTree def : type.defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl methodDecl = (JCMethodDecl) def;
					if (methodDecl.name.toString().equals("<init>") && getGeneratedBy(methodDecl) != null) {
						constructors.add(methodDecl);
					}
				}
			}
		}
		return constructors.toList();
	}
	
	private <T> List<T> appendToList(List<T> list, T append) {
		return appendToList(list, List.of(append));
	}
	
	private <T> List<T> appendToList(List<T> list, List<T> append) {
		return List.<T>nil().appendList(list).appendList(append);
	}
}
