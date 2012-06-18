/*
 * Copyright (C) 2011 The Project Lombok Authors.
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

public class PatchValEclipsePortal {
	static final String LOCALDECLARATION_SIG = "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration";
	static final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
	static final String VARIABLEDECLARATIONSTATEMENT_SIG = "org.eclipse.jdt.core.dom.VariableDeclarationStatement";
	static final String SINGLEVARIABLEDECLARATION_SIG = "org.eclipse.jdt.core.dom.SingleVariableDeclaration";
	static final String ASTCONVERTER_SIG = "org.eclipse.jdt.core.dom.ASTConverter";
	
	public static void copyInitializationOfForEachIterable(Object parser) {
		try {
			Reflection.copyInitializationOfForEachIterable.invoke(null, parser);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
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
		}
	}
	
	public static void copyInitializationOfLocalDeclaration(Object parser) {
		try {
			Reflection.copyInitializationOfLocalDeclaration.invoke(null, parser);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
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
		}
	}
	
	public static void addFinalAndValAnnotationToVariableDeclarationStatement(Object converter, Object out, Object in) {
		try {
			Reflection.addFinalAndValAnnotationToVariableDeclarationStatement.invoke(null, converter, out, in);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
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
		}
	}
	
	public static void addFinalAndValAnnotationToSingleVariableDeclaration(Object converter, Object out, Object in) {
		try {
			Reflection.addFinalAndValAnnotationToSingleVariableDeclaration.invoke(null, converter, out, in);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
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
		}
	}
	
	private static final class Reflection {
		public static final Method copyInitializationOfForEachIterable;
		public static final Method copyInitializationOfLocalDeclaration;
		public static final Method addFinalAndValAnnotationToVariableDeclarationStatement;
		public static final Method addFinalAndValAnnotationToSingleVariableDeclaration;
		public static final Throwable problem;
		
		static {
			Method m = null, n = null, o = null, p = null;
			Throwable problem_ = null;
			try {
				m = PatchValEclipse.class.getMethod("copyInitializationOfForEachIterable", Class.forName(PARSER_SIG));
				n = PatchValEclipse.class.getMethod("copyInitializationOfLocalDeclaration", Class.forName(PARSER_SIG));
				o = PatchValEclipse.class.getMethod("addFinalAndValAnnotationToVariableDeclarationStatement",
						Object.class,
						Class.forName(VARIABLEDECLARATIONSTATEMENT_SIG),
						Class.forName(LOCALDECLARATION_SIG));
				p = PatchValEclipse.class.getMethod("addFinalAndValAnnotationToSingleVariableDeclaration",
						Object.class,
						Class.forName(SINGLEVARIABLEDECLARATION_SIG),
						Class.forName(LOCALDECLARATION_SIG));
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problem_ = t;
			}
			copyInitializationOfForEachIterable = m;
			copyInitializationOfLocalDeclaration = n;
			addFinalAndValAnnotationToVariableDeclarationStatement = o;
			addFinalAndValAnnotationToSingleVariableDeclaration = p;
			problem = problem_;
		}
	}
}
