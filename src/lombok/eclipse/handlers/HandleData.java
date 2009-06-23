package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.PKG.*;

import java.lang.reflect.Modifier;
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
import lombok.eclipse.handlers.PKG.MethodExistsResult;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
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
		
		if ( methodExists("toString", typeNode) == MethodExistsResult.NOT_EXISTS ) {
			MethodDeclaration toString = createToString(typeNode, nodesForConstructorAndToString, ast);
			injectMethod(typeNode, toString);
		}
		
		if ( constructorExists(typeNode) == MethodExistsResult.NOT_EXISTS ) {
			ConstructorDeclaration constructor = createConstructor(
					ann.staticConstructor().isEmpty(), typeNode, nodesForConstructorAndToString, ast);
			injectMethod(typeNode, constructor);
		}
		
		if ( !ann.staticConstructor().isEmpty() ) {
			if ( methodExists("of", typeNode) == MethodExistsResult.NOT_EXISTS ) {
				MethodDeclaration staticConstructor = createStaticConstructor(
						ann.staticConstructor(), typeNode, nodesForConstructorAndToString, ast);
				injectMethod(typeNode, staticConstructor);
			}
		}
		
		//TODO generate hashCode, equals.
		return false;
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
		method.returnType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {0, 0, 0});
		method.annotations = null;
		method.arguments = null;
		method.selector = "toString".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
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
			args.add(new Argument(field.name, fieldPos, copyType(field.type), 0));
		}
		
		constructor.statements = assigns.toArray(new Statement[assigns.size()]);
		constructor.arguments = args.toArray(new Argument[args.size()]);
		return constructor;
	}
	
	private MethodDeclaration createStaticConstructor(String name, Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		
		MethodDeclaration constructor = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = PKG.toModifier(AccessLevel.PUBLIC) | Modifier.STATIC;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		if ( typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0 ) {
			TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
			int idx = 0;
			for ( TypeParameter param : typeDecl.typeParameters ) {
				refs[idx++] = new SingleTypeReference(param.name, 0);
			}
			constructor.returnType = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
		} else constructor.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = copyTypeParams(((TypeDeclaration)type.get()).typeParameters);
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pos.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pos.sourceEnd;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.type = copyType(constructor.returnType);
		
		for ( Node fieldNode : fields ) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			assigns.add(new SingleNameReference(field.name, fieldPos));
			args.add(new Argument(field.name, fieldPos, copyType(field.type), 0));
		}
		
		statement.arguments = assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = args.toArray(new Argument[args.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int)(p >> 32), (int)p) };
		return constructor;
	}
	
	private MethodDeclaration createEquals(Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.annotations = null;
		method.selector = "equals".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.arguments = new Argument[] {
				new Argument(new char[] { 'o' }, 0,
						new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { 0, 0, 0 }), 0)
		};
		
		List<Statement> statements = new ArrayList<Statement>();
		
		/* if ( o == this ) return true; */ {
			EqualExpression otherEqualsThis = new EqualExpression(
					new SingleNameReference(new char[] { 'o' }, 0),
					new ThisReference(0, 0), OperatorIds.EQUAL_EQUAL);
			
			ReturnStatement returnTrue = new ReturnStatement(new TrueLiteral(0, 0), 0, 0);
			IfStatement ifOtherEqualsThis = new IfStatement(otherEqualsThis, returnTrue, 0, 0);
			statements.add(ifOtherEqualsThis);
		}
		
		/* if ( o == null ) return false; */ {
			EqualExpression otherEqualsNull = new EqualExpression(
					new SingleNameReference(new char[] { 'o' }, 0),
					new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
			
			ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
			IfStatement ifOtherEqualsNull = new IfStatement(otherEqualsNull, returnFalse, 0, 0);
			statements.add(ifOtherEqualsNull);
		}
		
		/* if ( o.getClass() != getClass() ) return false; */ {
			MessageSend otherGetClass = new MessageSend();
			otherGetClass.receiver = new SingleNameReference(new char[] { 'o' }, 0);
			otherGetClass.selector = "getClass".toCharArray();
			MessageSend thisGetClass = new MessageSend();
			thisGetClass.receiver = new ThisReference(0, 0);
			thisGetClass.selector = "getClass".toCharArray();
			EqualExpression classesNotEqual = new EqualExpression(otherGetClass, thisGetClass, OperatorIds.NOT_EQUAL);
			ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
			IfStatement ifClassesNotEqual = new IfStatement(classesNotEqual, returnFalse, 0, 0);
			statements.add(ifClassesNotEqual);
		}
		
		char[] otherN = "other".toCharArray();
		
		//TODO fix generics raw type warnings by inserting Wildcards.
		/* MyType other = (MyType) o; */ {
			if ( !fields.isEmpty() ) {
				LocalDeclaration other = new LocalDeclaration(otherN, 0, 0);
				other.initialization = new CastExpression(
						new SingleNameReference(new char[] { 'o' }, 0),
						new SingleNameReference(((TypeDeclaration)type.get()).name, 0));
			}
		}
		
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			//compare if primitive, write per-primitive special code, otherwise use == null ? other == null ? .equals(other).
			//TODO I LEFT IT HERE
		}
		
		/* return true; */ {
			statements.add(new ReturnStatement(new TrueLiteral(0, 0), 0, 0));
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private MethodDeclaration createHashCode(Node type, Collection<Node> fields, ASTNode pos) {
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
