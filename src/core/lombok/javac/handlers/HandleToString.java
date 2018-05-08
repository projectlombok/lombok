/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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
import lombok.core.AST.Kind;
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
import com.sun.tools.javac.util.ListBuffer;

/**
 * Handles the {@code ToString} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleToString extends JavacAnnotationHandler<ToString> {
	private void checkForBogusFieldNames(JavacNode type, AnnotationValues<ToString> annotation, List<String> excludes, List<String> includes) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(excludes, type, true, false)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(includes, type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	@Override
	public void handle(AnnotationValues<ToString> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@ToString");
		
		deleteAnnotationIfNeccessary(annotationNode, ToString.class);
		
		ToString ann = annotation.getInstance();
		List<String> excludes = List.from(ann.exclude());
		List<String> includes = List.from(ann.of());
		JavacNode typeNode = annotationNode.up();
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		checkForBogusFieldNames(typeNode, annotation, excludes, includes);

		Boolean fqn = ann.fqn();
		Boolean callSuper = ann.callSuper();
		String prefix = ann.prefix();
		String separator = ann.separator();
		String infix = ann.infix();
		String suffix = ann.suffix();

		if (!annotation.isExplicit("fqn")) fqn = null;
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		if (!annotation.isExplicit("prefix")) prefix = null;
		if (!annotation.isExplicit("separator")) separator = null;
		if (!annotation.isExplicit("infix")) infix = null;
		if (!annotation.isExplicit("suffix")) suffix = null;
		
		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		Boolean fieldNamesConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
		boolean includeFieldNames = annotation.isExplicit("includeFieldNames") || fieldNamesConfiguration == null ? ann.includeFieldNames() : fieldNamesConfiguration;

		generateToString(typeNode, annotationNode, fqn, prefix, callSuper, includeFieldNames, separator, fieldAccess, excludes, includes, infix, suffix, true);
	}
	
	public void generateToStringForType(JavacNode typeNode, JavacNode errorNode) {
		if (hasAnnotation(ToString.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		
		boolean includeFieldNames = true;
		try {
			Boolean configuration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
			includeFieldNames = configuration != null ? configuration : ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}
		
		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;
		
		generateToString(typeNode, errorNode, null, null, null, includeFieldNames, null, access, null, null, null, null, false);
	}
	
	private void generateToString(JavacNode typeNode, JavacNode source, Boolean fqn, String prefix, Boolean callSuper,
								  boolean includeFieldNames, String separator, FieldAccess fieldAccess, List<String> excludes, List<String> includes,
								  String infix, String suffix, boolean whineIfExists) {
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		}
		
		if (notAClass) {
			source.addError("@ToString is only supported on a class or enum.");
			return;
		}

		if (fqn == null) {
			try {
				fqn = ((Boolean)ToString.class.getMethod("fqn").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}

		if (prefix == null) {
			try {
				prefix = ((String)ToString.class.getMethod("prefix").getDefaultValue());
			} catch (Exception ignore) {}
		}

		if (callSuper == null) {
			try {
				callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}

		if (separator == null) {
			try {
				separator = ((String)ToString.class.getMethod("separator").getDefaultValue());
			} catch (Exception ignore) {}
		}

		ListBuffer<JavacNode> nodesForToString = new ListBuffer<JavacNode>();
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

		if (infix == null) {
			try {
				infix = ((String)ToString.class.getMethod("infix").getDefaultValue());
			} catch (Exception ignore) {}
		}

		if (suffix == null) {
			try {
				suffix = ((String)ToString.class.getMethod("suffix").getDefaultValue());
			} catch (Exception ignore) {}
		}
		
		switch (methodExists("toString", typeNode, 0)) {
		case NOT_EXISTS:
			JCMethodDecl method = createToString(typeNode, source.get(), fqn, prefix, callSuper, includeFieldNames,
					separator, fieldAccess, nodesForToString.toList(), infix, suffix);
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
	
	static JCMethodDecl createToString(JavacNode typeNode, JCTree source, boolean fqn, String prefix, boolean callSuper,
									   boolean includeFieldNames, String separator, FieldAccess fieldAccess,
									   Collection<JavacNode> fields, String infix, String suffix) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = genJavaLangTypeRef(typeNode, "String");

		String resultLiteral;
		String typeName = getTypeName(typeNode, fqn);
		if (callSuper) {
			resultLiteral = typeName + prefix + "super" + separator;
		} else if (fields.isEmpty()) {
			resultLiteral = typeName + prefix + suffix;
		} else if (includeFieldNames) {
			resultLiteral = typeName + prefix + ((JCVariableDecl)fields.iterator().next().get()).name.toString() + separator;
		} else {
			resultLiteral = typeName + prefix;
		}

		boolean first = true;
		JCExpression current = maker.Literal(resultLiteral);
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("toString")),
					List.<JCExpression>nil());
			current = maker.Binary(CTC_PLUS, current, callToSuper);
			first = false;
		}
		
		for (JavacNode fieldNode : fields) {
			JCExpression expr;
			
			JCExpression fieldAccessor = createFieldAccessor(maker, fieldNode, fieldAccess);
			
			JCExpression fieldType = getFieldType(fieldNode, fieldAccess);
			
			// The distinction between primitive and object will be useful if we ever add a 'hideNulls' option.
			boolean fieldIsPrimitive = fieldType instanceof JCPrimitiveTypeTree;
			boolean fieldIsPrimitiveArray = fieldType instanceof JCArrayTypeTree && ((JCArrayTypeTree) fieldType).elemtype instanceof JCPrimitiveTypeTree;
			boolean fieldIsObjectArray = !fieldIsPrimitiveArray && fieldType instanceof JCArrayTypeTree;
			@SuppressWarnings("unused")
			boolean fieldIsObject = !fieldIsPrimitive && !fieldIsPrimitiveArray && !fieldIsObjectArray;
			
			if (fieldIsPrimitiveArray || fieldIsObjectArray) {
				JCExpression tsMethod = chainDots(typeNode, "java", "util", "Arrays", fieldIsObjectArray ? "deepToString" : "toString");
				expr = maker.Apply(List.<JCExpression>nil(), tsMethod, List.<JCExpression>of(fieldAccessor));
			} else expr = fieldAccessor;
			
			if (first) {
				current = maker.Binary(CTC_PLUS, current, expr);
				first = false;
				continue;
			}
			
			if (includeFieldNames) {
				current = maker.Binary(CTC_PLUS, current, maker.Literal(infix + fieldNode.getName() + separator));
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
	
	private static String getTypeName(JavacNode typeNode, boolean fqn) {
		if (fqn) {
			return ((JCClassDecl) typeNode.get()).sym.getQualifiedName().toString();
		} else {
			String typeName = ((JCClassDecl) typeNode.get()).name.toString();
			JavacNode upType = typeNode.up();
			while (upType.getKind() == Kind.TYPE) {
				typeName = ((JCClassDecl) upType.get()).name.toString() + "." + typeName;
				upType = upType.up();
			}
			return typeName;
		}
	}
}
