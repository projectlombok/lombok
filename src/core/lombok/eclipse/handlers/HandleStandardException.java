/*
 * Copyright (C) 2021-2024 The Project Lombok Authors.
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

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.experimental.StandardException;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.*;
import lombok.spi.Provides;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import java.lang.reflect.Modifier;
import java.util.*;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

@Provides
public class HandleStandardException extends EclipseAnnotationHandler<StandardException> {
	@Override
	public void handle(AnnotationValues<StandardException> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.STANDARD_EXCEPTION_FLAG_USAGE, "@StandardException");
		
		EclipseNode typeNode = annotationNode.up();
		if (!isClass(typeNode)) {
			annotationNode.addError("@StandardException is only supported on a class");
			return;
		}
		
		TypeDeclaration classDef = (TypeDeclaration) typeNode.get();
		if (classDef.superclass == null) {
			annotationNode.addError("@StandardException requires that you extend a Throwable type");
			return;
		}
		
		AccessLevel access = annotation.getInstance().access();
		
		generateNoArgsConstructor(typeNode, access, annotationNode);
		generateMsgOnlyConstructor(typeNode, access, annotationNode);
		generateCauseOnlyConstructor(typeNode, access, annotationNode);
		generateFullConstructor(typeNode, access, annotationNode);
	}
	
	private void generateNoArgsConstructor(EclipseNode typeNode, AccessLevel level, EclipseNode source) {
		if (hasConstructor(typeNode) != MemberExistsResult.NOT_EXISTS) return;
		int pS = source.get().sourceStart, pE = source.get().sourceEnd;
		
		Expression messageArgument = new CastExpression(new NullLiteral(pS, pE), generateQualifiedTypeRef(source.get(), TypeConstants.JAVA_LANG_STRING));
		Expression causeArgument = new CastExpression(new NullLiteral(pS, pE), generateQualifiedTypeRef(source.get(), TypeConstants.JAVA_LANG_THROWABLE));
		ExplicitConstructorCall explicitCall = new ExplicitConstructorCall(ExplicitConstructorCall.This);
		explicitCall.arguments = new Expression[] {messageArgument, causeArgument};
		ConstructorDeclaration constructor = createConstructor(level, typeNode, false, false, source, explicitCall, null);
		injectMethod(typeNode, constructor);
	}
	
	private void generateMsgOnlyConstructor(EclipseNode typeNode, AccessLevel level, EclipseNode source) {
		if (hasConstructor(typeNode, String.class) != MemberExistsResult.NOT_EXISTS) return;
		int pS = source.get().sourceStart, pE = source.get().sourceEnd;
		long p = (long) pS << 32 | pE;
		
		Expression messageArgument = new SingleNameReference(MESSAGE, p);
		Expression causeArgument = new CastExpression(new NullLiteral(pS, pE), generateQualifiedTypeRef(source.get(), TypeConstants.JAVA_LANG_THROWABLE));
		ExplicitConstructorCall explicitCall = new ExplicitConstructorCall(ExplicitConstructorCall.This);
		explicitCall.arguments = new Expression[] {messageArgument, causeArgument};
		ConstructorDeclaration constructor = createConstructor(level, typeNode, true, false, source, explicitCall, null);
		injectMethod(typeNode, constructor);
	}
	
	private void generateCauseOnlyConstructor(EclipseNode typeNode, AccessLevel level, EclipseNode source) {
		if (hasConstructor(typeNode, Throwable.class) != MemberExistsResult.NOT_EXISTS) return;
		int pS = source.get().sourceStart, pE = source.get().sourceEnd;
		long p = (long) pS << 32 | pE;
		
		ExplicitConstructorCall explicitCall = new ExplicitConstructorCall(ExplicitConstructorCall.This);
		Expression causeNotNull = new EqualExpression(new SingleNameReference(CAUSE, p), new NullLiteral(pS, pE), OperatorIds.NOT_EQUAL);
		MessageSend causeDotGetMessage = new MessageSend();
		causeDotGetMessage.sourceStart = pS; causeDotGetMessage.sourceEnd = pE;
		causeDotGetMessage.receiver = new SingleNameReference(CAUSE, p);
		causeDotGetMessage.selector = GET_MESSAGE;
		Expression messageExpr = new ConditionalExpression(causeNotNull, causeDotGetMessage, new NullLiteral(pS, pE));
		explicitCall.arguments = new Expression[] {messageExpr, new SingleNameReference(CAUSE, p)};
		ConstructorDeclaration constructor = createConstructor(level, typeNode, false, true, source, explicitCall, null);
		injectMethod(typeNode, constructor);
	}
	
	private void generateFullConstructor(EclipseNode typeNode, AccessLevel level, EclipseNode source) {
		if (hasConstructor(typeNode, String.class, Throwable.class) != MemberExistsResult.NOT_EXISTS) return;
		int pS = source.get().sourceStart, pE = source.get().sourceEnd;
		long p = (long) pS << 32 | pE;
		
		ExplicitConstructorCall explicitCall = new ExplicitConstructorCall(ExplicitConstructorCall.Super);
		explicitCall.arguments = new Expression[] {new SingleNameReference(MESSAGE, p)};
		Expression causeNotNull = new EqualExpression(new SingleNameReference(CAUSE, p), new NullLiteral(pS, pE), OperatorIds.NOT_EQUAL);
		MessageSend causeDotInitCause = new MessageSend();
		causeDotInitCause.sourceStart = pS; causeDotInitCause.sourceEnd = pE;
		causeDotInitCause.receiver = new SuperReference(pS, pE);
		causeDotInitCause.selector = INIT_CAUSE;
		causeDotInitCause.arguments = new Expression[] {new SingleNameReference(CAUSE, p)};
		IfStatement ifs = new IfStatement(causeNotNull, causeDotInitCause, pS, pE);
		ConstructorDeclaration constructor = createConstructor(level, typeNode, true, true, source, explicitCall, ifs);
		injectMethod(typeNode, constructor);
	}
	
	/**
	 * Checks if a constructor with the provided parameters exists under the type node.
	 */
	public static MemberExistsResult hasConstructor(EclipseNode node, Class<?>... paramTypes) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) node.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (def instanceof ConstructorDeclaration) {
					if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
					if (!paramsMatch(node, def.arguments, paramTypes)) continue;
					return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	private static boolean paramsMatch(EclipseNode node, Argument[] a, Class<?>[] b) {
		if (a == null) return b == null || b.length == 0;
		if (b == null) return a.length == 0;
		if (a.length != b.length) return false;
		
		for (int i = 0; i < a.length; i++) {
			if (!typeMatches(b[i], node, a[i].type)) return false;
		}
		return true;
	}
	
	private static final char[][] JAVA_BEANS_CONSTRUCTORPROPERTIES = new char[][] { "java".toCharArray(), "beans".toCharArray(), "ConstructorProperties".toCharArray() };
	private static final char[] MESSAGE = "message".toCharArray(), CAUSE = "cause".toCharArray(), GET_MESSAGE = "getMessage".toCharArray(), INIT_CAUSE = "initCause".toCharArray();
	
	public static Annotation[] createConstructorProperties(ASTNode source, boolean msgParam, boolean causeParam) {
		if (!msgParam && !causeParam) return null;
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		long[] poss = new long[3];
		Arrays.fill(poss, p);
		
		QualifiedTypeReference constructorPropertiesType = new QualifiedTypeReference(JAVA_BEANS_CONSTRUCTORPROPERTIES, poss);
		setGeneratedBy(constructorPropertiesType, source);
		SingleMemberAnnotation ann = new SingleMemberAnnotation(constructorPropertiesType, pS);
		ann.declarationSourceEnd = pE;
		
		ArrayInitializer fieldNames = new ArrayInitializer();
		fieldNames.sourceStart = pS;
		fieldNames.sourceEnd = pE;
		fieldNames.expressions = new Expression[(msgParam && causeParam) ? 2 : 1];
		
		int ctr = 0;
		if (msgParam) {
			fieldNames.expressions[ctr] = new StringLiteral(MESSAGE, pS, pE, 0);
			setGeneratedBy(fieldNames.expressions[ctr], source);
			ctr++;
		}
		if (causeParam) {
			fieldNames.expressions[ctr] = new StringLiteral(CAUSE, pS, pE, 0);
			setGeneratedBy(fieldNames.expressions[ctr], source);
			ctr++;
		}
		
		ann.memberValue = fieldNames;
		setGeneratedBy(ann, source);
		setGeneratedBy(ann.memberValue, source);
		return new Annotation[] { ann };
	}
	
	@SuppressWarnings("deprecation") public static ConstructorDeclaration createConstructor(AccessLevel level, EclipseNode typeNode, boolean msgParam, boolean causeParam, EclipseNode sourceNode, ExplicitConstructorCall explicitCall, Statement extra) {
		ASTNode source = sourceNode.get();
		TypeDeclaration typeDeclaration = ((TypeDeclaration) typeNode.get());
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		boolean addConstructorProperties;
		if ((!msgParam && !causeParam) || isLocalType(typeNode)) {
			addConstructorProperties = false;
		} else {
			Boolean v = typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES);
			addConstructorProperties = v != null ? v.booleanValue() :
				Boolean.FALSE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(((CompilationUnitDeclaration) typeNode.top().get()).compilationResult);
		
		constructor.modifiers = toEclipseModifier(level);
		constructor.selector = typeDeclaration.name;
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pS;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pE;
		constructor.arguments = null;
		
		List<Argument> params = new ArrayList<Argument>();
		
		if (msgParam) {
			TypeReference typeRef = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {p, p, p});
			Argument parameter = new Argument(MESSAGE, p, typeRef, Modifier.FINAL);
			params.add(parameter);
		}
		if (causeParam) {
			TypeReference typeRef = new QualifiedTypeReference(TypeConstants.JAVA_LANG_THROWABLE, new long[] {p, p, p});
			Argument parameter = new Argument(CAUSE, p, typeRef, Modifier.FINAL);
			params.add(parameter);
		}
		
		explicitCall.sourceStart = pS;
		explicitCall.sourceEnd = pE;
		constructor.constructorCall = explicitCall;
		constructor.statements = extra != null ? new Statement[] {extra} : null;
		constructor.arguments = params.isEmpty() ? null : params.toArray(new Argument[0]);
		
		Annotation[] constructorProperties = null;
		if (addConstructorProperties) constructorProperties = createConstructorProperties(source, msgParam, causeParam);
		constructor.annotations = copyAnnotations(source, constructorProperties);
		constructor.traverse(new SetGeneratedByVisitor(source), typeDeclaration.scope);
		return constructor;
	}

	public static boolean isLocalType(EclipseNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
}
