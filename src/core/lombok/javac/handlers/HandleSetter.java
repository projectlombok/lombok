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

import static com.sun.tools.javac.code.TypeTags.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Handles the {@code lombok.Setter} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSetter implements JavacAnnotationHandler<Setter> {
	public boolean generateSetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelSetter) {
		if (checkForTypeLevelSetter) {
			if (typeNode != null) for (JavacNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					if (Javac.annotationTypeMatches(Setter.class, child)) {
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
			errorNode.addError("@Setter is only supported on a class or a field.");
			return false;
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
			
			generateSetterForField(field, errorNode.get(), level, List.<JCExpression>nil(), List.<JCExpression>nil());
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
	 * 
	 * @param fieldNode The node representing the field you want a setter for.
	 * @param pos The node responsible for generating the setter (the {@code @Data} or {@code @Setter} annotation).
	 */
	public void generateSetterForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level, List<JCExpression> onMethod, List<JCExpression> onParam) {
		for (JavacNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (Javac.annotationTypeMatches(Setter.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		createSetterForField(level, fieldNode, fieldNode, false, onMethod, onParam);
	}
	
	@Override public boolean handle(AnnotationValues<Setter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		markAnnotationAsProcessed(annotationNode, Setter.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		
		if (level == AccessLevel.NONE) return true;
		if (node == null) return false;
		
		List<JCExpression> onParamList = getAndRemoveAnnotationParameter(ast, "onParam");
		List<JCExpression> onMethodList = getAndRemoveAnnotationParameter(ast, "onMethod");
		
		if (node.getKind() == Kind.FIELD) {
			return createSetterForFields(level, fields, annotationNode, true, onMethodList, onParamList);
		}
		if (node.getKind() == Kind.TYPE) {
			if (!onMethodList.isEmpty()) annotationNode.addError("'onMethod' is not supported for @Setter on a type.");
			if (!onParamList.isEmpty()) annotationNode.addError("'onParam' is not supported for @Setter on a type.");
			
			return generateSetterForType(node, annotationNode, level, false);
		}
		return false;
	}
	
	private boolean createSetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists,
			List<JCExpression> onMethod, List<JCExpression> onParam) {
		
		for (JavacNode fieldNode : fieldNodes) {
			createSetterForField(level, fieldNode, errorNode, whineIfExists, onMethod, onParam);
		}
		
		return true;
	}
	
	private boolean createSetterForField(AccessLevel level,
			JavacNode fieldNode, JavacNode errorNode, boolean whineIfExists, List<JCExpression> onMethod, List<JCExpression> onParam) {
		
		if (fieldNode.getKind() != Kind.FIELD) {
			fieldNode.addError("@Setter is only supported on a class or a field.");
			return true;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		String methodName = toSetterName(fieldDecl);
		
		switch (methodExists(methodName, fieldNode, false)) {
		case EXISTS_BY_LOMBOK:
			return true;
		case EXISTS_BY_USER:
			if (whineIfExists) errorNode.addWarning(
					String.format("Not generating %s(%s %s): A method with that name already exists",
					methodName, fieldDecl.vartype, fieldDecl.name));
			return true;
		default:
		case NOT_EXISTS:
			//continue with creating the setter
		}
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		injectMethod(fieldNode.up(), createSetter(access, fieldNode, fieldNode.getTreeMaker(), onMethod, onParam));
		
		return true;
	}
	
	private JCMethodDecl createSetter(long access, JavacNode field, TreeMaker treeMaker, List<JCExpression> onMethod, List<JCExpression> onParam) {
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		JCExpression fieldRef = createFieldAccessor(treeMaker, field, true);
		JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(fieldDecl.name));
		
		List<JCStatement> statements;
		List<JCAnnotation> nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
		List<JCAnnotation> nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
		
		if (nonNulls.isEmpty()) {
			statements = List.<JCStatement>of(treeMaker.Exec(assign));
		} else {
			JCStatement nullCheck = generateNullCheck(treeMaker, field);
			if (nullCheck != null) statements = List.<JCStatement>of(nullCheck, treeMaker.Exec(assign));
			else statements = List.<JCStatement>of(treeMaker.Exec(assign));
		}
		
		JCBlock methodBody = treeMaker.Block(0, statements);
		Name methodName = field.toName(toSetterName(fieldDecl));
		List<JCAnnotation> annsOnParam = copyAnnotations(onParam);
		annsOnParam = annsOnParam.appendList(nonNulls).appendList(nullables);
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(Flags.FINAL, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);
		//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
		JCExpression methodType = treeMaker.Type(new JCNoType(TypeTags.VOID));
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, copyAnnotations(onMethod)), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
	
	private static class JCNoType extends Type implements NoType {
		public JCNoType(int tag) {
			super(tag, null);
		}
		
		@Override
		public TypeKind getKind() {
			switch (tag) {
			case VOID:  return TypeKind.VOID;
			case NONE:  return TypeKind.NONE;
			default:
				throw new AssertionError("Unexpected tag: " + tag);
			}
		}
		
		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
	
	@Override public boolean isResolutionBased() {
		return false;
	}
}
