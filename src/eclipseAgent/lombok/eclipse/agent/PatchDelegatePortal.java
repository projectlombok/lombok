/*
 * Copyright (C) 2012-2020 The Project Lombok Authors.
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

import lombok.Lombok;

public class PatchDelegatePortal {
	static final String CLASS_SCOPE = "org.eclipse.jdt.internal.compiler.lookup.ClassScope";
	static final String I_JAVA_ELEMENT_ARRAY = "[Lorg.eclipse.jdt.core.IJavaElement;";
	static final String SOURCE_TYPE_ELEMENT_INFO = "org.eclipse.jdt.internal.core.SourceTypeElementInfo";
	
	public static boolean handleDelegateForType(Object classScope) {
		try {
			return (Boolean) Reflection.handleDelegateForType.invoke(null, classScope);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return false;
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		} catch (NullPointerException e) {
			if (!"false".equals(System.getProperty("lombok.debug.reflection", "false"))) {
				e.initCause(Reflection.problem);
				throw e;
			}
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return false;
		}
	}
	
	public static Object[] getChildren(Object returnValue, Object javaElement) {
		try {
			return (Object[]) Reflection.getChildren.invoke(null, returnValue, javaElement);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return (Object[]) returnValue;
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		} catch (NullPointerException e) {
			if (!"false".equals(System.getProperty("lombok.debug.reflection", "false"))) {
				e.initCause(Reflection.problem);
				throw e;
			}
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return (Object[]) returnValue;
		}
	}
	
	private static final class Reflection {
		public static final Method handleDelegateForType;
		public static final Method getChildren;
		public static final Throwable problem;
		
		static {
			Method m = null, n = null;
			Throwable problem_ = null;
			try {
				m = PatchDelegate.class.getMethod("handleDelegateForType", Class.forName(CLASS_SCOPE));
				n = PatchDelegate.class.getMethod("getChildren", Class.forName(I_JAVA_ELEMENT_ARRAY), Class.forName(SOURCE_TYPE_ELEMENT_INFO));
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problem_ = t;
			}
			handleDelegateForType = m;
			getChildren = n;
			problem = problem_;
		}
	}
}
