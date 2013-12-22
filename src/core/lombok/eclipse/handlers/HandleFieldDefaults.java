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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.FieldDefaults} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
@HandlerPriority(-2048) //-2^11; to ensure @Value picks up on messing with the fields' 'final' state, run earlier.
public class HandleFieldDefaults extends EclipseAnnotationHandler<FieldDefaults> {
	public boolean generateFieldDefaultsForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean makeFinal, boolean checkForTypeLevelFieldDefaults) {
		if (checkForTypeLevelFieldDefaults) {
			if (hasAnnotation(FieldDefaults.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			pos.addError("@FieldDefaults is only supported on a class or an enum.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
			if (!filterField(fieldDecl, false)) continue;
			
			Class<?> t = field.get().getClass();
			if (t == FieldDeclaration.class) {
				// There are various other things that extend FieldDeclaration that really
				// aren't field declarations. Typing 'ma' in an otherwise blank class is a
				// CompletionOnFieldType object (extends FieldDeclaration). If we mess with the
				// modifiers of such a thing, you take away template suggestions such as
				// 'main method'. See issue 411.
				setFieldDefaultsForField(field, pos.get(), level, makeFinal);
			}
		}
		return true;
	}
	
	public void setFieldDefaultsForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level, boolean makeFinal) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		if (level != null && level != AccessLevel.NONE) {
			if ((field.modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected)) == 0) {
				if (!hasAnnotation(PackagePrivate.class, fieldNode)) {
					field.modifiers |= EclipseHandlerUtil.toEclipseModifier(level);
				}
			}
		}
		
		if (makeFinal && (field.modifiers & ClassFileConstants.AccFinal) == 0) {
			if (!hasAnnotation(NonFinal.class, fieldNode)) {
				field.modifiers |= ClassFileConstants.AccFinal;
			}
		}
		
		fieldNode.rebuild();
	}
	
	public void handle(AnnotationValues<FieldDefaults> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, FieldDefaults.FLAG_USAGE, "@FieldDefaults");
		
		EclipseNode node = annotationNode.up();
		FieldDefaults instance = annotation.getInstance();
		AccessLevel level = instance.level();
		boolean makeFinal = instance.makeFinal();
		
		if (level == AccessLevel.NONE && !makeFinal) {
			annotationNode.addError("This does nothing; provide either level or makeFinal or both.");
			return;
		}
		
		if (level == AccessLevel.PACKAGE) {
			annotationNode.addError("Setting 'level' to PACKAGE does nothing. To force fields as package private, use the @PackagePrivate annotation on the field.");
		}
		
		if (!makeFinal && annotation.isExplicit("makeFinal")) {
			annotationNode.addError("Setting 'makeFinal' to false does nothing. To force fields to be non-final, use the @NonFinal annotation on the field.");
		}
		
		if (node == null) return;
		
		generateFieldDefaultsForType(node, annotationNode, level, makeFinal, false);
	}
}
