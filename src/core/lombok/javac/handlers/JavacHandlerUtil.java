/*
 * Copyright (C) 2009-2025 The Project Lombok Authors.
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
import static lombok.javac.JavacAugments.JCTree_keepPosition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
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
import com.sun.tools.javac.tree.JCTree.JCLiteral;
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
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.Getter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.CleanupTask;
import lombok.core.LombokImmutableList;
import lombok.core.TypeResolver;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.configuration.NullAnnotationLibrary;
import lombok.core.configuration.NullCheckExceptionType;
import lombok.core.configuration.TypeName;
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.delombok.LombokOptionsFactory;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import lombok.javac.Javac;
import lombok.javac.JavacAugments;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.JavacTreeMaker.TypeTag;
import lombok.permit.Permit;

/**
 * Container for static utility methods useful to handlers written for javac.
 */
public class JavacHandlerUtil {
	private JavacHandlerUtil() {
		//Prevent instantiation
	}
	
	private static class MarkingScanner extends TreeScanner {
		private final JavacNode source;
		
		MarkingScanner(JavacNode source) {
			this.source = source;
		}
		
		@Override public void scan(JCTree tree) {
			if (tree == null) return;
			if (JCTree_keepPosition.get(tree)) return;
			setGeneratedBy(tree, source);
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
	
	public static boolean inNetbeansEditor(Context context) {
		Options options = Options.instance(context);
		return (options.keySet().contains("ide") && !options.keySet().contains("backgroundCompilation"));
	}
	
	public static boolean inNetbeansCompileOnSave(Context context) {
		Options options = Options.instance(context);
		return (options.keySet().contains("ide") && options.keySet().contains("backgroundCompilation"));
	}
	
	public static JCTree getGeneratedBy(JCTree node) {
		return JCTree_generatedNode.get(node);
	}
	
	public static boolean isGenerated(JCTree node) {
		return getGeneratedBy(node) != null;
	}
	
	public static <T extends JCTree> T recursiveSetGeneratedBy(T node, JavacNode source) {
		if (node == null) return null;
		setGeneratedBy(node, source);
		node.accept(new MarkingScanner(source));
		return node;
	}
	
	public static <T extends JCTree> T setGeneratedBy(T node, JavacNode sourceNode) {
		if (node == null) return null;
		if (sourceNode == null) {
			JCTree_generatedNode.clear(node);
			return node;
		}
		JCTree_generatedNode.set(node, sourceNode.get());
		
		if (JCTree_keepPosition.get(node)) return node;
		if (inNetbeansEditor(sourceNode.getContext()) && !isParameter(node)) return node;
		
		node.pos = sourceNode.getStartPos();
		storeEnd(node, sourceNode.getEndPosition(), (JCCompilationUnit) sourceNode.top().get());
		return node;
	}

	public static boolean isParameter(JCTree node) {
		return node instanceof JCVariableDecl && (((JCVariableDecl) node).mods.flags & Flags.PARAMETER) != 0;
	}
	
	public static boolean hasAnnotation(String type, JavacNode node) {
		return hasAnnotation(type, node, false);
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
	
	private static boolean hasAnnotation(String type, JavacNode node, boolean delete) {
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
	
	public static JavacNode findInnerClass(JavacNode parent, String name) {
		for (JavacNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			JCClassDecl td = (JCClassDecl) child.get();
			if (td.name.contentEquals(name)) return child;
		}
		return null;
	}
	
	public static JavacNode findAnnotation(Class<? extends Annotation> type, JavacNode node) {
		return findAnnotation(type, node, false);
	}
	
	public static JavacNode findAnnotation(Class<? extends Annotation> type, JavacNode node, boolean delete) {
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
		return typeMatches(type, node, ((JCAnnotation) node.get()).annotationType);
	}
	
	/**
	 * Checks if the Annotation AST Node provided is likely to be an instance of the provided annotation type.
	 * 
	 * @param type An actual annotation type, such as {@code lombok.Getter.class}.
	 * @param node A Lombok AST node representing an annotation in source code.
	 */
	public static boolean annotationTypeMatches(String type, JavacNode node) {
		if (node.getKind() != Kind.ANNOTATION) return false;
		return typeMatches(type, node, ((JCAnnotation) node.get()).annotationType);
	}
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeNode A type reference to check.
	 */
	public static boolean typeMatches(Class<?> type, JavacNode node, JCTree typeNode) {
		return typeMatches(type.getName(), node, typeNode);
	}
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeNode A type reference to check.
	 */
	public static boolean typeMatches(String type, JavacNode node, JCTree typeNode) {
		String typeName = getTypeName(typeNode);
		return typeMatches(type, node, typeName);
	}

	private static boolean typeMatches(String type, JavacNode node, String typeName) {
		if (typeName == null || typeName.length() == 0) return false;
		int lastIndexA = typeName.lastIndexOf('.') + 1;
		int lastIndexB = Math.max(type.lastIndexOf('.'), type.lastIndexOf('$')) + 1;
		int len = typeName.length() - lastIndexA;
		if (len != type.length() - lastIndexB) return false;
		for (int i = 0; i < len; i++) if (typeName.charAt(i + lastIndexA) != type.charAt(i + lastIndexB)) return false;
		TypeResolver resolver = node.getImportListAsTypeResolver();
		return resolver.typeMatches(node, type, typeName);
	}

	private static String getTypeName(JCTree typeNode) {
		return typeNode == null ? null : typeNode.toString();
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
	
	public static CheckerFrameworkVersion getCheckerFrameworkVersion(JavacNode node) {
		CheckerFrameworkVersion cfv = node.getAst().readConfiguration(ConfigurationKeys.CHECKER_FRAMEWORK);
		return cfv == null ? CheckerFrameworkVersion.NONE : cfv;
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
		return createAnnotation(type, (JCAnnotation) node.get(), node);
	}
	
	/**
	 * Creates an instance of {@code AnnotationValues} for the provided AST Node
	 * and Annotation expression.
	 *
	 * @param type An annotation class type, such as {@code lombok.Getter.class}.
	 * @param anno the annotation expression
	 * @param node A Lombok AST node representing an annotation in source code.
	 */
	public static <A extends Annotation> AnnotationValues<A> createAnnotation(Class<A> type, JCAnnotation anno, final JavacNode node) {
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
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
				List<JCExpression> elems = ((JCNewArray) rhs).elems;
				for (JCExpression inner : elems) {
					raws.add(inner.toString());
					expressions.add(inner);
					if (inner instanceof JCAnnotation) {
						try {
							@SuppressWarnings("unchecked")
							Class<A> innerClass = (Class<A>) Class.forName(inner.type.toString());
							
							guesses.add(createAnnotation(innerClass, (JCAnnotation) inner, node));
						} catch (ClassNotFoundException ex) {
							guesses.add(calculateGuess(inner));
						}
					} else {
						guesses.add(calculateGuess(inner));
					}
					positions.add(inner.pos());
				}
			} else {
				raws.add(rhs.toString());
				expressions.add(rhs);
				if (rhs instanceof JCAnnotation) {
					try {
						@SuppressWarnings("unchecked")
						Class<A> innerClass = (Class<A>) Class.forName(rhs.type.toString());
						
						guesses.add(createAnnotation(innerClass, (JCAnnotation) rhs, node));
					} catch (ClassNotFoundException ex) {
						guesses.add(calculateGuess(rhs));
					}
				} else {
					guesses.add(calculateGuess(rhs));
				}
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
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, String annotationType) {
		deleteAnnotationIfNeccessary0(annotation, annotationType);
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType) {
		deleteAnnotationIfNeccessary0(annotation, annotationType.getName());
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType1, Class<? extends Annotation> annotationType2) {
		deleteAnnotationIfNeccessary0(annotation, annotationType1.getName(), annotationType2.getName());
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType1, String annotationType2) {
		deleteAnnotationIfNeccessary0(annotation, annotationType1.getName(), annotationType2);
	}
	
	private static void deleteAnnotationIfNeccessary0(JavacNode annotation, String... annotationTypes) {
		if (inNetbeansEditor(annotation)) return;
		if (!annotation.shouldDeleteLombokAnnotations()) return;
		JavacNode parentNode = annotation.directUp();
		switch (parentNode.getKind()) {
		case FIELD:
		case ARGUMENT:
		case LOCAL:
			JCVariableDecl variable = (JCVariableDecl) parentNode.get();
			variable.mods.annotations = filterList(variable.mods.annotations, annotation.get());
			
			if ((variable.mods.flags & GENERATED_MEMBER) != 0) {
				JavacNode typeNode = upToTypeNode(annotation);
				if (isRecord(typeNode)) {
					RecordComponentReflect.deleteAnnotation((JCClassDecl) typeNode.get(), variable, annotation.get());
				}
			}
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
		for (String annotationType : annotationTypes) {
			deleteImportFromCompilationUnit(annotation, annotationType.replace("$", "."));
		}
	}
	
	public static void deleteImportFromCompilationUnit(JavacNode node, String name) {
		if (inNetbeansEditor(node)) return;
		if (!node.shouldDeleteLombokAnnotations()) return;
		
		JCCompilationUnit unit = (JCCompilationUnit) node.top().get();
		
		for (JCTree def : unit.defs) {
			if (!(def instanceof JCImport)) continue;
			JCImport imp0rt = (JCImport) def;
			if (imp0rt.staticImport) continue;
			if (!Javac.getQualid(imp0rt).toString().equals(name)) continue;
			JavacAugments.JCImport_deletable.set(imp0rt, true);
		}
	}
	
	private static List<JCAnnotation> filterList(List<JCAnnotation> annotations, JCTree jcTree) {
		ListBuffer<JCAnnotation> newAnnotations = new ListBuffer<JCAnnotation>();
		for (JCAnnotation ann : annotations) {
			if (jcTree != ann) newAnnotations.append(ann);
		}
		return newAnnotations.toList();
	}
	
	private static List<JCAnnotation> filterListByPos(List<JCAnnotation> annotations, JCTree jcTree) {
		ListBuffer<JCAnnotation> newAnnotations = new ListBuffer<JCAnnotation>();
		for (JCAnnotation ann : annotations) {
			if (jcTree.pos != ann.pos) newAnnotations.append(ann);
		}
		return newAnnotations.toList();
	}
	
	
	static class RecordComponentReflect {
		
		private static final Field astField;
		private static final Field originalAnnos;
		private static final Method getRecordComponents = Permit.permissiveGetMethod(ClassSymbol.class, "getRecordComponents");
		
		static {
			Field a = null;
			Field o = null;
			try {
				Class<?> forName = Class.forName("com.sun.tools.javac.code.Symbol$RecordComponent");
				a = Permit.permissiveGetField(forName, "ast");
				o = Permit.permissiveGetField(forName, "originalAnnos");
			} catch (Throwable e) {
				// Ignore
			}
			astField = a;
			originalAnnos = o;
		}
		
		static void deleteAnnotation(JCClassDecl record, JCVariableDecl component, JCTree annotation) {
			if ((astField == null && originalAnnos == null) || getRecordComponents == null) return;
			
			try {
				List<?> recordComponents = (List<?>) Permit.invokeSneaky(getRecordComponents, record.sym);
				
				for (Object recordComponent : recordComponents) {
					Name recordComponentName = ((Symbol) recordComponent).name;
					if (!recordComponentName.equals(component.name)) continue;
					
					if (astField != null) {
						// OpenJDK
						JCVariableDecl variable = Permit.get(astField, recordComponent);
						variable.mods.annotations =  filterListByPos(variable.mods.annotations, annotation);
					} else {
						// Zulu JDK 17
						List<JCAnnotation> annotations = Permit.get(originalAnnos, recordComponent);
						Permit.set(originalAnnos, recordComponent, filterListByPos(annotations, annotation));
					}
					return;
				}
			} catch (Throwable e) {
				// Ignore
			}
		}
	}
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_LOMBOK, EXISTS_BY_USER;
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllGetterNames(JavacNode field) {
		return HandlerUtil.toAllGetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllGetterNames(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllGetterNames(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(JavacNode field) {
		return HandlerUtil.toGetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toGetterName(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(JavacNode field) {
		return HandlerUtil.toAllSetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllSetterNames(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(JavacNode field) {
		return HandlerUtil.toSetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toSetterName(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible with names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithNames(JavacNode field) {
		return HandlerUtil.toAllWithNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible with names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithNames(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllWithNames(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible withBy names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithByNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithByNames(JavacNode field) {
		return HandlerUtil.toAllWithByNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible withBy names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithByNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithByNames(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllWithByNames(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely with name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithName(JavacNode field) {
		return HandlerUtil.toWithName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely with name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithName(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toWithName(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely withBy name for the stated field. (e.g. private boolean foo; to withFooBy).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithByName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithByName(JavacNode field) {
		return HandlerUtil.toWithByName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely withBy name for the stated field. (e.g. private boolean foo; to withFooBy).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithByName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithByName(JavacNode field, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toWithByName(field.getAst(), accessors, field.getName(), isBoolean(field));
	}
	
	/**
	 * When generating a setter, the setter either returns void (beanspec) or Self (fluent).
	 * This method scans for the {@code Accessors} annotation to figure that out.
	 */
	public static boolean shouldReturnThis(JavacNode field, AnnotationValues<Accessors> accessors) {
		if ((((JCVariableDecl) field.get()).mods.flags & Flags.STATIC) != 0) return false;
		
		return HandlerUtil.shouldReturnThis0(accessors, field.getAst());
	}
	
	/**
	 * When generating a setter/getter/wither, should it be made final?
	 */
	public static boolean shouldMakeFinal(JavacNode field, AnnotationValues<Accessors> accessors) {
		if ((((JCVariableDecl) field.get()).mods.flags & Flags.STATIC) != 0) return false;
		
		return HandlerUtil.shouldMakeFinal0(accessors, field.getAst());
	}
	
	public static JCExpression cloneSelfType(JavacNode childOfType) {
		JavacNode typeNode = childOfType;
		JavacTreeMaker maker = childOfType.getTreeMaker();
		while (typeNode != null && typeNode.getKind() != Kind.TYPE) typeNode = typeNode.up();
		return JavacHandlerUtil.namePlusTypeParamsToTypeReference(maker, typeNode, ((JCClassDecl) typeNode.get()).typarams);
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
		AnnotationValues<Accessors> values = null;
		
		for (JavacNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				values = createAnnotation(Accessors.class, node);
				break;
			}
		}
		
		JavacNode current = field.up();
		while (current != null) {
			for (JavacNode node : current.down()) {
				if (annotationTypeMatches(Accessors.class, node)) {
					AnnotationValues<Accessors> onType = createAnnotation(Accessors.class, node);
					values = values == null ? onType : values.integrate(onType);
					break;
				}
			}
			current = current.up();
		}
		
		return values == null ? AnnotationValues.of(Accessors.class, field) : values;
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
			for (JCTree def : ((JCClassDecl) node.get()).defs) {
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
		String name = findMethodInvocationName(statement);
		return "this".equals(name) || "super".equals(name);
	}
	
	public static boolean isThisCall(final JCStatement statement) {
		String name = findMethodInvocationName(statement);
		return "this".equals(name);
	}
	
	private static String findMethodInvocationName(final JCStatement statement) {
		if (!(statement instanceof JCExpressionStatement)) return null;
		JCExpression expr = ((JCExpressionStatement) statement).expr;
		if (!(expr instanceof JCMethodInvocation)) return null;
		JCExpression invocation = ((JCMethodInvocation) expr).meth;
		String name = null;
		if (invocation instanceof JCFieldAccess) {
			name = ((JCFieldAccess) invocation).name.toString();
		} else if (invocation instanceof JCIdent) {
			name = ((JCIdent) invocation).name.toString();
		}
		return name;
	}
	
	/**
	 * Turns an {@code AccessLevel} instance into the flag bit used by javac.
	 */
	@SuppressWarnings("deprecation") // We have to use MODULE here to make it act according to spec, which is to treat it like `PACKAGE`.
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
		
		if (!hasGetterAnnotation && HandleGetter.fieldQualifiesForGetterGeneration(field)) {
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
		if (field.getKind() == Kind.METHOD) return ((JCMethodDecl) field.get()).restype;
		
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		
		if (getter == null) {
			return ((JCVariableDecl) field.get()).vartype;
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
	
	static JCExpression createMethodAccessor(JavacTreeMaker maker, JavacNode method) {
		return createMethodAccessor(maker, method, null);
	}
	
	static JCExpression createMethodAccessor(JavacTreeMaker maker, JavacNode method, JCExpression receiver) {
		JCMethodDecl methodDecl = (JCMethodDecl) method.get();
		
		if (receiver == null && (methodDecl.mods.flags & Flags.STATIC) == 0) {
			receiver = maker.Ident(method.toName("this"));
		} else if (receiver == null) {
			JavacNode containerNode = method.up();
			if (containerNode != null && containerNode.get() instanceof JCClassDecl) {
				JCClassDecl container = (JCClassDecl) method.up().get();
				receiver = maker.Ident(container.name);
			}
		}
		
		JCMethodInvocation call = maker.Apply(List.<JCExpression>nil(),
			receiver == null ? maker.Ident(methodDecl.name) : maker.Select(receiver, methodDecl.name), List.<JCExpression>nil());
		return call;
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
	
	public static JavacNode injectField(JavacNode typeNode, JCVariableDecl field, boolean addGenerated) {
		return injectField(typeNode, field, addGenerated, false);
	}
	
	public static JavacNode injectField(JavacNode typeNode, JCVariableDecl field, boolean addGenerated, boolean specialEnumHandling) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		if (addGenerated) {
			addSuppressWarningsAll(field.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(field)), typeNode.getContext());
			addGenerated(field.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(field)), typeNode.getContext());
		}
		
		List<JCTree> insertAfter = null;
		List<JCTree> insertBefore = type.defs;
		while (true) {
			boolean skip = false;
			if (insertBefore.head instanceof JCVariableDecl) {
				JCVariableDecl f = (JCVariableDecl) insertBefore.head;
				if ((!specialEnumHandling && isEnumConstant(f)) || isGenerated(f)) skip = true;
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
		
		EnterReflect.memberEnter(field, typeNode);
		
		return typeNode.add(field, Kind.FIELD);
	}
	
	public static boolean isEnumConstant(final JCVariableDecl field) {
		return (field.mods.flags & Flags.ENUM) != 0;
	}
	
	static class JCAnnotatedTypeReflect {
		private static final Class<?> TYPE;
		private static final Constructor<?> CONSTRUCTOR;
		private static final Field ANNOTATIONS, UNDERLYING_TYPE;
		
		static {
			Class<?> t = null;
			Constructor<?> c = null;
			Field a = null;
			Field u = null;
			
			try {
				t = Class.forName("com.sun.tools.javac.tree.JCTree$JCAnnotatedType");
				c = Permit.getConstructor(t, List.class, JCExpression.class);
				a = Permit.permissiveGetField(t, "annotations");
				u = Permit.permissiveGetField(t, "underlyingType");
			} catch (Exception e) {
				// Ignore
			}
			
			TYPE = t;
			CONSTRUCTOR = c;
			ANNOTATIONS = a;
			UNDERLYING_TYPE = u;
		}
		
		static boolean is(JCTree obj) {
			if (obj == null) return false;
			return obj.getClass() == TYPE;
		}
		
		@SuppressWarnings("unchecked")
		static List<JCAnnotation> getAnnotations(JCTree obj) {
			if (ANNOTATIONS == null) return List.nil();
			try {
				return (List<JCAnnotation>) ANNOTATIONS.get(obj);
			} catch (Exception e) {
				return List.nil();
			}
		}
		
		static void setAnnotations(JCTree obj, List<JCAnnotation> anns) {
			if (ANNOTATIONS == null) return;
			try {
				ANNOTATIONS.set(obj, anns);
			} catch (Exception e) {
				// Ignore
			}
		}
		
		static JCExpression getUnderlyingType(JCTree obj) {
			if (UNDERLYING_TYPE == null) return null;
			try {
				return (JCExpression) UNDERLYING_TYPE.get(obj);
			} catch (Exception e) {
				return null;
			}
		}
		
		static JCExpression create(List<JCAnnotation> annotations, JCExpression underlyingType) {
			if (CONSTRUCTOR == null) return underlyingType;
			try {
				return (JCExpression) CONSTRUCTOR.newInstance(annotations, underlyingType);
			} catch (Exception e) {
				return underlyingType;
			}
		}
	}
	
	static class JCAnnotationReflect {
		private static final Field ATTRIBUTE;

		static {
			ATTRIBUTE = Permit.permissiveGetField(JCAnnotation.class, "attribute");
		}

		static Attribute.Compound getAttribute(JCAnnotation jcAnnotation) {
			if (ATTRIBUTE != null) {
				try {
					return (Attribute.Compound) ATTRIBUTE.get(jcAnnotation);
				} catch (Exception e) {
					// Ignore
				}
			}
			return null;
		}
		
		static void setAttribute(JCAnnotation jcAnnotation, Attribute.Compound attribute) {
			if (ATTRIBUTE != null) {
				try {
					Permit.set(ATTRIBUTE, jcAnnotation, attribute);
				} catch (Exception e) {
					// Ignore
				}
			}
		}
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
				f = Permit.getField(ClassSymbol.class, "members_field");
				r = Permit.getMethod(f.getType(), "remove", Symbol.class);
				e = Permit.getMethod(f.getType(), "enter", Symbol.class);
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
				Permit.invoke(removeMethod, scope, toRemove);
			} catch (Exception e) {}
		}
		
		static void enter(ClassSymbol from, Symbol toEnter) {
			if (from == null) return;
			try {
				Scope scope = (Scope) membersField.get(from);
				if (scope == null) return;
				Permit.invoke(enterMethod, scope, toEnter);
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Adds the given new method declaration to the provided type AST Node.
	 * Can also inject constructors.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectMethod(JavacNode typeNode, JCMethodDecl method) {
		JavacNode source = typeNode.getNodeFor(getGeneratedBy(method));
		injectMethod(typeNode, source, method);
	}
	
	/**
	 * Adds the given new method declaration to the provided type AST Node.
	 * Can also inject constructors.
	 * 
	 * Also takes care of updating the JavacAST.
	 * 
	 * Ordinarily the 'source' is determined automatically. In rare cases generally involving combinations between
	 * annotations, some of which require re-attribution such as {@code @Delegate}, that doesn't work. This method
	 * exists if you explicitly have the source.
	 */
	public static void injectMethod(JavacNode typeNode, JavacNode source, JCMethodDecl method) {
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
						ClassSymbolMembersField.remove(type.sym, ((JCMethodDecl) def).sym);
						break;
					}
				}
				idx++;
			}
		}
		
		addSuppressWarningsAll(method.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(method)), typeNode.getContext());
		addGenerated(method.mods, typeNode, source, typeNode.getContext());
		type.defs = type.defs.append(method);
		
		EnterReflect.memberEnter(method, typeNode);
		
		typeNode.add(method, Kind.METHOD);
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
		addSuppressWarningsAll(type.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(type)), typeNode.getContext());
		addGenerated(type.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(type)), typeNode.getContext());
		typeDecl.defs = typeDecl.defs.append(type);
		
		EnterReflect.classEnter(type, typeNode);
		
		return typeNode.add(type, Kind.TYPE);
	}
	
	static class EnterReflect {
		private static final Method classEnter;
		private static final Method memberEnter;
		private static final Method blockAnnotations;
		private static final Method unblockAnnotations;
		
		static {
			classEnter = Permit.permissiveGetMethod(Enter.class, "classEnter", JCTree.class, Env.class);
			memberEnter = Permit.permissiveGetMethod(MemberEnter.class, "memberEnter", JCTree.class, Env.class);
			
			Method block = Permit.permissiveGetMethod(Annotate.class, "blockAnnotations");
			if (block == null) block = Permit.permissiveGetMethod(Annotate.class, "enterStart");
			blockAnnotations = block;
			
			Method unblock = Permit.permissiveGetMethod(Annotate.class, "unblockAnnotations");
			if (unblock == null) unblock = Permit.permissiveGetMethod(Annotate.class, "enterDone");
			unblockAnnotations = unblock;
		}
		
		static Type classEnter(JCTree tree, JavacNode parent) {
			Enter enter = Enter.instance(parent.getContext());
			Env<AttrContext> classEnv = enter.getEnv((TypeSymbol) parent.getElement());
			if (classEnv == null) return null;
			Type type = (Type) Permit.invokeSneaky(classEnter, enter, tree, classEnv);
			if (type == null) return null;
			type.complete();
			return type;
		}
		
		static void memberEnter(JCTree tree, JavacNode parent) {
			Context context = parent.getContext();
			MemberEnter me = MemberEnter.instance(context);
			Annotate annotate = Annotate.instance(context);
			Enter enter = Enter.instance(context);
			
			Env<AttrContext> classEnv = enter.getEnv((TypeSymbol) parent.getElement());
			if (classEnv == null) return;
			
			Permit.invokeSneaky(blockAnnotations, annotate);
			Permit.invokeSneaky(memberEnter, me, tree, classEnv);
			Permit.invokeSneaky(unblockAnnotations, annotate);
		}
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
	
	public static void addSuppressWarningsAll(JCModifiers mods, JavacNode node, JavacNode source, Context context) {
		if (!LombokOptionsFactory.getDelombokOptions(context).getFormatPreferences().generateSuppressWarnings()) return;
		
		boolean addJLSuppress = !Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_SUPPRESSWARNINGS_ANNOTATIONS));
		
		if (addJLSuppress) {
			for (JCAnnotation ann : mods.annotations) {
				JCTree type = ann.getAnnotationType();
				Name n = null;
				if (type instanceof JCIdent) n = ((JCIdent) type).name;
				else if (type instanceof JCFieldAccess) n = ((JCFieldAccess) type).name;
				if (n != null && n.contentEquals("SuppressWarnings")) {
					addJLSuppress = false;
				}
			}
		}
		if (addJLSuppress) addAnnotation(mods, node, source, "java.lang.SuppressWarnings", node.getTreeMaker().Literal("all"));
		
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS))) {
			JavacTreeMaker maker = node.getTreeMaker();
			JCExpression arg = maker.Assign(maker.Ident(node.toName("justification")), maker.Literal("generated code"));
			addAnnotation(mods, node, source, "edu.umd.cs.findbugs.annotations.SuppressFBWarnings", arg);
		}
	}
	
	public static void addGenerated(JCModifiers mods, JavacNode node, JavacNode source, Context context) {
		if (!LombokOptionsFactory.getDelombokOptions(context).getFormatPreferences().generateGenerated()) return;
		
		if (HandlerUtil.shouldAddGenerated(node)) {
			addAnnotation(mods, node, source, "javax.annotation.Generated", node.getTreeMaker().Literal("lombok"));
		}
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_JAKARTA_GENERATED_ANNOTATIONS))) {
			addAnnotation(mods, node, source, "jakarta.annotation.Generated", node.getTreeMaker().Literal("lombok"));
		}
		if (!Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_LOMBOK_GENERATED_ANNOTATIONS))) {
			addAnnotation(mods, node, source, "lombok.Generated", null);
		}
	}
	
	public static void addAnnotation(JCModifiers mods, JavacNode node, JavacNode source, String annotationTypeFqn, JCExpression arg) {
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
		List<JCExpression> argList = arg != null ? List.of(arg) : List.<JCExpression>nil();
		JCAnnotation annotation = recursiveSetGeneratedBy(maker.Annotation(annType, argList), source);
		mods.annotations = mods.annotations.append(annotation);
	}
	
	static JCExpression addCheckerFrameworkReturnsReceiver(JCExpression returnType, JavacTreeMaker maker, JavacNode typeNode, CheckerFrameworkVersion cfv) {
		if (cfv.generateReturnsReceiver()) {
			JCAnnotation rrAnnotation = maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__RETURNS_RECEIVER), List.<JCExpression>nil());
			returnType = maker.AnnotatedType(List.of(rrAnnotation), returnType);
		}
		return returnType;
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
	
	public static String scanForNearestAnnotation(JavacNode node, String... anns) {
		while (node != null) {
			for (JavacNode ann : node.down()) {
				if (ann.getKind() != Kind.ANNOTATION) continue;
				JCAnnotation a = (JCAnnotation) ann.get();
				for (String annToFind : anns) if (typeMatches(annToFind, node, a.annotationType)) return annToFind;
			}
			node = node.up();
		}
		
		return null;
	}
	
	public static boolean hasNonNullAnnotations(JavacNode node) {
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				String annotationTypeName = getTypeName(annotation.annotationType);
				for (String nn : NONNULL_ANNOTATIONS) if (typeMatches(nn, node, annotationTypeName)) return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasNonNullAnnotations(JavacNode node, List<JCAnnotation> anns) {
		if (anns == null) return false;
		for (JCAnnotation ann : anns) {
			String annotationTypeName = getTypeName(ann.annotationType);
			for (String nn : NONNULL_ANNOTATIONS) if (typeMatches(nn, node, annotationTypeName)) return true;
		}
		
		return false;
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that is 'copyable' (either via configuration or from the base list).
	 */
	public static List<JCAnnotation> findCopyableAnnotations(JavacNode node) {
		java.util.List<TypeName> configuredCopyable = node.getAst().readConfiguration(ConfigurationKeys.COPYABLE_ANNOTATIONS);
		
		ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				String annotationTypeName = getTypeName(annotation.annotationType);
				boolean match = false;
				for (TypeName cn : configuredCopyable) if (cn != null && typeMatches(cn.toString(), node, annotationTypeName)) {
					result.append(annotation);
					match = true;
					break;
				}
				if (!match) for (String bn : BASE_COPYABLE_ANNOTATIONS) if (typeMatches(bn, node, annotationTypeName)) {
					result.append(annotation);
					break;
				}
			}
		}

		return copyAnnotations(result.toList(), node.getTreeMaker());
	}
	
	/**
	 * Searches the given field node for annotations that are specifically intended to be copied to the getter.
	 * 
	 * @param forceCopyJacksonAnnotations If {@code true}, always copy the annotations regardless of lombok configuration key {@code lombok.copyJacksonAnnotationsToAccessors}.
	 */
	public static List<JCAnnotation> findCopyableToGetterAnnotations(JavacNode node, boolean forceCopyJacksonAnnotations) {
		if (!forceCopyJacksonAnnotations) {
			Boolean copyAnnotations = node.getAst().readConfiguration(ConfigurationKeys.COPY_JACKSON_ANNOTATIONS_TO_ACCESSORS);
			if (copyAnnotations == null || !copyAnnotations) return List.nil();
		}
		return findAnnotationsInList(node, JACKSON_COPY_TO_GETTER_ANNOTATIONS);
	}

	/**
	 * Searches the given field node for annotations that are specifically intended to be copied to the setter.
	 * 
	 * @param forceCopyJacksonAnnotations If {@code true}, always copy the annotations regardless of lombok configuration key {@code lombok.copyJacksonAnnotationsToAccessors}.
	 */
	public static List<JCAnnotation> findCopyableToSetterAnnotations(JavacNode node, boolean forceCopyJacksonAnnotations) {
		if (!forceCopyJacksonAnnotations) {
			Boolean copyAnnotations = node.getAst().readConfiguration(ConfigurationKeys.COPY_JACKSON_ANNOTATIONS_TO_ACCESSORS);
			if (copyAnnotations == null || !copyAnnotations) return List.nil();
		}
		return findAnnotationsInList(node, JACKSON_COPY_TO_SETTER_ANNOTATIONS);
	}

	/**
	 * Searches the given field node for annotations that are specifically intended to be copied to the builder's singular method.
	 */
	public static List<JCAnnotation> findCopyableToBuilderSingularSetterAnnotations(JavacNode node) {
		return findAnnotationsInList(node, JACKSON_COPY_TO_BUILDER_SINGULAR_SETTER_ANNOTATIONS);
	}
	
	/**
	 * Searches the given field node for annotations that are in the given list, and returns those.
	 */
	private static List<JCAnnotation> findAnnotationsInList(JavacNode node, java.util.List<String> annotationsToFind) {
		JCAnnotation anno = null;
		String annoName = null;
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (anno != null) {
					annoName = "";
					break;
				}
				JCAnnotation annotation = (JCAnnotation) child.get();
				annoName = annotation.annotationType.toString();
				anno = annotation;
			}
		}
		
		if (annoName == null) return List.nil();
		
		if (!annoName.isEmpty()) {
			for (String bn : annotationsToFind) if (typeMatches(bn, node, annoName)) return copyAnnotations(List.of(anno), node.getTreeMaker());
		}
		
		ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				String annotationTypeName = getTypeName(annotation.annotationType);
				boolean match = false;
				if (!match) for (String bn : annotationsToFind) if (typeMatches(bn, node, annotationTypeName)) {
					result.append(annotation);
					break;
				}
			}
		}
		return copyAnnotations(result.toList(), node.getTreeMaker());
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a configured exception with the
	 * variable name as message.
	 */
	public static JCStatement generateNullCheck(JavacTreeMaker maker, JavacNode variable, JavacNode source) {
		return generateNullCheck(maker, (JCVariableDecl) variable.get(), source);
	}
	
	/**
	 * Generates a new statement that checks if the given local is null, and if so, throws a configured exception with the
	 * local variable name as message. 
	 */
	public static JCStatement generateNullCheck(JavacTreeMaker maker, JCExpression typeNode, Name varName, JavacNode source, String customMessage) {
		NullCheckExceptionType exceptionType = source.getAst().readConfiguration(ConfigurationKeys.NON_NULL_EXCEPTION_TYPE);
		if (exceptionType == null) exceptionType = NullCheckExceptionType.NULL_POINTER_EXCEPTION;
		
		if (typeNode != null && isPrimitive(typeNode)) return null;
		JCLiteral message = maker.Literal(exceptionType.toExceptionMessage(varName.toString(), customMessage));
		
		LombokImmutableList<String> method = exceptionType.getMethod();
		if (method != null) {
			return maker.Exec(maker.Apply(List.<JCExpression>nil(), chainDots(source, method), List.of(maker.Ident(varName), message)));
		}
		
		if (exceptionType == NullCheckExceptionType.ASSERTION) {
			return maker.Assert(maker.Binary(CTC_NOT_EQUAL, maker.Ident(varName), maker.Literal(CTC_BOT, null)), message);
		}
		
		JCExpression exType = genTypeRef(source, exceptionType.getExceptionType());
		JCExpression exception = maker.NewClass(null, List.<JCExpression>nil(), exType, List.<JCExpression>of(message), null);
		JCStatement throwStatement = maker.Throw(exception);
		JCBlock throwBlock = maker.Block(0, List.of(throwStatement));
		return maker.If(maker.Binary(CTC_EQUAL, maker.Ident(varName), maker.Literal(CTC_BOT, null)), throwBlock, null);
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a configured exception with the
	 * variable name as message. 
	 * 
	 * This is a special case method reserved for use when the provided declaration differs from the
	 * variable's declaration, i.e. in a constructor or setter where the local parameter is named the same but with the prefix
	 * stripped as a result of @Accessors.prefix.
	 */
	public static JCStatement generateNullCheck(JavacTreeMaker maker, JCVariableDecl varDecl, JavacNode source) {
		return generateNullCheck(maker, varDecl.vartype, varDecl.name, source, null);
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
		for (JCAnnotation annotation : result) {
			clearTypes(annotation);
		}
		ast.args = params.toList();
		return result.toList();
	}
	
	/**
	 * Removes all type information from the provided tree.
	 */
	private static void clearTypes(JCTree tree) {
		tree.accept(new TreeScanner() {
			@Override public void scan(JCTree tree) {
				if (tree == null) return;
				tree.type = null;
				super.scan(tree);
			}
			@Override public void visitClassDef(JCClassDecl tree) {
				tree.sym = null;
				super.visitClassDef(tree);
			}
			@Override public void visitMethodDef(JCMethodDecl tree) {
				tree.sym = null;
				super.visitMethodDef(tree);
			}
			@Override public void visitVarDef(JCVariableDecl tree) {
				tree.sym = null;
				super.visitVarDef(tree);
			}
			@Override public void visitSelect(JCFieldAccess tree) {
				tree.sym = null;
				super.visitSelect(tree);
			}
			@Override public void visitIdent(JCIdent tree) {
				tree.sym = null;
				super.visitIdent(tree);
			}
			@Override public void visitAnnotation(JCAnnotation tree) {
				JCAnnotationReflect.setAttribute(tree, null);
				super.visitAnnotation(tree);
			}
		});
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
		for (JCTypeParameter tp : params) {
			List<JCExpression> bounds = tp.bounds;
			if (bounds != null && !bounds.isEmpty()) {
				ListBuffer<JCExpression> boundsCopy = new ListBuffer<JCExpression>();
				for (JCExpression expr : tp.bounds) {
					boundsCopy.append(cloneType(maker, expr, source));
				}
				bounds = boundsCopy.toList();
			}
			out.append(maker.TypeParameter(tp.name, bounds));
		}
		return out.toList();
	}
	
	public static List<JCAnnotation> getTypeUseAnnotations(JCExpression from) {
		if (!JCAnnotatedTypeReflect.is(from)) return List.nil();
		return JCAnnotatedTypeReflect.getAnnotations(from);
	}
	
	public static JCExpression removeTypeUseAnnotations(JCExpression from) {
		if (!JCAnnotatedTypeReflect.is(from)) return from;
		return JCAnnotatedTypeReflect.getUnderlyingType(from);
	}
	
	public static JCExpression namePlusTypeParamsToTypeReference(JavacTreeMaker maker, JavacNode type, List<JCTypeParameter> params) {
		JCClassDecl td = (JCClassDecl) type.get();
		boolean instance = !type.isStatic();
		return namePlusTypeParamsToTypeReference(maker, type.up(), td.name, instance, params, List.<JCAnnotation>nil());
	}
	
	public static JCExpression namePlusTypeParamsToTypeReference(JavacTreeMaker maker, JavacNode type, List<JCTypeParameter> params, List<JCAnnotation> annotations) {
		JCClassDecl td = (JCClassDecl) type.get();
		boolean instance = !type.isStatic();
		return namePlusTypeParamsToTypeReference(maker, type.up(), td.name, instance, params, annotations);
	}
	
	public static JCExpression namePlusTypeParamsToTypeReference(JavacTreeMaker maker, JavacNode parentType, Name typeName, boolean instance, List<JCTypeParameter> params) {
		return namePlusTypeParamsToTypeReference(maker, parentType, typeName, instance, params, List.<JCAnnotation>nil());
	}
	
	public static JCExpression namePlusTypeParamsToTypeReference(JavacTreeMaker maker, JavacNode parentType, Name typeName, boolean instance, List<JCTypeParameter> params, List<JCAnnotation> annotations) {
		JCExpression r = null;
		if (parentType != null && parentType.getKind() == Kind.TYPE && !parentType.getName().isEmpty()) {
			JCClassDecl td = (JCClassDecl) parentType.get();
			boolean outerInstance = instance && !parentType.isStatic();
			List<JCTypeParameter> outerParams = instance ? td.typarams : List.<JCTypeParameter>nil();
			r = namePlusTypeParamsToTypeReference(maker, parentType.up(), td.name, outerInstance, outerParams, List.<JCAnnotation>nil());
		}
		
		r = r == null ? maker.Ident(typeName) : maker.Select(r, typeName);
		if (!annotations.isEmpty()) r = JCAnnotatedTypeReflect.create(annotations, r);
		if (!params.isEmpty()) r = maker.TypeApply(r, typeParameterNames(maker, params));
		return r;
	}
	
	public static List<JCExpression> typeParameterNames(JavacTreeMaker maker, List<JCTypeParameter> params) {
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter param : params) {
			typeArgs.append(maker.Ident(param.name));
		}
		return typeArgs.toList();
	}
	
	public static void sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(JavacNode typeNode, JavacNode errorNode) {
		List<String> disallowed = List.nil();
		for (JavacNode child : typeNode.down()) {
			for (String annType : INVALID_ON_BUILDERS) {
				if (annotationTypeMatches(annType, child)) {
					int lastIndex = annType.lastIndexOf('.');
					disallowed = disallowed.append(lastIndex == -1 ? annType : annType.substring(lastIndex + 1));
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
	
	static List<JCAnnotation> copyAnnotations(List<? extends JCExpression> in, JavacTreeMaker maker) {
		ListBuffer<JCAnnotation> out = new ListBuffer<JCAnnotation>();
		for (JCExpression expr : in) {
			if (!(expr instanceof JCAnnotation)) continue;
			out.append(copyExpression((JCAnnotation) expr, maker));
		}
		return out.toList();
	}
	
	@SuppressWarnings("unchecked")
	static <T extends JCExpression> T copyExpression(T expression, JavacTreeMaker maker) {
		TreeVisitor<JCTree, Void> visitor = new TreeCopier<Void>(maker.getUnderlyingTreeMaker());
		return (T) expression.accept(visitor, null);
	}
	
	static List<JCAnnotation> mergeAnnotations(List<JCAnnotation> a, List<JCAnnotation> b) {
		if (a == null || a.isEmpty()) return b;
		if (b == null || b.isEmpty()) return a;
		ListBuffer<JCAnnotation> out = new ListBuffer<JCAnnotation>();
		for (JCAnnotation ann : a) out.append(ann);
		for (JCAnnotation ann : b) out.append(ann);
		return out.toList();
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class and not some other type declaration (so, not an annotation definition, interface, enum, or record).
	 */
	public static boolean isClass(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION | RECORD);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class or enum and not some other type declaration (so, not an annotation definition, interface, or record).
	 */
	public static boolean isClassOrEnum(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.INTERFACE | Flags.ANNOTATION | RECORD);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class an enum or an interface and not some other type declaration (so, not an annotation definition, or record).
	 */
	public static boolean isClassEnumOrInterface(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.ANNOTATION | RECORD);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class, an enum, an interface or a record and not some other type declaration (so, not an annotation definition).
	 */
	public static boolean isClassEnumInterfaceOrRecord(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.ANNOTATION);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class, an enum or a record and not some other type declaration (so, not an annotation definition or interface).
	 */
	public static boolean isClassEnumOrRecord(JavacNode typeNode) {
		return isClassAndDoesNotHaveFlags(typeNode, Flags.INTERFACE | Flags.ANNOTATION);
	}
	
	/**
	 * Returns {@code true} if the provided node is a record declaration (so, not an annotation definition, interface, enum, or plain class).
	 */
	public static boolean isRecord(JavacNode typeNode) {
		return typeNode.getKind() == Kind.TYPE && (((JCClassDecl) typeNode.get()).mods.flags & RECORD) != 0;
	}
	
	public static boolean isClassAndDoesNotHaveFlags(JavacNode typeNode, long flags) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		else return false;
		
		long typeDeclflags = typeDecl == null ? 0 : typeDecl.mods.flags;
		return (typeDeclflags & flags) == 0;
	}
	
	/**
	 * Returns {@code true} if the provided node supports static methods and types (top level or static class)
	 */
	public static boolean isStaticAllowed(JavacNode typeNode) {
		return typeNode.isStatic() || typeNode.up() == null || typeNode.up().getKind() == Kind.COMPILATION_UNIT || isRecord(typeNode);
	}
	
	public static JavacNode upToTypeNode(JavacNode node) {
		if (node == null) throw new NullPointerException("node");
		while ((node != null) && !(node.get() instanceof JCClassDecl)) node = node.up();
		
		return node;
	}
	
	public static List<JCExpression> cloneTypes(JavacTreeMaker maker, List<JCExpression> in, JavacNode source) {
		if (in.isEmpty()) return List.nil();
		if (in.size() == 1) return List.of(cloneType(maker, in.get(0), source));
		ListBuffer<JCExpression> lb = new ListBuffer<JCExpression>();
		for (JCExpression expr : in) lb.append(cloneType(maker, expr, source));
		return lb.toList();
	}
	
	/**
	 * Creates a full clone of a given javac AST type node. Every part is cloned (every identifier, every select, every wildcard, every type apply, every type_use annotation).
	 * 
	 * If there's any node in the tree that we don't know how to clone, that part isn't cloned. However, we wouldn't know what could possibly show up that we
	 * can't currently clone; that's just a safeguard.
	 * 
	 * This should be used if the type looks the same in the code, but resolves differently. For example, a static method that has some generics in it named after
	 * the class's own parameter, but as its a static method, the static method's notion of {@code T} is different from the class notion of {@code T}. If you're duplicating
	 * a type used in the class context, you need to use this method.
	 */
	public static JCExpression cloneType(JavacTreeMaker maker, JCExpression in, JavacNode source) {
		JCExpression out = cloneType0(maker, in);
		if (out != null) recursiveSetGeneratedBy(out, source);
		return out;
	}
	
	private static JCExpression cloneType0(JavacTreeMaker maker, JCTree in) {
		if (in == null) return null;
		
		if (in instanceof JCPrimitiveTypeTree) {
			return maker.TypeIdent(TypeTag.typeTag(in));
		}
		
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
		
		if (JCAnnotatedTypeReflect.is(in)) {
			JCExpression underlyingType = cloneType0(maker, JCAnnotatedTypeReflect.getUnderlyingType(in));
			List<JCAnnotation> anns = copyAnnotations(JCAnnotatedTypeReflect.getAnnotations(in), maker);
			return JCAnnotatedTypeReflect.create(anns, underlyingType);
		}
		
		// This is somewhat unsafe, but it's better than outright throwing an exception here. Returning null will just cause an exception down the pipeline.
		return (JCExpression) in;
	}
	
	public static enum CopyJavadoc {
		VERBATIM {
			@Override public String apply(final JCCompilationUnit cu, final JavacNode node) {
				return Javac.getDocComment(cu, node.get());
			}
		},
		GETTER {
			@Override public String apply(final JCCompilationUnit cu, final JavacNode node) {
				final JCTree n = node.get();
				String javadoc = Javac.getDocComment(cu, n);
				// step 1: Check if there is a 'GETTER' section. If yes, that becomes the new method's javadoc.
				String out = getJavadocSection(javadoc, "GETTER");
				final boolean sectionBased = out != null;
				if (!sectionBased) {
					out = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.PARAM);
				}
				node.getAst().cleanupTask("javadocfilter-getter", n, new CleanupTask() {
					@Override public void cleanup() {
						String javadoc = Javac.getDocComment(cu, n);
						if (javadoc == null || javadoc.isEmpty()) return;
						javadoc = stripSectionsFromJavadoc(javadoc);
						if (!sectionBased) {
							javadoc = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.RETURN);
						}
						Javac.setDocComment(cu, n, javadoc);
					}
				});
				return out;
			}
		},
		SETTER {
			@Override public String apply(final JCCompilationUnit cu, final JavacNode node) {
				return applySetter(cu, node, "SETTER");
			}
		},
		WITH {
			@Override public String apply(final JCCompilationUnit cu, final JavacNode node) {
				return addReturnsUpdatedSelfIfNeeded(applySetter(cu, node, "WITH|WITHER"));
			}
		},
		WITH_BY {
			@Override public String apply(final JCCompilationUnit cu, final JavacNode node) {
				return applySetter(cu, node, "WITHBY|WITH_BY");
			}
		};
		
		public abstract String apply(final JCCompilationUnit cu, final JavacNode node);
		
		private static String applySetter(final JCCompilationUnit cu, JavacNode node, String sectionName) {
			final JCTree n = node.get();
			String javadoc = Javac.getDocComment(cu, n);
			// step 1: Check if there is a 'SETTER' section. If yes, that becomes the new method's javadoc.
			String out = getJavadocSection(javadoc, sectionName);
			final boolean sectionBased = out != null;
			if (!sectionBased) {
				out = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.RETURN);
			}
			node.getAst().cleanupTask("javadocfilter-setter", n, new CleanupTask() {
				@Override public void cleanup() {
					String javadoc = Javac.getDocComment(cu, n);
					if (javadoc == null || javadoc.isEmpty()) return;
					javadoc = stripSectionsFromJavadoc(javadoc);
					if (!sectionBased) {
						javadoc = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.PARAM);
					}
					Javac.setDocComment(cu, n, javadoc);
				}
			});
			return shouldReturnThis(node, JavacHandlerUtil.getAccessorsForField(node)) ? addReturnsThisIfNeeded(out) : out;
		}
	}
	
	public static void copyJavadoc(JavacNode from, JCTree to, CopyJavadoc copyMode) {
		copyJavadoc(from, to, copyMode, false);
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
	public static void copyJavadoc(JavacNode from, JCTree to, CopyJavadoc copyMode, boolean forceAddReturn) {
		if (copyMode == null) copyMode = CopyJavadoc.VERBATIM;
		try {
			JCCompilationUnit cu = ((JCCompilationUnit) from.top().get());
			String newJavadoc = copyMode.apply(cu, from);
			if (forceAddReturn) {
				newJavadoc = addReturnsThisIfNeeded(newJavadoc);
			}
			Javac.setDocComment(cu, to, newJavadoc);
		} catch (Exception ignore) {}
	}
	
	public static void copyJavadocFromParam(JavacNode from, JCMethodDecl to, String paramName) {
		try {
			JCCompilationUnit cu = ((JCCompilationUnit) from.top().get());
			String methodComment = Javac.getDocComment(cu, from.get());
			String newJavadoc = addReturnsThisIfNeeded(getParamJavadoc(methodComment, paramName));
			Javac.setDocComment(cu, to, newJavadoc);
		} catch (Exception ignore) {}
	}
	
	public static boolean isDirectDescendantOfObject(JavacNode typeNode) {
		if (!(typeNode.get() instanceof JCClassDecl)) throw new IllegalArgumentException("not a type node");
		JCTree extending = Javac.getExtendsClause((JCClassDecl) typeNode.get());
		if (extending == null) return true;
		String p = extending.toString();
		return p.equals("Object") || p.equals("java.lang.Object");
	}
	
	public static void createRelevantNullableAnnotation(JavacNode typeNode, JCMethodDecl mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		applyAnnotationToMethodDecl(typeNode, mth, lib.getNullableAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNonNullAnnotation(JavacNode typeNode, JCMethodDecl mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		applyAnnotationToMethodDecl(typeNode, mth, lib.getNonNullAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNonNullAnnotation(JavacNode typeNode, JCVariableDecl arg) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToVarDecl(typeNode, arg, lib.getNonNullAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNullableAnnotation(JavacNode typeNode, JCVariableDecl arg) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToVarDecl(typeNode, arg, lib.getNullableAnnotation(), lib.isTypeUse());
	}
	
	private static void applyAnnotationToMethodDecl(JavacNode typeNode, JCMethodDecl mth, String annType, boolean typeUse) {
		if (annType == null) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation m = maker.Annotation(genTypeRef(typeNode, annType), List.<JCExpression>nil());
		if (typeUse) {
			JCExpression resType = mth.restype;
			if (resType instanceof JCTypeApply) {
				JCTypeApply ta = (JCTypeApply) resType;
				if (ta.clazz instanceof JCFieldAccess) {
					mth.restype = maker.TypeApply(maker.AnnotatedType(List.of(m), ta.clazz), ta.arguments);
					return;
				}
				resType = ta.clazz;
			}
			
			if (resType instanceof JCFieldAccess || resType instanceof JCArrayTypeTree) {
				mth.restype = maker.AnnotatedType(List.of(m), resType);
				return;
			}
			
			if (JCAnnotatedTypeReflect.is(resType)) {
				List<JCAnnotation> annotations = JCAnnotatedTypeReflect.getAnnotations(resType);
				JCAnnotatedTypeReflect.setAnnotations(resType, annotations.prepend(m));
				return;
			}
			
			if (resType instanceof JCPrimitiveTypeTree || resType instanceof JCIdent) {
				mth.mods.annotations = mth.mods.annotations == null ? List.of(m) : mth.mods.annotations.prepend(m);
			}
		} else {
			mth.mods.annotations = mth.mods.annotations == null ? List.of(m) : mth.mods.annotations.prepend(m);
		}
	}
	
	private static void applyAnnotationToVarDecl(JavacNode typeNode, JCVariableDecl arg, String annType, boolean typeUse) {
		if (annType == null) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation m = maker.Annotation(genTypeRef(typeNode, annType), List.<JCExpression>nil());
		if (typeUse) {
			JCExpression varType = arg.vartype;
			JCTypeApply ta = null;
			if (varType instanceof JCTypeApply) {
				ta = (JCTypeApply) varType;
				varType = ta.clazz;
			}
			
			if (varType instanceof JCFieldAccess || varType instanceof JCArrayTypeTree) {
				varType = maker.AnnotatedType(List.of(m), varType);
				if (ta != null) ta.clazz = varType;
				else arg.vartype = varType;
				return;
			}
			
			if (JCAnnotatedTypeReflect.is(varType)) {
				List<JCAnnotation> annotations = JCAnnotatedTypeReflect.getAnnotations(varType);
				JCAnnotatedTypeReflect.setAnnotations(varType, annotations.prepend(m));
				return;
			}
			
			if (varType instanceof JCPrimitiveTypeTree || varType instanceof JCIdent) {
				arg.mods.annotations = arg.mods.annotations == null ? List.of(m) : arg.mods.annotations.prepend(m);
			}
		} else {
			arg.mods.annotations = arg.mods.annotations == null ? List.of(m) : arg.mods.annotations.prepend(m);
		}
	}
}
