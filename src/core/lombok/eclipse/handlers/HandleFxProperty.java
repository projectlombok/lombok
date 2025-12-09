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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.extern.javafx.FxProperty;
import lombok.spi.Provides;

/**
 * Handles the {@link FxProperty} annotation for eclipse.
 */
@Provides
public class HandleFxProperty extends EclipseAnnotationHandler<FxProperty> {

	@Override public void handle(AnnotationValues<FxProperty> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.FXPROPERTY_FLAG_USAGE, "@FxProperty");
		
		Collection<EclipseNode> fields = annotationNode.upFromAnnotationToFields();
		EclipseNode node = annotationNode.up();
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
	
	private void createFxPropertyForType(EclipseNode typeNode, EclipseNode annotationNode, boolean readOnly) {
		TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
		boolean notClassOrEnum = (typeDecl.modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		if (notClassOrEnum) {
			annotationNode.addError("@FxProperty is only supported on a class, an enum, or a field.");
			return;
		}
		for (EclipseNode field : typeNode.down()) {
			if (field.getKind() != Kind.FIELD) continue;
			if (field.getName().startsWith("$")) continue;
			
			createFxPropertyForField(field, annotationNode, readOnly, false);
		}
	}

	private void createFxPropertyForFields(Collection<EclipseNode> fields, EclipseNode annotationNode, boolean readOnly) {
		for (EclipseNode field : fields) {
			createFxPropertyForField(field, annotationNode, readOnly, true);
		}
	}

	private void createFxPropertyForField(EclipseNode fieldNode, EclipseNode source, boolean readOnly, boolean whine) {
		FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
		EclipseNode typeNode = upToTypeNode(fieldNode);
		
		FieldInfo fieldInfo = getFieldInfo(fieldNode, fieldDecl);
		if (fieldInfo == null) {
			if (whine) source.addError("@FxProperty is only supported on predefined JavaFx property types.");
			return;
		}
		
		addPropertyAccessor(fieldNode, source, typeNode, fieldInfo, readOnly);
		addGetter(fieldNode, source, typeNode, fieldInfo);
		addSetter(fieldNode, source, typeNode, fieldInfo, readOnly);
	}

	private void addGetter(EclipseNode fieldNode, EclipseNode source, EclipseNode typeNode, FieldInfo fieldInfo) {
		String methodName = buildAccessorName(fieldNode, "get", fieldNode.getName());
		if (methodExists(methodName, typeNode, 0) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		MethodDeclaration method = createGetter(fieldNode, fieldInfo);
		method.traverse(new SetGeneratedByVisitor(source.get()), parent.scope);
		
		injectMethod(typeNode, method);
	}
	
	private void addSetter(EclipseNode fieldNode, EclipseNode source, EclipseNode typeNode, FieldInfo fieldInfo, boolean readOnly) {
		if (fieldInfo.readOnly) return;
		
		String methodName = buildAccessorName(fieldNode, "set", fieldNode.getName());
		if (methodExists(methodName, typeNode, 1) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		MethodDeclaration method = createSetter(fieldNode, fieldInfo, readOnly);
		method.traverse(new SetGeneratedByVisitor(source.get()), parent.scope);
		
		injectMethod(typeNode, method);
	}
	
	private void addPropertyAccessor(EclipseNode fieldNode, EclipseNode source, EclipseNode typeNode, FieldInfo fieldInfo, boolean readOnly) {
		String methodName = toPropertyName(fieldNode.getName());
		if (methodExists(methodName, typeNode, 0) != MemberExistsResult.NOT_EXISTS) {
			return;
		}
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		MethodDeclaration method = createPropertyAccessor(fieldNode, fieldInfo, readOnly);
		method.traverse(new SetGeneratedByVisitor(source.get()), parent.scope);
		
		injectMethod(typeNode, method);
	}
	

	private MethodDeclaration createPropertyAccessor(EclipseNode fieldNode, FieldInfo fieldInfo, boolean readOnly) {
		String methodName = toPropertyName(fieldNode.getName());
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, null);
		Statement returnStatement = new ReturnStatement(fieldRef, 0, 0);
		
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = ClassFileConstants.AccPublic;
		method.returnType = fieldInfo.getPropertyType(readOnly);
		method.selector = methodName.toCharArray();
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.statements = new Statement[] {returnStatement};
		
		return method;
	}

	private MethodDeclaration createSetter(EclipseNode fieldNode, FieldInfo fieldInfo, boolean readOnly) {
		String methodName = buildAccessorName(fieldNode, "set", fieldNode.getName());
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		char[] paramName = fieldNode.getName().toCharArray();
		Argument param = new Argument(paramName, 0, fieldInfo.getType(), Modifier.FINAL);
		
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, null);
		NameReference fieldNameRef = new SingleNameReference(paramName, 0);
		
		MessageSend set = new MessageSend();
		set.receiver = fieldRef;
		set.selector = "set".toCharArray();
		set.arguments = new Expression [] {fieldNameRef};
		
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = ClassFileConstants.AccFinal | (readOnly ? ClassFileConstants.AccPrivate : ClassFileConstants.AccPublic);
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.selector = methodName.toCharArray();
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.arguments = new Argument[] {param};
		method.statements = new Statement[] {set};
		
		return method;
	}

	private MethodDeclaration createGetter(EclipseNode fieldNode, FieldInfo fieldInfo) {
		String methodName = buildAccessorName(fieldNode, "get", fieldNode.getName());
		
		TypeDeclaration parent = (TypeDeclaration) upToTypeNode(fieldNode).get();
		
		Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, null);
		
		MessageSend get = new MessageSend();
		get.receiver = fieldRef;
		get.selector = "get".toCharArray();
		
		Statement returnStatement = new ReturnStatement(get, 0, 0);
		
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccFinal;
		method.returnType = fieldInfo.getType();
		method.selector = methodName.toCharArray();
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.statements = new Statement[] {returnStatement};
		
		return method;
	}
	
	static class FieldInfo {
		EclipseNode fieldNode;
		String propertyType;
		String returnType;
		boolean readOnly;
		private TypeReference[] typeArguments;

		FieldInfo(EclipseNode fieldNode, String propertyType, String returnType) {
			this.fieldNode = fieldNode;
			this.propertyType = propertyType;
			this.returnType = returnType;
			this.readOnly = propertyType.contains("ReadOnly");
		}
		
		FieldInfo(EclipseNode fieldNode, String propertyType, String returnType, TypeReference[] typeArguments) {
			this(fieldNode, propertyType, returnType);
			this.typeArguments = typeArguments;
		}
		
		TypeReference getType() {
			Integer primitiveType = PRIMITIVE_TYPE_MAP.get(returnType);
			if (primitiveType != null) {
				return TypeReference.baseTypeReference(primitiveType, 0);
			}
			
			if (returnType.endsWith("Object")) {
				return copyType(typeArguments[0]);
			}
			
			char[][] qualifiedName = Eclipse.fromQualifiedName(returnType);
			if (typeArguments == null) {
				return generateQualifiedTypeRef(fieldNode.get(), qualifiedName);
			}
			
			long[] p = Eclipse.poss(fieldNode.get(), qualifiedName.length);
			TypeReference[][] rr = new TypeReference[qualifiedName.length][];
			rr[rr.length - 1] = copyTypes(typeArguments, fieldNode.get());
			return new ParameterizedQualifiedTypeReference(qualifiedName, rr, 0, p);
		}
		
		TypeReference getPropertyType(boolean asReadOnly) {
			String type = propertyType;
			if (asReadOnly && !readOnly) {
				type = "javafx.beans.property.ReadOnly" + propertyType.substring(22);
			}
			char[][] qualifiedName = Eclipse.fromQualifiedName(type);
			if (typeArguments == null) {
				return generateQualifiedTypeRef(fieldNode.get(), qualifiedName);
			}
			
			long[] p = Eclipse.poss(fieldNode.get(), qualifiedName.length);
			TypeReference[][] rr = new TypeReference[qualifiedName.length][];
			rr[rr.length - 1] = copyTypes(typeArguments, fieldNode.get());
			return new ParameterizedQualifiedTypeReference(qualifiedName, rr, 0, p);
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
	
	private static Map<String, Integer> PRIMITIVE_TYPE_MAP;
	static {
		Map<String, Integer> m = new HashMap<String, Integer>();
		m.put("java.lang.Integer", TypeIds.T_int);
		m.put("java.lang.Long", TypeIds.T_long);
		m.put("java.lang.Float", TypeIds.T_float);
		m.put("java.lang.Double", TypeIds.T_double);
		m.put("java.lang.Boolean", TypeIds.T_boolean);
		PRIMITIVE_TYPE_MAP = Collections.unmodifiableMap(m);
	}
	
	private FieldInfo getFieldInfo(EclipseNode fieldNode, FieldDeclaration fieldDecl) {
		TypeReference vartype = fieldDecl.type;
		
		if (vartype.getTypeArguments() != null) {
			if (vartype.getTypeArguments().length != 1) {
				return null;
			}
			TypeReference[] arguments = vartype.getTypeArguments()[0];
			
			for (Entry<String, String> property : GENERIC_PROPERTY_TYPE_MAP.entrySet()) {
				if (typeMatches(property.getKey(), fieldNode, vartype)) {
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
