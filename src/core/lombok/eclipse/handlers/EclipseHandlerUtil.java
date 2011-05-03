/*
 * Copyright © 2009-2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import static lombok.eclipse.Eclipse.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

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
		case NONE:
		case PRIVATE:
			return ClassFileConstants.AccPrivate;
		}
	}
	
	private static class GetterMethod {
		private final char[] name;
		private final TypeReference type;
		
		GetterMethod(char[] name, TypeReference type) {
			this.name = name;
			this.type = type;
		}
	}
	
	private static GetterMethod findGetter(EclipseNode field) {
		TypeReference fieldType = ((FieldDeclaration)field.get()).type;
		boolean isBoolean = nameEquals(fieldType.getTypeName(), "boolean") && fieldType.dimensions() == 0;
		EclipseNode typeNode = field.up();
		for (String potentialGetterName : TransformationsUtil.toAllGetterNames(field.getName(), isBoolean)) {
			for (EclipseNode potentialGetter : typeNode.down()) {
				if (potentialGetter.getKind() != Kind.METHOD) continue;
				if (!(potentialGetter.get() instanceof MethodDeclaration)) continue;
				MethodDeclaration method = (MethodDeclaration) potentialGetter.get();
				if (!potentialGetterName.equalsIgnoreCase(new String(method.selector))) continue;
				/** static getX() methods don't count. */
				if ((method.modifiers & ClassFileConstants.AccStatic) != 0) continue;
				/** Nor do getters with a non-empty parameter list. */
				if (method.arguments != null && method.arguments.length > 0) continue;
				return new GetterMethod(method.selector, method.returnType);
			}
		}
		
		// Check if the field has a @Getter annotation.
		
		boolean hasGetterAnnotation = false;
		
		for (EclipseNode child : field.down()) {
			if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Getter.class, child)) {
				AnnotationValues<Getter> ann = Eclipse.createAnnotation(Getter.class, child);
				if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
				hasGetterAnnotation = true;
			}
		}
		
		// Check if the class has a @Getter annotation.
		
		if (!hasGetterAnnotation && new HandleGetter().fieldQualifiesForGetterGeneration(field)) {
			//Check if the class has @Getter or @Data annotation.
			
			EclipseNode containingType = field.up();
			if (containingType != null) for (EclipseNode child : containingType.down()) {
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Data.class, child)) hasGetterAnnotation = true;
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Getter.class, child)) {
					AnnotationValues<Getter> ann = Eclipse.createAnnotation(Getter.class, child);
					if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
					hasGetterAnnotation = true;
				}
			}
		}
		
		if (hasGetterAnnotation) {
			String getterName = TransformationsUtil.toGetterName(field.getName(), isBoolean);
			return new GetterMethod(getterName.toCharArray(), fieldType);
		}
		
		return null;
	}
	
	enum FieldAccess {
		GETTER, PREFER_FIELD, ALWAYS_FIELD;
	}
	
	static boolean lookForGetter(EclipseNode field, FieldAccess fieldAccess) {
		if (fieldAccess == FieldAccess.GETTER) return true;
		if (fieldAccess == FieldAccess.ALWAYS_FIELD) return false;
		
		// If @Getter(lazy = true) is used, then using it is mandatory.
		for (EclipseNode child : field.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			if (Eclipse.annotationTypeMatches(Getter.class, child)) {
				AnnotationValues<Getter> ann = Eclipse.createAnnotation(Getter.class, child);
				if (ann.getInstance().lazy()) return true;
			}
		}
		return false;
	}
	
	static TypeReference getFieldType(EclipseNode field, FieldAccess fieldAccess) {
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		if (getter == null) {
			return ((FieldDeclaration)field.get()).type;
		}
		
		return getter.type;
	}
	
	static Expression createFieldAccessor(EclipseNode field, FieldAccess fieldAccess, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		
		if (getter == null) {
			FieldDeclaration fieldDecl = (FieldDeclaration)field.get();
			FieldReference ref = new FieldReference(fieldDecl.name, p);
			if ((fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0) {
				EclipseNode containerNode = field.up();
				if (containerNode != null && containerNode.get() instanceof TypeDeclaration) {
					ref.receiver = new SingleNameReference(((TypeDeclaration)containerNode.get()).name, p);
				} else {
					Expression smallRef = new FieldReference(field.getName().toCharArray(), p);
					Eclipse.setGeneratedBy(smallRef, source);
					return smallRef;
				}
			} else {
				ref.receiver = new ThisReference(pS, pE);
			}
			Eclipse.setGeneratedBy(ref, source);
			Eclipse.setGeneratedBy(ref.receiver, source);
			return ref;
		}
		
		MessageSend call = new MessageSend();
		Eclipse.setGeneratedBy(call, source);
		call.sourceStart = pS; call.sourceEnd = pE;
		call.receiver = new ThisReference(pS, pE);
		Eclipse.setGeneratedBy(call.receiver, source);
		call.selector = getter.name;
		return call;
	}
	
	static Expression createFieldAccessor(EclipseNode field, FieldAccess fieldAccess, ASTNode source, char[] receiver) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		
		if (getter == null) {
			NameReference ref;
			
			char[][] tokens = new char[2][];
			tokens[0] = receiver;
			tokens[1] = field.getName().toCharArray();
			long[] poss = {p, p};
			
			ref = new QualifiedNameReference(tokens, poss, pS, pE);
			Eclipse.setGeneratedBy(ref, source);
			return ref;
		}
		
		MessageSend call = new MessageSend();
		Eclipse.setGeneratedBy(call, source);
		call.sourceStart = pS; call.sourceEnd = pE;
		call.receiver = new SingleNameReference(receiver, p);
		Eclipse.setGeneratedBy(call.receiver, source);
		call.selector = getter.name;
		return call;
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
	 * Checks if the field should be included in operations that work on 'all' fields:
	 *    If the field is static, or starts with a '$', or is actually an enum constant, 'false' is returned, indicating you should skip it.
	 */
	public static boolean filterField(FieldDeclaration declaration) {
		// Skip the fake fields that represent enum constants.
		if (declaration.initialization instanceof AllocationExpression &&
				((AllocationExpression)declaration.initialization).enumConstant != null) return false;
		
		if (declaration.type == null) return false;
		
		// Skip fields that start with $
		if (declaration.name.length > 0 && declaration.name[0] == '$') return false;
		
		// Skip static fields.
		if ((declaration.modifiers & ClassFileConstants.AccStatic) != 0) return false;
		
		return true;
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
				if (def instanceof MethodDeclaration) {
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
	 * The field carries the &#64;{@link SuppressWarnings}("all") annotation.
	 */
	public static void injectFieldSuppressWarnings(EclipseNode type, FieldDeclaration field) {
		field.annotations = createSuppressWarningsAll(field, field.annotations);
		injectField(type, field);
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
		
		if ((field.modifiers & Modifier.STATIC) != 0) {
			if (!hasClinit(parent)) {
				parent.addClinit();
			}
		}
		
		type.add(field, Kind.FIELD).recursiveSetHandled();
	}
	
	private static boolean hasClinit(TypeDeclaration parent) {
		if (parent.methods == null) return false;
		
		for (AbstractMethodDeclaration method : parent.methods) {
			if (method instanceof Clinit) return true;
		}
		return false;
	}

	/**
	 * Inserts a method into an existing type. The type must represent a {@code TypeDeclaration}.
	 */
	public static void injectMethod(EclipseNode type, AbstractMethodDeclaration method) {
		method.annotations = createSuppressWarningsAll(method, method.annotations);
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if (parent.scope != null && method.scope == null) {
			// We think this means heisenbug #164 is about to happen later in some other worker thread.
			// To improve our ability to figure out what the heck is going on, let's generate a log so we can ask those who stumble on this about it,
			// and thus see a far more useful stack trace.
			boolean report = true;
			for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
				// We intentionally hook into the middle of ClassScope filling in BlockScopes for PatchDelegate,
				// meaning that will trigger a false positive. Detect it and do not report the occurence of #164 if so.
				if ("lombok.eclipse.agent.PatchDelegate".equals(elem.getClassName())) {
					report = false;
					break;
				}
			}
			
			if (report) {
				Eclipse.warning("We believe you may have just stumbled on lombok issue #164. Please " +
						"report the stack trace associated with this message at:\n" +
						"http://code.google.com/p/projectlombok/issues/detail?id=164", new Throwable());
			}
		}
		
		if (parent.methods == null) {
			parent.methods = new AbstractMethodDeclaration[1];
			parent.methods[0] = method;
		} else {
			if (method instanceof ConstructorDeclaration) {
				for (int i = 0 ; i < parent.methods.length ; i++) {
					if (parent.methods[i] instanceof ConstructorDeclaration &&
							(parent.methods[i].bits & ASTNode.IsDefaultConstructor) != 0) {
						EclipseNode tossMe = type.getNodeFor(parent.methods[i]);
						
						AbstractMethodDeclaration[] withoutGeneratedConstructor = new AbstractMethodDeclaration[parent.methods.length - 1];
						
						System.arraycopy(parent.methods, 0, withoutGeneratedConstructor, 0, i);
						System.arraycopy(parent.methods, i + 1, withoutGeneratedConstructor, i, parent.methods.length - i - 1);
						
						parent.methods = withoutGeneratedConstructor;
						if (tossMe != null) tossMe.up().removeChild(tossMe);
						break;
					}
				}
			}
			int insertionPoint;
			for (insertionPoint = 0; insertionPoint < parent.methods.length; insertionPoint++) {
				AbstractMethodDeclaration current = parent.methods[insertionPoint];
				if (current instanceof Clinit) continue;
				if (method instanceof ConstructorDeclaration) {
					if (current instanceof ConstructorDeclaration) continue;
					break;
				}
				if (Eclipse.isGenerated(current)) continue;
				break;
			}
			AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
			System.arraycopy(parent.methods, 0, newArray, 0, insertionPoint);
			if (insertionPoint <= parent.methods.length) {
				System.arraycopy(parent.methods, insertionPoint, newArray, insertionPoint + 1, parent.methods.length - insertionPoint);
			}
			
			newArray[insertionPoint] = method;
			parent.methods = newArray;
		}
		
		type.add(method, Kind.METHOD).recursiveSetHandled();
	}
	
	private static final char[] ALL = "all".toCharArray();
	
	public static Annotation[] createSuppressWarningsAll(ASTNode source, Annotation[] originalAnnotationArray) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		long[] poss = new long[3];
		Arrays.fill(poss, p);
		QualifiedTypeReference suppressWarningsType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, poss);
		Eclipse.setGeneratedBy(suppressWarningsType, source);
		SingleMemberAnnotation ann = new SingleMemberAnnotation(suppressWarningsType, pS);
		ann.declarationSourceEnd = pE;
		ann.memberValue = new StringLiteral(ALL, pS, pE, 0);
		Eclipse.setGeneratedBy(ann, source);
		Eclipse.setGeneratedBy(ann.memberValue, source);
		if (originalAnnotationArray == null) return new Annotation[] { ann };
		Annotation[] newAnnotationArray = Arrays.copyOf(originalAnnotationArray, originalAnnotationArray.length + 1);
		newAnnotationArray[originalAnnotationArray.length] = ann;
		return newAnnotationArray;
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
	
	/**
	 * In eclipse 3.7+, the CastExpression constructor was changed from a really weird version to
	 * a less weird one. Unfortunately that means we need to use reflection as we want to be compatible
	 * with eclipse versions before 3.7 and 3.7+.
	 * 
	 * @param ref The {@code foo} in {@code (String)foo}.
	 * @param castTo The {@code String} in {@code (String)foo}.
	 */
	public static CastExpression makeCastExpression(Expression ref, TypeReference castTo, ASTNode source) {
		CastExpression result;
		try {
			if (castExpressionConstructorIsTypeRefBased) {
				result = castExpressionConstructor.newInstance(ref, castTo);
			} else {
				Expression castToConverted = castTo;
				
				if (castTo.getClass() == SingleTypeReference.class && !PRIMITIVE_NAMES.contains(
						" " + new String(((SingleTypeReference)castTo).token) + " ")) {
					SingleTypeReference str = (SingleTypeReference) castTo;
					//Why a SingleNameReference instead of a SingleTypeReference you ask? I don't know. It seems dumb. Ask the ecj guys.
					castToConverted = new SingleNameReference(str.token, 0);
					castToConverted.bits = (castToConverted.bits & ~Binding.VARIABLE) | Binding.TYPE;
					castToConverted.sourceStart = str.sourceStart;
					castToConverted.sourceEnd = str.sourceEnd;
					Eclipse.setGeneratedBy(castToConverted, source);
				} else if (castTo.getClass() == QualifiedTypeReference.class) {
					QualifiedTypeReference qtr = (QualifiedTypeReference) castTo;
					//Same here, but for the more complex types, they stay types.
					castToConverted = new QualifiedNameReference(qtr.tokens, qtr.sourcePositions, qtr.sourceStart, qtr.sourceEnd);
					castToConverted.bits = (castToConverted.bits & ~Binding.VARIABLE) | Binding.TYPE;
					Eclipse.setGeneratedBy(castToConverted, source);
				}
				
				result = castExpressionConstructor.newInstance(ref, castToConverted);
			}
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InstantiationException e) {
			throw Lombok.sneakyThrow(e);
		}
		
		Eclipse.setGeneratedBy(result, source);
		return result;
	}
	
	private static final String PRIMITIVE_NAMES = " int long float double char short byte boolean ";
	private static final Constructor<CastExpression> castExpressionConstructor;
	private static final boolean castExpressionConstructorIsTypeRefBased;
	
	static {
		Constructor<?> constructor = null;
		for (Constructor<?> ctor : CastExpression.class.getConstructors()) {
			if (ctor.getParameterTypes().length != 2) continue;
			constructor = ctor;
		}
		
		@SuppressWarnings("unchecked")
		Constructor<CastExpression> constructor_ = (Constructor<CastExpression>) constructor;
		castExpressionConstructor = constructor_;
		
		castExpressionConstructorIsTypeRefBased =
				(castExpressionConstructor.getParameterTypes()[1] == TypeReference.class);
	}
	
	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
	static Annotation[] getAndRemoveAnnotationParameter(Annotation annotation, String annotationName) {
		
		List<Annotation> result = new ArrayList<Annotation>();
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation)annotation;
			MemberValuePair[] memberValuePairs = normalAnnotation.memberValuePairs;
			List<MemberValuePair> pairs = new ArrayList<MemberValuePair>();
			if (memberValuePairs != null) for (MemberValuePair memberValuePair : memberValuePairs) {
				if (annotationName.equals(new String(memberValuePair.name))) {
					Expression value = memberValuePair.value;
					if (value instanceof ArrayInitializer) {
						ArrayInitializer array = (ArrayInitializer) value;
						for(Expression expression : array.expressions) {
							if (expression instanceof Annotation) {
								result.add((Annotation)expression);
							}
						}
					}
					else if (value instanceof Annotation) {
						result.add((Annotation)value);
					}
					continue;
				}
				pairs.add(memberValuePair);
			}
			
			if (!result.isEmpty()) {
				normalAnnotation.memberValuePairs = pairs.isEmpty() ? null : pairs.toArray(new MemberValuePair[0]);
				return result.toArray(EMPTY_ANNOTATION_ARRAY);
			}
		}
		
		return EMPTY_ANNOTATION_ARRAY;
	}
	
	static NameReference createNameReference(String name, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		char[][] nameTokens = fromQualifiedName(name);
		long[] pos = new long[nameTokens.length];
		Arrays.fill(pos, p);
		
		QualifiedNameReference nameReference = new QualifiedNameReference(nameTokens, pos, pS, pE);
		nameReference.statementEnd = pE;

		Eclipse.setGeneratedBy(nameReference, source);
		return nameReference;
	}
}
