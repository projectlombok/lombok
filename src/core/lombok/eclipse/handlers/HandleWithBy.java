/*
 * Copyright (C) 2020 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.WithBy;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleWithBy extends EclipseAnnotationHandler<WithBy> {
	public boolean generateWithByForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelWithBy) {
		if (checkForTypeLevelWithBy) {
			if (hasAnnotation(WithBy.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			pos.addError("@WithBy is only supported on a class or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
			if (!filterField(fieldDecl)) continue;
			
			//Skip final fields.
			if ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0 && fieldDecl.initialization != null) continue;
			
			generateWithByForField(field, pos, level);
		}
		return true;
	}
	
	
	/**
	 * Generates a withBy on the stated field.
	 * 
	 * Used by {@link HandleValue}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.experimental.WithBy} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the with method is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateWithByForField(EclipseNode fieldNode, EclipseNode sourceNode, AccessLevel level) {
		for (EclipseNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(WithBy.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		List<Annotation> empty = Collections.emptyList();
		createWithByForField(level, fieldNode, sourceNode, false, empty);
	}
	
	@Override public void handle(AnnotationValues<WithBy> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.WITHBY_FLAG_USAGE, "@WithBy");
		
		EclipseNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		if (level == AccessLevel.NONE || node == null) return;
		
		List<Annotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@WithBy(onMethod", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createWithByForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, true, onMethod);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) {
				annotationNode.addError("'onMethod' is not supported for @WithBy on a type.");
			}
			generateWithByForType(node, annotationNode, level, false);
			break;
		}
	}
	
	public void createWithByForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode sourceNode, boolean whineIfExists, List<Annotation> onMethod) {
		for (EclipseNode fieldNode : fieldNodes) {
			createWithByForField(level, fieldNode, sourceNode, whineIfExists, onMethod);
		}
	}
	
	public void createWithByForField(
		AccessLevel level, EclipseNode fieldNode, EclipseNode sourceNode,
		boolean whineIfExists, List<Annotation> onMethod) {
		
		ASTNode source = sourceNode.get();
		if (fieldNode.getKind() != Kind.FIELD) {
			sourceNode.addError("@WithBy is only supported on a class or a field.");
			return;
		}
		
		EclipseNode typeNode = fieldNode.up();
		boolean makeAbstract = typeNode != null && typeNode.getKind() == Kind.TYPE && (((TypeDeclaration) typeNode.get()).modifiers & ClassFileConstants.AccAbstract) != 0;
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = copyType(field.type, source);
		boolean isBoolean = isBoolean(fieldType);
		String withName = toWithByName(fieldNode, isBoolean);
		
		if (withName == null) {
			fieldNode.addWarning("Not generating a withXBy method for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((field.modifiers & ClassFileConstants.AccStatic) != 0) {
			fieldNode.addWarning("Not generating " + withName + " for this field: With methods cannot be generated for static fields.");
			return;
		}
		
		if ((field.modifiers & ClassFileConstants.AccFinal) != 0 && field.initialization != null) {
			fieldNode.addWarning("Not generating " + withName + " for this field: With methods cannot be generated for final, initialized fields.");
			return;
		}
		
		if (field.name != null && field.name.length > 0 && field.name[0] == '$') {
			fieldNode.addWarning("Not generating " + withName + " for this field: With methods cannot be generated for fields starting with $.");
			return;
		}
		
		for (String altName : toAllWithByNames(fieldNode, isBoolean)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(withName)) altNameExpl = String.format(" (%s)", altName);
					fieldNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", withName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		int modifier = toEclipseModifier(level);
		
		MethodDeclaration method = createWithBy((TypeDeclaration) fieldNode.up().get(), fieldNode, withName, modifier, sourceNode, onMethod, makeAbstract);
		injectMethod(fieldNode.up(), method);
	}
	
	private static final char[][] NAME_JUF_FUNCTION = Eclipse.fromQualifiedName("java.util.function.Function");
	private static final char[][] NAME_JUF_OP = Eclipse.fromQualifiedName("java.util.function.UnaryOperator");
	private static final char[][] NAME_JUF_DOUBLEOP = Eclipse.fromQualifiedName("java.util.function.DoubleUnaryOperator");
	private static final char[][] NAME_JUF_INTOP = Eclipse.fromQualifiedName("java.util.function.IntUnaryOperator");
	private static final char[][] NAME_JUF_LONGOP = Eclipse.fromQualifiedName("java.util.function.LongUnaryOperator");
	private static final char[] NAME_CHAR = {'c', 'h', 'a', 'r'};
	private static final char[] NAME_SHORT = {'s', 'h', 'o', 'r', 't'};
	private static final char[] NAME_BYTE = {'b', 'y', 't', 'e'};
	private static final char[] NAME_INT = {'i', 'n', 't'};
	private static final char[] NAME_LONG = {'l', 'o', 'n', 'g'};
	private static final char[] NAME_DOUBLE = {'d', 'o', 'u', 'b', 'l', 'e'};
	private static final char[] NAME_FLOAT = {'f', 'l', 'o', 'a', 't'};
	private static final char[] NAME_BOOLEAN = {'b', 'o', 'o', 'l', 'e', 'a', 'n'};
	private static final char[][] NAME_JAVA_LANG_BOOLEAN = Eclipse.fromQualifiedName("java.lang.Boolean");
	private static final char[] NAME_APPLY = {'a', 'p', 'p', 'l', 'y'};
	private static final char[] NAME_APPLY_AS_INT = {'a', 'p', 'p', 'l', 'y', 'A', 's', 'I', 'n', 't'};
	private static final char[] NAME_APPLY_AS_LONG = {'a', 'p', 'p', 'l', 'y', 'A', 's', 'L', 'o', 'n', 'g'};
	private static final char[] NAME_APPLY_AS_DOUBLE = {'a', 'p', 'p', 'l', 'y', 'A', 's', 'D', 'o', 'u', 'b', 'l', 'e'};
	private static final char[] NAME_TRANSFORMER = {'t', 'r', 'a', 'n', 's', 'f', 'o', 'r', 'm', 'e', 'r'};

	public MethodDeclaration createWithBy(TypeDeclaration parent, EclipseNode fieldNode, String name, int modifier, EclipseNode sourceNode, List<Annotation> onMethod, boolean makeAbstract) {
		ASTNode source = sourceNode.get();
		if (name == null) return null;
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		if (makeAbstract) modifier = modifier | ClassFileConstants.AccAbstract | ExtraCompilerModifiers.AccSemicolonBody;
		method.modifiers = modifier;
		method.returnType = cloneSelfType(fieldNode, source);
		if (method.returnType == null) return null;
		
		Annotation[] deprecated = null, checkerFramework = null;
		if (isFieldDeprecated(fieldNode)) deprecated = new Annotation[] { generateDeprecatedAnnotation(source) };
		if (getCheckerFrameworkVersion(fieldNode).generateSideEffectFree()) checkerFramework = new Annotation[] { generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE) };
		
		char[][] functionalInterfaceName = null;
		int requiredCast = -1;
		TypeReference parameterizer = null;
		boolean superExtendsStyle = true;
		char[] applyMethodName = NAME_APPLY;
		
		if (field.type instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference) field.type).token;
			if (Arrays.equals(token, NAME_CHAR)) {
				requiredCast = TypeIds.T_char;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (Arrays.equals(token, NAME_SHORT)) {
				requiredCast = TypeIds.T_short;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (Arrays.equals(token, NAME_BYTE)) {
				requiredCast = TypeIds.T_byte;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (Arrays.equals(token, NAME_INT)) {
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (Arrays.equals(token, NAME_LONG)) {
				functionalInterfaceName = NAME_JUF_LONGOP;
			} else if (Arrays.equals(token, NAME_FLOAT)) {
				requiredCast = TypeIds.T_float;
				functionalInterfaceName = NAME_JUF_DOUBLEOP;
			} else if (Arrays.equals(token, NAME_DOUBLE)) {
				functionalInterfaceName = NAME_JUF_DOUBLEOP;
			} else if (Arrays.equals(token, NAME_BOOLEAN)) {
				functionalInterfaceName = NAME_JUF_OP;
				parameterizer = new QualifiedTypeReference(NAME_JAVA_LANG_BOOLEAN, new long[] {0, 0, 0});
				superExtendsStyle = false;
			}
		}
		
		if (functionalInterfaceName == NAME_JUF_INTOP) applyMethodName = NAME_APPLY_AS_INT;
		if (functionalInterfaceName == NAME_JUF_LONGOP) applyMethodName = NAME_APPLY_AS_LONG;
		if (functionalInterfaceName == NAME_JUF_DOUBLEOP) applyMethodName = NAME_APPLY_AS_DOUBLE;
		if (functionalInterfaceName == null) {
			functionalInterfaceName = NAME_JUF_FUNCTION;
			parameterizer = copyType(field.type, source);
		}
		
		method.annotations = copyAnnotations(source, onMethod.toArray(new Annotation[0]), checkerFramework, deprecated);
		TypeReference fType = null;
		if (parameterizer != null && superExtendsStyle) {
			Wildcard w1 = new Wildcard(Wildcard.SUPER);
			w1.bound = parameterizer;
			Wildcard w2 = new Wildcard(Wildcard.EXTENDS);
			w2.bound = copyType(field.type, source);
			TypeReference[][] ta = new TypeReference[functionalInterfaceName.length][];
			ta[functionalInterfaceName.length - 1] = new TypeReference[] {w1, w2};
			long[] ps = new long[functionalInterfaceName.length];
			fType = new ParameterizedQualifiedTypeReference(functionalInterfaceName, ta, 0, ps);
		}
		if (parameterizer != null && !superExtendsStyle) {
			TypeReference[][] ta = new TypeReference[functionalInterfaceName.length][];
			ta[functionalInterfaceName.length - 1] = new TypeReference[] {parameterizer};
			long[] ps = new long[functionalInterfaceName.length];
			fType = new ParameterizedQualifiedTypeReference(functionalInterfaceName, ta, 0, ps);
		}
		if (parameterizer == null) {
			long[] ps = new long[functionalInterfaceName.length];
			fType = new QualifiedTypeReference(functionalInterfaceName, ps);
		}
		
		Argument param = new Argument(NAME_TRANSFORMER, p, fType, ClassFileConstants.AccFinal);
		param.sourceStart = pS; param.sourceEnd = pE;
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		if (!makeAbstract) {
			List<Expression> args = new ArrayList<Expression>();
			for (EclipseNode child : fieldNode.up().down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration childDecl = (FieldDeclaration) child.get();
				// Skip fields that start with $
				if (childDecl.name != null && childDecl.name.length > 0 && childDecl.name[0] == '$') continue;
				long fieldFlags = childDecl.modifiers;
				// Skip static fields.
				if ((fieldFlags & ClassFileConstants.AccStatic) != 0) continue;
				// Skip initialized final fields.
				if (((fieldFlags & ClassFileConstants.AccFinal) != 0) && childDecl.initialization != null) continue;
				if (child.get() == fieldNode.get()) {
					MessageSend ms = new MessageSend();
					ms.receiver = new SingleNameReference(NAME_TRANSFORMER, 0);
					ms.selector = applyMethodName;
					ms.arguments = new Expression[] {createFieldAccessor(child, FieldAccess.ALWAYS_FIELD, source)};
					if (requiredCast != -1) {
						args.add(makeCastExpression(ms, TypeReference.baseTypeReference(requiredCast, 0), source));
					} else {
						args.add(ms);
					}
				} else {
					args.add(createFieldAccessor(child, FieldAccess.ALWAYS_FIELD, source));
				}
			}
			
			AllocationExpression constructorCall = new AllocationExpression();
			constructorCall.arguments = args.toArray(new Expression[0]);
			constructorCall.type = cloneSelfType(fieldNode, source);
			
			Statement returnStatement = new ReturnStatement(constructorCall, pS, pE);
			method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
			method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
			
			List<Statement> statements = new ArrayList<Statement>(5);
			if (hasNonNullAnnotations(fieldNode)) {
				Statement nullCheck = generateNullCheck(field, sourceNode, null);
				if (nullCheck != null) statements.add(nullCheck);
			}
			statements.add(returnStatement);
			
			method.statements = statements.toArray(new Statement[0]);
		}
		
		createRelevantNonNullAnnotation(sourceNode, param);
		createRelevantNonNullAnnotation(fieldNode, method);
		
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		copyJavadoc(fieldNode, method, CopyJavadoc.WITH_BY);
		return method;
	}
}
