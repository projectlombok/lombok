/*
 * Copyright (C) 2022 The Project Lombok Authors.
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
import static lombok.javac.Javac.CTC_BOOLEAN;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.PostConstructor;
import lombok.experimental.PostConstructor.InvokePostConstructors;
import lombok.experimental.PostConstructor.SkipPostConstructors;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.spi.Provides;

/**
 * Handles the {@code lombok.experimental.PostConstructor} annotation for javac.
 */
@Provides
@HandlerPriority(value = 1024)
public class HandlePostConstructor extends JavacAnnotationHandler<PostConstructor> {

	private HandleConstructor handleConstructor = new HandleConstructor();
	
	@Override public void handle(AnnotationValues<PostConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.POST_CONSTRUCTOR_FLAG_USAGE, "@PostConstructor");
		
		if (inNetbeansEditor(annotationNode)) return;
		
		deleteAnnotationIfNeccessary(annotationNode, PostConstructor.class);
		JavacNode methodNode = annotationNode.up();
		
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof JCMethodDecl)) {
			annotationNode.addError("@PostConstructor is legal only on methods.");
			return;
		}
		
		JCMethodDecl method = (JCMethodDecl)methodNode.get();
		
		if ((method.mods.flags & Flags.ABSTRACT) != 0) {
			annotationNode.addError("@PostConstructor is legal only on concrete methods.");
			return;
		}
		
		if ((method.mods.flags & Flags.STATIC) != 0) {
			annotationNode.addError("@PostConstructor is legal only on instance methods.");
			return;
		}
		
		if (method.params.nonEmpty()) {
			annotationNode.addError("@PostConstructor is legal only on methods without parameters.");
			return;
		}
		
		JavacTreeMaker maker = methodNode.getTreeMaker();
		JavacNode typeNode = upToTypeNode(annotationNode);
		
		handleConstructor.generateConstructor(typeNode, AccessLevel.PUBLIC, List.<JCAnnotation>nil(), List.<JavacNode>nil(), false, null, SkipIfConstructorExists.YES, annotationNode);
		
		List<JavacNode> constructors = findConstructors(typeNode);
		for (JavacNode constructorNode : constructors) {
			JCMethodDecl constructor = (JCMethodDecl) constructorNode.get();
			
			boolean hasSkip = hasAnnotation(SkipPostConstructors.class, constructorNode);
			boolean hasInvoke = hasAnnotation(InvokePostConstructors.class, constructorNode);
			boolean hasThisCall = isThisCall(((JCMethodDecl) constructorNode.get()).body.stats.head);
			
			if (hasSkip) {
				if (hasInvoke) {
					constructorNode.addError("@InvokePostConstructors and @SkipPostConstructors are mutually exclusive.");
				}
				if (hasThisCall) {
					constructorNode.addWarning("@SkipPostConstructors is not needed; constructors calling this(...) are skipped anyway.");
				}
				continue;
			}
			
			if (hasThisCall && !hasInvoke) {
				continue;
			}
			
			if (!isGenerated(constructor) && !hasInvoke) {
				constructorNode.addError("Constructor needs to be annotated with @PostConstruct.(Invoke|Skip)PostConstructors or needs to start with a this(...) call.");
				continue;
			};
			
			JCStatement statement = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(method.name), List.<JCExpression>nil()));
			recursiveSetGeneratedBy(statement, annotationNode);
			
			JCBlock block = getOrCreatePostConstructorBlock(constructor, annotationNode);
			block.stats = block.stats.append(statement);
			
			for (JCExpression t : method.thrown) {
				constructor.thrown = constructor.thrown.append(cloneType(maker, t, annotationNode));
			}
			
			constructorNode.rebuild();
		}
	}
	
	private List<JavacNode> findConstructors(JavacNode typeNode) {
		ListBuffer<JavacNode> constructors = new ListBuffer<JavacNode>();
		for (JavacNode methodNode : typeNode.down()) {
			if (methodNode.getKind() != Kind.METHOD) continue;
			if (!methodNode.getName().equals("<init>")) continue;
			
			constructors.add(methodNode);
		}
		return constructors.toList();
	}
	
	private JCBlock getOrCreatePostConstructorBlock(JCMethodDecl method, JavacNode source) {
		// Generated constructors and methods without return cannot exit early
		if (isGenerated(method) || !containsReturn(method)) {
			return method.body;
		}
		
		List<JCStatement> statements = method.body.stats;
		JCStatement head = statements.head;
		
		// Search existing try ... catch ... finally and return 
		if (head instanceof JCVariableDecl) {
			JCVariableDecl jcVariableDecl = (JCVariableDecl) head;
			if (jcVariableDecl.name.toString().equals("$callPostConstructor") && isGenerated(jcVariableDecl)) {
				return (JCBlock) ((JCIf)((JCTry) statements.tail.head).finalizer.stats.head).thenpart;
			}
		}
		
		// Not found, create it ...
		JavacTreeMaker maker = source.getTreeMaker();
		// boolean $callPostConstructor = true
		JCVariableDecl callPostConstructorVar = maker.VarDef(maker.Modifiers(0), source.toName("$callPostConstructor"), maker.TypeIdent(CTC_BOOLEAN), maker.Literal(Javac.CTC_BOOLEAN, 1));
		recursiveSetGeneratedBy(callPostConstructorVar, source);
		
		// Throwable $ex
		JCVariableDecl catchParam = maker.VarDef(maker.Modifiers(Flags.FINAL | Flags.PARAMETER), source.toName("$ex"), genJavaLangTypeRef(source, "Throwable"), null);
		// { $callPostConstructor = false; throw $ex; }
		JCStatement assignStatement = maker.Exec(maker.Assign(maker.Ident(source.toName("$callPostConstructor")), maker.Literal(Javac.CTC_BOOLEAN, 0)));
		JCThrow throwStatement = maker.Throw(maker.Ident(source.toName("$ex")));
		JCBlock catchBody = maker.Block(0, List.<JCStatement>of(assignStatement, throwStatement));
		// if ($callPostConstructor) { ... }
		JCBlock postConstructorBlock = maker.Block(0, List.<JCStatement>nil());
		JCIf callPostConstructorIf = maker.If(maker.Ident(source.toName("$callPostConstructor")), postConstructorBlock, null);
		JCBlock finallyBody = maker.Block(0, List.<JCStatement>of(callPostConstructorIf));
		// try { ... } catch { ... } finally { ... }
		JCTry tryStatement = maker.Try(maker.Block(0, List.<JCStatement>nil()), List.of(maker.Catch(catchParam, catchBody)), finallyBody);
		recursiveSetGeneratedBy(tryStatement, source);
		tryStatement.body.stats = statements;
		
		method.body.stats = List.of(callPostConstructorVar, tryStatement);
		
		return postConstructorBlock;
	}

	private boolean containsReturn(JCMethodDecl method) {
		ReturnTreeScanner returnTreeScanner = new ReturnTreeScanner();
		method.accept(returnTreeScanner);
		return returnTreeScanner.found;
	}
	
	private static final class ReturnTreeScanner extends TreeScanner {
		private boolean found = false;
		
		@Override public void scan(JCTree tree) {
			if (tree == null || found) return;
			if (tree.getClass().getName().equals("com.sun.tools.javac.tree.JCTree$JCLambda")) return;
			super.scan(tree);
		}
		
		@Override public void visitClassDef(JCClassDecl arg0) {
			return;
		}
		
		@Override public void visitReturn(JCReturn tree) {
			found = true;
		}
	}
}
