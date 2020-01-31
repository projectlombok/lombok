/*
 * Copyright (C) 2015-2020 The Project Lombok Authors.
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

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.LombokImmutableList;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

public class JavacSingularsRecipes {
	public interface ExpressionMaker {
		JCExpression make();
	}
	
	public interface StatementMaker {
		JCStatement make();
	}
	
	private static final JavacSingularsRecipes INSTANCE = new JavacSingularsRecipes();
	private final Map<String, JavacSingularizer> singularizers = new HashMap<String, JavacSingularizer>();
	private final TypeLibrary singularizableTypes = new TypeLibrary();
	
	private JavacSingularsRecipes() {
		try {
			loadAll(singularizableTypes, singularizers);
			singularizableTypes.lock();
		} catch (IOException e) {
			System.err.println("Lombok's @Singularizable feature is broken due to misconfigured SPI files: " + e);
		}
	}
	
	private static void loadAll(TypeLibrary library, Map<String, JavacSingularizer> map) throws IOException {
		for (JavacSingularizer handler : SpiLoadUtil.findServices(JavacSingularizer.class, JavacSingularizer.class.getClassLoader())) {
			for (String type : handler.getSupportedTypes()) {
				JavacSingularizer existingSingularizer = map.get(type);
				if (existingSingularizer != null) {
					JavacSingularizer toKeep = existingSingularizer.getClass().getName().compareTo(handler.getClass().getName()) > 0 ? handler : existingSingularizer;
					System.err.println("Multiple singularizers found for type " + type + "; the alphabetically first class is used: " + toKeep.getClass().getName());
					map.put(type, toKeep);
				} else {
					map.put(type, handler);
					library.addType(type);
				}
			}
		}
	}
	
	public static JavacSingularsRecipes get() {
		return INSTANCE;
	}
	
	public String toQualified(String typeReference) {
		java.util.List<String> q = singularizableTypes.toQualifieds(typeReference);
		if (q.isEmpty()) return null;
		return q.get(0);
	}
	
	public JavacSingularizer getSingularizer(String fqn, JavacNode node) {
		final JavacSingularizer singularizer = singularizers.get(fqn);
		final boolean useGuavaInstead = Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_USE_GUAVA));
		return useGuavaInstead ? singularizer.getGuavaInstead(node) : singularizer;
	}
	
	public static final class SingularData {
		private final JavacNode annotation;
		private final Name singularName;
		private final Name pluralName;
		private final List<JCExpression> typeArgs;
		private final String targetFqn;
		private final JavacSingularizer singularizer;
		private final String setterPrefix;
		private final boolean ignoreNullCollections;
		
		public SingularData(JavacNode annotation, Name singularName, Name pluralName, List<JCExpression> typeArgs, String targetFqn, JavacSingularizer singularizer, boolean ignoreNullCollections) {
			this(annotation, singularName, pluralName, typeArgs, targetFqn, singularizer, ignoreNullCollections, "");
		}
		
		public SingularData(JavacNode annotation, Name singularName, Name pluralName, List<JCExpression> typeArgs, String targetFqn, JavacSingularizer singularizer, boolean ignoreNullCollections, String setterPrefix) {
			this.annotation = annotation;
			this.singularName = singularName;
			this.pluralName = pluralName;
			this.typeArgs = typeArgs;
			this.targetFqn = targetFqn;
			this.singularizer = singularizer;
			this.setterPrefix = setterPrefix;
			this.ignoreNullCollections = ignoreNullCollections;
		}
		
		public JavacNode getAnnotation() {
			return annotation;
		}
		
		public Name getSingularName() {
			return singularName;
		}
		
		public Name getPluralName() {
			return pluralName;
		}
		
		public String getSetterPrefix() {
			return setterPrefix;
		}
		
		public List<JCExpression> getTypeArgs() {
			return typeArgs;
		}
		
		public String getTargetFqn() {
			return targetFqn;
		}
		
		public JavacSingularizer getSingularizer() {
			return singularizer;
		}
		
		public boolean isIgnoreNullCollections() {
			return ignoreNullCollections;
		}
		
		public String getTargetSimpleType() {
			int idx = targetFqn.lastIndexOf(".");
			return idx == -1 ? targetFqn : targetFqn.substring(idx + 1);
		}
	}
	
	public static abstract class JavacSingularizer {
		public abstract LombokImmutableList<String> getSupportedTypes();
		
		protected JavacSingularizer getGuavaInstead(JavacNode node) {
			return this;
		}
		
		protected JCModifiers makeMods(JavacTreeMaker maker, CheckerFrameworkVersion cfv, JavacNode node, boolean deprecate, AccessLevel access) {
			JCAnnotation deprecateAnn = deprecate ? maker.Annotation(genJavaLangTypeRef(node, "Deprecated"), List.<JCExpression>nil()) : null;
			JCAnnotation rrAnn = cfv.generateReturnsReceiver() ? maker.Annotation(genTypeRef(node, CheckerFrameworkVersion.NAME__RETURNS_RECEIVER), List.<JCExpression>nil()) : null;
			
			List<JCAnnotation> annsOnMethod = (deprecateAnn != null && rrAnn != null) ? List.of(deprecateAnn, rrAnn) : deprecateAnn != null ? List.of(deprecateAnn) : rrAnn != null ? List.of(rrAnn) : List.<JCAnnotation>nil();
			return maker.Modifiers(toJavacModifier(access), annsOnMethod);
		}
		
		/** Checks if any of the to-be-generated nodes (fields, methods) already exist. If so, errors on these (singulars don't support manually writing some of it, and returns true). */
		public boolean checkForAlreadyExistingNodesAndGenerateError(JavacNode builderType, SingularData data) {
			for (JavacNode child : builderType.down()) {
				switch (child.getKind()) {
				case FIELD: {
					JCVariableDecl field = (JCVariableDecl) child.get();
					Name name = field.name;
					if (name == null) break;
					if (getGeneratedBy(field) != null) continue;
					for (Name fieldToBeGenerated : listFieldsToBeGenerated(data, builderType)) {
						if (!fieldToBeGenerated.equals(name)) continue;
						child.addError("Manually adding a field that @Singular @Builder would generate is not supported. If you want to manually manage the builder aspect for this field/parameter, don't use @Singular.");
						return true;
					}
					break;
				}
				case METHOD: {
					JCMethodDecl method = (JCMethodDecl) child.get();
					Name name = method.name;
					if (name == null) break;
					if (getGeneratedBy(method) != null) continue;
					for (Name methodToBeGenerated : listMethodsToBeGenerated(data, builderType)) {
						if (!methodToBeGenerated.equals(name)) continue;
						child.addError("Manually adding a method that @Singular @Builder would generate is not supported. If you want to manually manage the builder aspect for this field/parameter, don't use @Singular.");
						return true;
					}
					break;
				}}
			}
			
			return false;
		}
		
		public java.util.List<Name> listFieldsToBeGenerated(SingularData data, JavacNode builderType) {
			return Collections.singletonList(data.pluralName);
		}
		
		public java.util.List<Name> listMethodsToBeGenerated(SingularData data, JavacNode builderType) {
			Name p = data.pluralName;
			Name s = data.singularName;
			if (p.equals(s)) return Collections.singletonList(p);
			return Arrays.asList(p, s);
		}
		
		public abstract java.util.List<JavacNode> generateFields(SingularData data, JavacNode builderType, JCTree source);
		
		/**
		 * Generates the singular, plural, and clear methods for the given {@link SingularData}.
		 * Uses the given {@code builderType} as return type if {@code chain == true}, {@code void} otherwise.
		 * If you need more control over the return type and value, use
		 * {@link #generateMethods(SingularData, boolean, JavacNode, JCTree, boolean, ExpressionMaker, StatementMaker)}.
		 */
		public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, final JavacNode builderType, JCTree source, boolean fluent, final boolean chain, AccessLevel access) {
			final JavacTreeMaker maker = builderType.getTreeMaker();
			
			ExpressionMaker returnTypeMaker = new ExpressionMaker() { @Override public JCExpression make() {
				return chain ? 
					cloneSelfType(builderType) : 
					maker.Type(createVoidType(builderType.getSymbolTable(), CTC_VOID));
			}};
			
			StatementMaker returnStatementMaker = new StatementMaker() { @Override public JCStatement make() {
				return chain ? maker.Return(maker.Ident(builderType.toName("this"))) : null;
			}};
			
			generateMethods(cfv, data, deprecate, builderType, source, fluent, returnTypeMaker, returnStatementMaker, access);
		}
		
		/**
		 * Generates the singular, plural, and clear methods for the given {@link SingularData}.
		 * Uses the given {@code returnTypeMaker} and {@code returnStatementMaker} for the generated methods.
		 */
		public abstract void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access);
		
		protected void doGenerateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, ExpressionMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
			JavacTreeMaker maker = builderType.getTreeMaker();
			generateSingularMethod(cfv, deprecate, maker, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, source, fluent, access);
			generatePluralMethod(cfv, deprecate, maker, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, source, fluent, access);
			generateClearMethod(cfv, deprecate, maker, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, source, access);
		}
		
		private void finishAndInjectMethod(CheckerFrameworkVersion cfv, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean deprecate, ListBuffer<JCStatement> statements, Name methodName, List<JCVariableDecl> jcVariableDecls, AccessLevel access, Boolean ignoreNullCollections) {
			if (returnStatement != null) statements.append(returnStatement);
			JCBlock body = maker.Block(0, statements.toList());
			JCModifiers mods = makeMods(maker, cfv, builderType, deprecate, access);
			List<JCTypeParameter> typeParams = List.nil();
			List<JCExpression> thrown = List.nil();
			
			if (ignoreNullCollections != null) {
				if (ignoreNullCollections.booleanValue()) {
					for (JCVariableDecl d : jcVariableDecls) createRelevantNullableAnnotation(builderType, d);
				} else {
					for (JCVariableDecl d : jcVariableDecls) createRelevantNonNullAnnotation(builderType, d);
				}
			}
			
			JCMethodDecl method = maker.MethodDef(mods, methodName, returnType, typeParams, jcVariableDecls, thrown, body, null);
			recursiveSetGeneratedBy(method, source, builderType.getContext());
			if (returnStatement != null) createRelevantNonNullAnnotation(builderType, method);
			injectMethod(builderType, method);
		}
		
		private void generateClearMethod(CheckerFrameworkVersion cfv, boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, AccessLevel access) {
			JCStatement clearStatement = generateClearStatements(maker, data, builderType);
			ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
			statements.add(clearStatement);
			
			Name methodName = builderType.toName(HandlerUtil.buildAccessorName("clear", data.getPluralName().toString()));
			finishAndInjectMethod(cfv, maker, returnType, returnStatement, data, builderType, source, deprecate, statements, methodName, List.<JCVariableDecl>nil(), access, null);
		}
		
		protected abstract JCStatement generateClearStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType);
		
		private void generateSingularMethod(CheckerFrameworkVersion cfv, boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent, AccessLevel access) {
			ListBuffer<JCStatement> statements = generateSingularMethodStatements(maker, data, builderType, source);
			List<JCVariableDecl> params = generateSingularMethodParameters(maker, data, builderType, source);
			Name name = data.getSingularName();
			String setterPrefix = data.getSetterPrefix();
			if (setterPrefix.isEmpty() && !fluent) setterPrefix = getAddMethodName();
			if (!setterPrefix.isEmpty()) name = builderType.toName(HandlerUtil.buildAccessorName(setterPrefix, name.toString()));
			
			statements.prepend(createConstructBuilderVarIfNeeded(maker, data, builderType, source));
			finishAndInjectMethod(cfv, maker, returnType, returnStatement, data, builderType, source, deprecate, statements, name, params, access, null);
		}
		
		protected JCVariableDecl generateSingularMethodParameter(int typeIndex, JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source, Name name) {
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
			JCExpression type = cloneParamType(typeIndex, maker, data.getTypeArgs(), builderType, source);
			List<JCAnnotation> typeUseAnns = getTypeUseAnnotations(type);
			type = removeTypeUseAnnotations(type);
			JCModifiers mods = typeUseAnns.isEmpty() ? maker.Modifiers(flags) : maker.Modifiers(flags, typeUseAnns);
			return maker.VarDef(mods, name, type, null);
		}
		
		protected JCStatement generateSingularMethodAddStatement(JavacTreeMaker maker, JavacNode builderType, Name argumentName, String builderFieldName) {
			JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", builderFieldName, "add");
			JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, List.<JCExpression>of(maker.Ident(argumentName)));
			return maker.Exec(invokeAdd);
		}
		
		protected abstract ListBuffer<JCStatement> generateSingularMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source);
		
		protected abstract List<JCVariableDecl> generateSingularMethodParameters(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source);
		
		private void generatePluralMethod(CheckerFrameworkVersion cfv, boolean deprecate, JavacTreeMaker maker, JCExpression returnType, JCStatement returnStatement, SingularData data, JavacNode builderType, JCTree source, boolean fluent, AccessLevel access) {
			ListBuffer<JCStatement> statements = generatePluralMethodStatements(maker, data, builderType, source);
			
			Name name = data.getPluralName();
			String setterPrefix = data.getSetterPrefix();
			if (setterPrefix.isEmpty() && !fluent) setterPrefix = getAddMethodName() + "All";
			if (!setterPrefix.isEmpty()) name = builderType.toName(HandlerUtil.buildAccessorName(setterPrefix, name.toString()));
			JCExpression paramType = getPluralMethodParamType(builderType);
			paramType = addTypeArgs(getTypeArgumentsCount(), true, builderType, paramType, data.getTypeArgs(), source);
			long paramFlags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, builderType.getContext());
			boolean ignoreNullCollections = data.isIgnoreNullCollections();
			JCModifiers paramMods = maker.Modifiers(paramFlags);
			JCVariableDecl param = maker.VarDef(paramMods, data.getPluralName(), paramType, null);
			statements.prepend(createConstructBuilderVarIfNeeded(maker, data, builderType, source));
			
			if (ignoreNullCollections) {
				JCExpression incomingIsNotNull = maker.Binary(CTC_NOT_EQUAL, maker.Ident(data.getPluralName()), maker.Literal(CTC_BOT, null));
				JCStatement onNotNull = maker.Block(0, statements.toList());
				statements = new ListBuffer<JCStatement>();
				statements.add(maker.If(incomingIsNotNull, onNotNull, null));
			} else {
				statements.prepend(JavacHandlerUtil.generateNullCheck(maker, null, data.getPluralName(), builderType, "%s cannot be null"));
			}
			
			finishAndInjectMethod(cfv, maker, returnType, returnStatement, data, builderType, source, deprecate, statements, name, List.of(param), access, ignoreNullCollections);
		}
		
		protected ListBuffer<JCStatement> generatePluralMethodStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source) {
			ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
			
			JCExpression thisDotFieldDotAdd = chainDots(builderType, "this", data.getPluralName().toString(), getAddMethodName() + "All");
			JCExpression invokeAdd = maker.Apply(List.<JCExpression>nil(), thisDotFieldDotAdd, List.<JCExpression>of(maker.Ident(data.getPluralName())));
			statements.append(maker.Exec(invokeAdd));
			
			return statements;
		}
		
		protected abstract JCExpression getPluralMethodParamType(JavacNode builderType);
		
		protected abstract JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, JCTree source);
		
		public abstract void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName, String builderVariable);
		
		public boolean shadowedDuringBuild() {
			return true;
		}
		
		public boolean requiresCleaning() {
			try {
				return !getClass().getMethod("appendCleaningCode", SingularData.class, JavacNode.class, JCTree.class, ListBuffer.class).getDeclaringClass().equals(JavacSingularizer.class);
			} catch (NoSuchMethodException e) {
				return false;
			}
		}
		
		public void appendCleaningCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements) {
		}
		
		// -- Utility methods --
		
		/**
		 * Adds the requested number of type arguments to the provided type, copying each argument in {@code typeArgs}. If typeArgs is too long, the extra elements are ignored.
		 * If {@code typeArgs} is null or too short, {@code java.lang.Object} will be substituted for each missing type argument.
		 * 
		 * @param count The number of type arguments requested.
		 * @param addExtends If {@code true}, all bounds are either '? extends X' or just '?'. If false, the reverse is applied, and '? extends Foo' is converted to Foo, '?' to Object, etc.
		 * @param node Some node in the same AST. Just used to obtain makers and contexts and such.
		 * @param type The type to add generics to.
		 * @param typeArgs the list of type args to clone.
		 * @param source The source annotation that is the root cause of this code generation.
		 */
		protected JCExpression addTypeArgs(int count, boolean addExtends, JavacNode node, JCExpression type, List<JCExpression> typeArgs, JCTree source) {
			JavacTreeMaker maker = node.getTreeMaker();
			List<JCExpression> clonedAndFixedTypeArgs = createTypeArgs(count, addExtends, node, typeArgs, source);
			
			return maker.TypeApply(type, clonedAndFixedTypeArgs);
		}
		
		protected List<JCExpression> createTypeArgs(int count, boolean addExtends, JavacNode node, List<JCExpression> typeArgs, JCTree source) {
			JavacTreeMaker maker = node.getTreeMaker();
			Context context = node.getContext();
			
			if (count < 0) throw new IllegalArgumentException("count is negative");
			if (count == 0) return List.nil();
			ListBuffer<JCExpression> arguments = new ListBuffer<JCExpression>();
			
			if (typeArgs != null) for (JCExpression orig : typeArgs) {
				if (!addExtends) {
					if (orig.getKind() == Kind.UNBOUNDED_WILDCARD || orig.getKind() == Kind.SUPER_WILDCARD) {
						arguments.append(genJavaLangTypeRef(node, "Object"));
					} else if (orig.getKind() == Kind.EXTENDS_WILDCARD) {
						JCExpression inner;
						try {
							inner = (JCExpression) ((JCWildcard) orig).inner;
						} catch (Exception e) {
							inner = genJavaLangTypeRef(node, "Object");
						}
						arguments.append(cloneType(maker, inner, source, context));
					} else {
						arguments.append(cloneType(maker, orig, source, context));
					}
				} else {
					if (orig.getKind() == Kind.UNBOUNDED_WILDCARD || orig.getKind() == Kind.SUPER_WILDCARD) {
						arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
					} else if (orig.getKind() == Kind.EXTENDS_WILDCARD) {
						arguments.append(cloneType(maker, orig, source, context));
					} else {
						arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), cloneType(maker, orig, source, context)));
					}
				}
				if (--count == 0) break;
			}
			
			while (count-- > 0) {
				if (addExtends) {
					arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
				} else {
					arguments.append(genJavaLangTypeRef(node, "Object"));
				}
			}
			
			return arguments.toList();
		}
		
		/** Generates '<em>builderVariable</em>.<em>name</em>.size()' as an expression; if nullGuard is true, it's this.name == null ? 0 : this.name.size(). */
		protected JCExpression getSize(JavacTreeMaker maker, JavacNode builderType, Name name, boolean nullGuard, boolean parens, String builderVariable) {
			Name thisName = builderType.toName(builderVariable);
			JCExpression fn = maker.Select(maker.Select(maker.Ident(thisName), name), builderType.toName("size"));
			JCExpression sizeInvoke = maker.Apply(List.<JCExpression>nil(), fn, List.<JCExpression>nil());
			if (nullGuard) {
				JCExpression isNull = maker.Binary(CTC_EQUAL, maker.Select(maker.Ident(thisName), name), maker.Literal(CTC_BOT, 0));
				JCExpression out = maker.Conditional(isNull, maker.Literal(CTC_INT, 0), sizeInvoke);
				if (parens) return maker.Parens(out);
				return out;
			}
			return sizeInvoke;
		}
		
		protected JCExpression cloneParamType(int index, JavacTreeMaker maker, List<JCExpression> typeArgs, JavacNode builderType, JCTree source) {
			if (typeArgs == null || typeArgs.size() <= index) {
				return genJavaLangTypeRef(builderType, "Object");
			} else {
				JCExpression originalType = typeArgs.get(index);
				if (originalType.getKind() == Kind.UNBOUNDED_WILDCARD || originalType.getKind() == Kind.SUPER_WILDCARD) {
					return genJavaLangTypeRef(builderType, "Object");
				} else if (originalType.getKind() == Kind.EXTENDS_WILDCARD) {
					try {
						return cloneType(maker, (JCExpression) ((JCWildcard) originalType).inner, source, builderType.getContext());
					} catch (Exception e) {
						return genJavaLangTypeRef(builderType, "Object");
					}
				} else {
					return cloneType(maker, originalType, source, builderType.getContext());
				}
			}
		}
		
		protected abstract String getAddMethodName();
		
		protected abstract int getTypeArgumentsCount();
		
		protected abstract String getEmptyMaker(String target);
	}
}
