/*
 * Copyright (C) 2015-2017 The Project Lombok Authors.
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

import lombok.core.LombokImmutableList;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

public class JavacSingularsRecipes {
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
		return singularizableTypes.toQualified(typeReference);
	}
	
	public JavacSingularizer getSingularizer(String fqn) {
		return singularizers.get(fqn);
	}
	
	public static final class SingularData {
		private final JavacNode annotation;
		private final Name singularName;
		private final Name pluralName;
		private final List<JCExpression> typeArgs;
		private final String targetFqn;
		private final JavacSingularizer singularizer;
		
		public SingularData(JavacNode annotation, Name singularName, Name pluralName, List<JCExpression> typeArgs, String targetFqn, JavacSingularizer singularizer) {
			this.annotation = annotation;
			this.singularName = singularName;
			this.pluralName = pluralName;
			this.typeArgs = typeArgs;
			this.targetFqn = targetFqn;
			this.singularizer = singularizer;
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
		
		public List<JCExpression> getTypeArgs() {
			return typeArgs;
		}
		
		public String getTargetFqn() {
			return targetFqn;
		}
		
		public JavacSingularizer getSingularizer() {
			return singularizer;
		}
		
		public String getTargetSimpleType() {
			int idx = targetFqn.lastIndexOf(".");
			return idx == -1 ? targetFqn : targetFqn.substring(idx + 1);
		}
	}
	
	public static abstract class JavacSingularizer {
		public abstract LombokImmutableList<String> getSupportedTypes();
		
		protected JCModifiers makeMods(JavacTreeMaker maker, JavacNode node, boolean deprecate) {
			if (deprecate) return maker.Modifiers(Flags.PUBLIC, List.<JCAnnotation>of(maker.Annotation(genJavaLangTypeRef(node, "Deprecated"), List.<JCExpression>nil())));
			return maker.Modifiers(Flags.PUBLIC);
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
		public abstract void generateMethods(SingularData data, boolean deprecate, JavacNode builderType, JCTree source, boolean fluent, boolean chain);
		public abstract void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName);
		
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
		
		/** Generates 'this.<em>name</em>.size()' as an expression; if nullGuard is true, it's this.name == null ? 0 : this.name.size(). */
		protected JCExpression getSize(JavacTreeMaker maker, JavacNode builderType, Name name, boolean nullGuard, boolean parens) {
			Name thisName = builderType.toName("this");
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
	}
}
