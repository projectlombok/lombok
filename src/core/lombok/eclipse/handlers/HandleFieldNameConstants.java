/*
 * Copyright (C) 2014-2019 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.IdentifierName;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.experimental.FieldNameConstants;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleFieldNameConstants extends EclipseAnnotationHandler<FieldNameConstants> {
	private static final IdentifierName FIELDS = IdentifierName.valueOf("Fields");

	public void generateFieldNameConstantsForType(EclipseNode typeNode, EclipseNode errorNode, AccessLevel level, boolean asEnum, IdentifierName innerTypeName, boolean onlyExplicit, boolean uppercase) {
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class or an enum.");
			return;
		}
		
		List<EclipseNode> qualified = new ArrayList<EclipseNode>();
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field, onlyExplicit)) qualified.add(field);
		}
		
		if (qualified.isEmpty()) {
			errorNode.addWarning("No fields qualify for @FieldNameConstants, therefore this annotation does nothing");
		} else {
			createInnerTypeFieldNameConstants(typeNode, errorNode, errorNode.get(), level, qualified, asEnum, innerTypeName, uppercase);
		}
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(EclipseNode field, boolean onlyExplicit) {
		if (field.getKind() != Kind.FIELD) return false;
		if (hasAnnotation(FieldNameConstants.Exclude.class, field)) return false;
		if (hasAnnotation(FieldNameConstants.Include.class, field)) return true;
		if (onlyExplicit) return false;
		
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		return filterField(fieldDecl);
	}
	
	@Override public void handle(AnnotationValues<FieldNameConstants> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.FIELD_NAME_CONSTANTS_FLAG_USAGE, "@FieldNameConstants");
		
		EclipseNode node = annotationNode.up();
		FieldNameConstants annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.level();
		boolean asEnum = annotationInstance.asEnum();
		boolean usingLombokv1_18_2 = annotation.isExplicit("prefix") || annotation.isExplicit("suffix") || node.getKind() == Kind.FIELD;
		
		if (usingLombokv1_18_2) {
			annotationNode.addError("@FieldNameConstants has been redesigned in lombok v1.18.4; please upgrade your project dependency on lombok. See https://projectlombok.org/features/experimental/FieldNameConstants for more information.");
			return;
		}
		
		if (level == AccessLevel.NONE) {
			annotationNode.addWarning("AccessLevel.NONE is not compatible with @FieldNameConstants. If you don't want the inner type, simply remove FieldNameConstants.");
			return;
		}
		
		IdentifierName innerTypeName;
		try {
			innerTypeName = IdentifierName.valueOf(annotationInstance.innerTypeName());
		} catch(IllegalArgumentException e) {
			annotationNode.addError("InnerTypeName " + annotationInstance.innerTypeName() + " is not a valid Java identifier.");
			return;
		}
		if (innerTypeName == null) innerTypeName = annotationNode.getAst().readConfiguration(ConfigurationKeys.FIELD_NAME_CONSTANTS_INNER_TYPE_NAME);
		if (innerTypeName == null) innerTypeName = FIELDS;
		Boolean uppercase = annotationNode.getAst().readConfiguration(ConfigurationKeys.FIELD_NAME_CONSTANTS_UPPERCASE);
		if (uppercase == null) uppercase = false;
		
		generateFieldNameConstantsForType(node, annotationNode, level, asEnum, innerTypeName, annotationInstance.onlyExplicitlyIncluded(), uppercase);
	}
	
	private void createInnerTypeFieldNameConstants(EclipseNode typeNode, EclipseNode errorNode, ASTNode source, AccessLevel level, List<EclipseNode> fields, boolean asEnum, IdentifierName innerTypeName, boolean uppercase) {
		if (fields.isEmpty()) return;
		
		ASTVisitor generatedByVisitor = new SetGeneratedByVisitor(source);
		TypeDeclaration parent = (TypeDeclaration) typeNode.get();
		EclipseNode fieldsType = findInnerClass(typeNode, innerTypeName.getName());
		boolean genConstr = false, genClinit = false;
		char[] name = innerTypeName.getCharArray();
		TypeDeclaration generatedInnerType = null;
		if (fieldsType == null) {
			generatedInnerType = new TypeDeclaration(parent.compilationResult);
			generatedInnerType.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
			generatedInnerType.modifiers = toEclipseModifier(level) | (asEnum ? ClassFileConstants.AccEnum : (ClassFileConstants.AccStatic | ClassFileConstants.AccFinal));
			generatedInnerType.name = name;
			fieldsType = injectType(typeNode, generatedInnerType);
			genConstr = true;
			genClinit = asEnum;
			generatedInnerType.traverse(generatedByVisitor, ((TypeDeclaration) typeNode.get()).scope);
		} else {
			TypeDeclaration builderTypeDeclaration = (TypeDeclaration) fieldsType.get();
			if (asEnum && (builderTypeDeclaration.modifiers & ClassFileConstants.AccEnum) == 0) {
				errorNode.addError("Existing " + innerTypeName + " must be declared as an 'enum'.");
				return;
			}
			if (!asEnum && (builderTypeDeclaration.modifiers & ClassFileConstants.AccStatic) == 0) {
				errorNode.addError("Existing " + innerTypeName + " must be declared as a 'static class'.");
				return;
			}
			genConstr = constructorExists(fieldsType) == MemberExistsResult.NOT_EXISTS;
		}
		
		if (genConstr) {
			ConstructorDeclaration constructor = new ConstructorDeclaration(parent.compilationResult);
			constructor.selector = name;
			constructor.modifiers = ClassFileConstants.AccPrivate;
			ExplicitConstructorCall superCall = new ExplicitConstructorCall(0);
			superCall.sourceStart = source.sourceStart;
			superCall.sourceEnd = source.sourceEnd;
			superCall.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
			constructor.constructorCall = superCall;
			if (!asEnum) constructor.statements = new Statement[0];
			injectMethod(fieldsType, constructor);
		}
		
		if (genClinit) {
			Clinit cli = new Clinit(parent.compilationResult);
			injectMethod(fieldsType, cli);
			cli.traverse(generatedByVisitor, ((TypeDeclaration) fieldsType.get()).scope);
		}
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			char[] fName = field.name;
			if (uppercase) fName = HandlerUtil.camelCaseToConstant(new String(fName)).toCharArray();
			if (fieldExists(new String(fName), fieldsType) != MemberExistsResult.NOT_EXISTS) continue;
			int pS = source.sourceStart, pE = source.sourceEnd;
			long p = (long) pS << 32 | pE;
			FieldDeclaration constantField = new FieldDeclaration(fName, pS, pE);
			constantField.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
			if (asEnum) {
				AllocationExpression ac = new AllocationExpression();
				ac.enumConstant = constantField;
				ac.sourceStart = source.sourceStart;
				ac.sourceEnd = source.sourceEnd;
				constantField.initialization = ac;
				constantField.modifiers = 0;
			} else {
				constantField.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {p, p, p});
				constantField.initialization = new StringLiteral(field.name, pS, pE, 0);
				constantField.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal;
			}
			injectField(fieldsType, constantField);
			constantField.traverse(generatedByVisitor, ((TypeDeclaration) fieldsType.get()).initializerScope);
		}
	}
}
