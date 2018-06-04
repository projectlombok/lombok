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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.handlers.HandlerUtil;
import lombok.core.AnnotationValues;
import lombok.experimental.FieldNameConstants;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleFieldNameConstants extends JavacAnnotationHandler<FieldNameConstants> {
	public void generateFieldNameConstantsForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, String prefix, String suffix) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class, an enum, or a field.");
			return;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field)) generateFieldNameConstantsForField(field, errorNode.get(), level, prefix, suffix);
		}
	}
	
	private void generateFieldNameConstantsForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level, String prefix, String suffix) {
		if (hasAnnotation(FieldNameConstants.class, fieldNode)) return;
		createFieldNameConstantsForField(level, prefix, suffix, fieldNode, fieldNode, false);
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(JavacNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		if (fieldDecl.name.toString().startsWith("$")) return false;
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) return false;
		return true;
	}
	
	public void handle(AnnotationValues<FieldNameConstants> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.FIELD_NAME_CONSTANTS_FLAG_USAGE, "@FieldNameConstants");
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, FieldNameConstants.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
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
			if (level != AccessLevel.NONE) createFieldNameConstantsForFields(level, prefix, suffix, fields, annotationNode, annotationNode, true);
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
	
	private void createFieldNameConstantsForFields(AccessLevel level, String prefix, String suffix, Collection<JavacNode> fieldNodes, JavacNode annotationNode, JavacNode errorNode, boolean whineIfExists) {
		for (JavacNode fieldNode : fieldNodes) createFieldNameConstantsForField(level, prefix, suffix, fieldNode, errorNode, whineIfExists);
	}
	
	private void createFieldNameConstantsForField(AccessLevel level, String prefix, String suffix, JavacNode fieldNode, JavacNode source, boolean whineIfExists) {
		if (fieldNode.getKind() != Kind.FIELD) {
			source.addError("@FieldNameConstants is only supported on a class, an enum, or a field");
			return;
		}
		
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		String fieldName = field.name.toString();
		String constantName = prefix + HandlerUtil.camelCaseToConstant(fieldName) + suffix;
		if (constantName.equals(fieldName)) {
			fieldNode.addWarning("Not generating constant for this field: The name of the constant would be equal to the name of this field.");
			return;
		}
		
		JavacTreeMaker treeMaker = fieldNode.getTreeMaker();
		JCModifiers modifiers = treeMaker.Modifiers(toJavacModifier(level) | Modifier.STATIC | Modifier.FINAL);
		JCExpression returnType = chainDots(fieldNode, "java", "lang", "String");
		JCExpression init = treeMaker.Literal(fieldNode.getName());
		JCVariableDecl fieldConstant = treeMaker.VarDef(modifiers, fieldNode.toName(constantName), returnType, init);
		injectField(fieldNode.up(), fieldConstant);
	}
}