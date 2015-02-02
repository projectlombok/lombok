/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.UtilityClass;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.experimental.UtilityClass} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class) public class HandleUtilityClass extends JavacAnnotationHandler<UtilityClass> {

	@Override public void handle(AnnotationValues<UtilityClass> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.UTLITY_CLASS_FLAG_USAGE, "@UtilityClass");
		deleteAnnotationIfNeccessary(annotationNode, UtilityClass.class);

		parse(annotationNode.up(), annotationNode);
	}

	private JCClassDecl getClassDecl(JavacNode typeNode) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) {
			typeDecl = (JCClassDecl) typeNode.get();
		}

		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		return notAClass ? null : typeDecl;
	}

	public void parse(JavacNode typeNode, JavacNode errorNode) {
		JCClassDecl classDecl = getClassDecl(typeNode);
		if (classDecl == null) {
			errorNode.addError("@UtilityClass is only supported on a class (can't be interface, enum or annotation).");
			return;
		}

		if (!is(classDecl.getModifiers(), Flags.FINAL)) {
			classDecl.mods.flags |= Flags.FINAL;
		}

		for (JavacNode element : typeNode.down()) {
			if (element.getKind() == Kind.FIELD) {
				JCVariableDecl fieldDecl = (JCVariableDecl) element.get();
				if (!is(fieldDecl.mods, Flags.STATIC)) {
					fieldDecl.mods.flags |= Flags.STATIC;
				}
			} else if (element.getKind() == Kind.METHOD) {
				JCMethodDecl methodDecl = (JCMethodDecl) element.get();
				if (methodDecl.name.contentEquals("<init>")) {
					if (!is(methodDecl.mods, Flags.GENERATEDCONSTR)) {
						errorNode.addError("@UtilityClasses cannot have declared constructors.");
						continue;
					}
				} else if (!is(methodDecl.mods, Flags.STATIC)) {
					methodDecl.mods.flags |= Flags.STATIC;
				}
			} else if (element.getKind() == Kind.TYPE) {
				JCClassDecl innerClassDecl = (JCClassDecl) typeNode.get();
				if (!is(innerClassDecl.mods, Flags.STATIC)) {
					innerClassDecl.mods.flags |= Flags.STATIC;
				}
			}
		}

		createPrivateDefaultConstructor(typeNode);
	}

	private static boolean is(JCModifiers mods, long f) {
		return (mods.flags & f) != 0;
	}

	private void createPrivateDefaultConstructor(JavacNode typeNode) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.PRIVATE, List.<JCAnnotation>nil());
		
		Name name = typeNode.toName("<init>");
		JCBlock block = maker.Block(0L, createThrowStatement(typeNode, maker));
		JCMethodDecl methodDef = maker.MethodDef(mods, name, null, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), block, null);
		JCMethodDecl constructor = recursiveSetGeneratedBy(methodDef, typeNode.get(), typeNode.getContext());
		JavacHandlerUtil.injectMethod(typeNode, constructor);
	}
	
	private List<JCStatement> createThrowStatement(JavacNode typeNode, JavacTreeMaker maker) {
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		JCExpression exceptionType = genTypeRef(typeNode, "java.lang.RuntimeException");
		JCExpression exceptionInstance = maker.NewClass(null, List.<JCExpression>nil(), exceptionType, List.<JCExpression>of(maker.Literal("Should not be instanciated!")), null);
		JCStatement throwStatement = maker.Throw(exceptionInstance);
		statements.add(throwStatement);
		return statements.toList();
	}
}
