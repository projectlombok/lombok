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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.spi.Provides;
import lombok.Getter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.agent.PatchDelegate;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
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
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

/**
 * Handles the {@code lombok.Getter} annotation for eclipse.
 */
@Provides
public class HandleGetter extends EclipseAnnotationHandler<Getter> {
	private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];
	private static final String GETTER_NODE_NOT_SUPPORTED_ERR = "@Getter is only supported on a class, an enum, or a field.";

	public boolean generateGetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelGetter, List<Annotation> onMethod) {
		if (checkForTypeLevelGetter) {
			if (hasAnnotation(Getter.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return true;
			}
		}
		
		if (!isClassOrEnum(typeNode)) {
			pos.addError(GETTER_NODE_NOT_SUPPORTED_ERR);
			return false;
		}
		
		for (EclipseNode field : typeNode.down()) {
			if (fieldQualifiesForGetterGeneration(field)) generateGetterForField(field, pos.get(), level, false, onMethod);
		}
		return true;
	}
	
	public static boolean fieldQualifiesForGetterGeneration(EclipseNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
		return filterField(fieldDecl);
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
	public void generateGetterForField(EclipseNode fieldNode, ASTNode pos, AccessLevel level, boolean lazy, List<Annotation> onMethod) {
		if (hasAnnotation(Getter.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		createGetterForField(level, fieldNode, fieldNode, pos, false, lazy, onMethod);
	}
	
	public void handle(AnnotationValues<Getter> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.GETTER_FLAG_USAGE, "@Getter");
		
		EclipseNode node = annotationNode.up();
		Getter annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.value();
		boolean lazy = annotationInstance.lazy();
		if (lazy) handleFlagUsage(annotationNode, ConfigurationKeys.GETTER_LAZY_FLAG_USAGE, "@Getter(lazy=true)");
		
		if (level == AccessLevel.NONE) {
			if (lazy) annotationNode.addWarning("'lazy' does not work with AccessLevel.NONE.");
			return;
		}
		
		if (node == null) return;
		
		List<Annotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Getter(onMethod", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createGetterForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, annotationNode.get(), true, lazy, onMethod);
			break;
		case TYPE:
			if (lazy) annotationNode.addError("'lazy' is not supported for @Getter on a type.");
			generateGetterForType(node, annotationNode, level, false, onMethod);
			break;
		}
	}
	
	public void createGetterForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode errorNode, ASTNode source, boolean whineIfExists, boolean lazy, List<Annotation> onMethod) {
		for (EclipseNode fieldNode : fieldNodes) {
			createGetterForField(level, fieldNode, errorNode, source, whineIfExists, lazy, onMethod);
		}
	}
	
	public void createGetterForField(AccessLevel level,
			EclipseNode fieldNode, EclipseNode errorNode, ASTNode source, boolean whineIfExists, boolean lazy, List<Annotation> onMethod) {
		
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError(GETTER_NODE_NOT_SUPPORTED_ERR);
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		if (lazy) {
			if ((field.modifiers & ClassFileConstants.AccPrivate) == 0 || (field.modifiers & ClassFileConstants.AccFinal) == 0) {
				errorNode.addError("'lazy' requires the field to be private and final.");
				return;
			}
			if ((field.modifiers & ClassFileConstants.AccTransient) != 0) {
				errorNode.addError("'lazy' is not supported on transient fields.");
				return;
			}
			if (field.initialization == null) {
				errorNode.addError("'lazy' requires field initialization.");
				return;
			}
		}
		
		TypeReference fieldType = copyType(field.type, source);
		boolean isBoolean = isBoolean(fieldType);
		AnnotationValues<Accessors> accessors = getAccessorsForField(fieldNode);
		String getterName = toGetterName(fieldNode, isBoolean, accessors);
		
		if (getterName == null) {
			errorNode.addWarning("Not generating getter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);
		
		for (String altName : toAllGetterNames(fieldNode, isBoolean, accessors)) {
			switch (methodExists(altName, fieldNode, false, 0)) {
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
		
		MethodDeclaration method = createGetter((TypeDeclaration) fieldNode.up().get(), fieldNode, getterName, modifier, source, lazy, onMethod);
		
		injectMethod(fieldNode.up(), method);
	}
	
	public static Annotation[] findDelegatesAndMarkAsHandled(EclipseNode fieldNode) {
		List<Annotation> delegates = new ArrayList<Annotation>();
		for (EclipseNode child : fieldNode.down()) {
			if (annotationTypeMatches(Delegate.class, child)) {
				Annotation delegate = (Annotation)child.get();
				PatchDelegate.markHandled(delegate);
				delegates.add(delegate);
			}
		}
		return delegates.toArray(EMPTY_ANNOTATIONS_ARRAY);
	}
	
	public MethodDeclaration createGetter(TypeDeclaration parent, EclipseNode fieldNode, String name, int modifier, ASTNode source, boolean lazy, List<Annotation> onMethod) {
		// Remember the type; lazy will change it;
		TypeReference returnType = copyType(((FieldDeclaration) fieldNode.get()).type, source);
		
		Statement[] statements;
		boolean addSuppressWarningsUnchecked = false;
		if (lazy) {
			statements = createLazyGetterBody(source, fieldNode);
			addSuppressWarningsUnchecked = true;
		} else {
			statements = createSimpleGetterBody(source, fieldNode);
		}
		
		AnnotationValues<Accessors> accessors = getAccessorsForField(fieldNode);
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		if (shouldMakeFinal(fieldNode, accessors)) modifier |= ClassFileConstants.AccFinal;
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
		
		EclipseHandlerUtil.registerCreatedLazyGetter((FieldDeclaration) fieldNode.get(), method.selector, returnType);
		
		/* Generate annotations that must be put on the generated method, and attach them. */ {
			Annotation[] deprecated = null, checkerFramework = null;
			if (isFieldDeprecated(fieldNode)) deprecated = new Annotation[] { generateDeprecatedAnnotation(source) };
			if (fieldNode.isFinal()) {
				if (getCheckerFrameworkVersion(fieldNode).generatePure()) checkerFramework = new Annotation[] { generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__PURE) };
			} else {
				if (getCheckerFrameworkVersion(fieldNode).generateSideEffectFree()) checkerFramework = new Annotation[] { generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE) };
			}
			
			method.annotations = copyAnnotations(source,
				onMethod.toArray(new Annotation[0]),
				findCopyableAnnotations(fieldNode),
				findDelegatesAndMarkAsHandled(fieldNode),
				checkerFramework,
				deprecated);
		}
		
		if (addSuppressWarningsUnchecked) {
			List<Expression> suppressions = new ArrayList<Expression>(2);
			if (!Boolean.FALSE.equals(fieldNode.getAst().readConfiguration(ConfigurationKeys.ADD_SUPPRESSWARNINGS_ANNOTATIONS))) {
				suppressions.add(new StringLiteral(ALL, 0, 0, 0));
			}
			suppressions.add(new StringLiteral(UNCHECKED, 0, 0, 0));
			ArrayInitializer arr = new ArrayInitializer();
			arr.expressions = suppressions.toArray(new Expression[0]);
			method.annotations = addAnnotation(source, method.annotations, TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, arr);
		}
		
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		copyJavadoc(fieldNode, method, CopyJavadoc.GETTER);
		return method;
	}

	public Statement[] createSimpleGetterBody(ASTNode source, EclipseNode fieldNode) {
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
		Statement returnStatement = new ReturnStatement(fieldRef, field.sourceStart, field.sourceEnd);
		return new Statement[] {returnStatement};
	}
	
	private static final char[][] AR = fromQualifiedName("java.util.concurrent.atomic.AtomicReference");
	
	public static final java.util.Map<String, char[][]> TYPE_MAP;
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
	private static char[] actualValueName = "actualValue".toCharArray();
	
	private static final int PARENTHESIZED = (1 << ASTNode.ParenthesizedSHIFT) & ASTNode.ParenthesizedMASK;
	
	public Statement[] createLazyGetterBody(ASTNode source, EclipseNode fieldNode) {
		/*
		java.lang.Object value = this.fieldName.get();
		if (value == null) {
			synchronized (this.fieldName) {
				value = this.fieldName.get();
				if (value == null) {
					final RawValueType actualValue = INITIALIZER_EXPRESSION;
					[IF PRIMITIVE]
					value = actualValue;
					[ELSE]
					value = actualValue == null ? this.fieldName : actualValue;
					[END IF]
					this.fieldName.set(value);
				}
			}
		}
		[IF PRIMITIVE]
		return (BoxedValueType) value;
		[ELSE]
		return (BoxedValueType) (value == this.fieldName ? null : value);
		[END IF]
		*/
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		TypeReference rawComponentType = copyType(field.type, source);
		TypeReference boxedComponentType = null;
		boolean isPrimitive = false;
		if (field.type instanceof SingleTypeReference && !(field.type instanceof ArrayTypeReference)) {
			char[][] newType = TYPE_MAP.get(new String(((SingleTypeReference)field.type).token));
			if (newType != null) {
				boxedComponentType = new QualifiedTypeReference(newType, poss(source, 3));
				isPrimitive = true;
			}
		}
		if (boxedComponentType == null) boxedComponentType = copyType(field.type, source);
		boxedComponentType.sourceStart = pS; boxedComponentType.sourceEnd = boxedComponentType.statementEnd = pE;
		
		Statement[] statements = new Statement[3];
		
		/* java.lang.Object value = this.fieldName.get(); */ {
			LocalDeclaration valueDecl = new LocalDeclaration(valueName, pS, pE);
			valueDecl.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(source, 3));
			valueDecl.type.sourceStart = pS; valueDecl.type.sourceEnd = valueDecl.type.statementEnd = pE;
			
			MessageSend getter = new MessageSend();
			getter.sourceStart = pS; getter.statementEnd = getter.sourceEnd = pE;
			getter.selector = new char[] {'g', 'e', 't'};
			getter.receiver = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
			
			valueDecl.initialization = getter;
			statements[0] = valueDecl;
		}
		
		/*
		if (value == null) {
			synchronized (this.fieldName) {
				value = this.fieldName.get();
				if (value == null) { 
					final ValueType actualValue = INITIALIZER_EXPRESSION;
					[IF PRIMITIVE]
					value = actualValue;
					[ELSE]
					value = actualValue == null ? this.fieldName : actualValue;
					[END IF]
					this.fieldName.set(value);
				}
			}
		}
		 */ {
			EqualExpression cond = new EqualExpression(
					new SingleNameReference(valueName, p), new NullLiteral(pS, pE),
					BinaryExpression.EQUAL_EQUAL);
			Block then = new Block(0);
			Expression lock = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
			Block inner = new Block(0);
			inner.statements = new Statement[2];
			/* value = this.fieldName.get(); */ {
				MessageSend getter = new MessageSend();
				getter.sourceStart = pS; getter.sourceEnd = getter.statementEnd = pE;
				getter.selector = new char[] {'g', 'e', 't'};
				getter.receiver = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
				Assignment assign = new Assignment(new SingleNameReference(valueName, p), getter, pE);
				assign.sourceStart = pS; assign.statementEnd = assign.sourceEnd = pE;
				inner.statements[0] = assign;
			}
			/* if (value == null) */ {
				EqualExpression innerCond = new EqualExpression(
						new SingleNameReference(valueName, p), new NullLiteral(pS, pE),
						BinaryExpression.EQUAL_EQUAL);
				innerCond.sourceStart = pS; innerCond.sourceEnd = innerCond.statementEnd = pE;
				Block innerThen = new Block(0);
				innerThen.statements = new Statement[3];
				/* final ValueType actualValue = INITIALIZER_EXPRESSION */ {
					LocalDeclaration actualValueDecl = new LocalDeclaration(actualValueName, pS, pE);
					actualValueDecl.type = rawComponentType;
					actualValueDecl.type.sourceStart = pS; actualValueDecl.type.sourceEnd = actualValueDecl.type.statementEnd = pE;
					actualValueDecl.initialization = field.initialization;
					actualValueDecl.modifiers = ClassFileConstants.AccFinal;
					innerThen.statements[0] = actualValueDecl;
				}
				/* [IF PRIMITIVE] value = actualValue; */ {
					if (isPrimitive) {
						Assignment innerAssign = new Assignment(new SingleNameReference(valueName, p), new SingleNameReference(actualValueName, p), pE);
						innerAssign.sourceStart = pS; innerAssign.statementEnd = innerAssign.sourceEnd = pE;
						innerThen.statements[1] = innerAssign;
					}
				}
				/* [ELSE] value = actualValue == null ? this.fieldName : actualValue; */ {
					if (!isPrimitive) {
						EqualExpression avIsNull = new EqualExpression(
								new SingleNameReference(actualValueName, p), new NullLiteral(pS, pE),
								BinaryExpression.EQUAL_EQUAL);
						avIsNull.sourceStart = pS; avIsNull.sourceEnd = avIsNull.statementEnd = pE;
						Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
						ConditionalExpression ternary = new ConditionalExpression(avIsNull, fieldRef, new SingleNameReference(actualValueName, p));
						ternary.sourceStart = pS; ternary.sourceEnd = ternary.statementEnd = pE;
						Assignment innerAssign = new Assignment(new SingleNameReference(valueName, p), ternary, pE);
						innerAssign.sourceStart = pS; innerAssign.statementEnd = innerAssign.sourceEnd = pE;
						innerThen.statements[1] = innerAssign;
					}
				}
				
				/* this.fieldName.set(value); */ {
					MessageSend setter = new MessageSend();
					setter.sourceStart = pS; setter.sourceEnd = setter.statementEnd =  pE;
					setter.receiver = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
					setter.selector = new char[] { 's', 'e', 't' };
					setter.arguments = new Expression[] {
							new SingleNameReference(valueName, p)};
					innerThen.statements[2] = setter;
				}
				
				IfStatement innerIf = new IfStatement(innerCond, innerThen, pS, pE);
				inner.statements[1] = innerIf;
			}
			
			SynchronizedStatement sync = new SynchronizedStatement(lock, inner, pS, pE);
			then.statements = new Statement[] {sync};
			
			IfStatement ifStatement = new IfStatement(cond, then, pS, pE);
			statements[1] = ifStatement;
		}
		
		/* [IF PRIMITIVE] return (BoxedValueType)value; */ {
			if (isPrimitive) {
				CastExpression cast = makeCastExpression(new SingleNameReference(valueName, p), boxedComponentType, source);
				statements[2] = new ReturnStatement(cast, pS, pE);
			}
		}
		/* [ELSE] return (BoxedValueType)(value == this.fieldName ? null : value); */ {
			if (!isPrimitive) {
				EqualExpression vIsThisFieldName = new EqualExpression(
						new SingleNameReference(valueName, p), createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source),
						BinaryExpression.EQUAL_EQUAL);
				vIsThisFieldName.sourceStart = pS; vIsThisFieldName.sourceEnd = vIsThisFieldName.statementEnd = pE;
				ConditionalExpression ternary = new ConditionalExpression(vIsThisFieldName, new NullLiteral(pS, pE), new SingleNameReference(valueName, p));
				ternary.sourceStart = pS; ternary.sourceEnd = ternary.statementEnd = pE;
				ternary.bits |= PARENTHESIZED;
				CastExpression cast = makeCastExpression(ternary, boxedComponentType, source);
				statements[2] = new ReturnStatement(cast, pS, pE);
			}
		}
		
		// update the field type and init last
			
		/* 	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> fieldName = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>(); */ {
			TypeReference innerType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(source, 3));
			TypeReference[][] typeParams = new TypeReference[5][];
			typeParams[4] = new TypeReference[] {innerType};
			TypeReference type = new ParameterizedQualifiedTypeReference(AR, typeParams, 0, poss(source, 5));
			
			// Some magic here
			type.sourceStart = -1; type.sourceEnd = -2;
			
			field.type = type;
			AllocationExpression init = new AllocationExpression();
			// Some magic here
			init.sourceStart = field.initialization.sourceStart; init.sourceEnd = init.statementEnd = field.initialization.sourceEnd;
			init.type = copyType(type, source);
			field.initialization = init;
		}
		return statements;
	}
}
