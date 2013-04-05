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
package lombok.javac;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Pattern;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.TreeMaker;

/**
 * Container for static utility methods relevant to lombok's operation on javac.
 */
public class Javac {
	private Javac() {
		// prevent instantiation
	}
	
	/** Matches any of the 8 primitive names, such as {@code boolean}. */
	private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile("^(boolean|byte|short|int|long|float|double|char)$");
	
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
	
//	/**
//	 * Retrieves the current ordinal position of a type tag.
//	 * 
//	 * For JDK 8 this is the ordinal position within the
//	 * <code>com.sun.tools.javac.code.TypeTag enum</code> for JDK 7 and lower,
//	 * this is the value of the constant within
//	 * <code>com.sun.tools.javac.code.TypeTags</code>
//	 * 
//	 * Solves the problem of compile time constant inlining, resulting in lombok
//	 * having the wrong value (javac compiler changes private api constants from
//	 * time to time).
//	 * 
//	 * @param identifier
//	 * @return the ordinal value of the typetag constant
//	 */
//	public static int getTypeTag(String identifier) {
//		try {
//			if (JavaCompiler.version().startsWith("1.8")) {
//				Object enumInstance = Class.forName("com.sun.tools.javac.code.TypeTag").getField(identifier).get(null);
//				return (int) Class.forName("com.sun.tools.javac.code.TypeTag").getField("order").get(enumInstance);
//				
//			} else {
//				return (int) Class.forName("com.sun.tools.javac.code.TypeTags").getField(identifier).get(null);
//			}
//		} catch (NoSuchFieldException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (Exception e) {
//			if (e instanceof RuntimeException) throw (RuntimeException) e;
//			throw new RuntimeException(e);
//		}
//	}
	
	
	
	public static boolean compareCTC(Object ctc1, Object ctc2) {
		return Objects.equals(ctc1, ctc2);
	}
	
	/**
	 * Retrieves the current type tag. The actual type object differs depending on the Compiler version
	 * 
	 * For JDK 8 this is an enum value of type <code>com.sun.tools.javac.code.TypeTag</code> 
	 * for JDK 7 and lower, this is the value of the constant within <code>com.sun.tools.javac.code.TypeTags</code>
	 * 
	 * Solves the problem of compile time constant inlining, resulting in lombok
	 * having the wrong value (javac compiler changes private api constants from
	 * time to time).
	 * 
	 * @param identifier
	 * @return the ordinal value of the typetag constant
	 */
	public static Object getTypeTag(String identifier) {
		try {
			if (JavaCompiler.version().startsWith("1.8")) {
				return Class.forName("com.sun.tools.javac.code.TypeTag").getField(identifier).get(null);
			} else {
				return Class.forName("com.sun.tools.javac.code.TypeTags").getField(identifier).get(null);
			}
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}	
	}
	
	public static Object getTreeTag(String identifier) {
 		try {
			if (JavaCompiler.version().startsWith("1.8")) {
				return Class.forName("com.sun.tools.javac.tree.JCTree$Tag").getField(identifier).get(null);
			} else {
				return Class.forName("com.sun.tools.javac.tree.JCTree").getField(identifier).get(null);
			}
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static Object getTreeTypeTag(JCPrimitiveTypeTree tree) {
		return tree.typetag;
	}
	
	public static Object getTreeTypeTag(JCLiteral tree) {
		return tree.typetag;
	}

	public static JCExpression makeTypeIdent(TreeMaker maker, Object ctc) {
		try {
			Method createIdent;
			if (JavaCompiler.version().startsWith("1.8")) {
				createIdent = TreeMaker.class.getMethod("TypeIdent", Class.forName("com.sun.tools.javac.code.TypeTag"));
			} else {
				createIdent = TreeMaker.class.getMethod("TypeIdent", Integer.TYPE);
			}
			return (JCExpression) createIdent.invoke(maker, ctc);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}
	
	public static JCLiteral makeLiteral(TreeMaker maker, Object ctc, Object argument) {
		try {
			Method createLiteral;
			if (JavaCompiler.version().startsWith("1.8")) {
				createLiteral = TreeMaker.class.getMethod("Literal", Class.forName("com.sun.tools.javac.code.TypeTag"), Object.class);
			} else {
				createLiteral = TreeMaker.class.getMethod("Literal", Integer.TYPE, Object.class);
			}
			return (JCLiteral) createLiteral.invoke(maker, ctc, argument);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}
	
	public static JCUnary makeUnary(TreeMaker maker, Object ctc, JCExpression argument) {
		try {
			Method createUnary;
			if (JavaCompiler.version().startsWith("1.8")) {
				createUnary = TreeMaker.class.getMethod("Unary", Class.forName("com.sun.tools.javac.code.TypeTag"), JCExpression.class);
			} else {
				createUnary = TreeMaker.class.getMethod("Unary", Integer.TYPE, JCExpression.class);
			}
			return (JCUnary) createUnary.invoke(maker, ctc, argument);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}
	
	public static JCBinary makeBinary(TreeMaker maker, Object ctc, JCExpression rhsArgument, JCExpression lhsArgument) {
		try {
			Method createUnary;
			if (JavaCompiler.version().startsWith("1.8")) {
				createUnary = TreeMaker.class.getMethod("Binary", Class.forName("com.sun.tools.javac.code.TypeTag"), JCExpression.class, JCExpression.class);
			} else {
				createUnary = TreeMaker.class.getMethod("Binary", Integer.TYPE, JCExpression.class, JCExpression.class);
			}
			return (JCBinary) createUnary.invoke(maker, ctc, rhsArgument, lhsArgument);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}
	

}
