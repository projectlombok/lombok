/*
 * Copyright (C) 2012-2021 The Project Lombok Authors.
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

import java.lang.reflect.Method;

import lombok.permit.Permit;

public class PatchDelegatePortal {
	static final String CLASS_SCOPE = "org.eclipse.jdt.internal.compiler.lookup.ClassScope";
	static final String I_JAVA_ELEMENT_ARRAY = "[Lorg.eclipse.jdt.core.IJavaElement;";
	static final String SOURCE_TYPE_ELEMENT_INFO = "org.eclipse.jdt.internal.core.SourceTypeElementInfo";
	
	public static boolean handleDelegateForType(Object classScope) {
		Boolean v = (Boolean) Permit.invokeSneaky(Reflection.problemHandleDelegate, Reflection.handleDelegateForType, null, classScope);
		if (v == null) return false;
		return v.booleanValue();
	}
	
	public static Object[] addGeneratedDelegateMethods(Object returnValue, Object javaElement) {
		return (Object[]) Permit.invokeSneaky(Reflection.problemAddGeneratedDelegateMethods, Reflection.addGeneratedDelegateMethods, null, returnValue, javaElement);
	}
	
	private static final class Reflection {
		public static final Method handleDelegateForType;
		public static final Method addGeneratedDelegateMethods;
		public static final Throwable problemHandleDelegate;
		public static final Throwable problemAddGeneratedDelegateMethods;
		
		static {
			Method m = null, n = null;
			Throwable problemHandleDelegate_ = null;
			Throwable problemAddGeneratedDelegateMethods_ = null;
			try {
				m = Permit.getMethod(PatchDelegate.class, "handleDelegateForType", Class.forName(CLASS_SCOPE));
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problemHandleDelegate_ = t;
			}
			handleDelegateForType = m;
			problemHandleDelegate = problemHandleDelegate_;
			
			try {
				n = Permit.getMethod(PatchDelegate.class, "addGeneratedDelegateMethods", Object[].class, Object.class);
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problemAddGeneratedDelegateMethods_ = t;
			}
			addGeneratedDelegateMethods = n;
			problemAddGeneratedDelegateMethods = problemAddGeneratedDelegateMethods_;
		}
	}
}
