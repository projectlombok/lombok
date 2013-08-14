/*
 * Copyright (C) 2013 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.Collections;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.TransformationsUtil;
import lombok.experimental.Builder;
import lombok.experimental.NonFinal;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import static lombok.javac.Javac.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleBuilder extends JavacAnnotationHandler<Builder> {
	@Override public void handle(AnnotationValues<Builder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Builder builderInstance = annotation.getInstance();
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		String builderClassName = builderInstance.builderClassName();
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		if (builderClassName == null) builderClassName = "";
		
		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		if (!builderClassName.isEmpty()) {
			if (!checkName("builderClassName", builderClassName, annotationNode)) return;
		}
		
		deleteAnnotationIfNeccessary(annotationNode, Builder.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.experimental.Builder");
		
		JavacNode parent = annotationNode.up();
		
		java.util.List<JCExpression> typesOfParameters = new ArrayList<JCExpression>();
		java.util.List<Name> namesOfParameters = new ArrayList<Name>();
		JCExpression returnType;
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrownExceptions = List.nil();
		Name nameOfStaticBuilderMethod;
		JavacNode tdParent;
		
		JCMethodDecl fillParametersFrom = parent.get() instanceof JCMethodDecl ? ((JCMethodDecl) parent.get()) : null;
		
		if (parent.get() instanceof JCClassDecl) {
			tdParent = parent;
			JCClassDecl td = (JCClassDecl) tdParent.get();
			ListBuffer<JavacNode> allFields = ListBuffer.lb();
			@SuppressWarnings("deprecation")
			boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation(lombok.experimental.Value.class, parent));
			for (JavacNode fieldNode : HandleConstructor.findAllFields(tdParent)) {
				JCVariableDecl fd = (JCVariableDecl) fieldNode.get();
				// final fields with an initializer cannot be written to, so they can't be 'builderized'. Unfortunately presence of @Value makes
				// non-final fields final, but @Value's handler hasn't done this yet, so we have to do this math ourselves.
				// Value will only skip making a field final if it has an explicit @NonFinal annotation, so we check for that.
				if (fd.init != null && valuePresent && !hasAnnotation(NonFinal.class, fieldNode)) continue;
				namesOfParameters.add(removePrefixFromField(fieldNode));
				typesOfParameters.add(fd.vartype);
				allFields.append(fieldNode);
			}
			
			new HandleConstructor().generateConstructor(tdParent, AccessLevel.PACKAGE, List.<JCAnnotation>nil(), allFields.toList(), null, SkipIfConstructorExists.I_AM_BUILDER, true, annotationNode);
			
			returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
			typeParams = td.typarams;
			thrownExceptions = List.nil();
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = td.name.toString() + "Builder";
		} else if (fillParametersFrom != null && fillParametersFrom.getName().toString().equals("<init>")) {
			if (!fillParametersFrom.typarams.isEmpty()) {
				annotationNode.addError("@Builder is not supported on constructors with constructor type parameters.");
				return;
			}
			tdParent = parent.up();
			JCClassDecl td = (JCClassDecl) tdParent.get();
			returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
			typeParams = td.typarams;
			thrownExceptions = fillParametersFrom.thrown;
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = td.name.toString() + "Builder";
		} else if (fillParametersFrom != null) {
			tdParent = parent.up();
			JCClassDecl td = (JCClassDecl) tdParent.get();
			if ((fillParametersFrom.mods.flags & Flags.STATIC) == 0) {
				annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
				return;
			}
			returnType = fillParametersFrom.restype;
			typeParams = fillParametersFrom.typarams;
			thrownExceptions = fillParametersFrom.thrown;
			nameOfStaticBuilderMethod = fillParametersFrom.name;
			if (builderClassName.isEmpty()) {
				if (returnType instanceof JCTypeApply) {
					returnType = ((JCTypeApply) returnType).clazz;
				}
				if (returnType instanceof JCFieldAccess) {
					builderClassName = ((JCFieldAccess) returnType).name.toString() + "Builder";
				} else if (returnType instanceof JCIdent) {
					Name n = ((JCIdent) returnType).name;
					
					for (JCTypeParameter tp : typeParams) {
						if (tp.name.equals(n)) {
							annotationNode.addError("@Builder requires specifying 'builderClassName' if used on methods with a type parameter as return type.");
							return;
						}
					}
					builderClassName = n.toString() + "Builder";
				} else if (returnType instanceof JCPrimitiveTypeTree) {
					builderClassName = returnType.toString() + "Builder";
					if (Character.isLowerCase(builderClassName.charAt(0))) {
						builderClassName = Character.toTitleCase(builderClassName.charAt(0)) + builderClassName.substring(1);
					}
					
				} else {
					// This shouldn't happen.
					System.err.println("Lombok bug ID#20140614-1651: javac HandleBuilder: return type to name conversion failed: " + returnType.getClass());
					builderClassName = td.name.toString() + "Builder";
				}
			}
		} else {
			annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
			return;
		}
		
		if (fillParametersFrom != null) {
			for (JCVariableDecl param : fillParametersFrom.params) {
				namesOfParameters.add(param.name);
				typesOfParameters.add(param.vartype);
			}
		}
		
		JavacNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = makeBuilderClass(tdParent, builderClassName, typeParams, ast);
		} else {
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderType, annotationNode);
		}
		java.util.List<JavacNode> fieldNodes = addFieldsToBuilder(builderType, namesOfParameters, typesOfParameters, ast);
		java.util.List<JCMethodDecl> newMethods = new ArrayList<JCMethodDecl>();
		for (JavacNode fieldNode : fieldNodes) {
			JCMethodDecl newMethod = makeSetterMethodForBuilder(builderType, fieldNode, ast, builderInstance.fluent(), builderInstance.chain());
			if (newMethod != null) newMethods.add(newMethod);
		}
		
		if (constructorExists(builderType) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PACKAGE, List.<JCAnnotation>nil(), builderType, List.<JavacNode>nil(), true, ast);
			if (cd != null) injectMethod(builderType, cd);
		}
		
		for (JCMethodDecl newMethod : newMethods) injectMethod(builderType, newMethod);
		
		if (methodExists(buildMethodName, builderType, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuildMethod(buildMethodName, nameOfStaticBuilderMethod, returnType, namesOfParameters, builderType, thrownExceptions);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = HandleToString.createToString(builderType, fieldNodes, true, false, FieldAccess.ALWAYS_FIELD, ast);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuilderMethod(builderMethodName, builderClassName, tdParent, typeParams);
			if (md != null) injectMethod(tdParent, md);
		}
	}
	
	private JCMethodDecl generateBuildMethod(String name, Name staticName, JCExpression returnType, java.util.List<Name> fieldNames, JavacNode type, List<JCExpression> thrownExceptions) {
		TreeMaker maker = type.getTreeMaker();
		
		JCExpression call;
		JCStatement statement;
		
		ListBuffer<JCExpression> args = ListBuffer.lb();
		for (Name n : fieldNames) {
			args.append(maker.Ident(n));
		}
		
		if (staticName == null) {
			call = maker.NewClass(null, List.<JCExpression>nil(), returnType, args.toList(), null);
			statement = maker.Return(call);
		} else {
			ListBuffer<JCExpression> typeParams = ListBuffer.lb();
			for (JCTypeParameter tp : ((JCClassDecl) type.get()).typarams) {
				typeParams.append(maker.Ident(tp.name));
			}
			
			JCExpression fn = maker.Select(maker.Ident(((JCClassDecl) type.up().get()).name), staticName);
			call = maker.Apply(typeParams.toList(), fn, args.toList());
			if (returnType instanceof JCPrimitiveTypeTree && ((JCPrimitiveTypeTree) returnType).typetag == CTC_VOID) {
				statement = maker.Exec(call);
			} else {
				statement = maker.Return(call);
			}
		}
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), type.toName(name), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
	}
	
	private JCMethodDecl generateBuilderMethod(String builderMethodName, String builderClassName, JavacNode type, List<JCTypeParameter> typeParams) {
		TreeMaker maker = type.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = ListBuffer.lb();
		for (JCTypeParameter typeParam : typeParams) {
			typeArgs.append(maker.Ident(typeParam.name));
		}
		
		JCExpression call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, type.toName(builderClassName), typeParams), List.<JCExpression>nil(), null);
		JCStatement statement = maker.Return(call);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		return maker.MethodDef(maker.Modifiers(Flags.STATIC | Flags.PUBLIC), type.toName(builderMethodName), namePlusTypeParamsToTypeReference(maker, type.toName(builderClassName), typeParams), copyTypeParams(maker, typeParams), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	private java.util.List<JavacNode> addFieldsToBuilder(JavacNode builderType, java.util.List<Name> namesOfParameters, java.util.List<JCExpression> typesOfParameters, JCTree source) {
		int len = namesOfParameters.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		java.util.List<JavacNode>out = new ArrayList<JavacNode>();
		
		top:
		for (int i = len - 1; i >= 0; i--) {
			Name name = namesOfParameters.get(i);
			for (JavacNode exists : existing) {
				Name n = ((JCVariableDecl) exists.get()).name;
				if (n.equals(name)) {
					out.add(exists);
					continue top;
				}
			}
			TreeMaker maker = builderType.getTreeMaker();
			JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
			JCVariableDecl newField = maker.VarDef(mods, name, cloneType(maker, typesOfParameters.get(i), source), null);
			out.add(injectField(builderType, newField));
		}
		
		Collections.reverse(out);
		return out;
	}
	
	
	private JCMethodDecl makeSetterMethodForBuilder(JavacNode builderType, JavacNode fieldNode, JCTree source, boolean fluent, boolean chain) {
		Name fieldName = ((JCVariableDecl) fieldNode.get()).name;
		
		for (JavacNode child : builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			Name existingName = ((JCMethodDecl) child.get()).name;
			if (existingName.equals(fieldName)) return null;
		}
		
		boolean isBoolean = isBoolean(fieldNode);
		String setterName = fluent ? fieldNode.getName() : TransformationsUtil.toSetterName(null, fieldNode.getName(), isBoolean);
		
		TreeMaker maker = builderType.getTreeMaker();
		return HandleSetter.createSetter(Flags.PUBLIC, fieldNode, maker, setterName, chain, source, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
	}
	
	private JavacNode findInnerClass(JavacNode parent, String name) {
		for (JavacNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			JCClassDecl td = (JCClassDecl) child.get();
			if (td.name.contentEquals(name)) return child;
		}
		return null;
	}
	
	private JavacNode makeBuilderClass(JavacNode tdParent, String builderClassName, List<JCTypeParameter> typeParams, JCAnnotation ast) {
		TreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC | Flags.STATIC);
		JCClassDecl builder = ClassDef(maker, mods, tdParent.toName(builderClassName), copyTypeParams(maker, typeParams), null, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
	}
}
