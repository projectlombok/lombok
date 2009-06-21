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
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleData implements EclipseAnnotationHandler<Data> {
	@Override public boolean handle(AnnotationValues<Data> annotation, Annotation ast, Node annotationNode) {
		Data ann = annotation.getInstance();
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
		
		switch ( constructorExists(typeNode) ) {
		case NOT_EXISTS:
			ConstructorDeclaration constructor = createConstructor(
					ann.staticConstructor().isEmpty(), typeNode, nodesForConstructorAndToString, ast);
			injectMethod(typeNode, constructor);
			break;
		}
		
		if ( !ann.staticConstructor().isEmpty() ) {
			switch ( methodExists("of", typeNode) ) {
			case NOT_EXISTS:
				MethodDeclaration staticConstructor = createStaticConstructor(
						ann.staticConstructor(), typeNode, nodesForConstructorAndToString, ast);
				injectMethod(typeNode, staticConstructor);
				break;
			case EXISTS_BY_LOMBOK:
				injectScopeIntoStaticConstructor((MethodDeclaration) getExistingLombokMethod(
						ann.staticConstructor(), typeNode).get(),
						nodesForConstructorAndToString, typeDecl);
			}
		}
		
		//TODO generate hashCode, equals.
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
	
	private ConstructorDeclaration createConstructor(boolean isPublic, Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = PKG.toModifier(isPublic ? AccessLevel.PUBLIC : AccessLevel.PRIVATE);
		constructor.annotations = null;
		constructor.selector = ((TypeDeclaration)type.get()).name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pos.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pos.sourceEnd;
		constructor.arguments = null;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Statement> assigns = new ArrayList<Statement>();
		
		for ( Node fieldNode : fields ) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			FieldReference thisX = new FieldReference(("this." + new String(field.name)).toCharArray(), p);
			thisX.receiver = new ThisReference((int)(p >> 32), (int)p);
			thisX.token = field.name;
			assigns.add(new Assignment(thisX, new SingleNameReference(field.name, p), (int)p));
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			args.add(new Argument(field.name, fieldPos, field.type, 0));
		}
		
		constructor.statements = assigns.toArray(new Statement[assigns.size()]);
		constructor.arguments = args.toArray(new Argument[args.size()]);
		return constructor;
	}
	
	private void injectScopeIntoStaticConstructor(MethodDeclaration constructor, Collection<Node> fields, TypeDeclaration typeDecl) {
		if ( typeDecl.scope != null ) {
			constructor.scope = new MethodScope(typeDecl.scope, constructor, false);
//			constructor.binding = new MethodBinding(constructor.modifiers,
//					constructor.selector, null, null, null, typeDecl.binding);
			constructor.returnType.resolvedType = typeDecl.binding;
//			TypeBinding[] bindings = new TypeBinding[fields.size()];
//			int idx = 0;
//			for ( Node field : fields ) bindings[idx++] = ((FieldDeclaration)field.get()).type.resolvedType;
//			constructor.binding.parameters = bindings;
		}
	}
	
	private MethodDeclaration createStaticConstructor(String name, Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		
		MethodDeclaration constructor = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		constructor.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		injectScopeIntoStaticConstructor(constructor, fields, (TypeDeclaration) type.get());
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pos.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pos.sourceEnd;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.type = constructor.returnType;
		
		for ( Node fieldNode : fields ) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			assigns.add(new SingleNameReference(field.name, fieldPos));
			args.add(new Argument(field.name, fieldPos, field.type, 0));
		}
		
		statement.arguments = assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = args.toArray(new Argument[args.size()]);
		constructor.statements = new Statement[] { statement };
		return constructor;
	}
	
	private MethodDeclaration createEquals(Collection<Node> fields) {
		//If using an of() constructor, make private.
		//method params
		//on loop: Assignment(FieldReference(ThisReference, "x"), SingleNameReference("x"))
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
