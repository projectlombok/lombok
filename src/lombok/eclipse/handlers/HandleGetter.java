package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleGetter implements EclipseAnnotationHandler<Getter> {
	public void generateGetterForField(Node fieldNode, ASTNode pos) {
		AccessLevel level = AccessLevel.PUBLIC;
		Node errorNode = fieldNode;
		boolean whineIfExists = false;
		
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(Getter.class, child) ) {
					level = Eclipse.createAnnotation(Getter.class, child).getInstance().value();
					errorNode = child;
					pos = child.get();
					whineIfExists = true;
					break;
				}
			}
		}
		
		createGetterForField(level, fieldNode, errorNode, pos, whineIfExists);
	}
	
	@Override public boolean handle(AnnotationValues<Getter> annotation, Annotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		return createGetterForField(level, fieldNode, annotationNode, annotationNode.get(), true);
	}
	
	private boolean createGetterForField(AccessLevel level, Node fieldNode, Node errorNode, ASTNode pos, boolean whineIfExists) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Getter is only supported on a field.");
			return false;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = field.type;
		String getterName = TransformationsUtil.toGetterName(
				new String(field.name), nameEquals(fieldType.getTypeName(), "boolean"));
		
		int modifier = toModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		switch ( methodExists(getterName, fieldNode) ) {
		case EXISTS_BY_LOMBOK:
			return true;
		case EXISTS_BY_USER:
			if ( whineIfExists ) errorNode.addWarning(
					String.format("Not generating %s(): A method with that name already exists",  getterName));
			return false;
		default:
		case NOT_EXISTS:
			//continue with creating the getter
		}
		
		if ( new String(field.name).equals("a") ) fieldNode.up().traverse(new EclipseASTVisitor.Printer());
		MethodDeclaration method = generateGetter((TypeDeclaration) fieldNode.up().get(), field, getterName, modifier, pos);
		
		injectMethod(fieldNode.up(), method);
		
		return true;
	}
	
	private MethodDeclaration generateGetter(TypeDeclaration parent, FieldDeclaration field, String name,
			int modifier, ASTNode pos) {
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = modifier;
		method.returnType = field.type;
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		Expression fieldExpression = new SingleNameReference(field.name, (field.declarationSourceStart << 32) | field.declarationSourceEnd);
		Statement returnStatement = new ReturnStatement(fieldExpression, field.sourceStart, field.sourceEnd);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
}
