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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Handles the {@code lombok.FieldDefaults} annotation for javac.
 */
@Provides(JavacASTVisitor.class)
@HandlerPriority(-2048) //-2^11; to ensure @Value picks up on messing with the fields' 'final' state, run earlier.
public class HandleFieldDefaults extends JavacASTAdapter {
	public boolean generateFieldDefaultsForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean makeFinal, boolean checkForTypeLevelFieldDefaults) {
		if (checkForTypeLevelFieldDefaults) {
			if (hasAnnotation(FieldDefaults.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		if (!isClassOrEnum(typeNode)) {
			errorNode.addError("@FieldDefaults is only supported on a class or an enum.");
			return false;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			
			setFieldDefaultsForField(field, level, makeFinal);
		}
		
		return true;
	}
	
	public void setFieldDefaultsForField(JavacNode fieldNode, AccessLevel level, boolean makeFinal) {
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		if (level != null && level != AccessLevel.NONE) {
			if ((field.mods.flags & (Flags.PUBLIC | Flags.PRIVATE | Flags.PROTECTED)) == 0) {
				if (!hasAnnotationAndDeleteIfNeccessary(PackagePrivate.class, fieldNode)) {
					if ((field.mods.flags & Flags.STATIC) == 0) {
						field.mods.flags |= toJavacModifier(level);
					}
				}
			}
		}
		
		if (makeFinal && (field.mods.flags & Flags.FINAL) == 0) {
			if (!hasAnnotationAndDeleteIfNeccessary(NonFinal.class, fieldNode)) {
				if ((field.mods.flags & Flags.STATIC) == 0) {
					field.mods.flags |= Flags.FINAL;
				}
			}
		}
		
		fieldNode.rebuild();
	}
	
	@Override public void visitType(JavacNode typeNode, JCClassDecl type) {
		AnnotationValues<FieldDefaults> fieldDefaults = null;
		JavacNode source = typeNode;
		
		boolean levelIsExplicit = false;
		boolean makeFinalIsExplicit = false;
		FieldDefaults fd = null;
		for (JavacNode jn : typeNode.down()) {
			if (jn.getKind() != Kind.ANNOTATION) continue;
			JCAnnotation ann = (JCAnnotation) jn.get();
			JCTree typeTree = ann.annotationType;
			if (typeTree == null) continue;
			String typeTreeToString = typeTree.toString();
			if (!typeTreeToString.equals("FieldDefaults") && !typeTreeToString.equals("lombok.experimental.FieldDefaults")) continue;
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
			
			deleteAnnotationIfNeccessary(jn, FieldDefaults.class);
			deleteImportFromCompilationUnit(jn, "lombok.AccessLevel");
			break;
		}
		
		if (fd == null && (type.mods.flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0) return;
		
		boolean defaultToPrivate = levelIsExplicit ? false : Boolean.TRUE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.FIELD_DEFAULTS_PRIVATE_EVERYWHERE));
		boolean defaultToFinal = makeFinalIsExplicit ? false : Boolean.TRUE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.FIELD_DEFAULTS_FINAL_EVERYWHERE));
		
		if (!defaultToPrivate && !defaultToFinal && fieldDefaults == null) return;
		// Do not apply field defaults to records if set using the the config system
		if (fieldDefaults == null && !isClassOrEnum(typeNode)) return;
		AccessLevel fdAccessLevel = (fieldDefaults != null && levelIsExplicit) ? fd.level() : defaultToPrivate ? AccessLevel.PRIVATE : null;
		boolean fdToFinal = (fieldDefaults != null && makeFinalIsExplicit) ? fd.makeFinal() : defaultToFinal;
		
		generateFieldDefaultsForType(typeNode, source, fdAccessLevel, fdToFinal, false);
	}
}
