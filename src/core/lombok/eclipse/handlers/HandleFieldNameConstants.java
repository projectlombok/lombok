package lombok.eclipse.handlers;

import static java.lang.Character.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.FieldNameConstants;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class) public class HandleFieldNameConstants extends EclipseAnnotationHandler<FieldNameConstants> {
	
	public boolean generateFieldDefaultsForType(EclipseNode typeNode, EclipseNode errorNode, AccessLevel level, boolean checkForTypeLevelFieldNameConstants) {
		
		if (checkForTypeLevelFieldNameConstants) {
			if (hasAnnotation(FieldNameConstants.class, typeNode)) {
				return true;
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class or an enum or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field)) generateFieldNameConstantsForField(field, errorNode.get(), level);
			if (field.getKind() != Kind.FIELD) return false;
		}
		return true;
	}
	
	private void generateFieldNameConstantsForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level) {
		if (hasAnnotation(FieldNameConstants.class, fieldNode)) {
			return;
		}
		createFieldNameConstantsForField(level, fieldNode, fieldNode, pos, false);
	}
	
	private void createFieldNameConstantsForField(AccessLevel level, EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@FieldNameConstants is only supported on a class or a field");
			return;
		}
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		String constantName = camelCaseToConstant(new String(field.name));
		if (constantName == null) {
			errorNode.addWarning("Not generating constant for this field: It does not fit in your @Accessors prefix list");
			return;
		}
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		FieldDeclaration fieldConstant = new FieldDeclaration(constantName.toCharArray(), pS,pE);
		fieldConstant.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		fieldConstant.modifiers = toEclipseModifier(level) | Modifier.STATIC | Modifier.FINAL;
		fieldConstant.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[]{p,p,p});
		fieldConstant.initialization = new StringLiteral(field.name, pS,pE,0);
		injectField(fieldNode.up(), fieldConstant);
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(EclipseNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		return filterField(fieldDecl);
	}
	
	public void handle(AnnotationValues<FieldNameConstants> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode node = annotationNode.up();
		FieldNameConstants annotatationInstance = annotation.getInstance();
		AccessLevel level = annotatationInstance.level();
		if (node == null) return;
		switch (node.getKind()){
		case FIELD:
			createFieldNameConstantsForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true);
			break;
		case TYPE:
			generateFieldDefaultsForType(node, annotationNode, level, false);
			break;
			
		}
		
	}
	
	private void createFieldNameConstantsForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists) {
		for (EclipseNode fieldNode : fieldNodes){
			createFieldNameConstantsForField(level, fieldNode, errorNode, source, whineIfExists);
		}
	}

	public static String camelCaseToConstant(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) return "";
		char[] chars = fieldName.toCharArray();
		StringBuilder b = new StringBuilder();
		b.append(toUpperCase(chars[0]));
		for (int i = 1, iend = chars.length; i < iend; i++) {
			char c = chars[i];
			if (isUpperCase(c)) {
				b.append('_');
			} else {
				c = toUpperCase(c);
			}
			b.append(c);
		}
		return b.toString();
	}
	
}
