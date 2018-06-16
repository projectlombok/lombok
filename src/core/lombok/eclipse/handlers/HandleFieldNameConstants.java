/*
 * Copyright (C) 2014-2018 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.FieldNameConstants;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleFieldNameConstants extends EclipseAnnotationHandler<FieldNameConstants> {
	public void generateFieldNameConstantsForType(EclipseNode typeNode, EclipseNode errorNode, AccessLevel level, String prefix, String suffix) {
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class, an enum, or a field.");
			return;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field)) generateFieldNameConstantsForField(field, errorNode.get(), level, prefix, suffix);
		}
	}
	
	private void generateFieldNameConstantsForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level, String prefix, String suffix) {
		if (hasAnnotation(FieldNameConstants.class, fieldNode)) return;
		createFieldNameConstantsForField(level, prefix, suffix, fieldNode, fieldNode, pos, false);
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(EclipseNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		return filterField(fieldDecl);
	}
	
	public void handle(AnnotationValues<FieldNameConstants> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.FIELD_NAME_CONSTANTS_FLAG_USAGE, "@FieldNameConstants");
		
		EclipseNode node = annotationNode.up();
		FieldNameConstants annotatationInstance = annotation.getInstance();
		AccessLevel level = annotatationInstance.level();
		String prefix = annotatationInstance.prefix();
		String suffix = annotatationInstance.suffix();
		if (prefix.equals(" CONFIG DEFAULT ")) prefix = annotationNode.getAst().readConfiguration(ConfigurationKeys.FIELD_NAME_CONSTANTS_PREFIX);
		if (suffix.equals(" CONFIG DEFAULT ")) suffix = annotationNode.getAst().readConfiguration(ConfigurationKeys.FIELD_NAME_CONSTANTS_SUFFIX);
		if (prefix == null) prefix = "FIELD_";
		if (suffix == null) suffix = "";
		if (node == null) return;
		
		switch (node.getKind()) {
		case FIELD:
			if (level != AccessLevel.NONE) createFieldNameConstantsForFields(level, prefix, suffix, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true);
			break;
		case TYPE:
			if (level == AccessLevel.NONE) {
				annotationNode.addWarning("type-level '@FieldNameConstants' does not work with AccessLevel.NONE.");
				return;
			}
			generateFieldNameConstantsForType(node, annotationNode, level, prefix, suffix);
			break;
		}
	}
	
	private void createFieldNameConstantsForFields(AccessLevel level, String prefix, String suffix, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		for (EclipseNode fieldNode : fieldNodes) createFieldNameConstantsForField(level, prefix, suffix, fieldNode, errorNode, source, whineIfExists);
	}
	
	private void createFieldNameConstantsForField(AccessLevel level, String prefix, String suffix, EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@FieldNameConstants is only supported on a class, an enum, or a field");
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		String fieldName = new String(field.name);
		String constantName = prefix + HandlerUtil.camelCaseToConstant(fieldName) + suffix;
		if (constantName.equals(fieldName)) {
			fieldNode.addWarning("Not generating constant for this field: The name of the constant would be equal to the name of this field.");
			return;
		}
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		FieldDeclaration fieldConstant = new FieldDeclaration(constantName.toCharArray(), pS,pE);
		fieldConstant.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		fieldConstant.modifiers = toEclipseModifier(level) | Modifier.STATIC | Modifier.FINAL;
		fieldConstant.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {p,p,p});
		fieldConstant.initialization = new StringLiteral(field.name, pS, pE, 0);
		injectField(fieldNode.up(), fieldConstant);
	}
}
