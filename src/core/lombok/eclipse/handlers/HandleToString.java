/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CallSuperType;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.core.handlers.InclusionExclusionUtils;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

/**
 * Handles the {@code ToString} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleToString extends EclipseAnnotationHandler<ToString> {
	public void handle(AnnotationValues<ToString> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@ToString");
		
		ToString ann = annotation.getInstance();
		List<Included<EclipseNode, ToString.Include>> members = InclusionExclusionUtils.handleToStringMarking(annotationNode.up(), annotation, annotationNode);
		if (members == null) return;
		
		Boolean callSuper = ann.callSuper();
		
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		
		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		Boolean fieldNamesConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
		boolean includeFieldNames = annotation.isExplicit("includeFieldNames") || fieldNamesConfiguration == null ? ann.includeFieldNames() : fieldNamesConfiguration;
		
		generateToString(annotationNode.up(), annotationNode, members, includeFieldNames, callSuper, true, fieldAccess);
	}
	
	public void generateToStringForType(EclipseNode typeNode, EclipseNode errorNode) {
		if (hasAnnotation(ToString.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		boolean includeFieldNames = true;
		try {
			Boolean configuration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
			includeFieldNames = configuration != null ? configuration : ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}
		
		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;
		
		List<Included<EclipseNode, ToString.Include>> members = InclusionExclusionUtils.handleToStringMarking(typeNode, null, null);
		generateToString(typeNode, errorNode, members, includeFieldNames, null, false, access);
	}
	
	public void generateToString(EclipseNode typeNode, EclipseNode errorNode, List<Included<EclipseNode, ToString.Include>> members,
		boolean includeFieldNames, Boolean callSuper, boolean whineIfExists, FieldAccess fieldAccess) {
		
		TypeDeclaration typeDecl = null;
		
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@ToString is only supported on a class or enum.");
			return;
		}
		
		switch (methodExists("toString", typeNode, 0)) {
		case NOT_EXISTS:
			if (callSuper == null) {
				if (isDirectDescendantOfObject(typeNode)) {
					callSuper = false;
				} else {
					CallSuperType cst = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_CALL_SUPER);
					if (cst == null) cst = CallSuperType.SKIP;
					switch (cst) {
					default:
					case SKIP:
						callSuper = false;
						break;
					case WARN:
						errorNode.addWarning("Generating toString implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this intentional, add '@ToString(callSuper=false)' to your type.");
						callSuper = false;
						break;
					case CALL:
						callSuper = true;
						break;
					}
				}
			}
			MethodDeclaration toString = createToString(typeNode, members, includeFieldNames, callSuper, errorNode.get(), fieldAccess);
			injectMethod(typeNode, toString);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating toString(): A method with that name already exists");
			}
		}
	}
	
	public static MethodDeclaration createToString(EclipseNode type, Collection<Included<EclipseNode, ToString.Include>> members,
		boolean includeNames, boolean callSuper, ASTNode source, FieldAccess fieldAccess) {
		
		String typeName = getTypeName(type);
		boolean isEnum = type.isEnumType();

		char[] suffix = ")".toCharArray();
		String infixS = ", ";
		char[] infix = infixS.toCharArray();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		final int PLUS = OperatorIds.PLUS;
		
		String prefix;
		
		if (callSuper) {
			prefix = "(super=";
		} else if (members.isEmpty()) {
			prefix = isEnum ? "" : "()";
		} else if (includeNames) {
			Included<EclipseNode, ToString.Include> firstMember = members.iterator().next();
			String name = firstMember.getInc() == null ? "" : firstMember.getInc().name();
			if (name.isEmpty()) name = firstMember.getNode().getName();
			prefix = "(" + name + "=";
		} else {
			prefix = "(";
		}
		
		boolean first = true;
		Expression current;
		if (!isEnum) {
			current = new StringLiteral((typeName + prefix).toCharArray(), pS, pE, 0);
			setGeneratedBy(current, source);
		} else {
			current = new StringLiteral((typeName + ".").toCharArray(), pS, pE, 0);
			setGeneratedBy(current, source);

			MessageSend thisName = new MessageSend();
			thisName.sourceStart = pS; thisName.sourceEnd = pE;
			setGeneratedBy(thisName, source);
			thisName.receiver = new ThisReference(pS, pE);
			setGeneratedBy(thisName.receiver, source);
			thisName.selector = "name".toCharArray();
			current = new BinaryExpression(current, thisName, PLUS);
			setGeneratedBy(current, source);
			
			if (!prefix.isEmpty()) {
				StringLiteral px = new StringLiteral(prefix.toCharArray(), pS, pE, 0);
				setGeneratedBy(px, source);				
				current = new BinaryExpression(current, px, PLUS);
				current.sourceStart = pS; current.sourceEnd = pE;
				setGeneratedBy(current, source);
			}
		}
		
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			setGeneratedBy(callToSuper, source);
			callToSuper.receiver = new SuperReference(pS, pE);
			setGeneratedBy(callToSuper.receiver, source);
			callToSuper.selector = "toString".toCharArray();
			current = new BinaryExpression(current, callToSuper, PLUS);
			setGeneratedBy(current, source);
			first = false;
		}
		
		for (Included<EclipseNode, ToString.Include> member : members) {
			EclipseNode memberNode = member.getNode();
			
			TypeReference fieldType = getFieldType(memberNode, fieldAccess);
			Expression memberAccessor;
			if (memberNode.getKind() == Kind.METHOD) {
				memberAccessor = createMethodAccessor(memberNode, source);
			} else {
				memberAccessor = createFieldAccessor(memberNode, fieldAccess, source);
			}
			
			// The distinction between primitive and object will be useful if we ever add a 'hideNulls' option.
			boolean fieldBaseTypeIsPrimitive = BUILT_IN_TYPES.contains(new String(fieldType.getLastToken()));
			@SuppressWarnings("unused")
			boolean fieldIsPrimitive = fieldType.dimensions() == 0 && fieldBaseTypeIsPrimitive;
			boolean fieldIsPrimitiveArray = fieldType.dimensions() == 1 && fieldBaseTypeIsPrimitive;
			boolean fieldIsObjectArray = fieldType.dimensions() > 0 && !fieldIsPrimitiveArray;
			
			Expression ex;
			if (fieldIsPrimitiveArray || fieldIsObjectArray) {
				MessageSend arrayToString = new MessageSend();
				arrayToString.sourceStart = pS; arrayToString.sourceEnd = pE;
				arrayToString.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				arrayToString.arguments = new Expression[] { memberAccessor };
				setGeneratedBy(arrayToString.arguments[0], source);
				arrayToString.selector = (fieldIsObjectArray ? "deepToString" : "toString").toCharArray();
				ex = arrayToString;
			} else {
				ex = memberAccessor;
			}
			setGeneratedBy(ex, source);
			
			if (first) {
				current = new BinaryExpression(current, ex, PLUS);
				current.sourceStart = pS; current.sourceEnd = pE;
				setGeneratedBy(current, source);
				first = false;
				continue;
			}
			
			StringLiteral fieldNameLiteral;
			if (includeNames) {
				String n = member.getInc() == null ? "" : member.getInc().name();
				if (n.isEmpty()) n = memberNode.getName();
				char[] namePlusEqualsSign = (infixS + n + "=").toCharArray();
				fieldNameLiteral = new StringLiteral(namePlusEqualsSign, pS, pE, 0);
			} else {
				fieldNameLiteral = new StringLiteral(infix, pS, pE, 0);
			}
			setGeneratedBy(fieldNameLiteral, source);
			current = new BinaryExpression(current, fieldNameLiteral, PLUS);
			setGeneratedBy(current, source);
			current = new BinaryExpression(current, ex, PLUS);
			setGeneratedBy(current, source);
		}
		if (!first) {
			StringLiteral suffixLiteral = new StringLiteral(suffix, pS, pE, 0);
			setGeneratedBy(suffixLiteral, source);
			current = new BinaryExpression(current, suffixLiteral, PLUS);
			setGeneratedBy(current, source);
		}
		
		ReturnStatement returnStatement = new ReturnStatement(current, pS, pE);
		setGeneratedBy(returnStatement, source);
		
		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(method, source);
		method.modifiers = toEclipseModifier(AccessLevel.PUBLIC);
		method.returnType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {p, p, p});
		setGeneratedBy(method.returnType, source);
		Annotation overrideAnnotation = makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source);
		if (getCheckerFrameworkVersion(type).generateSideEffectFree()) {
			method.annotations = new Annotation[] { overrideAnnotation, generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE) };
		} else {
			method.annotations = new Annotation[] { overrideAnnotation };
		}
		method.arguments = null;
		method.selector = "toString".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
	
	public static String getTypeName(EclipseNode type) {
		String typeName = getSingleTypeName(type);
		EclipseNode upType = type.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = getSingleTypeName(upType) + "." + typeName;
			upType = upType.up();
		}
		return typeName;
	}
	
	public static String getSingleTypeName(EclipseNode type) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		char[] rawTypeName = typeDeclaration.name;
		return rawTypeName == null ? "" : new String(rawTypeName);
	}
	
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	public static NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		NameReference ref;
		if (varNames.length > 1) ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
		else ref = new SingleNameReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
}
