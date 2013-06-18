/*
 * Copyright (C) 2009-2013 The Project Lombok Authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Container for static utility methods relevant to lombok's operation on javac.
 */
public class Javac {
	private Javac() {
		//prevent instantiation
	}
	
	/** Matches any of the 8 primitive names, such as {@code boolean}. */
	private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile(
			"^(boolean|byte|short|int|long|float|double|char)$");
	
	private static final Pattern VERSION_PARSER = Pattern.compile("^(\\d{1,6})\\.(\\d{1,6}).*$");
	
	/**
	 * Returns the version of this java compiler, i.e. the JDK that it shipped in. For example, for javac v1.7, this returns {@code 7}.
	 */
	public static int getJavaCompilerVersion() {
		Matcher m = VERSION_PARSER.matcher(JavaCompiler.version());
		if (m.matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			if (major == 1) return minor;
		}
		
		return 6;
	}
	
	/**
	 * Checks if the given expression (that really ought to refer to a type expression) represents a primitive type.
	 */
	public static boolean isPrimitive(JCExpression ref) {
		String typeName = ref.toString();
		return PRIMITIVE_TYPE_NAME_PATTERN.matcher(typeName).matches();
	}
	
	/**
	 * Turns an expression into a guessed intended literal. Only works for literals, as you can imagine.
	 * 
	 * Will for example turn a TrueLiteral into 'Boolean.valueOf(true)'.
	 */
	public static Object calculateGuess(JCExpression expr) {
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
	
	public static final int CTC_BOOLEAN = getCtcInt(TypeTags.class, "BOOLEAN");
	public static final int CTC_INT = getCtcInt(TypeTags.class, "INT");
	public static final int CTC_DOUBLE = getCtcInt(TypeTags.class, "DOUBLE");
	public static final int CTC_FLOAT = getCtcInt(TypeTags.class, "FLOAT");
	public static final int CTC_SHORT = getCtcInt(TypeTags.class, "SHORT");
	public static final int CTC_BYTE = getCtcInt(TypeTags.class, "BYTE");
	public static final int CTC_LONG = getCtcInt(TypeTags.class, "LONG");
	public static final int CTC_CHAR = getCtcInt(TypeTags.class, "CHAR");
	public static final int CTC_VOID = getCtcInt(TypeTags.class, "VOID");
	public static final int CTC_NONE = getCtcInt(TypeTags.class, "NONE");
	
	public static final int CTC_NOT_EQUAL = getCtcInt(JCTree.class, "NE");
	public static final int CTC_NOT = getCtcInt(JCTree.class, "NOT");
	public static final int CTC_BITXOR = getCtcInt(JCTree.class, "BITXOR");
	public static final int CTC_UNSIGNED_SHIFT_RIGHT = getCtcInt(JCTree.class, "USR");
	public static final int CTC_MUL = getCtcInt(JCTree.class, "MUL");
	public static final int CTC_PLUS = getCtcInt(JCTree.class, "PLUS");
	public static final int CTC_BOT = getCtcInt(TypeTags.class, "BOT");
	public static final int CTC_EQUAL = getCtcInt(JCTree.class, "EQ");
	
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
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final Field JCTREE_TAG;
	private static final Method JCTREE_GETTAG;
	static {
		Field f = null;
		try {
			f = JCTree.class.getDeclaredField("tag");
		} catch (NoSuchFieldException e) {}
		JCTREE_TAG = f;
		
		Method m = null;
		try {
			m = JCTree.class.getDeclaredMethod("getTag");
		} catch (NoSuchMethodException e) {}
		JCTREE_GETTAG = m;
	}
	
	public static int getTag(JCTree node) {
		if (JCTREE_GETTAG != null) {
			try {
				return (Integer) JCTREE_GETTAG.invoke(node);
			} catch (Exception e) {}
		}
		try {
			return (Integer) JCTREE_TAG.get(node);
		} catch (Exception e) {
			throw new IllegalStateException("Can't get node tag");
		}
	}
	
	private static Method method;
	
	public static JCClassDecl ClassDef(TreeMaker maker, JCModifiers mods, Name name, List<JCTypeParameter> typarams, JCExpression extending, List<JCExpression> implementing, List<JCTree> defs) {
		if (method == null) try {
			method = TreeMaker.class.getDeclaredMethod("ClassDef", JCModifiers.class, Name.class, List.class, JCExpression.class, List.class, List.class);
		} catch (NoSuchMethodException ignore) {}
		if (method == null) try {
			method = TreeMaker.class.getDeclaredMethod("ClassDef", JCModifiers.class, Name.class, List.class, JCTree.class, List.class, List.class);
		} catch (NoSuchMethodException ignore) {}
		
		if (method == null) throw new IllegalStateException("Lombok bug #20130617-1310: ClassDef doesn't look like anything we thought it would look like.");
		if (!Modifier.isPublic(method.getModifiers()) && !method.isAccessible()) {
			method.setAccessible(true);
		}
		
		try {
			return (JCClassDecl) method.invoke(maker, mods, name, typarams, extending, implementing, defs);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	private static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) throw new NullPointerException("t");
		Javac.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
}
