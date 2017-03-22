/*
 * Copyright (C) 2009-2015 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.EqualsAndHashCode;
import lombok.core.AST.Kind;
import lombok.core.handlers.HandlerUtil;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CallSuperType;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.FieldAccess;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;

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
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
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

/**
 * Handles the {@code EqualsAndHashCode} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleEqualsAndHashCode extends EclipseAnnotationHandler<EqualsAndHashCode> {
	
	private final char[] PRIME = "PRIME".toCharArray();
	private final char[] RESULT = "result".toCharArray();
	
	public static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	public void checkForBogusFieldNames(EclipseNode type, AnnotationValues<EqualsAndHashCode> annotation) {
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
		if (hasAnnotation(EqualsAndHashCode.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;
		
		generateMethods(typeNode, errorNode, null, null, null, false, access, new ArrayList<Annotation>());
	}
	
	@Override public void handle(AnnotationValues<EqualsAndHashCode> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.EQUALS_AND_HASH_CODE_FLAG_USAGE, "@EqualsAndHashCode");
		
		EqualsAndHashCode ann = annotation.getInstance();
		List<String> excludes = Arrays.asList(ann.exclude());
		List<String> includes = Arrays.asList(ann.of());
		EclipseNode typeNode = annotationNode.up();
		
		List<Annotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@EqualsAndHashCode(onParam", annotationNode);
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		generateMethods(typeNode, annotationNode, excludes, includes, callSuper, true, fieldAccess, onParam);
	}
	
	public void generateMethods(EclipseNode typeNode, EclipseNode errorNode, List<String> excludes, List<String> includes,
			Boolean callSuper, boolean whineIfExists, FieldAccess fieldAccess, List<Annotation> onParam) {
		assert excludes == null || includes == null;
		
		TypeDeclaration typeDecl = null;
		
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@EqualsAndHashCode is only supported on a class.");
			return;
		}
		
		boolean implicitCallSuper = callSuper == null;
		
		if (callSuper == null) {
			try {
				callSuper = ((Boolean)EqualsAndHashCode.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {
				throw new InternalError("Lombok bug - this cannot happen - can't find callSuper field in EqualsAndHashCode annotation.");
			}
		}
		
		boolean isDirectDescendantOfObject = true;
		
		if (typeDecl.superclass != null) {
			String p = typeDecl.superclass.toString();
			isDirectDescendantOfObject = p.equals("Object") || p.equals("java.lang.Object");
		}
		
		if (isDirectDescendantOfObject && callSuper) {
			errorNode.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return;
		}
		
		if (implicitCallSuper && !isDirectDescendantOfObject) {
			CallSuperType cst = typeNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_CALL_SUPER);
			if (cst == null) cst = CallSuperType.WARN;
			
			switch (cst) {
			default:
			case WARN:
				errorNode.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
				callSuper = false;
				break;
			case SKIP:
				callSuper = false;
				break;
			case CALL:
				callSuper = true;
				break;
			}
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
				if (!filterField(fieldDecl)) continue;
				
				//Skip transient fields.
				if ((fieldDecl.modifiers & ClassFileConstants.AccTransient) != 0) continue;
				//Skip excluded fields.
				if (excludes != null && excludes.contains(new String(fieldDecl.name))) continue;
				nodesForEquality.add(child);
			}
		}
		
		boolean isFinal = (typeDecl.modifiers & ClassFileConstants.AccFinal) != 0;
		boolean needsCanEqual = !isFinal || !isDirectDescendantOfObject;
		MemberExistsResult equalsExists = methodExists("equals", typeNode, 1);
		MemberExistsResult hashCodeExists = methodExists("hashCode", typeNode, 0);
		MemberExistsResult canEqualExists = methodExists("canEqual", typeNode, 1);
		switch (Collections.max(Arrays.asList(equalsExists, hashCodeExists))) {
		case EXISTS_BY_LOMBOK:
			return;
		case EXISTS_BY_USER:
			if (whineIfExists) {
				String msg = "Not generating equals and hashCode: A method with one of those names already exists. (Either both or none of these methods will be generated).";
				errorNode.addWarning(msg);
			} else if (equalsExists == MemberExistsResult.NOT_EXISTS || hashCodeExists == MemberExistsResult.NOT_EXISTS) {
				// This means equals OR hashCode exists and not both.
				// Even though we should suppress the message about not generating these, this is such a weird and surprising situation we should ALWAYS generate a warning.
				// The user code couldn't possibly (barring really weird subclassing shenanigans) be in a shippable state anyway; the implementations of these 2 methods are
				// all inter-related and should be written by the same entity.
				String msg = String.format("Not generating %s: One of equals or hashCode exists. " +
						"You should either write both of these or none of these (in the latter case, lombok generates them).",
						equalsExists == MemberExistsResult.NOT_EXISTS ? "equals" : "hashCode");
				errorNode.addWarning(msg);
			}
			return;
		case NOT_EXISTS:
		default:
			//fallthrough
		}
		
		MethodDeclaration equalsMethod = createEquals(typeNode, nodesForEquality, callSuper, errorNode.get(), fieldAccess, needsCanEqual, onParam);
		equalsMethod.traverse(new SetGeneratedByVisitor(errorNode.get()), ((TypeDeclaration)typeNode.get()).scope);
		injectMethod(typeNode, equalsMethod);
		
		if (needsCanEqual && canEqualExists == MemberExistsResult.NOT_EXISTS) {
			MethodDeclaration canEqualMethod = createCanEqual(typeNode, errorNode.get(), onParam);
			canEqualMethod.traverse(new SetGeneratedByVisitor(errorNode.get()), ((TypeDeclaration)typeNode.get()).scope);
			injectMethod(typeNode, canEqualMethod);
		}
		
		MethodDeclaration hashCodeMethod = createHashCode(typeNode, nodesForEquality, callSuper, errorNode.get(), fieldAccess);
		hashCodeMethod.traverse(new SetGeneratedByVisitor(errorNode.get()), ((TypeDeclaration)typeNode.get()).scope);
		injectMethod(typeNode, hashCodeMethod);
	}
	
	public MethodDeclaration createHashCode(EclipseNode type, Collection<EclipseNode> fields, boolean callSuper, ASTNode source, FieldAccess fieldAccess) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(method, source);
		
		method.modifiers = toEclipseModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		setGeneratedBy(method.returnType, source);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		method.selector = "hashCode".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.arguments = null;
		
		List<Statement> statements = new ArrayList<Statement>();
		
		final boolean isEmpty = fields.isEmpty();
		
		/* final int PRIME = X; */ {
			/* Without fields, PRIME isn't used, and that would trigger a 'local variable not used' warning. */
			if (!isEmpty || callSuper) {
				LocalDeclaration primeDecl = new LocalDeclaration(PRIME, pS, pE);
				setGeneratedBy(primeDecl, source);
				primeDecl.modifiers |= Modifier.FINAL;
				primeDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
				primeDecl.type.sourceStart = pS; primeDecl.type.sourceEnd = pE;
				setGeneratedBy(primeDecl.type, source);
				primeDecl.initialization = makeIntLiteral(String.valueOf(HandlerUtil.primeForHashcode()).toCharArray(), source);
				statements.add(primeDecl);
			}
		}
		
		/* int result = 1; */ {
			LocalDeclaration resultDecl = new LocalDeclaration(RESULT, pS, pE);
			setGeneratedBy(resultDecl, source);
			resultDecl.initialization = makeIntLiteral("1".toCharArray(), source);
			resultDecl.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
			resultDecl.type.sourceStart = pS; resultDecl.type.sourceEnd = pE;
			setGeneratedBy(resultDecl.type, source);
			statements.add(resultDecl);
		}
		
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			setGeneratedBy(callToSuper, source);
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			callToSuper.receiver = new SuperReference(pS, pE);
			setGeneratedBy(callToSuper.receiver, source);
			callToSuper.selector = "hashCode".toCharArray();
			statements.add(createResultCalculation(source, callToSuper));
		}
		
		for (EclipseNode field : fields) {
			TypeReference fType = getFieldType(field, fieldAccess);
			char[] dollarFieldName = ("$" + field.getName()).toCharArray();
			char[] token = fType.getLastToken();
			Expression fieldAccessor = createFieldAccessor(field, fieldAccess, source);
			if (fType.dimensions() == 0 && token != null) {
				if (Arrays.equals(TypeConstants.BOOLEAN, token)) {
					/* booleanField ? X : Y */
					IntLiteral intTrue = makeIntLiteral(String.valueOf(HandlerUtil.primeForTrue()).toCharArray(), source);
					IntLiteral intFalse = makeIntLiteral(String.valueOf(HandlerUtil.primeForFalse()).toCharArray(), source);
					ConditionalExpression intForBool = new ConditionalExpression(fieldAccessor, intTrue, intFalse);
					setGeneratedBy(intForBool, source);
					statements.add(createResultCalculation(source, intForBool));
				} else if (Arrays.equals(TypeConstants.LONG, token)) {
					statements.add(createLocalDeclaration(source, dollarFieldName, TypeReference.baseTypeReference(TypeIds.T_long, 0), fieldAccessor));
					SingleNameReference copy1 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy1, source);
					SingleNameReference copy2 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy2, source);
					statements.add(createResultCalculation(source, longToIntForHashCode(copy1, copy2, source)));
				} else if (Arrays.equals(TypeConstants.FLOAT, token)) {
					/* Float.floatToIntBits(fieldName) */
					MessageSend floatToIntBits = new MessageSend();
					floatToIntBits.sourceStart = pS; floatToIntBits.sourceEnd = pE;
					setGeneratedBy(floatToIntBits, source);
					floatToIntBits.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA_LANG_FLOAT);
					floatToIntBits.selector = "floatToIntBits".toCharArray();
					floatToIntBits.arguments = new Expression[] { fieldAccessor };
					statements.add(createResultCalculation(source, floatToIntBits));
				} else if (Arrays.equals(TypeConstants.DOUBLE, token)) {
					/* longToIntForHashCode(Double.doubleToLongBits(fieldName)) */
					MessageSend doubleToLongBits = new MessageSend();
					doubleToLongBits.sourceStart = pS; doubleToLongBits.sourceEnd = pE;
					setGeneratedBy(doubleToLongBits, source);
					doubleToLongBits.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA_LANG_DOUBLE);
					doubleToLongBits.selector = "doubleToLongBits".toCharArray();
					doubleToLongBits.arguments = new Expression[] { fieldAccessor };
					statements.add(createLocalDeclaration(source, dollarFieldName, TypeReference.baseTypeReference(TypeIds.T_long, 0), doubleToLongBits));
					SingleNameReference copy1 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy1, source);
					SingleNameReference copy2 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy2, source);
					statements.add(createResultCalculation(source, longToIntForHashCode(copy1, copy2, source)));
				} else if (BUILT_IN_TYPES.contains(new String(token))) {
					statements.add(createResultCalculation(source, fieldAccessor));
				} else /* objects */ {
					/* final java.lang.Object $fieldName = this.fieldName; */
					/* $fieldName == null ? NULL_PRIME : $fieldName.hashCode() */
					statements.add(createLocalDeclaration(source, dollarFieldName, generateQualifiedTypeRef(source, TypeConstants.JAVA_LANG_OBJECT), fieldAccessor));
					
					SingleNameReference copy1 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy1, source);
					SingleNameReference copy2 = new SingleNameReference(dollarFieldName, p);
					setGeneratedBy(copy2, source);
					
					MessageSend hashCodeCall = new MessageSend();
					hashCodeCall.sourceStart = pS; hashCodeCall.sourceEnd = pE;
					setGeneratedBy(hashCodeCall, source);
					hashCodeCall.receiver = copy1;
					hashCodeCall.selector = "hashCode".toCharArray();
					NullLiteral nullLiteral = new NullLiteral(pS, pE);
					setGeneratedBy(nullLiteral, source);
					EqualExpression objIsNull = new EqualExpression(copy2, nullLiteral, OperatorIds.EQUAL_EQUAL);
					setGeneratedBy(objIsNull, source);
					IntLiteral intMagic = makeIntLiteral(String.valueOf(HandlerUtil.primeForNull()).toCharArray(), source);
					ConditionalExpression nullOrHashCode = new ConditionalExpression(objIsNull, intMagic, hashCodeCall);
					nullOrHashCode.sourceStart = pS; nullOrHashCode.sourceEnd = pE;
					setGeneratedBy(nullOrHashCode, source);
					statements.add(createResultCalculation(source, nullOrHashCode));
				}
			} else if (fType.dimensions() > 0 && token != null) {
				/* Arrays.deepHashCode(array)  //just hashCode for simple arrays */
				MessageSend arraysHashCodeCall = new MessageSend();
				arraysHashCodeCall.sourceStart = pS; arraysHashCodeCall.sourceEnd = pE;
				setGeneratedBy(arraysHashCodeCall, source);
				arraysHashCodeCall.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if (fType.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token))) {
					arraysHashCodeCall.selector = "deepHashCode".toCharArray();
				} else {
					arraysHashCodeCall.selector = "hashCode".toCharArray();
				}
				arraysHashCodeCall.arguments = new Expression[] { fieldAccessor };
				statements.add(createResultCalculation(source, arraysHashCodeCall));
			}
		}
		
		/* return result; */ {
			SingleNameReference resultRef = new SingleNameReference(RESULT, p);
			setGeneratedBy(resultRef, source);
			ReturnStatement returnStatement = new ReturnStatement(resultRef, pS, pE);
			setGeneratedBy(returnStatement, source);
			statements.add(returnStatement);
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}

	public LocalDeclaration createLocalDeclaration(ASTNode source, char[] dollarFieldName, TypeReference type, Expression initializer) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		LocalDeclaration tempVar = new LocalDeclaration(dollarFieldName, pS, pE);
		setGeneratedBy(tempVar, source);
		tempVar.initialization = initializer;
		tempVar.type = type;
		tempVar.type.sourceStart = pS; tempVar.type.sourceEnd = pE;
		setGeneratedBy(tempVar.type, source);
		tempVar.modifiers = Modifier.FINAL;
		return tempVar;
	}

	public Expression createResultCalculation(ASTNode source, Expression ex) {
		/* result = result * PRIME + (ex); */
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		SingleNameReference resultRef = new SingleNameReference(RESULT, p);
		setGeneratedBy(resultRef, source);
		SingleNameReference primeRef = new SingleNameReference(PRIME, p);
		setGeneratedBy(primeRef, source);
		BinaryExpression multiplyByPrime = new BinaryExpression(resultRef, primeRef, OperatorIds.MULTIPLY);
		multiplyByPrime.sourceStart = pS; multiplyByPrime.sourceEnd = pE;
		setGeneratedBy(multiplyByPrime, source);
		BinaryExpression addItem = new BinaryExpression(multiplyByPrime, ex, OperatorIds.PLUS);
		addItem.sourceStart = pS; addItem.sourceEnd = pE;
		setGeneratedBy(addItem, source);
		resultRef = new SingleNameReference(RESULT, p);
		setGeneratedBy(resultRef, source);
		Assignment assignment = new Assignment(resultRef, addItem, pE);
		assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
		setGeneratedBy(assignment, source);
		return assignment;
	}
	
	/**
	 * @param type Type to 'copy' into a typeref
	 * @param p position
	 * @param addWildcards If false, all generics are cut off. If true, replaces all genericparams with a ?.
	 * @return
	 */
	public TypeReference createTypeReference(EclipseNode type, long p, ASTNode source, boolean addWildcards) {
		int pS = source.sourceStart; int pE = source.sourceEnd;
		List<String> list = new ArrayList<String>();
		list.add(type.getName());
		EclipseNode tNode = type.up();
		while (tNode != null && tNode.getKind() == Kind.TYPE) {
			list.add(tNode.getName());
			tNode = tNode.up();
		}
		Collections.reverse(list);
		
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		int typeParamCount = typeDecl.typeParameters == null ? 0 : typeDecl.typeParameters.length;
		if (typeParamCount == 0) addWildcards = false;
		TypeReference[] typeArgs = null;
		if (addWildcards) {
			typeArgs = new TypeReference[typeParamCount];
			for (int i = 0; i < typeParamCount; i++) {
				typeArgs[i] = new Wildcard(Wildcard.UNBOUND);
				typeArgs[i].sourceStart = pS; typeArgs[i].sourceEnd = pE;
				setGeneratedBy(typeArgs[i], source);
			}
		}
		
		if (list.size() == 1) {
			if (addWildcards) {
				return new ParameterizedSingleTypeReference(list.get(0).toCharArray(), typeArgs, 0, p);
			} else {
				return new SingleTypeReference(list.get(0).toCharArray(), p);
			}
		}
		long[] ps = new long[list.size()];
		char[][] tokens = new char[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			ps[i] = p;
			tokens[i] = list.get(i).toCharArray();
		}
		if (addWildcards) {
			TypeReference[][] typeArgs2 = new TypeReference[tokens.length][];
			typeArgs2[typeArgs2.length - 1] = typeArgs;
			return new ParameterizedQualifiedTypeReference(tokens, typeArgs2, 0, ps);
		} else {
			return new QualifiedTypeReference(tokens, ps);
		}
	}
	
	public MethodDeclaration createEquals(EclipseNode type, Collection<EclipseNode> fields, boolean callSuper, ASTNode source, FieldAccess fieldAccess, boolean needsCanEqual, List<Annotation> onParam) {
		int pS = source.sourceStart; int pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(method, source);
		method.modifiers = toEclipseModifier(AccessLevel.PUBLIC);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		setGeneratedBy(method.returnType, source);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		method.selector = "equals".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		TypeReference objectRef = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { p, p, p });
		setGeneratedBy(objectRef, source);
		method.arguments = new Argument[] {new Argument(new char[] { 'o' }, 0, objectRef, Modifier.FINAL)};
		method.arguments[0].sourceStart = pS; method.arguments[0].sourceEnd = pE;
		if (!onParam.isEmpty()) method.arguments[0].annotations = onParam.toArray(new Annotation[0]);
		setGeneratedBy(method.arguments[0], source);
		
		List<Statement> statements = new ArrayList<Statement>();
		
		/* if (o == this) return true; */ {
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			setGeneratedBy(oRef, source);
			ThisReference thisRef = new ThisReference(pS, pE);
			setGeneratedBy(thisRef, source);
			EqualExpression otherEqualsThis = new EqualExpression(oRef, thisRef, OperatorIds.EQUAL_EQUAL);
			setGeneratedBy(otherEqualsThis, source);
			
			TrueLiteral trueLiteral = new TrueLiteral(pS, pE);
			setGeneratedBy(trueLiteral, source);
			ReturnStatement returnTrue = new ReturnStatement(trueLiteral, pS, pE);
			setGeneratedBy(returnTrue, source);
			IfStatement ifOtherEqualsThis = new IfStatement(otherEqualsThis, returnTrue, pS, pE);
			setGeneratedBy(ifOtherEqualsThis, source);
			statements.add(ifOtherEqualsThis);
		}
		
		/* if (!(o instanceof Outer.Inner.MyType) return false; */ {
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			setGeneratedBy(oRef, source);
			
			TypeReference typeReference = createTypeReference(type, p, source, false);
			setGeneratedBy(typeReference, source);

			InstanceOfExpression instanceOf = new InstanceOfExpression(oRef, typeReference);
			instanceOf.sourceStart = pS; instanceOf.sourceEnd = pE;
			setGeneratedBy(instanceOf, source);
			
			Expression notInstanceOf = new UnaryExpression(instanceOf, OperatorIds.NOT);
			setGeneratedBy(notInstanceOf, source);
			
			FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
			setGeneratedBy(falseLiteral, source);
			
			ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
			setGeneratedBy(returnFalse, source);
			
			IfStatement ifNotInstanceOf = new IfStatement(notInstanceOf, returnFalse, pS, pE);
			setGeneratedBy(ifNotInstanceOf, source);
			statements.add(ifNotInstanceOf);
		}
		
		char[] otherName = "other".toCharArray();
		
		/* Outer.Inner.MyType<?> other = (Outer.Inner.MyType<?>) o; */ {
			if (!fields.isEmpty() || needsCanEqual) {
				LocalDeclaration other = new LocalDeclaration(otherName, pS, pE);
				other.modifiers |= ClassFileConstants.AccFinal;
				setGeneratedBy(other, source);
				TypeReference targetType = createTypeReference(type, p, source, true);
				setGeneratedBy(targetType, source);
				other.type = createTypeReference(type, p, source, true);
				setGeneratedBy(other.type, source);
				NameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
				setGeneratedBy(oRef, source);
				other.initialization = makeCastExpression(oRef, targetType, source);
				statements.add(other);
			}
		}
		
		/* if (!other.canEqual((java.lang.Object) this)) return false; */ {
			if (needsCanEqual) {
				MessageSend otherCanEqual = new MessageSend();
				otherCanEqual.sourceStart = pS; otherCanEqual.sourceEnd = pE;
				setGeneratedBy(otherCanEqual, source);
				otherCanEqual.receiver = new SingleNameReference(otherName, p);
				setGeneratedBy(otherCanEqual.receiver, source);
				otherCanEqual.selector = "canEqual".toCharArray();
				
				ThisReference thisReference = new ThisReference(pS, pE);
				setGeneratedBy(thisReference, source);
				CastExpression castThisRef = makeCastExpression(thisReference, generateQualifiedTypeRef(source, TypeConstants.JAVA_LANG_OBJECT), source);
				castThisRef.sourceStart = pS; castThisRef.sourceEnd = pE;
				
				otherCanEqual.arguments = new Expression[] {castThisRef};
				
				Expression notOtherCanEqual = new UnaryExpression(otherCanEqual, OperatorIds.NOT);
				setGeneratedBy(notOtherCanEqual, source);
				
				FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
				setGeneratedBy(falseLiteral, source);
				
				ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
				setGeneratedBy(returnFalse, source);
				
				IfStatement ifNotCanEqual = new IfStatement(notOtherCanEqual, returnFalse, pS, pE);
				setGeneratedBy(ifNotCanEqual, source);
				
				statements.add(ifNotCanEqual);
			}
		}
		
		/* if (!super.equals(o)) return false; */
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			setGeneratedBy(callToSuper, source);
			callToSuper.receiver = new SuperReference(pS, pE);
			setGeneratedBy(callToSuper.receiver, source);
			callToSuper.selector = "equals".toCharArray();
			SingleNameReference oRef = new SingleNameReference(new char[] { 'o' }, p);
			setGeneratedBy(oRef, source);
			callToSuper.arguments = new Expression[] {oRef};
			Expression superNotEqual = new UnaryExpression(callToSuper, OperatorIds.NOT);
			setGeneratedBy(superNotEqual, source);
			FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
			setGeneratedBy(falseLiteral, source);
			ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
			setGeneratedBy(returnFalse, source);
			IfStatement ifSuperEquals = new IfStatement(superNotEqual, returnFalse, pS, pE);
			setGeneratedBy(ifSuperEquals, source);
			statements.add(ifSuperEquals);
		}
		
		for (EclipseNode field : fields) {
			TypeReference fType = getFieldType(field, fieldAccess);
			char[] token = fType.getLastToken();
			Expression thisFieldAccessor = createFieldAccessor(field, fieldAccess, source);
			Expression otherFieldAccessor = createFieldAccessor(field, fieldAccess, source, otherName);
			
			if (fType.dimensions() == 0 && token != null) {
				if (Arrays.equals(TypeConstants.FLOAT, token)) {
					statements.add(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, "Float".toCharArray(), source));
				} else if (Arrays.equals(TypeConstants.DOUBLE, token)) {
					statements.add(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, "Double".toCharArray(), source));
				} else if (BUILT_IN_TYPES.contains(new String(token))) {
					EqualExpression fieldsNotEqual = new EqualExpression(thisFieldAccessor, otherFieldAccessor, OperatorIds.NOT_EQUAL);
					setGeneratedBy(fieldsNotEqual, source);
					FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
					setGeneratedBy(falseLiteral, source);
					ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
					setGeneratedBy(returnStatement, source);
					IfStatement ifStatement = new IfStatement(fieldsNotEqual, returnStatement, pS, pE);
					setGeneratedBy(ifStatement, source);
					statements.add(ifStatement);
				} else /* objects */ {
					/* final java.lang.Object this$fieldName = this.fieldName; */
					/* final java.lang.Object other$fieldName = other.fieldName; */
					/* if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false;; */
					char[] thisDollarFieldName = ("this$" + field.getName()).toCharArray();
					char[] otherDollarFieldName = ("other$" + field.getName()).toCharArray();
					
					statements.add(createLocalDeclaration(source, thisDollarFieldName, generateQualifiedTypeRef(source, TypeConstants.JAVA_LANG_OBJECT), thisFieldAccessor));
					statements.add(createLocalDeclaration(source, otherDollarFieldName, generateQualifiedTypeRef(source, TypeConstants.JAVA_LANG_OBJECT), otherFieldAccessor));
					
					SingleNameReference this1 = new SingleNameReference(thisDollarFieldName, p);
					setGeneratedBy(this1, source);
					SingleNameReference this2 = new SingleNameReference(thisDollarFieldName, p);
					setGeneratedBy(this2, source);
					SingleNameReference other1 = new SingleNameReference(otherDollarFieldName, p);
					setGeneratedBy(other1, source);
					SingleNameReference other2 = new SingleNameReference(otherDollarFieldName, p);
					setGeneratedBy(other2, source);

					
					NullLiteral nullLiteral = new NullLiteral(pS, pE);
					setGeneratedBy(nullLiteral, source);
					EqualExpression fieldIsNull = new EqualExpression(this1, nullLiteral, OperatorIds.EQUAL_EQUAL);
					nullLiteral = new NullLiteral(pS, pE);
					setGeneratedBy(nullLiteral, source);
					EqualExpression otherFieldIsntNull = new EqualExpression(other1, nullLiteral, OperatorIds.NOT_EQUAL);
					MessageSend equalsCall = new MessageSend();
					equalsCall.sourceStart = pS; equalsCall.sourceEnd = pE;
					setGeneratedBy(equalsCall, source);
					equalsCall.receiver = this2;
					equalsCall.selector = "equals".toCharArray();
					equalsCall.arguments = new Expression[] { other2 };
					UnaryExpression fieldsNotEqual = new UnaryExpression(equalsCall, OperatorIds.NOT);
					fieldsNotEqual.sourceStart = pS; fieldsNotEqual.sourceEnd = pE;
					setGeneratedBy(fieldsNotEqual, source);
					ConditionalExpression fullEquals = new ConditionalExpression(fieldIsNull, otherFieldIsntNull, fieldsNotEqual);
					fullEquals.sourceStart = pS; fullEquals.sourceEnd = pE;
					setGeneratedBy(fullEquals, source);
					FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
					setGeneratedBy(falseLiteral, source);
					ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
					setGeneratedBy(returnStatement, source);
					IfStatement ifStatement = new IfStatement(fullEquals, returnStatement, pS, pE);
					setGeneratedBy(ifStatement, source);
					statements.add(ifStatement);
				}
			} else if (fType.dimensions() > 0 && token != null) {
				MessageSend arraysEqualCall = new MessageSend();
				arraysEqualCall.sourceStart = pS; arraysEqualCall.sourceEnd = pE;
				setGeneratedBy(arraysEqualCall, source);
				arraysEqualCall.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				if (fType.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(token))) {
					arraysEqualCall.selector = "deepEquals".toCharArray();
				} else {
					arraysEqualCall.selector = "equals".toCharArray();
				}
				arraysEqualCall.arguments = new Expression[] { thisFieldAccessor, otherFieldAccessor };
				UnaryExpression arraysNotEqual = new UnaryExpression(arraysEqualCall, OperatorIds.NOT);
				arraysNotEqual.sourceStart = pS; arraysNotEqual.sourceEnd = pE;
				setGeneratedBy(arraysNotEqual, source);
				FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
				setGeneratedBy(falseLiteral, source);
				ReturnStatement returnStatement = new ReturnStatement(falseLiteral, pS, pE);
				setGeneratedBy(returnStatement, source);
				IfStatement ifStatement = new IfStatement(arraysNotEqual, returnStatement, pS, pE);
				setGeneratedBy(ifStatement, source);
				statements.add(ifStatement);
			}
		}
		
		/* return true; */ {
			TrueLiteral trueLiteral = new TrueLiteral(pS, pE);
			setGeneratedBy(trueLiteral, source);
			ReturnStatement returnStatement = new ReturnStatement(trueLiteral, pS, pE);
			setGeneratedBy(returnStatement, source);
			statements.add(returnStatement);
		}
		method.statements = statements.toArray(new Statement[statements.size()]);
		return method;
	}
	
	
	public MethodDeclaration createCanEqual(EclipseNode type, ASTNode source, List<Annotation> onParam) {
		/* protected boolean canEqual(final java.lang.Object other) {
		 *     return other instanceof Outer.Inner.MyType;
		 * }
		 */
		int pS = source.sourceStart; int pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		char[] otherName = "other".toCharArray();
		
		MethodDeclaration method = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(method, source);
		method.modifiers = toEclipseModifier(AccessLevel.PROTECTED);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		setGeneratedBy(method.returnType, source);
		method.selector = "canEqual".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		TypeReference objectRef = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { p, p, p });
		setGeneratedBy(objectRef, source);
		method.arguments = new Argument[] {new Argument(otherName, 0, objectRef, Modifier.FINAL)};
		method.arguments[0].sourceStart = pS; method.arguments[0].sourceEnd = pE;
		if (!onParam.isEmpty()) method.arguments[0].annotations = onParam.toArray(new Annotation[0]);
		setGeneratedBy(method.arguments[0], source);
		
		SingleNameReference otherRef = new SingleNameReference(otherName, p);
		setGeneratedBy(otherRef, source);
		
		TypeReference typeReference = createTypeReference(type, p, source, false);
		setGeneratedBy(typeReference, source);
		
		InstanceOfExpression instanceOf = new InstanceOfExpression(otherRef, typeReference);
		instanceOf.sourceStart = pS; instanceOf.sourceEnd = pE;
		setGeneratedBy(instanceOf, source);
		
		ReturnStatement returnStatement = new ReturnStatement(instanceOf, pS, pE);
		setGeneratedBy(returnStatement, source);
		
		method.statements = new Statement[] {returnStatement};
		return method;
	}

	
	public IfStatement generateCompareFloatOrDouble(Expression thisRef, Expression otherRef, char[] floatOrDouble, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		/* if (Float.compare(fieldName, other.fieldName) != 0) return false */
		MessageSend floatCompare = new MessageSend();
		floatCompare.sourceStart = pS; floatCompare.sourceEnd = pE;
		setGeneratedBy(floatCompare, source);
		floatCompare.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.LANG, floatOrDouble);
		floatCompare.selector = "compare".toCharArray();
		floatCompare.arguments = new Expression[] {thisRef, otherRef};
		IntLiteral int0 = makeIntLiteral("0".toCharArray(), source);
		EqualExpression ifFloatCompareIsNot0 = new EqualExpression(floatCompare, int0, OperatorIds.NOT_EQUAL);
		ifFloatCompareIsNot0.sourceStart = pS; ifFloatCompareIsNot0.sourceEnd = pE;
		setGeneratedBy(ifFloatCompareIsNot0, source);
		FalseLiteral falseLiteral = new FalseLiteral(pS, pE);
		setGeneratedBy(falseLiteral, source);
		ReturnStatement returnFalse = new ReturnStatement(falseLiteral, pS, pE);
		setGeneratedBy(returnFalse, source);
		IfStatement ifStatement = new IfStatement(ifFloatCompareIsNot0, returnFalse, pS, pE);
		setGeneratedBy(ifStatement, source);
		return ifStatement;
	}
	
	/** Give 2 clones! */
	public Expression longToIntForHashCode(Expression ref1, Expression ref2, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		/* (int)(ref >>> 32 ^ ref) */
		IntLiteral int32 = makeIntLiteral("32".toCharArray(), source);
		BinaryExpression higherBits = new BinaryExpression(ref1, int32, OperatorIds.UNSIGNED_RIGHT_SHIFT);
		setGeneratedBy(higherBits, source);
		BinaryExpression xorParts = new BinaryExpression(ref2, higherBits, OperatorIds.XOR);
		setGeneratedBy(xorParts, source);
		TypeReference intRef = TypeReference.baseTypeReference(TypeIds.T_int, 0);
		intRef.sourceStart = pS; intRef.sourceEnd = pE;
		setGeneratedBy(intRef, source);
		CastExpression expr = makeCastExpression(xorParts, intRef, source);
		expr.sourceStart = pS; expr.sourceEnd = pE;
		return expr;
	}
	
	public NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		NameReference ref;
		
		if (varNames.length > 1) ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
		else ref = new SingleNameReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
	
	public TypeReference generateQualifiedTypeRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		TypeReference ref;
		
		long[] poss = Eclipse.poss(source, varNames.length);
		if (varNames.length > 1) ref = new QualifiedTypeReference(varNames, poss);
		else ref = new SingleTypeReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
}
