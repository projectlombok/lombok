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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Container for static utility methods relevant to lombok's operation on javac.
 */
public class Javac {
	private Javac() {
		// prevent instantiation
	}
	
	private static final ConcurrentMap<String, Object> TYPE_TAG_CACHE = new ConcurrentHashMap<String, Object>();
	private static final ConcurrentMap<String, Object> TREE_TAG_CACHE = new ConcurrentHashMap<String, Object>();
	
	/** Matches any of the 8 primitive names, such as {@code boolean}. */
	private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile("^(boolean|byte|short|int|long|float|double|char)$");
	
	private static final Pattern VERSION_PARSER = Pattern.compile("^(\\d{1,6})\\.(\\d{1,6}).*$");
	
	private static final AtomicInteger compilerVersion = new AtomicInteger(-1);
	
	/**
	 * Returns the version of this java compiler, i.e. the JDK that it shipped in. For example, for javac v1.7, this returns {@code 7}.
	 */
	public static int getJavaCompilerVersion() {
		int cv = compilerVersion.get();
		if (cv != -1) return cv;
		Matcher m = VERSION_PARSER.matcher(JavaCompiler.version());
		if (m.matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			if (major == 1) {
				compilerVersion.set(minor);
				return minor;
			}
		}
		
		compilerVersion.set(6);
		return 6;
	}
	
	/**
	 * Checks if the given expression (that really ought to refer to a type
	 * expression) represents a primitive type.
	 */
	public static boolean isPrimitive(JCExpression ref) {
		String typeName = ref.toString();
		return PRIMITIVE_TYPE_NAME_PATTERN.matcher(typeName).matches();
	}
	
	/**
	 * Turns an expression into a guessed intended literal. Only works for
	 * literals, as you can imagine.
	 * 
	 * Will for example turn a TrueLiteral into 'Boolean.valueOf(true)'.
	 */
	public static Object calculateGuess(JCExpression expr) {
		if (expr instanceof JCLiteral) {
			JCLiteral lit = (JCLiteral) expr;
			if (lit.getKind() == com.sun.source.tree.Tree.Kind.BOOLEAN_LITERAL) {
				return ((Number) lit.value).intValue() == 0 ? false : true;
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
		} else
			return null;
	}
	
	public static final Object CTC_BOOLEAN = getTypeTag("BOOLEAN");
	public static final Object CTC_INT = getTypeTag("INT");
	public static final Object CTC_DOUBLE = getTypeTag("DOUBLE");
	public static final Object CTC_FLOAT = getTypeTag("FLOAT");
	public static final Object CTC_SHORT = getTypeTag("SHORT");
	public static final Object CTC_BYTE = getTypeTag("BYTE");
	public static final Object CTC_LONG = getTypeTag("LONG");
	public static final Object CTC_CHAR = getTypeTag("CHAR");
	public static final Object CTC_VOID = getTypeTag("VOID");
	public static final Object CTC_NONE = getTypeTag("NONE");
	public static final Object CTC_BOT = getTypeTag("BOT");
	public static final Object CTC_CLASS = getTypeTag("CLASS");
	
	public static final Object CTC_NOT_EQUAL = getTreeTag("NE");
	public static final Object CTC_NOT = getTreeTag("NOT");
	public static final Object CTC_BITXOR = getTreeTag("BITXOR");
	public static final Object CTC_UNSIGNED_SHIFT_RIGHT = getTreeTag("USR");
	public static final Object CTC_MUL = getTreeTag("MUL");
	public static final Object CTC_PLUS = getTreeTag("PLUS");
	public static final Object CTC_EQUAL = getTreeTag("EQ");
	
	public static boolean compareCTC(Object ctc1, Object ctc2) {
		return ctc1 == null ? ctc2 == null : ctc1.equals(ctc2);
	}
	
	/**
	 * Retrieves the provided TypeTag value, in a compiler version independent manner.
	 * 
	 * The actual type object differs depending on the Compiler version:
	 * <ul>
	 * <li>For JDK 8 this is an enum value of type <code>com.sun.tools.javac.code.TypeTag</code> 
	 * <li>for JDK 7 and lower, this is the value of the constant within <code>com.sun.tools.javac.code.TypeTags</code>
	 * </ul>
	 * Solves the problem of compile time constant inlining, resulting in lombok
	 * having the wrong value (javac compiler changes private api constants from
	 * time to time).
	 * 
	 * @param identifier Identifier to turn into a TypeTag.
	 * @return the value of the typetag constant (either enum instance or an Integer object).
	 */
	public static Object getTypeTag(String identifier) {
		return getFieldCached(TYPE_TAG_CACHE, getJavaCompilerVersion() < 8 ? "com.sun.tools.javac.code.TypeTags" : "com.sun.tools.javac.code.TypeTag", identifier);
	}
	
	public static Object getTreeTag(String identifier) {
		return getFieldCached(TREE_TAG_CACHE, getJavaCompilerVersion() < 8 ? "com.sun.tools.javac.tree.JCTree" : "com.sun.tools.javac.tree.JCTree$Tag", identifier);
	}
	
	private static Object getFieldCached(ConcurrentMap<String, Object> cache, String className, String fieldName) {
		Object value = cache.get(fieldName);
		if (value != null) return value;
		try {
			value = Class.forName(className).getField(fieldName).get(null);
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (ClassNotFoundException e) {
			throw sneakyThrow(e);
		}
		
		cache.putIfAbsent(fieldName, value);
		return value;
	}
	
	public static Object getTreeTypeTag(JCPrimitiveTypeTree tree) {
		return tree.typetag;
	}
	
	public static Object getTreeTypeTag(JCLiteral tree) {
		return tree.typetag;
	}
	
	private static final Method createIdent, createLiteral, createUnary, createBinary, createThrow, getExtendsClause, getEndPosition;
	
	static {
		if (getJavaCompilerVersion() < 8) {
			createIdent = getMethod(TreeMaker.class, "TypeIdent", int.class);
		} else {
			createIdent = getMethod(TreeMaker.class, "TypeIdent", "com.sun.tools.javac.code.TypeTag");
		}
		createIdent.setAccessible(true);
		
		if (getJavaCompilerVersion() < 8) {
			createLiteral = getMethod(TreeMaker.class, "Literal", int.class, Object.class);
		} else {
			createLiteral = getMethod(TreeMaker.class, "Literal", "com.sun.tools.javac.code.TypeTag", "java.lang.Object");
		}
		createLiteral.setAccessible(true);
		
		if (getJavaCompilerVersion() < 8) {
			createUnary = getMethod(TreeMaker.class, "Unary", int.class, JCExpression.class);
		} else {
			createUnary = getMethod(TreeMaker.class, "Unary", "com.sun.tools.javac.tree.JCTree$Tag", JCExpression.class.getName());
		}
		createUnary.setAccessible(true);
		
		if (getJavaCompilerVersion() < 8) {
			createBinary = getMethod(TreeMaker.class, "Binary", Integer.TYPE, JCExpression.class, JCExpression.class);
		} else {
			createBinary = getMethod(TreeMaker.class, "Binary", "com.sun.tools.javac.tree.JCTree$Tag", JCExpression.class.getName(), JCExpression.class.getName());
		}
		createBinary.setAccessible(true);
		
		if (getJavaCompilerVersion() < 8) {
			createThrow = getMethod(TreeMaker.class, "Throw", JCTree.class);
		} else {
			createThrow = getMethod(TreeMaker.class, "Throw", JCExpression.class);
		}
		createBinary.setAccessible(true);
		
		getExtendsClause = getMethod(JCClassDecl.class, "getExtendsClause", new Class<?>[0]);
		getExtendsClause.setAccessible(true);
		
		if (getJavaCompilerVersion() < 8) {
			getEndPosition = getMethod(DiagnosticPosition.class, "getEndPosition", java.util.Map.class);
		} else {
			getEndPosition = getMethod(DiagnosticPosition.class, "getEndPosition", "com.sun.tools.javac.tree.EndPosTable");
		}
		getEndPosition.setAccessible(true);
	}
	
	private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException e) {
			throw sneakyThrow(e);
		}
	}
	
	private static Method getMethod(Class<?> clazz, String name, String... paramTypes) {
		try {
			Class<?>[] c = new Class[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) c[i] = Class.forName(paramTypes[i]);
			return clazz.getMethod(name, c);
		} catch (NoSuchMethodException e) {
			throw sneakyThrow(e);
		} catch (ClassNotFoundException e) {
			throw sneakyThrow(e);
		}
	}
	
	public static JCExpression makeTypeIdent(TreeMaker maker, Object ctc) {
		try {
			return (JCExpression) createIdent.invoke(maker, ctc);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static JCLiteral makeLiteral(TreeMaker maker, Object ctc, Object argument) {
		try {
			return (JCLiteral) createLiteral.invoke(maker, ctc, argument);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static JCUnary makeUnary(TreeMaker maker, Object ctc, JCExpression argument) {
		try {
			return (JCUnary) createUnary.invoke(maker, ctc, argument);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static JCBinary makeBinary(TreeMaker maker, Object ctc, JCExpression lhsArgument, JCExpression rhsArgument) {
		try {
			return (JCBinary) createBinary.invoke(maker, ctc, lhsArgument, rhsArgument);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static JCStatement makeThrow(TreeMaker maker, JCExpression expression) {
		try {
			return (JCStatement) createThrow.invoke(maker, expression);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static JCTree getExtendsClause(JCClassDecl decl) {
		try {
			return (JCTree) getExtendsClause.invoke(decl);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	public static Object getDocComments(JCCompilationUnit cu) {
		try {
			return JCCOMPILATIONUNIT_DOCCOMMENTS.get(cu);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}
	
	public static int getEndPosition(DiagnosticPosition pos, JCCompilationUnit top) {
		try {
			Object endPositions = JCCOMPILATIONUNIT_ENDPOSITIONS.get(top);
			return (Integer) getEndPosition.invoke(pos, endPositions);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	private static final Class<?> JC_VOID_TYPE, JC_NO_TYPE;
	
	static {
		Class<?> c = null;
		try {
			c = Class.forName("com.sun.tools.javac.code.Type$JCVoidType");
		} catch (Exception ignore) {}
		JC_VOID_TYPE = c;
		c = null;
		try {
			c = Class.forName("com.sun.tools.javac.code.Type$JCNoType");
		} catch (Exception ignore) {}
		JC_NO_TYPE = c;
	}
	
	public static Type createVoidType(TreeMaker maker, Object tag) {
		if (Javac.getJavaCompilerVersion() < 8) {
			return new JCNoType(((Integer) tag).intValue());
		} else {
			try {
				if (compareCTC(tag, CTC_VOID)) {
					return (Type) JC_VOID_TYPE.newInstance();
				} else {
					return (Type) JC_NO_TYPE.newInstance();
				}
			} catch (IllegalAccessException e) {
				throw sneakyThrow(e);
			} catch (InstantiationException e) {
				throw sneakyThrow(e);
			}
		}
	}
	
	private static class JCNoType extends Type implements NoType {
		public JCNoType(int tag) {
			super(tag, null);
		}
		
		@Override
		public TypeKind getKind() {
			if (Javac.compareCTC(tag, CTC_VOID)) return TypeKind.VOID;
			if (Javac.compareCTC(tag, CTC_NONE)) return TypeKind.NONE;
			throw new AssertionError("Unexpected tag: " + tag);
		}
		
		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
	
	private static final Field JCTREE_TAG, JCLITERAL_TYPETAG, JCPRIMITIVETYPETREE_TYPETAG, JCCOMPILATIONUNIT_ENDPOSITIONS, JCCOMPILATIONUNIT_DOCCOMMENTS;
	private static final Method JCTREE_GETTAG;
	static {
		Field f = null;
		try {
			f = JCTree.class.getDeclaredField("tag");
		} catch (NoSuchFieldException e) {}
		JCTREE_TAG = f;
		
		f = null;
		try {
			f = JCLiteral.class.getDeclaredField("typetag");
		} catch (NoSuchFieldException e) {}
		JCLITERAL_TYPETAG = f;
		
		f = null;
		try {
			f = JCPrimitiveTypeTree.class.getDeclaredField("typetag");
		} catch (NoSuchFieldException e) {}
		JCPRIMITIVETYPETREE_TYPETAG = f;
		
		f = null;
		try {
			f = JCCompilationUnit.class.getDeclaredField("endPositions");
		} catch (NoSuchFieldException e) {}
		JCCOMPILATIONUNIT_ENDPOSITIONS = f;
		
		f = null;
		try {
			f = JCCompilationUnit.class.getDeclaredField("docComments");
		} catch (NoSuchFieldException e) {}
		JCCOMPILATIONUNIT_DOCCOMMENTS = f;
		
		Method m = null;
		try {
			m = JCTree.class.getDeclaredMethod("getTag");
		} catch (NoSuchMethodException e) {}
		JCTREE_GETTAG = m;
	}
	
	public static Object getTag(JCTree node) {
		if (JCTREE_GETTAG != null) {
			try {
				return JCTREE_GETTAG.invoke(node);
			} catch (Exception e) {}
		}
		try {
			return JCTREE_TAG.get(node);
		} catch (Exception e) {
			throw new IllegalStateException("Can't get node tag");
		}
	}
	
	public static Object getTypeTag(JCLiteral node) {
		try {
			return JCLITERAL_TYPETAG.get(node);
		} catch (Exception e) {
			throw new IllegalStateException("Can't get JCLiteral typetag");
		}
	}
	
	public static Object getTypeTag(JCPrimitiveTypeTree node) {
		try {
			return JCPRIMITIVETYPETREE_TYPETAG.get(node);
		} catch (Exception e) {
			throw new IllegalStateException("Can't get JCPrimitiveTypeTree typetag");
		}
	}
	
	private static Method classDef;
	
	public static JCClassDecl ClassDef(TreeMaker maker, JCModifiers mods, Name name, List<JCTypeParameter> typarams, JCExpression extending, List<JCExpression> implementing, List<JCTree> defs) {
		if (classDef == null) try {
			classDef = TreeMaker.class.getDeclaredMethod("ClassDef", JCModifiers.class, Name.class, List.class, JCExpression.class, List.class, List.class);
		} catch (NoSuchMethodException ignore) {}
		if (classDef == null) try {
			classDef = TreeMaker.class.getDeclaredMethod("ClassDef", JCModifiers.class, Name.class, List.class, JCTree.class, List.class, List.class);
		} catch (NoSuchMethodException ignore) {}
		
		if (classDef == null) throw new IllegalStateException("Lombok bug #20130617-1310: ClassDef doesn't look like anything we thought it would look like.");
		if (!Modifier.isPublic(classDef.getModifiers()) && !classDef.isAccessible()) {
			classDef.setAccessible(true);
		}
		
		try {
			return (JCClassDecl) classDef.invoke(maker, mods, name, typarams, extending, implementing, defs);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) throw new NullPointerException("t");
		Javac.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
}
