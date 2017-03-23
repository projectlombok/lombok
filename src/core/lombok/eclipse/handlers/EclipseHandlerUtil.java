/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.EclipseAugments.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.Data;
import lombok.Getter;
import lombok.Lombok;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.TypeResolver;
import lombok.core.configuration.NullCheckExceptionType;
import lombok.core.debug.ProblemReporter;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
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
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

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
	
	/**
	 * Checks if the given TypeReference node is likely to be a reference to the provided class.
	 * 
	 * @param type An actual type. This method checks if {@code typeNode} is likely to be a reference to this type.
	 * @param node A Lombok AST node. Any node in the appropriate compilation unit will do (used to get access to import statements).
	 * @param typeRef A type reference to check.
	 */
	public static boolean typeMatches(Class<?> type, EclipseNode node, TypeReference typeRef) {
		if (typeRef == null || typeRef.getTypeName() == null || typeRef.getTypeName().length == 0) return false;
		String lastPartA = new String(typeRef.getTypeName()[typeRef.getTypeName().length -1]);
		String lastPartB = type.getSimpleName();
		if (!lastPartA.equals(lastPartB)) return false;
		String typeName = toQualifiedName(typeRef.getTypeName());
		
		TypeResolver resolver = new TypeResolver(node.getImportList());
		return resolver.typeMatches(node, type.getName(), typeName);
		
	}
	
	public static void sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(EclipseNode typeNode, EclipseNode errorNode) {
		List<String> disallowed = null;
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.ANNOTATION) continue;
			for (Class<? extends java.lang.annotation.Annotation> annType : INVALID_ON_BUILDERS) {
				if (annotationTypeMatches(annType, child)) {
					if (disallowed == null) disallowed = new ArrayList<String>();
					disallowed.add(annType.getSimpleName());
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
			return ann;
		}
		
		if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation ann = new SingleMemberAnnotation(copyType(annotation.type, source), pS);
			setGeneratedBy(ann, source);
			ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = pE;
			//TODO memberValue(s) need to be copied as well (same for copying a NormalAnnotation as below).
			ann.memberValue = ((SingleMemberAnnotation)annotation).memberValue;
			return ann;
		}
		
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation ann = new NormalAnnotation(copyType(annotation.type, source), pS);
			setGeneratedBy(ann, source);
			ann.declarationSourceEnd = ann.statementEnd = ann.sourceEnd = pE;
			ann.memberValuePairs = ((NormalAnnotation)annotation).memberValuePairs;
			return ann;
		}
		
		return annotation;
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
	
	public static TypeReference namePlusTypeParamsToTypeReference(char[] typeName, TypeParameter[] params, long p) {
		if (params != null && params.length > 0) {
			TypeReference[] refs = new TypeReference[params.length];
			int idx = 0;
			for (TypeParameter param : params) {
				TypeReference typeRef = new SingleTypeReference(param.name, p);
				refs[idx++] = typeRef;
			}
			return new ParameterizedSingleTypeReference(typeName, refs, 0, p);
		}
		
		return new SingleTypeReference(typeName, p);
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
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof ArrayQualifiedTypeReference) {
			ArrayQualifiedTypeReference iRef = (ArrayQualifiedTypeReference) ref;
			TypeReference typeRef = new ArrayQualifiedTypeReference(iRef.tokens, iRef.dimensions(), copy(iRef.sourcePositions));
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof QualifiedTypeReference) {
			QualifiedTypeReference iRef = (QualifiedTypeReference) ref;
			TypeReference typeRef = new QualifiedTypeReference(iRef.tokens, copy(iRef.sourcePositions));
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
			
			TypeReference typeRef = new ParameterizedSingleTypeReference(iRef.token, args, iRef.dimensions(), (long)iRef.sourceStart << 32 | iRef.sourceEnd);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof ArrayTypeReference) {
			ArrayTypeReference iRef = (ArrayTypeReference) ref;
			TypeReference typeRef = new ArrayTypeReference(iRef.token, iRef.dimensions(), (long)iRef.sourceStart << 32 | iRef.sourceEnd);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		if (ref instanceof Wildcard) {
			Wildcard original = (Wildcard)ref;
			
			Wildcard wildcard = new Wildcard(original.kind);
			wildcard.sourceStart = original.sourceStart;
			wildcard.sourceEnd = original.sourceEnd;
			if (original.bound != null) wildcard.bound = copyType(original.bound, source);
			if (source != null) setGeneratedBy(wildcard, source);
			return wildcard;
		}
		
		if (ref instanceof SingleTypeReference) {
			SingleTypeReference iRef = (SingleTypeReference) ref;
			TypeReference typeRef = new SingleTypeReference(iRef.token, (long)iRef.sourceStart << 32 | iRef.sourceEnd);
			if (source != null) setGeneratedBy(typeRef, source);
			return typeRef;
		}
		
		return ref;
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
	
	/**
	 * Checks if the provided annotation type is likely to be the intended type for the given annotation node.
	 * 
	 * This is a guess, but a decent one.
	 */
	public static boolean annotationTypeMatches(Class<? extends java.lang.annotation.Annotation> type, EclipseNode node) {
		if (node.getKind() != Kind.ANNOTATION) return false;
		return typeMatches(type, node, ((Annotation) node.get()).type);
	}
	
	public static TypeReference cloneSelfType(EclipseNode context) {
		return cloneSelfType(context, null);
	}
	
	public static TypeReference cloneSelfType(EclipseNode context, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
		long p = (long)pS << 32 | pE;
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
				result = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
			} else {
				result = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
			}
		}
		if (result != null && source != null) setGeneratedBy(result, source);
		return result;
	}
	
	public static TypeReference makeType(TypeBinding binding, ASTNode pos, boolean allowCompound) {
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
					return makeType(wildcard.bound, pos, false);
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
		/* Calculate generics */ {
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
				expressions = ((ArrayInitializer)rhs).expressions;
			} else if (rhs != null) {
				expressions = new Expression[] { rhs };
			}
			if (expressions != null) for (Expression ex : expressions) {
				StringBuffer sb = new StringBuffer();
				ex.print(0, sb);
				raws.add(sb.toString());
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
		
		if (!hasGetterAnnotation && new HandleGetter().fieldQualifiesForGetterGeneration(field)) {
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
	
	public enum FieldAccess {
		GETTER, PREFER_FIELD, ALWAYS_FIELD;
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
		boolean lookForGetter = lookForGetter(field, fieldAccess);
		
		GetterMethod getter = lookForGetter ? findGetter(field) : null;
		if (getter == null) {
			return ((FieldDeclaration)field.get()).type;
		}
		
		return getter.type;
	}
	
	static Expression createFieldAccessor(EclipseNode field, FieldAccess fieldAccess, ASTNode source) {
		int pS = source == null ? 0 : source.sourceStart, pE = source == null ? 0 : source.sourceEnd;
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
	
	/** Serves as return value for the methods that check for the existence of fields and methods. */
	public enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_LOMBOK, EXISTS_BY_USER;
	}
	
	/**
	 * Translates the given field into all possible getter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllGetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static List<String> toAllGetterNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllGetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely getter name for the stated field. (e.g. private boolean foo; to isFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toGetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toGetterName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toGetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible setter names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllSetterNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllSetterNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllSetterNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely setter name for the stated field. (e.g. private boolean foo; to setFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toSetterName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toSetterName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toSetterName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * Translates the given field into all possible wither names.
	 * Convenient wrapper around {@link TransformationsUtil#toAllWitherNames(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static java.util.List<String> toAllWitherNames(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toAllWitherNames(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * @return the likely wither name for the stated field. (e.g. private boolean foo; to withFoo).
	 * 
	 * Convenient wrapper around {@link TransformationsUtil#toWitherName(lombok.core.AnnotationValues, CharSequence, boolean)}.
	 */
	public static String toWitherName(EclipseNode field, boolean isBoolean) {
		return HandlerUtil.toWitherName(field.getAst(), getAccessorsForField(field), field.getName(), isBoolean);
	}
	
	/**
	 * When generating a setter, the setter either returns void (beanspec) or Self (fluent).
	 * This method scans for the {@code Accessors} annotation and associated config properties to figure that out.
	 */
	public static boolean shouldReturnThis(EclipseNode field) {
		if ((((FieldDeclaration) field.get()).modifiers & ClassFileConstants.AccStatic) != 0) return false;
		AnnotationValues<Accessors> accessors = EclipseHandlerUtil.getAccessorsForField(field);
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
				((AllocationExpression)declaration.initialization).enumConstant != null) return false;
		
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
		for (EclipseNode node : field.down()) {
			if (annotationTypeMatches(Accessors.class, node)) {
				return createAnnotation(Accessors.class, node);
			}
		}
		
		EclipseNode current = field.up();
		while (current != null) {
			for (EclipseNode node : current.down()) {
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
		while (node != null && !(node.get() instanceof TypeDeclaration)) {
			node = node.up();
		}
		
		if (node != null && node.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
				if (def instanceof ConstructorDeclaration) {
					if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
					
					if (isTolerate(node, def)) continue;
					
					return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
				}
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
				if (isEnumConstant(f) || isGenerated(f)) continue;
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
	
	private static final char[] ALL = "all".toCharArray();
	private static final char[] JUSTIFICATION = "justification".toCharArray();
	private static final char[] GENERATED_CODE = "generated code".toCharArray();
	private static final char[] LOMBOK = "lombok".toCharArray();
	private static final char[][] JAVAX_ANNOTATION_GENERATED = Eclipse.fromQualifiedName("javax.annotation.Generated");
	private static final char[][] LOMBOK_GENERATED = Eclipse.fromQualifiedName("lombok.Generated");
	private static final char[][] EDU_UMD_CS_FINDBUGS_ANNOTATIONS_SUPPRESSFBWARNINGS = Eclipse.fromQualifiedName("edu.umd.cs.findbugs.annotations.SuppressFBWarnings");
	
	public static Annotation[] addSuppressWarningsAll(EclipseNode node, ASTNode source, Annotation[] originalAnnotationArray) {
		Annotation[] anns = addAnnotation(source, originalAnnotationArray, TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new StringLiteral(ALL, 0, 0, 0));
		
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
		if (Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.ADD_LOMBOK_GENERATED_ANNOTATIONS))) {
			result = addAnnotation(source, result, LOMBOK_GENERATED, null);
		}
		return result;
	}
	
	private static Annotation[] addAnnotation(ASTNode source, Annotation[] originalAnnotationArray, char[][] annotationTypeFqn, ASTNode arg) {
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
		long p = (long)pS << 32 | pE;
		long[] poss = new long[annotationTypeFqn.length];
		Arrays.fill(poss, p);
		QualifiedTypeReference qualifiedType = new QualifiedTypeReference(annotationTypeFqn, poss);
		setGeneratedBy(qualifiedType, source);
		Annotation ann;
		if (arg instanceof Expression) {
			SingleMemberAnnotation sma = new SingleMemberAnnotation(qualifiedType, pS);
			sma.declarationSourceEnd = pE;
			arg.sourceStart = pS;
			arg.sourceEnd = pE;
			sma.memberValue = (Expression) arg;
			setGeneratedBy(sma.memberValue, source);
			ann = sma;
		} else if (arg instanceof MemberValuePair) {
			NormalAnnotation na = new NormalAnnotation(qualifiedType, pS);
			na.declarationSourceEnd = pE;
			arg.sourceStart = pS;
			arg.sourceEnd = pE;
			na.memberValuePairs = new MemberValuePair[] {(MemberValuePair) arg};
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
	
	/**
	 * Generates a new statement that checks if the given variable is null, and if so, throws a specified exception with the
	 * variable name as message.
	 * 
	 * @param exName The name of the exception to throw; normally {@code java.lang.NullPointerException}.
	 */
	public static Statement generateNullCheck(AbstractVariableDeclaration variable, EclipseNode sourceNode) {
		NullCheckExceptionType exceptionType = sourceNode.getAst().readConfiguration(ConfigurationKeys.NON_NULL_EXCEPTION_TYPE);
		if (exceptionType == null) exceptionType = NullCheckExceptionType.NULL_POINTER_EXCEPTION;
		
		ASTNode source = sourceNode.get();
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		if (isPrimitive(variable.type)) return null;
		AllocationExpression exception = new AllocationExpression();
		setGeneratedBy(exception, source);
		int partCount = 1;
		String exceptionTypeStr = exceptionType.getExceptionType();
		for (int i = 0; i < exceptionTypeStr.length(); i++) if (exceptionTypeStr.charAt(i) == '.') partCount++;
		long[] ps = new long[partCount];
		Arrays.fill(ps, 0L);
		exception.type = new QualifiedTypeReference(fromQualifiedName(exceptionTypeStr), ps);
		setGeneratedBy(exception.type, source);
		exception.arguments = new Expression[] {
				new StringLiteral(exceptionType.toExceptionMessage(new String(variable.name)).toCharArray(), pS, pE, 0)
		};
		setGeneratedBy(exception.arguments[0], source);
		ThrowStatement throwStatement = new ThrowStatement(exception, pS, pE);
		setGeneratedBy(throwStatement, source);
		
		SingleNameReference varName = new SingleNameReference(variable.name, p);
		setGeneratedBy(varName, source);
		NullLiteral nullLiteral = new NullLiteral(pS, pE);
		setGeneratedBy(nullLiteral, source);
		EqualExpression equalExpression = new EqualExpression(varName, nullLiteral, OperatorIds.EQUAL_EQUAL);
		equalExpression.sourceStart = pS; equalExpression.statementEnd = equalExpression.sourceEnd = pE;
		setGeneratedBy(equalExpression, source);
		Block throwBlock = new Block(0);
		throwBlock.statements = new Statement[] {throwStatement};
		throwBlock.sourceStart = pS; throwBlock.sourceEnd = pE;
		setGeneratedBy(throwBlock, source);
		IfStatement ifStatement = new IfStatement(equalExpression, throwBlock, 0, 0);
		setGeneratedBy(ifStatement, source);
		return ifStatement;
	}
	
	/**
	 * Create an annotation of the given name, and is marked as being generated by the given source.
	 */
	public static MarkerAnnotation makeMarkerAnnotation(char[][] name, ASTNode source) {
		long pos = (long)source.sourceStart << 32 | source.sourceEnd;
		TypeReference typeRef = new QualifiedTypeReference(name, new long[] {pos, pos, pos});
		setGeneratedBy(typeRef, source);
		MarkerAnnotation ann = new MarkerAnnotation(typeRef, (int)(pos >> 32));
		ann.declarationSourceEnd = ann.sourceEnd = ann.statementEnd = (int)pos;
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
		try {
			if (intLiteralConstructor != null) {
				result = intLiteralConstructor.newInstance(token, pS, pE);
			} else {
				result = (IntLiteral) intLiteralFactoryMethod.invoke(null, token, pS, pE);
			}
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InstantiationException e) {
			throw Lombok.sneakyThrow(e);
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
			intLiteralConstructor_ = IntLiteral.class.getConstructor(parameterTypes);
		} catch (Throwable ignore) {
			// probably eclipse 3.7++
		}
		try { 
			intLiteralFactoryMethod_ = IntLiteral.class.getMethod("buildIntLiteral", parameterTypes);
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
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		char[][] nameTokens = fromQualifiedName(name);
		long[] pos = new long[nameTokens.length];
		Arrays.fill(pos, p);
		
		QualifiedNameReference nameReference = new QualifiedNameReference(nameTokens, pos, pS, pE);
		nameReference.statementEnd = pE;
		
		setGeneratedBy(nameReference, source);
		return nameReference;
	}
	
	private static long[] copy(long[] array) {
		return array == null ? null : array.clone();
	}
}
