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

import static lombok.eclipse.handlers.PKG.*;

import static lombok.eclipse.Eclipse.copyTypes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
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
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

/**
 * Handles the <code>EqualsAndHashCode</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleEqualsAndHashCode implements EclipseAnnotationHandler<EqualsAndHashCode> {
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	private void checkForBogusFieldNames(Node type, AnnotationValues<EqualsAndHashCode> annotation) {
		if ( annotation.isExplicit("exclude") ) {
			for ( int i : createListOfNonExistentFields(Arrays.asList(annotation.getInstance().exclude()), type, true, true) ) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if ( annotation.isExplicit("of") ) {
			for ( int i : createListOfNonExistentFields(Arrays.asList(annotation.getInstance().of()), type, false, false) ) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	public void generateEqualsAndHashCodeForType(Node typeNode, Node errorNode) {
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(EqualsAndHashCode.class, child) ) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		generateMethods(typeNode, errorNode, null, null, null, false);
	}
	
	@Override public boolean handle(AnnotationValues<EqualsAndHashCode> annotation, Annotation ast, Node annotationNode) {
		EqualsAndHashCode ann = annotation.getInstance();
		List<String> excludes = Arrays.asList(ann.exclude());
		List<String> includes = Arrays.asList(ann.of());
		Node typeNode = annotationNode.up();
		
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		if ( !annotation.isExplicit("callSuper") ) callSuper = null;
		if ( !annotation.isExplicit("exclude") ) excludes = null;
		if ( !annotation.isExplicit("of") ) includes = null;
		
		if ( excludes != null && includes != null ) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		return generateMethods(typeNode, annotationNode, excludes, includes, callSuper, true);
	}
	
	public boolean generateMethods(Node typeNode, Node errorNode, List<String> excludes, List<String> includes,
			Boolean callSuper, boolean whineIfExists) {
		assert excludes == null || includes == null;
		
		TypeDeclaration typeDecl = null;
		
		if ( typeNode.get() instanceof TypeDeclaration ) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if ( typeDecl == null || notAClass ) {
			errorNode.addError("@EqualsAndHashCode is only supported on a class.");
			return false;
		}
		
		boolean implicitCallSuper = callSuper == null;
		
		if ( callSuper == null ) {
			try {
				callSuper = ((Boolean)EqualsAndHashCode.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch ( Exception ignore ) {}
		}
		
		boolean isDirectDescendantOfObject = true;
		
		if ( typeDecl.superclass != null ) {
			String p = typeDecl.superclass.toString();
			isDirectDescendantOfObject = p.equals("Object") || p.equals("java.lang.Object");
		}
		
		if ( isDirectDescendantOfObject && callSuper ) {
			errorNode.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return true;
		}
		
		if ( !isDirectDescendantOfObject && !callSuper && implicitCallSuper ) {
			errorNode.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
		}
		
		List<Node> nodesForEquality = new ArrayList<Node>();
		if ( includes != null ) {
			for ( Node child : typeNode.down() ) {
				if ( child.getKind() != Kind.FIELD ) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				if ( includes.contains(new String(fieldDecl.name)) ) nodesForEquality.add(child);
			}
		} else {
			for ( Node child : typeNode.down() ) {
				if ( child.getKind() != Kind.FIELD ) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				//Skip static fields.
				if ( (fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
				//Skip transient fields.
				if ( (fieldDecl.modifiers & ClassFileConstants.AccTransient) != 0 ) continue;
				//Skip excluded fields.
				if ( excludes != null && excludes.contains(new String(fieldDecl.name)) ) continue;
				//Skip fields that start with $.
				if ( fieldDecl.name.length > 0 && fieldDecl.name[0] == '$' ) continue;
				nodesForEquality.add(child);
			}
		}
		
		switch ( methodExists("hashCode", typeNode) ) {
		case NOT_EXISTS:
			MethodDeclaration hashCode = createHashCode(typeNode, nodesForEquality, callSuper, errorNode.get());
			injectMethod(typeNode, hashCode);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if ( whineIfExists ) {
				errorNode.addWarning("Not generating hashCode(): A method with that name already exists");
			}
			break;
		}
		
		switch ( methodExists("equals", typeNode) ) {
		case NOT_EXISTS:
			MethodDeclaration equals = createEquals(typeNode, nodesForEquality, callSuper, errorNode.get());
			injectMethod(typeNode, equals);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if ( whineIfExists ) {
				errorNode.addWarning("Not generating equals(Object other): A method with that name already exists");
			}
			break;
		}
		
		return true;
	}
	
	private MethodDeclaration createHashCode(Node type, Collection<Node> fields, boolean callSuper, ASTNode pos) {
		int pS = pos.sourceStart, pE = pos.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, p)};
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
			/* Without fields, PRIME isn't used, and that would trigger a 'local variable not used' warning. */
			if ( !isEmpty || callSuper ) {
				LocalDeclaration primeDecl = new LocalDeclaration(PRIME, pS, pE);
				primeDecl.modifiers |= Modifier.FINAL;
				primeDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
				primeDecl.initialization = new IntLiteral("31".toCharArray(), pS, pE);
				statements.add(primeDecl);
			}
		}
		
		/* int result = 1; */ {
			LocalDeclaration resultDecl = new LocalDeclaration(RESULT, pS, pE);
			resultDecl.initialization = new IntLiteral("1".toCharArray(), pS, pE);
			resultDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
			statements.add(resultDecl);
		}
		
		if ( callSuper ) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.receiver = new SuperReference(pS, pE);
			callToSuper.selector = "hashCode".toCharArray();
			intoResult.add(callToSuper);
		}
		
		int tempCounter = 0;
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if ( f.type.dimensions() == 0 && token != null ) {
				if ( Arrays.equals(TypeConstants.FLOAT, token) ) {
					/* Float.floatToIntBits(fieldName) */
					MessageSend floatToIntBits = new MessageSend();
					floatToIntBits.receiver = generateQualifiedNameRef(p, TypeConstants.JAVA_LANG_FLOAT);
					floatToIntBits.selector = "floatToIntBits".toCharArray();
					floatToIntBits.arguments = new Expression[] { generateFieldReference(f.name, p) };
					intoResult.add(floatToIntBits);
				} else if ( Arrays.equals(TypeConstants.DOUBLE, token) ) {
					/* longToIntForHashCode(Double.doubleToLongBits(fieldName)) */
					MessageSend doubleToLongBits = new MessageSend();
					doubleToLongBits.receiver = generateQualifiedNameRef(p, TypeConstants.JAVA_LANG_DOUBLE);
					doubleToLongBits.selector = "doubleToLongBits".toCharArray();
					doubleToLongBits.arguments = new Expression[] { generateFieldReference(f.name, p) };
					final char[] tempName = ("temp" + ++tempCounter).toCharArray();
					LocalDeclaration tempVar = new LocalDeclaration(tempName, pS, pE);
					tempVar.initialization = doubleToLongBits;
					tempVar.type = TypeReference.baseTypeReference(TypeIds.T_long, 0);
					tempVar.modifiers = Modifier.FINAL;
					statements.add(tempVar);
					intoResult.add(longToIntForHashCode(
							new SingleNameReference(tempName, p), new SingleNameReference(tempName, p), p));
				} else if ( Arrays.equals(TypeConstants.BOOLEAN, token) ) {
					/* booleanField ? 1231 : 1237 */
					intoResult.add(new ConditionalExpression(
							generateFieldReference(f.name, p),
							new IntLiteral("1231".toCharArray(), pS, pE),
							new IntLiteral("1237".toCharArray(), pS, pE)));
				} else if ( Arrays.equals(TypeConstants.LONG, token) ) {
					intoResult.add(longToIntForHashCode(generateFieldReference(f.name, p), generateFieldReference(f.name, p), p));
				} else if ( BUILT_IN_TYPES.contains(new String(token)) ) {
					intoResult.add(generateFieldReference(f.name, p));
				} else /* objects */ {
					/* this.fieldName == null ? 0 : this.fieldName.hashCode() */
					MessageSend hashCodeCall = new MessageSend();
					hashCodeCall.receiver = generateFieldReference(f.name, p);
					hashCodeCall.selector = "hashCode".toCharArray();
					EqualExpression objIsNull = new EqualExpression(
							generateFieldReference(f.name, p),
							new NullLiteral(pS, pE),
							OperatorIds.EQUAL_EQUAL);
					ConditionalExpression nullOrHashCode = new ConditionalExpression(
							objIsNull,
							new IntLiteral("0".toCharArray(), pS, pE),
							hashCodeCall);
					intoResult.add(nullOrHashCode);
				}
			} else if ( f.type.dimensions() > 0 && token != null ) {
				/* Arrays.deepHashCode(array)  //just hashCode for simple arrays */
				MessageSend arraysHashCodeCall = new MessageSend();
				arraysHashCodeCall.receiver = generateQualifiedNameRef(p, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token)) ) {
					arraysHashCodeCall.selector = "deepHashCode".toCharArray();
				} else {
					arraysHashCodeCall.selector = "hashCode".toCharArray();
				}
				arraysHashCodeCall.arguments = new Expression[] { generateFieldReference(f.name, p) };
				intoResult.add(arraysHashCodeCall);
			}
		}
		
		/* fold each intoResult entry into:
		   result = result * PRIME + (item); */ {
			for ( Expression ex : intoResult ) {
				BinaryExpression multiplyByPrime = new BinaryExpression(new SingleNameReference(RESULT, p),
						new SingleNameReference(PRIME, 0), OperatorIds.MULTIPLY);
				BinaryExpression addItem = new BinaryExpression(multiplyByPrime, ex, OperatorIds.PLUS);
				statements.add(new Assignment(new SingleNameReference(RESULT, p), addItem, pE));
			}
		}
		
		/* return result; */ {
			statements.add(new ReturnStatement(new SingleNameReference(RESULT, p), pS, pE));
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private MethodDeclaration createEquals(Node type, Collection<Node> fields, boolean callSuper, ASTNode pos) {
		int pS = pos.sourceStart; int pE = pos.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, p)};
		method.selector = "equals".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.arguments = new Argument[] {
				new Argument(new char[] { 'o' }, 0,
						new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { p, p, p }), Modifier.FINAL)
		};
		
		List<Statement> statements = new ArrayList<Statement>();
		
		/* if ( o == this ) return true; */ {
			EqualExpression otherEqualsThis = new EqualExpression(
					new SingleNameReference(new char[] { 'o' }, p),
					new ThisReference(pS, pE), OperatorIds.EQUAL_EQUAL);
			
			ReturnStatement returnTrue = new ReturnStatement(new TrueLiteral(pS, pE), pS, pE);
			IfStatement ifOtherEqualsThis = new IfStatement(otherEqualsThis, returnTrue, pS, pE);
			statements.add(ifOtherEqualsThis);
		}
		
		/* if ( o == null ) return false; */ {
			EqualExpression otherEqualsNull = new EqualExpression(
					new SingleNameReference(new char[] { 'o' }, p),
					new NullLiteral(pS, pE), OperatorIds.EQUAL_EQUAL);
			
			ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
			IfStatement ifOtherEqualsNull = new IfStatement(otherEqualsNull, returnFalse, pS, pE);
			statements.add(ifOtherEqualsNull);
		}
		
		/* if ( o.getClass() != getClass() ) return false; */ {
			MessageSend otherGetClass = new MessageSend();
			otherGetClass.receiver = new SingleNameReference(new char[] { 'o' }, p);
			otherGetClass.selector = "getClass".toCharArray();
			MessageSend thisGetClass = new MessageSend();
			thisGetClass.receiver = new ThisReference(pS, pE);
			thisGetClass.selector = "getClass".toCharArray();
			EqualExpression classesNotEqual = new EqualExpression(otherGetClass, thisGetClass, OperatorIds.NOT_EQUAL);
			ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
			IfStatement ifClassesNotEqual = new IfStatement(classesNotEqual, returnFalse, pS, pE);
			statements.add(ifClassesNotEqual);
		}
		
		char[] otherN = "other".toCharArray();
		
		/* if ( !super.equals(o) ) return false; */
		if ( callSuper ) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.receiver = new SuperReference(pS, pE);
			callToSuper.selector = "equals".toCharArray();
			callToSuper.arguments = new Expression[] {new SingleNameReference(new char[] { 'o' }, p) };
			Expression superNotEqual = new UnaryExpression(callToSuper, OperatorIds.NOT);
			ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
			IfStatement ifSuperEquals = new IfStatement(superNotEqual, returnFalse, pS, pE);
			statements.add(ifSuperEquals);
		}
		
		TypeDeclaration typeDecl = (TypeDeclaration)type.get();
		/* MyType<?> other = (MyType<?>) o; */ {
			if ( !fields.isEmpty() ) {
				LocalDeclaration other = new LocalDeclaration(otherN, pS, pE);
				char[] typeName = typeDecl.name;
				Expression targetType;
				if ( typeDecl.typeParameters == null || typeDecl.typeParameters.length == 0 ) {
					targetType = new SingleNameReference(((TypeDeclaration)type.get()).name, p);
					other.type = new SingleTypeReference(typeName, p);
				} else {
					TypeReference[] typeArgs = new TypeReference[typeDecl.typeParameters.length];
					for ( int i = 0 ; i < typeArgs.length ; i++ ) typeArgs[i] = new Wildcard(Wildcard.UNBOUND);
					targetType = new ParameterizedSingleTypeReference(typeName, typeArgs, 0, p);
					other.type = new ParameterizedSingleTypeReference(typeName, copyTypes(typeArgs), 0, p);
				}
				other.initialization = new CastExpression(
						new SingleNameReference(new char[] { 'o' }, p),
						targetType);
				statements.add(other);
			}
		}
		
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if ( f.type.dimensions() == 0 && token != null ) {
				if ( Arrays.equals(TypeConstants.FLOAT, token) ) {
					statements.add(generateCompareFloatOrDouble(otherN, "Float".toCharArray(), f.name, p));
				} else if ( Arrays.equals(TypeConstants.DOUBLE, token) ) {
					statements.add(generateCompareFloatOrDouble(otherN, "Double".toCharArray(), f.name, p));
				} else if ( BUILT_IN_TYPES.contains(new String(token)) ) {
					EqualExpression fieldsNotEqual = new EqualExpression(
							new SingleNameReference(f.name, p),
							generateQualifiedNameRef(p, otherN, f.name),
							OperatorIds.NOT_EQUAL);
					ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
					statements.add(new IfStatement(fieldsNotEqual, returnStatement, pS, pE));
				} else /* objects */ {
					EqualExpression fieldIsNull = new EqualExpression(
							new SingleNameReference(f.name, p),
							new NullLiteral(pS, pE), OperatorIds.EQUAL_EQUAL);
					EqualExpression otherFieldIsntNull = new EqualExpression(
							generateQualifiedNameRef(p, otherN, f.name),
							new NullLiteral(pS, pE), OperatorIds.NOT_EQUAL);
					MessageSend equalsCall = new MessageSend();
					equalsCall.receiver = new SingleNameReference(f.name, p);
					equalsCall.selector = "equals".toCharArray();
					equalsCall.arguments = new Expression[] { generateQualifiedNameRef(p, otherN, f.name) };
					UnaryExpression fieldsNotEqual = new UnaryExpression(equalsCall, OperatorIds.NOT);
					ConditionalExpression fullEquals = new ConditionalExpression(fieldIsNull, otherFieldIsntNull, fieldsNotEqual);
					ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
					statements.add(new IfStatement(fullEquals, returnStatement, pS, pE));
				}
			} else if ( f.type.dimensions() > 0 && token != null ) {
				MessageSend arraysEqualCall = new MessageSend();
				arraysEqualCall.receiver = generateQualifiedNameRef(p, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token)) ) {
					arraysEqualCall.selector = "deepEquals".toCharArray();
				} else {
					arraysEqualCall.selector = "equals".toCharArray();
				}
				arraysEqualCall.arguments = new Expression[] {
						new SingleNameReference(f.name, p),
						generateQualifiedNameRef(p, otherN, f.name) };
				UnaryExpression arraysNotEqual = new UnaryExpression(arraysEqualCall, OperatorIds.NOT);
				ReturnStatement returnStatement = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
				statements.add(new IfStatement(arraysNotEqual, returnStatement, pS, pE));
			}
		}
		
		/* return true; */ {
			statements.add(new ReturnStatement(new TrueLiteral(pS, pE), pS, pE));
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private IfStatement generateCompareFloatOrDouble(char[] otherN, char[] floatOrDouble, char[] fieldName, long p) {
		int pS = (int)(p >> 32), pE = (int)p;
		/* if ( Float.compare(fieldName, other.fieldName) != 0 ) return false */
		MessageSend floatCompare = new MessageSend();
		floatCompare.receiver = generateQualifiedNameRef(p, TypeConstants.JAVA, TypeConstants.LANG, floatOrDouble);
		floatCompare.selector = "compare".toCharArray();
		floatCompare.arguments = new Expression[] {
				new SingleNameReference(fieldName, p),
				generateQualifiedNameRef(p, otherN, fieldName)
		};
		EqualExpression ifFloatCompareIsNot0 = new EqualExpression(floatCompare, new IntLiteral(new char[] {'0'}, pS, pE), OperatorIds.NOT_EQUAL);
		ReturnStatement returnFalse = new ReturnStatement(new FalseLiteral(pS, pE), pS, pE);
		return new IfStatement(ifFloatCompareIsNot0, returnFalse, pS, pE);
	}
	
	/** Give 2 clones! */
	private Expression longToIntForHashCode(Reference ref1, Reference ref2, long p) {
		int pS = (int)(p >> 32), pE = (int)p;
		/* (int)(ref >>> 32 ^ ref) */
		BinaryExpression higherBits = new BinaryExpression(
				ref1, new IntLiteral("32".toCharArray(), pS, pE),
				OperatorIds.UNSIGNED_RIGHT_SHIFT);
		BinaryExpression xorParts = new BinaryExpression(ref2, higherBits, OperatorIds.XOR);
		return new CastExpression(xorParts, TypeReference.baseTypeReference(TypeIds.T_int, 0));
	}
	
	private Reference generateFieldReference(char[] fieldName, long p) {
		FieldReference thisX = new FieldReference(("this." + new String(fieldName)).toCharArray(), p);
		thisX.receiver = new ThisReference((int)(p >> 32), (int)p);
		thisX.token = fieldName;
		return thisX;
	}
	
	private NameReference generateQualifiedNameRef(long p, char[]... varNames) {
		if ( varNames.length > 1 )
			return new QualifiedNameReference(varNames, new long[varNames.length], (int)(p >> 32), (int)p);
		else return new SingleNameReference(varNames[0], p);
	}
}
