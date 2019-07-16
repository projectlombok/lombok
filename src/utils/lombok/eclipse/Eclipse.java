/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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
package lombok.eclipse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.core.ClassLiteral;
import lombok.core.FieldSelect;
import lombok.permit.Permit;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class Eclipse {
	private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];
	/**
	 * Eclipse's Parser class is instrumented to not attempt to fill in the body of any method or initializer
	 * or field initialization if this flag is set. Set it on the flag field of
	 * any method, field, or initializer you create!
	 */
	public static final int ECLIPSE_DO_NOT_TOUCH_FLAG = ASTNode.Bit24;
	
	private static final Pattern SPLIT_AT_DOT = Pattern.compile("\\.");
	
	private Eclipse() {
		//Prevent instantiation
	}
	
	/**
	 * For 'speed' reasons, Eclipse works a lot with char arrays. I have my doubts this was a fruitful exercise,
	 * but we need to deal with it. This turns [[java][lang][String]] into "java.lang.String".
	 */
	public static String toQualifiedName(char[][] typeName) {
		int len = typeName.length - 1; // number of dots
		if (len == 0) return new String(typeName[0]);
		
		for (char[] c : typeName) len += c.length;
		char[] ret = new char[len];
		char[] part = typeName[0];
		System.arraycopy(part, 0, ret, 0, part.length);
		int pos = part.length;
		for (int i = 1; i < typeName.length; i++) {
			ret[pos++] = '.';
			part = typeName[i];
			System.arraycopy(part, 0, ret, pos, part.length);
			pos += part.length;
		}
		return new String(ret);
	}
	
	public static char[][] fromQualifiedName(String typeName) {
		String[] split = SPLIT_AT_DOT.split(typeName);
		char[][] result = new char[split.length][];
		for (int i = 0; i < split.length; i++) {
			result[i] = split[i].toCharArray();
		}
		return result;
	}
	
	public static long pos(ASTNode node) {
		return ((long) node.sourceStart << 32) | (node.sourceEnd & 0xFFFFFFFFL);
	}
	
	public static long[] poss(ASTNode node, int repeat) {
		long p = ((long) node.sourceStart << 32) | (node.sourceEnd & 0xFFFFFFFFL);
		long[] out = new long[repeat];
		Arrays.fill(out, p);
		return out;
	}
	
	/**
	 * Checks if an eclipse-style array-of-array-of-characters to represent a fully qualified name ('foo.bar.baz'), matches a plain
	 * string containing the same fully qualified name with dots in the string.
	 */
	public static boolean nameEquals(char[][] typeName, String string) {
		int pos = 0, len = string.length();
		for (int i = 0; i < typeName.length; i++) {
			char[] t = typeName[i];
			if (i > 0) {
				if (pos == len) return false;
				if (string.charAt(pos++) != '.') return false;
			}
			for (int j = 0; j < t.length; j++) {
				if (pos == len) return false;
				if (string.charAt(pos++) != t[j]) return false;
			}
		}
		
		return true;
	}
	
	public static boolean hasClinit(TypeDeclaration parent) {
		if (parent.methods == null) return false;
		
		for (AbstractMethodDeclaration method : parent.methods) {
			if (method instanceof Clinit) return true;
		}
		return false;
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that matches the provided regular expression pattern.
	 * 
	 * Only the simple name is checked - the package and any containing class are ignored.
	 */
	public static Annotation[] findAnnotations(AbstractVariableDeclaration field, Pattern namePattern) {
		List<Annotation> result = new ArrayList<Annotation>();
		if (field.annotations == null) return EMPTY_ANNOTATIONS_ARRAY;
		for (Annotation annotation : field.annotations) {
			TypeReference typeRef = annotation.type;
			if (typeRef != null && typeRef.getTypeName() != null) {
				char[][] typeName = typeRef.getTypeName();
				String suspect = new String(typeName[typeName.length - 1]);
				if (namePattern.matcher(suspect).matches()) {
					result.add(annotation);
				}
			}
		}	
		return result.toArray(EMPTY_ANNOTATIONS_ARRAY);
	}
	
	/** Matches any of the 8 primitive names, such as {@code boolean}. */
	private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile(
			"^(boolean|byte|short|int|long|float|double|char)$");
	
	/**
	 * Checks if the given type reference represents a primitive type.
	 */
	public static boolean isPrimitive(TypeReference ref) {
		if (ref.dimensions() > 0) return false;
		return PRIMITIVE_TYPE_NAME_PATTERN.matcher(toQualifiedName(ref.getTypeName())).matches();
	}
	
	/**
	 * Returns the actual value of the given Literal or Literal-like node.
	 */
	public static Object calculateValue(Expression e) {
		if (e instanceof Literal) {
			((Literal) e).computeConstant();
			switch (e.constant.typeID()) {
			case TypeIds.T_int: return e.constant.intValue();
			case TypeIds.T_byte: return e.constant.byteValue();
			case TypeIds.T_short: return e.constant.shortValue();
			case TypeIds.T_char: return e.constant.charValue();
			case TypeIds.T_float: return e.constant.floatValue();
			case TypeIds.T_double: return e.constant.doubleValue();
			case TypeIds.T_boolean: return e.constant.booleanValue();
			case TypeIds.T_long: return e.constant.longValue();
			case TypeIds.T_JavaLangString: return e.constant.stringValue();
			default: return null;
			}
		} else if (e instanceof ClassLiteralAccess) {
			return new ClassLiteral(Eclipse.toQualifiedName(((ClassLiteralAccess) e).type.getTypeName()));
		} else if (e instanceof SingleNameReference) {
			return new FieldSelect(new String(((SingleNameReference)e).token));
		} else if (e instanceof QualifiedNameReference) {
			String qName = Eclipse.toQualifiedName(((QualifiedNameReference) e).tokens);
			int idx = qName.lastIndexOf('.');
			return new FieldSelect(idx == -1 ? qName : qName.substring(idx+1));
		} else if (e instanceof UnaryExpression) {
			if ("-".equals(((UnaryExpression) e).operatorToString())) {
				Object inner = calculateValue(((UnaryExpression) e).expression);
				if (inner instanceof Integer) return - ((Integer) inner).intValue();
				if (inner instanceof Byte) return - ((Byte) inner).byteValue();
				if (inner instanceof Short) return - ((Short) inner).shortValue();
				if (inner instanceof Long) return - ((Long) inner).longValue();
				if (inner instanceof Float) return - ((Float) inner).floatValue();
				if (inner instanceof Double) return - ((Double) inner).doubleValue();
				return null;
			}
		}
		
		return null;
	}
	
	private static long latestEcjCompilerVersionConstantCached = 0;
	
	public static long getLatestEcjCompilerVersionConstant() {
		if (latestEcjCompilerVersionConstantCached != 0) return latestEcjCompilerVersionConstantCached;
		
		int highestVersionSoFar = 0;
		for (Field f : ClassFileConstants.class.getDeclaredFields()) {
			try {
				if (f.getName().startsWith("JDK1_")) {
					int thisVersion = Integer.parseInt(f.getName().substring("JDK1_".length()));
					if (thisVersion > highestVersionSoFar) {
						highestVersionSoFar = thisVersion;
						latestEcjCompilerVersionConstantCached = (Long) f.get(null);
					}
				}
			} catch (Exception ignore) {}
		}
		
		if (highestVersionSoFar > 6 && !ecjSupportsJava7Features()) {
			latestEcjCompilerVersionConstantCached = ClassFileConstants.JDK1_6;
		}
		return latestEcjCompilerVersionConstantCached;
	}
	
	private static int ecjCompilerVersionCached = -1;
	public static int getEcjCompilerVersion() {
		if (ecjCompilerVersionCached >= 0) return ecjCompilerVersionCached;
		
		for (Field f : CompilerOptions.class.getDeclaredFields()) {
			try {
				if (f.getName().startsWith("VERSION_1_")) {
					ecjCompilerVersionCached = Math.max(ecjCompilerVersionCached, Integer.parseInt(f.getName().substring("VERSION_1_".length())));
				}
			} catch (Exception ignore) {}
		}
		
		if (ecjCompilerVersionCached < 5) ecjCompilerVersionCached = 5;
		if (!ecjSupportsJava7Features()) ecjCompilerVersionCached = Math.min(6, ecjCompilerVersionCached);
		return ecjCompilerVersionCached;
	}
	
	/**
	 * Certain ECJ versions that only go up to -source 6 report that they support -source 7 and even fail to error when -source 7 is applied.
	 * We detect this and correctly say that no more than -source 6 is supported. (when this is the case, this method returns false).
	 */
	private static boolean ecjSupportsJava7Features() {
		try {
			TryStatement.class.getDeclaredField("resources");
			return true;
		} catch (NoSuchFieldException e) {
			return false;
		}
	}
	
	private static boolean caseStatementInit = false;
	private static Field caseStatementConstantExpressions = null;
	public static CaseStatement createCaseStatement(Expression expr) {
		CaseStatement stat = new CaseStatement(expr, 0, 0);
		if (expr == null) return stat;
		if (!caseStatementInit) {
			try {
				caseStatementConstantExpressions = Permit.getField(CaseStatement.class, "constantExpressions");
				caseStatementConstantExpressions.setAccessible(true);
			} catch (NoSuchFieldException ignore) {}
			caseStatementInit = true;
		}
		if (caseStatementConstantExpressions != null) try {
			caseStatementConstantExpressions.set(stat, new Expression[] {expr});
		} catch (IllegalArgumentException ignore) {
		} catch (IllegalAccessException ignore) {}
		return stat;
	}
}
