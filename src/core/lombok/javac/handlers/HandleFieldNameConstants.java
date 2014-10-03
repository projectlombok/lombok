package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.FieldNameConstants;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
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
import com.sun.tools.javac.util.List;

@ProviderFor(JavacAnnotationHandler.class) @SuppressWarnings("restriction") public class HandleFieldConstants extends JavacAnnotationHandler<FieldNameConstants> {
	
	public void generateFieldDefaultsForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelFieldNameConstants) {
		
		if (checkForTypeLevelFieldNameConstants) {
			if (hasAnnotation(FieldNameConstants.class, typeNode)) {
				return;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@FieldNameConstants is only supported on a class or an enum or a field.");
			return;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (fieldQualifiesForFieldNameConstantsGeneration(field)) generateFieldNameConstantsForField(field, errorNode.get(), level);
			
		}
	}
	
	private void generateFieldNameConstantsForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level) {
		if (hasAnnotation(FieldNameConstants.class, fieldNode)) {
			return;
		}
		createFieldNameConstantsForField(level, fieldNode, fieldNode, false, List.<JCAnnotation>nil());
	}
	
	private boolean fieldQualifiesForFieldNameConstantsGeneration(JavacNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		if (fieldDecl.name.toString().startsWith("$")) return false;
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) return false;
		return true;
	}
	
	public void handle(AnnotationValues<FieldNameConstants> annotation, JCAnnotation ast, JavacNode annotationNode) {
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, FieldNameConstants.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		FieldNameConstants annotatationInstance = annotation.getInstance();
		AccessLevel level = annotatationInstance.level();
		if (level == AccessLevel.NONE) {
			annotationNode.addWarning("'lazy' does not work with AccessLevel.NONE.");
			return;
		}
		if (node == null) return;
		List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@FieldNameConstants(onMethod=", annotationNode);
		switch (node.getKind()) {
		case FIELD:
			createFieldNameConstantsForFields(level, fields, annotationNode, annotationNode, true, onMethod);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) {
				annotationNode.addError("'onMethod' is not supported for @FieldNameConstants on a type.");
			}
			generateFieldDefaultsForType(node, annotationNode, level, false);
			break;
		}
	}
	
	private void createFieldNameConstantsForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode annotationNode, JavacNode errorNode, boolean whineIfExists, List<JCAnnotation> onMethod) {
		for (JavacNode fieldNode : fieldNodes) {
			createFieldNameConstantsForField(level, fieldNode, errorNode, whineIfExists, onMethod);
		}
	}
	
	private void createFieldNameConstantsForField(AccessLevel level, JavacNode fieldNode, JavacNode source, boolean whineIfExists, List<JCAnnotation> onMethod) {
		if (fieldNode.getKind() != Kind.FIELD) {
			source.addError("@FieldNameConstants is only supported on a class or a field");
			return;
		}
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		String constantName = camelCaseToConstant(field.name.toString());
		if (constantName == null) {
			source.addWarning("Not generating constant for this field: It does not fit in your @Accessors prefix list");
			return;
		}
		
		JavacTreeMaker treeMaker = fieldNode.getTreeMaker();
		JCModifiers modifiers = treeMaker.Modifiers(toJavacModifier(level) | Modifier.STATIC | Modifier.FINAL);
		JCExpression returnType = chainDots(fieldNode, "java", "lang", "String");
		JCExpression init = treeMaker.Literal(fieldNode.getName());
		JCVariableDecl fieldConstant = treeMaker.VarDef(modifiers, fieldNode.toName(constantName), returnType, init);
		injectField(fieldNode.up(), fieldConstant);
	}
	
	public static String camelCaseToConstant(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) return "";
		char[] chars = fieldName.toCharArray();
		StringBuilder b = new StringBuilder();
		b.append(Character.toUpperCase(chars[0]));
		for (int i = 1, iend = chars.length; i < iend; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c)) {
				b.append('_');
			} else {
				c = Character.toUpperCase(c);
			}
			b.append(c);
		}
		return b.toString();
	}
}