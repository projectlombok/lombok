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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import lombok.Lombok;


public class PatchExtensionMethodPortal {
	private static final String TYPE_BINDING = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
	private static final String TYPE_BINDING_ARRAY = "[Lorg.eclipse.jdt.internal.compiler.lookup.TypeBinding;";
	private static final String MESSAGE_SEND = "org.eclipse.jdt.internal.compiler.ast.MessageSend";
	private static final String BLOCK_SCOPE = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
	private static final String METHOD_BINDING = "org.eclipse.jdt.internal.compiler.lookup.MethodBinding";
	private static final String PROBLEM_REPORTER = "org.eclipse.jdt.internal.compiler.problem.ProblemReporter";
	private static final String COMPLETION_PROPOSAL_COLLECTOR = "org.eclipse.jdt.ui.text.java.CompletionProposalCollector";
	private static final String I_JAVA_COMPLETION_PROPOSAL_ARRAY = "[Lorg.eclipse.jdt.ui.text.java.IJavaCompletionProposal;";
	
	public static TypeBinding resolveType(Object resolvedType, Object methodCall, Object scope) {
		try {
			return (TypeBinding) Reflection.resolveType.invoke(null, resolvedType, methodCall, scope);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return (TypeBinding) resolvedType;
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e);
		} catch (NullPointerException e) {
			e.initCause(Reflection.problem);
			throw e;
		}
	}
	
	public static void errorNoMethodFor(Object problemReporter, Object messageSend, Object recType, Object params) {
		try {
			Reflection.errorNoMethodFor.invoke(null, problemReporter, messageSend, recType, params);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		} catch (IllegalAccessException e) {
			Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			Lombok.sneakyThrow(e);
		} catch (NullPointerException e) {
			e.initCause(Reflection.problem);
			throw e;
		}
	}
	
	public static void invalidMethod(Object problemReporter, Object messageSend, Object method) {
		try {
			Reflection.invalidMethod.invoke(null, problemReporter, messageSend, method);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		} catch (IllegalAccessException e) {
			Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			Lombok.sneakyThrow(e);
		} catch (NullPointerException e) {
			e.initCause(Reflection.problem);
			throw e;
		}
	}
	
	public static Object getJavaCompletionProposals(Object[] javaCompletionProposals, Object completionProposalCollector) {
		try {
			return Reflection.getJavaCompletionProposals.invoke(null, javaCompletionProposals, completionProposalCollector);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return javaCompletionProposals;
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e);
		} catch (NullPointerException e) {
			e.initCause(Reflection.problem);
			throw e;
		}
	}

	private static final class Reflection {
		public static final Method resolveType, errorNoMethodFor, invalidMethod, getJavaCompletionProposals;
		public static final Throwable problem;
		
		static {
			Method m = null, n = null, o = null, p = null;
			Throwable problem_ = null;
			try {
				m = PatchExtensionMethod.class.getMethod("resolveType", Class.forName(TYPE_BINDING), Class.forName(MESSAGE_SEND), Class.forName(BLOCK_SCOPE));
				n = PatchExtensionMethod.class.getMethod("errorNoMethodFor", Class.forName(PROBLEM_REPORTER),
						Class.forName(MESSAGE_SEND), Class.forName(TYPE_BINDING), Class.forName(TYPE_BINDING_ARRAY));
				o = PatchExtensionMethod.class.getMethod("invalidMethod", Class.forName(PROBLEM_REPORTER), Class.forName(MESSAGE_SEND), Class.forName(METHOD_BINDING));
				p = PatchExtensionMethod.class.getMethod("getJavaCompletionProposals", Class.forName(I_JAVA_COMPLETION_PROPOSAL_ARRAY), Class.forName(COMPLETION_PROPOSAL_COLLECTOR));
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problem_ = t;
			}
			resolveType = m;
			errorNoMethodFor = n;
			invalidMethod = o;
			getJavaCompletionProposals = p;
			problem = problem_;
		}
	}
}
