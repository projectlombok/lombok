/*
 * Copyright (C) 2015-2020 The Project Lombok Authors.
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
import static lombok.javac.Javac.CTC_VOID;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
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
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.UtilityClass;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

/**
 * Handles the {@code @UtilityClass} annotation for javac.
 */
@HandlerPriority(-4096) //-2^12; to ensure @FieldDefaults picks up on the 'static' we set here.
@ProviderFor(JavacAnnotationHandler.class)
public class HandleUtilityClass extends JavacAnnotationHandler<UtilityClass> {
	@Override public void handle(AnnotationValues<UtilityClass> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.UTILITY_CLASS_FLAG_USAGE, "@UtilityClass");
		
		deleteAnnotationIfNeccessary(annotationNode, UtilityClass.class);
		
		JavacNode typeNode = annotationNode.up();
		if (!checkLegality(typeNode, annotationNode)) return;
		changeModifiersAndGenerateConstructor(annotationNode.up(), annotationNode);
	}
	
	private static boolean checkLegality(JavacNode typeNode, JavacNode errorNode) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@UtilityClass is only supported on a class (can't be an interface, enum, or annotation).");
			return false;
		}
		
		// It might be an inner class. This is okay, but only if it is / can be a static inner class. Thus, all of its parents have to be static inner classes until the top-level.
		JavacNode typeWalk = typeNode;
		while (true) {
			typeWalk = typeWalk.up();
			switch (typeWalk.getKind()) {
			case TYPE:
				JCClassDecl typeDef = (JCClassDecl) typeWalk.get();
				if ((typeDef.mods.flags & (Flags.STATIC | Flags.ANNOTATION | Flags.ENUM | Flags.INTERFACE)) != 0) continue;
				if (typeWalk.up().getKind() == Kind.COMPILATION_UNIT) return true;
				errorNode.addError("@UtilityClass automatically makes the class static, however, this class cannot be made static.");
				return false;
			case COMPILATION_UNIT:
				return true;
			default:
				errorNode.addError("@UtilityClass cannot be placed on a method local or anonymous inner class, or any class nested in such a class.");
				return false;
			}
		}
	}
	
	private void changeModifiersAndGenerateConstructor(JavacNode typeNode, JavacNode errorNode) {
		JCClassDecl classDecl = (JCClassDecl) typeNode.get();
		
		boolean makeConstructor = true;
		
		classDecl.mods.flags |= Flags.FINAL;
		
		boolean markStatic = true;
		
		if (typeNode.up().getKind() == Kind.COMPILATION_UNIT) markStatic = false;
		if (markStatic && typeNode.up().getKind() == Kind.TYPE) {
			JCClassDecl typeDecl = (JCClassDecl) typeNode.up().get();
			if ((typeDecl.mods.flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0) markStatic = false;
		}
		
		if (markStatic) classDecl.mods.flags |= Flags.STATIC;
		
		for (JavacNode element : typeNode.down()) {
			if (element.getKind() == Kind.FIELD) {
				JCVariableDecl fieldDecl = (JCVariableDecl) element.get();
				fieldDecl.mods.flags |= Flags.STATIC;
			} else if (element.getKind() == Kind.METHOD) {
				JCMethodDecl methodDecl = (JCMethodDecl) element.get();
				if (methodDecl.name.contentEquals("<init>")) {
					if (getGeneratedBy(methodDecl) == null && (methodDecl.mods.flags & Flags.GENERATEDCONSTR) == 0) {
						element.addError("@UtilityClasses cannot have declared constructors.");
						makeConstructor = false;
						continue;
					}
				}
				
				methodDecl.mods.flags |= Flags.STATIC;
			} else if (element.getKind() == Kind.TYPE) {
				JCClassDecl innerClassDecl = (JCClassDecl) element.get();
				innerClassDecl.mods.flags |= Flags.STATIC;
			}
		}
		
		if (makeConstructor) createPrivateDefaultConstructor(typeNode);
	}
	
	private void createPrivateDefaultConstructor(JavacNode typeNode) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.PRIVATE, List.<JCAnnotation>nil());
		
		Name name = typeNode.toName("<init>");
		JCBlock block = maker.Block(0L, createThrowStatement(typeNode, maker));
		JCMethodDecl methodDef = maker.MethodDef(mods, name, null, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), block, null);
		JCMethodDecl constructor = recursiveSetGeneratedBy(methodDef, typeNode);
		JavacHandlerUtil.injectMethod(typeNode, constructor, List.<Type>nil(), Javac.createVoidType(typeNode.getSymbolTable(), CTC_VOID));
	}
	
	private List<JCStatement> createThrowStatement(JavacNode typeNode, JavacTreeMaker maker) {
		JCExpression exceptionType = genJavaLangTypeRef(typeNode, "UnsupportedOperationException");
		List<JCExpression> jceBlank = List.nil();
		JCExpression message = maker.Literal("This is a utility class and cannot be instantiated");
		JCExpression exceptionInstance = maker.NewClass(null, jceBlank, exceptionType, List.of(message), null);
		JCStatement throwStatement = maker.Throw(exceptionInstance);
		return List.of(throwStatement);
	}
}
