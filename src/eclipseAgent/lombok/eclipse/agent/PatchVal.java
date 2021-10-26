/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
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
package lombok.eclipse.agent;

import lombok.permit.Permit;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

import java.lang.reflect.Field;

import static lombok.Lombok.sneakyThrow;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static org.eclipse.jdt.core.compiler.CategorizedProblem.CAT_TYPE;

public class PatchVal {
	
	// This is half of the work for 'val' support - the other half is in PatchValEclipse. This half is enough for ecj.
	// Creates a copy of the 'initialization' field on a LocalDeclaration if the type of the LocalDeclaration is 'val', because the completion parser will null this out,
	// which in turn stops us from inferring the intended type for 'val x = 5;'. We look at the copy.
	// Also patches .resolve() on LocalDeclaration itself to just-in-time replace the 'val' vartype with the right one.
	
	public static boolean matches(String key, char[] array) {
		if (array == null || key.length() != array.length) return false;
		for (int i = 0; i < array.length; i++) {
			if (key.charAt(i) != array[i]) return false;
		}
		
		return true;
	}
	
	public static boolean couldBe(ImportBinding[] imports, String key, TypeReference ref) {
		String[] keyParts = key.split("\\.");
		if (ref instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference)ref).token;
			if (!matches(keyParts[keyParts.length - 1], token)) return false;
			if (imports == null) return true;
			top:
			for (ImportBinding ib : imports) {
				ImportReference ir = ib.reference;
				if (ir == null) continue;
				if (ir.isStatic()) continue;
				boolean star = ((ir.bits & ASTNode.OnDemand) != 0);
				int len = keyParts.length - (star ? 1 : 0);
				char[][] t = ir.tokens;
				if (len != t.length) continue;
				for (int i = 0; i < len; i++) {
					if (keyParts[i].length() != t[i].length) continue top;
					for (int j = 0; j < t[i].length; j++) if (keyParts[i].charAt(j) != t[i][j]) continue top;
				}
				return true;
			}
			return false;
		}
		
		if (ref instanceof QualifiedTypeReference) {
			char[][] tokens = ((QualifiedTypeReference)ref).tokens;
			if (keyParts.length != tokens.length) return false;
			for(int i = 0; i < tokens.length; ++i) {
				String part = keyParts[i];
				char[] token = tokens[i];
				if (!matches(part, token)) return false;
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean couldBe(ImportReference[] imports, String key, TypeReference ref) {
		String[] keyParts = key.split("\\.");
		if (ref instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference) ref).token;
			if (!matches(keyParts[keyParts.length - 1], token)) return false;
			if (imports == null) return true;
			top:
			for (ImportReference ir : imports) {
				if (ir.isStatic()) continue;
				boolean star = ((ir.bits & ASTNode.OnDemand) != 0);
				int len = keyParts.length - (star ? 1 : 0);
				char[][] t = ir.tokens;
				if (len != t.length) continue;
				for (int i = 0; i < len; i++) {
					if (keyParts[i].length() != t[i].length) continue top;
					for (int j = 0; j < t[i].length; j++) if (keyParts[i].charAt(j) != t[i][j]) continue top;
				}
				return true;
			}
			return false;
		}
		
		if (ref instanceof QualifiedTypeReference) {
			char[][] tokens = ((QualifiedTypeReference) ref).tokens;
			if (keyParts.length != tokens.length) return false;
			for(int i = 0; i < tokens.length; ++i) {
				String part = keyParts[i];
				char[] token = tokens[i];
				if (!matches(part, token)) return false;
			}
			return true;
		}
		
		return false;
	}
	
	private static boolean is(TypeReference ref, BlockScope scope, String key) {
		Scope s = scope.parent;
		while (s != null && !(s instanceof CompilationUnitScope)) {
			Scope ns = s.parent;
			s = ns == s ? null : ns;
		}
		ImportBinding[] imports = null;
		if (s instanceof CompilationUnitScope) imports = ((CompilationUnitScope) s).imports;
		if (!couldBe(imports, key, ref)) return false;
		
		TypeBinding resolvedType = ref.resolvedType;
		if (resolvedType == null) resolvedType = ref.resolveType(scope, false);
		if (resolvedType == null) return false;
		
		char[] pkg = resolvedType.qualifiedPackageName();
		char[] nm = resolvedType.qualifiedSourceName();
		int pkgFullLength = pkg.length > 0 ? pkg.length + 1: 0;
		char[] fullName = new char[pkgFullLength + nm.length];
		if(pkg.length > 0) {
			System.arraycopy(pkg, 0, fullName, 0, pkg.length);
			fullName[pkg.length] = '.';
		}
		System.arraycopy(nm, 0, fullName, pkgFullLength, nm.length);
		return matches(key, fullName);
	}
	
	public static final class Reflection {
		private static final Field initCopyField, iterableCopyField;
		
		static {
			Field a = null, b = null;
			
			try {
				a = Permit.getField(LocalDeclaration.class, "$initCopy");
				b = Permit.getField(LocalDeclaration.class, "$iterableCopy");
			} catch (Throwable t) {
				//ignore - no $initCopy exists when running in ecj.
			}
			
			initCopyField = a;
			iterableCopyField = b;
		}
	}
	public static boolean handleValForLocalDeclaration(LocalDeclaration local, BlockScope scope) {
		if (local == null || !LocalDeclaration.class.equals(local.getClass())) return false;
		boolean decomponent = false;
		
		boolean val = isVal(local, scope);
		boolean var = isVar(local, scope);
		if (!(val || var)) return false;
		
		if (val) {
			StackTraceElement[] st = new Throwable().getStackTrace();
			for (int i = 0; i < st.length - 2 && i < 10; i++) {
				if (st[i].getClassName().equals("lombok.launch.PatchFixesHider$Val")) {
					boolean valInForStatement = 
						st[i + 1].getClassName().equals("org.eclipse.jdt.internal.compiler.ast.LocalDeclaration") &&
						st[i + 2].getClassName().equals("org.eclipse.jdt.internal.compiler.ast.ForStatement");
					if (valInForStatement) return false;
					break;
				}
			}
		}
		
		Expression init = local.initialization;
		if (init == null && Reflection.initCopyField != null) {
			try {
				init = (Expression) Reflection.initCopyField.get(local);
			} catch (Exception e) {
				// init remains null.
			}
		}
		
		if (init == null && Reflection.iterableCopyField != null) {
			try {
				init = (Expression) Reflection.iterableCopyField.get(local);
				decomponent = true;
			} catch (Exception e) {
				// init remains null.
			}
		}
		
		TypeReference replacement = null;
		
		// Java 10+: Lombok uses the native 'var' support and transforms 'val' to 'final var'.
		if (hasNativeVarSupport(scope) && val) {
			replacement = new SingleTypeReference("var".toCharArray(), pos(local.type));
			local.initialization = init;
			init = null;
		}
		
		if (init != null) {
			if (init.getClass().getName().equals("org.eclipse.jdt.internal.compiler.ast.LambdaExpression")) {
				return false;
			}
			
			TypeBinding resolved = null;
			try {
				resolved = decomponent ? getForEachComponentType(init, scope) : resolveForExpression(init, scope);
			} catch (NullPointerException e) {
				// This definitely occurs if as part of resolving the initializer expression, a
				// lambda expression in it must also be resolved (such as when lambdas are part of
				// a ternary expression). This can't result in a viable 'val' matching, so, we
				// just go with 'Object' and let the IDE print the appropriate errors.
				resolved = null;
			}
			
			if (resolved == null) {
				if (init instanceof ConditionalExpression) {
					ConditionalExpression cexp = (ConditionalExpression) init;
					Expression ifTrue = cexp.valueIfTrue;
					Expression ifFalse = cexp.valueIfFalse;
					TypeBinding ifTrueResolvedType = ifTrue.resolvedType;
					CompilationResult compilationResult = scope.referenceCompilationUnit().compilationResult;
					CategorizedProblem[] problems = compilationResult.problems;
					CategorizedProblem lastProblem = problems[compilationResult.problemCount - 1];
					if (ifTrueResolvedType != null && ifFalse.resolvedType == null && lastProblem.getCategoryID() == CAT_TYPE) {
						int problemCount = compilationResult.problemCount;
						for (int i = 0; i < problemCount; ++i) {
							if (problems[i] == lastProblem) {
								problems[i] = null;
								if (i + 1 < problemCount) {
									System.arraycopy(problems, i + 1, problems, i, problemCount - i + 1);
								}
								break;
							}
						}
						compilationResult.removeProblem(lastProblem);
						if (!compilationResult.hasErrors()) {
							clearIgnoreFurtherInvestigationField(scope.referenceContext());
							setValue(getField(CompilationResult.class, "hasMandatoryErrors"), compilationResult, false);
						}
						
						if (ifFalse instanceof FunctionalExpression) {
							FunctionalExpression functionalExpression = (FunctionalExpression) ifFalse;
							functionalExpression.setExpectedType(ifTrueResolvedType);
						}
						if (ifFalse.resolvedType == null) {
							resolveForExpression(ifFalse, scope);
						}
						
						resolved = ifTrueResolvedType;
					}
				}
			}
			
			if (resolved != null) {
				try {
					replacement = makeType(resolved, local.type, false);
					if (!decomponent) init.resolvedType = replacement.resolveType(scope);
				} catch (Exception e) {
					// Some type thing failed.
				}
			}
		}
		
		if (val) local.modifiers |= ClassFileConstants.AccFinal;
		local.annotations = addValAnnotation(local.annotations, local.type, scope);
		local.type = replacement != null ? replacement : new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(local.type, 3));
		return false;
	}
	
	private static boolean isVar(LocalDeclaration local, BlockScope scope) {
		return is(local.type, scope, "lombok.experimental.var") || is(local.type, scope, "lombok.var");
	}
	
	private static boolean isVal(LocalDeclaration local, BlockScope scope) {
		return is(local.type, scope, "lombok.val");
	}
	
	private static boolean hasNativeVarSupport(Scope scope) {
		long sl = scope.problemReporter().options.sourceLevel >> 16;
		long cl = scope.problemReporter().options.complianceLevel >> 16;
		if (sl == 0) sl = cl;
		if (cl == 0) cl = sl;
		return Math.min((int)(sl - 44), (int)(cl - 44)) >= 10;
	}
	
	public static boolean handleValForForEach(ForeachStatement forEach, BlockScope scope) {
		if (forEach.elementVariable == null) return false;
		
		boolean val = isVal(forEach.elementVariable, scope);
		boolean var = isVar(forEach.elementVariable, scope);
		if (!(val || var)) return false;
		
		if (hasNativeVarSupport(scope)) return false;
		
		TypeBinding component = getForEachComponentType(forEach.collection, scope);
		if (component == null) return false;
		TypeReference replacement = makeType(component, forEach.elementVariable.type, false);
		
		if (val) forEach.elementVariable.modifiers |= ClassFileConstants.AccFinal;
		forEach.elementVariable.annotations = addValAnnotation(forEach.elementVariable.annotations, forEach.elementVariable.type, scope);
		forEach.elementVariable.type = replacement != null ? replacement :
			new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(forEach.elementVariable.type, 3));
		
		return false;
	}
	
	private static Annotation[] addValAnnotation(Annotation[] originals, TypeReference originalRef, BlockScope scope) {
		Annotation[] newAnn;
		if (originals != null) {
			newAnn = new Annotation[1 + originals.length];
			System.arraycopy(originals, 0, newAnn, 0, originals.length);
		} else {
			newAnn = new Annotation[1];
		}
		
		TypeReference qualifiedTypeRef = generateQualifiedTypeRef(originalRef, originalRef.getTypeName());
		newAnn[newAnn.length - 1] = new org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation(qualifiedTypeRef, qualifiedTypeRef.sourceStart);
		
		return newAnn;
	}
	
	private static TypeBinding getForEachComponentType(Expression collection, BlockScope scope) {
		if (collection != null) {
			TypeBinding resolved = collection.resolvedType;
			if (resolved == null) resolved = resolveForExpression(collection, scope);
			if (resolved == null) return null;
			if (resolved.isArrayType()) {
				resolved = ((ArrayBinding) resolved).elementsType();
				return resolved;
			} else if (resolved instanceof ReferenceBinding) {
				ReferenceBinding iterableType = ((ReferenceBinding) resolved).findSuperTypeOriginatingFrom(TypeIds.T_JavaLangIterable, false);
				
				TypeBinding[] arguments = null;
				if (iterableType != null) switch (iterableType.kind()) {
					case Binding.GENERIC_TYPE : // for (T t : Iterable<T>) - in case used inside Iterable itself
						arguments = iterableType.typeVariables();
						break;
					case Binding.PARAMETERIZED_TYPE : // for(E e : Iterable<E>)
						arguments = ((ParameterizedTypeBinding)iterableType).arguments;
						break;
					case Binding.RAW_TYPE : // for(Object e : Iterable)
						return null;
				}
				
				if (arguments != null && arguments.length == 1) {
					return arguments[0];
				}
			}
		}
		
		return null;
	}
	
	private static TypeBinding resolveForExpression(Expression collection, BlockScope scope) {
		try {
			return collection.resolveType(scope);
		} catch (ArrayIndexOutOfBoundsException e) {
			// Known cause of issues; for example: val e = mth("X"), where mth takes 2 arguments.
			return null;
		} catch (AbortCompilation e) {
			return null;
		}
	}
	
	private static void clearIgnoreFurtherInvestigationField(ReferenceContext currentContext) {
		if (currentContext instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) currentContext;
			methodDeclaration.ignoreFurtherInvestigation = false;
		} else if (currentContext instanceof LambdaExpression) {
			LambdaExpression lambdaExpression = (LambdaExpression) currentContext;
			setValue(getField(LambdaExpression.class, "ignoreFurtherInvestigation"), lambdaExpression, false);
			
			Scope parent = lambdaExpression.enclosingScope.parent;
			while (parent != null) {
				switch(parent.kind) {
					case Scope.CLASS_SCOPE:
					case Scope.METHOD_SCOPE:
						ReferenceContext parentAST = parent.referenceContext();
						if (parentAST != lambdaExpression) {
							clearIgnoreFurtherInvestigationField(parentAST);
							return;
						}
					default:
						parent = parent.parent;
						break;
				}
			}
			
		} else if (currentContext instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) currentContext;
			typeDeclaration.ignoreFurtherInvestigation = false;
		} else if (currentContext instanceof CompilationUnitDeclaration) {
			CompilationUnitDeclaration typeDeclaration = (CompilationUnitDeclaration) currentContext;
			typeDeclaration.ignoreFurtherInvestigation = false;
		} else {
			throw new UnsupportedOperationException("clearIgnoreFurtherInvestigationField for " + currentContext.getClass());
		}
	}
	
	private static void setValue(Field field, Object object, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}
	
	private static Field getField(Class<?> clazz, String name) {
		try {
			return Permit.getField(clazz, name);
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		}
	}
}
