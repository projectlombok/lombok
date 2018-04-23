/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.Wither;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.experimental.Wither} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleWither extends JavacAnnotationHandler<Wither> {
	public void generateWitherForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelWither) {
		if (checkForTypeLevelWither) {
			if (hasAnnotation(Wither.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@Wither is only supported on a class or a field.");
			return;
		}
		
		for (JavacNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			//Skip static fields.
			if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
			//Skip final initialized fields.
			if ((fieldDecl.mods.flags & Flags.FINAL) != 0 && fieldDecl.init != null) continue;
			
			generateWitherForField(field, errorNode.get(), level);
		}
	}
	
	/**
	 * Generates a wither on the stated field.
	 * 
	 * Used by {@link HandleValue}.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.experimental.Wither} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the wither is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 * 
	 * @param fieldNode The node representing the field you want a wither for.
	 * @param pos The node responsible for generating the wither (the {@code @Value} or {@code @Wither} annotation).
	 */
	public void generateWitherForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level) {
		if (hasAnnotation(Wither.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		createWitherForField(level, fieldNode, fieldNode, false, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
	}
	
	@Override public void handle(AnnotationValues<Wither> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.WITHER_FLAG_USAGE, "@Wither");
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, Wither.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		
		if (level == AccessLevel.NONE || node == null) return;
		
		List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@Wither(onMethod", annotationNode);
		List<JCAnnotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@Wither(onParam", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createWitherForFields(level, fields, annotationNode, true, onMethod, onParam);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) annotationNode.addError("'onMethod' is not supported for @Wither on a type.");
			if (!onParam.isEmpty()) annotationNode.addError("'onParam' is not supported for @Wither on a type.");
			generateWitherForType(node, annotationNode, level, false);
			break;
		}
	}
	
	public void createWitherForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		for (JavacNode fieldNode : fieldNodes) {
			createWitherForField(level, fieldNode, errorNode, whineIfExists, onMethod, onParam);
		}
	}
	
	public void createWitherForField(AccessLevel level, JavacNode fieldNode, JavacNode source, boolean strictMode, List<JCAnnotation> onMethod, List<JCAnnotation> onParam) {
		JavacNode typeNode = fieldNode.up();
		boolean makeAbstract = typeNode != null && typeNode.getKind() == Kind.TYPE && (((JCClassDecl) typeNode.get()).mods.flags & Flags.ABSTRACT) != 0;
		
		if (fieldNode.getKind() != Kind.FIELD) {
			fieldNode.addError("@Wither is only supported on a class or a field.");
			return;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		String methodName = toWitherName(fieldNode);
		
		if (methodName == null) {
			fieldNode.addWarning("Not generating wither for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) {
			if (strictMode) {
				fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for static fields.");
			}
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.FINAL) != 0 && fieldDecl.init != null) {
			if (strictMode) {
				fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for final, initialized fields.");
			}
			return;
		}
		
		if (fieldDecl.name.toString().startsWith("$")) {
			if (strictMode) {
				fieldNode.addWarning("Not generating wither for this field: Withers cannot be generated for fields starting with $.");
			}
			return;
		}
		
		for (String altName : toAllWitherNames(fieldNode)) {
			switch (methodExists(altName, fieldNode, false, 1)) {
			case EXISTS_BY_LOMBOK:
				return;
			case EXISTS_BY_USER:
				if (strictMode) {
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
		
		long access = toJavacModifier(level);
		
		JCMethodDecl createdWither = createWither(access, fieldNode, fieldNode.getTreeMaker(), source, onMethod, onParam, makeAbstract);
		ClassSymbol sym = ((JCClassDecl) fieldNode.up().get()).sym;
		Type returnType = sym == null ? null : sym.type;
		
		injectMethod(typeNode, createdWither, List.<Type>of(getMirrorForFieldType(fieldNode)), returnType);
	}
	
	public JCMethodDecl createWither(long access, JavacNode field, JavacTreeMaker maker, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, boolean makeAbstract) {
		String witherName = toWitherName(field);
		if (witherName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		List<JCAnnotation> nonNulls = findAnnotations(field, NON_NULL_PATTERN);
		List<JCAnnotation> nullables = findAnnotations(field, NULLABLE_PATTERN);
		
		Name methodName = field.toName(witherName);
		
		JCExpression returnType = cloneSelfType(field);
		
		JCBlock methodBody = null;
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
		List<JCAnnotation> annsOnParam = copyAnnotations(onParam).appendList(nonNulls).appendList(nullables);
		
		JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);
		
		if (!makeAbstract) {
			ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
			
			JCExpression selfType = cloneSelfType(field);
			if (selfType == null) return null;
			
			ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
			for (JavacNode child : field.up().down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl childDecl = (JCVariableDecl) child.get();
				// Skip fields that start with $
				if (childDecl.name.toString().startsWith("$")) continue;
				long fieldFlags = childDecl.mods.flags;
				// Skip static fields.
				if ((fieldFlags & Flags.STATIC) != 0) continue;
				// Skip initialized final fields.
				if (((fieldFlags & Flags.FINAL) != 0) && childDecl.init != null) continue;
				if (child.get() == field.get()) {
					args.append(maker.Ident(fieldDecl.name));
				} else {
					args.append(createFieldAccessor(maker, child, FieldAccess.ALWAYS_FIELD));
				}
			}
			
			JCNewClass newClass = maker.NewClass(null, List.<JCExpression>nil(), selfType, args.toList(), null);
			JCExpression identityCheck = maker.Binary(CTC_EQUAL, createFieldAccessor(maker, field, FieldAccess.ALWAYS_FIELD), maker.Ident(fieldDecl.name));
			JCConditional conditional = maker.Conditional(identityCheck, maker.Ident(field.toName("this")), newClass);
			JCReturn returnStatement = maker.Return(conditional);
			
			if (nonNulls.isEmpty()) {
				statements.append(returnStatement);
			} else {
				JCStatement nullCheck = generateNullCheck(maker, field, source);
				if (nullCheck != null) statements.append(nullCheck);
				statements.append(returnStatement);
			}
			
			methodBody = maker.Block(0, statements.toList());
		}
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod);
		
		if (isFieldDeprecated(field)) {
			annsOnMethod = annsOnMethod.prepend(maker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		}
		if (makeAbstract) access = access | Flags.ABSTRACT;
		JCMethodDecl decl = recursiveSetGeneratedBy(maker.MethodDef(maker.Modifiers(access, annsOnMethod), methodName, returnType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), field.getContext());
		copyJavadoc(field, decl, CopyJavadoc.WITHER);
		return decl;
	}
}
