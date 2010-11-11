/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Handles the {@code lombok.Getter} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter implements JavacAnnotationHandler<Getter> {
	public boolean generateGetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelGetter) {
		if (checkForTypeLevelGetter) {
			if (typeNode != null) for (JavacNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (Javac.annotationTypeMatches(Getter.class, child)) {
						//The annotation will make it happen, so we can skip it.
						return true;
					}
				}
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@Getter is only supported on a class or a field.");
			return false;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (fieldQualifiesForGetterGeneration(field)) generateGetterForField(field, errorNode.get(), level, List.<JCExpression>nil(), false);
		}
		
		return true;
	}
	
	public boolean fieldQualifiesForGetterGeneration(JavacNode field) {
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
	 * @param lazy 
	 */
	public void generateGetterForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level, List<JCExpression> onMethod, boolean lazy) {
		for (JavacNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (Javac.annotationTypeMatches(Getter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createGetterForField(level, fieldNode, fieldNode, false, onMethod, lazy);
	}
	
	@Override public boolean handle(AnnotationValues<Getter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		markAnnotationAsProcessed(annotationNode, Getter.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		Getter annotationInstance = annotation.getInstance();
		AccessLevel level = annotationInstance.value();
		boolean lazy = annotationInstance.lazy();
		if (level == AccessLevel.NONE) {
			if (lazy) {
				annotationNode.addWarning("'lazy' does not work with AccessLevel.NONE.");
			}
			return true;
		}
		
		if (node == null) return false;
		
		List<JCExpression> onMethod = getAndRemoveAnnotationParameter(ast, "onMethod");
		if (node.getKind() == Kind.FIELD) {
			return createGetterForFields(level, fields, annotationNode, true, onMethod, lazy);
		}
		if (node.getKind() == Kind.TYPE) {
			if (!onMethod.isEmpty()) annotationNode.addError("'onMethod' is not supported for @Getter on a type.");
			if (lazy) annotationNode.addError("'lazy' is not supported for @Getter on a type.");
			return generateGetterForType(node, annotationNode, level, false);
		}
		return false;
	}
	
	private boolean createGetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, List<JCExpression> onMethod, boolean lazy) {
		for (JavacNode fieldNode : fieldNodes) {
			createGetterForField(level, fieldNode, errorNode, whineIfExists, onMethod, lazy);
		}
		
		return true;
	}
	
	private boolean createGetterForField(AccessLevel level,
			JavacNode fieldNode, JavacNode errorNode, boolean whineIfExists, List<JCExpression> onMethod, boolean lazy) {
		if (fieldNode.getKind() != Kind.FIELD) {
			errorNode.addError("@Getter is only supported on a class or a field.");
			return true;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		
		if (lazy) {
			if ((fieldDecl.mods.flags & Flags.PRIVATE) == 0 || (fieldDecl.mods.flags & Flags.FINAL) == 0) {
				errorNode.addError("'lazy' requires the field to be private and final.");
				return true;
			}
			if (fieldDecl.init == null) {
				errorNode.addError("'lazy' requires field initialization.");
				return true;
			}
		}
		
		String methodName = toGetterName(fieldDecl);
		
		for (String altName : toAllGetterNames(fieldDecl)) {
			switch (methodExists(altName, fieldNode, false)) {
			case EXISTS_BY_LOMBOK:
				return true;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(methodName)) altNameExpl = String.format(" (%s)", altName);
					errorNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", methodName, altNameExpl));
				}
				return true;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		injectMethod(fieldNode.up(), createGetter(access, fieldNode, fieldNode.getTreeMaker(), onMethod, lazy));
		
		return true;
	}
	
	
	private JCMethodDecl createGetter(long access, JavacNode field, TreeMaker treeMaker, List<JCExpression> onMethod, boolean lazy) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		
		// Remember the type; lazy will change it;
		JCExpression methodType = copyType(treeMaker, fieldNode);
		
		List<JCStatement> statements;
		if (lazy) {
			statements = createLazyGetterBody(treeMaker, field);
		} else {
			statements = createSimpleGetterBody(treeMaker, field);
		}
		
		JCBlock methodBody = treeMaker.Block(0, statements);
		Name methodName = field.toName(toGetterName(fieldNode));
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
		List<JCAnnotation> nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
		
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod).appendList(nonNulls).appendList(nullables);
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
	
	private List<JCStatement> createSimpleGetterBody(TreeMaker treeMaker, JavacNode field) {
		return List.<JCStatement>of(treeMaker.Return(createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD)));
	}
	
	private static final String AR = "java.util.concurrent.atomic.AtomicReference";
	private static final List<JCExpression> NIL_EXPRESSION = List.nil();
	
	private static final java.util.Map<Integer, String> TYPE_MAP;
	static {
		Map<Integer, String> m = new HashMap<Integer, String>();
		m.put(TypeTags.INT, "java.lang.Integer");
		m.put(TypeTags.DOUBLE, "java.lang.Double");
		m.put(TypeTags.FLOAT, "java.lang.Float");
		m.put(TypeTags.SHORT, "java.lang.Short");
		m.put(TypeTags.BYTE, "java.lang.Byte");
		m.put(TypeTags.LONG, "java.lang.Long");
		m.put(TypeTags.BOOLEAN, "java.lang.Boolean");
		m.put(TypeTags.CHAR, "java.lang.Character");
		TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	private List<JCStatement> createLazyGetterBody(TreeMaker maker, JavacNode fieldNode) {
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
		
		List<JCStatement> statements = List.nil();
		
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		field.type = null;
		if (field.vartype instanceof JCPrimitiveTypeTree) {
			String boxed = TYPE_MAP.get(((JCPrimitiveTypeTree)field.vartype).typetag);
			if (boxed != null) {
				field.vartype = chainDotsString(maker, fieldNode, boxed);
			}
		}
		
		Name valueName = fieldNode.toName("value");
		
		/* java.util.concurrent.atomic.AtomicReference<ValueType> value = this.fieldName.get();*/ {
			JCTypeApply valueVarType = maker.TypeApply(chainDotsString(maker, fieldNode, AR), List.of(copyType(maker, field)));
			statements = statements.append(maker.VarDef(maker.Modifiers(0), valueName, valueVarType, callGet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD))));
		}
		
		/* if (value == null) { */ {
			JCSynchronized synchronizedStatement;
			/* synchronized (this.fieldName) { */ {
				List<JCStatement> synchronizedStatements = List.nil();
				/* value = this.fieldName.get(); */ {
					JCExpressionStatement newAssign = maker.Exec(maker.Assign(maker.Ident(valueName), callGet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD))));
					synchronizedStatements = synchronizedStatements.append(newAssign);
				}
				
				/* if (value == null) { */ {
					List<JCStatement> innerIfStatements = List.nil();
					/* value = new java.util.concurrent.atomic.AtomicReference<ValueType>(new ValueType());*/ {
						JCTypeApply valueVarType = maker.TypeApply(chainDotsString(maker, fieldNode, AR), List.of(copyType(maker, field)));			
						JCNewClass newInstance = maker.NewClass(null, NIL_EXPRESSION, valueVarType, List.<JCExpression>of(field.init), null);
						
						JCStatement statement = maker.Exec(maker.Assign(maker.Ident(valueName), newInstance));
						innerIfStatements = innerIfStatements.append(statement);
					}
					/* this.fieldName.set(value); */ {
						JCStatement statement = callSet(fieldNode, createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD), maker.Ident(valueName));
						innerIfStatements = innerIfStatements.append(statement);
					}
					
					JCBinary isNull = maker.Binary(JCTree.EQ, maker.Ident(valueName), maker.Literal(TypeTags.BOT, null));
					JCIf ifStatement = maker.If(isNull, maker.Block(0, innerIfStatements), null);
					synchronizedStatements = synchronizedStatements.append(ifStatement);
				}
				
				synchronizedStatement = maker.Synchronized(createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD), maker.Block(0, synchronizedStatements));
			}
			
			JCBinary isNull = maker.Binary(JCTree.EQ, maker.Ident(valueName), maker.Literal(TypeTags.BOT, null));
			JCIf ifStatement = maker.If(isNull, maker.Block(0, List.<JCStatement>of(synchronizedStatement)), null);
			statements = statements.append(ifStatement);
		}
		/* return value.get(); */
		statements = statements.append(maker.Return(callGet(fieldNode, maker.Ident(valueName))));
		
		// update the field type and init last
		
		/* 	private final java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType> fieldName = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType>>(); */ {
			field.vartype = maker.TypeApply(chainDotsString(maker, fieldNode, AR), List.<JCExpression>of(maker.TypeApply(chainDotsString(maker, fieldNode, AR), List.of(copyType(maker, field)))));
			field.init = maker.NewClass(null, NIL_EXPRESSION, copyType(maker, field), NIL_EXPRESSION, null);
		}
		
		return statements;
	}
	
	private JCMethodInvocation callGet(JavacNode source, JCExpression receiver) {
		TreeMaker maker = source.getTreeMaker();
		return maker.Apply(NIL_EXPRESSION, maker.Select(receiver, source.toName("get")), NIL_EXPRESSION);
	}
	
	private JCStatement callSet(JavacNode source, JCExpression receiver, JCExpression value) {
		TreeMaker maker = source.getTreeMaker();
		return maker.Exec(maker.Apply(NIL_EXPRESSION, maker.Select(receiver, source.toName("set")), List.<JCExpression>of(value)));
	}
	
	private JCExpression copyType(TreeMaker treeMaker, JCVariableDecl fieldNode) {
		return fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
	}
}
