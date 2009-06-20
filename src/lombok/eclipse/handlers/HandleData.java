package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleData implements EclipseAnnotationHandler<Data> {
	@Override public boolean handle(AnnotationValues<Data> annotation, Annotation ast, Node annotationNode) {
		Node typeNode = annotationNode.up();
		
		TypeDeclaration typeDecl = null;
		if ( typeNode.get() instanceof TypeDeclaration ) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if ( typeDecl == null || notAClass ) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForEquality = new ArrayList<Node>();
		List<Node> nodesForConstructorAndToString = new ArrayList<Node>();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			//Skip static fields.
			if ( (fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
			if ( (fieldDecl.modifiers & ClassFileConstants.AccTransient) == 0 ) nodesForEquality.add(child);
			nodesForConstructorAndToString.add(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if ( (fieldDecl.modifiers & ClassFileConstants.AccFinal) == 0 )
				new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		switch ( methodExists("toString", typeNode) ) {
		case NOT_EXISTS:
			MethodDeclaration toString = createToString(typeNode, nodesForConstructorAndToString, ast);
			injectMethod(typeNode, toString);
			break;
		case EXISTS_BY_LOMBOK:
			injectScopeIntoToString((MethodDeclaration) getExistingLombokMethod("toString", typeNode).get(), typeDecl);
		}
		
		//TODO generate constructor, hashCode, equals.
		return false;
	}
	
	private void injectScopeIntoToString(MethodDeclaration method, TypeDeclaration typeDecl) {
		if ( typeDecl.scope != null ) {
			method.scope = new MethodScope(typeDecl.scope, method, false);
			method.returnType.resolvedType = typeDecl.scope.getJavaLangString();
			method.binding = new MethodBinding(method.modifiers,
					method.selector, typeDecl.scope.getJavaLangString(), null, null, typeDecl.binding);
		}
	}
	
	private MethodDeclaration createToString(Node type, Collection<Node> fields, ASTNode pos) {
		char[] rawTypeName = ((TypeDeclaration)type.get()).name;
		String typeName = rawTypeName == null ? "" : new String(rawTypeName);
		char[] prefix = (typeName + "(").toCharArray();
		char[] suffix = ")".toCharArray();
		char[] infix = ", ".toCharArray();
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		final int PLUS = OperatorIds.PLUS;
		
		boolean first = true;
		Expression current = new StringLiteral(prefix, 0, 0, 0);
		for ( Node field : fields ) {
			char[] fName = ((FieldDeclaration)field.get()).name;
			if ( fName == null ) continue;
			if ( !first ) {
				current = new BinaryExpression(current, new StringLiteral(infix, 0, 0, 0), PLUS);
			}
			else first = false;
			current = new BinaryExpression(current, new SingleNameReference(fName, p), PLUS);
		}
		current = new BinaryExpression(current, new StringLiteral(suffix, 0, 0, 0), PLUS);
		
		ReturnStatement returnStatement = new ReturnStatement(current, (int)(p >> 32), (int)p);
		
		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = Eclipse.TYPEREF_JAVA_LANG_STRING;
		method.annotations = null;
		method.arguments = null;
		method.selector = "toString".toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		injectScopeIntoToString(method, (TypeDeclaration) type.get());
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
	
	private MethodDeclaration createEquals(Collection<Node> fields) {
		return null;
	}
	
	private ConstructorDeclaration createConstructor(Collection<Node> fields) {
		//If using an of() constructor, make private.
		//method params
		//on loop: Assignment(FieldReference(ThisReference, "x"), SingleNameReference("x"))
		return null;
	}
	
	private MethodDeclaration createStaticConstructor(Collection<Node> fields) {
		//Return(AllocationExpression(SingleTypeReference("Bar"), namesOfFields);
		return null;
	}
	
	private MethodDeclaration createHashCode(Collection<Node> fields) {
		//booleans: conditionalexpression that bounces between 1231 and 1237.
		//longs: (int) (lng ^ (lng >>> 32));
		//doubles and floats: Double.doubleToLongBits, then as long.
		
		//local final var PRIME = IntLiteral(primeNumber)
		//local final var RESULT = IntLiteral(1)
		
		//    Assignment("RESULT", BinaryExpression("+", BinaryExpression("*", "PRIME", "RESULT"), "name")
		
		//    add = ConditionalExpression(EqualExpression("name", NullLiteral), IntLiteral(0), MessageSend("name", "hashCode()"))
		//    Assignment("RESULT", BinaryExpression("+", BinaryExpression("*", "PRIME", "RESULT"), add);
		
		return null;
	}
}
