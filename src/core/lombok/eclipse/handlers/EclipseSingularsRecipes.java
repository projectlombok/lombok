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
package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import lombok.core.LombokImmutableList;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.eclipse.EclipseNode;

public class EclipseSingularsRecipes {
	private static final EclipseSingularsRecipes INSTANCE = new EclipseSingularsRecipes();
	private final Map<String, EclipseSingularizer> singularizers = new HashMap<String, EclipseSingularizer>();
	private final TypeLibrary singularizableTypes = new TypeLibrary();
	
	private EclipseSingularsRecipes() {
		try {
			loadAll(singularizableTypes, singularizers);
			singularizableTypes.lock();
		} catch (IOException e) {
			System.err.println("Lombok's @Singularizable feature is broken due to misconfigured SPI files: " + e);
		}
	}
	
	private static void loadAll(TypeLibrary library, Map<String, EclipseSingularizer> map) throws IOException {
		for (EclipseSingularizer handler : SpiLoadUtil.findServices(EclipseSingularizer.class, EclipseSingularizer.class.getClassLoader())) {
			for (String type : handler.getSupportedTypes()) {
				EclipseSingularizer existingSingularizer = map.get(type);
				if (existingSingularizer != null) {
					EclipseSingularizer toKeep = existingSingularizer.getClass().getName().compareTo(handler.getClass().getName()) > 0 ? handler : existingSingularizer;
					System.err.println("Multiple singularizers found for type " + type + "; the alphabetically first class is used: " + toKeep.getClass().getName());
					map.put(type, toKeep);
				} else {
					map.put(type, handler);
					library.addType(type);
				}
			}
		}
	}
	
	public static EclipseSingularsRecipes get() {
		return INSTANCE;
	}
	
	public String toQualified(String typeReference) {
		return singularizableTypes.toQualified(typeReference);
	}
	
	public EclipseSingularizer getSingularizer(String fqn) {
		return singularizers.get(fqn);
	}
	
	public static final class SingularData {
		private final EclipseNode annotation;
		private final char[] singularName;
		private final char[] pluralName;
		private final List<TypeReference> typeArgs;
		private final String targetFqn;
		private final EclipseSingularizer singularizer;
		private final ASTNode source;
		
		public SingularData(EclipseNode annotation, char[] singularName, char[] pluralName, List<TypeReference> typeArgs, String targetFqn, EclipseSingularizer singularizer, ASTNode source) {
			this.annotation = annotation;
			this.singularName = singularName;
			this.pluralName = pluralName;
			this.typeArgs = typeArgs;
			this.targetFqn = targetFqn;
			this.singularizer = singularizer;
			this.source = source;
		}
		
		public void setGeneratedByRecursive(ASTNode target) {
			SetGeneratedByVisitor visitor = new SetGeneratedByVisitor(source);
			
			if (target instanceof AbstractMethodDeclaration) {
				((AbstractMethodDeclaration) target).traverse(visitor, (ClassScope) null);
			} else if (target instanceof FieldDeclaration) {
				((FieldDeclaration) target).traverse(visitor, (MethodScope) null);
			} else {
				target.traverse(visitor, null);
			}
		}
		
		public ASTNode getSource() {
			return source;
		}
		
		public EclipseNode getAnnotation() {
			return annotation;
		}
		
		public char[] getSingularName() {
			return singularName;
		}
		
		public char[] getPluralName() {
			return pluralName;
		}
		
		public List<TypeReference> getTypeArgs() {
			return typeArgs;
		}
		
		public String getTargetFqn() {
			return targetFqn;
		}
		
		public EclipseSingularizer getSingularizer() {
			return singularizer;
		}
		
		public String getTargetSimpleType() {
			int idx = targetFqn.lastIndexOf(".");
			return idx == -1 ? targetFqn : targetFqn.substring(idx + 1);
		}
	}
	
	public static abstract class EclipseSingularizer {
		protected static final long[] NULL_POSS = {0L};
		public abstract LombokImmutableList<String> getSupportedTypes();
		
		/** Checks if any of the to-be-generated nodes (fields, methods) already exist. If so, errors on these (singulars don't support manually writing some of it, and returns true). */
		public boolean checkForAlreadyExistingNodesAndGenerateError(EclipseNode builderType, SingularData data) {
			for (EclipseNode child : builderType.down()) {
				switch (child.getKind()) {
				case FIELD: {
					FieldDeclaration fd = (FieldDeclaration) child.get();
					char[] name = fd.name;
					if (name == null) continue;
					if (getGeneratedBy(fd) != null) continue;
					for (char[] fieldToBeGenerated : listFieldsToBeGenerated(data, builderType)) {
						if (!Arrays.equals(name, fieldToBeGenerated)) continue;
						child.addError("Manually adding a field that @Singular @Builder would generate is not supported. If you want to manually manage the builder aspect for this field/parameter, don't use @Singular.");
						return true;
					}
					break;
				}
				case METHOD: {
					AbstractMethodDeclaration method = (AbstractMethodDeclaration) child.get();
					char[] name = method.selector;
					if (name == null) continue;
					if (getGeneratedBy(method) != null) continue;
					for (char[] methodToBeGenerated : listMethodsToBeGenerated(data, builderType)) {
						if (!Arrays.equals(name, methodToBeGenerated)) continue;
						child.addError("Manually adding a method that @Singular @Builder would generate is not supported. If you want to manually manage the builder aspect for this field/parameter, don't use @Singular.");
						return true;
					}
					break;
				}}
			}
			
			return false;
		}
		
		public List<char[]> listFieldsToBeGenerated(SingularData data, EclipseNode builderType) {
			return Collections.singletonList(data.pluralName);
		}
		
		public List<char[]> listMethodsToBeGenerated(SingularData data, EclipseNode builderType) {
			char[] p = data.pluralName;
			char[] s = data.singularName;
			if (Arrays.equals(p, s)) return Collections.singletonList(p);
			return Arrays.asList(p, s);
		}
		
		public abstract List<EclipseNode> generateFields(SingularData data, EclipseNode builderType);
		public abstract void generateMethods(SingularData data, boolean deprecate, EclipseNode builderType, boolean fluent, boolean chain);
		public abstract void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName);
		
		public boolean requiresCleaning() {
			try {
				return !getClass().getMethod("appendCleaningCode", SingularData.class, EclipseNode.class, List.class).getDeclaringClass().equals(EclipseSingularizer.class);
			} catch (NoSuchMethodException e) {
				return false;
			}
		}
		
		public void appendCleaningCode(SingularData data, EclipseNode builderType, List<Statement> statements) {
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
		protected TypeReference addTypeArgs(int count, boolean addExtends, EclipseNode node, TypeReference type, List<TypeReference> typeArgs) {
			TypeReference[] clonedAndFixedArgs = createTypeArgs(count, addExtends, node, typeArgs);
			if (type instanceof SingleTypeReference) {
				type = new ParameterizedSingleTypeReference(((SingleTypeReference) type).token, clonedAndFixedArgs, 0, 0L);
			} else if (type instanceof QualifiedTypeReference) {
				QualifiedTypeReference qtr = (QualifiedTypeReference) type;
				TypeReference[][] trs = new TypeReference[qtr.tokens.length][];
				trs[qtr.tokens.length - 1] = clonedAndFixedArgs;
				type = new ParameterizedQualifiedTypeReference(((QualifiedTypeReference) type).tokens, trs, 0, NULL_POSS);
			} else {
				node.addError("Don't know how to clone-and-parameterize type: " + type);
			}
			
			return type;
		}
		
		protected TypeReference[] createTypeArgs(int count, boolean addExtends, EclipseNode node, List<TypeReference> typeArgs) {
			if (count < 0) throw new IllegalArgumentException("count is negative");
			if (count == 0) return null;
			List<TypeReference> arguments = new ArrayList<TypeReference>();
			
			if (typeArgs != null) for (TypeReference orig : typeArgs) {
				Wildcard wildcard = orig instanceof Wildcard ? (Wildcard) orig : null;
				if (!addExtends) {
					if (wildcard != null && (wildcard.kind == Wildcard.UNBOUND || wildcard.kind == Wildcard.SUPER)) {
						arguments.add(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, NULL_POSS));
					} else if (wildcard != null && wildcard.kind == Wildcard.EXTENDS) {
						try {
							arguments.add(copyType(wildcard.bound));
						} catch (Exception e) {
							arguments.add(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, NULL_POSS));
						}
					} else {
						arguments.add(copyType(orig));
					}
				} else {
					if (wildcard != null && (wildcard.kind == Wildcard.UNBOUND || wildcard.kind == Wildcard.SUPER)) {
						Wildcard w = new Wildcard(Wildcard.UNBOUND);
						arguments.add(w);
					} else if (wildcard != null && wildcard.kind == Wildcard.EXTENDS) {
						arguments.add(copyType(orig));
					} else {
						Wildcard w = new Wildcard(Wildcard.EXTENDS);
						w.bound = copyType(orig);
						arguments.add(w);
					}
				}
				if (--count == 0) break;
			}
			
			while (count-- > 0) {
				if (addExtends) {
					arguments.add(new Wildcard(Wildcard.UNBOUND));
				} else {
					arguments.add(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, NULL_POSS));
				}
			}
			
			if (arguments.isEmpty()) return null;
			return arguments.toArray(new TypeReference[arguments.size()]);
		}
		
		private static final char[] SIZE_TEXT = new char[] {'s', 'i', 'z', 'e'};
		
		/** Generates 'this.<em>name</em>.size()' as an expression; if nullGuard is true, it's this.name == null ? 0 : this.name.size(). */
		protected Expression getSize(EclipseNode builderType, char[] name, boolean nullGuard) {
			MessageSend invoke = new MessageSend();
			ThisReference thisRef = new ThisReference(0, 0);
			FieldReference thisDotName = new FieldReference(name, 0L);
			thisDotName.receiver = thisRef;
			invoke.receiver = thisDotName;
			invoke.selector = SIZE_TEXT;
			if (!nullGuard) return invoke;
			
			ThisReference cdnThisRef = new ThisReference(0, 0);
			FieldReference cdnThisDotName = new FieldReference(name, 0L);
			cdnThisDotName.receiver = cdnThisRef;
			NullLiteral nullLiteral = new NullLiteral(0, 0);
			EqualExpression isNull = new EqualExpression(cdnThisDotName, nullLiteral, OperatorIds.EQUAL_EQUAL);
			IntLiteral zeroLiteral = makeIntLiteral(new char[] {'0'}, null);
			ConditionalExpression conditional = new ConditionalExpression(isNull, zeroLiteral, invoke);
			return conditional;
		}
		
		protected TypeReference cloneParamType(int index, List<TypeReference> typeArgs, EclipseNode builderType) {
			if (typeArgs != null && typeArgs.size() > index) {
				TypeReference originalType = typeArgs.get(index);
				if (originalType instanceof Wildcard) {
					Wildcard wOriginalType = (Wildcard) originalType;
					if (wOriginalType.kind == Wildcard.EXTENDS) {
						try {
							return copyType(wOriginalType.bound);
						} catch (Exception e) {
							// fallthrough
						}
					}
				} else {
					return copyType(originalType);
				}
			}
			
			return new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, NULL_POSS);
		}
	}
}
