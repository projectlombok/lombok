/*
 * Copyright (C) 2010-2020 The Project Lombok Authors.
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

import static lombok.eclipse.handlers.EclipseHandlerUtil.getQualifiedInnerName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DeltaProcessor;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodInfo;
import org.eclipse.jdt.internal.core.SourceType;

import lombok.eclipse.EcjAugments.EclipseAugments;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseNode;

public class PatchDelegateChildren implements IPatchDelegateChildren {
	
	public Object createDelegateSourceMethod(EclipseNode typeNode, CompilationUnitDeclaration top, MethodDeclaration method) {
		String qualifiedName = new String(CharOperation.concatWith(getQualifiedInnerName(typeNode.up(), typeNode.getName().toCharArray()), '$'));
		SourceType sourceType = getSourceType(top, qualifiedName);
		return DelegateSourceMethod.forMethodDeclaration(sourceType, method);
	}
	
	public List<Object> getDelegateMethods(EclipseNode typeNode, CompilationUnitDeclaration top) {
		String qualifiedName = new String(CharOperation.concatWith(getQualifiedInnerName(typeNode.up(), typeNode.getName().toCharArray()), '$'));
		SourceType sourceType = getSourceType(top, qualifiedName);
		List delegateSourceMethods = getDelegateMethods(sourceType);
		return delegateSourceMethods;
	}
	
	public void cleanupDelegateMethods(CompilationUnitDeclaration cud) {
		CompilationUnit compilationUnit = getCompilationUnit(cud);
		if (compilationUnit != null) {
			EclipseAugments.CompilationUnit_delegateMethods.clear(compilationUnit);
		}
	}
	
	private static boolean javaModelManagerAvailable = true;
	
	public void notifyDelegateMethodsAdded(CompilationUnitDeclaration cud) {
		CompilationUnit compilationUnit = getCompilationUnit(cud);
		if (compilationUnit != null && javaModelManagerAvailable) {
			try {
				DeltaProcessor deltaProcessor = JavaModelManager.getJavaModelManager().getDeltaProcessor();
				deltaProcessor.fire(new JavaElementDelta(compilationUnit), ElementChangedEvent.POST_CHANGE);
			} catch (NoClassDefFoundError e) {
				javaModelManagerAvailable = false;
			}
		}
	}
	
	private static CompilationUnit getCompilationUnit(Object iCompilationUnit) {
		if (iCompilationUnit instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) iCompilationUnit;
			return compilationUnit.originalFromClone();
		}
		return null;
	}
	
	private static CompilationUnit getCompilationUnit(CompilationUnitDeclaration cud) {
		return getCompilationUnit(cud.compilationResult.compilationUnit);
	}
	
	private static final class DelegateSourceMethod extends SourceMethod {
		private DelegateSourceMethodInfo sourceMethodInfo;
		
		private static DelegateSourceMethod forMethodDeclaration(JavaElement parent, MethodDeclaration method) {
			Argument[] arguments = method.arguments != null ? method.arguments : new Argument[0];
			String[] parameterTypes = new String[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				parameterTypes[i] = Signature.createTypeSignature(CharOperation.concatWith(arguments[i].type.getParameterizedTypeName(), '.'), false);
			}
			return new DelegateSourceMethod(parent, new String(method.selector), parameterTypes, method);
		}
		
		private DelegateSourceMethod(JavaElement parent, String name, String[] parameterTypes, MethodDeclaration md) {
			super(parent, name, parameterTypes);
			sourceMethodInfo = new DelegateSourceMethodInfo(this, md);
		}
		
		@Override public Object getElementInfo() throws JavaModelException {
			return sourceMethodInfo;
		}
		
		/**
		 * Disable refactoring for delegate methods
		 */
		@Override public boolean isReadOnly() {
			return true;
		}
		
		/**
		 * This is required to prevent duplicate entries in the outline
		 */
		@Override public boolean equals(Object o) {
			return this == o;
		}
		
		public static final class DelegateSourceMethodInfo extends SourceMethodInfo {
			DelegateSourceMethodInfo(DelegateSourceMethod delegateSourceMethod, MethodDeclaration md) {
				int pS = md.sourceStart;
				int pE = md.sourceEnd;
				
				Argument[] methodArguments = md.arguments != null ? md.arguments : new Argument[0];
				char[][] argumentNames = new char[methodArguments.length][];
				arguments = new ILocalVariable[methodArguments.length];
				for (int i = 0; i < methodArguments.length; i++) {
					Argument argument = methodArguments[i];
					argumentNames[i] = argument.name;
					arguments[i] = new LocalVariable(delegateSourceMethod, new String(argument.name), pS, pE, pS, pS, delegateSourceMethod.getParameterTypes()[i], argument.annotations, argument.modifiers, true);
				}
				setArgumentNames(argumentNames);
				
				setSourceRangeStart(pS);
				setSourceRangeEnd(pE);
				setNameSourceStart(pS);
				setNameSourceEnd(pE);
				
				setExceptionTypeNames(CharOperation.NO_CHAR_CHAR);
				setReturnType(md.returnType == null ? new char[] {'v', 'o', 'i', 'd'} : CharOperation.concatWith(md.returnType.getParameterizedTypeName(), '.'));
				setFlags(md.modifiers);
			}
		}
	}
	
	private static List<SourceMethod> getDelegateMethods(SourceType sourceType) {
		if (sourceType != null) {
			CompilationUnit compilationUnit = getCompilationUnit(sourceType.getCompilationUnit());
			if (compilationUnit != null) {
				ConcurrentMap<String, List<SourceMethod>> map = EclipseAugments.CompilationUnit_delegateMethods.setIfAbsent(compilationUnit, new ConcurrentHashMap<String, List<SourceMethod>>());
				List<SourceMethod> newList = new ArrayList<SourceMethod>();
				List<SourceMethod> oldList = map.putIfAbsent(sourceType.getTypeQualifiedName(), newList);
				return oldList != null ? oldList : newList;
			}
		}
		return null;
	}
	
	private static SourceType getSourceType(CompilationUnitDeclaration cud, String typeName) {
		CompilationUnit compilationUnit = getCompilationUnit(cud);
		if (compilationUnit != null) {
			try {
				for (IType type : compilationUnit.getAllTypes()) {
					if (type instanceof SourceType && type.getTypeQualifiedName().equals(typeName)) {
						return (SourceType) type;
					}
				}
			} catch (JavaModelException e) {
				// Ignore
			}
		}
		return null;
	}
	
	public Set<String> findAlreadyImplementedMethods(TypeDeclaration decl) {
		Set<String> sigs = new HashSet<String>();
		for (AbstractMethodDeclaration md : decl.methods) {
			if (md.isStatic()) continue;
			if ((md.modifiers & ClassFileConstants.AccBridge) != 0) continue;
			if (md.isConstructor()) continue;
			if ((md.modifiers & ExtraCompilerModifiers.AccDefaultAbstract) != 0) continue;
			if ((md.modifiers & ClassFileConstants.AccPublic) == 0) continue;
			if ((md.modifiers & ClassFileConstants.AccSynthetic) != 0) continue;
			
			sigs.add(printSig(md, decl.scope));
		}
		return sigs;
	}
	
	private static String printSig(AbstractMethodDeclaration md, ClassScope scope) {
		StringBuilder signature = new StringBuilder();
		
		signature.append(md.selector);
		signature.append("(");
		boolean first = true;
		if (md.arguments != null) {
			TypeParameter[] typeParameters = md.typeParameters();
			Map<String, TypeParameter> typeParametersMap = new HashMap<String, TypeParameter>();
			if (typeParameters != null) {
				for (TypeParameter typeParameter : typeParameters) {
					typeParametersMap.put(new String(typeParameter.name), typeParameter);
				}
			}
			
			for (Argument argument : md.arguments) {
				TypeBinding typeBinding = makeTypeBinding(argument.type, typeParametersMap, scope);
				
				if (!first) signature.append(", ");
				first = false;
				signature.append(PatchDelegate.typeBindingToSignature(typeBinding));
			}
		}
		signature.append(")");
		
		return signature.toString();
	}

	private static TypeBinding makeTypeBinding(TypeReference typeReference, Map<String, TypeParameter> typeParametersMap, ClassScope scope) {
		char[][] typeName = typeReference.getTypeName();
		String typeNameString = Eclipse.toQualifiedName(typeName);
		
		TypeParameter typeParameter = typeParametersMap.get(typeNameString);
		if (typeParameter != null) {
			if (typeParameter.type != null) {
				typeName = typeParameter.type.getTypeName();
			} else {
				typeName = TypeConstants.JAVA_LANG_OBJECT;
			}
		}

		TypeBinding typeBinding = scope.getType(typeName, typeName.length);
		if (typeReference.dimensions() > 0) {
			typeBinding = scope.createArrayType(typeBinding, typeReference.dimensions());
		}
		return typeBinding;
	}
}
