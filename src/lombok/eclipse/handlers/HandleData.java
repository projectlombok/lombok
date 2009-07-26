/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.PKG.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;
import lombok.eclipse.handlers.PKG.MemberExistsResult;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
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
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the <code>lombok.Data</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleData implements EclipseAnnotationHandler<Data> {
	public boolean handle(AnnotationValues<Data> annotation, Annotation ast, Node annotationNode) {
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
		List<Node> nodesForConstructor = new ArrayList<Node>();
		List<Node> nodesForToString = new ArrayList<Node>();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			//Skip static fields.
			if ( (fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
			if ( (fieldDecl.modifiers & ClassFileConstants.AccTransient) == 0 ) nodesForEquality.add(child);
			boolean isFinal = (fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0;
			nodesForToString.add(child);
			if ( isFinal && fieldDecl.initialization == null ) nodesForConstructor.add(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if ( !isFinal ) new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		if ( methodExists("toString", typeNode) == MemberExistsResult.NOT_EXISTS ) {
			MethodDeclaration toString = createToString(typeNode, nodesForToString, ast);
			injectMethod(typeNode, toString);
		}
		
		if ( methodExists("equals", typeNode) == MemberExistsResult.NOT_EXISTS ) {
			MethodDeclaration equals = createEquals(typeNode, nodesForEquality, ast);
			injectMethod(typeNode, equals);
		}
		
		if ( methodExists("hashCode", typeNode) == MemberExistsResult.NOT_EXISTS ) {
			MethodDeclaration hashCode = createHashCode(typeNode, nodesForEquality, ast);
			injectMethod(typeNode, hashCode);
		}
		
		//Careful: Generate the public static constructor (if there is one) LAST, so that any attempt to
		//'find callers' on the annotation node will find callers of the constructor, which is by far the
		//most useful of the many methods built by @Data. This trick won't work for the non-static constructor,
		//for whatever reason, though you can find callers of that one by focussing on the class name itself
		//and hitting 'find callers'.
		
		if ( constructorExists(typeNode) == MemberExistsResult.NOT_EXISTS ) {
			ConstructorDeclaration constructor = createConstructor(
					ann.staticConstructor().length() == 0, typeNode, nodesForConstructor, ast);
			injectMethod(typeNode, constructor);
		}
		
		if ( ann.staticConstructor().length() > 0 ) {
			if ( methodExists("of", typeNode) == MemberExistsResult.NOT_EXISTS ) {
				MethodDeclaration staticConstructor = createStaticConstructor(
						ann.staticConstructor(), typeNode, nodesForConstructor, ast);
				injectMethod(typeNode, staticConstructor);
			}
		}
		
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
			FieldDeclaration f = (FieldDeclaration)field.get();
			if ( f.name == null || f.type == null ) continue;
			if ( !first ) {
				current = new BinaryExpression(current, new StringLiteral(infix, 0, 0, 0), PLUS);
			}
			else first = false;
			
			Expression ex;
			if ( f.type.dimensions() > 0 ) {
				MessageSend arrayToString = new MessageSend();
				arrayToString.receiver = generateQualifiedNameRef(TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				arrayToString.arguments = new Expression[] { new SingleNameReference(f.name, p) };
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(f.type.getLastToken())) ) {
					arrayToString.selector = "deepToString".toCharArray();
				} else {
					arrayToString.selector = "toString".toCharArray();
				}
				ex = arrayToString;
			} else ex = new SingleNameReference(f.name, p);
			
			current = new BinaryExpression(current, ex, PLUS);
		}
		current = new BinaryExpression(current, new StringLiteral(suffix, 0, 0, 0), PLUS);
		
		ReturnStatement returnStatement = new ReturnStatement(current, (int)(p >> 32), (int)p);
		
		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {0, 0, 0});
		method.annotations = new Annotation[] {
				new MarkerAnnotation(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OVERRIDE, new long[] { 0, 0, 0}), 0)
		};
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
		
		constructor.statements = assigns.isEmpty() ? null : assigns.toArray(new Statement[assigns.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
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
				refs[idx++] = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
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
		
		statement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int)(p >> 32), (int)p) };
		return constructor;
	}
	
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	private MethodDeclaration createEquals(Node type, Collection<Node> fields, ASTNode pos) {
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.annotations = new Annotation[] {
				new MarkerAnnotation(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OVERRIDE, new long[] { 0, 0, 0}), 0)
		};
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
		
		
		TypeDeclaration typeDecl = (TypeDeclaration)type.get();
		/* MyType<?> other = (MyType<?>) o; */ {
			if ( !fields.isEmpty() ) {
				LocalDeclaration other = new LocalDeclaration(otherN, 0, 0);
				char[] typeName = typeDecl.name;
				Expression targetType;
				if ( typeDecl.typeParameters == null || typeDecl.typeParameters.length == 0 ) {
					targetType = new SingleNameReference(((TypeDeclaration)type.get()).name, 0);
					other.type = new SingleTypeReference(typeName, 0);
				} else {
					TypeReference[] typeArgs = new TypeReference[typeDecl.typeParameters.length];
					for ( int i = 0 ; i < typeArgs.length ; i++ ) typeArgs[i] = new Wildcard(Wildcard.UNBOUND);
					targetType = new ParameterizedSingleTypeReference(typeName, typeArgs, 0, 0);
					other.type = new ParameterizedSingleTypeReference(typeName, copyTypes(typeArgs), 0, 0);
				}
				other.initialization = new CastExpression(
						new SingleNameReference(new char[] { 'o' }, 0),
						targetType);
				statements.add(other);
			}
		}
		
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if ( f.type.dimensions() == 0 && token != null ) {
				if ( Arrays.equals(TypeConstants.FLOAT, token) ) {
					statements.add(generateCompareFloatOrDouble(otherN, "Float".toCharArray(), f.name));
				} else if ( Arrays.equals(TypeConstants.DOUBLE, token) ) {
					statements.add(generateCompareFloatOrDouble(otherN, "Double".toCharArray(), f.name));
				} else if ( BUILT_IN_TYPES.contains(new String(token)) ) {
					EqualExpression fieldsNotEqual = new EqualExpression(
							new SingleNameReference(f.name, 0),
							generateQualifiedNameRef(otherN, f.name),
							OperatorIds.NOT_EQUAL);
					ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
					statements.add(new IfStatement(fieldsNotEqual, returnStatement, 0, 0));
				} else /* objects */ {
					EqualExpression fieldIsNull = new EqualExpression(
							new SingleNameReference(f.name, 0),
							new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
					EqualExpression otherFieldIsntNull = new EqualExpression(
							generateQualifiedNameRef(otherN, f.name),
							new NullLiteral(0, 0), OperatorIds.NOT_EQUAL);
					MessageSend equalsCall = new MessageSend();
					equalsCall.receiver = new SingleNameReference(f.name, 0);
					equalsCall.selector = "equals".toCharArray();
					equalsCall.arguments = new Expression[] { generateQualifiedNameRef(otherN, f.name) };
					UnaryExpression fieldsNotEqual = new UnaryExpression(equalsCall, OperatorIds.NOT);
					ConditionalExpression fullEquals = new ConditionalExpression(fieldIsNull, otherFieldIsntNull, fieldsNotEqual);
					ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
					statements.add(new IfStatement(fullEquals, returnStatement, 0, 0));
				}
			} else if ( f.type.dimensions() > 0 && token != null ) {
				MessageSend arraysEqualCall = new MessageSend();
				arraysEqualCall.receiver = generateQualifiedNameRef(TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token)) ) {
					arraysEqualCall.selector = "deepEquals".toCharArray();
				} else {
					arraysEqualCall.selector = "equals".toCharArray();
				}
				arraysEqualCall.arguments = new Expression[] {
						new SingleNameReference(f.name, 0),
						generateQualifiedNameRef(otherN, f.name) };
				UnaryExpression arraysNotEqual = new UnaryExpression(arraysEqualCall, OperatorIds.NOT);
				ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
				statements.add(new IfStatement(arraysNotEqual, returnStatement, 0, 0));
			}
		}
		
		/* return true; */ {
			statements.add(new ReturnStatement(new TrueLiteral(0, 0), 0, 0));
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private IfStatement generateCompareFloatOrDouble(char[] otherN, char[] floatOrDouble, char[] fieldName) {
		/* if ( Float.compare(fieldName, other.fieldName) != 0 ) return false */
		MessageSend floatCompare = new MessageSend();
		floatCompare.receiver = generateQualifiedNameRef(TypeConstants.JAVA, TypeConstants.LANG, floatOrDouble);
		floatCompare.selector = "compare".toCharArray();
		floatCompare.arguments = new Expression[] {
				new SingleNameReference(fieldName, 0),
				generateQualifiedNameRef(otherN, fieldName)
		};
		EqualExpression ifFloatCompareIsNot0 = new EqualExpression(floatCompare, new IntLiteral(new char[] {'0'}, 0, 0), OperatorIds.NOT_EQUAL);
		ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(0, 0), 0, 0);
		return new IfStatement(ifFloatCompareIsNot0, returnFalse, 0, 0);
	}
	
	private Reference generateFieldReference(char[] fieldName) {
		FieldReference thisX = new FieldReference(("this." + new String(fieldName)).toCharArray(), 0);
		thisX.receiver = new ThisReference(0, 0);
		thisX.token = fieldName;
		return thisX;
	}
	
	private NameReference generateQualifiedNameRef(char[]... varNames) {
		if ( varNames.length > 1 )
			return new QualifiedNameReference(varNames, new long[varNames.length], 0, 0);
		else return new SingleNameReference(varNames[0], 0);
	}
	
	private MethodDeclaration createHashCode(Node type, Collection<Node> fields, ASTNode pos) {
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		method.annotations = new Annotation[] {
				new MarkerAnnotation(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OVERRIDE, new long[] { 0, 0, 0}), 0)
		};
		method.selector = "hashCode".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.arguments = null;
		
		List<Statement> statements = new ArrayList<Statement>();
		List<Expression> intoResult = new ArrayList<Expression>();
		
		final char[] PRIME = "PRIME".toCharArray();
		final char[] RESULT = "result".toCharArray();
		final boolean isEmpty = fields.isEmpty();
		
		/* final int PRIME = 31; */ {
			if ( !isEmpty ) { /* Without fields, PRIME isn't used, and that would trigger a 'local variable not used' warning. */
				LocalDeclaration primeDecl = new LocalDeclaration(PRIME, 0 ,0);
				primeDecl.modifiers = Modifier.FINAL;
				primeDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
				primeDecl.initialization = new IntLiteral("31".toCharArray(), 0, 0);
				statements.add(primeDecl);
			}
		}
		
		/* int result = 1; */ {
			LocalDeclaration resultDecl = new LocalDeclaration(RESULT, 0, 0);
			resultDecl.initialization = new IntLiteral("1".toCharArray(), 0, 0);
			resultDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
			statements.add(resultDecl);
		}
		
		int tempCounter = 0;
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if ( f.type.dimensions() == 0 && token != null ) {
				if ( Arrays.equals(TypeConstants.FLOAT, token) ) {
					/* Float.floatToIntBits(fieldName) */
					MessageSend floatToIntBits = new MessageSend();
					floatToIntBits.receiver = generateQualifiedNameRef(TypeConstants.JAVA_LANG_FLOAT);
					floatToIntBits.selector = "floatToIntBits".toCharArray();
					floatToIntBits.arguments = new Expression[] { generateFieldReference(f.name) };
					intoResult.add(floatToIntBits);
				} else if ( Arrays.equals(TypeConstants.DOUBLE, token) ) {
					/* longToIntForHashCode(Double.doubleToLongBits(fieldName)) */
					MessageSend doubleToLongBits = new MessageSend();
					doubleToLongBits.receiver = generateQualifiedNameRef(TypeConstants.JAVA_LANG_DOUBLE);
					doubleToLongBits.selector = "doubleToLongBits".toCharArray();
					doubleToLongBits.arguments = new Expression[] { generateFieldReference(f.name) };
					final char[] tempName = ("temp" + ++tempCounter).toCharArray();
					LocalDeclaration tempVar = new LocalDeclaration(tempName, 0, 0);
					tempVar.initialization = doubleToLongBits;
					tempVar.type = TypeReference.baseTypeReference(TypeIds.T_long, 0);
					tempVar.modifiers = Modifier.FINAL;
					statements.add(tempVar);
					intoResult.add(longToIntForHashCode(
							new SingleNameReference(tempName, 0), new SingleNameReference(tempName, 0)));
				} else if ( Arrays.equals(TypeConstants.BOOLEAN, token) ) {
					/* booleanField ? 1231 : 1237 */
					intoResult.add(new ConditionalExpression(
							generateFieldReference(f.name),
							new IntLiteral("1231".toCharArray(), 0, 0),
							new IntLiteral("1237".toCharArray(), 0 ,0)));
				} else if ( Arrays.equals(TypeConstants.LONG, token) ) {
					intoResult.add(longToIntForHashCode(generateFieldReference(f.name), generateFieldReference(f.name)));
				} else if ( BUILT_IN_TYPES.contains(new String(token)) ) {
					intoResult.add(generateFieldReference(f.name));
				} else /* objects */ {
					/* this.fieldName == null ? 0 : this.fieldName.hashCode() */
					MessageSend hashCodeCall = new MessageSend();
					hashCodeCall.receiver = generateFieldReference(f.name);
					hashCodeCall.selector = "hashCode".toCharArray();
					EqualExpression objIsNull = new EqualExpression(
							generateFieldReference(f.name),
							new NullLiteral(0, 0),
							OperatorIds.EQUAL_EQUAL);
					ConditionalExpression nullOrHashCode = new ConditionalExpression(
							objIsNull,
							new IntLiteral("0".toCharArray(), 0, 0),
							hashCodeCall);
					intoResult.add(nullOrHashCode);
				}
			} else if ( f.type.dimensions() > 0 && token != null ) {
				/* Arrays.deepHashCode(array)  //just hashCode for simple arrays */
				MessageSend arraysHashCodeCall = new MessageSend();
				arraysHashCodeCall.receiver = generateQualifiedNameRef(TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token)) ) {
					arraysHashCodeCall.selector = "deepHashCode".toCharArray();
				} else {
					arraysHashCodeCall.selector = "hashCode".toCharArray();
				}
				arraysHashCodeCall.arguments = new Expression[] { generateFieldReference(f.name) };
				intoResult.add(arraysHashCodeCall);
			}
		}
		
		/* fold each intoResult entry into:
		   result = result * PRIME + (item); */ {
			for ( Expression ex : intoResult ) {
				BinaryExpression multiplyByPrime = new BinaryExpression(new SingleNameReference(RESULT, 0),
						new SingleNameReference(PRIME, 0), OperatorIds.MULTIPLY);
				BinaryExpression addItem = new BinaryExpression(multiplyByPrime, ex, OperatorIds.PLUS);
				statements.add(new Assignment(new SingleNameReference(RESULT, 0), addItem, 0));
			}
		}
		
		/* return result; */ {
			statements.add(new ReturnStatement(new SingleNameReference(RESULT, 0), 0, 0));
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	/** Give 2 clones! */
	private Expression longToIntForHashCode(Reference ref1, Reference ref2) {
		/* (int)(ref >>> 32 ^ ref) */
		BinaryExpression higherBits = new BinaryExpression(
				ref1, new IntLiteral("32".toCharArray(), 0, 0),
				OperatorIds.UNSIGNED_RIGHT_SHIFT);
		BinaryExpression xorParts = new BinaryExpression(ref2, higherBits, OperatorIds.XOR);
		return new CastExpression(xorParts, TypeReference.baseTypeReference(TypeIds.T_int, 0));
	}
}
