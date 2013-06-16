/*
 * Copyright (C) 2010-2013 The Project Lombok Authors.
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.experimental.Builder;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class HandleConstructor {
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleNoArgsConstructor extends JavacAnnotationHandler<NoArgsConstructor> {
		@Override public void handle(AnnotationValues<NoArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			deleteAnnotationIfNeccessary(annotationNode, NoArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NoArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@NoArgsConstructor(onConstructor=", annotationNode);
			NoArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			if (level == AccessLevel.NONE) return;
			List<JavacNode> fields = List.nil();
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, fields, staticName, SkipIfConstructorExists.NO, false, annotationNode);
		}
	}
	
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleRequiredArgsConstructor extends JavacAnnotationHandler<RequiredArgsConstructor> {
		@Override public void handle(AnnotationValues<RequiredArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			deleteAnnotationIfNeccessary(annotationNode, RequiredArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, RequiredArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@RequiredArgsConstructor(onConstructor=", annotationNode);
			RequiredArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			@SuppressWarnings("deprecation")
			boolean suppressConstructorProperties = ann.suppressConstructorProperties();
			if (level == AccessLevel.NONE) return;
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, findRequiredFields(typeNode), staticName, SkipIfConstructorExists.NO, suppressConstructorProperties, annotationNode);
		}
	}
	
	private static List<JavacNode> findRequiredFields(JavacNode typeNode) {
		ListBuffer<JavacNode> fields = ListBuffer.lb();
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ((fieldFlags & Flags.STATIC) != 0) continue;
			boolean isFinal = (fieldFlags & Flags.FINAL) != 0;
			boolean isNonNull = !findAnnotations(child, TransformationsUtil.NON_NULL_PATTERN).isEmpty();
			if ((isFinal || isNonNull) && fieldDecl.init == null) fields.append(child);
		}
		return fields.toList();
	}
	
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleAllArgsConstructor extends JavacAnnotationHandler<AllArgsConstructor> {
		@Override public void handle(AnnotationValues<AllArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			deleteAnnotationIfNeccessary(annotationNode, AllArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, AllArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@AllArgsConstructor(onConstructor=", annotationNode);
			AllArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			String staticName = ann.staticName();
			@SuppressWarnings("deprecation")
			boolean suppressConstructorProperties = ann.suppressConstructorProperties();
			if (level == AccessLevel.NONE) return;
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, findAllFields(typeNode), staticName, SkipIfConstructorExists.NO, suppressConstructorProperties, annotationNode);
		}
	}
	
	static List<JavacNode> findAllFields(JavacNode typeNode) {
		ListBuffer<JavacNode> fields = ListBuffer.lb();
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ((fieldFlags & Flags.STATIC) != 0) continue;
			//Skip initialized final fields
			boolean isFinal = (fieldFlags & Flags.FINAL) != 0;
			if (!isFinal || fieldDecl.init == null) fields.append(child);
		}
		return fields.toList();
	}
	
	static boolean checkLegality(JavacNode typeNode, JavacNode errorNode, String name) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError(name + " is only supported on a class or an enum.");
			return false;
		}
		
		return true;
	}
	
	public void generateRequiredArgsConstructor(JavacNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findRequiredFields(typeNode), staticName, skipIfConstructorExists, false, source);
	}
	
	public enum SkipIfConstructorExists {
		YES, NO, I_AM_BUILDER;
	}
	
	public void generateAllArgsConstructor(JavacNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findAllFields(typeNode), staticName, skipIfConstructorExists, false, source);
	}
	
	public void generateConstructor(JavacNode typeNode, AccessLevel level, List<JCAnnotation> onConstructor, List<JavacNode> fields, String staticName, SkipIfConstructorExists skipIfConstructorExists, boolean suppressConstructorProperties, JavacNode source) {
		boolean staticConstrRequired = staticName != null && !staticName.equals("");
		
		if (skipIfConstructorExists != SkipIfConstructorExists.NO && constructorExists(typeNode) != MemberExistsResult.NOT_EXISTS) return;
		if (skipIfConstructorExists != SkipIfConstructorExists.NO) {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() == Kind.ANNOTATION) {
					boolean skipGeneration = annotationTypeMatches(NoArgsConstructor.class, child) ||
							annotationTypeMatches(AllArgsConstructor.class, child) ||
							annotationTypeMatches(RequiredArgsConstructor.class, child);
					
					if (!skipGeneration && skipIfConstructorExists == SkipIfConstructorExists.YES) {
						skipGeneration = annotationTypeMatches(Builder.class, child);
					}
					
					if (skipGeneration) {
						if (staticConstrRequired) {
							// @Data has asked us to generate a constructor, but we're going to skip this instruction, as an explicit 'make a constructor' annotation
							// will take care of it. However, @Data also wants a specific static name; this will be ignored; the appropriate way to do this is to use
							// the 'staticName' parameter of the @XArgsConstructor you've stuck on your type.
							// We should warn that we're ignoring @Data's 'staticConstructor' param.
							source.addWarning("Ignoring static constructor name: explicit @XxxArgsConstructor annotation present; its `staticName` parameter will be used.");
						}
						return;
					}
				}
			}
		}
		
		JCMethodDecl constr = createConstructor(staticConstrRequired ? AccessLevel.PRIVATE : level, onConstructor, typeNode, fields, suppressConstructorProperties, source.get());
		injectMethod(typeNode, constr);
		if (staticConstrRequired) {
			JCMethodDecl staticConstr = createStaticConstructor(staticName, level, typeNode, fields, source.get());
			injectMethod(typeNode, staticConstr);
		}
	}
	
	private static void addConstructorProperties(JCModifiers mods, JavacNode node, List<JavacNode> fields) {
		if (fields.isEmpty()) return;
		TreeMaker maker = node.getTreeMaker();
		JCExpression constructorPropertiesType = chainDots(node, "java", "beans", "ConstructorProperties");
		ListBuffer<JCExpression> fieldNames = ListBuffer.lb();
		for (JavacNode field : fields) {
			fieldNames.append(maker.Literal(field.getName()));
		}
		JCExpression fieldNamesArray = maker.NewArray(null, List.<JCExpression>nil(), fieldNames.toList());
		JCAnnotation annotation = maker.Annotation(constructorPropertiesType, List.of(fieldNamesArray));
		mods.annotations = mods.annotations.append(annotation);
	}
	
	static JCMethodDecl createConstructor(AccessLevel level, List<JCAnnotation> onConstructor, JavacNode typeNode, List<JavacNode> fields, boolean suppressConstructorProperties, JCTree source) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		boolean isEnum = (((JCClassDecl) typeNode.get()).mods.flags & Flags.ENUM) != 0;
		if (isEnum) level = AccessLevel.PRIVATE;
		
		ListBuffer<JCStatement> nullChecks = ListBuffer.lb();
		ListBuffer<JCStatement> assigns = ListBuffer.lb();
		ListBuffer<JCVariableDecl> params = ListBuffer.lb();
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, TransformationsUtil.NON_NULL_PATTERN);
			List<JCAnnotation> nullables = findAnnotations(fieldNode, TransformationsUtil.NULLABLE_PATTERN);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.FINAL, nonNulls.appendList(nullables)), field.name, field.vartype, null);
			params.append(param);
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), field.name);
			JCAssign assign = maker.Assign(thisX, maker.Ident(field.name));
			assigns.append(maker.Exec(assign));
			
			if (!nonNulls.isEmpty()) {
				JCStatement nullCheck = generateNullCheck(maker, fieldNode);
				if (nullCheck != null) nullChecks.append(nullCheck);
			}
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		if (!suppressConstructorProperties && level != AccessLevel.PRIVATE && !isLocalType(typeNode)) {
			addConstructorProperties(mods, typeNode, fields);
		}
		if (onConstructor != null) mods.annotations = mods.annotations.appendList(copyAnnotations(onConstructor));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
				null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(), maker.Block(0L, nullChecks.appendList(assigns).toList()), null), source);
	}
	
	private static boolean isLocalType(JavacNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
	
	private JCMethodDecl createStaticConstructor(String name, AccessLevel level, JavacNode typeNode, List<JavacNode> fields, JCTree source) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.STATIC | toJavacModifier(level));
		
		JCExpression returnType, constructorType;
		
		ListBuffer<JCTypeParameter> typeParams = ListBuffer.lb();
		ListBuffer<JCVariableDecl> params = ListBuffer.lb();
		ListBuffer<JCExpression> typeArgs1 = ListBuffer.lb();
		ListBuffer<JCExpression> typeArgs2 = ListBuffer.lb();
		ListBuffer<JCExpression> args = ListBuffer.lb();
		
		if (!type.typarams.isEmpty()) {
			for (JCTypeParameter param : type.typarams) {
				typeArgs1.append(maker.Ident(param.name));
				typeArgs2.append(maker.Ident(param.name));
				typeParams.append(maker.TypeParameter(param.name, param.bounds));
			}
			returnType = maker.TypeApply(maker.Ident(type.name), typeArgs1.toList());
			constructorType = maker.TypeApply(maker.Ident(type.name), typeArgs2.toList());
		} else {
			returnType = maker.Ident(type.name);
			constructorType = maker.Ident(type.name);
		}
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression pType = cloneType(maker, field.vartype, source);
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, TransformationsUtil.NON_NULL_PATTERN);
			List<JCAnnotation> nullables = findAnnotations(fieldNode, TransformationsUtil.NULLABLE_PATTERN);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.FINAL, nonNulls.appendList(nullables)), field.name, pType, null);
			params.append(param);
			args.append(maker.Ident(field.name));
		}
		JCReturn returnStatement = maker.Return(maker.NewClass(null, List.<JCExpression>nil(), constructorType, args.toList(), null));
		JCBlock body = maker.Block(0, List.<JCStatement>of(returnStatement));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName(name), returnType, typeParams.toList(), params.toList(), List.<JCExpression>nil(), body, null), source);
	}
}
