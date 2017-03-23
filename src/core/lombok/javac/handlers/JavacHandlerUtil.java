/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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

import static com.sun.tools.javac.code.Flags.GENERATEDCONSTR;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacAugments.JCTree_generatedNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.Getter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.LombokImmutableList;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.TypeResolver;
import lombok.core.configuration.NullCheckExceptionType;
import lombok.core.handlers.HandlerUtil;
import lombok.delombok.LombokOptionsFactory;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import lombok.javac.Javac;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;

/**
 * Container for static utility methods useful to handlers written for javac.
 */
public class JavacHandlerUtil {
	private JavacHandlerUtil() {
		//Prevent instantiation
	}
	
	private static class MarkingScanner extends TreeScanner {
		private final JCTree source;
		private final Context context;
		
		MarkingScanner(JCTree source, Context context) {
			this.source = source;
			this.context = context;
		}
		
		@Override public void scan(JCTree tree) {
			if (tree == null) return;
			setGeneratedBy(tree, source, context);
			super.scan(tree);
		}
	}
	
	/**
	 * Contributed by Jan Lahoda; many lombok transformations should not be run (or a lite version should be run) when the netbeans editor
	 * is running javac on the open source file to find inline errors and such. As class files are compiled separately this does not affect
	 * actual runtime behaviour or file output of the netbeans IDE.
	 */
	public static boolean inNetbeansEditor(JavacNode node) {
		return inNetbeansEditor(node.getContext());
	}
	
	private static boolean inNetbeansEditor(Context context) {
		Options options = Options.instance(context);
		return (options.keySet().contains("ide") && !options.keySet().contains("backgroundCompilation"));
	}
	
	public static JCTree getGeneratedBy(JCTree node) {
		return JCTree_generatedNode.get(node);
	}
	
	public static boolean isGenerated(JCTree node) {
		return getGeneratedBy(node) != null;
	}
	
	public static <T extends JCTree> T recursiveSetGeneratedBy(T node, JCTree source, Context context) {
		if (node == null) return null;
		setGeneratedBy(node, source, context);
		node.accept(new MarkingScanner(source, context));
		return node;
	}
	
	public static <T extends JCTree> T setGeneratedBy(T node, JCTree source, Context context) {
		if (node == null) return null;
		if (source == null) JCTree_generatedNode.clear(node);
		else JCTree_generatedNode.set(node, source);
		if (source != null && (!inNetbeansEditor(context) || (node instanceof JCVariableDecl && (((JCVariableDecl) node).mods.flags & Flags.PARAMETER) != 0))) node.pos = source.pos;
		return node;
	}
	
	public static boolean hasAnnotation(Class<? extends Annotation> type, JavacNode node) {
		return hasAnnotation(type, node, false);
	}
	
	public static boolean hasAnnotationAndDeleteIfNeccessary(Class<? extends Annotation> type, JavacNode node) {
		return hasAnnotation(type, node, true);
	}
	
	private static boolean hasAnnotation(Class<? extends Annotation> type, JavacNode node, boolean delete) {
		if (node == null) return false;
		if (type == null) return false;
		switch (node.getKind()) {
		case ARGUMENT:
		case FIELD:
		case LOCAL:
		case TYPE:
		case METHOD:
			for (JavacNode child : node.down()) {
				if (annotationTypeMatches(type, child)) {
					if (delete) deleteAnnotationIfNeccessary(child, type);
					return true;
				}
			}
			// intentional fallthrough
		default:
			return false;
		}
	}
	
	static JavacNode findAnnotation(Class<? extends Annotation> type, JavacNode node, boolean delete) {
		if (node == null) return null;
		if (type == null) return null;
		switch (node.getKind()) {
		case ARGUMENT:
		case FIELD:
		case LOCAL:
		case TYPE:
		case METHOD:
			for (JavacNode child : node.down()) {
				if (annotationTypeMatches(type, child)) {
					if (delete) deleteAnnotationIfNeccessary(child, type);
					return child;
				}
			}
			// intentional fallthrough
		default:
			return null;
		}
	}
	
	/**
	 * Checks if the Annotation AST Node provided is likely to be an instance of the provided annotation type.
	 * 
	 * @param type An actual annotation type, such as {@code lombok.Getter.class}.
	 * @param node A Lombok AST node representing an annotation in source code.
	 */
	public static boolean annotationTypeMatches(Class<? extends Annotation> type, JavacNode node) {
		if (node.getKind() != Kind.ANNOTATION) return false;
		return typeMatches(type, node, ((JCAnnotation)node.get()).annotationType);
	}
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeNode A type reference to check.
	 */
	public static boolean typeMatches(Class<?> type, JavacNode node, JCTree typeNode) {
		String typeName = typeNode.toString();
		
		TypeResolver resolver = new TypeResolver(node.getImportList());
		return resolver.typeMatches(node, type.getName(), typeName);
	}
	
	/**
	 * Returns if a field is marked deprecated, either by {@code @Deprecated} or in javadoc
	 * @param field the field to check
	 * @return {@code true} if a field is marked deprecated, either by {@code @Deprecated} or in javadoc, otherwise {@code false}
	 */
	public static boolean isFieldDeprecated(JavacNode field) {
		if (!(field.get() instanceof JCVariableDecl)) return false;
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		if ((fieldNode.mods.flags & Flags.DEPRECATED) != 0) {
			return true;
		}
		for (JavacNode child : field.down()) {
			if (annotationTypeMatches(Deprecated.class, child)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns if a node is marked deprecated (as picked up on by the parser).
	 * @param node the node to check (type, method, or field decl).
	 */
	public static boolean nodeHasDeprecatedFlag(JCTree node) {
		if (node instanceof JCVariableDecl) return (((JCVariableDecl) node).mods.flags & Flags.DEPRECATED) != 0;
		if (node instanceof JCMethodDecl) return (((JCMethodDecl) node).mods.flags & Flags.DEPRECATED) != 0;
		if (node instanceof JCClassDecl) return (((JCClassDecl) node).mods.flags & Flags.DEPRECATED) != 0;
		return false;
	}
	
	/**
	 * Creates an instance of {@code AnnotationValues} for the provided AST Node.
	 * 
	 * @param type An annotation class type, such as {@code lombok.Getter.class}.
	 * @param node A Lombok AST node representing an annotation in source code.
	 */
	public static <A extends Annotation> AnnotationValues<A> createAnnotation(Class<A> type, final JavacNode node) {
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
		JCAnnotation anno = (JCAnnotation) node.get();
		List<JCExpression> arguments = anno.getArguments();
		
		for (JCExpression arg : arguments) {
			String mName;
			JCExpression rhs;
			java.util.List<String> raws = new ArrayList<String>();
			java.util.List<Object> guesses = new ArrayList<Object>();
			java.util.List<Object> expressions = new ArrayList<Object>();
			final java.util.List<DiagnosticPosition> positions = new ArrayList<DiagnosticPosition>();
			
			if (arg instanceof JCAssign) {
				JCAssign assign = (JCAssign) arg;
				mName = assign.lhs.toString();
				rhs = assign.rhs;
			} else {
				rhs = arg;
				mName = "value";
			}
			
			if (rhs instanceof JCNewArray) {
				List<JCExpression> elems = ((JCNewArray)rhs).elems;
				for (JCExpression inner : elems) {
					raws.add(inner.toString());
					expressions.add(inner);
					guesses.add(calculateGuess(inner));
					positions.add(inner.pos());
				}
			} else {
				raws.add(rhs.toString());
				expressions.add(rhs);
				guesses.add(calculateGuess(rhs));
				positions.add(rhs.pos());
			}
			
			values.put(mName, new AnnotationValue(node, raws, expressions, guesses, true) {
				@Override public void setError(String message, int valueIdx) {
					if (valueIdx < 0) node.addError(message);
					else node.addError(message, positions.get(valueIdx));
				}
				
				@Override public void setWarning(String message, int valueIdx) {
					if (valueIdx < 0) node.addWarning(message);
					else node.addWarning(message, positions.get(valueIdx));
				}
			});
		}
		
		for (Method m : type.getDeclaredMethods()) {
			if (!Modifier.isPublic(m.getModifiers())) continue;
			String name = m.getName();
			if (!values.containsKey(name)) {
				values.put(name, new AnnotationValue(node, new ArrayList<String>(), new ArrayList<Object>(), new ArrayList<Object>(), false) {
					@Override public void setError(String message, int valueIdx) {
						node.addError(message);
					}
					@Override public void setWarning(String message, int valueIdx) {
						node.addWarning(message);
					}
				});
			}
		}
		
		return new AnnotationValues<A>(type, values, node);
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	@SuppressWarnings("unchecked")
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType) {
		deleteAnnotationIfNeccessary0(annotation, annotationType);
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	@SuppressWarnings("unchecked")
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType1, Class<? extends Annotation> annotationType2) {
		deleteAnnotationIfNeccessary0(annotation, annotationType1, annotationType2);
	}
	
	private static void deleteAnnotationIfNeccessary0(JavacNode annotation, Class<? extends Annotation>... annotationTypes) {
		if (inNetbeansEditor(annotation)) return;
		if (!annotation.shouldDeleteLombokAnnotations()) return;
		JavacNode parentNode = annotation.directUp();
		switch (parentNode.getKind()) {
		case FIELD:
		case ARGUMENT:
		case LOCAL:
			JCVariableDecl variable = (JCVariableDecl) parentNode.get();
			variable.mods.annotations = filterList(variable.mods.annotations, annotation.get());
			break;
		case METHOD:
			JCMethodDecl method = (JCMethodDecl) parentNode.get();
			method.mods.annotations = filterList(method.mods.annotations, annotation.get());
			break;
		case TYPE:
			try {
				JCClassDecl type = (JCClassDecl) parentNode.get();
				type.mods.annotations = filterList(type.mods.annotations, annotation.get());
			} catch (ClassCastException e) {
				//something rather odd has been annotated. Better to just break only delombok instead of everything.
			}
			break;
		default:
			//This really shouldn't happen, but if it does, better just break delombok instead of breaking everything.
			return;
		}
		
		parentNode.getAst().setChanged();
		for (Class<?> annotationType : annotationTypes) {
			deleteImportFromCompilationUnit(annotation, annotationType.getName());
		}
	}
	
	public static void deleteImportFromCompilationUnit(JavacNode node, String name) {
		if (inNetbeansEditor(node)) return;
		if (!node.shouldDeleteLombokAnnotations()) return;
		ListBuffer<JCTree> newDefs = new ListBuffer<JCTree>();
		
		JCCompilationUnit unit = (JCCompilationUnit) node.top().get();
		
		for (JCTree def : unit.defs) {
			boolean delete = false;
			if (def instanceof JCImport) {
				JCImport imp0rt = (JCImport)def;
				delete = (!imp0rt.staticImport && imp0rt.qualid.toString().equals(name));
			}
			if (!delete) newDefs.append(def);
		}
		unit.defs = newDefs.toList();
	}

	private static List<JCAnnotation> filterList(List<JCAnnotation> annotations, JCTree jcTree) {
		ListBuffer<JCAnnotation> newAnnotations = new ListBuffer<JCAnnotation>();
		for (JCAnnotation ann : annotations) {
			if (jcTree != ann) newAnnotations.append(ann);
		}
		return newAnnotations.toList();
	}
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_LOMBOK, EXISTS_BY_USER;
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllGetterNames(JavacNode field) {
		return HandlerUtil.toAllGetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(JavacNode field) {
		return HandlerUtil.toGetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(JavacNode field) {
		return HandlerUtil.toAllSetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(JavacNode field) {
		return HandlerUtil.toSetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible wither names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllWitherNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWitherNames(JavacNode field) {
		return HandlerUtil.toAllWitherNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely wither name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toWitherName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWitherName(JavacNode field) {
		return HandlerUtil.toWitherName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * When generating a setter, the setter either returns void (beanspec) or Self (fluent).
	 * This method scans for the {@code Accessors} annotation to figure that out.
	 */
	public static boolean shouldReturnThis(JavacNode field) {
		if ((((JCVariableDecl) field.get()).mods.flags & Flags.STATIC) != 0) return false;
		
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(field);
		
		return HandlerUtil.shouldReturnThis0(accessors, field.getAst());
	}
	
	public static JCExpression cloneSelfType(JavacNode childOfType) {
		JavacNode typeNode = childOfType;
		JavacTreeMaker maker = childOfType.getTreeMaker();
		while (typeNode != null && typeNode.getKind() != Kind.TYPE) typeNode = typeNode.up();
		if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
			JCClassDecl type = (JCClassDecl) typeNode.get();
			ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
			if (!type.typarams.isEmpty()) {
				for (JCTypeParameter tp : type.typarams) {
					typeArgs.append(maker.Ident(tp.name));
				}
				return maker.TypeApply(maker.Ident(type.name), typeArgs.toList());
			} else {
				return maker.Ident(type.name);
			}
		} else {
			return null;
		}
	}
	
	public static boolean isBoolean(JavacNode field) {
		JCExpression varType = ((JCVariableDecl) field.get()).vartype;
		return isBoolean(varType);
	}
	
	public static boolean isBoolean(JCExpression varType) {
		return varType != null && varType.toString().equals("boolean");
	}
	
	public static Name removePrefixFromField(JavacNode field) {
		java.util.List<String> prefixes = null;
		for (JavacNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				AnnotationValues<Accessors> ann = createAnnotation(Accessors.class, node);
				if (ann.isExplicit("prefix")) prefixes = Arrays.asList(ann.getInstance().prefix());
				break;
			}
		}
		
		if (prefixes == null) {
			JavacNode current = field.up();
			outer:
			while (current != null) {
				for (JavacNode node : current.down()) {
					if (annotationTypeMatches(Accessors.class, node)) {
						AnnotationValues<Accessors> ann = createAnnotation(Accessors.class, node);
						if (ann.isExplicit("prefix")) prefixes = Arrays.asList(ann.getInstance().prefix());
						break outer;
					}
				}
				current = current.up();
			}
		}
		
		if (prefixes == null) prefixes = field.getAst().readConfiguration(ConfigurationKeys.ACCESSORS_PREFIX);
		
		if (!prefixes.isEmpty()) {
			CharSequence newName = removePrefix(field.getName(), prefixes);
			if (newName != null) return field.toName(newName.toString());
		}
		
		return ((JCVariableDecl) field.get()).name;
	}
	
	public static AnnotationValues<Accessors> getAccessorsForField(JavacNode field) {
		for (JavacNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				return createAnnotation(Accessors.class, node);
			}
		}
		
		JavacNode current = field.up();
		while (current != null) {
			for (JavacNode node : current.down()) {
				if (annotationTypeMatches(Accessors.class, node)) {
					return createAnnotation(Accessors.class, node);
				}
			}
			current = current.up();
		}
		
		return AnnotationValues.of(Accessors.class, field);
	}
	
	/**
	 * Checks if there is a field with the provided name.
	 * 
	 * @param fieldName the field name to check for.
	 * @param node Any node that represents the Type (JCClassDecl) to look in, or any child node thereof.
	 */
	public static MemberExistsResult fieldExists(String fieldName, JavacNode node) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCVariableDecl) {
					if (((JCVariableDecl)def).name.contentEquals(fieldName)) {
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	public static MemberExistsResult methodExists(String methodName, JavacNode node, int params) {
		return methodExists(methodName, node, true, params);
	}
	
	/**
	 * Checks if there is a method with the provided name. In case of multiple methods (overloading), only
	 * the first method decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param methodName the method name to check for.
	 * @param node Any node that represents the Type (JCClassDecl) to look in, or any child node thereof.
	 * @param caseSensitive If the search should be case sensitive.
	 * @param params The number of parameters the method should have; varargs count as 0-*. Set to -1 to find any method with the appropriate name regardless of parameter count.
	 */
	public static MemberExistsResult methodExists(String methodName, JavacNode node, boolean caseSensitive, int params) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof JCClassDecl) {
			top: for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					String name = md.name.toString();
					boolean matches = caseSensitive ? name.equals(methodName) : name.equalsIgnoreCase(methodName);
					if (matches) {
						if (params > -1) {
							List<JCVariableDecl> ps = md.params;
							int minArgs = 0;
							int maxArgs = 0;
							if (ps != null && ps.length() > 0) {
								minArgs = ps.length();
								if ((ps.last().mods.flags & Flags.VARARGS) != 0) {
									maxArgs = Integer.MAX_VALUE;
									minArgs--;
								} else {
									maxArgs = minArgs;
								}
							}
							
							if (params < minArgs || params > maxArgs) continue;
						}
						
						if (isTolerate(node, md)) continue top;
						
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	public static boolean isTolerate(JavacNode node, JCTree.JCMethodDecl md) {
		List<JCAnnotation> annotations = md.getModifiers().getAnnotations();
		if (annotations != null) for (JCTree.JCAnnotation anno : annotations) {
			if (typeMatches(Tolerate.class, node, anno.getAnnotationType())) return true;
		}
		return false;
	}
	
	/**
	 * Checks if there is a (non-default) constructor. In case of multiple constructors (overloading), only
	 * the first constructor decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param node Any node that represents the Type (JCClassDecl) to look in, or any child node thereof.
	 */
	public static MemberExistsResult constructorExists(JavacNode node) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					if (md.name.contentEquals("<init>")) {
						if ((md.mods.flags & Flags.GENERATEDCONSTR) != 0) continue;
						if (isTolerate(node, md)) continue;
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	public static boolean isConstructorCall(final JCStatement statement) {
		if (!(statement instanceof JCExpressionStatement)) return false;
		JCExpression expr = ((JCExpressionStatement) statement).expr;
		if (!(expr instanceof JCMethodInvocation)) return false;
		JCExpression invocation = ((JCMethodInvocation) expr).meth;
		String name;
		if (invocation instanceof JCFieldAccess) {
			name = ((JCFieldAccess) invocation).name.toString();
		} else if (invocation instanceof JCIdent) {
			name = ((JCIdent) invocation).name.toString();
		} else {
			name = "";
		}
		
		return "super".equals(name) || "this".equals(name);
	}
	
	/**
	 * Turns an {@code AccessLevel} instance into the flag bit used by javac.
	 */
	public static int toJavacModifier(AccessLevel accessLevel) {
		switch (accessLevel) {
		case MODULE:
		case PACKAGE:
			return 0;
		default:
		case PUBLIC:
			return Flags.PUBLIC;
		case NONE:
		case PRIVATE:
			return Flags.PRIVATE;
		case PROTECTED:
			return Flags.PROTECTED;
		}
	}
	
	private static class GetterMethod {
		private final Name name;
		private final JCExpression type;
		
		GetterMethod(Name name, JCExpression type) {
			this.name = name;
			this.type = type;
		}
	}
	
	private static GetterMethod findGetter(JavacNode field) {
		JCVariableDecl decl = (JCVariableDecl)field.get();
		JavacNode typeNode = field.up();
		for (String potentialGetterName : toAllGetterNames(field)) {
			for (JavacNode potentialGetter : typeNode.down()) {
				if (potentialGetter.getKind() != Kind.METHOD) continue;
				JCMethodDecl method = (JCMethodDecl) potentialGetter.get();
				if (!method.name.toString().equalsIgnoreCase(potentialGetterName)) continue;
				/** static getX() methods don't count. */
				if ((method.mods.flags & Flags.STATIC) != 0) continue;
				/** Nor do getters with a non-empty parameter list. */
				if (method.params != null && method.params.size() > 0) continue;
				return new GetterMethod(method.name, method.restype);
			}
		}
		
		// Check if the field has a @Getter annotation.
		
		boolean hasGetterAnnotation = false;
		
		for (JavacNode child : field.down()) {
			if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Getter.class, child)) {
				AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
				if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
				hasGetterAnnotation = true;
			}
		}
		
		// Check if the class has a @Getter annotation.
		
		if (!hasGetterAnnotation && new HandleGetter().fieldQualifiesForGetterGeneration(field)) {
			//Check if the class has @Getter or @Data annotation.
			
			JavacNode containingType = field.up();
			if (containingType != null) for (JavacNode child : containingType.down()) {
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Data.class, child)) hasGetterAnnotation = true;
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Getter.class, child)) {
					AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
					if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
					hasGetterAnnotation = true;
				}
			}
		}
		
		if (hasGetterAnnotation) {
			String getterName = toGetterName(field);
			if (getterName == null) return null;
			return new GetterMethod(field.toName(getterName), decl.vartype);
		}
		
		return null;
	}
	
	public enum FieldAccess {
		GETTER, PREFER_FIELD, ALWAYS_FIELD;
	}
	
	static boolean lookForGetter(JavacNode field, FieldAccess fieldAccess) {
		if (fieldAccess == FieldAccess.GETTER) return true;
		if (fieldAccess == FieldAccess.ALWAYS_FIELD) return false;
		
		// If @Getter(lazy = true) is used, then using it is mandatory.
		for (JavacNode child : field.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			if (annotationTypeMatches(Getter.class, child)) {
				AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
				if (ann.getInstance().lazy()) return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the type of the field, unless a getter exists for this field, in which case the return type of the getter is returned.
	 * 
	 * @see #createFieldAccessor(TreeMaker, JavacNode, FieldAccess)
	 */
	static JCExpression getFieldType(JavacNode field, FieldAccess fieldAccess) {
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		
		if (getter == null) {
			return ((JCVariableDecl)field.get()).vartype;
		}
		
		return getter.type;
	}
	
	/**
	 * Creates an expression that reads the field. Will either be {@code this.field} or {@code this.getField()} depending on whether or not there's a getter.
	 */
	static JCExpression createFieldAccessor(JavacTreeMaker maker, JavacNode field, FieldAccess fieldAccess) {
		return createFieldAccessor(maker, field, fieldAccess, null);
	}
	
	static JCExpression createFieldAccessor(JavacTreeMaker maker, JavacNode field, FieldAccess fieldAccess, JCExpression receiver) {
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		if (getter == null) {
			if (receiver == null) {
				if ((fieldDecl.mods.flags & Flags.STATIC) == 0) {
					receiver = maker.Ident(field.toName("this"));
				} else {
					JavacNode containerNode = field.up();
					if (containerNode != null && containerNode.get() instanceof JCClassDecl) {
						JCClassDecl container = (JCClassDecl) field.up().get();
						receiver = maker.Ident(container.name);
					}
				}
			}
			
			return receiver == null ? maker.Ident(fieldDecl.name) : maker.Select(receiver, fieldDecl.name);
		}
		
		if (receiver == null) receiver = maker.Ident(field.toName("this"));
		JCMethodInvocation call = maker.Apply(List.<JCExpression>nil(),
				maker.Select(receiver, getter.name), List.<JCExpression>nil());
		return call;
	}
	
	public static Type getMirrorForFieldType(JavacNode fieldNode) {
		Element fieldElement = fieldNode.getElement();
		if (fieldElement instanceof VarSymbol) return ((VarSymbol) fieldElement).type;
		return null;
	}
	
	/**
	 * Adds the given new field declaration to the provided type AST Node.
	 * The field carries the &#64;{@link SuppressWarnings}("all") annotation.
	 * Also takes care of updating the JavacAST.
	 */
	public static JavacNode injectFieldAndMarkGenerated(JavacNode typeNode, JCVariableDecl field) {
		return injectField(typeNode, field, true);
	}
	
	/**
	 * Adds the given new field declaration to the provided type AST Node.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static JavacNode injectField(JavacNode typeNode, JCVariableDecl field) {
		return injectField(typeNode, field, false);
	}

	private static JavacNode injectField(JavacNode typeNode, JCVariableDecl field, boolean addGenerated) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		if (addGenerated) {
			addSuppressWarningsAll(field.mods, typeNode, field.pos, getGeneratedBy(field), typeNode.getContext());
			addGenerated(field.mods, typeNode, field.pos, getGeneratedBy(field), typeNode.getContext());
		}
		
		List<JCTree> insertAfter = null;
		List<JCTree> insertBefore = type.defs;
		while (true) {
			boolean skip = false;
			if (insertBefore.head instanceof JCVariableDecl) {
				JCVariableDecl f = (JCVariableDecl) insertBefore.head;
				if (isEnumConstant(f) || isGenerated(f)) skip = true;
			} else if (insertBefore.head instanceof JCMethodDecl) {
				if ((((JCMethodDecl) insertBefore.head).mods.flags & GENERATEDCONSTR) != 0) skip = true;
			}
			if (skip) {
				insertAfter = insertBefore;
				insertBefore = insertBefore.tail;
				continue;
			}
			break;
		}
		List<JCTree> fieldEntry = List.<JCTree>of(field);
		fieldEntry.tail = insertBefore;
		if (insertAfter == null) {
			type.defs = fieldEntry;
		} else {
			insertAfter.tail = fieldEntry;
		}
		
		return typeNode.add(field, Kind.FIELD);
	}
	
	public static boolean isEnumConstant(final JCVariableDecl field) {
		return (field.mods.flags & Flags.ENUM) != 0;
	}
	
	// jdk9 support, types have changed, names stay the same
	static class ClassSymbolMembersField {
		private static final Field membersField;
		private static final Method removeMethod;
		private static final Method enterMethod;
		
		static {
			Field f = null;
			Method r = null;
			Method e = null;
			try {
				f = ClassSymbol.class.getField("members_field");
				r = f.getType().getMethod("remove", Symbol.class);
				e = f.getType().getMethod("enter", Symbol.class);
			} catch (Exception ex) {}
			membersField = f;
			removeMethod = r;
			enterMethod = e;
		}
		
		static void remove(ClassSymbol from, Symbol toRemove) {
			if (from == null) return;
			try {
				Scope scope = (Scope) membersField.get(from);
				if (scope == null) return;
				removeMethod.invoke(scope, toRemove);
			} catch (Exception e) {}
		}
		
		static void enter(ClassSymbol from, Symbol toEnter) {
			if (from == null) return;
			try {
				Scope scope = (Scope) membersField.get(from);
				if (scope == null) return;
				enterMethod.invoke(scope, toEnter);
			} catch (Exception e) {}
		}
	}
	
	public static void injectMethod(JavacNode typeNode, JCMethodDecl method) {
		injectMethod(typeNode, method, null, null);
	}
	
	/**
	 * Adds the given new method declaration to the provided type AST Node.
	 * Can also inject constructors.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectMethod(JavacNode typeNode, JCMethodDecl method, List<Type> paramTypes, Type returnType) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		if (method.getName().contentEquals("<init>")) {
			//Scan for default constructor, and remove it.
			int idx = 0;
			for (JCTree def : type.defs) {
				if (def instanceof JCMethodDecl) {
					if ((((JCMethodDecl) def).mods.flags & Flags.GENERATEDCONSTR) != 0) {
						JavacNode tossMe = typeNode.getNodeFor(def);
						if (tossMe != null) tossMe.up().removeChild(tossMe);
						type.defs = addAllButOne(type.defs, idx);
						ClassSymbolMembersField.remove(type.sym, ((JCMethodDecl)def).sym);
						break;
					}
				}
				idx++;
			}
		}
		
		addSuppressWarningsAll(method.mods, typeNode, method.pos, getGeneratedBy(method), typeNode.getContext());
		addGenerated(method.mods, typeNode, method.pos, getGeneratedBy(method), typeNode.getContext());
		type.defs = type.defs.append(method);
		
		fixMethodMirror(typeNode.getContext(), typeNode.getElement(), method.getModifiers().flags, method.getName(), paramTypes, returnType);
		
		typeNode.add(method, Kind.METHOD);
	}
	
	private static void fixMethodMirror(Context context, Element typeMirror, long access, Name methodName, List<Type> paramTypes, Type returnType) {
		if (typeMirror == null || paramTypes == null || returnType == null) return;
		ClassSymbol cs = (ClassSymbol) typeMirror;
		MethodSymbol methodSymbol = new MethodSymbol(access, methodName, new MethodType(paramTypes, returnType, List.<Type>nil(), Symtab.instance(context).methodClass), cs);
		ClassSymbolMembersField.enter(cs, methodSymbol);
	}
	
	/**
	 * Adds an inner type (class, interface, enum) to the given type. Cannot inject top-level types.
	 * 
	 * @param typeNode parent type to inject new type into
	 * @param type New type (class, interface, etc) to inject.
	 * @return 
	 */
	public static JavacNode injectType(JavacNode typeNode, final JCClassDecl type) {
		JCClassDecl typeDecl = (JCClassDecl) typeNode.get();
		addSuppressWarningsAll(type.mods, typeNode, type.pos, getGeneratedBy(type), typeNode.getContext());
		addGenerated(type.mods, typeNode, type.pos, getGeneratedBy(type), typeNode.getContext());
		typeDecl.defs = typeDecl.defs.append(type);
		return typeNode.add(type, Kind.TYPE);
	}
	
	public static long addFinalIfNeeded(long flags, Context context) {
		boolean addFinal = LombokOptionsFactory.getDelombokOptions(context).getFormatPreferences().generateFinalParams();
		
		if (addFinal) flags |= Flags.FINAL;
		return flags;
	}
	
	public static JCExpression genTypeRef(JavacNode node, String complexName) {
		String[] parts = complexName.split("\\.");
		if (parts.length > 2 && parts[0].equals("java") && parts[1].equals("lang")) {
			String[] subParts = new String[parts.length - 2];
			System.arraycopy(parts, 2, subParts, 0, subParts.length);
			return genJavaLangTypeRef(node, subParts);
		}
		
		return chainDots(node, parts);
	}
	
	public static JCExpression genJavaLangTypeRef(JavacNode node, String... simpleNames) {
		if (LombokOptionsFactory.getDelombokOptions(node.getContext()).getFormatPreferences().javaLangAsFqn()) {
			return chainDots(node, "java", "lang", simpleNames);
		} else {
			return chainDots(node, null, null, simpleNames);
		}
	}
	
	public static JCExpression genJavaLangTypeRef(JavacNode node, int pos, String... simpleNames) {
		if (LombokOptionsFactory.getDelombokOptions(node.getContext()).getFormatPreferences().javaLangAsFqn()) {
			return chainDots(node, pos, "java", "lang", simpleNames);
		} else {
			return chainDots(node, pos, null, null, simpleNames);
		}
	}
	
	public static void addSuppressWarningsAll(JCModifiers mods, JavacNode node, int pos, JCTree source, Context context) {
		if (!LombokOptionsFactory.getDelombokOptions(context).getFormatPreferences().generateSuppressWarnings()) return;
		addAnnotation(mods, node, pos, source, context, "java.lang.SuppressWarnings", node.getTreeMaker().Literal("all"));
		
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS))) {
			JavacTreeMaker maker = node.getTreeMaker();
			JCExpression arg = maker.Assign(maker.Ident(node.toName("justification")), maker.Literal("generated code"));
			addAnnotation(mods, node, pos, source, context, "edu.umd.cs.findbugs.annotations.SuppressFBWarnings", arg);
		}
	}
	
	public static void addGenerated(JCModifiers mods, JavacNode node, int pos, JCTree source, Context context) {
		if (!LombokOptionsFactory.getDelombokOptions(context).getFormatPreferences().generateGenerated()) return;
		
		if (HandlerUtil.shouldAddGenerated(node)) {
			addAnnotation(mods, node, pos, source, context, "javax.annotation.Generated", node.getTreeMaker().Literal("lombok"));
		}
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_LOMBOK_GENERATED_ANNOTATIONS))) {
			addAnnotation(mods, node, pos, source, context, "lombok.Generated", null);
		}
	}
	
	private static void addAnnotation(JCModifiers mods, JavacNode node, int pos, JCTree source, Context context, String annotationTypeFqn, JCExpression arg) {
		boolean isJavaLangBased;
		String simpleName; {
			int idx = annotationTypeFqn.lastIndexOf('.');
			simpleName = idx == -1 ? annotationTypeFqn : annotationTypeFqn.substring(idx + 1);
			
			isJavaLangBased = idx == 9 && annotationTypeFqn.regionMatches(0, "java.lang.", 0, 10);
		}
		
		for (JCAnnotation ann : mods.annotations) {
			JCTree annType = ann.getAnnotationType();
			if (annType instanceof JCIdent) {
				Name lastPart = ((JCIdent) annType).name;
				if (lastPart.contentEquals(simpleName)) return;
			}
			
			if (annType instanceof JCFieldAccess) {
				if (annType.toString().equals(annotationTypeFqn)) return;
			}
		}
		
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression annType = isJavaLangBased ? genJavaLangTypeRef(node, simpleName) : chainDotsString(node, annotationTypeFqn);
		annType.pos = pos;
		if (arg != null) {
			arg.pos = pos;
			if (arg instanceof JCAssign) {
				((JCAssign) arg).lhs.pos = pos;
				((JCAssign) arg).rhs.pos = pos;
			}
		}
		List<JCExpression> argList = arg != null ? List.of(arg) : List.<JCExpression>nil();
		JCAnnotation annotation = recursiveSetGeneratedBy(maker.Annotation(annType, argList), source, context);
		annotation.pos = pos;
		mods.annotations = mods.annotations.append(annotation);
	}
	
	private static List<JCTree> addAllButOne(List<JCTree> defs, int idx) {
		ListBuffer<JCTree> out = new ListBuffer<JCTree>();
		int i = 0;
		for (JCTree def : defs) {
			if (i++ != idx) out.append(def);
		}
		return out.toList();
	}
	
	/**
	 * In javac, dotted access of any kind, from {@code java.lang.String} to {@code var.methodName}
	 * is represented by a fold-left of {@code Select} nodes with the leftmost string represented by
	 * a {@code Ident} node. This method generates such an expression.
	 * <p>
	 * The position of the generated node(s) will be unpositioned (-1).
	 * 
	 * For example, maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	 * 
	 * @see com.sun.tools.javac.tree.JCTree.JCIdent
	 * @see com.sun.tools.javac.tree.JCTree.JCFieldAccess
	 */
	public static JCExpression chainDots(JavacNode node, String elem1, String elem2, String... elems) {
		return chainDots(node, -1, elem1, elem2, elems);
	}
	
	public static JCExpression chainDots(JavacNode node, String[] elems) {
		return chainDots(node, -1, null, null, elems);
	}
	
	public static JCExpression chainDots(JavacNode node, LombokImmutableList<String> elems) {
		assert elems != null;
		
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression e = null;
		for (String elem : elems) {
			if (e == null) e = maker.Ident(node.toName(elem));
			else e = maker.Select(e, node.toName(elem));
		}
		return e;
	}
	/**
	 * In javac, dotted access of any kind, from {@code java.lang.String} to {@code var.methodName}
	 * is represented by a fold-left of {@code Select} nodes with the leftmost string represented by
	 * a {@code Ident} node. This method generates such an expression.
	 * <p>
	 * The position of the generated node(s) will be equal to the {@code pos} parameter.
	 *
	 * For example, maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	 * 
	 * @see com.sun.tools.javac.tree.JCTree.JCIdent
	 * @see com.sun.tools.javac.tree.JCTree.JCFieldAccess
	 */
	public static JCExpression chainDots(JavacNode node, int pos, String elem1, String elem2, String... elems) {
		assert elems != null;
		
		JavacTreeMaker maker = node.getTreeMaker();
		if (pos != -1) maker = maker.at(pos);
		JCExpression e = null;
		if (elem1 != null) e = maker.Ident(node.toName(elem1));
		if (elem2 != null) e = e == null ? maker.Ident(node.toName(elem2)) : maker.Select(e, node.toName(elem2));
		for (int i = 0 ; i < elems.length ; i++) {
			e = e == null ? maker.Ident(node.toName(elems[i])) : maker.Select(e, node.toName(elems[i]));
		}
		
		assert e != null;
		
		return e;
	}
	
	/**
	 * In javac, dotted access of any kind, from {@code java.lang.String} to {@code var.methodName}
	 * is represented by a fold-left of {@code Select} nodes with the leftmost string represented by
	 * a {@code Ident} node. This method generates such an expression.
	 * 
	 * For example, maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	 * 
	 * @see com.sun.tools.javac.tree.JCTree.JCIdent
	 * @see com.sun.tools.javac.tree.JCTree.JCFieldAccess
	 */
	public static JCExpression chainDotsString(JavacNode node, String elems) {
		return chainDots(node, null, null, elems.split("\\."));
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that matches the provided regular expression pattern.
	 * 
	 * Only the simple name is checked - the package and any containing class are ignored.
	 */
	public static List<JCAnnotation> findAnnotations(JavacNode fieldNode, Pattern namePattern) {
		ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
		for (JavacNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				String name = annotation.annotationType.toString();
				int idx = name.lastIndexOf(".");
				String suspect = idx == -1 ? name : name.substring(idx + 1);
				if (namePattern.matcher(suspect).matches()) {
					result.append(annotation);
				}
			}
		}	
		return result.toList();
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a specified exception with the
	 * variable name as message.
	 * 
	 * @param exName The name of the exception to throw; normally {@code java.lang.NullPointerException}.
	 */
	public static JCStatement generateNullCheck(JavacTreeMaker maker, JavacNode variable, JavacNode source) {
		NullCheckExceptionType exceptionType = source.getAst().readConfiguration(ConfigurationKeys.NON_NULL_EXCEPTION_TYPE);
		if (exceptionType == null) exceptionType = NullCheckExceptionType.NULL_POINTER_EXCEPTION;
		
		JCVariableDecl varDecl = (JCVariableDecl) variable.get();
		if (isPrimitive(varDecl.vartype)) return null;
		Name fieldName = varDecl.name;
		JCExpression exType = genTypeRef(variable, exceptionType.getExceptionType());
		JCExpression exception = maker.NewClass(null, List.<JCExpression>nil(), exType, List.<JCExpression>of(maker.Literal(exceptionType.toExceptionMessage(fieldName.toString()))), null);
		JCStatement throwStatement = maker.Throw(exception);
		JCBlock throwBlock = maker.Block(0, List.of(throwStatement));
		return maker.If(maker.Binary(CTC_EQUAL, maker.Ident(fieldName), maker.Literal(CTC_BOT, null)), throwBlock, null);
	}
	
	/**
	 * Given a list of field names and a node referring to a type, finds each name in the list that does not match a field within the type.
	 */
	public static List<Integer> createListOfNonExistentFields(List<String> list, JavacNode type, boolean excludeStandard, boolean excludeTransient) {
		boolean[] matched = new boolean[list.size()];
		
		for (JavacNode child : type.down()) {
			if (list.isEmpty()) break;
			if (child.getKind() != Kind.FIELD) continue;
			JCVariableDecl field = (JCVariableDecl)child.get();
			if (excludeStandard) {
				if ((field.mods.flags & Flags.STATIC) != 0) continue;
				if (field.name.toString().startsWith("$")) continue;
			}
			if (excludeTransient && (field.mods.flags & Flags.TRANSIENT) != 0) continue;
			
			int idx = list.indexOf(child.getName());
			if (idx > -1) matched[idx] = true;
		}
		
		ListBuffer<Integer> problematic = new ListBuffer<Integer>();
		for (int i = 0 ; i < list.size() ; i++) {
			if (!matched[i]) problematic.append(i);
		}
		
		return problematic.toList();
	}
	
	static List<JCAnnotation> unboxAndRemoveAnnotationParameter(JCAnnotation ast, String parameterName, String errorName, JavacNode annotationNode) {
		ListBuffer<JCExpression> params = new ListBuffer<JCExpression>();
		ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
		
		outer:
		for (JCExpression param : ast.args) {
			boolean allowRaw;
			String nameOfParam = "value";
			JCExpression valueOfParam = null;
			if (param instanceof JCAssign) {
				JCAssign assign = (JCAssign) param;
				if (assign.lhs instanceof JCIdent) {
					JCIdent ident = (JCIdent) assign.lhs;
					nameOfParam = ident.name.toString();
				}
				valueOfParam = assign.rhs;
			}
			
			/* strip trailing underscores */ {
				int lastIdx;
				for (lastIdx = nameOfParam.length() ; lastIdx > 0; lastIdx--) {
					if (nameOfParam.charAt(lastIdx - 1) != '_') break;
				}
				allowRaw = lastIdx < nameOfParam.length();
				nameOfParam = nameOfParam.substring(0, lastIdx);
			}
			
			if (!parameterName.equals(nameOfParam)) {
				params.append(param);
				continue outer;
			}
			
			int endPos = Javac.getEndPosition(param.pos(), (JCCompilationUnit) annotationNode.top().get());
			annotationNode.getAst().removeFromDeferredDiagnostics(param.pos, endPos);
			
			if (valueOfParam instanceof JCAnnotation) {
				String dummyAnnotationName = ((JCAnnotation) valueOfParam).annotationType.toString();
				dummyAnnotationName = dummyAnnotationName.replace("_", "").replace("$", "").replace("x", "").replace("X", "");
				if (dummyAnnotationName.length() > 0) {
					if (allowRaw) {
						result.append((JCAnnotation) valueOfParam);
					} else {
						addError(errorName, annotationNode);
						continue outer;
					}
				} else {
					for (JCExpression expr : ((JCAnnotation) valueOfParam).args) {
						if (expr instanceof JCAssign && ((JCAssign) expr).lhs instanceof JCIdent) {
							JCIdent id = (JCIdent) ((JCAssign) expr).lhs;
							if ("value".equals(id.name.toString())) {
								expr = ((JCAssign) expr).rhs;
							} else {
								addError(errorName, annotationNode);
							}
						}
						
						if (expr instanceof JCAnnotation) {
							result.append((JCAnnotation) expr);
						} else if (expr instanceof JCNewArray) {
							for (JCExpression expr2 : ((JCNewArray) expr).elems) {
								if (expr2 instanceof JCAnnotation) {
									result.append((JCAnnotation) expr2);
								} else {
									addError(errorName, annotationNode);
									continue outer;
								}
							}
						} else {
							addError(errorName, annotationNode);
							continue outer;
						}
					}
				}
			} else if (valueOfParam instanceof JCNewArray) {
				JCNewArray arr = (JCNewArray) valueOfParam;
				if (arr.elems.isEmpty()) {
					// Just remove it, this is always fine.
				} else if (allowRaw) {
					for (JCExpression jce : arr.elems) {
						if (jce instanceof JCAnnotation) result.append((JCAnnotation) jce);
						else addError(errorName, annotationNode);
					}
				} else {
					addError(errorName, annotationNode);
				}
			} else {
				addError(errorName, annotationNode);
			}
		}
		ast.args = params.toList();
		return result.toList();
	}
	
	private static void addError(String errorName, JavacNode node) {
		if (node.getLatestJavaSpecSupported() < 8) {
			node.addError("The correct format up to JDK7 is " + errorName + "=@__({@SomeAnnotation, @SomeOtherAnnotation}))");
		} else {
			node.addError("The correct format for JDK8+ is " + errorName + "_={@SomeAnnotation, @SomeOtherAnnotation})");
		}
	}
	
	public static List<JCTypeParameter> copyTypeParams(JavacNode source, List<JCTypeParameter> params) {
		if (params == null || params.isEmpty()) return params;
		ListBuffer<JCTypeParameter> out = new ListBuffer<JCTypeParameter>();
		JavacTreeMaker maker = source.getTreeMaker();
		Context context = source.getContext();
		for (JCTypeParameter tp : params) {
			List<JCExpression> bounds = tp.bounds;
			if (bounds != null && !bounds.isEmpty()) {
				ListBuffer<JCExpression> boundsCopy = new ListBuffer<JCExpression>();
				for (JCExpression expr : tp.bounds) {
					boundsCopy.append(cloneType(maker, expr, source.get(), context));
				}
				bounds = boundsCopy.toList();
			}
			out.append(maker.TypeParameter(tp.name, bounds));
		}
		return out.toList();
	}
	
	public static JCExpression namePlusTypeParamsToTypeReference(JavacTreeMaker maker, Name typeName, List<JCTypeParameter> params) {
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		
		if (!params.isEmpty()) {
			for (JCTypeParameter param : params) {
				typeArgs.append(maker.Ident(param.name));
			}
			
			return maker.TypeApply(maker.Ident(typeName), typeArgs.toList());
		}
		
		return maker.Ident(typeName);
	}
	
	public static void sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(JavacNode typeNode, JavacNode errorNode) {
		List<String> disallowed = List.nil();
		for (JavacNode child : typeNode.down()) {
			for (Class<? extends java.lang.annotation.Annotation> annType : INVALID_ON_BUILDERS) {
				if (annotationTypeMatches(annType, child)) {
					disallowed = disallowed.append(annType.getSimpleName());
				}
			}
		}
		
		int size = disallowed.size();
		if (size == 0) return;
		if (size == 1) {
			errorNode.addError("@" + disallowed.head + " is not allowed on builder classes.");
			return;
		}
		StringBuilder out = new StringBuilder();
		for (String a : disallowed) out.append("@").append(a).append(", ");
		out.setLength(out.length() - 2);
		errorNode.addError(out.append(" are not allowed on builder classes.").toString());
	}
	
	static List<JCAnnotation> copyAnnotations(List<? extends JCExpression> in) {
		ListBuffer<JCAnnotation> out = new ListBuffer<JCAnnotation>();
		for (JCExpression expr : in) {
			if (!(expr instanceof JCAnnotation)) continue;
			out.append((JCAnnotation) expr.clone());
		}
		return out.toList();
	}
	
	static boolean isClass(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION);
	}
	
	static boolean isClassOrEnum(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.INTERFACE | Flags.ANNOTATION);
	}
	
	public static boolean isClassAndDoesNotHaveFlags(JavacNode typeNode, int flags) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl)typeNode.get();
		else return false;
		
		long typeDeclflags = typeDecl == null ? 0 : typeDecl.mods.flags;
		return (typeDeclflags & flags) == 0;
	}
	
	public static JavacNode upToTypeNode(JavacNode node) {
		if (node == null) throw new NullPointerException("node");
		while ((node != null) && !(node.get() instanceof JCClassDecl)) node = node.up();
		
		return node;
	}
	
	/**
	 * Creates a full clone of a given javac AST type node. Every part is cloned (every identifier, every select, every wildcard, every type apply).
	 * 
	 * If there's any node in the tree that we don't know how to clone, that part isn't cloned. However, we wouldn't know what could possibly show up that we
	 * can't currently clone; that's just a safeguard.
	 * 
	 * This should be used if the type looks the same in the code, but resolves differently. For example, a static method that has some generics in it named after
	 * the class's own parameter, but as its a static method, the static method's notion of {@code T} is different from the class notion of {@code T}. If you're duplicating
	 * a type used in the class context, you need to use this method.
	 */
	public static JCExpression cloneType(JavacTreeMaker maker, JCExpression in, JCTree source, Context context) {
		JCExpression out = cloneType0(maker, in);
		if (out != null) recursiveSetGeneratedBy(out, source, context);
		return out;
	}
	
	private static JCExpression cloneType0(JavacTreeMaker maker, JCTree in) {
		if (in == null) return null;
		
		if (in instanceof JCPrimitiveTypeTree) return (JCExpression) in;
		
		if (in instanceof JCIdent) {
			return maker.Ident(((JCIdent) in).name);
		}
		
		if (in instanceof JCFieldAccess) {
			JCFieldAccess fa = (JCFieldAccess) in;
			return maker.Select(cloneType0(maker, fa.selected), fa.name);
		}
		
		if (in instanceof JCArrayTypeTree) {
			JCArrayTypeTree att = (JCArrayTypeTree) in;
			return maker.TypeArray(cloneType0(maker, att.elemtype));
		}
		
		if (in instanceof JCTypeApply) {
			JCTypeApply ta = (JCTypeApply) in;
			ListBuffer<JCExpression> lb = new ListBuffer<JCExpression>();
			for (JCExpression typeArg : ta.arguments) {
				lb.append(cloneType0(maker, typeArg));
			}
			return maker.TypeApply(cloneType0(maker, ta.clazz), lb.toList());
		}
		
		if (in instanceof JCWildcard) {
			JCWildcard w = (JCWildcard) in;
			JCExpression newInner = cloneType0(maker, w.inner);
			TypeBoundKind newKind;
			switch (w.getKind()) {
			case SUPER_WILDCARD:
				newKind = maker.TypeBoundKind(BoundKind.SUPER);
				break;
			case EXTENDS_WILDCARD:
				newKind = maker.TypeBoundKind(BoundKind.EXTENDS);
				break;
			default:
			case UNBOUNDED_WILDCARD:
				newKind = maker.TypeBoundKind(BoundKind.UNBOUND);
				break;
			}
			return maker.Wildcard(newKind, newInner);
		}
		
		// This is somewhat unsafe, but it's better than outright throwing an exception here. Returning null will just cause an exception down the pipeline.
		return (JCExpression) in;
	}
	
	private static final Pattern SECTION_FINDER = Pattern.compile("^\\s*\\**\\s*[-*][-*]+\\s*([GS]ETTER|WITHER)\\s*[-*][-*]+\\s*\\**\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	
	public static String stripLinesWithTagFromJavadoc(String javadoc, String regexpFragment) {
		Pattern p = Pattern.compile("^\\s*\\**\\s*" + regexpFragment + "\\s*\\**\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(javadoc);
		return m.replaceAll("");
	}
	
	public static String stripSectionsFromJavadoc(String javadoc) {
		Matcher m = SECTION_FINDER.matcher(javadoc);
		if (!m.find()) return javadoc;
		
		return javadoc.substring(0, m.start());
	}
	
	public static String[] splitJavadocOnSectionIfPresent(String javadoc, String sectionName) {
		Matcher m = SECTION_FINDER.matcher(javadoc);
		int getterSectionHeaderStart = -1;
		int getterSectionStart = -1;
		int getterSectionEnd = -1;
		while (m.find()) {
			if (m.group(1).equalsIgnoreCase(sectionName)) {
				getterSectionStart = m.end() + 1;
				getterSectionHeaderStart = m.start();
			} else if (getterSectionStart != -1) {
				getterSectionEnd = m.start();
			}
		}
		
		if (getterSectionStart != -1) {
			if (getterSectionEnd != -1) {
				return new String[] {javadoc.substring(getterSectionStart, getterSectionEnd), javadoc.substring(0, getterSectionHeaderStart) + javadoc.substring(getterSectionEnd)};
			} else {
				return new String[] {javadoc.substring(getterSectionStart), javadoc.substring(0, getterSectionHeaderStart)};
			}
		}
		
		return null;
	}
	
	public static enum CopyJavadoc {
		VERBATIM,
		GETTER {
			@Override public String[] split(String javadoc) {
				// step 1: Check if there is a 'GETTER' section. If yes, that becomes the new method's javadoc and we strip that from the original.
				String[] out = splitJavadocOnSectionIfPresent(javadoc, "GETTER");
				if (out != null) return out;
				// failing that, create a copy, but strip @return from the original and @param from the copy, as well as other sections.
				String copy = javadoc;
				javadoc = stripLinesWithTagFromJavadoc(javadoc, "@returns?\\s+.*");
				copy = stripLinesWithTagFromJavadoc(copy, "@param(?:eter)?\\s+.*");
				copy = stripSectionsFromJavadoc(copy);
				return new String[] {copy, javadoc};
			}
		},
		SETTER {
			@Override public String[] split(String javadoc) {
				return splitForSetters(javadoc, "SETTER");
			}
		},
		WITHER {
			@Override public String[] split(String javadoc) {
				return splitForSetters(javadoc, "WITHER");
			}
		};
		
		private static String[] splitForSetters(String javadoc, String sectionName) {
			// step 1: Check if there is a 'SETTER' section. If yes, that becomes the new one and we strip that from the original.
			String[] out = splitJavadocOnSectionIfPresent(javadoc, sectionName);
			if (out != null) return out;
			// failing that, create a copy, but strip @param from the original and @return from the copy.
			String copy = javadoc;
			javadoc = stripLinesWithTagFromJavadoc(javadoc, "@param(?:eter)?\\s+.*");
			copy = stripLinesWithTagFromJavadoc(copy, "@returns?\\s+.*");
			copy = stripSectionsFromJavadoc(copy);
			return new String[] {copy, javadoc};
		}
		
		/** Splits the javadoc into the section to be copied (ret[0]) and the section to replace the original with (ret[1]) */
		public String[] split(String javadoc) {
			return new String[] {javadoc, javadoc};
		}
	}
	
	/**
	 * Copies javadoc on one node to the other.
	 * 
	 * in 'GETTER' copyMode, first a 'GETTER' segment is searched for. If it exists, that will become the javadoc for the 'to' node, and this section is
	 * stripped out of the 'from' node. If no 'GETTER' segment is found, then the entire javadoc is taken minus any {@code @param} lines and other sections.
	 * any {@code @return} lines are stripped from 'from'.
	 * 
	 * in 'SETTER' mode, stripping works similarly to 'GETTER' mode, except {@code param} are copied and stripped from the original and {@code @return} are skipped.
	 */
	public static void copyJavadoc(JavacNode from, JCTree to, CopyJavadoc copyMode) {
		if (copyMode == null) copyMode = CopyJavadoc.VERBATIM;
		try {
			JCCompilationUnit cu = ((JCCompilationUnit) from.top().get());
			Object dc = Javac.getDocComments(cu);
			if (dc instanceof Map) {
				copyJavadoc_jdk6_7(from, to, copyMode, dc);
			} else if (Javac.instanceOfDocCommentTable(dc)) {
				CopyJavadoc_8.copyJavadoc(from, to, copyMode, dc);
			}
		} catch (Exception ignore) {}
	}
	
	private static final Pattern FIND_RETURN = Pattern.compile("^\\s*\\**\\s*@returns?\\s+.*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	static String addReturnsThisIfNeeded(String in) {
		if (FIND_RETURN.matcher(in).find()) return in;
		
		return addJavadocLine(in, "@return this");
	}
	
	static String addJavadocLine(String in, String line) {
		if (in.endsWith("\n")) return in + line + "\n";
		return in + "\n" + line;
	}
	
	private static class CopyJavadoc_8 {
		static void copyJavadoc(JavacNode from, JCTree to, CopyJavadoc copyMode, Object dc) {
			DocCommentTable dct = (DocCommentTable) dc;
			Comment javadoc = dct.getComment(from.get());
			
			if (javadoc != null) {
				String[] filtered = copyMode.split(javadoc.getText());
				if (copyMode == CopyJavadoc.SETTER && shouldReturnThis(from)) {
					filtered[0] = addReturnsThisIfNeeded(filtered[0]);
				}
				dct.putComment(to, createJavadocComment(filtered[0], from));
				dct.putComment(from.get(), createJavadocComment(filtered[1], from));
			}
		}
		
		private static Comment createJavadocComment(final String text, final JavacNode field) {
			return new Comment() {
				@Override public String getText() {
					return text;
				}
				
				@Override public int getSourcePos(int index) {
					return -1;
				}
				
				@Override public CommentStyle getStyle() {
					return CommentStyle.JAVADOC;
				}
				
				@Override public boolean isDeprecated() {
					return text.contains("@deprecated") && field.getKind() == Kind.FIELD && isFieldDeprecated(field);
				}
			};
		}
	}

	@SuppressWarnings({"unchecked", "all"})
	private static void copyJavadoc_jdk6_7(JavacNode from, JCTree to, CopyJavadoc copyMode, Object dc) {
		Map<JCTree, String> docComments = (Map<JCTree, String>) dc;
		String javadoc = docComments.get(from.get());
		
		if (javadoc != null) {
			String[] filtered = copyMode.split(javadoc);
			if (copyMode == CopyJavadoc.SETTER && shouldReturnThis(from)) {
				filtered[0] = addReturnsThisIfNeeded(filtered[0]);
			}
			docComments.put(to, filtered[0]);
			docComments.put(from.get(), filtered[1]);
		}
	}
}
