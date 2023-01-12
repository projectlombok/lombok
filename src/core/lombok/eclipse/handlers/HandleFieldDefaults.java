/*
 * Copyright (C) 2012-2021 The Project Lombok Authors.
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

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseASTAdapter;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseNode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;
import lombok.spi.Provides;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Handles the {@code lombok.FieldDefaults} annotation for eclipse.
 */
@Provides(EclipseASTVisitor.class)
@HandlerPriority(-2048) //-2^11; to ensure @Value picks up on messing with the fields' 'final' state, run earlier.
public class HandleFieldDefaults extends EclipseASTAdapter {
	public boolean generateFieldDefaultsForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean makeFinal, boolean checkForTypeLevelFieldDefaults) {
		if (checkForTypeLevelFieldDefaults) {
			if (hasAnnotation(FieldDefaults.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		if (!isClassOrEnum(typeNode)) {
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
					if ((field.modifiers & ClassFileConstants.AccStatic) == 0) {
						field.modifiers |= EclipseHandlerUtil.toEclipseModifier(level);
					}
				}
			}
		}
		
		if (makeFinal && (field.modifiers & ClassFileConstants.AccFinal) == 0) {
			if (!hasAnnotation(NonFinal.class, fieldNode)) {
				if ((field.modifiers & ClassFileConstants.AccStatic) == 0) {
					field.modifiers |= ClassFileConstants.AccFinal;
				}
			}
		}
		
		fieldNode.rebuild();
	}
	
	private static final char[] FIELD_DEFAULTS = "FieldDefaults".toCharArray();
	
	@Override public void visitType(EclipseNode typeNode, TypeDeclaration type) {
		AnnotationValues<FieldDefaults> fieldDefaults = null;
		EclipseNode source = typeNode;
		
		boolean levelIsExplicit = false;
		boolean makeFinalIsExplicit = false;
		FieldDefaults fd = null;
		for (EclipseNode jn : typeNode.down()) {
			if (jn.getKind() != Kind.ANNOTATION) continue;
			Annotation ann = (Annotation) jn.get();
			TypeReference typeTree = ann.type;
			if (typeTree == null) continue;
			if (typeTree instanceof SingleTypeReference) {
				char[] t = ((SingleTypeReference) typeTree).token;
				if (!Arrays.equals(t, FIELD_DEFAULTS)) continue;
			} else if (typeTree instanceof QualifiedTypeReference) {
				char[][] t = ((QualifiedTypeReference) typeTree).tokens;
				if (!Eclipse.nameEquals(t, "lombok.experimental.FieldDefaults")) continue;
			} else {
				continue;
			}
			
			if (!typeMatches(FieldDefaults.class, jn, typeTree)) continue;
			
			source = jn;
			fieldDefaults = createAnnotation(FieldDefaults.class, jn);
			levelIsExplicit = fieldDefaults.isExplicit("level");
			makeFinalIsExplicit = fieldDefaults.isExplicit("makeFinal");
			
			handleExperimentalFlagUsage(jn, ConfigurationKeys.FIELD_DEFAULTS_FLAG_USAGE, "@FieldDefaults");
			
			fd = fieldDefaults.getInstance();
			if (!levelIsExplicit && !makeFinalIsExplicit) {
				jn.addError("This does nothing; provide either level or makeFinal or both.");
			}
			
			if (levelIsExplicit && fd.level() == AccessLevel.NONE) {
				jn.addError("AccessLevel.NONE doesn't mean anything here. Pick another value.");
				levelIsExplicit = false;
			}
			break;
		}
		
		if (fd == null && (type.modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0) return;
		
		boolean defaultToPrivate = levelIsExplicit ? false : Boolean.TRUE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.FIELD_DEFAULTS_PRIVATE_EVERYWHERE));
		boolean defaultToFinal = makeFinalIsExplicit ? false : Boolean.TRUE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.FIELD_DEFAULTS_FINAL_EVERYWHERE));
		
		if (!defaultToPrivate && !defaultToFinal && fieldDefaults == null) return;
		// Do not apply field defaults to records if set using the config system
		if (fieldDefaults == null && !isClassOrEnum(typeNode)) return;
		AccessLevel fdAccessLevel = (fieldDefaults != null && levelIsExplicit) ? fd.level() : defaultToPrivate ? AccessLevel.PRIVATE : null;
		boolean fdToFinal = (fieldDefaults != null && makeFinalIsExplicit) ? fd.makeFinal() : defaultToFinal;
		
		generateFieldDefaultsForType(typeNode, source, fdAccessLevel, fdToFinal, false);
	}
}
