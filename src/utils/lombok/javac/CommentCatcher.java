/*
 * Copyright (C) 2011-2014 The Project Lombok Authors.
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
package lombok.javac;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import lombok.core.FieldAugment;
import lombok.permit.Permit;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

public class CommentCatcher {
	private final JavaCompiler compiler;
	public static final FieldAugment<JCCompilationUnit, List<CommentInfo>> JCCompilationUnit_comments = FieldAugment.augment(JCCompilationUnit.class, List.class, "lombok$comments");
	
	public static CommentCatcher create(Context context) {
		registerCommentsCollectingScannerFactory(context);
		JavaCompiler compiler = new JavaCompiler(context);
		
		setInCompiler(compiler, context);
		
		compiler.keepComments = true;
		compiler.genEndPos = true;
		
		return new CommentCatcher(compiler);
	}
	
	private CommentCatcher(JavaCompiler compiler) {
		this.compiler = compiler;
	}
	
	public JavaCompiler getCompiler() {
		return compiler;
	}
	
	public void setComments(JCCompilationUnit ast, List<CommentInfo> comments) {
		if (comments != null) {
			JCCompilationUnit_comments.set(ast, comments);
		} else {
			JCCompilationUnit_comments.clear(ast);
		}
	}
	
	public List<CommentInfo> getComments(JCCompilationUnit ast) {
		List<CommentInfo> list = JCCompilationUnit_comments.get(ast);
		return list == null ? Collections.<CommentInfo>emptyList() : list;
	}
	
	private static void registerCommentsCollectingScannerFactory(Context context) {
		try {
			Class<?> scannerFactory;
			int javaCompilerVersion = Javac.getJavaCompilerVersion();
			if (javaCompilerVersion <= 6) {
				scannerFactory = Class.forName("lombok.javac.java6.CommentCollectingScannerFactory");
			} else if (javaCompilerVersion == 7) {
				scannerFactory = Class.forName("lombok.javac.java7.CommentCollectingScannerFactory");
			} else {
				scannerFactory = Class.forName("lombok.javac.java8.CommentCollectingScannerFactory");
			}
			Permit.getMethod(scannerFactory, "preRegister", Context.class).invoke(null, context);
		} catch (InvocationTargetException e) {
			throw Javac.sneakyThrow(e.getCause());
		} catch (Exception e) {
			throw Javac.sneakyThrow(e);
		}
	}
	
	private static void setInCompiler(JavaCompiler compiler, Context context) {
		try {
			Class<?> parserFactory;
			int javaCompilerVersion = Javac.getJavaCompilerVersion();
			if (javaCompilerVersion <= 6) {
				parserFactory = Class.forName("lombok.javac.java6.CommentCollectingParserFactory");
			} else if (javaCompilerVersion == 7) {
				parserFactory = Class.forName("lombok.javac.java7.CommentCollectingParserFactory");
			} else if (javaCompilerVersion == 8) {
				parserFactory = Class.forName("lombok.javac.java8.CommentCollectingParserFactory");
			} else {
				parserFactory = Class.forName("lombok.javac.java9.CommentCollectingParserFactory");
			}
			Permit.getMethod(parserFactory, "setInCompiler", JavaCompiler.class, Context.class).invoke(null, compiler, context);
		} catch (InvocationTargetException e) {
			throw Javac.sneakyThrow(e.getCause());
		} catch (Exception e) {
			throw Javac.sneakyThrow(e);
		}
	}
}
