/*
 * Copyright (C) 2009-2024 The Project Lombok Authors.
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

import static lombok.javac.Javac.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Setter;
import lombok.core.AST.Kind;
import lombok.experimental.Accessors;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.Setter} annotation for javac.
 */
@Provides
public class HandleSetter extends JavacAnnotationHandler<Setter> {
	private static final String SETTER_NODE_NOT_SUPPORTED_ERR = "@Setter is only supported on a class or a field.";
	
	public void generateSetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelSetter, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		if (checkForTypeLevelSetter) {
			if (hasAnnotation(Setter.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return;
			}
		}
		
		if (!isClass(typeNode)) {
			errorNode.addError(SETTER_NODE_NOT_SUPPORTED_ERR);
			return;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			//Skip static fields.
			if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
			//Skip final fields.
			if ((fieldDecl.mods.flags & Flags.FINAL) != 0) continue;
			
			generateSetterForField(field, errorNode, level, onMethod, onParam);
		}
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
	 * 
	 * @param fieldNode The node representing the field you want a setter for.
	 * @param pos The node responsible for generating the setter (the {@code @Data} or {@code @Setter} annotation).
	 */
	public void generateSetterForField(JavacNode fieldNode, JavacNode sourceNode, AccessLevel level, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		if (hasAnnotation(Setter.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		createSetterForField(level, fieldNode, sourceNode, false, onMethod, onParam);
	}
	
	@Override public void handle(AnnotationValues<Setter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.SETTER_FLAG_USAGE, "@Setter");
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, Setter.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		
		if (level == AccessLevel.NONE || node == null) return;
		
		List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Setter(onMethod", annotationNode);
		if (!onMethod.isEmpty()) {
			handleFlagUsage(annotationNode, ConfigurationKeys.ON_X_FLAG_USAGE, "@Setter(onMethod=...)");
		}
		List<JCAnnotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@Setter(onParam", annotationNode);
		if (!onParam.isEmpty()) {
			handleFlagUsage(annotationNode, ConfigurationKeys.ON_X_FLAG_USAGE, "@Setter(onParam=...)");
		}
		
		switch (node.getKind()) {
		case FIELD:
			createSetterForFields(level, fields, annotationNode, true, onMethod, onParam);
			break;
		case TYPE:
			generateSetterForType(node, annotationNode, level, false, onMethod, onParam);
			break;
		}
	}
	
	public void createSetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		for (JavacNode fieldNode : fieldNodes) {
			createSetterForField(level, fieldNode, errorNode, whineIfExists, onMethod, onParam);
		}
	}
	
	public void createSetterForField(AccessLevel level, JavacNode fieldNode, JavacNode sourceNode, boolean whineIfExists, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		if (fieldNode.getKind() != Kind.FIELD) {
			fieldNode.addError(SETTER_NODE_NOT_SUPPORTED_ERR);
			return;
		}
		
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(fieldNode);
		JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();
		String methodName = toSetterName(fieldNode, accessors);
		
		if (methodName == null) {
			fieldNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.FINAL) != 0) {
			fieldNode.addWarning("Not generating setter for this field: Setters cannot be generated for final fields.");
			return;
		}
		
		for (String altName : toAllSetterNames(fieldNode, accessors)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (whineIfExists) {
					String altNameExpl = "";
					if (!altName.equals(methodName)) altNameExpl = String.format(" (%s)", altName);
					fieldNode.addWarning(
						String.format("Not generating %s(): A method with that name already exists%s", methodName, altNameExpl));
				}
				return;
			default:
			case NOT_EXISTS:
				//continue scanning the other alt names.
			}
		}
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		JCMethodDecl createdSetter = createSetter(access, fieldNode, fieldNode.getTreeMaker(), sourceNode, onMethod, onParam);
		injectMethod(fieldNode.up(), createdSetter);
	}
	
	public static JCMethodDecl createSetter(long access, JavacNode field, JavacTreeMaker treeMaker, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(field);
		String setterName = toSetterName(field, accessors);
		boolean returnThis = shouldReturnThis(field, accessors);
		JCMethodDecl setter = createSetter(access, false, field, treeMaker, setterName, null, null, returnThis, source, onMethod, onParam);
		return setter;
	}
	
	public static JCMethodDecl createSetter(long access, boolean deprecate, JavacNode field, JavacTreeMaker treeMaker, String setterName, Name paramName, Name booleanFieldToSet, boolean shouldReturnThis, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		JCExpression returnType = null;
		JCReturn returnStatement = null;
		if (shouldReturnThis) {
			returnType = cloneSelfType(field);
			returnType = addCheckerFrameworkReturnsReceiver(returnType, treeMaker, field, getCheckerFrameworkVersion(source));
			returnStatement = treeMaker.Return(treeMaker.Ident(field.toName("this")));
		}
		
		return createSetter(access, deprecate, field, treeMaker, setterName, paramName, booleanFieldToSet, returnType, returnStatement, source, onMethod, onParam);
	}
	
	public static JCMethodDecl createSetterWithRecv(long access, boolean deprecate, JavacNode field, JavacTreeMaker treeMaker, String setterName, Name paramName, Name booleanFieldToSet, boolean shouldReturnThis, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, JCVariableDecl recv) {
		JCExpression returnType = null;
		JCReturn returnStatement = null;
		if (shouldReturnThis) {
			returnType = cloneSelfType(field);
			returnType = addCheckerFrameworkReturnsReceiver(returnType, treeMaker, field, getCheckerFrameworkVersion(source));
			returnStatement = treeMaker.Return(treeMaker.Ident(field.toName("this")));
		}
		
		JCMethodDecl d = createSetterWithRecv(access, deprecate, field, treeMaker, setterName, paramName, booleanFieldToSet, returnType, returnStatement, source, onMethod, onParam, recv);
		return d;
	}
	
	public static JCMethodDecl createSetter(long access, boolean deprecate, JavacNode field, JavacTreeMaker treeMaker, String setterName, Name paramName, Name booleanFieldToSet, JCExpression methodType, JCStatement returnStatement, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		return createSetterWithRecv(access, deprecate, field, treeMaker, setterName, paramName, booleanFieldToSet, methodType, returnStatement, source, onMethod, onParam, null);
	}
	
	public static JCMethodDecl createSetterWithRecv(long access, boolean deprecate, JavacNode field, JavacTreeMaker treeMaker, String setterName, Name paramName, Name booleanFieldToSet, JCExpression methodType, JCStatement returnStatement, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, JCVariableDecl recv) {
		if (setterName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		if (paramName == null) paramName = fieldDecl.name;
		
		JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
		JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(paramName));
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		List<JCAnnotation> copyableAnnotations = findCopyableAnnotations(field);
		
		Name methodName = field.toName(setterName);
		List<JCAnnotation> annsOnParam = copyAnnotations(onParam, treeMaker).appendList(copyableAnnotations);
		
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
		JCExpression pType = cloneType(treeMaker, fieldDecl.vartype, source);
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(flags, annsOnParam), paramName, pType, null);
		
		if (!hasNonNullAnnotations(field) && !hasNonNullAnnotations(field, onParam)) {
			statements.append(treeMaker.Exec(assign));
		} else {
			JCStatement nullCheck = generateNullCheck(treeMaker, fieldDecl.vartype, paramName, source, null);
			if (nullCheck != null) statements.append(nullCheck);
			statements.append(treeMaker.Exec(assign));
		}
		
		if (booleanFieldToSet != null) {
			JCAssign setBool = treeMaker.Assign(treeMaker.Ident(booleanFieldToSet), treeMaker.Literal(CTC_BOOLEAN, 1));
			statements.append(treeMaker.Exec(setBool));
		}
		
		if (methodType == null) {
			//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
			methodType = treeMaker.Type(Javac.createVoidType(field.getSymbolTable(), CTC_VOID));
			returnStatement = null;
		}
		
		if (returnStatement != null) statements.append(returnStatement);
		
		JCBlock methodBody = treeMaker.Block(0, statements.toList());
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = mergeAnnotations(copyAnnotations(onMethod, treeMaker), findCopyableToSetterAnnotations(field));
		if (isFieldDeprecated(field) || deprecate) {
			annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		}
		
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(field);
		if (shouldMakeFinal(field, accessors)) access |= Flags.FINAL;
		JCMethodDecl methodDef;
		if (recv != null && treeMaker.hasMethodDefWithRecvParam()) {
			methodDef = treeMaker.MethodDefWithRecvParam(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, recv, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
		} else {
			methodDef = treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
		}
		if (returnStatement != null) createRelevantNonNullAnnotation(source, methodDef);
		JCMethodDecl decl = recursiveSetGeneratedBy(methodDef, source);
		copyJavadoc(field, decl, CopyJavadoc.SETTER, returnStatement != null);
		return decl;
	}
}
