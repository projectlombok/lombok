/*
 * Copyright (C) 2010-2011 The Project Lombok Authors.
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

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static lombok.eclipse.Eclipse.*;

import java.lang.reflect.Field;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class PatchVal {
	
	// This is half of the work for 'val' support - the other half is in PatchValEclipse. This half is enough for ecj.
	// Creates a copy of the 'initialization' field on a LocalDeclaration if the type of the LocalDeclaration is 'val', because the completion parser will null this out,
	// which in turn stops us from inferring the intended type for 'val x = 5;'. We look at the copy.
	// Also patches local declaration to not call .resolveType() on the initializer expression if we've already done so (calling it twice causes weird errors),
	// and patches .resolve() on LocalDeclaration itself to just-in-time replace the 'val' vartype with the right one.
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
		if (expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled2(Expression expr, BlockScope scope, LocalDeclaration decl) {
		if (decl != null && LocalDeclaration.class.equals(decl.getClass()) && expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
	
	public static boolean matches(String key, char[] array) {
		if (array == null || key.length() != array.length) return false;
		for (int i = 0; i < array.length; i++) {
			if (key.charAt(i) != array[i]) return false;
		}
		
		return true;
	}
	
	private static boolean couldBeVal(TypeReference ref) {
		if (ref instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference)ref).token;
			return matches("val", token);
		}
		
		if (ref instanceof QualifiedTypeReference) {
			char[][] tokens = ((QualifiedTypeReference)ref).tokens;
			if (tokens == null || tokens.length != 2) return false;
			return matches("lombok", tokens[0]) && matches("val", tokens[1]);
		}
		
		return false;
	}
	
	private static boolean isVal(TypeReference ref, BlockScope scope) {
		if (!couldBeVal(ref)) return false;
		
		TypeBinding resolvedType = ref.resolvedType;
		if (resolvedType == null) resolvedType = ref.resolveType(scope, false);
		if (resolvedType == null) return false;
		
		char[] pkg = resolvedType.qualifiedPackageName();
		char[] nm = resolvedType.qualifiedSourceName();
		return matches("lombok", pkg) && matches("val", nm);
	}
	
	public static final class Reflection {
		private static final Field initCopyField, iterableCopyField;
		
		static {
			Field a = null, b = null;
			
			try {
				a = LocalDeclaration.class.getDeclaredField("$initCopy");
				b = LocalDeclaration.class.getDeclaredField("$iterableCopy");
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
		
		if (!isVal(local.type, scope)) return false;
		
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
		
		if (init != null) {
			TypeBinding resolved = decomponent ? getForEachComponentType(init, scope) : init.resolveType(scope);
			if (resolved != null) {
				replacement = makeType(resolved, local.type, false);
			}
		}
		
		local.modifiers |= ClassFileConstants.AccFinal;
		local.annotations = addValAnnotation(local.annotations, local.type, scope);
		local.type = replacement != null ? replacement : new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, poss(local.type, 3));
		
		return false;
	}
	
	public static boolean handleValForForEach(ForeachStatement forEach, BlockScope scope) {
		if (forEach.elementVariable == null) return false;
		
		if (!isVal(forEach.elementVariable.type, scope)) return false;
		
		TypeBinding component = getForEachComponentType(forEach.collection, scope);
		if (component == null) return false;
		TypeReference replacement = makeType(component, forEach.elementVariable.type, false);
		
		forEach.elementVariable.modifiers |= ClassFileConstants.AccFinal;
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
		
		newAnn[newAnn.length - 1] = new org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation(originalRef, originalRef.sourceStart);
		
		return newAnn;
	}
	
	private static TypeBinding getForEachComponentType(Expression collection, BlockScope scope) {
		if (collection != null) {
			TypeBinding resolved = collection.resolveType(scope);
			if (resolved == null) return null;
			if (resolved.isArrayType()) {
				resolved = ((ArrayBinding) resolved).elementsType();
				return resolved;
			} else if (resolved instanceof ReferenceBinding) {
				ReferenceBinding iterableType = ((ReferenceBinding)resolved).findSuperTypeOriginatingFrom(TypeIds.T_JavaLangIterable, false);
				
				TypeBinding[] arguments = null;
				if (iterableType != null) switch (iterableType.kind()) {
					case Binding.GENERIC_TYPE : // for (T t : Iterable<T>) - in case used inside Iterable itself
						arguments = iterableType.typeVariables();
						break;
					case Binding.PARAMETERIZED_TYPE : // for(E e : Iterable<E>)
						arguments = ((ParameterizedTypeBinding)iterableType).arguments;
						break;
				}
				
				if (arguments != null && arguments.length == 1) {
					return arguments[0];
				}
			}
		}
		
		return null;
	}
}
