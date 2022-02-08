/*
 * Copyright (C) 2020-2022 The Project Lombok Authors.
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
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import javax.lang.model.type.TypeKind;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.experimental.Accessors;
import lombok.experimental.WithBy;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.JavacTreeMaker.TypeTag;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;
import lombok.spi.Provides;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.With} annotation for javac.
 */
@Provides
public class HandleWithBy extends JavacAnnotationHandler<WithBy> {
	public void generateWithByForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelWithBy) {
		if (checkForTypeLevelWithBy) {
			if (hasAnnotation(WithBy.class, typeNode)) {
				//The annotation will make it happen, so we can skip it.
				return;
			}
		}
		
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@WithBy is only supported on a class or a field.");
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
			
			generateWithByForField(field, errorNode.get(), level);
		}
	}
	
	/**
	 * Generates a withBy on the stated field.
	 * 
	 * The difference between this call and the handle method is as follows:
	 * 
	 * If there is a {@code lombok.experimental.WithBy} annotation on the field, it is used and the
	 * same rules apply (e.g. warning if the method already exists, stated access level applies).
	 * If not, the with is still generated if it isn't already there, though there will not
	 * be a warning if its already there. The default access level is used.
	 * 
	 * @param fieldNode The node representing the field you want a with for.
	 * @param pos The node responsible for generating the {@code @WithBy} annotation.
	 */
	public void generateWithByForField(JavacNode fieldNode, DiagnosticPosition pos, AccessLevel level) {
		if (hasAnnotation(WithBy.class, fieldNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		createWithByForField(level, fieldNode, fieldNode, false, List.<JCAnnotation>nil());
	}
	
	@Override public void handle(AnnotationValues<WithBy> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.WITHBY_FLAG_USAGE, "@WithBy");
		
		deleteAnnotationIfNeccessary(annotationNode, WithBy.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode node = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		
		if (level == AccessLevel.NONE || node == null) return;
		
		List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@WithBy(onMethod", annotationNode);
		
		switch (node.getKind()) {
		case FIELD:
			createWithByForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, true, onMethod);
			break;
		case TYPE:
			if (!onMethod.isEmpty()) annotationNode.addError("'onMethod' is not supported for @WithBy on a type.");
			generateWithByForType(node, annotationNode, level, false);
			break;
		}
	}
	
	public void createWithByForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, List<JCAnnotation> onMethod) {
		for (JavacNode fieldNode : fieldNodes) {
			createWithByForField(level, fieldNode, errorNode, whineIfExists, onMethod);
		}
	}
	
	public void createWithByForField(AccessLevel level, JavacNode fieldNode, JavacNode source, boolean strictMode, List<JCAnnotation> onMethod) {
		JavacNode typeNode = fieldNode.up();
		boolean makeAbstract = typeNode != null && typeNode.getKind() == Kind.TYPE && (((JCClassDecl) typeNode.get()).mods.flags & Flags.ABSTRACT) != 0;
		
		if (fieldNode.getKind() != Kind.FIELD) {
			fieldNode.addError("@WithBy is only supported on a class or a field.");
			return;
		}
		
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(fieldNode);
		JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();
		String methodName = toWithByName(fieldNode, accessors);
		
		if (methodName == null) {
			fieldNode.addWarning("Not generating a withXBy method for this field: It does not fit your @Accessors prefix list.");
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.STATIC) != 0) {
			if (strictMode) fieldNode.addWarning("Not generating " + methodName + " for this field: WithBy methods cannot be generated for static fields.");
			return;
		}
		
		if ((fieldDecl.mods.flags & Flags.FINAL) != 0 && fieldDecl.init != null) {
			if (strictMode) fieldNode.addWarning("Not generating " + methodName + " for this field: WithBy methods cannot be generated for final, initialized fields.");
			return;
		}
		
		if (fieldDecl.name.toString().startsWith("$")) {
			if (strictMode) fieldNode.addWarning("Not generating " + methodName + " for this field: WithBy methods cannot be generated for fields starting with $.");
			return;
		}
		
		for (String altName : toAllWithByNames(fieldNode, accessors)) {
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
		
		JCMethodDecl createdWithBy = createWithBy(access, fieldNode, fieldNode.getTreeMaker(), source, onMethod, makeAbstract);
		recursiveSetGeneratedBy(createdWithBy, source);
		injectMethod(typeNode, createdWithBy);
	}
	
	private static final LombokImmutableList<String> NAME_JUF_FUNCTION = LombokImmutableList.of("java", "util", "function", "Function");
	private static final LombokImmutableList<String> NAME_JUF_OP = LombokImmutableList.of("java", "util", "function", "UnaryOperator");
	private static final LombokImmutableList<String> NAME_JUF_DOUBLEOP = LombokImmutableList.of("java", "util", "function", "DoubleUnaryOperator");
	private static final LombokImmutableList<String> NAME_JUF_INTOP = LombokImmutableList.of("java", "util", "function", "IntUnaryOperator");
	private static final LombokImmutableList<String> NAME_JUF_LONGOP = LombokImmutableList.of("java", "util", "function", "LongUnaryOperator");
	
	public JCMethodDecl createWithBy(long access, JavacNode field, JavacTreeMaker maker, JavacNode source, List<JCAnnotation> onMethod, boolean makeAbstract) {
		String withByName = toWithByName(field);
		if (withByName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		Name methodName = field.toName(withByName);
		
		JCExpression returnType = cloneSelfType(field);
		
		JCBlock methodBody = null;
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
		
		LombokImmutableList<String> functionalInterfaceName = null;
		TypeTag requiredCast = null;
		JCExpression parameterizer = null;
		boolean superExtendsStyle = true;
		String applyMethodName = "apply";
		
		if (fieldDecl.vartype instanceof JCPrimitiveTypeTree) {
			TypeKind kind = ((JCPrimitiveTypeTree) fieldDecl.vartype).getPrimitiveTypeKind();
			if (kind == TypeKind.CHAR) {
				requiredCast = Javac.CTC_CHAR;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (kind == TypeKind.SHORT) {
				requiredCast = Javac.CTC_SHORT;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (kind == TypeKind.BYTE) {
				requiredCast = Javac.CTC_BYTE;
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (kind == TypeKind.INT) {
				functionalInterfaceName = NAME_JUF_INTOP;
			} else if (kind == TypeKind.LONG) {
				functionalInterfaceName = NAME_JUF_LONGOP;
			} else if (kind == TypeKind.FLOAT) {
				functionalInterfaceName = NAME_JUF_DOUBLEOP;
				requiredCast = Javac.CTC_FLOAT;
			} else if (kind == TypeKind.DOUBLE) {
				functionalInterfaceName = NAME_JUF_DOUBLEOP;
			} else if (kind == TypeKind.BOOLEAN) {
				functionalInterfaceName = NAME_JUF_OP;
				parameterizer = JavacHandlerUtil.genJavaLangTypeRef(field, "Boolean");
				superExtendsStyle = false;
			}
		}
		if (functionalInterfaceName == null) {
			functionalInterfaceName = NAME_JUF_FUNCTION;
			parameterizer = cloneType(maker, fieldDecl.vartype, source);
		}
		if (functionalInterfaceName == NAME_JUF_INTOP) applyMethodName = "applyAsInt";
		if (functionalInterfaceName == NAME_JUF_LONGOP) applyMethodName = "applyAsLong";
		if (functionalInterfaceName == NAME_JUF_DOUBLEOP) applyMethodName = "applyAsDouble";
		
		JCExpression varType = chainDots(field, functionalInterfaceName);
		if (parameterizer != null && superExtendsStyle) {
			JCExpression parameterizer1 = parameterizer;
			JCExpression parameterizer2 = cloneType(maker, parameterizer, source);
			// TODO: Apply copyable annotations to 'parameterizer' and 'parameterizer2'.
			JCExpression arg1 = maker.Wildcard(maker.TypeBoundKind(BoundKind.SUPER), parameterizer1);
			JCExpression arg2 = maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), parameterizer2);
			varType = maker.TypeApply(varType, List.of(arg1, arg2));
		}
		if (parameterizer != null && !superExtendsStyle) {
			varType = maker.TypeApply(varType, List.of(parameterizer));
		}
		Name paramName = field.toName("transformer");
		
		JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), paramName, varType, null);
		
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
					JCExpression invoke = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(paramName), field.toName(applyMethodName)), List.<JCExpression>of(createFieldAccessor(maker, child, FieldAccess.ALWAYS_FIELD)));
					if (requiredCast != null) invoke = maker.TypeCast(maker.TypeIdent(requiredCast), invoke);
					args.append(invoke);
				} else {
					args.append(createFieldAccessor(maker, child, FieldAccess.ALWAYS_FIELD));
				}
			}
			
			JCNewClass newClass = maker.NewClass(null, List.<JCExpression>nil(), selfType, args.toList(), null);
			JCReturn returnStatement = maker.Return(newClass);
			
			statements.append(returnStatement);
			
			methodBody = maker.Block(0, statements.toList());
		}
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod);
		CheckerFrameworkVersion checkerFramework = getCheckerFrameworkVersion(source);
		if (checkerFramework.generateSideEffectFree()) annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(source, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		
		if (isFieldDeprecated(field)) annsOnMethod = annsOnMethod.prepend(maker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		
		if (makeAbstract) access = access | Flags.ABSTRACT;
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(field);
		boolean makeFinal = shouldMakeFinal(field, accessors);
		if (makeFinal) access |= Flags.FINAL;
		createRelevantNonNullAnnotation(source, param);
		JCMethodDecl decl = recursiveSetGeneratedBy(maker.MethodDef(maker.Modifiers(access, annsOnMethod), methodName, returnType,
			methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source);
		copyJavadoc(field, decl, CopyJavadoc.WITH_BY);
		createRelevantNonNullAnnotation(source, decl);
		return decl;
	}
}
