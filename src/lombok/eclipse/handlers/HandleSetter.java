package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSetter implements EclipseAnnotationHandler<Setter> {
	public void generateSetterForField(Node fieldNode, ASTNode pos) {
		AccessLevel level = AccessLevel.PUBLIC;
		Node errorNode = fieldNode;
		boolean whineIfExists = false;
		
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(Setter.class, child) ) {
					level = Eclipse.createAnnotation(Setter.class, child).getInstance().value();
					errorNode = child;
					pos = child.get();
					whineIfExists = true;
					break;
				}
			}
		}
		
		createSetterForField(level, fieldNode, errorNode, pos, whineIfExists);
	}
	
	public boolean handle(AnnotationValues<Setter> annotation, Annotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		if ( fieldNode.getKind() != Kind.FIELD ) return false;
		AccessLevel level = annotation.getInstance().value();
		return createSetterForField(level, fieldNode, annotationNode, annotationNode.get(), true);
	}
	
	private boolean createSetterForField(AccessLevel level, Node fieldNode, Node errorNode, ASTNode pos, boolean whineIfExists) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Setter is only supported on a field.");
			return false;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		String setterName = TransformationsUtil.toSetterName(new String(field.name));
		
		int modifier = toModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		switch ( methodExists(setterName, fieldNode) ) {
		case EXISTS_BY_LOMBOK:
			return true;
		case EXISTS_BY_USER:
			if ( whineIfExists ) errorNode.addWarning(
					String.format("Not generating %s(%s %s): A method with that name already exists",
					setterName, field.type, new String(field.name)));
			return false;
		default:
		case NOT_EXISTS:
			//continue with creating the setter
		}
		
		
		MethodDeclaration method = generateSetter((TypeDeclaration) fieldNode.up().get(), field, setterName, modifier, pos);
		
		injectMethod(fieldNode.up(), method);
		
		return false;
	}
	
	private MethodDeclaration generateSetter(TypeDeclaration parent, FieldDeclaration field, String name,
			int modifier, ASTNode ast) {
		long pos = (((long)ast.sourceStart) << 32) | ast.sourceEnd;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = modifier;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.annotations = null;
		Argument param = new Argument(field.name, pos, Eclipse.copyType(field.type), 0);
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = parent.scope == null ? null : new MethodScope(parent.scope, method, false);
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		FieldReference thisX = new FieldReference(field.name, pos);
		thisX.receiver = new ThisReference(ast.sourceStart, ast.sourceEnd);
		Assignment assignment = new Assignment(thisX, new SingleNameReference(field.name, pos), (int)pos);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = ast.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = ast.sourceEnd;
		method.statements = new Statement[] { assignment };
		return method;
	}
}
