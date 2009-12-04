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
package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.fromQualifiedName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Container for static utility methods useful to handlers written for eclipse.
 */
public class EclipseHandlerUtil {
	private EclipseHandlerUtil() {
		//Prevent instantiation
	}
	
	/**
	 * Checks if the given type reference represents a primitive type.
	 */
	public static boolean isPrimitive(TypeReference ref) {
		if (ref.dimensions() > 0) return false;
		return TransformationsUtil.PRIMITIVE_TYPE_NAME_PATTERN.matcher(Eclipse.toQualifiedName(ref.getTypeName())).matches();
	}
	
	/**
	 * Turns an {@code AccessLevel} instance into the flag bit used by eclipse.
	 */
	public static int toEclipseModifier(AccessLevel value) {
		switch (value) {
		case MODULE:
		case PACKAGE:
			return 0;
		default:
		case PUBLIC:
			return ClassFileConstants.AccPublic;
		case PROTECTED:
			return ClassFileConstants.AccProtected;
		case PRIVATE:
			return ClassFileConstants.AccPrivate;
		}
	}
	
	/**
	 * Checks if an eclipse-style array-of-array-of-characters to represent a fully qualified name ('foo.bar.baz'), matches a plain
	 * string containing the same fully qualified name with dots in the string.
	 */
	public static boolean nameEquals(char[][] typeName, String string) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (char[] elem : typeName) {
			if (first) first = false;
			else sb.append('.');
			sb.append(elem);
		}
		
		return string.contentEquals(sb);
	}
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_USER, EXISTS_BY_LOMBOK;
	}
	
	/**
	 * Checks if there is a field with the provided name.
	 * 
	 * @param fieldName the field name to check for.
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static MemberExistsResult fieldExists(String fieldName, EclipseNode node) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if (typeDecl.fields != null) for (FieldDeclaration def : typeDecl.fields) {
				char[] fName = def.name;
				if (fName == null) continue;
				if (fieldName.equals(new String(fName))) {
					EclipseNode existing = node.getNodeFor(def);
					if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	/**
	 * Wrapper for {@link #methodExists(String, EclipseNode, boolean)} with {@code caseSensitive} = {@code true}.
	 */
	public static MemberExistsResult methodExists(String methodName, EclipseNode node) {
		return methodExists(methodName, node, true);
	}
	
	/**
	 * Checks if there is a method with the provided name. In case of multiple methods (overloading), only
	 * the first method decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param methodName the method name to check for.
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 * @param caseSensitive If the search should be case sensitive.
	 */
	public static MemberExistsResult methodExists(String methodName, EclipseNode node, boolean caseSensitive) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				char[] mName = def.selector;
				if (mName == null) continue;
				boolean nameEquals = caseSensitive ? methodName.equals(new String(mName)) : methodName.equalsIgnoreCase(new String(mName));
				if (nameEquals) {
					EclipseNode existing = node.getNodeFor(def);
					if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	/**
	 * Checks if there is a (non-default) constructor. In case of multiple constructors (overloading), only
	 * the first constructor decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static MemberExistsResult constructorExists(EclipseNode node) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (def instanceof ConstructorDeclaration) {
					if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
					EclipseNode existing = node.getNodeFor(def);
					if (existing == null || !existing.isHandled()) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	/**
	 * Returns the constructor that's already been generated by lombok.
	 * Provide any node that represents the type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static EclipseNode getExistingLombokConstructor(EclipseNode node) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node == null) return null;
		
		if (node.get() instanceof TypeDeclaration) {
			for (AbstractMethodDeclaration def : ((TypeDeclaration)node.get()).methods) {
				if (def instanceof ConstructorDeclaration) {
					if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
					EclipseNode existing = node.getNodeFor(def);
					if (existing.isHandled()) return existing;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the method that's already been generated by lombok with the given name.
	 * Provide any node that represents the type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static EclipseNode getExistingLombokMethod(String methodName, EclipseNode node) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node == null) return null;
		
		if (node.get() instanceof TypeDeclaration) {
			for (AbstractMethodDeclaration def : ((TypeDeclaration)node.get()).methods) {
				char[] mName = def.selector;
				if (mName == null) continue;
				if (methodName.equals(new String(mName))) {
					EclipseNode existing = node.getNodeFor(def);
					if (existing.isHandled()) return existing;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Inserts a field into an existing type. The type must represent a {@code TypeDeclaration}.
	 */
	public static void injectField(EclipseNode type, FieldDeclaration field) {
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if (parent.fields == null) {
			parent.fields = new FieldDeclaration[1];
			parent.fields[0] = field;
		} else {
			FieldDeclaration[] newArray = new FieldDeclaration[parent.fields.length + 1];
			System.arraycopy(parent.fields, 0, newArray, 0, parent.fields.length);
			newArray[parent.fields.length] = field;
			parent.fields = newArray;
		}
		
		type.add(field, Kind.FIELD).recursiveSetHandled();
	}
	
	/**
	 * Inserts a method into an existing type. The type must represent a {@code TypeDeclaration}.
	 */
	public static void injectMethod(EclipseNode type, AbstractMethodDeclaration method) {
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if (parent.methods == null) {
			parent.methods = new AbstractMethodDeclaration[1];
			parent.methods[0] = method;
		} else {
			boolean injectionComplete = false;
			if (method instanceof ConstructorDeclaration) {
				for (int i = 0 ; i < parent.methods.length ; i++) {
					if (parent.methods[i] instanceof ConstructorDeclaration &&
							(parent.methods[i].bits & ASTNode.IsDefaultConstructor) != 0) {
						EclipseNode tossMe = type.getNodeFor(parent.methods[i]);
						parent.methods[i] = method;
						if (tossMe != null) tossMe.up().removeChild(tossMe);
						injectionComplete = true;
						break;
					}
				}
			}
			if (!injectionComplete) {
				AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
				System.arraycopy(parent.methods, 0, newArray, 0, parent.methods.length);
				newArray[parent.methods.length] = method;
				parent.methods = newArray;
			}
		}
		
		type.add(method, Kind.METHOD).recursiveSetHandled();
	}
	
	/**
	 * Searches the given field node for annotations and returns each one that matches the provided regular expression pattern.
	 * 
	 * Only the simple name is checked - the package and any containing class are ignored.
	 */
	public static Annotation[] findAnnotations(FieldDeclaration field, Pattern namePattern) {
		List<Annotation> result = new ArrayList<Annotation>();
		if (field.annotations == null) return new Annotation[0];
		for (Annotation annotation : field.annotations) {
			TypeReference typeRef = annotation.type;
			if (typeRef != null && typeRef.getTypeName()!= null) {
				char[][] typeName = typeRef.getTypeName();
				String suspect = new String(typeName[typeName.length - 1]);
				if (namePattern.matcher(suspect).matches()) {
					result.add(annotation);
				}
			}
		}	
		return result.toArray(new Annotation[0]);
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a {@code NullPointerException} with the
	 * variable name as message.
	 */
	public static Statement generateNullCheck(AbstractVariableDeclaration variable, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		if (isPrimitive(variable.type)) return null;
		AllocationExpression exception = new AllocationExpression();
		Eclipse.setGeneratedBy(exception, source);
		exception.type = new QualifiedTypeReference(fromQualifiedName("java.lang.NullPointerException"), new long[]{p, p, p});
		Eclipse.setGeneratedBy(exception.type, source);
		exception.arguments = new Expression[] { new StringLiteral(variable.name, pS, pE, 0)};
		Eclipse.setGeneratedBy(exception.arguments[0], source);
		ThrowStatement throwStatement = new ThrowStatement(exception, pS, pE);
		Eclipse.setGeneratedBy(throwStatement, source);
		
		SingleNameReference varName = new SingleNameReference(variable.name, p);
		Eclipse.setGeneratedBy(varName, source);
		NullLiteral nullLiteral = new NullLiteral(pS, pE);
		Eclipse.setGeneratedBy(nullLiteral, source);
		EqualExpression equalExpression = new EqualExpression(varName, nullLiteral, OperatorIds.EQUAL_EQUAL);
		equalExpression.sourceStart = pS; equalExpression.sourceEnd = pE;
		Eclipse.setGeneratedBy(equalExpression, source);
		IfStatement ifStatement = new IfStatement(equalExpression, throwStatement, 0, 0);
		Eclipse.setGeneratedBy(ifStatement, source);
		return ifStatement;
	}
	
	/**
	 * Create an annotation of the given name, and is marked as being generated by the given source.
	 */
	public static MarkerAnnotation makeMarkerAnnotation(char[][] name, ASTNode source) {
		long pos = (long)source.sourceStart << 32 | source.sourceEnd;
		TypeReference typeRef = new QualifiedTypeReference(name, new long[] {pos, pos, pos});
		Eclipse.setGeneratedBy(typeRef, source);
		MarkerAnnotation ann = new MarkerAnnotation(typeRef, (int)(pos >> 32));
		ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = (int)pos;
		Eclipse.setGeneratedBy(ann, source);
		return ann;
	}
	
	/**
	 * Given a list of field names and a node referring to a type, finds each name in the list that does not match a field within the type.
	 */
	public static List<Integer> createListOfNonExistentFields(List<String> list, EclipseNode type, boolean excludeStandard, boolean excludeTransient) {
		boolean[] matched = new boolean[list.size()];
		
		for (EclipseNode child : type.down()) {
			if (list.isEmpty()) break;
			if (child.getKind() != Kind.FIELD) continue;
			if (excludeStandard) {
				if ((((FieldDeclaration)child.get()).modifiers & ClassFileConstants.AccStatic) != 0) continue;
				if (child.getName().startsWith("$")) continue;
			}
			if (excludeTransient && (((FieldDeclaration)child.get()).modifiers & ClassFileConstants.AccTransient) != 0) continue;
			int idx = list.indexOf(child.getName());
			if (idx > -1) matched[idx] = true;
		}
		
		List<Integer> problematic = new ArrayList<Integer>();
		for (int i = 0 ; i < list.size() ; i++) {
			if (!matched[i]) problematic.add(i);
		}
		
		return problematic;
	}
}
