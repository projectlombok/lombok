/*
 * Copyright (C) 2010-2014 The Project Lombok Authors.
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.delombok.LombokOptionsFactory;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

public class HandleConstructor {
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleNoArgsConstructor extends JavacAnnotationHandler<NoArgsConstructor> {
		@Override public void handle(AnnotationValues<NoArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.NO_ARGS_CONSTRUCTOR_FLAG_USAGE, "@NoArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, NoArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NoArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@NoArgsConstructor(onConstructor", annotationNode);
			NoArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			boolean force = ann.force();
			List<JavacNode> fields = force ? findFinalFields(typeNode) : List.<JavacNode>nil();
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, fields, force, staticName, SkipIfConstructorExists.NO, annotationNode);
		}
	}
	
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleRequiredArgsConstructor extends JavacAnnotationHandler<RequiredArgsConstructor> {
		@Override public void handle(AnnotationValues<RequiredArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE, "@RequiredArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, RequiredArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, RequiredArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@RequiredArgsConstructor(onConstructor", annotationNode);
			RequiredArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, findRequiredFields(typeNode), false, staticName, SkipIfConstructorExists.NO, annotationNode);
		}
	}
	
	public static List<JavacNode> findRequiredFields(JavacNode typeNode) {
		return findFields(typeNode, true);
	}
	
	public static List<JavacNode> findFinalFields(JavacNode typeNode) {
		return findFields(typeNode, false);
	}
	
	public static List<JavacNode> findFields(JavacNode typeNode, boolean nullMarked) {
		ListBuffer<JavacNode> fields = new ListBuffer<JavacNode>();
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ((fieldFlags & Flags.STATIC) != 0) continue;
			boolean isFinal = (fieldFlags & Flags.FINAL) != 0;
			boolean isNonNull = nullMarked && !findAnnotations(child, NON_NULL_PATTERN).isEmpty();
			if ((isFinal || isNonNull) && fieldDecl.init == null) fields.append(child);
		}
		return fields.toList();
	}
	
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleAllArgsConstructor extends JavacAnnotationHandler<AllArgsConstructor> {
		@Override public void handle(AnnotationValues<AllArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.ALL_ARGS_CONSTRUCTOR_FLAG_USAGE, "@AllArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, AllArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, AllArgsConstructor.class.getSimpleName())) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@AllArgsConstructor(onConstructor", annotationNode);
			AllArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			new HandleConstructor().generateConstructor(typeNode, level, onConstructor, findAllFields(typeNode), false, staticName, SkipIfConstructorExists.NO, annotationNode);
		}
	}
	
	public static List<JavacNode> findAllFields(JavacNode typeNode) {
		return findAllFields(typeNode, false);
	}
	
	public static List<JavacNode> findAllFields(JavacNode typeNode, boolean evenFinalInitialized) {
		ListBuffer<JavacNode> fields = new ListBuffer<JavacNode>();
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
			if (evenFinalInitialized || !isFinal || fieldDecl.init == null) fields.append(child);
		}
		return fields.toList();
	}
	
	public static boolean checkLegality(JavacNode typeNode, JavacNode errorNode, String name) {
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
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findRequiredFields(typeNode), false, staticName, skipIfConstructorExists, source);
	}
	
	public enum SkipIfConstructorExists {
		YES, NO, I_AM_BUILDER;
	}
	
	public void generateAllArgsConstructor(JavacNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findAllFields(typeNode), false, staticName, skipIfConstructorExists, source);
	}
	
	public void generateConstructor(JavacNode typeNode, AccessLevel level, List<JCAnnotation> onConstructor, List<JavacNode> fields, boolean allToDefault, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
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
		
		JCMethodDecl constr = createConstructor(staticConstrRequired ? AccessLevel.PRIVATE : level, onConstructor, typeNode, fields, allToDefault, source);
		ListBuffer<Type> argTypes = new ListBuffer<Type>();
		for (JavacNode fieldNode : fields) {
			Type mirror = getMirrorForFieldType(fieldNode);
			if (mirror == null) {
				argTypes = null;
				break;
			}
			argTypes.append(mirror);
		}
		List<Type> argTypes_ = argTypes == null ? null : argTypes.toList();
		injectMethod(typeNode, constr, argTypes_, Javac.createVoidType(typeNode.getSymbolTable(), CTC_VOID));
		if (staticConstrRequired) {
			ClassSymbol sym = ((JCClassDecl) typeNode.get()).sym;
			Type returnType = sym == null ? null : sym.type;
			JCMethodDecl staticConstr = createStaticConstructor(staticName, level, typeNode, allToDefault ? List.<JavacNode>nil() : fields, source.get());
			injectMethod(typeNode, staticConstr, argTypes_, returnType);
		}
	}
	
	public static void addConstructorProperties(JCModifiers mods, JavacNode node, List<JavacNode> fields) {
		if (fields.isEmpty()) return;
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression constructorPropertiesType = chainDots(node, "java", "beans", "ConstructorProperties");
		ListBuffer<JCExpression> fieldNames = new ListBuffer<JCExpression>();
		for (JavacNode field : fields) {
			Name fieldName = removePrefixFromField(field);
			fieldNames.append(maker.Literal(fieldName.toString()));
		}
		JCExpression fieldNamesArray = maker.NewArray(null, List.<JCExpression>nil(), fieldNames.toList());
		JCAnnotation annotation = maker.Annotation(constructorPropertiesType, List.of(fieldNamesArray));
		mods.annotations = mods.annotations.append(annotation);
	}
	
	public static JCMethodDecl createConstructor(AccessLevel level, List<JCAnnotation> onConstructor, JavacNode typeNode, List<JavacNode> fields, boolean allToDefault, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		boolean isEnum = (((JCClassDecl) typeNode.get()).mods.flags & Flags.ENUM) != 0;
		if (isEnum) level = AccessLevel.PRIVATE;
		
		boolean suppressConstructorProperties;
		
		if (fields.isEmpty()) {
			suppressConstructorProperties = false;
		} else {
			suppressConstructorProperties = Boolean.TRUE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}
		
		ListBuffer<JCStatement> nullChecks = new ListBuffer<JCStatement>();
		ListBuffer<JCStatement> assigns = new ListBuffer<JCStatement>();
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			Name fieldName = removePrefixFromField(fieldNode);
			Name rawName = field.name;
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, NON_NULL_PATTERN);
			if (!allToDefault) {
				List<JCAnnotation> nullables = findAnnotations(fieldNode, NULLABLE_PATTERN);
				long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
				JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, nonNulls.appendList(nullables)), fieldName, field.vartype, null);
				params.append(param);
				if (!nonNulls.isEmpty()) {
					JCStatement nullCheck = generateNullCheck(maker, fieldNode, source);
					if (nullCheck != null) nullChecks.append(nullCheck);
				}
			}
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), rawName);
			JCExpression assign = maker.Assign(thisX, allToDefault ? getDefaultExpr(maker, field.vartype) : maker.Ident(fieldName));
			assigns.append(maker.Exec(assign));
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		if (!allToDefault && !suppressConstructorProperties && !isLocalType(typeNode) && LombokOptionsFactory.getDelombokOptions(typeNode.getContext()).getFormatPreferences().generateConstructorProperties()) {
			addConstructorProperties(mods, typeNode, fields);
		}
		if (onConstructor != null) mods.annotations = mods.annotations.appendList(copyAnnotations(onConstructor));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, nullChecks.appendList(assigns).toList()), null), source.get(), typeNode.getContext());
	}
	
	private static JCExpression getDefaultExpr(JavacTreeMaker maker, JCExpression type) {
		if (type instanceof JCPrimitiveTypeTree) {
			switch (((JCPrimitiveTypeTree) type).getPrimitiveTypeKind()) {
			case BOOLEAN:
				return maker.Literal(CTC_BOOLEAN, 0);
			case CHAR:
				return maker.Literal(CTC_CHAR, 0);
			default:
			case BYTE:
			case SHORT:
			case INT:
				return maker.Literal(CTC_INT, 0);
			case LONG:
				return maker.Literal(CTC_LONG, 0L);
			case FLOAT:
				return maker.Literal(CTC_FLOAT, 0F);
			case DOUBLE:
				return maker.Literal(CTC_DOUBLE, 0D);
			}
		}
		
		return maker.Literal(CTC_BOT, null);
		
	}
	
	public static boolean isLocalType(JavacNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
	
	public JCMethodDecl createStaticConstructor(String name, AccessLevel level, JavacNode typeNode, List<JavacNode> fields, JCTree source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.STATIC | toJavacModifier(level));
		
		JCExpression returnType, constructorType;
		
		ListBuffer<JCTypeParameter> typeParams = new ListBuffer<JCTypeParameter>();
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		ListBuffer<JCExpression> typeArgs1 = new ListBuffer<JCExpression>();
		ListBuffer<JCExpression> typeArgs2 = new ListBuffer<JCExpression>();
		ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
		
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
			Name fieldName = removePrefixFromField(fieldNode);
			JCExpression pType = cloneType(maker, field.vartype, source, typeNode.getContext());
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, NON_NULL_PATTERN);
			List<JCAnnotation> nullables = findAnnotations(fieldNode, NULLABLE_PATTERN);
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, nonNulls.appendList(nullables)), fieldName, pType, null);
			params.append(param);
			args.append(maker.Ident(fieldName));
		}
		JCReturn returnStatement = maker.Return(maker.NewClass(null, List.<JCExpression>nil(), constructorType, args.toList(), null));
		JCBlock body = maker.Block(0, List.<JCStatement>of(returnStatement));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName(name), returnType, typeParams.toList(), params.toList(), List.<JCExpression>nil(), body, null), source, typeNode.getContext());
	}
}
