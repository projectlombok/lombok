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

import static lombok.javac.Javac.getCtcInt;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
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
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Handles the {@code ToString} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleToString extends JavacAnnotationHandler<ToString> {
	private void checkForBogusFieldNames(JavacNode type, AnnotationValues<ToString> annotation) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().exclude()), type, true, false)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().of()), type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	@Override public void handle(AnnotationValues<ToString> annotation, JCAnnotation ast, JavacNode annotationNode) {
		deleteAnnotationIfNeccessary(annotationNode, ToString.class);
		
		ToString ann = annotation.getInstance();
		List<String> excludes = List.from(ann.exclude());
		List<String> includes = List.from(ann.of());
		JavacNode typeNode = annotationNode.up();
		
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		FieldAccess fieldAccess = ann.doNotUseGetters() ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		generateToString(typeNode, annotationNode, excludes, includes, ann.includeFieldNames(), callSuper, ann.ignoreNullFields(), true, fieldAccess);
	}
	
	public void generateToStringForType(JavacNode typeNode, JavacNode errorNode) {
		if (hasAnnotation(ToString.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		
		boolean includeFieldNames = true;
		try {
			includeFieldNames = ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}
		generateToString(typeNode, errorNode, null, null, includeFieldNames, null, null, false, FieldAccess.GETTER);
	}
	
	public void generateToString(JavacNode typeNode, JavacNode source, List<String> excludes, List<String> includes,
			boolean includeFieldNames, Boolean callSuper, Boolean ignoreNullFields, boolean whineIfExists, FieldAccess fieldAccess) {
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		}
		
		if (notAClass) {
			source.addError("@ToString is only supported on a class or enum.");
			return;
		}
		
		if (callSuper == null) {
			try {
				callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}
		if (ignoreNullFields == null) {
			try {
				ignoreNullFields = ((Boolean)ToString.class.getMethod("ignoreNullFields").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}
		
		ListBuffer<JavacNode> nodesForToString = ListBuffer.lb();
		if (includes != null) {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				if (includes.contains(fieldDecl.name.toString())) nodesForToString.append(child);
			}
		} else {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				//Skip static fields.
				if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
				//Skip excluded fields.
				if (excludes != null && excludes.contains(fieldDecl.name.toString())) continue;
				//Skip fields that start with $.
				if (fieldDecl.name.toString().startsWith("$")) continue;
				nodesForToString.append(child);
			}
		}
		
		switch (methodExists("toString", typeNode, 0)) {
		case NOT_EXISTS:
			JCMethodDecl method = createToString(typeNode, nodesForToString.toList(), includeFieldNames, callSuper, ignoreNullFields, fieldAccess, source.get());
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
	
	private JCMethodDecl createToString(JavacNode typeNode, List<JavacNode> fields, boolean includeFieldNames, boolean callSuper, boolean ignoreNullFields, FieldAccess fieldAccess, JCTree source) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = chainDots(typeNode, "java", "lang", "String");
		
		boolean first = true;
		
		String typeName = getTypeName(typeNode);
		String infix = ", ";
		String suffix = ")javac";
		String prefix;
		if (callSuper) {
			prefix = typeName + "(super=";
		} else if (fields.isEmpty()) {
			prefix = typeName + "()";
		} else if (includeFieldNames && !ignoreNullFields) {
			prefix = typeName + "(" + ((JCVariableDecl)fields.iterator().next().get()).name.toString() + "=";
		} else {
			prefix = typeName + "(";
		}
		
		JCExpression current = maker.Literal(prefix);
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("toString")),
					List.<JCExpression>nil());
			current = maker.Binary(getCtcInt(JCTree.class, "PLUS"), current, callToSuper);
			first = false;
		}
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression expr;
			JCExpression fieldExpr;
			
			JCExpression fieldAccessor = createFieldAccessor(maker, fieldNode, fieldAccess);
			
			if (getFieldType(fieldNode, fieldAccess) instanceof JCArrayTypeTree) {
				boolean multiDim = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)field.vartype).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepTS = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(typeNode, "java", "util", "Arrays", useDeepTS ? "deepToString" : "toString");
				expr = maker.Apply(List.<JCExpression>nil(), hcMethod, List.<JCExpression>of(fieldAccessor));
			} else expr = fieldAccessor;
			
			if (includeFieldNames) {
				fieldExpr = maker.Binary(getCtcInt(JCTree.class, "PLUS"), maker.Literal(fieldNode.getName() + "="), expr);
			} else {
				fieldExpr = expr;
			}
			
			if (first) {
				first = false;
			} else {
				fieldExpr = maker.Binary(getCtcInt(JCTree.class, "PLUS"), maker.Literal(infix), fieldExpr);
			}
			
			if (ignoreNullFields) {
				JCExpression isNull = maker.Binary(getCtcInt(JCTree.class, "EQ"), expr, maker.Literal(TypeTags.BOT, null));
				current = maker.Binary(getCtcInt(JCTree.class, "PLUS"), current, maker.Conditional(isNull, maker.Literal(""), fieldExpr));
			} else {
				current = maker.Binary(getCtcInt(JCTree.class, "PLUS"), current, fieldExpr);
			}
		}
		
		if (!first) current = maker.Binary(getCtcInt(JCTree.class, "PLUS"), current, maker.Literal(suffix));
		
		JCStatement returnStatement = maker.Return(current);
		
		JCBlock body = maker.Block(0, List.of(returnStatement));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("toString"), returnType,
				List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null), source);
	}
	
	private String getTypeName(JavacNode typeNode) {
		String typeName = ((JCClassDecl) typeNode.get()).name.toString();
		JavacNode upType = typeNode.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = ((JCClassDecl) upType.get()).name.toString() + "." + typeName;
			upType = upType.up();
		}
		return typeName;
	}
}
