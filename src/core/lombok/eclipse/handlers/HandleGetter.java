/*
 * Copyright © 2009-2011 Reinier Zwitserloot and Roel Spilker.
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
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.FieldAccess;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Getter} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleGetter implements EclipseAnnotationHandler<Getter> {
	public boolean generateGetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelGetter) {
		if (checkForTypeLevelGetter) {
			if (typeNode != null) for (EclipseNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(Getter.class, child)) {
						//The annotation will make it happen, so we can skip it.
						return true;
					}
				}
			}
		}
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			pos.addError("@Getter is only supported on a class, an enum, or a field.");
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForGetterGeneration(field)) generateGetterForField(field, pos.get(), level, null, false);
		}
		return true;
	}
	
	public boolean fieldQualifiesForGetterGeneration(EclipseNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		return EclipseHandlerUtil.filterField(fieldDecl);
	}
	
	/**
	 * Generates a getter on the stated field.
	 * 
	 * Used by {@link HandleData}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.Getter} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the getter is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 */
	public void generateGetterForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level, Annotation[] onMethod, boolean lazy) {
		for (EclipseNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(Getter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createGetterForField(level, fieldNode, fieldNode, pos, false, onMethod, lazy);
	}
	
	public void handle(AnnotationValues<Getter> annotation, Annotation ast, EclipseNode annotationNode) {
		EclipseNode node = annotationNode.up();
		Getter annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.value();
		boolean lazy = annotationInstance.lazy();
		if (level == AccessLevel.NONE) {
			if (lazy) {
				annotationNode.addWarning("'lazy' does not work with AccessLevel.NONE.");
			}
			return;
		}
		
		if (node == null) return;
		
		Annotation[] onMethod = getAndRemoveAnnotationParameter(ast, "onMethod");
		switch (node.getKind()) {
		case FIELD:
			createGetterForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true, onMethod, lazy);
			break;
		case TYPE:
			if (onMethod != null && onMethod.length != 0) annotationNode.addError("'onMethod' is not supported for @Getter on a type.");
			if (lazy) annotationNode.addError("'lazy' is not supported for @Getter on a type.");
			generateGetterForType(node, annotationNode, level, false);
			break;
		}
	}
	
	private void createGetterForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists, Annotation[] onMethod, boolean lazy) {
		for (EclipseNode fieldNode : fieldNodes) {
			createGetterForField(level, fieldNode, errorNode, source, whineIfExists, onMethod, lazy);
		}
	}
	
	private void createGetterForField(AccessLevel level,
			EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists, Annotation[] onMethod, boolean lazy) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@Getter is only supported on a class or a field.");
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		if (lazy) {
			if ((field.modifiers & ClassFileConstants.AccPrivate) == 0 || (field.modifiers & ClassFileConstants.AccFinal) == 0) {
				errorNode.addError("'lazy' requires the field to be private and final.");
				return;
			}
			if (field.initialization == null) {
				errorNode.addError("'lazy' requires field initialization.");
				return;
			}
		}
		
		TypeReference fieldType = copyType(field.type, source);
		String fieldName = new String(field.name);
		boolean isBoolean = nameEquals(fieldType.getTypeName(), "boolean") && fieldType.dimensions() == 0;
		String getterName = TransformationsUtil.toGetterName(fieldName, isBoolean);
		
		int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for (String altName : TransformationsUtil.toAllGetterNames(fieldName, isBoolean)) {
			switch (methodExists(altName, fieldNode, false)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(getterName)) altNameExpl = String.format(" (%s)", altName);
					errorNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", getterName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		MethodDeclaration method = generateGetter((TypeDeclaration) fieldNode.up().get(), fieldNode, getterName, modifier, source, lazy);
		Annotation[] copiedAnnotations = copyAnnotations(source, findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN), findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN), onMethod);
		if (copiedAnnotations.length != 0) {
			method.annotations = copiedAnnotations;
		}
		
		injectMethod(fieldNode.up(), method);
	}
	
	private MethodDeclaration generateGetter(TypeDeclaration parent, EclipseNode fieldNode, String name, int modifier, ASTNode source, boolean lazy) {
		
		// Remember the type; lazy will change it;
		TypeReference returnType = copyType(((FieldDeclaration) fieldNode.get()).type, source);
		
		Statement[] statements;
		if (lazy) {
			statements = createLazyGetterBody(source, fieldNode);
		} else {
			statements = createSimpleGetterBody(source, fieldNode);
		}
		
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = returnType;
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = statements;
		return method;
	}

	private Statement[] createSimpleGetterBody(ASTNode source, EclipseNode fieldNode) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
		Statement returnStatement = new ReturnStatement(fieldRef, field.sourceStart, field.sourceEnd);
		Eclipse.setGeneratedBy(returnStatement, source);
		return new Statement[] {returnStatement};
	}
	
	private static final char[][] AR = fromQualifiedName("java.util.concurrent.atomic.AtomicReference");
	private static final TypeReference[][] AR_PARAMS = new TypeReference[5][];
	
	private static final java.util.Map<String, char[][]> TYPE_MAP;
	static {
		Map<String, char[][]> m = new HashMap<String, char[][]>();
		m.put("int", fromQualifiedName("java.lang.Integer"));
		m.put("double", fromQualifiedName("java.lang.Double"));
		m.put("float", fromQualifiedName("java.lang.Float"));
		m.put("short", fromQualifiedName("java.lang.Short"));
		m.put("byte", fromQualifiedName("java.lang.Byte"));
		m.put("long", fromQualifiedName("java.lang.Long"));
		m.put("boolean", fromQualifiedName("java.lang.Boolean"));
		m.put("char", fromQualifiedName("java.lang.Character"));
		TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	private static char[] valueName = "value".toCharArray();
	
	private Statement[] createLazyGetterBody(ASTNode source, EclipseNode fieldNode) {
		/*
		java.util.concurrent.atomic.AtomicReference<ValueType> value = this.fieldName.get();
		if (value == null) {
			synchronized (this.fieldName) {
				value = this.fieldName.get();
				if (value == null) { 
					value = new java.util.concurrent.atomic.AtomicReference<ValueType>(new ValueType());
					this.fieldName.set(value);
				}
			}
		}
		return value.get();
		*/
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		TypeReference componentType = copyType(field.type, source);
		if (field.type instanceof SingleTypeReference && !(field.type instanceof ArrayTypeReference)) {
			char[][] newType = TYPE_MAP.get(new String(((SingleTypeReference)field.type).token));
			if (newType != null) {
				componentType = new QualifiedTypeReference(newType, poss(source, 3));
				Eclipse.setGeneratedBy(componentType, source);
			}
		}
		
		Statement[] statements = new Statement[3];
		
		/* java.util.concurrent.atomic.AtomicReference<ValueType> value = this.fieldName.get(); */ {
			LocalDeclaration valueDecl = new LocalDeclaration(valueName, pS, pE);
			Eclipse.setGeneratedBy(valueDecl, source);
			TypeReference[][] typeParams = AR_PARAMS.clone();
			typeParams[4] = new TypeReference[] {copyType(componentType, source)};
			valueDecl.type = new ParameterizedQualifiedTypeReference(AR, typeParams, 0, poss(source, 5));
			valueDecl.type.sourceStart = pS; valueDecl.type.sourceEnd = pE;
			Eclipse.setGeneratedBy(valueDecl.type, source);
			
			MessageSend getter = new MessageSend();
			Eclipse.setGeneratedBy(getter, source);
			getter.sourceStart = pS; getter.sourceEnd = pE;
			getter.selector = new char[] {'g', 'e', 't'};
			getter.receiver = EclipseHandlerUtil.createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
			
			valueDecl.initialization = getter;
			Eclipse.setGeneratedBy(valueDecl.initialization, source);
			statements[0] = valueDecl;
		}
		
		/*
		if (value == null) {
			synchronized (this.fieldName) {
				value = this.fieldName.get();
				if (value == null) { 
					value = new java.util.concurrent.atomic.AtomicReference<ValueType>(new ValueType());
					this.fieldName.set(value);
				}
			}
		}
		 */ {
			EqualExpression cond = new EqualExpression(
					new SingleNameReference(valueName, p), new NullLiteral(pS, pE),
					BinaryExpression.EQUAL_EQUAL);
			Eclipse.setGeneratedBy(cond.left, source);
			Eclipse.setGeneratedBy(cond.right, source);
			Eclipse.setGeneratedBy(cond, source);
			Block then = new Block(0);
			Eclipse.setGeneratedBy(then, source);
			Expression lock = EclipseHandlerUtil.createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
			Block inner = new Block(0);
			Eclipse.setGeneratedBy(inner, source);
			inner.statements = new Statement[2];
			/* value = this.fieldName.get(); */ {
				MessageSend getter = new MessageSend();
				Eclipse.setGeneratedBy(getter, source);
				getter.sourceStart = pS; getter.sourceEnd = pE;
				getter.selector = new char[] {'g', 'e', 't'};
				getter.receiver = EclipseHandlerUtil.createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
				Assignment assign = new Assignment(new SingleNameReference(valueName, p), getter, pE);
				Eclipse.setGeneratedBy(assign, source);
				Eclipse.setGeneratedBy(assign.lhs, source);
				inner.statements[0] = assign;
			}
			/* if (value == null) */ {
				EqualExpression innerCond = new EqualExpression(
						new SingleNameReference(valueName, p), new NullLiteral(pS, pE),
						BinaryExpression.EQUAL_EQUAL);
				Eclipse.setGeneratedBy(innerCond.left, source);
				Eclipse.setGeneratedBy(innerCond.right, source);
				Eclipse.setGeneratedBy(innerCond, source);
				Block innerThen = new Block(0);
				Eclipse.setGeneratedBy(innerThen, source);
				innerThen.statements = new Statement[2];
				/*value = new java.util.concurrent.atomic.AtomicReference<ValueType>(new ValueType()); */ {
					AllocationExpression create = new AllocationExpression();
					Eclipse.setGeneratedBy(create, source);
					create.sourceStart = pS; create.sourceEnd = pE;
					TypeReference[][] typeParams = AR_PARAMS.clone();
					typeParams[4] = new TypeReference[] {copyType(componentType, source)};
					create.type = new ParameterizedQualifiedTypeReference(AR, typeParams, 0, poss(source, 5));
					create.type.sourceStart = pS; create.type.sourceEnd = pE;
					Eclipse.setGeneratedBy(create.type, source);
					create.arguments = new Expression[] {field.initialization};
					Assignment innerAssign = new Assignment(new SingleNameReference(valueName, p), create, pE);
					Eclipse.setGeneratedBy(innerAssign, source);
					Eclipse.setGeneratedBy(innerAssign.lhs, source);
					innerThen.statements[0] = innerAssign;
				}
				
				/*this.fieldName.set(value);*/ {
					MessageSend setter = new MessageSend();
					Eclipse.setGeneratedBy(setter, source);
					setter.sourceStart = pS; setter.sourceEnd = pE;
					setter.receiver = EclipseHandlerUtil.createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
					setter.selector = new char[] { 's', 'e', 't' };
					setter.arguments = new Expression[] {
							new SingleNameReference(valueName, p)};
					Eclipse.setGeneratedBy(setter.arguments[0], source);
					innerThen.statements[1] = setter;
				}
				
				IfStatement innerIf = new IfStatement(innerCond, innerThen, pS, pE);
				Eclipse.setGeneratedBy(innerIf, source);
				inner.statements[1] = innerIf;
			}
			
			SynchronizedStatement sync = new SynchronizedStatement(lock, inner, pS, pE);
			Eclipse.setGeneratedBy(sync, source);
			then.statements = new Statement[] {sync};
			
			IfStatement ifStatement = new IfStatement(cond, then, pS, pE);
			Eclipse.setGeneratedBy(ifStatement, source);
			statements[1] = ifStatement;
		}
		
		/* return value.get(); */ {
			MessageSend getter = new MessageSend();
			Eclipse.setGeneratedBy(getter, source);
			getter.sourceStart = pS; getter.sourceEnd = pE;
			getter.selector = new char[] {'g', 'e', 't'};
			getter.receiver = new SingleNameReference(valueName, p);
			Eclipse.setGeneratedBy(getter.receiver, source);
			
			statements[2] = new ReturnStatement(getter, pS, pE);
			Eclipse.setGeneratedBy(statements[2], source);
		}
		
		
		// update the field type and init last
			
		/* 	private final java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType> fieldName = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType>>(); */ {
			
			LocalDeclaration first = (LocalDeclaration) statements[0];
			TypeReference innerType = copyType(first.type, source);
			
			TypeReference[][] typeParams = AR_PARAMS.clone();
			typeParams[4] = new TypeReference[] {copyType(innerType, source)};
			TypeReference type = new ParameterizedQualifiedTypeReference(AR, typeParams, 0, poss(source, 5));
			// Some magic here
			type.sourceStart = -1; type.sourceEnd = -2;
			Eclipse.setGeneratedBy(type, source);
			
			field.type = type;
			AllocationExpression init = new AllocationExpression();
			// Some magic here
			init.sourceStart = field.initialization.sourceStart; init.sourceEnd = field.initialization.sourceEnd;
			init.type = copyType(type, source);
			field.initialization = init;
		}
		return statements;
	}
}
