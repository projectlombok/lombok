/*
 * Copyright (C) 2009-2022 The Project Lombok Authors.
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
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Setter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Accessors;
import lombok.spi.Provides;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Handles the {@code lombok.Setter} annotation for eclipse.
 */
@Provides
public class HandleSetter extends EclipseAnnotationHandler<Setter> {
	private static final String SETTER_NODE_NOT_SUPPORTED_ERR = "@Setter is only supported on a class or a field.";
	
	public boolean generateSetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean internString, boolean checkForTypeLevelSetter, List<Annotation> onMethod, List<Annotation> onParam) {
		if (checkForTypeLevelSetter) {
			if (hasAnnotation(Setter.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		if (!isClass(typeNode)) {
			pos.addError(SETTER_NODE_NOT_SUPPORTED_ERR);
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
			if (!filterField(fieldDecl)) continue;
			
			//Skip final fields.
			if ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0) continue;
			
			generateSetterForField(field, pos, level, internString, onMethod, onParam);
		}
		return true;
	}
	
	/**
	 * Generates a setter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.Setter} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the setter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateSetterForField(EclipseNode fieldNode, EclipseNode sourceNode, AccessLevel level, boolean internString, List<Annotation> onMethod, List<Annotation> onParam) {
		if (hasAnnotation(Setter.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		createSetterForField(level, internString, fieldNode, sourceNode, false, onMethod, onParam);
	}
	
	@Override public void handle(AnnotationValues<Setter> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.SETTER_FLAG_USAGE, "@Setter");
		
		EclipseNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		boolean internString = annotation.getInstance().internString();
		if (level == AccessLevel.NONE || node == null) return;
		
		List<Annotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Setter(onMethod", annotationNode);
		List<Annotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@Setter(onParam", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createSetterForFields(level, internString, annotationNode.upFromAnnotationToFields(), annotationNode, true, onMethod, onParam);
			break;
		case TYPE:
			generateSetterForType(node, annotationNode, level, internString, false, onMethod, onParam);
			break;
		}
	}
	
	public void createSetterForFields(AccessLevel level, boolean internString, Collection<EclipseNode> fieldNodes, EclipseNode sourceNode, boolean whineIfExists, List<Annotation> onMethod, List<Annotation> onParam) {
		for (EclipseNode fieldNode : fieldNodes) {
			createSetterForField(level, internString, fieldNode, sourceNode, whineIfExists, onMethod, onParam);
		}
	}
	
	public void createSetterForField(
			AccessLevel level, boolean internString, EclipseNode fieldNode, EclipseNode sourceNode,
			boolean whineIfExists, List<Annotation> onMethod,
			List<Annotation> onParam) {
		
		ASTNode source = sourceNode.get();
		if (fieldNode.getKind() != Kind.FIELD) {
			sourceNode.addError(SETTER_NODE_NOT_SUPPORTED_ERR);
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		TypeReference fieldType = copyType(field.type, source);
		boolean isBoolean = isBoolean(fieldType);
		AnnotationValues<Accessors> accessors = getAccessorsForField(fieldNode);
		String setterName = toSetterName(fieldNode, isBoolean, accessors);
		boolean shouldReturnThis = shouldReturnThis(fieldNode, accessors);
		
		if (setterName == null) {
			fieldNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for (String altName : toAllSetterNames(fieldNode, isBoolean, accessors)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(setterName)) altNameExpl = String.format(" (%s)", altName);
					fieldNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", setterName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		MethodDeclaration method = createSetter((TypeDeclaration) fieldNode.up().get(), internString, false, fieldNode, setterName, null, null, shouldReturnThis, modifier, sourceNode, onMethod, onParam);
		injectMethod(fieldNode.up(), method);
	}

	static MethodDeclaration createSetter(TypeDeclaration parent, boolean internString, boolean deprecate, EclipseNode fieldNode, String name, char[] paramName, char[] booleanFieldToSet, boolean shouldReturnThis, int modifier, EclipseNode sourceNode, List<Annotation> onMethod, List<Annotation> onParam) {
		ASTNode source = sourceNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		TypeReference returnType = null;
		ReturnStatement returnThis = null;
		if (shouldReturnThis) {
			returnType = cloneSelfType(fieldNode, source);
			addCheckerFrameworkReturnsReceiver(returnType, source, getCheckerFrameworkVersion(sourceNode));
			ThisReference thisRef = new ThisReference(pS, pE);
			returnThis = new ReturnStatement(thisRef, pS, pE);
		}
		
		MethodDeclaration d = createSetter(parent, internString, deprecate, fieldNode, name, paramName, booleanFieldToSet, returnType, returnThis, modifier, sourceNode, onMethod, onParam);
		return d;
	}
	
	static MethodDeclaration createSetter(TypeDeclaration parent, boolean internString, boolean deprecate, EclipseNode fieldNode, String name, char[] paramName, char[] booleanFieldToSet, TypeReference returnType, Statement returnStatement, int modifier, EclipseNode sourceNode, List<Annotation> onMethod, List<Annotation> onParam) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		if (paramName == null) paramName = field.name;
		ASTNode source = sourceNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		AnnotationValues<Accessors> accessors = getAccessorsForField(fieldNode);
		if (shouldMakeFinal(fieldNode, accessors)) modifier |= ClassFileConstants.AccFinal;
		method.modifiers = modifier;
		if (returnType != null) {
			method.returnType = returnType;
		} else {
			method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
			method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		}
		Annotation[] deprecated = null;
		if (isFieldDeprecated(fieldNode) || deprecate) {
			deprecated = new Annotation[] { generateDeprecatedAnnotation(source) };
		}
		method.annotations = copyAnnotations(source, onMethod.toArray(new Annotation[0]), deprecated, findCopyableToSetterAnnotations(fieldNode));
		Argument param = new Argument(paramName, p, copyType(field.type, source), Modifier.FINAL);
		param.sourceStart = pS; param.sourceEnd = pE;
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
		Assignment assignment;
		if(internString && isStringType(field)) {
			ConditionalExpression internExpression = createInternExpression(fieldNode, paramName, p, source);
			assignment = new Assignment(fieldRef, internExpression, (int) p);
		} else {
			NameReference fieldNameRef = new SingleNameReference(paramName, p);
			assignment = new Assignment(fieldRef, fieldNameRef, (int) p);
		}
		
		assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		
		Annotation[] copyableAnnotations = findCopyableAnnotations(fieldNode);
		List<Statement> statements = new ArrayList<Statement>(5);
		if (!hasNonNullAnnotations(fieldNode) && !hasNonNullAnnotations(fieldNode, onParam)) {
			statements.add(assignment);
		} else {
			Statement nullCheck = generateNullCheck(field.type, paramName, sourceNode, null);
			if (nullCheck != null) statements.add(nullCheck);
			statements.add(assignment);
		}
		
		if (booleanFieldToSet != null) {
			statements.add(new Assignment(new SingleNameReference(booleanFieldToSet, p), new TrueLiteral(pS, pE), pE));
		}
		
		if (returnType != null && returnStatement != null) {
			statements.add(returnStatement);
		}
		method.statements = statements.toArray(new Statement[0]);
		param.annotations = copyAnnotations(source, copyableAnnotations, onParam.toArray(new Annotation[0]));
		if (param.annotations != null) {
			param.bits |= Eclipse.HasTypeAnnotations;
			method.bits |= Eclipse.HasTypeAnnotations;
		}
		
		if (returnType != null && returnStatement != null) createRelevantNonNullAnnotation(sourceNode, method);
		
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		copyJavadoc(fieldNode, method, CopyJavadoc.SETTER, returnStatement != null);
		return method;
	}
	
	private static final char[][] TYPENAME_STRING = new char[][] {{'S','t','r','i','n','g'}};
	
	private static final char[][] TYPENAME_STRING_FULL = new char[][] {{'j','a','v','a'},{'l','a','n','g'},{'S','t','r','i','n','g'}};
	
	/* is String or java.lang.String */
	static boolean isStringType(FieldDeclaration field) {
		if (field == null) {
			return false;
		}
		
		char[][] typeName = field.type.getTypeName();
		return CharOperation.equals(typeName, TYPENAME_STRING) || CharOperation.equals(typeName, TYPENAME_STRING_FULL);
	}
	
	/* field == null ? null : field.intern() */
	static ConditionalExpression createInternExpression(EclipseNode fieldNode, char[] paramName, long p, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		NameReference fieldNameRef = new SingleNameReference(paramName, p);
		NullLiteral nullLiteral = new NullLiteral(pS, pE);
		setGeneratedBy(nullLiteral, source);
		EqualExpression fieldIsNull = new EqualExpression(fieldNameRef, nullLiteral, OperatorIds.EQUAL_EQUAL);
		
		nullLiteral = new NullLiteral(pS, pE);
		
		fieldNameRef = new SingleNameReference(paramName, p);
		MessageSend feildIntern = new MessageSend();
		feildIntern.sourceStart = pS; feildIntern.sourceEnd = pE;
		setGeneratedBy(feildIntern, source);
		feildIntern.receiver = fieldNameRef;
		setGeneratedBy(feildIntern.receiver, source);
		feildIntern.selector = "intern".toCharArray();
		
		ConditionalExpression internExpression = new ConditionalExpression(fieldIsNull, nullLiteral, feildIntern);
		internExpression.sourceStart = pS; internExpression.sourceEnd = pE;
		setGeneratedBy(internExpression, source);
		return internExpression;
	}
}
