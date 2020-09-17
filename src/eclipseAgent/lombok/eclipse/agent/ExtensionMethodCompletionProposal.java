/*
 * Copyright (C) 2012 The Project Lombok Authors.
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

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccStatic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccVarargs;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.NameLookup;

public class ExtensionMethodCompletionProposal extends InternalCompletionProposal {
	private char[] fullSignature;
	private char[][] parameterNames;
	
	public ExtensionMethodCompletionProposal(final int replacementOffset) {
		super(CompletionProposal.METHOD_REF, replacementOffset - 1);
	}
	
	public void setMethodBinding(final MethodBinding method, final ASTNode node) {
		// Add proposal parameter names, sometimes its empty...
		if (method.parameterNames != null && method.parameterNames.length > 0) {
			setParameterNames(Arrays.copyOfRange(method.parameterNames, 1, method.parameterNames.length));
		} else {
			// Copy signature for parameter name resolution, this is more reliable but slower
			fullSignature = CompletionEngine.getSignature(method);
		}

		MethodBinding original = method.original();
		TypeBinding[] parameters = Arrays.copyOf(method.parameters, method.parameters.length);
		method.parameters = Arrays.copyOfRange(method.parameters, 1, method.parameters.length);

		TypeBinding[] originalParameters = null;
		if (original != method) {
			originalParameters = Arrays.copyOf(method.original().parameters, method.original().parameters.length);
			method.original().parameters = Arrays.copyOfRange(method.original().parameters, 1, method.original().parameters.length);
		}
		
		int length = method.parameters == null ? 0 : method.parameters.length;
		char[][] parameterPackageNames = new char[length][];
		char[][] parameterTypeNames = new char[length][];

		for (int i = 0; i < length; i++) {
			TypeBinding type = method.original().parameters[i];
			parameterPackageNames[i] = type.qualifiedPackageName();
			parameterTypeNames[i] = type.qualifiedSourceName();
		}
		char[] completion = CharOperation.concat(method.selector, new char[] { '(', ')' });
		setDeclarationSignature(CompletionEngine.getSignature(method.declaringClass));
		setSignature(CompletionEngine.getSignature(method));

		if (original != method) {
			setOriginalSignature(CompletionEngine.getSignature(original));
		}
		setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
		setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
		setParameterPackageNames(parameterPackageNames);
		setParameterTypeNames(parameterTypeNames);
		setPackageName(method.returnType.qualifiedPackageName());
		setTypeName(method.returnType.qualifiedSourceName());
		setName(method.selector);
		setCompletion(completion);
		setFlags(method.modifiers & (~AccStatic));
		// Remove varargs flag if it is the only parameter
		if (method.isVarargs() && length == 0) {
			setFlags(getFlags() & (~AccVarargs));
		}
		int index = node.sourceEnd + 1;
		if (node instanceof CompletionOnQualifiedNameReference) {
			index -= ((CompletionOnQualifiedNameReference) node).completionIdentifier.length;
		}
		if (node instanceof CompletionOnMemberAccess) {
			index -= ((CompletionOnMemberAccess) node).token.length;
		}
		if (node instanceof CompletionOnSingleNameReference) {
			index -= ((CompletionOnSingleNameReference) node).token.length;
		}
		setReplaceRange(index, index);
		setTokenRange(index, index);

		setRelevance(100);

		method.parameters = parameters;
		if (original != method) {
			method.original().parameters = originalParameters;
		}
	}
	
	@Override
	public char[][] findParameterNames(IProgressMonitor monitor) {
		if (parameterNames != null) {
			return parameterNames;
		}
		
		NameLookup.Answer answer = this.nameLookup.findType(
			new String(this.declarationTypeName),
			new String(this.declarationPackageName),
			false,
			NameLookup.ACCEPT_CLASSES & NameLookup.ACCEPT_INTERFACES,
			true/* consider secondary types */,
			false/* do NOT wait for indexes */,
			false/*don't check restrictions*/,
			null);
		
		if (answer != null && answer.type != null) {
			char[][] parameterTypes = Signature.getParameterTypes(fullSignature);
			
			String[] args = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				args[i] = new String(parameterTypes[i]);
			}
			IMethod method = answer.type.getMethod(new String(this.getName()), args);
			IMethod[] methods = answer.type.findMethods(method);
			if (methods != null && methods.length > 0) {
				method = methods[0];
			}
			if (method != null) {
				try {
					String[] parameterNames = method.getParameterNames();
					char[][] parameterNamesAsChar = new char[parameterNames.length - 1][];
					for (int i = 0; i < parameterNamesAsChar.length; i++) {
						parameterNamesAsChar[i] = parameterNames[i + 1].toCharArray();
					}
					setParameterNames(parameterNamesAsChar);
				} catch (JavaModelException e) {
					// Nope
				}
			}
		}
		// Seems like we failed, fallback
		if (parameterNames == null) {
			parameterNames = super.findParameterNames(monitor);
		}
		return parameterNames;
	}
	
	@Override
	public void setParameterNames(char[][] parameterNames) {
		this.parameterNames = parameterNames;
		super.setParameterNames(parameterNames);
	}
}
