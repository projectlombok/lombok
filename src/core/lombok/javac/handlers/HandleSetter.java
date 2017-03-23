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

import static lombok.javac.Javac.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Setter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
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
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSetter extends JavacAnnotationHandler<Setter> {
	public void generateSetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelSetter) {
		if (checkForTypeLevelSetter) {
			if (hasAnnotation(Setter.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@Setter is only supported on a class or a field.");
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
			
			generateSetterForField(field, errorNode, level);
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
	public void generateSetterForField(JavacNode fieldNode, JavacNode sourceNode, AccessLevel level) {
		if (hasAnnotation(Setter.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		createSetterForField(level, fieldNode, sourceNode, false, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
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
		List<JCAnnotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@Setter(onParam", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createSetterForFields(level, fields, annotationNode, true, onMethod, onParam);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) annotationNode.addError("'onMethod' is not supported for @Setter on a type.");
			if (!onParam.isEmpty()) annotationNode.addError("'onParam' is not supported for @Setter on a type.");
			generateSetterForType(node, annotationNode, level, false);
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
			fieldNode.addError("@Setter is only supported on a class or a field.");
			return;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();
		String methodName = toSetterName(fieldNode);
		
		if (methodName == null) {
			fieldNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.FINAL) != 0) {
			fieldNode.addWarning("Not generating setter for this field: Setters cannot be generated for final fields.");
			return;
		}
		
		for (String altName : toAllSetterNames(fieldNode)) {
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
		Type fieldType = getMirrorForFieldType(fieldNode);
		Type returnType;
		
		if (shouldReturnThis(fieldNode)) {
			ClassSymbol sym = ((JCClassDecl) fieldNode.up().get()).sym;
			returnType = sym == null ? null : sym.type;
		} else {
			returnType = Javac.createVoidType(fieldNode.getSymbolTable(), CTC_VOID);
		}
		
		injectMethod(fieldNode.up(), createdSetter, fieldType == null ? null : List.of(fieldType), returnType);
	}
	
	public static JCMethodDecl createSetter(long access, JavacNode field, JavacTreeMaker treeMaker, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		String setterName = toSetterName(field);
		boolean returnThis = shouldReturnThis(field);
		return createSetter(access, false, field, treeMaker, setterName, null, returnThis, source, onMethod, onParam);
	}
	
	public static JCMethodDecl createSetter(long access, boolean deprecate, JavacNode field, JavacTreeMaker treeMaker, String setterName, Name booleanFieldToSet, boolean shouldReturnThis, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		if (setterName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
		JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(fieldDecl.name));
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		List<JCAnnotation> nonNulls = findAnnotations(field, NON_NULL_PATTERN);
		List<JCAnnotation> nullables = findAnnotations(field, NULLABLE_PATTERN);
		
		Name methodName = field.toName(setterName);
		List<JCAnnotation> annsOnParam = copyAnnotations(onParam).appendList(nonNulls).appendList(nullables);
		
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(flags, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);
		
		if (nonNulls.isEmpty()) {
			statements.append(treeMaker.Exec(assign));
		} else {
			JCStatement nullCheck = generateNullCheck(treeMaker, field, source);
			if (nullCheck != null) statements.append(nullCheck);
			statements.append(treeMaker.Exec(assign));
		}
		
		if (booleanFieldToSet != null) {
			JCAssign setBool = treeMaker.Assign(treeMaker.Ident(booleanFieldToSet), treeMaker.Literal(CTC_BOOLEAN, 1));
			statements.append(treeMaker.Exec(setBool));
		}
		
		JCExpression methodType = null;
		if (shouldReturnThis) {
			methodType = cloneSelfType(field);
		}
		
		if (methodType == null) {
			//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
			methodType = treeMaker.Type(Javac.createVoidType(field.getSymbolTable(), CTC_VOID));
			shouldReturnThis = false;
		}
		
		if (shouldReturnThis) {
			JCReturn returnStatement = treeMaker.Return(treeMaker.Ident(field.toName("this")));
			statements.append(returnStatement);
		}
		
		JCBlock methodBody = treeMaker.Block(0, statements.toList());
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod);
		if (isFieldDeprecated(field) || deprecate) {
			annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		}
		
		JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), field.getContext());
		copyJavadoc(field, decl, CopyJavadoc.SETTER);
		return decl;
	}
}
