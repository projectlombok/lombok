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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.Builder;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;

import static lombok.javac.Javac.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

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
			new HandleConstructor().generateAllArgsConstructor(tdParent, AccessLevel.PRIVATE, null, SkipIfConstructorExists.I_AM_BUILDER, annotationNode);
			
			for (JavacNode fieldNode : HandleConstructor.findAllFields(tdParent)) {
				JCVariableDecl fd = (JCVariableDecl) fieldNode.get();
				namesOfParameters.add(fd.name);
				typesOfParameters.add(fd.vartype);
			}
			
			returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
			typeParams = td.typarams;
			thrownExceptions = null;
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
			if (builderClassName.isEmpty()) builderClassName = td.name.toString();
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
					builderClassName = ((JCFieldAccess) returnType).name.toString();
				} else if (returnType instanceof JCIdent) {
					Name n = ((JCIdent) returnType).name;
					
					for (JCTypeParameter tp : typeParams) {
						if (tp.name.contentEquals(n)) {
							annotationNode.addError("@Builder requires specifying 'builderClassName' if used on methods with a type parameter as return type.");
							return;
						}
					}
					builderClassName = n.toString();
				} else {
					// This shouldn't happen.
					System.err.println("Lombok bug ID#20140614-1651: javac HandleBuilder: return type to name conversion failed: " + returnType.getClass());
					builderClassName = td.name.toString();
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
		if (builderType == null) builderType = makeBuilderClass(tdParent, builderClassName, typeParams, ast);
		java.util.List<JavacNode> fieldNodes = addFieldsToBuilder(builderType, namesOfParameters, typesOfParameters, ast);
		java.util.List<JCMethodDecl> newMethods = new ArrayList<JCMethodDecl>();
		for (JavacNode fieldNode : fieldNodes) {
			JCMethodDecl newMethod = makeSetterMethodForBuider(builderType, fieldNode, ast);
			if (newMethod != null) newMethods.add(newMethod);
		}
		
		if (constructorExists(builderType) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PACKAGE, List.<JCAnnotation>nil(), builderType, List.<JavacNode>nil(), true, ast);
			if (cd != null) injectMethod(builderType, cd);
		}
		
		for (JCMethodDecl newMethod : newMethods) injectMethod(builderType, newMethod);
		
		if (methodExists(buildMethodName, builderType, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuildMethod(buildMethodName, nameOfStaticBuilderMethod, returnType, namesOfParameters, builderType, ast, thrownExceptions);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuilderMethod(builderMethodName, builderClassName, tdParent, typeParams, ast);
			if (md != null) injectMethod(tdParent, md);
		}
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
		JCClassDecl builder = maker.ClassDef(mods, tdParent.toName(builderClassName), typeParams, null, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
	}
}
