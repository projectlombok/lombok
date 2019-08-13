/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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
import static lombok.javac.Javac.*;

import java.util.Collection;

import lombok.ConfigurationKeys;
import lombok.ToString;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CallSuperType;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.AST.Kind;
import lombok.core.handlers.InclusionExclusionUtils;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code ToString} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleToString extends JavacAnnotationHandler<ToString> {
	@Override public void handle(AnnotationValues<ToString> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@ToString");
		
		deleteAnnotationIfNeccessary(annotationNode, ToString.class);
		
		ToString ann = annotation.getInstance();
		java.util.List<Included<JavacNode, ToString.Include>> members = InclusionExclusionUtils.handleToStringMarking(annotationNode.up(), annotation, annotationNode);
		if (members == null) return;
		
		Boolean callSuper = ann.callSuper();
		
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		
		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		Boolean fieldNamesConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
		boolean includeNames = annotation.isExplicit("includeFieldNames") || fieldNamesConfiguration == null ? ann.includeFieldNames() : fieldNamesConfiguration;
		
		generateToString(annotationNode.up(), annotationNode, members, includeNames, callSuper, true, fieldAccess);
	}
	
	public void generateToStringForType(JavacNode typeNode, JavacNode errorNode) {
		if (hasAnnotation(ToString.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		boolean includeFieldNames = true;
		try {
			Boolean configuration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
			includeFieldNames = configuration != null ? configuration : ((Boolean) ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}
		
		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;
		
		java.util.List<Included<JavacNode, ToString.Include>> members = InclusionExclusionUtils.handleToStringMarking(typeNode, null, null);
		generateToString(typeNode, errorNode, members, includeFieldNames, null, false, access);
	}
	
	public void generateToString(JavacNode typeNode, JavacNode source, java.util.List<Included<JavacNode, ToString.Include>> members,
		boolean includeFieldNames, Boolean callSuper, boolean whineIfExists, FieldAccess fieldAccess) {
		
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl) typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		}
		
		if (notAClass) {
			source.addError("@ToString is only supported on a class or enum.");
			return;
		}
		
		switch (methodExists("toString", typeNode, 0)) {
		case NOT_EXISTS:
			if (callSuper == null) {
				if (isDirectDescendantOfObject(typeNode)) {
					callSuper = false;
				} else {
					CallSuperType cst = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_CALL_SUPER);
					if (cst == null) cst = CallSuperType.SKIP;
					switch (cst) {
					default:
					case SKIP:
						callSuper = false;
						break;
					case WARN:
						source.addWarning("Generating toString implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@ToString(callSuper=false)' to your type.");
						callSuper = false;
						break;
					case CALL:
						callSuper = true;
						break;
					}
				}
			}
			JCMethodDecl method = createToString(typeNode, members, includeFieldNames, callSuper, fieldAccess, source.get());
			injectMethod(typeNode, method);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				source.addWarning("Not generating toString(): A method with that name already exists");
			}
			break;
		}
	}
	
	static JCMethodDecl createToString(JavacNode typeNode, Collection<Included<JavacNode, ToString.Include>> members,
		boolean includeNames, boolean callSuper, FieldAccess fieldAccess, JCTree source) {
		
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		List<JCAnnotation> annsOnMethod = List.of(overrideAnnotation);
		if (getCheckerFrameworkVersion(typeNode).generateSideEffectFree()) annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, annsOnMethod);
		JCExpression returnType = genJavaLangTypeRef(typeNode, "String");
		
		boolean first = true;
		
		String typeName = getTypeName(typeNode);
		boolean isEnum = typeNode.isEnumType();
		
		String infix = ", ";
		String suffix = ")";
		String prefix;
		if (callSuper) {
			prefix = "(super=";
		} else if (members.isEmpty()) {
			prefix = isEnum ? "" : "()";
		} else if (includeNames) {
			Included<JavacNode, ToString.Include> firstMember = members.iterator().next();
			String name = firstMember.getInc() == null ? "" : firstMember.getInc().name();
			if (name.isEmpty()) name = firstMember.getNode().getName();
			prefix = "(" + name + "=";
		} else {
			prefix = "(";
		}
		
		JCExpression current;
		if (!isEnum) { 
			current = maker.Literal(typeName + prefix);
		} else {
			current = maker.Binary(CTC_PLUS, maker.Literal(typeName + "."), maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("this")), typeNode.toName("name")),
					List.<JCExpression>nil()));
			if (!prefix.isEmpty()) current = maker.Binary(CTC_PLUS, current, maker.Literal(prefix));
		}
		
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
				maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("toString")),
				List.<JCExpression>nil());
			current = maker.Binary(CTC_PLUS, current, callToSuper);
			first = false;
		}
		
		for (Included<JavacNode, ToString.Include> member : members) {
			JCExpression expr;
			
			JCExpression memberAccessor;
			JavacNode memberNode = member.getNode();
			if (memberNode.getKind() == Kind.METHOD) {
				memberAccessor = createMethodAccessor(maker, memberNode);
			} else {
				memberAccessor = createFieldAccessor(maker, memberNode, fieldAccess);
			}
			
			JCExpression memberType = getFieldType(memberNode, fieldAccess);
			
			// The distinction between primitive and object will be useful if we ever add a 'hideNulls' option.
			@SuppressWarnings("unused")
			boolean fieldIsPrimitive = memberType instanceof JCPrimitiveTypeTree;
			boolean fieldIsPrimitiveArray = memberType instanceof JCArrayTypeTree && ((JCArrayTypeTree) memberType).elemtype instanceof JCPrimitiveTypeTree;
			boolean fieldIsObjectArray = !fieldIsPrimitiveArray && memberType instanceof JCArrayTypeTree;
			
			if (fieldIsPrimitiveArray || fieldIsObjectArray) {
				JCExpression tsMethod = chainDots(typeNode, "java", "util", "Arrays", fieldIsObjectArray ? "deepToString" : "toString");
				expr = maker.Apply(List.<JCExpression>nil(), tsMethod, List.<JCExpression>of(memberAccessor));
			} else expr = memberAccessor;
			
			if (first) {
				current = maker.Binary(CTC_PLUS, current, expr);
				first = false;
				continue;
			}
			
			if (includeNames) {
				String n = member.getInc() == null ? "" : member.getInc().name();
				if (n.isEmpty()) n = memberNode.getName();
				current = maker.Binary(CTC_PLUS, current, maker.Literal(infix + n + "="));
			} else {
				current = maker.Binary(CTC_PLUS, current, maker.Literal(infix));
			}
			
			current = maker.Binary(CTC_PLUS, current, expr);
		}
		
		if (!first) current = maker.Binary(CTC_PLUS, current, maker.Literal(suffix));
		
		JCStatement returnStatement = maker.Return(current);
		
		JCBlock body = maker.Block(0, List.of(returnStatement));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("toString"), returnType,
			List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null), source, typeNode.getContext());
	}
	
	public static String getTypeName(JavacNode typeNode) {
		String typeName = ((JCClassDecl) typeNode.get()).name.toString();
		JavacNode upType = typeNode.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = ((JCClassDecl) upType.get()).name.toString() + "." + typeName;
			upType = upType.up();
		}
		return typeName;
	}
}
