/*
 * Copyright © 2009-2011 Reinier Zwitserloot and Roel Spilker.
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
package lombok.javac;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.Lombok;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * Container for static utility methods relevant to lombok's operation on javac.
 */
public class Javac {
	private Javac() {
		//prevent instantiation
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
		
		TypeLibrary library = new TypeLibrary();
		library.addType(type.getName());
		TypeResolver resolver = new TypeResolver(library, node.getPackageDeclaration(), node.getImportStatements());
		Collection<String> typeMatches = resolver.findTypeMatches(node, typeName);
		
		for (String match : typeMatches) {
			if (match.equals(type.getName())) return true;
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
			List<String> raws = new ArrayList<String>();
			List<Object> guesses = new ArrayList<Object>();
			List<Object> expressions = new ArrayList<Object>();
			final List<DiagnosticPosition> positions = new ArrayList<DiagnosticPosition>();
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
	 * Turns an expression into a guessed intended literal. Only works for literals, as you can imagine.
	 * 
	 * Will for example turn a TrueLiteral into 'Boolean.valueOf(true)'.
	 */
	private static Object calculateGuess(JCExpression expr) {
		if (expr instanceof JCLiteral) {
			JCLiteral lit = (JCLiteral)expr;
			if (lit.getKind() == com.sun.source.tree.Tree.Kind.BOOLEAN_LITERAL) {
				return ((Number)lit.value).intValue() == 0 ? false : true;
			}
			return lit.value;
		} else if (expr instanceof JCIdent || expr instanceof JCFieldAccess) {
			String x = expr.toString();
			if (x.endsWith(".class")) x = x.substring(0, x.length() - 6);
			else {
				int idx = x.lastIndexOf('.');
				if (idx > -1) x = x.substring(idx + 1);
			}
			return x;
		} else return null;
	}
	
	/**
	 * Retrieves a compile time constant of type int from the specified class location.
	 * 
	 * Solves the problem of compile time constant inlining, resulting in lombok having the wrong value 
	 * (javac compiler changes private api constants from time to time)
	 * 
	 * @param ctcLocation location of the compile time constant
	 * @param identifier the name of the field of the compile time constant.
	 */
	public static int getCtcInt(Class<?> ctcLocation, String identifier) {
		try {
			return (Integer)ctcLocation.getField(identifier).get(null);
		} catch (NoSuchFieldException e) {
			throw Lombok.sneakyThrow(e);
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		}
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
	
	private static Map<JCTree, JCTree> generatedNodes = new WeakHashMap<JCTree, JCTree>();
	
	public static JCTree getGeneratedBy(JCTree node) {
		synchronized (generatedNodes) {
			return generatedNodes.get(node);
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
			else generatedNodes.put(node, source);
		}
		return node;
	}
}
