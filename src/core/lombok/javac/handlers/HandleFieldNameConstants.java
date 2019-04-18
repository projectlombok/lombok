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

import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.FieldNameConstants;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
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

@ProviderFor(JavacAnnotationHandler.class)
public class HandleFieldNameConstants extends JavacAnnotationHandler<FieldNameConstants> {
	public void generateFieldNameConstantsForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean asEnum, String innerTypeName, boolean onlyExplicit) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class or an enum.");
			return;
		}
		
		java.util.List<JavacNode> qualified = new ArrayList<JavacNode>();
		
		for (JavacNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field, onlyExplicit)) qualified.add(field);
		}
		
		if (qualified.isEmpty()) {
			errorNode.addWarning("No fields qualify for @FieldNameConstants, therefore this annotation does nothing");
		} else {
			createInnerTypeFieldNameConstants(typeNode, errorNode, errorNode.get(), level, qualified, asEnum, innerTypeName);
		}
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(JavacNode field, boolean onlyExplicit) {
		if (field.getKind() != Kind.FIELD) return false;
		boolean exclAnn = JavacHandlerUtil.hasAnnotationAndDeleteIfNeccessary(FieldNameConstants.Exclude.class, field);
		boolean inclAnn = JavacHandlerUtil.hasAnnotationAndDeleteIfNeccessary(FieldNameConstants.Include.class, field);
		if (exclAnn) return false;
		if (inclAnn) return true;
		if (onlyExplicit) return false;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		if (fieldDecl.name.toString().startsWith("$")) return false;
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) return false;
		return true;
	}
	
	public void handle(AnnotationValues<FieldNameConstants> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.FIELD_NAME_CONSTANTS_FLAG_USAGE, "@FieldNameConstants");
		
		deleteAnnotationIfNeccessary(annotationNode, FieldNameConstants.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		FieldNameConstants annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.level();
		boolean asEnum = annotationInstance.asEnum();
		boolean usingLombokv1_18_2 = annotation.isExplicit("prefix") || annotation.isExplicit("suffix") || node.getKind() == Kind.FIELD;
		
		if (usingLombokv1_18_2) {
			annotationNode.addError("@FieldNameConstants has been redesigned in lombok v1.18.4; please upgrade your project dependency on lombok. See https://projectlombok.org/features/experimental/FieldNameConstants for more information.");
			return;
		}
		
		
		if (level == AccessLevel.NONE) {
			annotationNode.addWarning("AccessLevel.NONE is not compatible with @FieldNameConstants. If you don't want the inner type, simply remove @FieldNameConstants.");
			return;
		}
		
		String innerTypeName = annotationInstance.innerTypeName();
		if (innerTypeName.isEmpty()) innerTypeName = annotationNode.getAst().readConfiguration(ConfigurationKeys.FIELD_NAME_CONSTANTS_INNER_TYPE_NAME);
		if (innerTypeName == null || innerTypeName.isEmpty()) innerTypeName = "Fields";
		
		generateFieldNameConstantsForType(node, annotationNode, level, asEnum, innerTypeName, annotationInstance.onlyExplicitlyIncluded());
	}
	
	private void createInnerTypeFieldNameConstants(JavacNode typeNode, JavacNode errorNode, JCTree pos, AccessLevel level, java.util.List<JavacNode> fields, boolean asEnum, String innerTypeName) {
		if (fields.isEmpty()) return;
		
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCModifiers mods = maker.Modifiers(toJavacModifier(level) | (asEnum ? Flags.ENUM : Flags.STATIC | Flags.FINAL));
		
		Name fieldsName = typeNode.toName(innerTypeName);
		
		JavacNode fieldsType = findInnerClass(typeNode, innerTypeName);
		boolean genConstr = false;
		if (fieldsType == null) {
			JCClassDecl innerType = maker.ClassDef(mods, fieldsName, List.<JCTypeParameter>nil(), null, List.<JCExpression>nil(), List.<JCTree>nil());
			fieldsType = injectType(typeNode, innerType);
			recursiveSetGeneratedBy(innerType, pos, typeNode.getContext());
			genConstr = true;
		} else {
			JCClassDecl builderTypeDeclaration = (JCClassDecl) fieldsType.get();
			long f = builderTypeDeclaration.getModifiers().flags;
			if (asEnum && (f & Flags.ENUM) == 0) {
				errorNode.addError("Existing " + innerTypeName + " must be declared as an 'enum'.");
				return;
			}
			if (!asEnum && (f & Flags.STATIC) == 0) {
				errorNode.addError("Existing " + innerTypeName + " must be declared as a 'static class'.");
				return;
			}
			genConstr = constructorExists(fieldsType) == MemberExistsResult.NOT_EXISTS;
		}
		
		if (genConstr) {
			JCModifiers genConstrMods = maker.Modifiers(Flags.GENERATEDCONSTR | (asEnum ? 0L : Flags.PRIVATE));
			JCBlock genConstrBody = maker.Block(0L, List.<JCStatement>of(maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(typeNode.toName("super")), List.<JCExpression>nil()))));
			JCMethodDecl c = maker.MethodDef(genConstrMods, typeNode.toName("<init>"), null, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), genConstrBody, null);
			recursiveSetGeneratedBy(c, pos, typeNode.getContext());
			injectMethod(fieldsType, c);
		}
		
		java.util.List<JCVariableDecl> generated = new ArrayList<JCVariableDecl>();
		for (JavacNode field : fields) {
			Name fName = ((JCVariableDecl) field.get()).name;
			if (fieldExists(fName.toString(), fieldsType) != MemberExistsResult.NOT_EXISTS) continue;
			JCModifiers constantValueMods = maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL | (asEnum ? Flags.ENUM : 0L));
			JCExpression returnType;
			JCExpression init;
			if (asEnum) {
				returnType = maker.Ident(fieldsName);
				init = maker.NewClass(null, List.<JCExpression>nil(), maker.Ident(fieldsName), List.<JCExpression>nil(), null);
			} else {
				returnType = chainDots(field, "java", "lang", "String");
				init = maker.Literal(field.getName());
			}
			JCVariableDecl constantField = maker.VarDef(constantValueMods, fName, returnType, init);
			injectField(fieldsType, constantField, false, true);
			setGeneratedBy(constantField, pos, typeNode.getContext());
			generated.add(constantField);
		}
		for (JCVariableDecl cf : generated) recursiveSetGeneratedBy(cf, pos, typeNode.getContext());
	}
}
