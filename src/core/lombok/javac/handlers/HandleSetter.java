/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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
import static lombok.javac.Javac.*;

import java.util.Collection;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
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
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
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
			if (typeNode != null) for (JavacNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(Setter.class, child)) {
						//The annotation will make it happen, so we can skip it.
						return;
					}
				}
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
			
			generateSetterForField(field, errorNode.get(), level);
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
	public void generateSetterForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level) {
		for (JavacNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (annotationTypeMatches(Setter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createSetterForField(level, fieldNode, fieldNode, false);
	}
	
	@Override public void handle(AnnotationValues<Setter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, Setter.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		
		if (level == AccessLevel.NONE || node == null) return;
		
		switch (node.getKind()) {
		case FIELD:
			createSetterForFields(level, fields, annotationNode, true);
			break;
		case TYPE:
			generateSetterForType(node, annotationNode, level, false);
			break;
		}
	}
	
	private void createSetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists) {
		
		for (JavacNode fieldNode : fieldNodes) {
			createSetterForField(level, fieldNode, errorNode, whineIfExists);
		}
	}
	
	private void createSetterForField(AccessLevel level,
			JavacNode fieldNode, JavacNode source, boolean whineIfExists) {
		
		if (fieldNode.getKind() != Kind.FIELD) {
			fieldNode.addError("@Setter is only supported on a class or a field.");
			return;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		String methodName = toSetterName(fieldNode);
		
		if (methodName == null) {
			source.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
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
		
		JCMethodDecl createdSetter = createSetter(access, fieldNode, fieldNode.getTreeMaker(), source.get());
		injectMethod(fieldNode.up(), createdSetter);
	}
	
	private JCMethodDecl createSetter(long access, JavacNode field, TreeMaker treeMaker, JCTree source) {
		String setterName = toSetterName(field);
		boolean returnThis = shouldReturnThis(field);
		if (setterName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
		JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(fieldDecl.name));
		
		ListBuffer<JCStatement> statements = ListBuffer.lb();
		List<JCAnnotation> nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
		List<JCAnnotation> nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
		
		Name methodName = field.toName(setterName);
		List<JCAnnotation> annsOnParam = nonNulls.appendList(nullables);
		
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(Flags.FINAL, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);
		
		if (nonNulls.isEmpty()) {
			statements.append(treeMaker.Exec(assign));
		} else {
			JCStatement nullCheck = generateNullCheck(treeMaker, field);
			if (nullCheck != null) statements.append(nullCheck);
			statements.append(treeMaker.Exec(assign));
		}
		
		JCExpression methodType = null;
		if (returnThis) {
			JavacNode typeNode = field;
			while (typeNode != null && typeNode.getKind() != Kind.TYPE) typeNode = typeNode.up();
			if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
				JCClassDecl type = (JCClassDecl) typeNode.get();
				ListBuffer<JCExpression> typeArgs = ListBuffer.lb();
				if (!type.typarams.isEmpty()) {
					for (JCTypeParameter tp : type.typarams) {
						typeArgs.append(treeMaker.Ident(tp.name));
					}
					methodType = treeMaker.TypeApply(treeMaker.Ident(type.name), typeArgs.toList());
				} else {
					methodType = treeMaker.Ident(type.name);
				}
			}
		}
		
		if (methodType == null) {
			//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
			methodType = treeMaker.Type(new JCNoType(getCtcInt(TypeTags.class, "VOID")));
			returnThis = false;
		}
		
		if (returnThis) {
			JCReturn returnStatement = treeMaker.Return(treeMaker.Ident(field.toName("this")));
			statements.append(returnStatement);
		}
		
		JCBlock methodBody = treeMaker.Block(0, statements.toList());
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = List.nil();
		if (isFieldDeprecated(field)) {
			annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(chainDots(field, "java", "lang", "Deprecated"), List.<JCExpression>nil()));
		}
		return recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source);
	}
	
	private static class JCNoType extends Type implements NoType {
		public JCNoType(int tag) {
			super(tag, null);
		}
		
		@Override
		public TypeKind getKind() {
			if (tag == getCtcInt(TypeTags.class, "VOID")) return TypeKind.VOID;
			if (tag == getCtcInt(TypeTags.class, "NONE")) return TypeKind.NONE;
			throw new AssertionError("Unexpected tag: " + tag);
		}
		
		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
}
