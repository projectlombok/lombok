/*
 * Copyright (C) 2009-2024 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.EcjAugments.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.EclipseReflectiveMembers.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.Getter;
import lombok.Lombok;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.LombokImmutableList;
import lombok.core.TypeResolver;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.configuration.NullAnnotationLibrary;
import lombok.core.configuration.NullCheckExceptionType;
import lombok.core.configuration.TypeName;
import lombok.core.debug.ProblemReporter;
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.core.handlers.HandlerUtil.JavadocTag;
import lombok.eclipse.EcjAugments;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import lombok.permit.Permit;

/**
 * Container for static utility methods useful to handlers written for eclipse.
 */
public class EclipseHandlerUtil {
	private EclipseHandlerUtil() {
		//Prevent instantiation
	}
	
	/**
	 * Generates an error in the Eclipse error log. Note that most people never look at it!
	 * 
	 * @param cud The {@code CompilationUnitDeclaration} where the error occurred.
	 *     An error will be generated on line 0 linking to the error log entry. Can be {@code null}.
	 * @param message Human readable description of the problem.
	 * @param ex The associated exception. Can be {@code null}.
	 */
	public static void error(CompilationUnitDeclaration cud, String message, Throwable ex) {
		ProblemReporter.error(message, ex);
		if (cud != null) EclipseAST.addProblemToCompilationResult(cud.getFileName(), cud.compilationResult, false, message + " - See error log.", 0, 0);
	}
	
	/**
	 * Generates a warning in the Eclipse error log. Note that most people never look at it!
	 * 
	 * @param message Human readable description of the problem.
	 * @param ex The associated exception. Can be {@code null}.
	 */
	public static void warning(String message, Throwable ex) {
		ProblemReporter.warning(message, ex);
	}
	
	public static ASTNode getGeneratedBy(ASTNode node) {
		return ASTNode_generatedBy.get(node);
	}
	
	public static boolean isGenerated(ASTNode node) {
		return getGeneratedBy(node) != null;
	}
	
	public static <T extends ASTNode> T setGeneratedBy(T node, ASTNode source) {
		ASTNode_generatedBy.set(node, source);
		return node;
	}
	
	public static MarkerAnnotation generateDeprecatedAnnotation(ASTNode source) {
		QualifiedTypeReference qtr = new QualifiedTypeReference(new char[][] {
				{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'D', 'e', 'p', 'r', 'e', 'c', 'a', 't', 'e', 'd'}}, poss(source, 3));
		setGeneratedBy(qtr, source);
		MarkerAnnotation ma = new MarkerAnnotation(qtr, source.sourceStart);
		// No matter what value you input for sourceEnd, the AST->DOM converter of eclipse will reparse to find the end, and will fail as
		// it can't find code that isn't really there. This results in the end position being set to 2 or 0 or some weird magic value, and thus,
		// length, as calculated by end-start, is all screwed up, resulting in IllegalArgumentException during a setSourceRange call MUCH later in the process.
		// We solve it by going with a voodoo magic source start value such that the calculated length so happens to exactly be 0. 0 lengths are accepted
		// by eclipse. For some reason.
		// TL;DR: Don't change 1. 1 is sacred. Trust the 1.
		// issue: #408.
		ma.sourceStart = 1;
		setGeneratedBy(ma, source);
		return ma;
	}
	
	public static MarkerAnnotation generateNamedAnnotation(ASTNode source, String typeName) {
		char[][] cc = fromQualifiedName(typeName);
		QualifiedTypeReference qtr = new QualifiedTypeReference(cc, poss(source, cc.length));
		setGeneratedBy(qtr, source);
		MarkerAnnotation ma = new MarkerAnnotation(qtr, source.sourceStart);
		// No matter what value you input for sourceEnd, the AST->DOM converter of eclipse will reparse to find the end, and will fail as
		// it can't find code that isn't really there. This results in the end position being set to 2 or 0 or some weird magic value, and thus,
		// length, as calculated by end-start, is all screwed up, resulting in IllegalArgumentException during a setSourceRange call MUCH later in the process.
		// We solve it by going with a voodoo magic source start value such that the calculated length so happens to exactly be 0. 0 lengths are accepted
		// by eclipse. For some reason.
		// TL;DR: Don't change 1. 1 is sacred. Trust the 1.
		// issue: #408.
		ma.sourceStart = 1;
		setGeneratedBy(ma, source);
		return ma;
	}
	
	public static boolean isFieldDeprecated(EclipseNode fieldNode) {
		if (!(fieldNode.get() instanceof FieldDeclaration)) return false;
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		if ((field.modifiers & ClassFileConstants.AccDeprecated) != 0) {
			return true;
		}
		if (field.annotations == null) return false;
		for (Annotation annotation : field.annotations) {
			if (typeMatches(Deprecated.class, fieldNode, annotation.type)) {
				return true;
			}
		}
		return false;
	}
	
	public static CheckerFrameworkVersion getCheckerFrameworkVersion(EclipseNode node) {
		CheckerFrameworkVersion cfv = node.getAst().readConfiguration(ConfigurationKeys.CHECKER_FRAMEWORK);
		return cfv != null ? cfv : CheckerFrameworkVersion.NONE;
	}
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeRef A type reference to check.
	 */
	public static boolean typeMatches(Class<?> type, EclipseNode node, TypeReference typeRef) {
		return typeMatches(type.getName(), node, typeRef);
	}
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeRef A type reference to check.
	 */
	public static boolean typeMatches(String type, EclipseNode node, TypeReference typeRef) {
		char[][] tn = typeRef == null ? null : typeRef.getTypeName();
		if (tn == null || tn.length == 0) return false;
		char[] lastPartA = tn[tn.length - 1];
		int lastIndex = Math.max(type.lastIndexOf('.'), type.lastIndexOf('$')) + 1;
		if (lastPartA.length != type.length() - lastIndex) return false;
		for (int i = 0; i < lastPartA.length; i++) if (lastPartA[i] != type.charAt(i + lastIndex)) return false;
		String typeName = toQualifiedName(tn);
		TypeResolver resolver = node.getImportListAsTypeResolver();
		return resolver.typeMatches(node, type, typeName);
	}
	
	public static void sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(EclipseNode typeNode, EclipseNode errorNode) {
		List<String> disallowed = null;
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			for (String annType : INVALID_ON_BUILDERS) {
				if (annotationTypeMatches(annType, child)) {
					if (disallowed == null) disallowed = new ArrayList<String>();
					int lastIndex = annType.lastIndexOf('.');
					disallowed.add(lastIndex == -1 ? annType : annType.substring(lastIndex + 1));
				}
			}
		}
		
		int size = disallowed == null ? 0 : disallowed.size();
		if (size == 0) return;
		if (size == 1) {
			errorNode.addError("@" + disallowed.get(0) + " is not allowed on builder classes.");
			return;
		}
		StringBuilder out = new StringBuilder();
		for (String a : disallowed) out.append("@").append(a).append(", ");
		out.setLength(out.length() - 2);
		errorNode.addError(out.append(" are not allowed on builder classes.").toString());
	}
	
	public static Annotation copyAnnotation(Annotation annotation, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		if (annotation instanceof MarkerAnnotation) {
			MarkerAnnotation ann = new MarkerAnnotation(copyType(annotation.type, source), pS);
			setGeneratedBy(ann, source);
			ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = pE;
			copyMemberValuePairName(ann, annotation);
			return ann;
		}
		
		if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation ann = new SingleMemberAnnotation(copyType(annotation.type, source), pS);
			setGeneratedBy(ann, source);
			ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = pE;
			ann.memberValue = copyAnnotationMemberValue(((SingleMemberAnnotation) annotation).memberValue);
			copyMemberValuePairName(ann, annotation);
			return ann;
		}
		
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation ann = new NormalAnnotation(copyType(annotation.type, source), pS);
			setGeneratedBy(ann, source);
			ann.declarationSourceEnd = ann.statementEnd = ann.sourceEnd = pE;
			MemberValuePair[] inPairs = ((NormalAnnotation) annotation).memberValuePairs;
			if (inPairs == null) {
				ann.memberValuePairs = null;
			} else {
				ann.memberValuePairs = new MemberValuePair[inPairs.length];
				for (int i = 0; i < inPairs.length; i++) ann.memberValuePairs[i] =
						new MemberValuePair(inPairs[i].name, inPairs[i].sourceStart, inPairs[i].sourceEnd, copyAnnotationMemberValue(inPairs[i].value));
			}
			copyMemberValuePairName(ann, annotation);
			return ann;
		}
		
		return annotation;
	}
	
	private static void copyMemberValuePairName(Annotation source, Annotation target) {
		if (ANNOTATION__MEMBER_VALUE_PAIR_NAME == null) return;
		
		try {
			reflectSet(ANNOTATION__MEMBER_VALUE_PAIR_NAME, source, reflect(ANNOTATION__MEMBER_VALUE_PAIR_NAME, target));
		} catch (Exception ignore) { /* Various eclipse versions don't have it */ }
	}
	
	static class EclipseReflectiveMembers {
		public static final Field STRING_LITERAL__LINE_NUMBER;
		public static final Field ANNOTATION__MEMBER_VALUE_PAIR_NAME;
		public static final Field TYPE_REFERENCE__ANNOTATIONS;
		public static final Class<?> INTERSECTION_BINDING1, INTERSECTION_BINDING2;
		public static final Field INTERSECTION_BINDING_TYPES1, INTERSECTION_BINDING_TYPES2;
		public static final Field TYPE_DECLARATION_RECORD_COMPONENTS;
		public static final Class<?> COMPILATION_UNIT;
		public static final Method COMPILATION_UNIT_ORIGINAL_FROM_CLONE;
		static {
			STRING_LITERAL__LINE_NUMBER = getField(StringLiteral.class, "lineNumber");
			ANNOTATION__MEMBER_VALUE_PAIR_NAME = getField(Annotation.class, "memberValuePairName");
			TYPE_REFERENCE__ANNOTATIONS = getField(TypeReference.class, "annotations");
			INTERSECTION_BINDING1 = getClass("org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18");
			INTERSECTION_BINDING2 = getClass("org.eclipse.jdt.internal.compiler.lookup.IntersectionCastTypeBinding");
			INTERSECTION_BINDING_TYPES1 = INTERSECTION_BINDING1 == null ? null : getField(INTERSECTION_BINDING1, "intersectingTypes");
			INTERSECTION_BINDING_TYPES2 = INTERSECTION_BINDING2 == null ? null : getField(INTERSECTION_BINDING2, "intersectingTypes");
			TYPE_DECLARATION_RECORD_COMPONENTS = getField(TypeDeclaration.class, "recordComponents");
			COMPILATION_UNIT = getClass("org.eclipse.jdt.internal.core.CompilationUnit");
			COMPILATION_UNIT_ORIGINAL_FROM_CLONE = COMPILATION_UNIT == null ? null : Permit.permissiveGetMethod(COMPILATION_UNIT, "originalFromClone");
		}
		
		public static int reflectInt(Field f, Object o) {
			try {
				return ((Number) f.get(o)).intValue();
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		public static void reflectSet(Field f, Object o, Object v) {
			try {
				f.set(o, v);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		public static Object reflect(Field f, Object o) {
			try {
				return f.get(o);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		private static Class<?> getClass(String fqn) {
			try {
				return Class.forName(fqn);
			} catch (Throwable t) {
				return null;
			}
		}
		
		private static Field getField(Class<?> c, String fName) {
			try {
				return Permit.getField(c, fName);
			} catch (Throwable t) {
				return null;
			}
		}
	}
	
	public static Expression copyAnnotationMemberValue(Expression in) {
		Expression out = copyAnnotationMemberValue0(in);
		out.constant = in.constant;
		return out;
	}
	
	private static Expression copyAnnotationMemberValue0(Expression in) {
		int s = in.sourceStart, e = in.sourceEnd;
		
		// literals
		
		if (in instanceof FalseLiteral) return new FalseLiteral(s, e);
		if (in instanceof TrueLiteral) return new TrueLiteral(s, e);
		if (in instanceof NullLiteral) return new NullLiteral(s, e);
		
		if (in instanceof CharLiteral) return new CharLiteral(((Literal) in).source(), s, e);
		if (in instanceof DoubleLiteral) return new DoubleLiteral(((Literal) in).source(), s, e);
		if (in instanceof FloatLiteral) return new FloatLiteral(((Literal) in).source(), s, e);
		if (in instanceof IntLiteral) return IntLiteral.buildIntLiteral(((Literal) in).source(), s, e);
		if (in instanceof LongLiteral) return LongLiteral.buildLongLiteral(((Literal) in).source(), s, e);
		
		if (in instanceof StringLiteral) return new StringLiteral(((Literal) in).source(), s, e, reflectInt(STRING_LITERAL__LINE_NUMBER, in) + 1);
		
		// enums and field accesses (as long as those are references to compile time constant literals that's also acceptable)
		
		if (in instanceof SingleNameReference) {
			SingleNameReference snr = (SingleNameReference) in;
			return new SingleNameReference(snr.token, pos(in));
		}
		if (in instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) in;
			return new QualifiedNameReference(qnr.tokens, poss(in, qnr.tokens.length), s, e);
		}
		
		// class refs
		if (in instanceof ClassLiteralAccess) return new ClassLiteralAccess(e, copyType(((ClassLiteralAccess) in).type));
		
		// arrays
		if (in instanceof ArrayInitializer) {
			ArrayInitializer out = new ArrayInitializer();
			out.sourceStart = s;
			out.sourceEnd = e;
			out.bits = in.bits;
			out.implicitConversion = in.implicitConversion;
			out.statementEnd = e;
			Expression[] exprs = ((ArrayInitializer) in).expressions;
			if (exprs != null) {
				Expression[] copy = new Expression[exprs.length];
				for (int i = 0; i < exprs.length; i++) copy[i] = copyAnnotationMemberValue(exprs[i]);
				out.expressions = copy;
			}
			return out;
		}
		
		if (in instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) in;
			BinaryExpression out = new BinaryExpression(be);
			out.left = copyAnnotationMemberValue(be.left);
			out.right = copyAnnotationMemberValue(be.right);
			out.sourceStart = s;
			out.sourceEnd = e;
			out.statementEnd = e;
			return out;
		}
		
		return in;
	}
	
	/**
	 * You can't share TypeParameter objects or bad things happen; for example, one 'T' resolves differently
	 * from another 'T', even for the same T in a single class file. Unfortunately the TypeParameter type hierarchy
	 * is complicated and there's no clone method on TypeParameter itself. This method can clone them.
	 */
	public static TypeParameter[] copyTypeParams(TypeParameter[] params, ASTNode source) {
		if (params == null) return null;
		TypeParameter[] out = new TypeParameter[params.length];
		int idx = 0;
		for (TypeParameter param : params) {
			TypeParameter o = new TypeParameter();
			setGeneratedBy(o, source);
			o.annotations = param.annotations;
			o.bits = param.bits;
			o.modifiers = param.modifiers;
			o.name = param.name;
			o.type = copyType(param.type, source);
			o.sourceStart = param.sourceStart;
			o.sourceEnd = param.sourceEnd;
			o.declarationEnd = param.declarationEnd;
			o.declarationSourceStart = param.declarationSourceStart;
			o.declarationSourceEnd = param.declarationSourceEnd;
			if (param.bounds != null) {
				TypeReference[] b = new TypeReference[param.bounds.length];
				int idx2 = 0;
				for (TypeReference ref : param.bounds) b[idx2++] = copyType(ref, source);
				o.bounds = b;
			}
			out[idx++] = o;
		}
		return out;
	}
	
	public static Annotation[] getTypeUseAnnotations(TypeReference from) {
		Annotation[][] a;
		try {
			a = (Annotation[][]) reflect(TYPE_REFERENCE__ANNOTATIONS, from);
		} catch (Exception e) {
			return null;
		}
		if (a == null) return null;
		Annotation[] b = a[a.length - 1];
		return b.length == 0 ? null : b;
	}
	
	public static void removeTypeUseAnnotations(TypeReference from) {
		try {
			reflectSet(TYPE_REFERENCE__ANNOTATIONS, from, null);
		} catch (Exception ignore) {}
	}
	
	public static TypeReference namePlusTypeParamsToTypeReference(EclipseNode type, TypeParameter[] params, long p) {
		TypeDeclaration td = (TypeDeclaration) type.get();
		boolean instance = (td.modifiers & MODIFIERS_INDICATING_STATIC) == 0;
		return namePlusTypeParamsToTypeReference(type.up(), td.name, instance, params, p);
	}
	
	public static TypeReference namePlusTypeParamsToTypeReference(EclipseNode parentType, char[] typeName, boolean instance, TypeParameter[] params, long p) {
		if (params != null && params.length > 0) {
			TypeReference[] refs = new TypeReference[params.length];
			int idx = 0;
			for (TypeParameter param : params) {
				TypeReference typeRef = new SingleTypeReference(param.name, p);
				refs[idx++] = typeRef;
			}
			return generateParameterizedTypeReference(parentType, typeName, instance, refs, p);
		}
		
		return generateTypeReference(parentType, typeName, instance, p);
	}
	
	public static TypeReference[] copyTypes(TypeReference[] refs) {
		return copyTypes(refs, null);
	}
	
	/**
	 * Convenience method that creates a new array and copies each TypeReference in the source array via
	 * {@link #copyType(TypeReference, ASTNode)}.
	 */
	public static TypeReference[] copyTypes(TypeReference[] refs, ASTNode source) {
		if (refs == null) return null;
		TypeReference[] outs = new TypeReference[refs.length];
		int idx = 0;
		for (TypeReference ref : refs) {
			outs[idx++] = copyType(ref, source);
		}
		return outs;
	}
	
	public static TypeReference copyType(TypeReference ref) {
		return copyType(ref, null);
	}
	
	/**
	 * You can't share TypeReference objects or subtle errors start happening.
	 * Unfortunately the TypeReference type hierarchy is complicated and there's no clone
	 * method on TypeReference itself. This method can clone them.
	 */
	public static TypeReference copyType(TypeReference ref, ASTNode source) {
		if (ref instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference iRef = (ParameterizedQualifiedTypeReference) ref;
			TypeReference[][] args = null;
			if (iRef.typeArguments != null) {
				args = new TypeReference[iRef.typeArguments.length][];
				int idx = 0;
				for (TypeReference[] inRefArray : iRef.typeArguments) {
					if (inRefArray == null) args[idx++] = null;
					else {
						TypeReference[] outRefArray = new TypeReference[inRefArray.length];
						int idx2 = 0;
						for (TypeReference inRef : inRefArray) {
							outRefArray[idx2++] = copyType(inRef, source);
						}
						args[idx++] = outRefArray;
					}
				}
			}
			
			TypeReference typeRef = new ParameterizedQualifiedTypeReference(iRef.tokens, args, iRef.dimensions(), copy(iRef.sourcePositions));
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof ArrayQualifiedTypeReference) {
			ArrayQualifiedTypeReference iRef = (ArrayQualifiedTypeReference) ref;
			TypeReference typeRef = new ArrayQualifiedTypeReference(iRef.tokens, iRef.dimensions(), copy(iRef.sourcePositions));
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof QualifiedTypeReference) {
			QualifiedTypeReference iRef = (QualifiedTypeReference) ref;
			TypeReference typeRef = new QualifiedTypeReference(iRef.tokens, copy(iRef.sourcePositions));
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference iRef = (ParameterizedSingleTypeReference) ref;
			TypeReference[] args = null;
			if (iRef.typeArguments != null) {
				args = new TypeReference[iRef.typeArguments.length];
				int idx = 0;
				for (TypeReference inRef : iRef.typeArguments) {
					if (inRef == null) args[idx++] = null;
					else args[idx++] = copyType(inRef, source);
				}
			}
			
			TypeReference typeRef = new ParameterizedSingleTypeReference(iRef.token, args, iRef.dimensions(), (long) iRef.sourceStart << 32 | iRef.sourceEnd);
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof ArrayTypeReference) {
			ArrayTypeReference iRef = (ArrayTypeReference) ref;
			TypeReference typeRef = new ArrayTypeReference(iRef.token, iRef.dimensions(), (long) iRef.sourceStart << 32 | iRef.sourceEnd);
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof Wildcard) {
			Wildcard original = (Wildcard) ref;
			
			Wildcard wildcard = new Wildcard(original.kind);
			wildcard.sourceStart = original.sourceStart;
			wildcard.sourceEnd = original.sourceEnd;
			if (original.bound != null) wildcard.bound = copyType(original.bound, source);
			copyTypeAnns(ref, wildcard);
			if (source != null) setGeneratedBy(wildcard, source);
			return wildcard;
		}
		
		if (ref instanceof SingleTypeReference) {
			SingleTypeReference iRef = (SingleTypeReference) ref;
			TypeReference typeRef = new SingleTypeReference(iRef.token, (long) iRef.sourceStart << 32 | iRef.sourceEnd);
			copyTypeAnns(ref, typeRef);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		return ref;
	}
	
	private static void copyTypeAnns(TypeReference in, TypeReference out) {
		Annotation[][] a;
		try {
			a = (Annotation[][]) reflect(TYPE_REFERENCE__ANNOTATIONS, in);
		} catch (Exception e) {
			return;
		}
		
		if (a == null) {
			reflectSet(TYPE_REFERENCE__ANNOTATIONS, out, null);
			return;
		}
		
		Annotation[][] b = new Annotation[a.length][];
		for (int i = 0; i < a.length; i++) {
			if (a[i] != null) {
				b[i] = new Annotation[a[i].length];
				for (int j = 0 ; j < a[i].length; j++) {
					b[i][j] = copyAnnotation(a[i][j], a[i][j]);
				}
			}
		}
		
		reflectSet(TYPE_REFERENCE__ANNOTATIONS, out, b);
	}
	
	public static Annotation[] copyAnnotations(ASTNode source, Annotation[]... allAnnotations) {
		List<Annotation> result = null;
		for (Annotation[] annotations : allAnnotations) {
			if (annotations != null) {
				for (Annotation annotation : annotations) {
					if (result == null) result = new ArrayList<Annotation>();
					result.add(copyAnnotation(annotation, source));
				}
			}
		}
		
		return result == null ? null : result.toArray(new Annotation[0]);
	}
	
	public static boolean hasAnnotation(Class<? extends java.lang.annotation.Annotation> type, EclipseNode node) {
		if (node == null) return false;
		if (type == null) return false;
		switch (node.getKind()) {
		case ARGUMENT:
		case FIELD:
		case LOCAL:
		case TYPE:
		case METHOD:
			for (EclipseNode child : node.down()) {
				if (annotationTypeMatches(type, child)) return true;
			}
			// intentional fallthrough
		default:
			return false;
		}
	}
	
	public static boolean hasAnnotation(String type, EclipseNode node) {
		if (node == null) return false;
		if (type == null) return false;
		switch (node.getKind()) {
		case ARGUMENT:
		case FIELD:
		case LOCAL:
		case TYPE:
		case METHOD:
			for (EclipseNode child : node.down()) {
				if (annotationTypeMatches(type, child)) return true;
			}
			// intentional fallthrough
		default:
			return false;
		}
	}
	
	public static EclipseNode findInnerClass(EclipseNode parent, String name) {
		char[] c = name.toCharArray();
		for (EclipseNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			TypeDeclaration td = (TypeDeclaration) child.get();
			if (Arrays.equals(td.name, c)) return child;
		}
		return null;
	}
	
	public static EclipseNode findAnnotation(Class<? extends java.lang.annotation.Annotation> type, EclipseNode node) {
		if (node == null) return null;
		if (type == null) return null;
		switch (node.getKind()) {
		case ARGUMENT:
		case FIELD:
		case LOCAL:
		case TYPE:
		case METHOD:
			for (EclipseNode child : node.down()) {
				if (annotationTypeMatches(type, child)) return child;
			}
			// intentional fallthrough
		default:
			return null;
		}
	}
	
	public static String scanForNearestAnnotation(EclipseNode node, String... anns) {
		while (node != null) {
			for (EclipseNode ann : node.down()) {
				if (ann.getKind() != Kind.ANNOTATION) continue;
				Annotation a = (Annotation) ann.get();
				TypeReference aType = a.type;
				for (String annToFind : anns) if (typeMatches(annToFind, node, aType)) return annToFind;
			}
			node = node.up();
		}
		
		return null;
	}
	
	public static boolean hasNonNullAnnotations(EclipseNode node) {
		for (EclipseNode child : node.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			Annotation annotation = (Annotation) child.get();
			for (String bn : NONNULL_ANNOTATIONS) if (typeMatches(bn, node, annotation.type)) return true;
		}
		return false;
	}
	
	public static boolean hasNonNullAnnotations(EclipseNode node, List<Annotation> anns) {
		if (anns == null) return false;
		for (Annotation annotation : anns) {
			TypeReference typeRef = annotation.type;
			if (typeRef != null && typeRef.getTypeName() != null) {
				for (String bn : NONNULL_ANNOTATIONS) if (typeMatches(bn, node, typeRef)) return true;
			}
		}
		return false;
	}
	
	private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];
	
	/**
	 * Searches the given field node for annotations and returns each one that is 'copyable' (either via configuration or from the base list).
	 */
	public static Annotation[] findCopyableAnnotations(EclipseNode node) {
		List<Annotation> result = new ArrayList<Annotation>();
		List<TypeName> configuredCopyable = node.getAst().readConfiguration(ConfigurationKeys.COPYABLE_ANNOTATIONS);
		for (EclipseNode child : node.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			
			Annotation annotation = (Annotation) child.get();
			TypeReference typeRef = annotation.type;
			boolean match = false;
			if (typeRef != null && typeRef.getTypeName() != null) {
				for (TypeName cn : configuredCopyable) if (cn != null && typeMatches(cn.toString(), node, typeRef)) {
					result.add(annotation);
					match = true;
					break;
				}
				if (!match) for (String bn : BASE_COPYABLE_ANNOTATIONS) if (typeMatches(bn, node, typeRef)) {
					result.add(annotation);
					break;
				}
			}
		}
		return result.toArray(EMPTY_ANNOTATIONS_ARRAY);
	}
	
	/**
	 * Searches the given field node for annotations that are specifically intentioned to be copied to the setter.
	 */
	public static Annotation[] findCopyableToSetterAnnotations(EclipseNode node) {
		return findAnnotationsInList(node, COPY_TO_SETTER_ANNOTATIONS);
	}

	/**
	 * Searches the given field node for annotations that are specifically intentioned to be copied to the builder's singular method.
	 */
	public static Annotation[] findCopyableToBuilderSingularSetterAnnotations(EclipseNode node) {
		return findAnnotationsInList(node, COPY_TO_BUILDER_SINGULAR_SETTER_ANNOTATIONS);
	}
	
	/**
	 * Searches the given field node for annotations that are in the given list, and returns those.
	 */
	private static Annotation[] findAnnotationsInList(EclipseNode node, java.util.List<String> annotationsToFind) {
		List<Annotation> result = new ArrayList<Annotation>();
		for (EclipseNode child : node.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			
			Annotation annotation = (Annotation) child.get();
			TypeReference typeRef = annotation.type;
			if (typeRef != null && typeRef.getTypeName() != null) {
				for (String bn : annotationsToFind) if (typeMatches(bn, node, typeRef)) {
					result.add(annotation);
					break;
				}
			}
		}
		return result.toArray(EMPTY_ANNOTATIONS_ARRAY);
	}
	
	/**
	 * Checks if the provided annotation type is likely to be the intended type for the given annotation node.
	 * 
	 * This is a guess, but a decent one.
	 */
	public static boolean annotationTypeMatches(Class<? extends java.lang.annotation.Annotation> type, EclipseNode node) {
		if (node.getKind() != Kind.ANNOTATION) return false;
		return typeMatches(type, node, ((Annotation) node.get()).type);
	}
	
	/**
	 * Checks if the provided annotation type is likely to be the intended type for the given annotation node.
	 * 
	 * This is a guess, but a decent one.
	 */
	public static boolean annotationTypeMatches(String type, EclipseNode node) {
		if (node.getKind() != Kind.ANNOTATION) return false;
		return typeMatches(type, node, ((Annotation) node.get()).type);
	}
	
	public static TypeReference cloneSelfType(EclipseNode context) {
		return cloneSelfType(context, null);
	}
	
	public static TypeReference cloneSelfType(EclipseNode context, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		long p = (long) pS << 32 | pE;
		EclipseNode type = context;
		TypeReference result = null;
		while (type != null && type.getKind() != Kind.TYPE) type = type.up();
		if (type != null && type.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) type.get();
			if (typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0) {
				TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
				int idx = 0;
				for (TypeParameter param : typeDecl.typeParameters) {
					TypeReference typeRef = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
					if (source != null) setGeneratedBy(typeRef, source);
					refs[idx++] = typeRef;
				}
				result = generateParameterizedTypeReference(type, refs, p);
			} else {
				result = generateTypeReference(type, p);
			}
		}
		if (result != null && source != null) setGeneratedBy(result, source);
		return result;
	}
	
	public static TypeReference generateParameterizedTypeReference(EclipseNode type, TypeReference[] typeParams, long p) {
		TypeDeclaration td = (TypeDeclaration) type.get();
		char[][] tn = getQualifiedInnerName(type.up(), td.name);
		if (tn.length == 1) return new ParameterizedSingleTypeReference(tn[0], typeParams, 0, p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		TypeReference[][] rr = new TypeReference[tnLen][];
		rr[tnLen - 1] = typeParams;
		boolean instance = (td.modifiers & MODIFIERS_INDICATING_STATIC) == 0;
		if (instance) fillOuterTypeParams(rr, tnLen - 2, type.up(), p);
		return new ParameterizedQualifiedTypeReference(tn, rr, 0, ps);
	}
	
	public static TypeReference generateParameterizedTypeReference(EclipseNode parent, char[] name, boolean instance, TypeReference[] typeParams, long p) {
		char[][] tn = getQualifiedInnerName(parent, name);
		if (tn.length == 1) return new ParameterizedSingleTypeReference(tn[0], typeParams, 0, p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		TypeReference[][] rr = new TypeReference[tnLen][];
		rr[tnLen - 1] = typeParams;
		if (instance) fillOuterTypeParams(rr, tnLen - 2, parent, p);
		return new ParameterizedQualifiedTypeReference(tn, rr, 0, ps);
	}
	
	private static final int MODIFIERS_INDICATING_STATIC = ClassFileConstants.AccInterface | ClassFileConstants.AccStatic | ClassFileConstants.AccEnum | Eclipse.AccRecord;
	
	/**
	 * This class will add type params to fully qualified chain of type references for inner types, such as {@code GrandParent.Parent.Child}; this is needed only as long as the chain does not involve static.
	 * 
	 * @return {@code true} if at least one parameterization is actually added, {@code false} otherwise.
	 */
	private static boolean fillOuterTypeParams(TypeReference[][] rr, int idx, EclipseNode node, long p) {
		if (idx < 0 || node == null || !(node.get() instanceof TypeDeclaration)) return false;
		boolean filled = false;
		TypeDeclaration td = (TypeDeclaration) node.get();
		if (0 != (td.modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccEnum))) {
			// any class defs inside an enum or interface are static, even if not marked as such.
			return false;
		}
		TypeParameter[] tps = td.typeParameters;
		if (tps != null && tps.length > 0) {
			TypeReference[] trs = new TypeReference[tps.length];
			for (int i = 0; i < tps.length; i++) {
				trs[i] = new SingleTypeReference(tps[i].name, p);
			}
			rr[idx] = trs;
			filled = true;
		}
		
		if ((td.modifiers & MODIFIERS_INDICATING_STATIC) != 0) return filled; // Once we hit a static class, no further typeparams needed.
		boolean f2 = fillOuterTypeParams(rr, idx - 1, node.up(), p);
		return f2 || filled;
	}
	
	public static NameReference generateNameReference(EclipseNode type, long p) {
		char[][] tn = getQualifiedInnerName(type.up(), ((TypeDeclaration) type.get()).name);
		if (tn.length == 1) return new SingleNameReference(tn[0], p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		int ss = (int) (p >> 32);
		int se = (int) p;
		return new QualifiedNameReference(tn, ps, ss, se);
	}
	
	public static NameReference generateNameReference(EclipseNode parent, char[] name, long p) {
		char[][] tn = getQualifiedInnerName(parent, name);
		if (tn.length == 1) return new SingleNameReference(tn[0], p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		int ss = (int) (p >> 32);
		int se = (int) p;
		return new QualifiedNameReference(tn, ps, ss, se);
	}
	
	public static TypeReference generateTypeReference(EclipseNode type, long p) {
		TypeDeclaration td = (TypeDeclaration) type.get();
		char[][] tn = getQualifiedInnerName(type.up(), td.name);
		if (tn.length == 1) return new SingleTypeReference(tn[0], p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		
		
		boolean instance = (td.modifiers & MODIFIERS_INDICATING_STATIC) == 0 && type.up() != null && type.up().get() instanceof TypeDeclaration;
		if (instance) {
			TypeReference[][] trs = new TypeReference[tn.length][];
			boolean filled = fillOuterTypeParams(trs, trs.length - 2, type.up(), p);
			if (filled) return new ParameterizedQualifiedTypeReference(tn, trs, 0, ps);
		}
		
		return new QualifiedTypeReference(tn, ps);
	}
	
	public static TypeReference generateTypeReference(EclipseNode parent, char[] name, boolean instance, long p) {
		char[][] tn = getQualifiedInnerName(parent, name);
		if (tn.length == 1) return new SingleTypeReference(tn[0], p);
		int tnLen = tn.length;
		long[] ps = new long[tnLen];
		for (int i = 0; i < tnLen; i++) ps[i] = p;
		
		if (instance && parent != null && parent.get() instanceof TypeDeclaration) {
			TypeReference[][] trs = new TypeReference[tn.length][];
			if (fillOuterTypeParams(trs, tn.length - 2, parent, p)) return new ParameterizedQualifiedTypeReference(tn, trs, 0, ps);
		}
		
		return new QualifiedTypeReference(tn, ps);
	}
	
	/**
	 * Generate a chain of names for the enclosing classes.
	 * 
	 * Given for example {@code class Outer { class Inner {} }} this would generate {@code char[][] { "Outer", "Inner" }}.
	 * For method local and top level types, this generates a size-1 char[][] where the only char[] element is {@code name} itself.
	 */
	public static char[][] getQualifiedInnerName(EclipseNode parent, char[] name) {
		int count = 0;
		
		EclipseNode n = parent;
		while (n != null && n.getKind() == Kind.TYPE && n.get() instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) n.get();
			if (td.name == null || td.name.length == 0) break;
			count++;
			n = n.up();
		}
		
		if (count == 0) return new char[][] { name };
		char[][] res = new char[count + 1][];
		res[count] = name;
		
		n = parent;
		while (count > 0) {
			TypeDeclaration td = (TypeDeclaration) n.get();
			res[--count] = td.name;
			n = n.up();
		}
		
		return res;
	}
	
	private static final char[] OBJECT_SIG = "Ljava/lang/Object;".toCharArray();
	
	private static int compare(char[] a, char[] b) {
		if (a == null) return b == null ? 0 : -1;
		if (b == null) return +1;
		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; i++) {
			if (a[i] < b[i]) return -1;
			if (a[i] > b[i]) return +1;
		}
		return a.length < b.length ? -1 : a.length > b.length ? +1 : 0;
	}
	
	public static TypeReference makeType(TypeBinding binding, ASTNode pos, boolean allowCompound) {
		Object[] arr = null;
		if (binding.getClass() == EclipseReflectiveMembers.INTERSECTION_BINDING1) {
			arr = (Object[]) EclipseReflectiveMembers.reflect(EclipseReflectiveMembers.INTERSECTION_BINDING_TYPES1, binding);
		} else if (binding.getClass() == EclipseReflectiveMembers.INTERSECTION_BINDING2) {
			arr = (Object[]) EclipseReflectiveMembers.reflect(EclipseReflectiveMembers.INTERSECTION_BINDING_TYPES2, binding);
		}
		
		if (arr != null) {
			// Is there a class? Alphabetically lowest wins.
			TypeBinding winner = null;
			int winLevel = 0; // 100 = array, 50 = class, 20 = typevar, 15 = wildcard, 10 = interface, 1 = Object.
			for (Object b : arr) {
				if (b instanceof TypeBinding) {
					TypeBinding tb = (TypeBinding) b;
					int level = 0;
					if (tb.isArrayType()) level = 100;
					else if (tb.isClass()) level = 50;
					else if (tb.isTypeVariable()) level = 20;
					else if (tb.isWildcard()) level = 15;
					else level = 10;
					
					if (level == 50 && compare(tb.signature(), OBJECT_SIG) == 0) level = 1;
					
					if (winLevel > level) continue;
					if (winLevel < level) {
						winner = tb;
						winLevel = level;
						continue;
					}
					if (compare(winner.signature(), tb.signature()) > 0) winner = tb;
				}
			}
			binding = winner;
		}
		int dims = binding.dimensions();
		binding = binding.leafComponentType();
		
		// Primitives
		
		char[] base = null;
		
		switch (binding.id) {
		case TypeIds.T_int:
			base = TypeConstants.INT;
			break;
		case TypeIds.T_long:
			base = TypeConstants.LONG;
			break;
		case TypeIds.T_short:
			base = TypeConstants.SHORT;
			break;
		case TypeIds.T_byte:
			base = TypeConstants.BYTE;
			break;
		case TypeIds.T_double:
			base = TypeConstants.DOUBLE;
			break;
		case TypeIds.T_float:
			base = TypeConstants.FLOAT;
			break;
		case TypeIds.T_boolean:
			base = TypeConstants.BOOLEAN;
			break;
		case TypeIds.T_char:
			base = TypeConstants.CHAR;
			break;
		case TypeIds.T_void:
			base = TypeConstants.VOID;
			break;
		case TypeIds.T_null:
			return null;
		}
		
		if (base != null) {
			if (dims > 0) {
				TypeReference result = new ArrayTypeReference(base, dims, pos(pos));
				setGeneratedBy(result, pos);
				return result;
			}
			TypeReference result = new SingleTypeReference(base, pos(pos));
			setGeneratedBy(result, pos);
			return result;
		}
		
		if (binding.isAnonymousType()) {
			ReferenceBinding ref = (ReferenceBinding)binding;
			ReferenceBinding[] supers = ref.superInterfaces();
			if (supers == null || supers.length == 0) supers = new ReferenceBinding[] {ref.superclass()};
			if (supers[0] == null) {
				TypeReference result = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(pos, 3));
				setGeneratedBy(result, pos);
				return result;
			}
			return makeType(supers[0], pos, false);
		}
		
		if (binding instanceof CaptureBinding) {
			return makeType(((CaptureBinding)binding).wildcard, pos, allowCompound);
		}
		
		if (binding.isUnboundWildcard()) {
			if (!allowCompound) {
				TypeReference result = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(pos, 3));
				setGeneratedBy(result, pos);
				return result;
			} else {
				Wildcard out = new Wildcard(Wildcard.UNBOUND);
				setGeneratedBy(out, pos);
				out.sourceStart = pos.sourceStart;
				out.sourceEnd = pos.sourceEnd;
				return out;
			}
		}
		
		if (binding.isWildcard()) {
			WildcardBinding wildcard = (WildcardBinding) binding;
			if (wildcard.boundKind == Wildcard.EXTENDS) {
				if (!allowCompound) {
					TypeBinding bound = wildcard.bound;
					boolean isObject = bound.id == TypeIds.T_JavaLangObject;
					TypeBinding[] otherBounds = wildcard.otherBounds;
					if (isObject && otherBounds != null && otherBounds.length > 0) {
						return makeType(otherBounds[0], pos, false);
					} else return makeType(bound, pos, false);
				} else {
					Wildcard out = new Wildcard(Wildcard.EXTENDS);
					setGeneratedBy(out, pos);
					out.bound = makeType(wildcard.bound, pos, false);
					out.sourceStart = pos.sourceStart;
					out.sourceEnd = pos.sourceEnd;
					return out;
				}
			} else if (allowCompound && wildcard.boundKind == Wildcard.SUPER) {
				Wildcard out = new Wildcard(Wildcard.SUPER);
				setGeneratedBy(out, pos);
				out.bound = makeType(wildcard.bound, pos, false);
				out.sourceStart = pos.sourceStart;
				out.sourceEnd = pos.sourceEnd;
				return out;
			} else {
				TypeReference result = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(pos, 3));
				setGeneratedBy(result, pos);
				return result;
			}
		}
		
		// Keep moving up via 'binding.enclosingType()' and gather generics from each binding. We stop after a local type, or a static type, or a top-level type.
		// Finally, add however many nullTypeArgument[] arrays as that are missing, inverse the list, toArray it, and use that as PTR's typeArgument argument.
		
		List<TypeReference[]> params = new ArrayList<TypeReference[]>();
		/* Calculate generics */
		if (!(binding instanceof RawTypeBinding)) {
			TypeBinding b = binding;
			while (true) {
				boolean isFinalStop = b.isLocalType() || !b.isMemberType() || b.enclosingType() == null;
				
				TypeReference[] tyParams = null;
				if (b instanceof ParameterizedTypeBinding) {
					ParameterizedTypeBinding paramized = (ParameterizedTypeBinding) b;
					if (paramized.arguments != null) {
						tyParams = new TypeReference[paramized.arguments.length];
						for (int i = 0; i < tyParams.length; i++) {
							tyParams[i] = makeType(paramized.arguments[i], pos, true);
						}
					}
				}
				
				params.add(tyParams);
				if (isFinalStop) break;
				b = b.enclosingType();
			}
		}
		
		char[][] parts;
		
		if (binding.isTypeVariable()) {
			parts = new char[][] { binding.shortReadableName() };
		} else if (binding.isLocalType()) {
			parts = new char[][] { binding.sourceName() };
		} else {
			String[] pkg = new String(binding.qualifiedPackageName()).split("\\.");
			String[] name = new String(binding.qualifiedSourceName()).split("\\.");
			if (pkg.length == 1 && pkg[0].isEmpty()) pkg = new String[0];
			parts = new char[pkg.length + name.length][];
			int ptr;
			for (ptr = 0; ptr < pkg.length; ptr++) parts[ptr] = pkg[ptr].toCharArray();
			for (; ptr < pkg.length + name.length; ptr++) parts[ptr] = name[ptr - pkg.length].toCharArray();
		}
		
		while (params.size() < parts.length) params.add(null);
		Collections.reverse(params);
		
		boolean isParamized = false;
		
		for (TypeReference[] tyParams : params) {
			if (tyParams != null) {
				isParamized = true;
				break;
			}
		}
		if (isParamized) {
			if (parts.length > 1) {
				TypeReference[][] typeArguments = params.toArray(new TypeReference[0][]);
				TypeReference result = new ParameterizedQualifiedTypeReference(parts, typeArguments, dims, poss(pos, parts.length));
				setGeneratedBy(result, pos);
				return result;
			}
			TypeReference result = new ParameterizedSingleTypeReference(parts[0], params.get(0), dims, pos(pos));
			setGeneratedBy(result, pos);
			return result;
		}
		
		if (dims > 0) {
			if (parts.length > 1) {
				TypeReference result = new ArrayQualifiedTypeReference(parts, dims, poss(pos, parts.length));
				setGeneratedBy(result, pos);
				return result;
			}
			TypeReference result = new ArrayTypeReference(parts[0], dims, pos(pos));
			setGeneratedBy(result, pos);
			return result;
		}
		
		if (parts.length > 1) {
			TypeReference result = new QualifiedTypeReference(parts, poss(pos, parts.length));
			setGeneratedBy(result, pos);
			return result;
		}
		TypeReference result = new SingleTypeReference(parts[0], pos(pos));
		setGeneratedBy(result, pos);
		return result;
	}
	
	/**
	 * Provides AnnotationValues with the data it needs to do its thing.
	 */
	public static <A extends java.lang.annotation.Annotation> AnnotationValues<A>
		createAnnotation(Class<A> type, final EclipseNode annotationNode) {
		
		final Annotation annotation = (Annotation) annotationNode.get();
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
		
		MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
		
		if (memberValuePairs != null) for (final MemberValuePair pair : memberValuePairs) {
			List<String> raws = new ArrayList<String>();
			List<Object> expressionValues = new ArrayList<Object>();
			List<Object> guesses = new ArrayList<Object>();
			Expression[] expressions = null;
			
			char[] n = pair.name;
			String mName = (n == null || n.length == 0) ? "value" : new String(pair.name);
			final Expression rhs = pair.value;
			if (rhs instanceof ArrayInitializer) {
				expressions = ((ArrayInitializer) rhs).expressions;
			} else if (rhs != null) {
				expressions = new Expression[] { rhs };
			}
			if (expressions != null) for (Expression ex : expressions) {
				raws.add(ex.toString()); 
				expressionValues.add(ex);
				guesses.add(calculateValue(ex));
			}
			
			final Expression[] exprs = expressions;
			values.put(mName, new AnnotationValue(annotationNode, raws, expressionValues, guesses, true) {
				@Override public void setError(String message, int valueIdx) {
					Expression ex;
					if (valueIdx == -1) ex = rhs;
					else ex = exprs != null ? exprs[valueIdx] : null;
					
					if (ex == null) ex = annotation;
					
					int sourceStart = ex.sourceStart;
					int sourceEnd = ex.sourceEnd;
					
					annotationNode.addError(message, sourceStart, sourceEnd);
				}
				
				@Override public void setWarning(String message, int valueIdx) {
					Expression ex;
					if (valueIdx == -1) ex = rhs;
					else ex = exprs != null ? exprs[valueIdx] : null;
					
					if (ex == null) ex = annotation;
					
					int sourceStart = ex.sourceStart;
					int sourceEnd = ex.sourceEnd;
					
					annotationNode.addWarning(message, sourceStart, sourceEnd);
				}
			});
		}
		
		for (Method m : type.getDeclaredMethods()) {
			if (!Modifier.isPublic(m.getModifiers())) continue;
			String name = m.getName();
			if (!values.containsKey(name)) {
				values.put(name, new AnnotationValue(annotationNode, new ArrayList<String>(), new ArrayList<Object>(), new ArrayList<Object>(), false) {
					@Override public void setError(String message, int valueIdx) {
						annotationNode.addError(message);
					}
					@Override public void setWarning(String message, int valueIdx) {
						annotationNode.addWarning(message);
					}
				});
			}
		}
		
		return new AnnotationValues<A>(type, values, annotationNode);
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
	
	static void registerCreatedLazyGetter(FieldDeclaration field, char[] methodName, TypeReference returnType) {
		if (isBoolean(returnType)) {
			FieldDeclaration_booleanLazyGetter.set(field, true);
		}
	}
	
	public static boolean isBoolean(TypeReference typeReference) {
		return nameEquals(typeReference.getTypeName(), "boolean") && typeReference.dimensions() == 0;
	}
	
	private static GetterMethod findGetter(EclipseNode field) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) field.get();
		boolean forceBool = FieldDeclaration_booleanLazyGetter.get(fieldDeclaration);
		TypeReference fieldType = fieldDeclaration.type;
		boolean isBoolean = forceBool || isBoolean(fieldType);
		
		EclipseNode typeNode = field.up();
		for (String potentialGetterName : toAllGetterNames(field, isBoolean)) {
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
				AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
				if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
				hasGetterAnnotation = true;
			}
		}
		
		// Check if the class has a @Getter annotation.
		
		if (!hasGetterAnnotation && HandleGetter.fieldQualifiesForGetterGeneration(field)) {
			//Check if the class has @Getter or @Data annotation.
			
			EclipseNode containingType = field.up();
			if (containingType != null) for (EclipseNode child : containingType.down()) {
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Data.class, child)) hasGetterAnnotation = true;
				if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Getter.class, child)) {
					AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
					if (ann.getInstance().value() == AccessLevel.NONE) return null;   //Definitely WONT have a getter.
					hasGetterAnnotation = true;
				}
			}
		}
		
		if (hasGetterAnnotation) {
			String getterName = toGetterName(field, isBoolean);
			if (getterName == null) return null;
			return new GetterMethod(getterName.toCharArray(), fieldType);
		}
		
		return null;
	}
	
	static boolean lookForGetter(EclipseNode field, FieldAccess fieldAccess) {
		if (fieldAccess == FieldAccess.GETTER) return true;
		if (fieldAccess == FieldAccess.ALWAYS_FIELD) return false;
		
		// If @Getter(lazy = true) is used, then using it is mandatory.
		for (EclipseNode child : field.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			if (annotationTypeMatches(Getter.class, child)) {
				AnnotationValues<Getter> ann = createAnnotation(Getter.class, child);
				if (ann.getInstance().lazy()) return true;
			}
		}
		return false;
	}
	
	static TypeReference getFieldType(EclipseNode field, FieldAccess fieldAccess) {
		if (field.get() instanceof MethodDeclaration) return ((MethodDeclaration) field.get()).returnType;
		
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		if (getter == null) {
			return ((FieldDeclaration) field.get()).type;
		}
		
		return getter.type;
	}
	
	static Expression createFieldAccessor(EclipseNode field, FieldAccess fieldAccess, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
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
					if (source != null) setGeneratedBy(smallRef, source);
					return smallRef;
				}
			} else {
				ref.receiver = new ThisReference(pS, pE);
			}
			
			if (source != null) {
				setGeneratedBy(ref, source);
				setGeneratedBy(ref.receiver, source);
			}
			return ref;
		}
		
		MessageSend call = new MessageSend();
		setGeneratedBy(call, source);
		call.sourceStart = pS; call.statementEnd = call.sourceEnd = pE;
		call.receiver = new ThisReference(pS, pE);
		setGeneratedBy(call.receiver, source);
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
			setGeneratedBy(ref, source);
			return ref;
		}
		
		MessageSend call = new MessageSend();
		setGeneratedBy(call, source);
		call.sourceStart = pS; call.statementEnd = call.sourceEnd = pE;
		call.receiver = new SingleNameReference(receiver, p);
		setGeneratedBy(call.receiver, source);
		call.selector = getter.name;
		return call;
	}
	
	static Expression createMethodAccessor(EclipseNode method, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration methodDecl = (MethodDeclaration) method.get();
		MessageSend call = new MessageSend();
		setGeneratedBy(call, source);
		call.sourceStart = pS; call.statementEnd = call.sourceEnd = pE;
		if ((methodDecl.modifiers & ClassFileConstants.AccStatic) == 0) {
			call.receiver = new ThisReference(pS, pE);
			setGeneratedBy(call.receiver, source);
		} else {
			EclipseNode containerNode = method.up();
			if (containerNode != null && containerNode.get() instanceof TypeDeclaration) {
				call.receiver = new SingleNameReference(((TypeDeclaration) containerNode.get()).name, p);
				setGeneratedBy(call.receiver, source);
			}
		}
		
		call.selector = methodDecl.selector;
		return call;
	}
	
	static Expression createMethodAccessor(EclipseNode method, ASTNode source, char[] receiver) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		MethodDeclaration methodDecl = (MethodDeclaration) method.get();
		MessageSend call = new MessageSend();
		setGeneratedBy(call, source);
		call.sourceStart = pS; call.statementEnd = call.sourceEnd = pE;
		call.receiver = new SingleNameReference(receiver, p);
		setGeneratedBy(call.receiver, source);
		call.selector = methodDecl.selector;
		return call;
	}
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_LOMBOK, EXISTS_BY_USER;
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static List<String> toAllGetterNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllGetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static List<String> toAllGetterNames(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllGetterNames(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toGetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toGetterName(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllSetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link HandlerUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllSetterNames(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toSetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toSetterName(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible with names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllWithNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible with names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithNames(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllWithNames(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible withBy names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithByNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithByNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllWithByNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible withBy names.
	 * Convenient wrapper around {@link HandlerUtil#toAllWithByNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWithByNames(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toAllWithByNames(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely with name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toWithName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely with name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithName(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toWithName(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely withBy name for the stated field. (e.g. private boolean foo; to withFooBy).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithByName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithByName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toWithByName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely withBy name for the stated field. (e.g. private boolean foo; to withFooBy).
	 * 
	 * Convenient wrapper around {@link HandlerUtil#toWithByName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWithByName(EclipseNode field, boolean isBoolean, AnnotationValues<Accessors> accessors) {
		return HandlerUtil.toWithByName(field.getAst(), accessors, field.getName(), isBoolean);
	}
	
	/**
	 * When generating a setter/getter/wither, should it be made final?
	 */
	public static boolean shouldMakeFinal(EclipseNode field, AnnotationValues<Accessors> accessors) {
		if ((((FieldDeclaration) field.get()).modifiers & ClassFileConstants.AccStatic) != 0) return false;
		return shouldMakeFinal0(accessors, field.getAst());
	}
	/**
	 * When generating a setter, the setter either returns void (beanspec) or Self (fluent).
	 * This method scans for the {@code Accessors} annotation and associated config properties to figure that out.
	 */
	public static boolean shouldReturnThis(EclipseNode field, AnnotationValues<Accessors> accessors) {
		if ((((FieldDeclaration) field.get()).modifiers & ClassFileConstants.AccStatic) != 0) return false;
		return shouldReturnThis0(accessors, field.getAst());
	}
	
	/**
	 * Checks if the field should be included in operations that work on 'all' fields:
	 *    If the field is static, or starts with a '$', or is actually an enum constant, 'false' is returned, indicating you should skip it.
	 */
	public static boolean filterField(FieldDeclaration declaration) {
		return filterField(declaration, true);
	}
	
	public static boolean filterField(FieldDeclaration declaration, boolean skipStatic) {
		// Skip the fake fields that represent enum constants.
		if (declaration.initialization instanceof AllocationExpression &&
				((AllocationExpression) declaration.initialization).enumConstant != null) return false;
		
		if (declaration.type == null) return false;
		
		// Skip fields that start with $
		if (declaration.name.length > 0 && declaration.name[0] == '$') return false;
		
		// Skip static fields.
		if (skipStatic && (declaration.modifiers & ClassFileConstants.AccStatic) != 0) return false;
		
		return true;
	}
	
	public static char[] removePrefixFromField(EclipseNode field) {
		List<String> prefixes = null;
		for (EclipseNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				AnnotationValues<Accessors> ann = createAnnotation(Accessors.class, node);
				if (ann.isExplicit("prefix")) prefixes = Arrays.asList(ann.getInstance().prefix());
				break;
			}
		}
		
		if (prefixes == null) {
			EclipseNode current = field.up();
			outer:
			while (current != null) {
				for (EclipseNode node : current.down()) {
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
			if (newName != null) return newName.toString().toCharArray();
		}
		
		return ((FieldDeclaration) field.get()).name;
	}
	
	public static AnnotationValues<Accessors> getAccessorsForField(EclipseNode field) {
		AnnotationValues<Accessors> values = null;
		
		for (EclipseNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				values = createAnnotation(Accessors.class, node);
				break;
			}
		}
		
		EclipseNode current = field.up();
		while (current != null) {
			for (EclipseNode node : current.down()) {
				if (annotationTypeMatches(Accessors.class, node)) {
					AnnotationValues<Accessors> onType = createAnnotation(Accessors.class, node);
					values = values == null ? onType : values.integrate(onType);
				}
			}
			current = current.up();
		}
		
		return values == null ? AnnotationValues.of(Accessors.class, field) : values;
	}
	
	public static EclipseNode upToTypeNode(EclipseNode node) {
		if (node == null) throw new NullPointerException("node");
		while (node != null && !(node.get() instanceof TypeDeclaration)) node = node.up();
		return node;
	}
	
	/**
	 * Checks if there is a field with the provided name.
	 * 
	 * @param fieldName the field name to check for.
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static MemberExistsResult fieldExists(String fieldName, EclipseNode node) {
		node = upToTypeNode(node);
		char[] fieldNameChars = null;
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) node.get();
			if (typeDecl.fields != null) for (FieldDeclaration def : typeDecl.fields) {
				char[] fName = def.name;
				if (fName == null) continue;
				if (fieldNameChars == null) fieldNameChars = fieldName.toCharArray();
				if (Arrays.equals(fName, fieldNameChars)) {
					return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	/**
	 * Wrapper for {@link #methodExists(String, EclipseNode, boolean, int)} with {@code caseSensitive} = {@code true}.
	 */
	public static MemberExistsResult methodExists(String methodName, EclipseNode node, int params) {
		return methodExists(methodName, node, true, params);
	}
	
	/**
	 * Checks if there is a method with the provided name. In case of multiple methods (overloading), only
	 * the first method decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param methodName the method name to check for.
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 * @param caseSensitive If the search should be case sensitive.
	 * @param params The number of parameters the method should have; varargs count as 0-*. Set to -1 to find any method with the appropriate name regardless of parameter count.
	 */
	public static MemberExistsResult methodExists(String methodName, EclipseNode node, boolean caseSensitive, int params) {
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if (typeDecl.methods != null) top: for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (def instanceof MethodDeclaration) {
					char[] mName = def.selector;
					if (mName == null) continue;
					boolean nameEquals = caseSensitive ? methodName.equals(new String(mName)) : methodName.equalsIgnoreCase(new String(mName));
					if (nameEquals) {
						if (params > -1) {
							int minArgs = 0;
							int maxArgs = 0;
							if (def.arguments != null && def.arguments.length > 0) {
								minArgs = def.arguments.length;
								if ((def.arguments[def.arguments.length - 1].type.bits & ASTNode.IsVarArgs) != 0) {
									minArgs--;
									maxArgs = Integer.MAX_VALUE;
								} else {
									maxArgs = minArgs;
								}
							}
							
							if (params < minArgs || params > maxArgs) continue;
						}
						
						
						if (isTolerate(node, def)) continue top;
						
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	public static boolean isTolerate(EclipseNode node, AbstractMethodDeclaration def) {
		if (def.annotations != null) for (Annotation anno : def.annotations) {
			if (typeMatches(Tolerate.class, node, anno.type)) return true;
		}
		return false;
	}
	
	/**
	 * Checks if there is a (non-default) constructor. In case of multiple constructors (overloading), only
	 * the first constructor decides if EXISTS_BY_USER or EXISTS_BY_LOMBOK is returned.
	 * 
	 * @param node Any node that represents the Type (TypeDeclaration) to look in, or any child node thereof.
	 */
	public static MemberExistsResult constructorExists(EclipseNode node) {
		node = upToTypeNode(node);
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) node.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (!(def instanceof ConstructorDeclaration)) continue;
				if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
				if (isTolerate(node, def)) continue;
				return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	/**
	 * Inserts a field into an existing type. The type must represent a {@code TypeDeclaration}.
	 * The field carries the &#64;{@link SuppressWarnings}("all") annotation.
	 */
	public static EclipseNode injectFieldAndMarkGenerated(EclipseNode type, FieldDeclaration field) {
		field.annotations = addSuppressWarningsAll(type, field, field.annotations);
		field.annotations = addGenerated(type, field, field.annotations);
		return injectField(type, field);
	}
	
	/**
	 * Inserts a field into an existing type. The type must represent a {@code TypeDeclaration}.
	 */
	public static EclipseNode injectField(EclipseNode type, FieldDeclaration field) {
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if (parent.fields == null) {
			parent.fields = new FieldDeclaration[1];
			parent.fields[0] = field;
		} else {
			int size = parent.fields.length;
			FieldDeclaration[] newArray = new FieldDeclaration[size + 1];
			System.arraycopy(parent.fields, 0, newArray, 0, size);
			int index = 0;
			for (; index < size; index++) {
				FieldDeclaration f = newArray[index];
				if (isEnumConstant(f) || isGenerated(f) || isRecordField(f)) continue;
				break;
			}
			System.arraycopy(newArray, index, newArray, index + 1, size - index);
			newArray[index] = field;
			parent.fields = newArray;
		}
		
		if (isEnumConstant(field) || (field.modifiers & Modifier.STATIC) != 0) {
			if (!hasClinit(parent)) {
				parent.addClinit();
			}
		}
		
		return type.add(field, Kind.FIELD);
	}
	
	public static boolean isEnumConstant(final FieldDeclaration field) {
		return ((field.initialization instanceof AllocationExpression) && (((AllocationExpression) field.initialization).enumConstant == field));
	}
	
	/**
	 * Inserts a method into an existing type. The type must represent a {@code TypeDeclaration}.
	 */
	public static EclipseNode injectMethod(EclipseNode type, AbstractMethodDeclaration method) {
		method.annotations = addSuppressWarningsAll(type, method, method.annotations);
		method.annotations = addGenerated(type, method, method.annotations);
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
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
			//We insert the method in the last position of the methods registered to the type
			//When changing this behavior, this may trigger issue #155 and #377
			AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
			System.arraycopy(parent.methods, 0, newArray, 0, parent.methods.length);
			newArray[parent.methods.length] = method;
			parent.methods = newArray;
		}
		
		return type.add(method, Kind.METHOD);
	}
	
	/**
	 * Adds an inner type (class, interface, enum) to the given type. Cannot inject top-level types.
	 * 
	 * @param typeNode parent type to inject new type into
	 * @param type New type (class, interface, etc) to inject.
	 */
	public static EclipseNode injectType(final EclipseNode typeNode, final TypeDeclaration type) {
		type.annotations = addSuppressWarningsAll(typeNode, type, type.annotations);
		type.annotations = addGenerated(typeNode, type, type.annotations);
		TypeDeclaration parent = (TypeDeclaration) typeNode.get();
		
		if (parent.memberTypes == null) {
			parent.memberTypes = new TypeDeclaration[] { type };
		} else {
			TypeDeclaration[] newArray = new TypeDeclaration[parent.memberTypes.length + 1];
			System.arraycopy(parent.memberTypes, 0, newArray, 0, parent.memberTypes.length);
			newArray[parent.memberTypes.length] = type;
			parent.memberTypes = newArray;
		}
		
		return typeNode.add(type, Kind.TYPE);
	}
	
	static final char[] ALL = "all".toCharArray();
	static final char[] UNCHECKED = "unchecked".toCharArray();
	private static final char[] JUSTIFICATION = "justification".toCharArray();
	private static final char[] GENERATED_CODE = "generated code".toCharArray();
	private static final char[] LOMBOK = "lombok".toCharArray();
	private static final char[][] JAVAX_ANNOTATION_GENERATED = Eclipse.fromQualifiedName("javax.annotation.Generated");
	private static final char[][] JAKARTA_ANNOTATION_GENERATED = Eclipse.fromQualifiedName("jakarta.annotation.Generated");
	private static final char[][] LOMBOK_GENERATED = Eclipse.fromQualifiedName("lombok.Generated");
	private static final char[][] EDU_UMD_CS_FINDBUGS_ANNOTATIONS_SUPPRESSFBWARNINGS = Eclipse.fromQualifiedName("edu.umd.cs.findbugs.annotations.SuppressFBWarnings");
	
	public static Annotation[] addSuppressWarningsAll(EclipseNode node, ASTNode source, Annotation[] originalAnnotationArray) {
		Annotation[] anns = originalAnnotationArray;
		
		if (!Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_SUPPRESSWARNINGS_ANNOTATIONS))) {
			anns = addAnnotation(source, anns, TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new StringLiteral(ALL, 0, 0, 0));
		}
		
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS))) {
			MemberValuePair mvp = new MemberValuePair(JUSTIFICATION, 0, 0, new StringLiteral(GENERATED_CODE, 0, 0, 0));
			anns = addAnnotation(source, anns, EDU_UMD_CS_FINDBUGS_ANNOTATIONS_SUPPRESSFBWARNINGS, mvp);
		}
		
		return anns;
	}
	
	public static Annotation[] addGenerated(EclipseNode node, ASTNode source, Annotation[] originalAnnotationArray) {
		Annotation[] result = originalAnnotationArray;
		if (HandlerUtil.shouldAddGenerated(node)) {
			result = addAnnotation(source, result, JAVAX_ANNOTATION_GENERATED, new StringLiteral(LOMBOK, 0, 0, 0));
		}
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_JAKARTA_GENERATED_ANNOTATIONS))) {
			result = addAnnotation(source, result, JAKARTA_ANNOTATION_GENERATED, new StringLiteral(LOMBOK, 0, 0, 0));
		}
		if (!Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_LOMBOK_GENERATED_ANNOTATIONS))) {
			result = addAnnotation(source, result, LOMBOK_GENERATED);
		}
		return result;
	}
	
	static Annotation[] addAnnotation(ASTNode source, Annotation[] originalAnnotationArray, char[][] annotationTypeFqn) {
		return addAnnotation(source, originalAnnotationArray, annotationTypeFqn, (ASTNode[]) null);
	}
	
	static Annotation[] addAnnotation(ASTNode source, Annotation[] originalAnnotationArray, char[][] annotationTypeFqn, ASTNode... args) {
		char[] simpleName = annotationTypeFqn[annotationTypeFqn.length - 1];
		
		if (originalAnnotationArray != null) for (Annotation ann : originalAnnotationArray) {
			if (ann.type instanceof QualifiedTypeReference) {
				char[][] t = ((QualifiedTypeReference) ann.type).tokens;
				if (Arrays.deepEquals(t, annotationTypeFqn)) return originalAnnotationArray;
			}
			
			if (ann.type instanceof SingleTypeReference) {
				char[] lastToken = ((SingleTypeReference) ann.type).token;
				if (Arrays.equals(lastToken, simpleName)) return originalAnnotationArray;
			}
		}
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		TypeReference qualifiedType = generateQualifiedTypeRef(source, annotationTypeFqn);
		Annotation ann;
		if (args != null && args.length == 1 && args[0] instanceof Expression) {
			SingleMemberAnnotation sma = new SingleMemberAnnotation(qualifiedType, pS);
			sma.declarationSourceEnd = pE;
			args[0].sourceStart = pS;
			args[0].sourceEnd = pE;
			sma.memberValue = (Expression) args[0];
			setGeneratedBy(sma.memberValue, source);
			ann = sma;
		} else if (args != null && args.length >= 1 && arrayHasOnlyElementsOfType(args, MemberValuePair.class)) {
			NormalAnnotation na = new NormalAnnotation(qualifiedType, pS);
			na.declarationSourceEnd = pE;
			na.memberValuePairs = new MemberValuePair[args.length];
			for (int i = 0; i < args.length; i++) {
				args[i].sourceStart = pS;
				args[i].sourceEnd = pE;
				na.memberValuePairs[i] = (MemberValuePair) args[i];			
			}
			setGeneratedBy(na.memberValuePairs[0], source);
			setGeneratedBy(na.memberValuePairs[0].value, source);
			na.memberValuePairs[0].value.sourceStart = pS;
			na.memberValuePairs[0].value.sourceEnd = pE;
			ann = na;
		} else {
			MarkerAnnotation ma = new MarkerAnnotation(qualifiedType, pS);
			ma.declarationSourceEnd = pE;
			ann = ma;
		}
		setGeneratedBy(ann, source);
		if (originalAnnotationArray == null) return new Annotation[] { ann };
		Annotation[] newAnnotationArray = new Annotation[originalAnnotationArray.length + 1];
		System.arraycopy(originalAnnotationArray, 0, newAnnotationArray, 0, originalAnnotationArray.length);
		newAnnotationArray[originalAnnotationArray.length] = ann;
		return newAnnotationArray;
	}
	
	public static void addCheckerFrameworkReturnsReceiver(TypeReference returnType, ASTNode source, CheckerFrameworkVersion cfv) {
		if (cfv.generateReturnsReceiver()) {
			Annotation rrAnn = generateNamedAnnotation(source, CheckerFrameworkVersion.NAME__RETURNS_RECEIVER);
			int levels = returnType.getAnnotatableLevels();
			returnType.annotations = new Annotation[levels][];
			returnType.annotations[levels-1] = new Annotation[] {rrAnn};
		}
	}
	
	private static boolean arrayHasOnlyElementsOfType(Object[] array, Class<?> clazz) {
		for (Object element : array) {
			if (!clazz.isInstance(element))
				return false;
		}
		return true;
	}

	/**
	 * Generates a new statement that checks if the given local variable is null, and if so, throws a specified exception with the
	 * variable name as message.
	 */
	public static Statement generateNullCheck(TypeReference type, char[] variable, EclipseNode sourceNode, String customMessage) {
		NullCheckExceptionType exceptionType = sourceNode.getAst().readConfiguration(ConfigurationKeys.NON_NULL_EXCEPTION_TYPE);
		if (exceptionType == null) exceptionType = NullCheckExceptionType.NULL_POINTER_EXCEPTION;
		
		ASTNode source = sourceNode.get();
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		if (type != null && isPrimitive(type)) return null;
		SingleNameReference varName = new SingleNameReference(variable, p);
		setGeneratedBy(varName, source);
		
		StringLiteral message = new StringLiteral(exceptionType.toExceptionMessage(new String(variable), customMessage).toCharArray(), pS, pE, 0);
		setGeneratedBy(message, source);
		
		LombokImmutableList<String> method = exceptionType.getMethod();
		if (method != null) {
			
			MessageSend invocation = new MessageSend();
			invocation.sourceStart = pS; invocation.sourceEnd = pE;
			setGeneratedBy(invocation, source);
			
			char[][] utilityTypeName = new char[method.size() - 1][];
			for (int i = 0; i < method.size() - 1; i++) {
				utilityTypeName[i] = method.get(i).toCharArray();
			}
			
			invocation.receiver = new QualifiedNameReference(utilityTypeName, new long[method.size() - 1], pS, pE);
			setGeneratedBy(invocation.receiver, source);
			invocation.selector = method.get(method.size() - 1).toCharArray();
			invocation.arguments = new Expression[] {varName, message};
			return invocation;
		}
		
		AllocationExpression exception = new AllocationExpression();
		setGeneratedBy(exception, source);
		
		NullLiteral nullLiteral = new NullLiteral(pS, pE);
		setGeneratedBy(nullLiteral, source);
		
		int equalOperator = exceptionType == NullCheckExceptionType.ASSERTION ? OperatorIds.NOT_EQUAL : OperatorIds.EQUAL_EQUAL; 
		EqualExpression equalExpression = new EqualExpression(varName, nullLiteral, equalOperator);
		equalExpression.sourceStart = pS; equalExpression.statementEnd = equalExpression.sourceEnd = pE;
		setGeneratedBy(equalExpression, source);
		
		if (exceptionType == NullCheckExceptionType.ASSERTION) {
			Statement assertStatement = new AssertStatement(message, equalExpression, pS);
			setGeneratedBy(assertStatement, source);
			return assertStatement;
		}
		
		String exceptionTypeStr = exceptionType.getExceptionType();
		int partCount = 1;
		for (int i = 0; i < exceptionTypeStr.length(); i++) if (exceptionTypeStr.charAt(i) == '.') partCount++;
		long[] ps = new long[partCount];
		Arrays.fill(ps, 0L);
		exception.type = new QualifiedTypeReference(fromQualifiedName(exceptionTypeStr), ps);
		setGeneratedBy(exception.type, source);
		exception.arguments = new Expression[] {message};
		
		ThrowStatement throwStatement = new ThrowStatement(exception, pS, pE);
		setGeneratedBy(throwStatement, source);
		
		Block throwBlock = new Block(0);
		throwBlock.statements = new Statement[] {throwStatement};
		throwBlock.sourceStart = pS; throwBlock.sourceEnd = pE;
		setGeneratedBy(throwBlock, source);
		IfStatement ifStatement = new IfStatement(equalExpression, throwBlock, 0, 0);
		setGeneratedBy(ifStatement, source);
		return ifStatement;
	}
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a specified exception with the
	 * variable name as message.
	 * 
	 * @param exName The name of the exception to throw; normally {@code java.lang.NullPointerException}.
	 */
	public static Statement generateNullCheck(AbstractVariableDeclaration variable, EclipseNode sourceNode, String customMessage) {
		return generateNullCheck(variable.type, variable.name, sourceNode, customMessage);
	}
	
	/**
	 * Create an annotation of the given name, and is marked as being generated by the given source.
	 */
	public static MarkerAnnotation makeMarkerAnnotation(char[][] name, ASTNode source) {
		long pos = (long) source.sourceStart << 32 | source.sourceEnd;
		long[] poss = new long[name.length];
		Arrays.fill(poss, pos);
		TypeReference typeRef = new QualifiedTypeReference(name, poss);
		setGeneratedBy(typeRef, source);
		MarkerAnnotation ann = new MarkerAnnotation(typeRef, (int) (pos >> 32));
		ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = (int) pos;
		setGeneratedBy(ann, source);
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
				if ((((FieldDeclaration) child.get()).modifiers & ClassFileConstants.AccStatic) != 0) continue;
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
				
				if (castTo.getClass() == SingleTypeReference.class && !isPrimitive(castTo)) {
					SingleTypeReference str = (SingleTypeReference) castTo;
					//Why a SingleNameReference instead of a SingleTypeReference you ask? I don't know. It seems dumb. Ask the ecj guys.
					castToConverted = new SingleNameReference(str.token, 0);
					castToConverted.bits = (castToConverted.bits & ~Binding.VARIABLE) | Binding.TYPE;
					castToConverted.sourceStart = str.sourceStart;
					castToConverted.sourceEnd = str.sourceEnd;
					setGeneratedBy(castToConverted, source);
				} else if (castTo.getClass() == QualifiedTypeReference.class) {
					QualifiedTypeReference qtr = (QualifiedTypeReference) castTo;
					//Same here, but for the more complex types, they stay types.
					castToConverted = new QualifiedNameReference(qtr.tokens, copy(qtr.sourcePositions), qtr.sourceStart, qtr.sourceEnd);
					castToConverted.bits = (castToConverted.bits & ~Binding.VARIABLE) | Binding.TYPE;
					setGeneratedBy(castToConverted, source);
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
		
		result.sourceStart = source.sourceStart;
		result.sourceEnd = source.sourceEnd;
		result.statementEnd = source.sourceEnd;
		
		setGeneratedBy(result, source);
		return result;
	}
	
	private static final Constructor<CastExpression> castExpressionConstructor;
	private static final boolean castExpressionConstructorIsTypeRefBased;
	
	static {
		Constructor<?> constructor = null;
		for (Constructor<?> ctor : CastExpression.class.getConstructors()) {
			if (ctor.getParameterTypes().length != 2) continue;
			constructor = ctor;
		}
		
		@SuppressWarnings("unchecked")
		Constructor<CastExpression> castExpressionConstructor_ = (Constructor<CastExpression>) constructor;
		castExpressionConstructor = castExpressionConstructor_;
		
		castExpressionConstructorIsTypeRefBased =
				(castExpressionConstructor.getParameterTypes()[1] == TypeReference.class);
	}
	
	/**
	 * In eclipse 3.7+, IntLiterals are created using a factory-method 
	 * Unfortunately that means we need to use reflection as we want to be compatible
	 * with eclipse versions before 3.7.
	 */
	public static IntLiteral makeIntLiteral(char[] token, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		IntLiteral result;
		if (intLiteralConstructor != null) {
			result = Permit.newInstanceSneaky(intLiteralConstructor, token, pS, pE);
		} else {
			result = (IntLiteral) Permit.invokeSneaky(intLiteralFactoryMethod, null, token, pS, pE);
		}
		
		if (source != null) setGeneratedBy(result, source);
		return result;
	}
	
	private static final Constructor<IntLiteral> intLiteralConstructor;
	private static final Method intLiteralFactoryMethod;
	
	static {
		Class<?>[] parameterTypes = {char[].class, int.class, int.class};
		Constructor<IntLiteral> intLiteralConstructor_ = null;
		Method intLiteralFactoryMethod_ = null;
		try { 
			intLiteralConstructor_ = Permit.getConstructor(IntLiteral.class, parameterTypes);
		} catch (Throwable ignore) {
			// probably eclipse 3.7++
		}
		try { 
			intLiteralFactoryMethod_ = Permit.getMethod(IntLiteral.class, "buildIntLiteral", parameterTypes);
		} catch (Throwable ignore) {
			// probably eclipse versions before 3.7
		}
		intLiteralConstructor = intLiteralConstructor_;
		intLiteralFactoryMethod = intLiteralFactoryMethod_;
	}
	
	private static boolean isAllValidOnXCharacters(char[] in) {
		if (in == null || in.length == 0) return false;
		for (char c : in) if (c != '_' && c != 'X' && c != 'x' && c != '$') return false;
		return true;
	}
	
	public static void addError(String errorName, EclipseNode node) {
		if (node.getLatestJavaSpecSupported() < 8) {
			node.addError("The correct format is " + errorName + "_={@SomeAnnotation, @SomeOtherAnnotation})");
		} else {
			node.addError("The correct format is " + errorName + "=@__({@SomeAnnotation, @SomeOtherAnnotation}))");
		}
	}
	
	public static List<Annotation> unboxAndRemoveAnnotationParameter(Annotation annotation, String annotationName, String errorName, EclipseNode errorNode) {
		if ("value".equals(annotationName)) {
			// We can't unbox this, because SingleMemberAnnotation REQUIRES a value, and this method
			// is supposed to remove the value. That means we need to replace the SMA with either
			// MarkerAnnotation or NormalAnnotation and that is beyond the scope of this method as we
			// don't need that at the time of writing this method; we only unbox onMethod, onParameter
			// and onConstructor. Let's exit early and very obviously:
			throw new UnsupportedOperationException("Lombok cannot unbox 'value' from SingleMemberAnnotation at this time.");
		}
		if (!NormalAnnotation.class.equals(annotation.getClass())) {
			// Prevent MarkerAnnotation, SingleMemberAnnotation, and
			// CompletionOnAnnotationMemberValuePair from triggering this handler.
			return Collections.emptyList();
		}
		
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		MemberValuePair[] pairs = normalAnnotation.memberValuePairs;
		
		if (pairs == null) return Collections.emptyList();
		
		char[] nameAsCharArray = annotationName.toCharArray();
		
		top:
		for (int i = 0; i < pairs.length; i++) {
			boolean allowRaw;
			char[] name = pairs[i].name;
			if (name == null) continue;
			if (name.length < nameAsCharArray.length) continue;
			for (int j = 0; j < nameAsCharArray.length; j++) {
				if (name[j] != nameAsCharArray[j]) continue top;
			}
			allowRaw = name.length > nameAsCharArray.length;
			for (int j = nameAsCharArray.length; j < name.length; j++) {
				if (name[j] != '_') continue top;
			}
			// If we're still here it's the targeted annotation param.
			Expression value = pairs[i].value;
			MemberValuePair[] newPairs = new MemberValuePair[pairs.length - 1];
			if (i > 0) System.arraycopy(pairs, 0, newPairs, 0, i);
			if (i < pairs.length - 1) System.arraycopy(pairs, i + 1, newPairs, i, pairs.length - i - 1);
			normalAnnotation.memberValuePairs = newPairs;
			// We have now removed the annotation parameter and stored the value,
			// which we must now unbox. It's either annotations, or @__(annotations).
			
			Expression content = null;
			
			if (value instanceof ArrayInitializer) {
				if (!allowRaw) {
					addError(errorName, errorNode);
					return Collections.emptyList();
				}
				content = value;
			} else if (!(value instanceof Annotation)) {
				addError(errorName, errorNode);
				return Collections.emptyList();
			} else {
				Annotation atDummyIdentifier = (Annotation) value;
				if (atDummyIdentifier.type instanceof SingleTypeReference && isAllValidOnXCharacters(((SingleTypeReference) atDummyIdentifier.type).token)) {
					if (atDummyIdentifier instanceof MarkerAnnotation) {
						return Collections.emptyList();
					} else if (atDummyIdentifier instanceof NormalAnnotation) {
						MemberValuePair[] mvps = ((NormalAnnotation) atDummyIdentifier).memberValuePairs;
						if (mvps == null || mvps.length == 0) {
							return Collections.emptyList();
						}
						if (mvps.length == 1 && Arrays.equals("value".toCharArray(), mvps[0].name)) {
							content = mvps[0].value;
						}
					} else if (atDummyIdentifier instanceof SingleMemberAnnotation) {
						content = ((SingleMemberAnnotation) atDummyIdentifier).memberValue;
					} else {
						addError(errorName, errorNode);
						return Collections.emptyList();
					}
				} else {
					if (allowRaw) {
						content = atDummyIdentifier;
					} else {
						addError(errorName, errorNode);
						return Collections.emptyList();
					}
				}
			}
			
			if (content == null) {
				addError(errorName, errorNode);
				return Collections.emptyList();
			}
			
			if (content instanceof Annotation) {
				return Collections.singletonList((Annotation) content);
			} else if (content instanceof ArrayInitializer) {
				Expression[] expressions = ((ArrayInitializer) content).expressions;
				List<Annotation> result = new ArrayList<Annotation>();
				if (expressions != null) for (Expression ex : expressions) {
					if (ex instanceof Annotation) result.add((Annotation) ex);
					else {
						addError(errorName, errorNode);
						return Collections.emptyList();
					}
				}
				return result;
			} else {
				addError(errorName, errorNode);
				return Collections.emptyList();
			}
		}
		
		return Collections.emptyList();
	}
	
	public static NameReference createNameReference(String name, Annotation source) {
		return generateQualifiedNameRef(source, fromQualifiedName(name));
	}
	
	private static long[] copy(long[] array) {
		return array == null ? null : array.clone();
	}
	
	public static <T> T[] concat(T[] first, T[] second, Class<T> type) {
		if (first == null)
			return second;
		if (second == null)
			return first;
		if (first.length == 0)
			return second;
		if (second.length == 0)
			return first;
		T[] result = newArray(type, first.length + second.length);
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(Class<T> type, int length) {
		return (T[]) Array.newInstance(type, length);
	}
	
	public static boolean isDirectDescendantOfObject(EclipseNode typeNode) {
		if (!(typeNode.get() instanceof TypeDeclaration)) throw new IllegalArgumentException("not a type node");
		TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
		if (typeDecl.superclass == null) return true;
		String p = typeDecl.superclass.toString();
		return p.equals("Object") || p.equals("java.lang.Object");
	}
	
	public static void createRelevantNullableAnnotation(EclipseNode typeNode, MethodDeclaration mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToMethodDecl(typeNode, mth, lib.getNullableAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNonNullAnnotation(EclipseNode typeNode, MethodDeclaration mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToMethodDecl(typeNode, mth, lib.getNonNullAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNullableAnnotation(EclipseNode typeNode, Argument arg, MethodDeclaration mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToVarDecl(typeNode, arg, mth, lib.getNullableAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNullableAnnotation(EclipseNode typeNode, Argument arg, MethodDeclaration mth, List<NullAnnotationLibrary> applied) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null || applied.contains(lib)) return;
		
		applyAnnotationToVarDecl(typeNode, arg, mth, lib.getNullableAnnotation(), lib.isTypeUse());
	}
	
	public static void createRelevantNonNullAnnotation(EclipseNode typeNode, Argument arg, MethodDeclaration mth) {
		NullAnnotationLibrary lib = typeNode.getAst().readConfiguration(ConfigurationKeys.ADD_NULL_ANNOTATIONS);
		if (lib == null) return;
		
		applyAnnotationToVarDecl(typeNode, arg, mth, lib.getNonNullAnnotation(), lib.isTypeUse());
	}
	
	private static void applyAnnotationToMethodDecl(EclipseNode typeNode, MethodDeclaration mth, String annType, boolean typeUse) {
		if (annType == null) return;
		
		int partCount = 1;
		for (int i = 0; i < annType.length(); i++) if (annType.charAt(i) == '.') partCount++;
		long[] ps = new long[partCount];
		Arrays.fill(ps, 0L);
		Annotation ann = new MarkerAnnotation(new QualifiedTypeReference(Eclipse.fromQualifiedName(annType), ps), 0);
		
		if (!typeUse || mth.returnType == null || mth.returnType.getTypeName().length < 2) {
			Annotation[] a = mth.annotations;
			if (a == null) a = new Annotation[1];
			else {
				Annotation[] b = new Annotation[a.length + 1];
				System.arraycopy(a, 0, b, 0, a.length);
				a = b;
			}
			a[a.length - 1] = ann;
			mth.annotations = a;
		} else {
			int len = mth.returnType.getTypeName().length;
			if (mth.returnType.annotations == null) mth.returnType.annotations = new Annotation[len][];
			Annotation[] a = mth.returnType.annotations[len - 1];
			if (a == null) a = new Annotation[1];
			else {
				Annotation[] b = new Annotation[a.length + 1];
				System.arraycopy(a, 0, b, 1, a.length);
				a = b;
			}
			a[0] = ann;
			mth.returnType.annotations[len - 1] = a;
			mth.bits |= Eclipse.HasTypeAnnotations;
		}
	}
	private static void applyAnnotationToVarDecl(EclipseNode typeNode, Argument arg, MethodDeclaration mth, String annType, boolean typeUse) {
		if (annType == null) return;
		
		int partCount = 1;
		for (int i = 0; i < annType.length(); i++) if (annType.charAt(i) == '.') partCount++;
		long[] ps = new long[partCount];
		Arrays.fill(ps, 0L);
		Annotation ann = new MarkerAnnotation(new QualifiedTypeReference(Eclipse.fromQualifiedName(annType), ps), 0);
		
		if (!typeUse || arg.type.getTypeName().length < 2) {
			Annotation[] a = arg.annotations;
			if (a == null) a = new Annotation[1];
			else {
				Annotation[] b = new Annotation[a.length + 1];
				System.arraycopy(a, 0, b, 0, a.length);
				a = b;
			}
			a[a.length - 1] = ann;
			arg.annotations = a;
		} else {
			int len = arg.type.getTypeName().length;
			if (arg.type.annotations == null) arg.type.annotations = new Annotation[len][];
			Annotation[] a = arg.type.annotations[len - 1];
			if (a == null) a = new Annotation[1];
			else {
				Annotation[] b = new Annotation[a.length + 1];
				System.arraycopy(a, 0, b, 1, a.length);
				a = b;
			}
			a[0] = ann;
			arg.type.annotations[len - 1] = a;
			arg.type.bits |= Eclipse.HasTypeAnnotations;
			arg.bits |= Eclipse.HasTypeAnnotations;
			mth.bits |= Eclipse.HasTypeAnnotations;
		}
	}
	
	public static NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		NameReference ref;
		
		if (varNames.length > 1) ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
		else ref = new SingleNameReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
	
	public static TypeReference generateQualifiedTypeRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		TypeReference ref;
		
		long[] poss = Eclipse.poss(source, varNames.length);
		if (varNames.length > 1) ref = new QualifiedTypeReference(varNames, poss);
		else ref = new SingleTypeReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
	
	public static TypeReference createTypeReference(String typeName, ASTNode source) {
		return generateQualifiedTypeRef(source, fromQualifiedName(typeName));
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class and not some other type declaration (so, not an annotation definition, interface, enum, or record).
	 */
	public static boolean isClass(EclipseNode typeNode) {
		return isTypeAndDoesNotHaveFlags(typeNode, ClassFileConstants.AccInterface | ClassFileConstants.AccEnum | ClassFileConstants.AccAnnotation | AccRecord);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class or enum and not some other type declaration (so, not an annotation definition, interface, or record).
	 */
	public static boolean isClassOrEnum(EclipseNode typeNode) {
		return isTypeAndDoesNotHaveFlags(typeNode, ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | AccRecord);
	}
	
	/**
	 * Returns {@code true} if the provided node is an actual class, an enum or a record and not some other type declaration (so, not an annotation definition or interface).
	 */
	public static boolean isClassEnumOrRecord(EclipseNode typeNode) {
		return isTypeAndDoesNotHaveFlags(typeNode, ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation);
	}
	
	/**
	 * Returns {@code true} if the provided node is a record declaration (so, not an annotation definition, interface, enum, or plain class).
	 */
	public static boolean isRecord(EclipseNode typeNode) {
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		return (modifiers & AccRecord) != 0;
	}
	
	/**
	 * Returns {@code true} If the provided node is a field declaration, and represents a field in a {@code record} declaration.
	 */
	public static boolean isRecordField(EclipseNode fieldNode) {
		return fieldNode.getKind() == Kind.FIELD && (((FieldDeclaration) fieldNode.get()).modifiers & AccRecord) != 0;
	}
	
	/**
	 * Returns {@code true} If the provided node is a field declaration, and represents a field in a {@code record} declaration.
	 */
	public static boolean isRecordField(FieldDeclaration fieldDeclaration) {
		return (fieldDeclaration.modifiers & AccRecord) != 0;
	}
	
	/**
	 * Returns {@code true) if the provided node is a type declaration <em>and</em> is <strong>not</strong> of any kind indicated by the flags (the intent is to pass flags usch as `ClassFileConstants.AccEnum`).
	 */
	static boolean isTypeAndDoesNotHaveFlags(EclipseNode typeNode, long flags) {
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		return (modifiers & flags) == 0;
	}
	
	/**
	 * Returns {@code true} if the provided node supports static methods and types (top level or static class)
	 */
	public static boolean isStaticAllowed(EclipseNode typeNode) {
		return typeNode.isStatic() || typeNode.up() == null || typeNode.up().getKind() == Kind.COMPILATION_UNIT || isRecord(typeNode);
	}
	
	public static AbstractVariableDeclaration[] getRecordComponents(TypeDeclaration typeDeclaration) {
		if (typeDeclaration == null || (typeDeclaration.modifiers & AccRecord) == 0) return null;
		try {
			return (AbstractVariableDeclaration[]) TYPE_DECLARATION_RECORD_COMPONENTS.get(typeDeclaration);
		} catch (Exception e) {
			// This presumably means this isn't a JDK16 - fall through.
		}
		return null;
	}
	
	public static Annotation[][] getRecordFieldAnnotations(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.fields == null) return null;
		Annotation[][] annotations = new Annotation[typeDeclaration.fields.length][];
		
		AbstractVariableDeclaration[] recordComponents = getRecordComponents(typeDeclaration);
		if (recordComponents != null) {
			int j = 0;
			for (int i = 0; i < typeDeclaration.fields.length; i++) {
				if ((typeDeclaration.fields[i].modifiers & AccRecord) != 0) {
					annotations[i] = recordComponents[j++].annotations;
				}
			}
		}
		return annotations;
	}
	
	public static String getDocComment(EclipseNode eclipseNode) {
		if (eclipseNode.getAst().getSource() == null) return null;
		
		int start = -1;
		int end = -1;
		
		final ASTNode node = eclipseNode.get();
		if (node instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			start = fieldDeclaration.declarationSourceStart;
			end = fieldDeclaration.declarationSourceEnd;
		} else if (node instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) node;
			start = abstractMethodDeclaration.declarationSourceStart;
			end = abstractMethodDeclaration.declarationSourceEnd;
		}
		if (start != -1 && end != -1) {
			char[] rawContent = CharOperation.subarray(eclipseNode.getAst().getSource(), start, end);
			String rawContentString = new String(rawContent);
			int startIndex = rawContentString.indexOf("/**");
			int endIndex = rawContentString.indexOf("*/");
			if (startIndex != -1 && endIndex != -1) {
				/* Remove all leading asterisks */
				return rawContentString.substring(startIndex + 3, endIndex).replaceAll("(?m)^\\s*\\* ?", "").trim();
			}
		}
		return null;
	}
	
	public static void setDocComment(EclipseNode typeNode, EclipseNode eclipseNode, String doc) {
		setDocComment((CompilationUnitDeclaration) eclipseNode.top().get(), (TypeDeclaration) typeNode.get(), eclipseNode.get(), doc);
	}
	
	public static void setDocComment(CompilationUnitDeclaration cud, EclipseNode eclipseNode, String doc) {
		setDocComment(cud, (TypeDeclaration) upToTypeNode(eclipseNode).get(), eclipseNode.get(), doc);
	}
	
	public static void setDocComment(CompilationUnitDeclaration cud, TypeDeclaration type, ASTNode node, String doc) {
		if (doc == null) return;
		
		ICompilationUnit compilationUnit = cud.compilationResult.compilationUnit;
		if (compilationUnit.getClass().equals(COMPILATION_UNIT)) {
			try {
				compilationUnit = (ICompilationUnit) Permit.invoke(COMPILATION_UNIT_ORIGINAL_FROM_CLONE, compilationUnit);
			} catch (Throwable t) { }
		}
		
		Map<String, String> docs = EcjAugments.CompilationUnit_javadoc.setIfAbsent(compilationUnit, new HashMap<String, String>());
		if (node instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) node;
			String signature = getSignature(type, methodDeclaration);
			/* Add javadoc start marker, remove trailing line break, add leading asterisks to each line, add javadoc end marker */
			docs.put(signature, String.format("/**%n%s%n */", doc.replaceAll("$\\r?\\n", "").replaceAll("(?m)^", " * ")));
		}
	}
	
	public static String getSignature(TypeDeclaration type, AbstractMethodDeclaration methodDeclaration) {
		StringBuilder sb = new StringBuilder();
		sb.append(type.name);
		sb.append(".");
		sb.append(methodDeclaration.selector);
		sb.append("(");
		Argument[] arguments = methodDeclaration.arguments;
		if (arguments != null) {
			for (Argument argument : arguments) {
				sb.append(String.valueOf(argument.type));
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static enum CopyJavadoc {
		VERBATIM {
			@Override public String apply(final EclipseNode node) {
				return getDocComment(node);
			}
		},
		GETTER {
			@Override public String apply(final EclipseNode node) {
				String javadoc = getDocComment(node);
				// step 1: Check if there is a 'GETTER' section. If yes, that becomes the new method's javadoc.
				String out = getJavadocSection(javadoc, "GETTER");
				final boolean sectionBased = out != null;
				if (!sectionBased) {
					out = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.PARAM);
				}
				return out;
			}
		},
		SETTER {
			@Override public String apply(final EclipseNode node) {
				return applySetter(node, "SETTER");
			}
		},
		WITH {
			@Override public String apply(final EclipseNode node) {
				return addReturnsUpdatedSelfIfNeeded(applySetter(node, "WITH|WITHER"));
			}
		},
		WITH_BY {
			@Override public String apply( final EclipseNode node) {
				return applySetter(node, "WITHBY|WITH_BY");
			}
		};
		
		public abstract String apply(final EclipseNode node);
		
		private static String applySetter(EclipseNode node, String sectionName) {
			String javadoc = getDocComment(node);
			// step 1: Check if there is a 'SETTER' section. If yes, that becomes the new method's javadoc.
			String out = getJavadocSection(javadoc, sectionName);
			final boolean sectionBased = out != null;
			if (!sectionBased) {
				out = stripLinesWithTagFromJavadoc(stripSectionsFromJavadoc(javadoc), JavadocTag.RETURN);
			}
			return shouldReturnThis(node, EclipseHandlerUtil.getAccessorsForField(node)) ? addReturnsThisIfNeeded(out) : out;
		}
	}
	
	/**
	 * Copies javadoc on one node to the other.
	 * 
	 * This one is a shortcut for {@link EclipseHandlerUtil#copyJavadoc(EclipseNode, ASTNode, TypeDeclaration, CopyJavadoc, boolean)}
	 * if source and target node are in the same type.
	 */
	public static void copyJavadoc(EclipseNode from, ASTNode to, CopyJavadoc copyMode) {
		copyJavadoc(from, to, (TypeDeclaration) upToTypeNode(from).get(), copyMode, false);
	}
	
	/**
	 * Copies javadoc on one node to the other.
	 * 
	 * This one is a shortcut for {@link EclipseHandlerUtil#copyJavadoc(EclipseNode, ASTNode, TypeDeclaration, CopyJavadoc, boolean)}
	 * if source and target node are in the same type.
	 */
	public static void copyJavadoc(EclipseNode from, ASTNode to, CopyJavadoc copyMode, boolean forceAddReturn) {
		copyJavadoc(from, to, (TypeDeclaration) upToTypeNode(from).get(), copyMode, forceAddReturn);
	}
	
	public static void copyJavadoc(EclipseNode from, ASTNode to, TypeDeclaration type, CopyJavadoc copyMode) {
		copyJavadoc(from, to, type, copyMode, false);
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
	public static void copyJavadoc(EclipseNode from, ASTNode to, TypeDeclaration type, CopyJavadoc copyMode, boolean forceAddReturn) {
		if (copyMode == null) copyMode = CopyJavadoc.VERBATIM;
		try {
			CompilationUnitDeclaration cud = ((CompilationUnitDeclaration) from.top().get());
			String newJavadoc = copyMode.apply(from);
			if (forceAddReturn) {
				newJavadoc = addReturnsThisIfNeeded(newJavadoc);
			}
			setDocComment(cud, type, to, newJavadoc);
		} catch (Exception ignore) {}
	}
	
	public static void copyJavadocFromParam(EclipseNode from, MethodDeclaration to, TypeDeclaration type, String param) {
		try {
			CompilationUnitDeclaration cud = (CompilationUnitDeclaration) from.top().get();
			String methodComment = getDocComment(from);
			String newJavadoc = addReturnsThisIfNeeded(getParamJavadoc(methodComment, param));
			setDocComment(cud, type, to, newJavadoc);
		} catch (Exception ignore) {}
	}

	/**
	 * Returns the method node containing the given annotation, or {@code null} if the given annotation node is not on a method or argument.
	 */
	public static EclipseNode getAnnotatedMethod(EclipseNode node) {
		if (node == null || node.getKind() != Kind.ANNOTATION) return null;
		
		EclipseNode result = node.up();
		if (result.getKind() == Kind.ARGUMENT) {
			result = node.up();
		}
		if (result.getKind() != Kind.METHOD) {
			result = null;
		}
		return result;
	}

	/**
	 * Returns {@code true} if the given method node body was parsed.
	 */
	public static boolean hasParsedBody(EclipseNode method) {
		if (method == null || method.getKind() != Kind.METHOD) return false;
		
		boolean isCompleteParse = method.getAst().isCompleteParse();
		if (isCompleteParse) return true;
		
		AbstractMethodDeclaration methodDecl = (AbstractMethodDeclaration) method.get();
		if (methodDecl.statements != null) return true;
		
		// If the method is part of a field initializer it was parsed
		EclipseNode parent = method.up();
		while (parent != null) {
			if (parent.getKind() == Kind.FIELD) return true;
			parent = parent.up();
		}
		return false;
	}
}
