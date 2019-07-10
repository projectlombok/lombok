/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TypeTag.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.experimental.Delegate;
import lombok.Getter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.JavacTreeMaker.TypeTag;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.Getter} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter extends JavacAnnotationHandler<Getter> {
	public void generateGetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelGetter, List<JCAnnotation> onMethod) {
		if (checkForTypeLevelGetter) {
			if (hasAnnotation(Getter.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@Getter is only supported on a class, an enum, or a field.");
			return;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (fieldQualifiesForGetterGeneration(field)) generateGetterForField(field, errorNode.get(), level, false, onMethod);
		}
	}
	
	public static boolean fieldQualifiesForGetterGeneration(JavacNode field) {
		if (field.getKind() != Kind.FIELD) return false;
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		//Skip fields that start with $
		if (fieldDecl.name.toString().startsWith("$")) return false;
		//Skip static fields.
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) return false;
		return true;
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
	 * 
	 * @param fieldNode The node representing the field you want a getter for.
	 * @param pos The node responsible for generating the getter (the {@code @Data} or {@code @Getter} annotation).
	 */
	public void generateGetterForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level, boolean lazy, List<JCAnnotation> onMethod) {
		if (hasAnnotation(Getter.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		createGetterForField(level, fieldNode, fieldNode, false, lazy, onMethod);
	}
	
	@Override public void handle(AnnotationValues<Getter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.GETTER_FLAG_USAGE, "@Getter");
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, Getter.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		Getter annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.value();
		boolean lazy = annotationInstance.lazy();
		if (lazy) handleFlagUsage(annotationNode, ConfigurationKeys.GETTER_LAZY_FLAG_USAGE, "@Getter(lazy=true)");
		
		if (level == AccessLevel.NONE) {
			if (lazy) annotationNode.addWarning("'lazy' does not work with AccessLevel.NONE.");
			return;
		}
		
		if (node == null) return;
		
		List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Getter(onMethod", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createGetterForFields(level, fields, annotationNode, true, lazy, onMethod);
			break;
		case TYPE:
			if (lazy) annotationNode.addError("'lazy' is not supported for @Getter on a type.");
			generateGetterForType(node, annotationNode, level, false, onMethod);
			break;
		}
	}
	
	public void createGetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, boolean lazy, List<JCAnnotation> onMethod) {
		for (JavacNode fieldNode : fieldNodes) {
			createGetterForField(level, fieldNode, errorNode, whineIfExists, lazy, onMethod);
		}
	}
	
	public void createGetterForField(AccessLevel level,
			JavacNode fieldNode, JavacNode source, boolean whineIfExists, boolean lazy, List<JCAnnotation> onMethod) {
		if (fieldNode.getKind() != Kind.FIELD) {
			source.addError("@Getter is only supported on a class or a field.");
			return;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		
		if (lazy) {
			if ((fieldDecl.mods.flags & Flags.PRIVATE) == 0 || (fieldDecl.mods.flags & Flags.FINAL) == 0) {
				source.addError("'lazy' requires the field to be private and final.");
				return;
			}
			if ((fieldDecl.mods.flags & Flags.TRANSIENT) != 0) {
				source.addError("'lazy' is not supported on transient fields.");
				return;
			}
			if (fieldDecl.init == null) {
				source.addError("'lazy' requires field initialization.");
				return;
			}
		}
		
		String methodName = toGetterName(fieldNode);
		
		if (methodName == null) {
			source.addWarning("Not generating getter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		for (String altName : toAllGetterNames(fieldNode)) {
			switch (methodExists(altName, fieldNode, false, 0)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(methodName)) altNameExpl = String.format(" (%s)", altName);
					source.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", methodName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		injectMethod(fieldNode.up(), createGetter(access, fieldNode, fieldNode.getTreeMaker(), source.get(), lazy, onMethod), List.<Type>nil(), getMirrorForFieldType(fieldNode));
	}
	
	public JCMethodDecl createGetter(long access, JavacNode field, JavacTreeMaker treeMaker, JCTree source, boolean lazy, List<JCAnnotation> onMethod) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		
		// Remember the type; lazy will change it
		JCExpression methodType = copyType(treeMaker, fieldNode);
		// Generate the methodName; lazy will change the field type
		Name methodName = field.toName(toGetterName(field));
		
		List<JCStatement> statements;
		JCTree toClearOfMarkers = null;
		if (lazy && !inNetbeansEditor(field)) {
			toClearOfMarkers = fieldNode.init;
			statements = createLazyGetterBody(treeMaker, field, source);
		} else {
			statements = createSimpleGetterBody(treeMaker, field);
		}
		
		JCBlock methodBody = treeMaker.Block(0, statements);
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> copyableAnnotations = findCopyableAnnotations(field);
		List<JCAnnotation> delegates = findDelegatesAndRemoveFromField(field);
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod).appendList(copyableAnnotations);
		if (isFieldDeprecated(field)) {
			annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		}
		
		JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source, field.getContext());
		
		if (toClearOfMarkers != null) recursiveSetGeneratedBy(toClearOfMarkers, null, null);
		decl.mods.annotations = decl.mods.annotations.appendList(delegates);
		
		copyJavadoc(field, decl, CopyJavadoc.GETTER);
		return decl;
	}
	
	public static List<JCAnnotation> findDelegatesAndRemoveFromField(JavacNode field) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		
		List<JCAnnotation> delegates = List.nil();
		for (JCAnnotation annotation : fieldNode.mods.annotations) {
			if (typeMatches(Delegate.class, field, annotation.annotationType)) {
				delegates = delegates.append(annotation);
			}
		}
		
		if (!delegates.isEmpty()) {
			ListBuffer<JCAnnotation> withoutDelegates = new ListBuffer<JCAnnotation>();
			for (JCAnnotation annotation : fieldNode.mods.annotations) {
				if (!delegates.contains(annotation)) {
					withoutDelegates.append(annotation);
				}
			}
			fieldNode.mods.annotations = withoutDelegates.toList();
			field.rebuild();
		}
		return delegates;
	}
	
	public List<JCStatement> createSimpleGetterBody(JavacTreeMaker treeMaker, JavacNode field) {
		return List.<JCStatement>of(treeMaker.Return(createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD)));
	}
	
	private static final String AR = "java.util.concurrent.atomic.AtomicReference";
	private static final List<JCExpression> NIL_EXPRESSION = List.nil();
	
	public static final java.util.Map<TypeTag, String> TYPE_MAP;
	static {
		Map<TypeTag, String> m = new HashMap<TypeTag, String>();
		m.put(CTC_INT, "Integer");
		m.put(CTC_DOUBLE, "Double");
		m.put(CTC_FLOAT, "Float");
		m.put(CTC_SHORT, "Short");
		m.put(CTC_BYTE, "Byte");
		m.put(CTC_LONG, "Long");
		m.put(CTC_BOOLEAN, "Boolean");
		m.put(CTC_CHAR, "Character");
		TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	public List<JCStatement> createLazyGetterBody(JavacTreeMaker maker, JavacNode fieldNode, JCTree source) {
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
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		JCExpression copyOfRawFieldType = copyType(maker, field);
		JCExpression copyOfBoxedFieldType = null;
		field.type = null;
		boolean isPrimitive = false;
		if (field.vartype instanceof JCPrimitiveTypeTree) {
			String boxed = TYPE_MAP.get(typeTag(field.vartype));
			if (boxed != null) {
				isPrimitive = true;
				field.vartype = genJavaLangTypeRef(fieldNode, boxed);
				copyOfBoxedFieldType = genJavaLangTypeRef(fieldNode, boxed);
			}
		}
		if (copyOfBoxedFieldType == null) copyOfBoxedFieldType = copyType(maker, field);
		
		Name valueName = fieldNode.toName("value");
		Name actualValueName = fieldNode.toName("actualValue");
		
		/* java.lang.Object value = this.fieldName.get();*/ {
			JCExpression valueVarType = genJavaLangTypeRef(fieldNode, "Object");
			statements.append(maker.VarDef(maker.Modifiers(0), valueName, valueVarType, callGet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD))));
		}
		
		/* if (value == null) { */ {
			JCSynchronized synchronizedStatement;
			/* synchronized (this.fieldName) { */ {
				ListBuffer<JCStatement> synchronizedStatements = new ListBuffer<JCStatement>();
				/* value = this.fieldName.get(); */ {
					JCExpressionStatement newAssign = maker.Exec(maker.Assign(maker.Ident(valueName), callGet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD))));
					synchronizedStatements.append(newAssign);
				}
				
				/* if (value == null) { */ {
					ListBuffer<JCStatement> innerIfStatements = new ListBuffer<JCStatement>();
					/* final RawValueType actualValue = INITIALIZER_EXPRESSION; */ {
						innerIfStatements.append(maker.VarDef(maker.Modifiers(Flags.FINAL), actualValueName, copyOfRawFieldType, field.init));
					}
					/* [IF primitive] value = actualValue; */ {
						if (isPrimitive) {
							JCStatement statement = maker.Exec(maker.Assign(maker.Ident(valueName), maker.Ident(actualValueName)));
							innerIfStatements.append(statement);
						}
					}
					/* [ELSE] value = actualValue == null ? this.fieldName : actualValue; */ {
						if (!isPrimitive) {
							JCExpression actualValueIsNull = maker.Binary(CTC_EQUAL, maker.Ident(actualValueName), maker.Literal(CTC_BOT, null));
							JCExpression thisDotFieldName = createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD);
							JCExpression ternary = maker.Conditional(actualValueIsNull, thisDotFieldName, maker.Ident(actualValueName));
							JCStatement statement = maker.Exec(maker.Assign(maker.Ident(valueName), ternary));
							innerIfStatements.append(statement);
						}
					}
					/* this.fieldName.set(value); */ {
						JCStatement statement = callSet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD), maker.Ident(valueName));
						innerIfStatements.append(statement);
					}
					
					JCBinary isNull = maker.Binary(CTC_EQUAL, maker.Ident(valueName), maker.Literal(CTC_BOT, null));
					JCIf ifStatement = maker.If(isNull, maker.Block(0, innerIfStatements.toList()), null);
					synchronizedStatements.append(ifStatement);
				}
				
				synchronizedStatement = maker.Synchronized(createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD), maker.Block(0, synchronizedStatements.toList()));
			}
			
			JCBinary isNull = maker.Binary(CTC_EQUAL, maker.Ident(valueName), maker.Literal(CTC_BOT, null));
			JCIf ifStatement = maker.If(isNull, maker.Block(0, List.<JCStatement>of(synchronizedStatement)), null);
			statements.append(ifStatement);
		}
		/* [IF PRIMITIVE] return (BoxedValueType) value; */ {
			if (isPrimitive) {
				statements.append(maker.Return(maker.TypeCast(copyOfBoxedFieldType, maker.Ident(valueName))));
			}
		}
		/* [ELSE] return (BoxedValueType) (value == this.fieldName ? null : value); */ {
			if (!isPrimitive) {
				JCExpression valueEqualsSelf = maker.Binary(CTC_EQUAL, maker.Ident(valueName), createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD));
				JCExpression ternary = maker.Conditional(valueEqualsSelf, maker.Literal(CTC_BOT,  null), maker.Ident(valueName));
				JCExpression typeCast = maker.TypeCast(copyOfBoxedFieldType, maker.Parens(ternary));
				statements.append(maker.Return(typeCast));
			}
		}
		
		// update the field type and init last
		
		/*	private final java.util.concurrent.atomic.AtomicReference<Object> fieldName = new java.util.concurrent.atomic.AtomicReference<Object>(); */ {
			field.vartype = recursiveSetGeneratedBy(
					maker.TypeApply(chainDotsString(fieldNode, AR), List.<JCExpression>of(genJavaLangTypeRef(fieldNode, "Object"))), source, fieldNode.getContext());
			field.init = recursiveSetGeneratedBy(maker.NewClass(null, NIL_EXPRESSION, copyType(maker, field), NIL_EXPRESSION, null), source, fieldNode.getContext());
		}
		
		return statements.toList();
	}
	
	public JCMethodInvocation callGet(JavacNode source, JCExpression receiver) {
		JavacTreeMaker maker = source.getTreeMaker();
		return maker.Apply(NIL_EXPRESSION, maker.Select(receiver, source.toName("get")), NIL_EXPRESSION);
	}
	
	public JCStatement callSet(JavacNode source, JCExpression receiver, JCExpression value) {
		JavacTreeMaker maker = source.getTreeMaker();
		return maker.Exec(maker.Apply(NIL_EXPRESSION, maker.Select(receiver, source.toName("set")), List.<JCExpression>of(value)));
	}
	
	public JCExpression copyType(JavacTreeMaker treeMaker, JCVariableDecl fieldNode) {
		return fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
	}
}
