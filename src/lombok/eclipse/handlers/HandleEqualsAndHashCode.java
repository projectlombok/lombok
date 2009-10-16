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
import lombok.eclipse.EclipseNode;

/**
 * Handles the <code>EqualsAndHashCode</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleEqualsAndHashCode implements EclipseAnnotationHandler<EqualsAndHashCode> {
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	private void checkForBogusFieldNames(EclipseNode type, AnnotationValues<EqualsAndHashCode> annotation) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(Arrays.asList(annotation.getInstance().exclude()), type, true, true)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(Arrays.asList(annotation.getInstance().of()), type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	public void generateEqualsAndHashCodeForType(EclipseNode typeNode, EclipseNode errorNode) {
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (Eclipse.annotationTypeMatches(EqualsAndHashCode.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		generateMethods(typeNode, errorNode, null, null, null, false);
	}
	
	@Override public boolean handle(AnnotationValues<EqualsAndHashCode> annotation,
			Annotation ast, EclipseNode annotationNode) {
		EqualsAndHashCode ann = annotation.getInstance();
		List<String> excludes = Arrays.asList(ann.exclude());
		List<String> includes = Arrays.asList(ann.of());
		EclipseNode typeNode = annotationNode.up();
		
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		return generateMethods(typeNode, annotationNode, excludes, includes, callSuper, true);
	}
	
	public boolean generateMethods(EclipseNode typeNode, EclipseNode errorNode, List<String> excludes, List<String> includes,
			Boolean callSuper, boolean whineIfExists) {
		assert excludes == null || includes == null;
		
		TypeDeclaration typeDecl = null;
		
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@EqualsAndHashCode is only supported on a class.");
			return false;
		}
		
		boolean implicitCallSuper = callSuper == null;
		
		if (callSuper == null) {
			try {
				callSuper = ((Boolean)EqualsAndHashCode.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}
		
		boolean isDirectDescendantOfObject = true;
		
		if (typeDecl.superclass != null) {
			String p = typeDecl.superclass.toString();
			isDirectDescendantOfObject = p.equals("Object") || p.equals("java.lang.Object");
		}
		
		if (isDirectDescendantOfObject && callSuper) {
			errorNode.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return true;
		}
		
		if (!isDirectDescendantOfObject && !callSuper && implicitCallSuper) {
			errorNode.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
		}
		
		List<EclipseNode> nodesForEquality = new ArrayList<EclipseNode>();
		if (includes != null) {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				if (includes.contains(new String(fieldDecl.name))) nodesForEquality.add(child);
			}
		} else {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				//Skip static fields.
				if ((fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0) continue;
				//Skip transient fields.
				if ((fieldDecl.modifiers & ClassFileConstants.AccTransient) != 0) continue;
				//Skip excluded fields.
				if (excludes != null && excludes.contains(new String(fieldDecl.name))) continue;
				//Skip fields that start with $.
				if (fieldDecl.name.length > 0 && fieldDecl.name[0] == '$') continue;
				nodesForEquality.add(child);
			}
		}
		
		switch (methodExists("hashCode", typeNode)) {
		case NOT_EXISTS:
			MethodDeclaration hashCode = createHashCode(typeNode, nodesForEquality, callSuper, errorNode.get());
			injectMethod(typeNode, hashCode);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating hashCode(): A method with that name already exists");
			}
			break;
		}
		
		switch (methodExists("equals", typeNode)) {
		case NOT_EXISTS:
			MethodDeclaration equals = createEquals(typeNode, nodesForEquality, callSuper, errorNode.get());
			injectMethod(typeNode, equals);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating equals(Object other): A method with that name already exists");
			}
			break;
		}
		
		return true;
	}
	
	private MethodDeclaration createHashCode(EclipseNode type, Collection<EclipseNode> fields, boolean callSuper, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		Eclipse.setGeneratedBy(method, source);
		
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		Eclipse.setGeneratedBy(method.returnType, source);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		method.selector = "hashCode".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.arguments = null;
		
		List<Statement> statements = new ArrayList<Statement>();
		List<Expression> intoResult = new ArrayList<Expression>();
		
		final char[] PRIME = "PRIME".toCharArray();
		final char[] RESULT = "result".toCharArray();
		final boolean isEmpty = fields.isEmpty();
		
		/* final int PRIME = 31; */ {
			/* Without fields, PRIME isn't used, and that would trigger a 'local variable not used' warning. */
			if (!isEmpty || callSuper) {
				LocalDeclaration primeDecl = new LocalDeclaration(PRIME, pS, pE);
				Eclipse.setGeneratedBy(primeDecl, source);
				primeDecl.modifiers |= Modifier.FINAL;
				primeDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
				primeDecl.type.sourceStart = pS; primeDecl.type.sourceEnd = pE;
				Eclipse.setGeneratedBy(primeDecl.type, source);
				primeDecl.initialization = new IntLiteral("31".toCharArray(), pS, pE);
				Eclipse.setGeneratedBy(primeDecl.initialization, source);
				statements.add(primeDecl);
			}
		}
		
		/* int result = 1; */ {
			LocalDeclaration resultDecl = new LocalDeclaration(RESULT, pS, pE);
			Eclipse.setGeneratedBy(resultDecl, source);
			resultDecl.initialization = new IntLiteral("1".toCharArray(), pS, pE);
			Eclipse.setGeneratedBy(resultDecl.initialization, source);
			resultDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
			resultDecl.type.sourceStart = pS; resultDecl.type.sourceEnd = pE;
			Eclipse.setGeneratedBy(resultDecl.type, source);
			statements.add(resultDecl);
		}
		
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			Eclipse.setGeneratedBy(callToSuper, source);
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			callToSuper.receiver = new SuperReference(pS, pE);
			Eclipse.setGeneratedBy(callToSuper.receiver, source);
			callToSuper.selector = "hashCode".toCharArray();
			intoResult.add(callToSuper);
		}
		
		int tempCounter = 0;
		for (EclipseNode field : fields) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if (f.type.dimensions() == 0 && token != null) {
				if (Arrays.equals(TypeConstants.FLOAT, token)) {
					/* Float.floatToIntBits(fieldName) */
					MessageSend floatToIntBits = new MessageSend();
					floatToIntBits.sourceStart = pS; floatToIntBits.sourceEnd = pE;
					Eclipse.setGeneratedBy(floatToIntBits, source);
					floatToIntBits.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA_LANG_FLOAT);
					floatToIntBits.selector = "floatToIntBits".toCharArray();
					floatToIntBits.arguments = new Expression[] { generateFieldReference(f.name, source) };
					intoResult.add(floatToIntBits);
				} else if (Arrays.equals(TypeConstants.DOUBLE, token)) {
					/* longToIntForHashCode(Double.doubleToLongBits(fieldName)) */
					MessageSend doubleToLongBits = new MessageSend();
					doubleToLongBits.sourceStart = pS; doubleToLongBits.sourceEnd = pE;
					Eclipse.setGeneratedBy(doubleToLongBits, source);
					doubleToLongBits.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA_LANG_DOUBLE);
					doubleToLongBits.selector = "doubleToLongBits".toCharArray();
					doubleToLongBits.arguments = new Expression[] { generateFieldReference(f.name, source) };
					final char[] tempName = ("temp" + ++tempCounter).toCharArray();
					LocalDeclaration tempVar = new LocalDeclaration(tempName, pS, pE);
					Eclipse.setGeneratedBy(tempVar, source);
					tempVar.initialization = doubleToLongBits;
					tempVar.type = TypeReference.baseTypeReference(TypeIds.T_long, 0);
					tempVar.type.sourceStart = pS; tempVar.type.sourceEnd = pE;
					Eclipse.setGeneratedBy(tempVar.type, source);
					tempVar.modifiers = Modifier.FINAL;
					statements.add(tempVar);
					SingleNameReference copy1 = new SingleNameReference(tempName, p);
					Eclipse.setGeneratedBy(copy1, source);
					SingleNameReference copy2 = new SingleNameReference(tempName, p);
					Eclipse.setGeneratedBy(copy2, source);
					intoResult.add(longToIntForHashCode(copy1, copy2, source));
				} else if (Arrays.equals(TypeConstants.BOOLEAN, token)) {
					/* booleanField ? 1231 : 1237 */
					IntLiteral int1231 = new IntLiteral("1231".toCharArray(), pS, pE);
					Eclipse.setGeneratedBy(int1231, source);
					IntLiteral int1237 = new IntLiteral("1237".toCharArray(), pS, pE);
					Eclipse.setGeneratedBy(int1237, source);
					ConditionalExpression int1231or1237 = new ConditionalExpression(
							generateFieldReference(f.name, source), int1231, int1237);
					Eclipse.setGeneratedBy(int1231or1237, source);
					intoResult.add(int1231or1237);
				} else if (Arrays.equals(TypeConstants.LONG, token)) {
					intoResult.add(longToIntForHashCode(generateFieldReference(f.name, source), generateFieldReference(f.name, source), source));
				} else if (BUILT_IN_TYPES.contains(new String(token))) {
					intoResult.add(generateFieldReference(f.name, source));
				} else /* objects */ {
					/* this.fieldName == null ? 0 : this.fieldName.hashCode() */
					MessageSend hashCodeCall = new MessageSend();
					hashCodeCall.sourceStart = pS; hashCodeCall.sourceEnd = pE;
					Eclipse.setGeneratedBy(hashCodeCall, source);
					hashCodeCall.receiver = generateFieldReference(f.name, source);
					hashCodeCall.selector = "hashCode".toCharArray();
					NullLiteral nullLiteral = new NullLiteral(pS, pE);
					Eclipse.setGeneratedBy(nullLiteral, source);
					EqualExpression objIsNull = new EqualExpression(
							generateFieldReference(f.name, source), nullLiteral, OperatorIds.EQUAL_EQUAL);
					Eclipse.setGeneratedBy(objIsNull, source);
					IntLiteral int0 = new IntLiteral("0".toCharArray(), pS, pE);
					Eclipse.setGeneratedBy(int0, source);
					ConditionalExpression nullOrHashCode = new ConditionalExpression(objIsNull, int0, hashCodeCall);
					nullOrHashCode.sourceStart = pS; nullOrHashCode.sourceEnd = pE;
					Eclipse.setGeneratedBy(nullOrHashCode, source);
					intoResult.add(nullOrHashCode);
				}
			} else if (f.type.dimensions() > 0 && token != null) {
				/* Arrays.deepHashCode(array)  //just hashCode for simple arrays */
				MessageSend arraysHashCodeCall = new MessageSend();
				arraysHashCodeCall.sourceStart = pS; arraysHashCodeCall.sourceEnd = pE;
				Eclipse.setGeneratedBy(arraysHashCodeCall, source);
				arraysHashCodeCall.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if (f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token))) {
					arraysHashCodeCall.selector = "deepHashCode".toCharArray();
				} else {
					arraysHashCodeCall.selector = "hashCode".toCharArray();
				}
				arraysHashCodeCall.arguments = new Expression[] { generateFieldReference(f.name, source) };
				intoResult.add(arraysHashCodeCall);
			}
		}
		
		/* fold each intoResult entry into:
		   result = result * PRIME + (item); */ {
			for (Expression ex : intoResult) {
				SingleNameReference resultRef = new SingleNameReference(RESULT, p);
				Eclipse.setGeneratedBy(resultRef, source);
				SingleNameReference primeRef = new SingleNameReference(PRIME, p);
				Eclipse.setGeneratedBy(primeRef, source);
				BinaryExpression multiplyByPrime = new BinaryExpression(resultRef, primeRef, OperatorIds.MULTIPLY);
				multiplyByPrime.sourceStart = pS; multiplyByPrime.sourceEnd = pE;
				Eclipse.setGeneratedBy(multiplyByPrime, source);
				BinaryExpression addItem = new BinaryExpression(multiplyByPrime, ex, OperatorIds.PLUS);
				addItem.sourceStart = pS; addItem.sourceEnd = pE;
				Eclipse.setGeneratedBy(addItem, source);
				resultRef = new SingleNameReference(RESULT, p);
				Eclipse.setGeneratedBy(resultRef, source);
				Assignment assignment = new Assignment(resultRef, addItem, pE);
				assignment.sourceStart = pS; assignment.sourceEnd = pE;
				Eclipse.setGeneratedBy(assignment, source);
				statements.add(assignment);
			}
		}
		
		/* return result; */ {
			SingleNameReference resultRef = new SingleNameReference(RESULT, p);
			Eclipse.setGeneratedBy(resultRef, source);
			ReturnStatement returnStatement = new ReturnStatement(resultRef, pS, pE);
			Eclipse.setGeneratedBy(returnStatement, source);
			statements.add(returnStatement);
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private MethodDeclaration createEquals(EclipseNode type, Collection<EclipseNode> fields, boolean callSuper, ASTNode source) {
		int pS = source.sourceStart; int pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		Eclipse.setGeneratedBy(method.returnType, source);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		method.selector = "equals".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		TypeReference objectRef = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { p, p, p });
		Eclipse.setGeneratedBy(objectRef, source);
		method.arguments = new Argument[] {new Argument(new char[] { 'o' }, 0, objectRef, Modifier.FINAL)};
		method.arguments[0].sourceStart = pS; method.arguments[0].sourceEnd = pE;
		Eclipse.setGeneratedBy(method.arguments[0], source);
		
		List<Statement> statements = new ArrayList<Statement>();
		
		/* if (o == this) return true; */ {
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			Eclipse.setGeneratedBy(oRef, source);
			ThisReference thisRef = new ThisReference(pS, pE);
			Eclipse.setGeneratedBy(thisRef, source);
			EqualExpression otherEqualsThis = new EqualExpression(oRef, thisRef, OperatorIds.EQUAL_EQUAL);
			Eclipse.setGeneratedBy(otherEqualsThis, source);
			
			TrueLiteral trueLiteral = new TrueLiteral(pS, pE);
			Eclipse.setGeneratedBy(trueLiteral, source);
			ReturnStatement returnTrue = new ReturnStatement(trueLiteral, pS, pE);
			Eclipse.setGeneratedBy(returnTrue, source);
			IfStatement ifOtherEqualsThis = new IfStatement(otherEqualsThis, returnTrue, pS, pE);
			Eclipse.setGeneratedBy(ifOtherEqualsThis, source);
			statements.add(ifOtherEqualsThis);
		}
		
		/* if (o == null) return false; */ {
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			Eclipse.setGeneratedBy(oRef, source);
			NullLiteral nullLiteral = new NullLiteral(pS, pE);
			Eclipse.setGeneratedBy(nullLiteral, source);
			EqualExpression otherEqualsNull = new EqualExpression(oRef, nullLiteral, OperatorIds.EQUAL_EQUAL);
			Eclipse.setGeneratedBy(otherEqualsNull, source);
			
			FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
			Eclipse.setGeneratedBy(falseLiteral, source);
			ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
			Eclipse.setGeneratedBy(returnFalse, source);
			IfStatement ifOtherEqualsNull = new IfStatement(otherEqualsNull, returnFalse, pS, pE);
			Eclipse.setGeneratedBy(ifOtherEqualsNull, source);
			statements.add(ifOtherEqualsNull);
		}
		
		/* if (o.getClass() != getClass()) return false; */ {
			MessageSend otherGetClass = new MessageSend();
			otherGetClass.sourceStart = pS; otherGetClass.sourceEnd = pE;
			Eclipse.setGeneratedBy(otherGetClass, source);
			otherGetClass.receiver = new SingleNameReference(new char[] { 'o' }, p);
			Eclipse.setGeneratedBy(otherGetClass.receiver, source);
			otherGetClass.selector = "getClass".toCharArray();
			MessageSend thisGetClass = new MessageSend();
			thisGetClass.sourceStart = pS; thisGetClass.sourceEnd = pE;
			Eclipse.setGeneratedBy(thisGetClass, source);
			thisGetClass.receiver = new ThisReference(pS, pE);
			Eclipse.setGeneratedBy(thisGetClass.receiver, source);
			thisGetClass.selector = "getClass".toCharArray();
			EqualExpression classesNotEqual = new EqualExpression(otherGetClass, thisGetClass, OperatorIds.NOT_EQUAL);
			Eclipse.setGeneratedBy(classesNotEqual, source);
			FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
			Eclipse.setGeneratedBy(falseLiteral, source);
			ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
			Eclipse.setGeneratedBy(returnFalse, source);
			IfStatement ifClassesNotEqual = new IfStatement(classesNotEqual, returnFalse, pS, pE);
			Eclipse.setGeneratedBy(ifClassesNotEqual, source);
			statements.add(ifClassesNotEqual);
		}
		
		char[] otherN = "other".toCharArray();
		
		/* if (!super.equals(o)) return false; */
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			Eclipse.setGeneratedBy(callToSuper, source);
			callToSuper.receiver = new SuperReference(pS, pE);
			Eclipse.setGeneratedBy(callToSuper.receiver, source);
			callToSuper.selector = "equals".toCharArray();
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			Eclipse.setGeneratedBy(oRef, source);
			callToSuper.arguments = new Expression[] {oRef};
			Expression superNotEqual = new UnaryExpression(callToSuper, OperatorIds.NOT);
			Eclipse.setGeneratedBy(superNotEqual, source);
			FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
			Eclipse.setGeneratedBy(falseLiteral, source);
			ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
			Eclipse.setGeneratedBy(returnFalse, source);
			IfStatement ifSuperEquals = new IfStatement(superNotEqual, returnFalse, pS, pE);
			Eclipse.setGeneratedBy(ifSuperEquals, source);
			statements.add(ifSuperEquals);
		}
		
		TypeDeclaration typeDecl = (TypeDeclaration)type.get();
		/* MyType<?> other = (MyType<?>) o; */ {
			if (!fields.isEmpty()) {
				LocalDeclaration other = new LocalDeclaration(otherN, pS, pE);
				Eclipse.setGeneratedBy(other, source);
				char[] typeName = typeDecl.name;
				Expression targetType;
				if (typeDecl.typeParameters == null || typeDecl.typeParameters.length == 0) {
					targetType = new SingleNameReference(((TypeDeclaration)type.get()).name, p);
					Eclipse.setGeneratedBy(targetType, source);
					other.type = new SingleTypeReference(typeName, p);
					Eclipse.setGeneratedBy(other.type, source);
				} else {
					TypeReference[] typeArgs = new TypeReference[typeDecl.typeParameters.length];
					for (int i = 0; i < typeArgs.length; i++) {
						typeArgs[i] = new Wildcard(Wildcard.UNBOUND);
						typeArgs[i].sourceStart = pS; typeArgs[i].sourceEnd = pE;
						Eclipse.setGeneratedBy(typeArgs[i], source);
					}
					targetType = new ParameterizedSingleTypeReference(typeName, typeArgs, 0, p);
					Eclipse.setGeneratedBy(targetType, source);
					other.type = new ParameterizedSingleTypeReference(typeName, copyTypes(typeArgs, source), 0, p);
					Eclipse.setGeneratedBy(other.type, source);
				}
				NameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
				Eclipse.setGeneratedBy(oRef, source);
				other.initialization = new CastExpression(oRef, targetType);
				Eclipse.setGeneratedBy(other.initialization, source);
				statements.add(other);
			}
		}
		
		for (EclipseNode field : fields) {
			FieldDeclaration f = (FieldDeclaration) field.get();
			char[] token = f.type.getLastToken();
			if (f.type.dimensions() == 0 && token != null) {
				if (Arrays.equals(TypeConstants.FLOAT, token)) {
					statements.add(generateCompareFloatOrDouble(otherN, "Float".toCharArray(), f.name, source));
				} else if (Arrays.equals(TypeConstants.DOUBLE, token)) {
					statements.add(generateCompareFloatOrDouble(otherN, "Double".toCharArray(), f.name, source));
				} else if (BUILT_IN_TYPES.contains(new String(token))) {
					NameReference fieldRef = new SingleNameReference(f.name, p);
					Eclipse.setGeneratedBy(fieldRef, source);
					EqualExpression fieldsNotEqual = new EqualExpression(fieldRef,
							generateQualifiedNameRef(source, otherN, f.name), OperatorIds.NOT_EQUAL);
					Eclipse.setGeneratedBy(fieldsNotEqual, source);
					FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
					Eclipse.setGeneratedBy(falseLiteral, source);
					ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
					Eclipse.setGeneratedBy(returnStatement, source);
					IfStatement ifStatement = new IfStatement(fieldsNotEqual, returnStatement, pS, pE);
					Eclipse.setGeneratedBy(ifStatement, source);
					statements.add(ifStatement);
				} else /* objects */ {
					NameReference fieldNameRef = new SingleNameReference(f.name, p);
					Eclipse.setGeneratedBy(fieldNameRef, source);
					NullLiteral nullLiteral = new NullLiteral(pS, pE);
					Eclipse.setGeneratedBy(nullLiteral, source);
					EqualExpression fieldIsNull = new EqualExpression(fieldNameRef, nullLiteral, OperatorIds.EQUAL_EQUAL);
					nullLiteral = new NullLiteral(pS, pE);
					Eclipse.setGeneratedBy(nullLiteral, source);
					EqualExpression otherFieldIsntNull = new EqualExpression(
							generateQualifiedNameRef(source, otherN, f.name),
							nullLiteral, OperatorIds.NOT_EQUAL);
					MessageSend equalsCall = new MessageSend();
					equalsCall.sourceStart = pS; equalsCall.sourceEnd = pE;
					Eclipse.setGeneratedBy(equalsCall, source);
					equalsCall.receiver = new SingleNameReference(f.name, p);
					Eclipse.setGeneratedBy(equalsCall.receiver, source);
					equalsCall.selector = "equals".toCharArray();
					equalsCall.arguments = new Expression[] { generateQualifiedNameRef(source, otherN, f.name) };
					UnaryExpression fieldsNotEqual = new UnaryExpression(equalsCall, OperatorIds.NOT);
					fieldsNotEqual.sourceStart = pS; fieldsNotEqual.sourceEnd = pE;
					Eclipse.setGeneratedBy(fieldsNotEqual, source);
					ConditionalExpression fullEquals = new ConditionalExpression(fieldIsNull, otherFieldIsntNull, fieldsNotEqual);
					fullEquals.sourceStart = pS; fullEquals.sourceEnd = pE;
					Eclipse.setGeneratedBy(fullEquals, source);
					FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
					Eclipse.setGeneratedBy(falseLiteral, source);
					ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
					Eclipse.setGeneratedBy(returnStatement, source);
					IfStatement ifStatement = new IfStatement(fullEquals, returnStatement, pS, pE);
					Eclipse.setGeneratedBy(ifStatement, source);
					statements.add(ifStatement);
				}
			} else if (f.type.dimensions() > 0 && token != null) {
				MessageSend arraysEqualCall = new MessageSend();
				arraysEqualCall.sourceStart = pS; arraysEqualCall.sourceEnd = pE;
				Eclipse.setGeneratedBy(arraysEqualCall, source);
				arraysEqualCall.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if (f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token))) {
					arraysEqualCall.selector = "deepEquals".toCharArray();
				} else {
					arraysEqualCall.selector = "equals".toCharArray();
				}
				NameReference fieldNameRef = new SingleNameReference(f.name, p);
				Eclipse.setGeneratedBy(fieldNameRef, source);
				arraysEqualCall.arguments = new Expression[] { fieldNameRef, generateQualifiedNameRef(source, otherN, f.name) };
				UnaryExpression arraysNotEqual = new UnaryExpression(arraysEqualCall, OperatorIds.NOT);
				arraysNotEqual.sourceStart = pS; arraysNotEqual.sourceEnd = pE;
				Eclipse.setGeneratedBy(arraysNotEqual, source);
				FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
				Eclipse.setGeneratedBy(falseLiteral, source);
				ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
				Eclipse.setGeneratedBy(returnStatement, source);
				IfStatement ifStatement = new IfStatement(arraysNotEqual, returnStatement, pS, pE);
				Eclipse.setGeneratedBy(ifStatement, source);
				statements.add(ifStatement);
			}
		}
		
		/* return true; */ {
			TrueLiteral trueLiteral = new TrueLiteral(pS, pE);
			Eclipse.setGeneratedBy(trueLiteral, source);
			ReturnStatement returnStatement = new ReturnStatement(trueLiteral, pS, pE);
			Eclipse.setGeneratedBy(returnStatement, source);
			statements.add(returnStatement);
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	private IfStatement generateCompareFloatOrDouble(char[] otherN, char[] floatOrDouble, char[] fieldName, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		/* if (Float.compare(fieldName, other.fieldName) != 0) return false */
		MessageSend floatCompare = new MessageSend();
		floatCompare.sourceStart = pS; floatCompare.sourceEnd = pE;
		Eclipse.setGeneratedBy(floatCompare, source);
		floatCompare.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.LANG, floatOrDouble);
		floatCompare.selector = "compare".toCharArray();
		NameReference fieldNameRef = new SingleNameReference(fieldName, p);
		Eclipse.setGeneratedBy(fieldNameRef, source);
		floatCompare.arguments = new Expression[] {fieldNameRef, generateQualifiedNameRef(source, otherN, fieldName)};
		IntLiteral int0 = new IntLiteral(new char[] {'0'}, pS, pE);
		Eclipse.setGeneratedBy(int0, source);
		EqualExpression ifFloatCompareIsNot0 = new EqualExpression(floatCompare, int0, OperatorIds.NOT_EQUAL);
		ifFloatCompareIsNot0.sourceStart = pS; ifFloatCompareIsNot0.sourceEnd = pE;
		Eclipse.setGeneratedBy(ifFloatCompareIsNot0, source);
		FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
		Eclipse.setGeneratedBy(falseLiteral, source);
		ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
		Eclipse.setGeneratedBy(returnFalse, source);
		IfStatement ifStatement = new IfStatement(ifFloatCompareIsNot0, returnFalse, pS, pE);
		Eclipse.setGeneratedBy(ifStatement, source);
		return ifStatement;
	}
	
	/** Give 2 clones! */
	private Expression longToIntForHashCode(Reference ref1, Reference ref2, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		/* (int)(ref >>> 32 ^ ref) */
		IntLiteral int32 = new IntLiteral("32".toCharArray(), pS, pE);
		Eclipse.setGeneratedBy(int32, source);
		BinaryExpression higherBits = new BinaryExpression(ref1, int32, OperatorIds.UNSIGNED_RIGHT_SHIFT);
		Eclipse.setGeneratedBy(higherBits, source);
		BinaryExpression xorParts = new BinaryExpression(ref2, higherBits, OperatorIds.XOR);
		Eclipse.setGeneratedBy(xorParts, source);
		TypeReference intRef = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		intRef.sourceStart = pS; intRef.sourceEnd = pE;
		Eclipse.setGeneratedBy(intRef, source);
		CastExpression expr = new CastExpression(xorParts, intRef);
		expr.sourceStart = pS; expr.sourceEnd = pE;
		Eclipse.setGeneratedBy(expr, source);
		return expr;
	}
	
	private Reference generateFieldReference(char[] fieldName, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		FieldReference thisX = new FieldReference(("this." + new String(fieldName)).toCharArray(), p);
		Eclipse.setGeneratedBy(thisX, source);
		thisX.receiver = new ThisReference(pS, pE);
		Eclipse.setGeneratedBy(thisX.receiver, source);
		thisX.token = fieldName;
		return thisX;
	}
	
	private NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		NameReference ref;
		
		if (varNames.length > 1) ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
		else ref = new SingleNameReference(varNames[0], p);
		Eclipse.setGeneratedBy(ref, source);
		return ref;
	}
}
