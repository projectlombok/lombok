/*
 * Copyright (C) 2012 The Project Lombok Authors.
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
import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Handles the {@code lombok.FieldDefaults} annotation for eclipse.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleFieldDefaults extends JavacAnnotationHandler<FieldDefaults> {
	public boolean generateFieldDefaultsForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean makeFinal, boolean checkForTypeLevelFieldDefaults) {
		if (checkForTypeLevelFieldDefaults) {
			if (hasAnnotation(FieldDefaults.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldDefaults is only supported on a class or an enum.");
			return false;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			
			setFieldDefaultsForField(field, errorNode.get(), level, makeFinal);
		}
		
		return true;
	}
	
	public void setFieldDefaultsForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level, boolean makeFinal) {
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		if (level != null && level != AccessLevel.NONE) {
			if ((field.mods.flags & (Flags.PUBLIC | Flags.PRIVATE | Flags.PROTECTED)) == 0) {
				if (!hasAnnotationAndDeleteIfNeccessary(PackagePrivate.class, fieldNode)) {
					field.mods.flags |= toJavacModifier(level);
				}
			}
		}
		
		if (makeFinal && (field.mods.flags & Flags.FINAL) == 0) {
			if (!hasAnnotationAndDeleteIfNeccessary(NonFinal.class, fieldNode)) {
				field.mods.flags |= Flags.FINAL;
			}
		}
	}
	
	@Override public void handle(AnnotationValues<FieldDefaults> annotation, JCAnnotation ast, JavacNode annotationNode) {
		deleteAnnotationIfNeccessary(annotationNode, FieldDefaults.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		FieldDefaults instance = annotation.getInstance();
		AccessLevel level = instance.level();
		boolean makeFinal = instance.makeFinal();
		
		if (level == AccessLevel.NONE && !makeFinal) {
			annotationNode.addError("This does nothing; provide either level or makeFinal or both.");
			return;
		}
		
		if (node == null) return;
		
		generateFieldDefaultsForType(node, annotationNode, level, makeFinal, false);
	}
}
