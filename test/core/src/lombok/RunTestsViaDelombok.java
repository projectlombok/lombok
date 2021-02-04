/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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
package lombok;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import lombok.delombok.Delombok;
import lombok.javac.CapturingDiagnosticListener;
import lombok.javac.Javac;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class RunTestsViaDelombok extends AbstractRunTests {
	private Delombok delombok = new Delombok();
	
	@Override
	public boolean transformCode(Collection<CompilerMessage> messages, StringWriter result, final File file, String encoding, Map<String, String> formatPreferences, int version) throws Throwable {
		delombok.setVerbose(true);
		ChangedChecker cc = new ChangedChecker();
		delombok.setFeedback(cc.feedback);
		delombok.setForceProcess(true);
		delombok.setCharset(encoding == null ? "UTF-8" : encoding);
		delombok.setFormatPreferences(formatPreferences);
		
		delombok.setDiagnosticsListener(new CapturingDiagnosticListener(file, messages));
		
		delombok.addAdditionalAnnotationProcessor(new ValidatePositionProcessor());
		
		delombok.addFile(file.getAbsoluteFile().getParentFile(), file.getName());
		delombok.setSourcepath(file.getAbsoluteFile().getParent());
		String bcp = System.getProperty("delombok.bootclasspath");
		if (bcp != null) delombok.setBootclasspath(bcp);
		delombok.setWriter(result);
		Locale originalLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			delombok.delombok();
			return cc.isChanged();
		} finally {
			Locale.setDefault(originalLocale);
		}
	}
	
	public static class ValidatePositionProcessor extends TreeProcessor {
		@Override void processCompilationUnit(final JCCompilationUnit unit) {
			unit.accept(new TreeScanner() {
				@Override public void scan(JCTree tree) {
					if (tree == null) return;
					if (tree instanceof JCMethodDecl && (((JCMethodDecl) tree).mods.flags & Flags.GENERATEDCONSTR) != 0) return;
					try {
						if (tree instanceof JCModifiers) return;
						
						if (!Javac.validateDocComment(unit, tree)) {
							fail("Start position of doc comment (" + Javac.getDocComment(unit, tree) + ") of " + tree + " not set");
						}
						
						if (tree.pos == -1) {
							fail("Start position of " + tree + " not set");
						}
						if (Javac.getEndPosition(tree, unit) == -1) {
							fail("End position of " + tree + " not set");
						}
					} finally {
						super.scan(tree);
					}
				}
				
				@Override public void visitMethodDef(JCMethodDecl tree) {
					super.visitMethodDef(tree);
				}
				
				@Override public void visitVarDef(JCVariableDecl tree) {
					if ((tree.mods.flags & Flags.ENUM) != 0) return;
					super.visitVarDef(tree);
				}
				
				@Override public void visitAnnotation(JCAnnotation tree) {
					scan(tree.annotationType);
					// Javac parser maps @Annotation("val") to @Annotation(value = "val") but does not add an end position for the new JCIdent...
					if (tree.args.length() == 1 && tree.args.head instanceof JCAssign && ((JCIdent)((JCAssign) tree.args.head).lhs).name.toString().equals("value")) {
						scan(((JCAssign) tree.args.head).rhs);
					} else {
						scan(tree.args);
					}
				}
			});
		}
	}
	
	public static abstract class TreeProcessor extends AbstractProcessor {
		private Trees trees;
		@Override public synchronized void init(ProcessingEnvironment processingEnv) {
			super.init(processingEnv);
			trees = Trees.instance(processingEnv);
		}
		
		@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			for (Element element : roundEnv.getRootElements()) {
				JCCompilationUnit unit = toUnit(element);
				if (unit != null) {
					processCompilationUnit(unit);
				}
			}
			return false;
		}
		
		abstract void processCompilationUnit(JCCompilationUnit unit);
		
		@Override public Set<String> getSupportedAnnotationTypes() {
			return Collections.singleton("*");
		}
		
		@Override public SourceVersion getSupportedSourceVersion() {
			return SourceVersion.latest();
		}
		
		private JCCompilationUnit toUnit(Element element) {
			TreePath path = null;
			if (trees != null) {
				try {
					path = trees.getPath(element);
				} catch (NullPointerException ignore) {
					// Happens if a package-info.java dowsn't conatin a package declaration.
					// https://github.com/rzwitserloot/lombok/issues/2184
					// We can safely ignore those, since they do not need any processing
				}
			}
			if (path == null) return null;
			
			return (JCCompilationUnit) path.getCompilationUnit();
		}
	}

	static class ChangedChecker {
		private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		private final PrintStream feedback;
		
		ChangedChecker() throws UnsupportedEncodingException {
			feedback = new PrintStream(bytes, true, "UTF-8");
		}
		
		boolean isChanged() throws UnsupportedEncodingException {
			feedback.flush();
			return bytes.toString("UTF-8").endsWith("[delomboked]\n");
		}
	}
}
