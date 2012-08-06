/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.TransformationsUtil;
import lombok.core.TypeResolver;
import lombok.experimental.Accessors;
import lombok.javac.Javac;
import lombok.javac.JavacNode;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
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
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
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
		
		MarkingScanner(JCTree source) {
			this.source = source;
		}
		
		@Override public void scan(JCTree tree) {
			setGeneratedBy(tree, source);
			super.scan(tree);
		}
	}
	
	private static Map<JCTree, WeakReference<JCTree>> generatedNodes = new WeakHashMap<JCTree, WeakReference<JCTree>>();
	
	/**
	 * Contributed by Jan Lahoda; many lombok transformations should not be run (or a lite version should be run) when the netbeans editor
	 * is running javac on the open source file to find inline errors and such. As class files are compiled separately this does not affect
	 * actual runtime behaviour or file output of the netbeans IDE.
	 */
	public static boolean inNetbeansEditor(JavacNode node) {
		Options options = Options.instance(node.getContext());
		return (options.keySet().contains("ide") && !options.keySet().contains("backgroundCompilation"));
	}
	
	public static JCTree getGeneratedBy(JCTree node) {
		synchronized (generatedNodes) {
			WeakReference<JCTree> ref = generatedNodes.get(node);
			return ref == null ? null : ref.get();
		}
	}
	
	public static boolean isGenerated(JCTree node) {
		return getGeneratedBy(node) != null;
	}
	
	public static <T extends JCTree> T recursiveSetGeneratedBy(T node, JCTree source) {
		setGeneratedBy(node, source);
		node.accept(new MarkingScanner(source));
		
		return node;
	}
	
	public static <T extends JCTree> T setGeneratedBy(T node, JCTree source) {
		synchronized (generatedNodes) {
			if (source == null) generatedNodes.remove(node);
			else generatedNodes.put(node, new WeakReference<JCTree>(source));
		}
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
		
		TypeResolver resolver = new TypeResolver(node.getPackageDeclaration(), node.getImportStatements());
		return resolver.typeMatches(node, type.getName(), typeName);
	}
	
	/**
	 * Returns if a field is marked deprecated, either by {@code @Deprecated} or in javadoc
	 * @param field the field to check
	 * @return {@code true} if a field is marked deprecated, either by {@code @Deprecated} or in javadoc, otherwise {@code false}
	 */
	public static boolean isFieldDeprecated(JavacNode field) {
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
	 * Creates an instance of {@code AnnotationValues} for the provided AST Node.
	 * 
	 * @param type An annotation class type, such as {@code lombok.Getter.class}.
	 * @param node A Lombok AST node representing an annotation in source code.
	 */
	public static <A extends Annotation> AnnotationValues<A> createAnnotation(Class<A> type, final JavacNode node) {
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
		JCAnnotation anno = (JCAnnotation) node.get();
		List<JCExpression> arguments = anno.getArguments();
		for (Method m : type.getDeclaredMethods()) {
			if (!Modifier.isPublic(m.getModifiers())) continue;
			String name = m.getName();
			java.util.List<String> raws = new ArrayList<String>();
			java.util.List<Object> guesses = new ArrayList<Object>();
			java.util.List<Object> expressions = new ArrayList<Object>();
			final java.util.List<DiagnosticPosition> positions = new ArrayList<DiagnosticPosition>();
			boolean isExplicit = false;
			
			for (JCExpression arg : arguments) {
				String mName;
				JCExpression rhs;
				
				if (arg instanceof JCAssign) {
					JCAssign assign = (JCAssign) arg;
					mName = assign.lhs.toString();
					rhs = assign.rhs;
				} else {
					rhs = arg;
					mName = "value";
				}
				
				if (!mName.equals(name)) continue;
				isExplicit = true;
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
			}
			
			values.put(name, new AnnotationValue(node, raws, expressions, guesses, isExplicit) {
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
		
		return new AnnotationValues<A>(type, values, node);
	}
	
	/**
	 * Removes the annotation from javac's AST (it remains in lombok's AST),
	 * then removes any import statement that imports this exact annotation (not star imports).
	 * Only does this if the DeleteLombokAnnotations class is in the context.
	 */
	public static void deleteAnnotationIfNeccessary(JavacNode annotation, Class<? extends Annotation> annotationType) {
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
		
		deleteImportFromCompilationUnit(annotation, annotationType.getName());
	}
	
	public static void deleteImportFromCompilationUnit(JavacNode node, String name) {
		if (inNetbeansEditor(node)) return;
		if (!node.shouldDeleteLombokAnnotations()) return;
		ListBuffer<JCTree> newDefs = ListBuffer.lb();
		
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
		ListBuffer<JCAnnotation> newAnnotations = ListBuffer.lb();
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
		return TransformationsUtil.toAllGetterNames(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(JavacNode field) {
		return TransformationsUtil.toGetterName(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(JavacNode field) {
		return TransformationsUtil.toAllSetterNames(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(JavacNode field) {
		return TransformationsUtil.toSetterName(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * Translates the given field into all possible wither names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllWitherNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWitherNames(JavacNode field) {
		return TransformationsUtil.toAllWitherNames(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * @return the likely wither name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toWitherName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWitherName(JavacNode field) {
		return TransformationsUtil.toWitherName(getAccessorsForField(field), field.getName(), isBoolean(field));
	}
	
	/**
	 * When generating a setter, the setter either returns void (beanspec) or Self (fluent).
	 * This method scans for the {@code Accessors} annotation to figure that out.
	 */
	public static boolean shouldReturnThis(JavacNode field) {
		if ((((JCVariableDecl) field.get()).mods.flags & Flags.STATIC) != 0) return false;
		
		AnnotationValues<Accessors> accessors = JavacHandlerUtil.getAccessorsForField(field);
		
		boolean forced = (accessors.getActualExpression("chain") != null);
		Accessors instance = accessors.getInstance();
		return instance.chain() || (instance.fluent() && !forced);
	}
	
	public static JCExpression cloneSelfType(JavacNode field) {
		JavacNode typeNode = field;
		TreeMaker maker = field.getTreeMaker();
		while (typeNode != null && typeNode.getKind() != Kind.TYPE) typeNode = typeNode.up();
		if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
			JCClassDecl type = (JCClassDecl) typeNode.get();
			ListBuffer<JCExpression> typeArgs = ListBuffer.lb();
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
	
	private static boolean isBoolean(JavacNode field) {
		JCExpression varType = ((JCVariableDecl) field.get()).vartype;
		return varType != null && varType.toString().equals("boolean");
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
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
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
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
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
					if (((JCMethodDecl)def).name.contentEquals("<init>")) {
						if ((((JCMethodDecl)def).mods.flags & Flags.GENERATEDCONSTR) != 0) continue;
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
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
	 * @see #createFieldAccessor(TreeMaker, JavacNode)
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
	static JCExpression createFieldAccessor(TreeMaker maker, JavacNode field, FieldAccess fieldAccess) {
		return createFieldAccessor(maker, field, fieldAccess, null);
	}
	
	static JCExpression createFieldAccessor(TreeMaker maker, JavacNode field, FieldAccess fieldAccess, JCExpression receiver) {
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
	
	/**
	 * Adds the given new field declaration to the provided type AST Node.
	 * The field carries the &#64;{@link SuppressWarnings}("all") annotation.
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectFieldSuppressWarnings(JavacNode typeNode, JCVariableDecl field) {
		injectField(typeNode, field, true);
	}
	
	/**
	 * Adds the given new field declaration to the provided type AST Node.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectField(JavacNode typeNode, JCVariableDecl field) {
		injectField(typeNode, field, false);
	}

	private static void injectField(JavacNode typeNode, JCVariableDecl field, boolean addSuppressWarnings) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		if (addSuppressWarnings) addSuppressWarningsAll(field.mods, typeNode, field.pos, getGeneratedBy(field));
		
		List<JCTree> insertAfter = null;
		List<JCTree> insertBefore = type.defs;
		while (insertBefore.tail != null) {
			if (insertBefore.head instanceof JCVariableDecl) {
				JCVariableDecl f = (JCVariableDecl) insertBefore.head;
				if (isEnumConstant(f) || isGenerated(f)) {
					insertAfter = insertBefore;
					insertBefore = insertBefore.tail;
					continue;
				}
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
		
		typeNode.add(field, Kind.FIELD);
	}
	
	private static boolean isEnumConstant(final JCVariableDecl field) {
		return (field.mods.flags & Flags.ENUM) != 0;
	}
	
	/**
	 * Adds the given new method declaration to the provided type AST Node.
	 * Can also inject constructors.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectMethod(JavacNode typeNode, JCMethodDecl method) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		if (method.getName().contentEquals("<init>")) {
			//Scan for default constructor, and remove it.
			int idx = 0;
			for (JCTree def : type.defs) {
				if (def instanceof JCMethodDecl) {
					if ((((JCMethodDecl)def).mods.flags & Flags.GENERATEDCONSTR) != 0) {
						JavacNode tossMe = typeNode.getNodeFor(def);
						if (tossMe != null) tossMe.up().removeChild(tossMe);
						type.defs = addAllButOne(type.defs, idx);
						if (type.sym != null && type.sym.members_field != null) {
							 type.sym.members_field.remove(((JCMethodDecl)def).sym);
						}
						break;
					}
				}
				idx++;
			}
		}
		
		addSuppressWarningsAll(method.mods, typeNode, method.pos, getGeneratedBy(method));
		type.defs = type.defs.append(method);
		
		typeNode.add(method, Kind.METHOD);
	}
	
	private static void addSuppressWarningsAll(JCModifiers mods, JavacNode node, int pos, JCTree source) {
		TreeMaker maker = node.getTreeMaker();
		JCExpression suppressWarningsType = chainDots(node, "java", "lang", "SuppressWarnings");
		JCLiteral allLiteral = maker.Literal("all");
		suppressWarningsType.pos = pos;
		allLiteral.pos = pos;
		JCAnnotation annotation = recursiveSetGeneratedBy(maker.Annotation(suppressWarningsType, List.<JCExpression>of(allLiteral)), source);
		annotation.pos = pos;
		mods.annotations = mods.annotations.append(annotation);
	}
	
	private static List<JCTree> addAllButOne(List<JCTree> defs, int idx) {
		ListBuffer<JCTree> out = ListBuffer.lb();
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
	public static JCExpression chainDots(JavacNode node, String... elems) {
		return chainDots(node, -1, elems);
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
	public static JCExpression chainDots(JavacNode node, int pos, String... elems) {
		assert elems != null;
		assert elems.length > 0;
		
		TreeMaker maker = node.getTreeMaker();
		if (pos != -1) maker = maker.at(pos);
		JCExpression e = maker.Ident(node.toName(elems[0]));
		for (int i = 1 ; i < elems.length ; i++) {
			e = maker.Select(e, node.toName(elems[i]));
		}
		
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
		return chainDots(node, elems.split("\\."));
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that matches the provided regular expression pattern.
	 * 
	 * Only the simple name is checked - the package and any containing class are ignored.
	 */
	public static List<JCAnnotation> findAnnotations(JavacNode fieldNode, Pattern namePattern) {
		ListBuffer<JCAnnotation> result = ListBuffer.lb();
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
	 * Generates a new statement that checks if the given variable is null, and if so, throws a {@code NullPointerException} with the
	 * variable name as message.
	 */
	public static JCStatement generateNullCheck(TreeMaker treeMaker, JavacNode variable) {
		JCVariableDecl varDecl = (JCVariableDecl) variable.get();
		if (isPrimitive(varDecl.vartype)) return null;
		Name fieldName = varDecl.name;
		JCExpression npe = chainDots(variable, "java", "lang", "NullPointerException");
		JCTree exception = treeMaker.NewClass(null, List.<JCExpression>nil(), npe, List.<JCExpression>of(treeMaker.Literal(fieldName.toString())), null);
		JCStatement throwStatement = treeMaker.Throw(exception);
		return treeMaker.If(treeMaker.Binary(Javac.getCtcInt(JCTree.class, "EQ"), treeMaker.Ident(fieldName), treeMaker.Literal(Javac.getCtcInt(TypeTags.class, "BOT"), null)), throwStatement, null);
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
		
		ListBuffer<Integer> problematic = ListBuffer.lb();
		for (int i = 0 ; i < list.size() ; i++) {
			if (!matched[i]) problematic.append(i);
		}
		
		return problematic.toList();
	}
	
	static List<JCExpression> getAndRemoveAnnotationParameter(JCAnnotation ast, String parameterName) {
		ListBuffer<JCExpression> params = ListBuffer.lb();
		List<JCExpression> result = List.nil();
		
		for (JCExpression param : ast.args) {
			if (param instanceof JCAssign) {
				JCAssign assign = (JCAssign) param;
				if (assign.lhs instanceof JCIdent) {
					JCIdent ident = (JCIdent) assign.lhs;
					if (parameterName.equals(ident.name.toString())) {
						if (assign.rhs instanceof JCNewArray) {
							result = ((JCNewArray) assign.rhs).elems;
						} else {
							result = result.append(assign.rhs);
						}
						continue;
					}
				}
			}
			
			params.append(param);
		}
		ast.args = params.toList();
		return result;
	}
	
	static List<JCAnnotation> copyAnnotations(List<JCExpression> in) {
		ListBuffer<JCAnnotation> out = ListBuffer.lb();
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
	
	private static boolean isClassAndDoesNotHaveFlags(JavacNode typeNode, int flags) {
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
	public static JCExpression cloneType(TreeMaker maker, JCExpression in, JCTree source) {
		JCExpression out = cloneType0(maker, in);
		if (out != null) recursiveSetGeneratedBy(out, source);
		return out;
	}
	
	private static JCExpression cloneType0(TreeMaker maker, JCTree in) {
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
			ListBuffer<JCExpression> lb = ListBuffer.lb();
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
}
