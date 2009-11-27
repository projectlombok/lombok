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

import static lombok.javac.handlers.JavacHandlerUtil.chainDots;
import static lombok.javac.handlers.JavacHandlerUtil.markAnnotationAsProcessed;

import java.util.ArrayList;
import java.util.Collection;

import lombok.SneakyThrows;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.SneakyThrows} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSneakyThrows implements JavacAnnotationHandler<SneakyThrows> {
	@Override public boolean handle(AnnotationValues<SneakyThrows> annotation, JCAnnotation ast, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, SneakyThrows.class);
		Collection<String> exceptionNames = annotation.getRawExpressions("value");
		
		List<JCExpression> memberValuePairs = ast.getArguments();
		if (memberValuePairs == null || memberValuePairs.size() == 0) return false;
		
		java.util.List<String> exceptions = new ArrayList<String>();
		for (String exception : exceptionNames) {
			if (exception.endsWith(".class")) exception = exception.substring(0, exception.length() - 6);
			exceptions.add(exception);
		}
		
		JavacNode owner = annotationNode.up();
		switch (owner.getKind()) {
		case METHOD:
			return handleMethod(annotationNode, (JCMethodDecl)owner.get(), exceptions);
		default:
			annotationNode.addError("@SneakyThrows is legal only on methods and constructors.");
			return true;
		}
	}
	
	private boolean handleMethod(JavacNode annotation, JCMethodDecl method, Collection<String> exceptions) {
		JavacNode methodNode = annotation.up();
		
		if ( (method.mods.flags & Flags.ABSTRACT) != 0) {
			annotation.addError("@SneakyThrows can only be used on concrete methods.");
			return true;
		}
		
		if (method.body == null) return false;
		
		List<JCStatement> contents = method.body.stats;
		
		for (String exception : exceptions) {
			contents = List.of(buildTryCatchBlock(methodNode, contents, exception));
		}
		
		method.body.stats = contents;
		methodNode.rebuild();
		
		return true;
	}

	private JCStatement buildTryCatchBlock(JavacNode node, List<JCStatement> contents, String exception) {
		TreeMaker maker = node.getTreeMaker();
		
		JCBlock tryBlock = maker.Block(0, contents);
		
		JCExpression varType = chainDots(maker, node, exception.split("\\."));
		
		JCVariableDecl catchParam = maker.VarDef(maker.Modifiers(0), node.toName("$ex"), varType, null);
		JCExpression lombokLombokSneakyThrowNameRef = chainDots(maker, node, "lombok", "Lombok", "sneakyThrow");
		JCBlock catchBody = maker.Block(0, List.<JCStatement>of(maker.Throw(maker.Apply(
				List.<JCExpression>nil(), lombokLombokSneakyThrowNameRef,
				List.<JCExpression>of(maker.Ident(node.toName("$ex")))))));
		
		return maker.Try(tryBlock, List.of(maker.Catch(catchParam, catchBody)), null);
	}
}
