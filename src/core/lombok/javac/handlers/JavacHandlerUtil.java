/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.javac.JavacNode;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Container for static utility methods useful to handlers written for javac.
 */
public class JavacHandlerUtil {
	private JavacHandlerUtil() {
		//Prevent instantiation
	}
	
	/**
	 * Checks if the given expression (that really ought to refer to a type expression) represents a primitive type.
	 */
	public static boolean isPrimitive(JCExpression ref) {
		String typeName = ref.toString();
		return TransformationsUtil.PRIMITIVE_TYPE_NAME_PATTERN.matcher(typeName).matches();
	}
	
	/**
	 * Removes the annotation from javac's AST, then removes it from lombok's AST,
	 * then removes any import statement that imports this exact annotation (not star imports).
	 */
	public static void markAnnotationAsProcessed(JavacNode annotation, Class<? extends Annotation> annotationType) {
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
		if (!node.shouldDeleteLombokAnnotations()) return;
		List<JCTree> newDefs = List.nil();
		
		JCCompilationUnit unit = (JCCompilationUnit) node.top().get();
		
		for (JCTree def : unit.defs) {
			boolean delete = false;
			if (def instanceof JCImport) {
				JCImport imp0rt = (JCImport)def;
				delete = (!imp0rt.staticImport && imp0rt.qualid.toString().equals(name));
			}
			if (!delete) newDefs = newDefs.append(def);
		}
		unit.defs = newDefs;
	}

	private static List<JCAnnotation> filterList(List<JCAnnotation> annotations, JCTree jcTree) {
		List<JCAnnotation> newAnnotations = List.nil();
		for (JCAnnotation ann : annotations) {
			if (jcTree != ann) newAnnotations = newAnnotations.append(ann);
		}
		return newAnnotations;
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllGetterNames(CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllGetterNames(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		boolean isBoolean = field.vartype.toString().equals("boolean");
		
		return TransformationsUtil.toAllGetterNames(fieldName, isBoolean);
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toGetterName(CharSequence, boolean)}.
	 */
	public static String toGetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		boolean isBoolean = field.vartype.toString().equals("boolean");
		
		return TransformationsUtil.toGetterName(fieldName, isBoolean);
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toSetterName(CharSequence)}.
	 */
	public static String toSetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		return TransformationsUtil.toSetterName(fieldName);
	}
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_USER, EXISTS_BY_LOMBOK;
	}
	
	/**
	 * Checks if there is a field with the provided name.
	 * 
	 * @param fieldName the field name to check for.
	 * @param node Any node that represents the Type (JCClassDecl) to look in, or any child node thereof.
	 */
	public static MemberExistsResult fieldExists(String fieldName, JavacNode node) {
		while (node != null && !(node.get() instanceof JCClassDecl)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCVariableDecl) {
					if (((JCVariableDecl)def).name.contentEquals(fieldName)) {
						JavacNode existing = node.getNodeFor(def);
						if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
						return MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	public static MemberExistsResult methodExists(String methodName, JavacNode node) {
		return methodExists(methodName, node, true);
	}
	
	/**
	 * Checks if there is a method with the provided name. In case of multiple methods (overloading), only
	 * the first method decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param methodName the method name to check for.
	 * @param node Any node that represents the Type (JCClassDecl) to look in, or any child node thereof.
	 * @param caseSensitive If the search should be case sensitive.
	 */
	public static MemberExistsResult methodExists(String methodName, JavacNode node, boolean caseSensitive) {
		while (node != null && !(node.get() instanceof JCClassDecl)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					String name = ((JCMethodDecl)def).name.toString();
					boolean matches = caseSensitive ? name.equals(methodName) : name.equalsIgnoreCase(methodName);
					if (matches) {
						JavacNode existing = node.getNodeFor(def);
						if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
						return MemberExistsResult.EXISTS_BY_LOMBOK;
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
		while (node != null && !(node.get() instanceof JCClassDecl)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl)node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					if (((JCMethodDecl)def).name.contentEquals("<init>")) {
						if ((((JCMethodDecl)def).mods.flags & Flags.GENERATEDCONSTR) != 0) continue;
						JavacNode existing = node.getNodeFor(def);
						if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
						return MemberExistsResult.EXISTS_BY_LOMBOK;
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
	
	/**
	 * Adds the given new field declaration to the provided type AST Node.
	 * 
	 * Also takes care of updating the JavacAST.
	 */
	public static void injectField(JavacNode typeNode, JCVariableDecl field) {
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		addSuppressWarningsAll(field.mods, typeNode, field.pos);
		type.defs = type.defs.append(field);
		
		typeNode.add(field, Kind.FIELD).recursiveSetHandled();
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
						break;
					}
				}
				idx++;
			}
		}
		
		addSuppressWarningsAll(method.mods, typeNode, method.pos);
		type.defs = type.defs.append(method);
		
		typeNode.add(method, Kind.METHOD).recursiveSetHandled();
	}
	
	private static void addSuppressWarningsAll(JCModifiers mods, JavacNode node, int pos) {
		TreeMaker maker = node.getTreeMaker();
		JCExpression suppressWarningsType = chainDots(maker, node, "java", "lang", "SuppressWarnings");
		JCLiteral allLiteral = maker.Literal("all");
		suppressWarningsType.pos = pos;
		allLiteral.pos = pos;
		JCAnnotation annotation = maker.Annotation(suppressWarningsType, List.<JCExpression>of(allLiteral));
		annotation.pos = pos;
		mods.annotations = mods.annotations.append(annotation);
	}
	
	private static List<JCTree> addAllButOne(List<JCTree> defs, int idx) {
		List<JCTree> out = List.nil();
		int i = 0;
		for (JCTree def : defs) {
			if (i++ != idx) out = out.append(def);
		}
		return out;
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
	public static JCExpression chainDots(TreeMaker maker, JavacNode node, String... elems) {
		assert elems != null;
		assert elems.length > 0;
		
		JCExpression e = maker.Ident(node.toName(elems[0]));
		for (int i = 1 ; i < elems.length ; i++) {
			e = maker.Select(e, node.toName(elems[i]));
		}
		
		return e;
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that matches the provided regular expression pattern.
	 * 
	 * Only the simple name is checked - the package and any containing class are ignored.
	 */
	public static List<JCAnnotation> findAnnotations(JavacNode fieldNode, Pattern namePattern) {
		List<JCAnnotation> result = List.nil();
		for (JavacNode child : fieldNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				String name = annotation.annotationType.toString();
				int idx = name.lastIndexOf(".");
				String suspect = idx == -1 ? name : name.substring(idx + 1);
				if (namePattern.matcher(suspect).matches()) {
					result = result.append(annotation);
				}
			}
		}	
		return result;
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a {@code NullPointerException} with the
	 * variable name as message.
	 */
	public static JCStatement generateNullCheck(TreeMaker treeMaker, JavacNode variable) {
		JCVariableDecl varDecl = (JCVariableDecl) variable.get();
		if (isPrimitive(varDecl.vartype)) return null;
		Name fieldName = varDecl.name;
		JCExpression npe = chainDots(treeMaker, variable, "java", "lang", "NullPointerException");
		JCTree exception = treeMaker.NewClass(null, List.<JCExpression>nil(), npe, List.<JCExpression>of(treeMaker.Literal(fieldName.toString())), null);
		JCStatement throwStatement = treeMaker.Throw(exception);
		return treeMaker.If(treeMaker.Binary(JCTree.EQ, treeMaker.Ident(fieldName), treeMaker.Literal(TypeTags.BOT, null)), throwStatement, null);
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
		
		List<Integer> problematic = List.nil();
		for (int i = 0 ; i < list.size() ; i++) {
			if (!matched[i]) problematic = problematic.append(i);
		}
		
		return problematic;
	}
}
