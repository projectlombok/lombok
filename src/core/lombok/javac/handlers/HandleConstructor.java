/*
 * Copyright (C) 2010-2024 The Project Lombok Authors.
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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.delombok.LombokOptionsFactory;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

public class HandleConstructor {
	@Provides
	public static class HandleNoArgsConstructor extends JavacAnnotationHandler<NoArgsConstructor> {
		private static final String NAME = NoArgsConstructor.class.getSimpleName();
		private HandleConstructor handleConstructor = new HandleConstructor();
		
		@Override public void handle(AnnotationValues<NoArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.NO_ARGS_CONSTRUCTOR_FLAG_USAGE, "@NoArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, NoArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NAME)) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@NoArgsConstructor(onConstructor", annotationNode);
			if (!onConstructor.isEmpty()) {
				handleFlagUsage(annotationNode, ConfigurationKeys.ON_X_FLAG_USAGE, "@NoArgsConstructor(onConstructor=...)");
			}
			NoArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			boolean force = ann.force();
			handleConstructor.generateConstructor(typeNode, level, onConstructor, List.<JavacNode>nil(), force, staticName, SkipIfConstructorExists.NO, annotationNode);
		}
	}
	
	@Provides
	public static class HandleRequiredArgsConstructor extends JavacAnnotationHandler<RequiredArgsConstructor> {
		private static final String NAME = RequiredArgsConstructor.class.getSimpleName();
		private HandleConstructor handleConstructor = new HandleConstructor();
		
		@Override public void handle(AnnotationValues<RequiredArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE, "@RequiredArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, RequiredArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NAME)) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@RequiredArgsConstructor(onConstructor", annotationNode);
			if (!onConstructor.isEmpty()) {
				handleFlagUsage(annotationNode, ConfigurationKeys.ON_X_FLAG_USAGE, "@RequiredArgsConstructor(onConstructor=...)");
			}
			RequiredArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			
			handleConstructor.generateConstructor(typeNode, level, onConstructor, findRequiredFields(typeNode), false, staticName, SkipIfConstructorExists.NO, annotationNode);
		}
	}
	
	@Provides
	public static class HandleAllArgsConstructor extends JavacAnnotationHandler<AllArgsConstructor> {
		private static final String NAME = AllArgsConstructor.class.getSimpleName();
		private HandleConstructor handleConstructor = new HandleConstructor();
		
		@Override public void handle(AnnotationValues<AllArgsConstructor> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.ALL_ARGS_CONSTRUCTOR_FLAG_USAGE, "@AllArgsConstructor", ConfigurationKeys.ANY_CONSTRUCTOR_FLAG_USAGE, "any @xArgsConstructor");
			
			deleteAnnotationIfNeccessary(annotationNode, AllArgsConstructor.class);
			deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
			JavacNode typeNode = annotationNode.up();
			if (!checkLegality(typeNode, annotationNode, NAME)) return;
			List<JCAnnotation> onConstructor = unboxAndRemoveAnnotationParameter(ast, "onConstructor", "@AllArgsConstructor(onConstructor", annotationNode);
			if (!onConstructor.isEmpty()) {
				handleFlagUsage(annotationNode, ConfigurationKeys.ON_X_FLAG_USAGE, "@AllArgsConstructor(onConstructor=...)");
			}
			AllArgsConstructor ann = annotation.getInstance();
			AccessLevel level = ann.access();
			if (level == AccessLevel.NONE) return;
			String staticName = ann.staticName();
			if (annotation.isExplicit("suppressConstructorProperties")) {
				annotationNode.addError("This deprecated feature is no longer supported. Remove it; you can create a lombok.config file with 'lombok.anyConstructor.suppressConstructorProperties = true'.");
			}
			handleConstructor.generateConstructor(typeNode, level, onConstructor, findAllFields(typeNode), false, staticName, SkipIfConstructorExists.NO, annotationNode);
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
			boolean isNonNull = nullMarked && hasNonNullAnnotations(child);
			if ((isFinal || isNonNull) && fieldDecl.init == null) fields.append(child);
		}
		return fields.toList();
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
		if (!isClassOrEnum(typeNode)) {
			errorNode.addError(name + " is only supported on a class or an enum.");
			return false;
		}
		
		return true;
	}
	
	public enum SkipIfConstructorExists {
		YES, NO, I_AM_BUILDER;
	}
	
	public void generateExtraNoArgsConstructor(JavacNode typeNode, JavacNode source) {
		if (!isDirectDescendantOfObject(typeNode)) return;
		
		Boolean v = typeNode.getAst().readConfiguration(ConfigurationKeys.NO_ARGS_CONSTRUCTOR_EXTRA_PRIVATE);
		if (v == null || !v) return;
		
		generate(typeNode, AccessLevel.PRIVATE, List.<JCAnnotation>nil(), List.<JavacNode>nil(), true, null, SkipIfConstructorExists.NO, source, true);
	}
	
	public void generateRequiredArgsConstructor(JavacNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findRequiredFields(typeNode), false, staticName, skipIfConstructorExists, source);
	}
	
	public void generateAllArgsConstructor(JavacNode typeNode, AccessLevel level, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generateConstructor(typeNode, level, List.<JCAnnotation>nil(), findAllFields(typeNode), false, staticName, skipIfConstructorExists, source);
	}
	
	public void generateConstructor(JavacNode typeNode, AccessLevel level, List<JCAnnotation> onConstructor, List<JavacNode> fields, boolean allToDefault, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source) {
		generate(typeNode, level, onConstructor, fields, allToDefault, staticName, skipIfConstructorExists, source, false);
	}

	private void generate(JavacNode typeNode, AccessLevel level, List<JCAnnotation> onConstructor, List<JavacNode> fields, boolean allToDefault, String staticName, SkipIfConstructorExists skipIfConstructorExists, JavacNode source, boolean noArgs) {
		boolean staticConstrRequired = staticName != null && !staticName.equals("");
		
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
		
		if (noArgs && noArgsConstructorExists(typeNode)) return;
		
		if (!(skipIfConstructorExists != SkipIfConstructorExists.NO && constructorExists(typeNode) != MemberExistsResult.NOT_EXISTS)) {
			JCMethodDecl constr = createConstructor(staticConstrRequired ? AccessLevel.PRIVATE : level, onConstructor, typeNode, fields, allToDefault, source);
			generateConstructorJavadoc(constr, typeNode, fields);
			injectMethod(typeNode, constr);
		}
		generateStaticConstructor(staticConstrRequired, typeNode, staticName, level, allToDefault, fields, source);
	}
	
	private void generateStaticConstructor(boolean staticConstrRequired, JavacNode typeNode, String staticName, AccessLevel level, boolean allToDefault, List<JavacNode> fields, JavacNode source) {
		if (staticConstrRequired) {
			JCMethodDecl staticConstr = createStaticConstructor(staticName, level, typeNode, allToDefault ? List.<JavacNode>nil() : fields, source);
			generateConstructorJavadoc(staticConstr, typeNode, fields);
			injectMethod(typeNode, staticConstr);
		}
	}
	
	private static boolean noArgsConstructorExists(JavacNode node) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl) node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					if (md.name.contentEquals("<init>") && md.params.size() == 0) return true;
				}
			}
		}
		
		for (JavacNode child : node.down()) {
			if (annotationTypeMatches(NoArgsConstructor.class, child)) return true;
			if (annotationTypeMatches(RequiredArgsConstructor.class, child) && findRequiredFields(node).isEmpty()) return true;
			if (annotationTypeMatches(AllArgsConstructor.class, child) && findAllFields(node).isEmpty()) return true;
		}
		
		return false;
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
	
	@SuppressWarnings("deprecation") public static JCMethodDecl createConstructor(AccessLevel level, List<JCAnnotation> onConstructor, JavacNode typeNode, List<JavacNode> fieldsToParam, boolean forceDefaults, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		boolean isEnum = (((JCClassDecl) typeNode.get()).mods.flags & Flags.ENUM) != 0;
		if (isEnum) level = AccessLevel.PRIVATE;
		
		boolean addConstructorProperties;
		
		List<JavacNode> fieldsToDefault = fieldsNeedingBuilderDefaults(typeNode, fieldsToParam);
		List<JavacNode> fieldsToExplicit = forceDefaults ? fieldsNeedingExplicitDefaults(typeNode, fieldsToParam) : List.<JavacNode>nil();
		
		if (fieldsToParam.isEmpty()) {
			addConstructorProperties = false;
		} else {
			Boolean v = typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES);
			addConstructorProperties = v != null ? v.booleanValue() :
				Boolean.FALSE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}
		
		ListBuffer<JCStatement> nullChecks = new ListBuffer<JCStatement>();
		ListBuffer<JCStatement> assigns = new ListBuffer<JCStatement>();
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		
		for (JavacNode fieldNode : fieldsToParam) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			Name fieldName = removePrefixFromField(fieldNode);
			Name rawName = field.name;
			List<JCAnnotation> copyableAnnotations = findCopyableAnnotations(fieldNode);
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCExpression pType = cloneType(fieldNode.getTreeMaker(), field.vartype, source);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, copyableAnnotations), fieldName, pType, null);
			params.append(param);
			if (hasNonNullAnnotations(fieldNode)) {
				JCStatement nullCheck = generateNullCheck(maker, param, source);
				if (nullCheck != null) nullChecks.append(nullCheck);
			}
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), rawName);
			JCExpression assign = maker.Assign(thisX, maker.Ident(fieldName));
			assigns.append(maker.Exec(assign));
		}
		
		for (JavacNode fieldNode : fieldsToExplicit) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			Name rawName = field.name;
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), rawName);
			JCExpression assign = maker.Assign(thisX, getDefaultExpr(maker, field.vartype));
			assigns.append(maker.Exec(assign));
		}
		
		for (JavacNode fieldNode : fieldsToDefault) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			Name rawName = field.name;
			Name fieldName = removePrefixFromField(fieldNode);
			Name nameOfDefaultProvider = typeNode.toName("$default$" + fieldName);
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), rawName);
			JCExpression assign = maker.Assign(thisX, maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(((JCClassDecl) typeNode.get()).name), nameOfDefaultProvider), List.<JCExpression>nil()));
			assigns.append(maker.Exec(assign));
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		if (addConstructorProperties && !isLocalType(typeNode) && LombokOptionsFactory.getDelombokOptions(typeNode.getContext()).getFormatPreferences().generateConstructorProperties()) {
			addConstructorProperties(mods, typeNode, fieldsToParam);
		}
		if (onConstructor != null) mods.annotations = mods.annotations.appendList(copyAnnotations(onConstructor));
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, nullChecks.appendList(assigns).toList()), null), source);
	}
	
	/**
	 * For each field which is not final and has no initializer that gets 'removed' by {@code @Builder.Default} there is no need to
	 * write an explicit 'this.x = foo' in the constructor, so strip them away here.
	 */
	private static List<JavacNode> fieldsNeedingBuilderDefaults(JavacNode typeNode, List<JavacNode> fieldsToParam) {
		ListBuffer<JavacNode> out = new ListBuffer<JavacNode>();
		top:
		for (JavacNode node : typeNode.down()) {
			if (node.getKind() != Kind.FIELD) continue top;
			JCVariableDecl varDecl = (JCVariableDecl) node.get();
			if ((varDecl.mods.flags & Flags.STATIC) != 0) continue top;
			for (JavacNode ftp : fieldsToParam) if (node == ftp) continue top;
			if (JavacHandlerUtil.hasAnnotation(Builder.Default.class, node)) out.append(node);
		}
		return out.toList();
	}
	
	/**
	 * Return each field which is final and has no initializer, and which is not already a parameter.
	 */
	private static List<JavacNode> fieldsNeedingExplicitDefaults(JavacNode typeNode, List<JavacNode> fieldsToParam) {
		ListBuffer<JavacNode> out = new ListBuffer<JavacNode>();
		top:
		for (JavacNode node : typeNode.down()) {
			if (node.getKind() != Kind.FIELD) continue top;
			JCVariableDecl varDecl = (JCVariableDecl) node.get();
			if (varDecl.init != null) continue top;
			if ((varDecl.mods.flags & Flags.FINAL) == 0) continue top;
			if ((varDecl.mods.flags & Flags.STATIC) != 0) continue top;
			for (JavacNode ftp : fieldsToParam) if (node == ftp) continue top;
			if (JavacHandlerUtil.hasAnnotation(Builder.Default.class, node)) continue top;
			out.append(node);
		}
		return out.toList();
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
	
	public JCMethodDecl createStaticConstructor(String name, AccessLevel level, JavacNode typeNode, List<JavacNode> fields, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.STATIC | toJavacModifier(level));
		
		JCExpression returnType, constructorType;
		
		ListBuffer<JCTypeParameter> typeParams = new ListBuffer<JCTypeParameter>();
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
		
		if (!type.typarams.isEmpty()) {
			for (JCTypeParameter param : type.typarams) {
				typeParams.append(maker.TypeParameter(param.name, cloneTypes(maker, param.bounds, source)));
			}
		}
		List<JCAnnotation> annsOnReturnType = List.nil();
		if (getCheckerFrameworkVersion(typeNode).generateUnique()) annsOnReturnType = List.of(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__UNIQUE), List.<JCExpression>nil()));
		returnType = namePlusTypeParamsToTypeReference(maker, typeNode, type.typarams, annsOnReturnType);
		constructorType = namePlusTypeParamsToTypeReference(maker, typeNode, type.typarams);
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			Name fieldName = removePrefixFromField(fieldNode);
			JCExpression pType = cloneType(maker, field.vartype, source);
			List<JCAnnotation> copyableAnnotations = findCopyableAnnotations(fieldNode);
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, copyableAnnotations), fieldName, pType, null);
			params.append(param);
			args.append(maker.Ident(fieldName));
		}
		JCReturn returnStatement = maker.Return(maker.NewClass(null, List.<JCExpression>nil(), constructorType, args.toList(), null));
		JCBlock body = maker.Block(0, List.<JCStatement>of(returnStatement));
		
		JCMethodDecl methodDef = maker.MethodDef(mods, typeNode.toName(name), returnType, typeParams.toList(), params.toList(), List.<JCExpression>nil(), body, null);
		createRelevantNonNullAnnotation(typeNode, methodDef);
		return recursiveSetGeneratedBy(methodDef, source);
	}
	
	private void generateConstructorJavadoc(JCMethodDecl constructor, JavacNode typeNode, List<JavacNode> fields) {
		if (fields.isEmpty()) return;
		
		JCCompilationUnit cu = ((JCCompilationUnit) typeNode.top().get());
		String constructorJavadoc = getConstructorJavadocHeader(typeNode.getName());
		boolean fieldDescriptionAdded = false;
		for (JavacNode fieldNode : fields) {
			String paramName = removePrefixFromField(fieldNode).toString();
			String fieldJavadoc = getDocComment(cu, fieldNode.get());
			String paramJavadoc = getConstructorParameterJavadoc(paramName, fieldJavadoc);
			
			if (paramJavadoc == null) {
				paramJavadoc = "@param " + paramName;
			} else {
				fieldDescriptionAdded = true;
			}
			
			constructorJavadoc = addJavadocLine(constructorJavadoc, paramJavadoc);
		}
		if (fieldDescriptionAdded) {
			setDocComment(cu, constructor, constructorJavadoc);
		}
	}
}
