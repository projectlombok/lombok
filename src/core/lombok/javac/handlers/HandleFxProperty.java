/*
 * Copyright (C) 2020-2021 The Project Lombok Authors.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.extern.javafx.FxProperty;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.JavacTreeMaker.TypeTag;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

/**
 * Handles the {@link FxProperty} annotation for javac.
 */
@Provides
public class HandleFxProperty extends JavacAnnotationHandler<FxProperty> {
	
	@Override public void handle(AnnotationValues<FxProperty> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.FXPROPERTY_FLAG_USAGE, "@FxProperty");
		
		Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
		deleteAnnotationIfNeccessary(annotationNode, FxProperty.class);
		JavacNode node = annotationNode.up();
		FxProperty annotationInstance = annotation.getInstance();
		boolean readOnly = annotationInstance.readOnly();
		
		if (node == null) return;
		
		switch (node.getKind()) {
		case FIELD:
			createFxPropertyForFields(fields, annotationNode, readOnly);
			break;
		case TYPE:
			createFxPropertyForType(node, annotationNode, readOnly);
			break;
		}
	}
	
	private void createFxPropertyForType(JavacNode typeNode, JavacNode annotationNode, boolean readOnly) {
		if (!isClassOrEnum(typeNode)) {
			annotationNode.addError("@FxProperty is only supported on a class, an enum, or a field.");
			return;
		}
		for (JavacNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			if (field.getName().startsWith("$")) continue;
			
			createFxPropertyForField(field, annotationNode, readOnly);
		}
	}

	private void createFxPropertyForFields(Collection<JavacNode> fields, JavacNode annotationNode, boolean readOnly) {
		for (JavacNode field : fields) {
			createFxPropertyForField(field, annotationNode, readOnly);
		}
	}
	
	private void createFxPropertyForField(JavacNode fieldNode, JavacNode source, boolean readOnly) {
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		JavacNode typeNode = upToTypeNode(fieldNode);
		
		FieldInfo fieldInfo = getFieldInfo(fieldNode, fieldDecl);
		if (fieldInfo == null) {
			source.addError("@FxProperty is only supported on predefined JavaFx property types.");
			return;
		}
		
		addPropertyAccessor(fieldNode, source, typeNode, fieldInfo, readOnly);
		addGetter(fieldNode, source, typeNode, fieldInfo);
		addSetter(fieldNode, source, typeNode, fieldInfo, readOnly);
	}

	private void addGetter(JavacNode fieldNode, JavacNode source, JavacNode typeNode, FieldInfo fieldInfo) {
		String methodName = buildAccessorName(fieldNode, "get", fieldNode.getName());
		if (methodExists(methodName, typeNode, 0) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		injectMethod(typeNode, recursiveSetGeneratedBy(createGetter(fieldNode, fieldInfo), source));
	}
	
	private void addSetter(JavacNode fieldNode, JavacNode source, JavacNode typeNode, FieldInfo fieldInfo, boolean readOnly) {
		if (fieldInfo.readOnly) return;
		
		String methodName = buildAccessorName(fieldNode, "set", fieldNode.getName());
		if (methodExists(methodName, typeNode, 1) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		injectMethod(typeNode, recursiveSetGeneratedBy(createSetter(fieldNode, fieldInfo, readOnly), source));
	}
	
	private void addPropertyAccessor(JavacNode fieldNode, JavacNode source, JavacNode typeNode, FieldInfo fieldInfo, boolean readOnly) {
		String methodName = toPropertyName(fieldNode.getName());
		if (methodExists(methodName, typeNode, 0) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		injectMethod(typeNode, recursiveSetGeneratedBy(createPropertyAccessor(fieldNode, fieldInfo, readOnly), source));
	}
	

	private JCMethodDecl createPropertyAccessor(JavacNode fieldNode, FieldInfo fieldInfo, boolean readOnly) {
		String methodName = toPropertyName(fieldNode.getName());
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression defaultValue = null;
		JCBlock methodBody = maker.Block(0, List.<JCStatement>of(maker.Return(createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD))));
		
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), fieldNode.toName(methodName), fieldInfo.getPropertyType(readOnly), methodGenericParams, parameters, throwsClauses, methodBody, defaultValue);
	}

	private JCMethodDecl createSetter(JavacNode fieldNode, FieldInfo fieldInfo, boolean readOnly) {
		Name methodName = fieldNode.toName(buildAccessorName(fieldNode, "set", fieldNode.getName()));
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		
		long modifiers = Flags.FINAL | (readOnly ? Flags.PRIVATE : Flags.PUBLIC);
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression defaultValue = null;
		JCExpression methodType = maker.Type(Javac.createVoidType(fieldNode.getSymbolTable(), CTC_VOID));
		
		Name paramName = fieldDecl.name;
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, fieldNode.getContext());
		List<JCVariableDecl> parameters = List.of(maker.VarDef(maker.Modifiers(flags, List.<JCAnnotation>nil()), paramName, fieldInfo.getType(), null));
		
		List<JCExpression> typeargs = List.nil();
		JCExpression fieldRef = createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD);
		JCMethodInvocation write = maker.Apply(typeargs, maker.Select(fieldRef, fieldNode.toName("set")), List.<JCExpression>of(maker.Ident(paramName)));
		JCBlock methodBody = maker.Block(0, List.<JCStatement>of(maker.Exec(write)));
		
		return maker.MethodDef(maker.Modifiers(modifiers), methodName, methodType, methodGenericParams, parameters, throwsClauses, methodBody, defaultValue);
	}

	private JCMethodDecl createGetter(JavacNode fieldNode, FieldInfo fieldInfo) {
		Name methodName = fieldNode.toName(buildAccessorName(fieldNode, "get", fieldNode.getName()));
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression defaultValue = null;
		List<JCExpression> args = List.nil();
		
		List<JCExpression> typeargs = List.nil();
		JCExpression fieldRef = createFieldAccessor(maker, fieldNode, FieldAccess.ALWAYS_FIELD);
		JCMethodInvocation apply = maker.Apply(typeargs, maker.Select(fieldRef, fieldNode.toName("get")), args);
		JCBlock methodBody = maker.Block(0, List.<JCStatement>of(maker.Return(apply)));
		
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC | Flags.FINAL), methodName, fieldInfo.getType(), methodGenericParams, parameters, throwsClauses, methodBody, defaultValue);
	}
	
	static class FieldInfo {
		JavacNode fieldNode;
		String propertyType;
		String returnType;
		boolean readOnly;
		private List<JCExpression> typeArguments;

		FieldInfo(JavacNode fieldNode, String propertyType, String returnType) {
			this.fieldNode = fieldNode;
			this.propertyType = propertyType;
			this.returnType = returnType;
			this.readOnly = propertyType.contains("ReadOnly");
		}
		
		FieldInfo(JavacNode fieldNode, String propertyType, String returnType, List<JCExpression> typeArguments) {
			this(fieldNode, propertyType, returnType);
			this.typeArguments = typeArguments;
		}
		
		JCExpression getType() {
			TypeTag primitiveTypeTag = PRIMITIVE_TYPE_MAP.get(returnType);
			if (primitiveTypeTag != null) {
				return fieldNode.getTreeMaker().TypeIdent(primitiveTypeTag);
			}
			
			JCExpression typeRef = genTypeRef(fieldNode, returnType);
			
			if (typeArguments == null) return typeRef;
			
			if (returnType.endsWith("Object")) {
				return typeArguments.get(0);
			}
			return fieldNode.getTreeMaker().TypeApply(typeRef, typeArguments);
		}
		
		JCExpression getPropertyType(boolean asReadOnly) {
			String type = propertyType;
			if (asReadOnly && !readOnly) {
				type = "javafx.beans.property.ReadOnly" + propertyType.substring(22);
			}
			return genTypeRef(fieldNode, type);
		}
	}
	
	private static Map<String, String> SIMPLE_PROPERTY_TYPE_MAP;
	private static Map<String, String> GENERIC_PROPERTY_TYPE_MAP;
	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put("javafx.beans.property.ObjectProperty", "java.lang.Object");
		m.put("javafx.beans.property.ListProperty", "javafx.collections.ObservableList");
		m.put("javafx.beans.property.MapProperty", "javafx.collections.ObservableMap");
		m.put("javafx.beans.property.SetProperty", "javafx.collections.ObservableSet");
		
		m.put("javafx.beans.property.ReadOnlyObjectProperty", "java.lang.Object");
		m.put("javafx.beans.property.ReadOnlyListProperty", "javafx.collections.ObservableList");
		m.put("javafx.beans.property.ReadOnlyMapProperty", "javafx.collections.ObservableMap");
		m.put("javafx.beans.property.ReadOnlySetProperty", "javafx.collections.ObservableSet");
		GENERIC_PROPERTY_TYPE_MAP = Collections.unmodifiableMap(m);
		
		m.put("javafx.beans.property.IntegerProperty", "java.lang.Integer");
		m.put("javafx.beans.property.LongProperty", "java.lang.Long");
		m.put("javafx.beans.property.FloatProperty", "java.lang.Float");
		m.put("javafx.beans.property.DoubleProperty", "java.lang.Double");
		m.put("javafx.beans.property.BooleanProperty", "java.lang.Boolean");
		m.put("javafx.beans.property.StringProperty", "java.lang.String");
		
		m.put("javafx.beans.property.ReadOnlyIntegerProperty", "java.lang.Integer");
		m.put("javafx.beans.property.ReadOnlyLongProperty", "java.lang.Long");
		m.put("javafx.beans.property.ReadOnlyFloatProperty", "java.lang.Float");
		m.put("javafx.beans.property.ReadOnlyDoubleProperty", "java.lang.Double");
		m.put("javafx.beans.property.ReadOnlyBooleanProperty", "java.lang.Boolean");
		m.put("javafx.beans.property.ReadOnlyStringProperty", "java.lang.String");
		SIMPLE_PROPERTY_TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	private static Map<String, TypeTag> PRIMITIVE_TYPE_MAP;
	static {
		Map<String, TypeTag> m = new HashMap<String, TypeTag>();
		m.put("java.lang.Integer", CTC_INT);
		m.put("java.lang.Long", CTC_LONG);
		m.put("java.lang.Float", CTC_FLOAT);
		m.put("java.lang.Double", CTC_DOUBLE);
		m.put("java.lang.Boolean", CTC_BOOLEAN);
		PRIMITIVE_TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	private FieldInfo getFieldInfo(JavacNode fieldNode, JCVariableDecl fieldDecl) {
		JCExpression vartype = fieldDecl.vartype;
		if (vartype instanceof JCTypeApply) {
			JCTypeApply typeApply = (JCTypeApply) vartype;
			JCExpression clazz = typeApply.clazz;
			List<JCExpression> arguments = typeApply.arguments;
			
			for (Entry<String, String> property : GENERIC_PROPERTY_TYPE_MAP.entrySet()) {
				if (typeMatches(property.getKey(), fieldNode, clazz)) {
					return new FieldInfo(fieldNode, property.getKey(), property.getValue(), arguments);
				}
			}
			return null;
		}
		
		for (Entry<String, String> property : SIMPLE_PROPERTY_TYPE_MAP.entrySet()) {
			if (typeMatches(property.getKey(), fieldNode, vartype)) {
				return new FieldInfo(fieldNode, property.getKey(), property.getValue());
			}
		}
		return null;
	}
}
