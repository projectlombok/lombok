/*
 * Copyright (C) 2009-2024 The Project Lombok Authors.
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.TreeScanner;

import lombok.delombok.Delombok;
import lombok.javac.CapturingDiagnosticListener;
import lombok.javac.Javac;

public class RunTestsViaDelombok extends AbstractRunTests {
	private Delombok delombok = new Delombok();
	
	@Override
	public TransformationResult transformCode(final File file, TestParameters parameters) throws Throwable {
		TransformationResult result = new TransformationResult();
		delombok.setVerbose(true);
		ChangedChecker cc = new ChangedChecker();
		delombok.setFeedback(cc.feedback);
		delombok.setForceProcess(true);
		delombok.setCharset(parameters.getEncoding() == null ? "UTF-8" : parameters.getEncoding());
		delombok.setFormatPreferences(parameters.getFormatPreferences());
		
		delombok.setDiagnosticsListener(new CapturingDiagnosticListener(file, result.getMessages()));
		
		if (parameters.isCheckPositions()) delombok.addAdditionalAnnotationProcessor(new ValidatePositionProcessor(parameters.getMinVersion()));
		delombok.addAdditionalAnnotationProcessor(new ValidateTypesProcessor());
		delombok.addAdditionalAnnotationProcessor(new ValidateNoDuplicateTreeNodeProcessor());
		
		delombok.addFile(file.getAbsoluteFile().getParentFile(), file.getName());
		delombok.setSourcepath(file.getAbsoluteFile().getParent());
		String bcp = System.getProperty("delombok.bootclasspath");
		if (bcp != null) delombok.setBootclasspath(bcp);
		StringWriter sw = new StringWriter();
		delombok.setWriter(sw);
		Locale originalLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			delombok.delombok();
			result.setOutput(sw.toString());
			result.setChanged(cc.isChanged());
			return result;
		} finally {
			Locale.setDefault(originalLocale);
		}
	}
	
	public static class ValidatePositionProcessor extends TreeProcessor {
		private final int version;
		
		public ValidatePositionProcessor(int version) {
			this.version = version;
		}
		
		private String craftFailMsg(String problematicNode, Deque<JCTree> astContext) {
			StringBuilder msg = new StringBuilder(problematicNode).append(" position of node not set: ");
			for (JCTree t : astContext) {
				msg.append("\n  ").append(t.getClass().getSimpleName());
				String asStr = t.toString();
				if (asStr.length() < 80) msg.append(": ").append(asStr);
				else if (t instanceof JCClassDecl) msg.append(": ").append(((JCClassDecl) t).name);
				else if (t instanceof JCMethodDecl) msg.append(": ").append(((JCMethodDecl) t).name);
				else if (t instanceof JCVariableDecl) msg.append(": ").append(((JCVariableDecl) t).name);
			}
			return msg.append("\n-------").toString();
		}
		
		@Override void processCompilationUnit(final JCCompilationUnit unit) {
			final Deque<JCTree> astContext = new ArrayDeque<JCTree>();
			unit.accept(new TreeScanner() {
				@Override public void scan(JCTree tree) {
					if (tree == null) return;
					if (tree instanceof JCMethodDecl && (((JCMethodDecl) tree).mods.flags & Flags.GENERATEDCONSTR) != 0) return;
					astContext.push(tree);
					try {
						if (tree instanceof JCModifiers) return;
						
						if (!Javac.validateDocComment(unit, tree)) {
							fail("Start position of doc comment (" + Javac.getDocComment(unit, tree) + ") of " + tree + " not set");
						}
						
						boolean check = true;
						if (version < 8 && tree instanceof TypeBoundKind) {
							// TypeBoundKind works differently in java6, and as a consequence,
							// the position is not set properly.
							// Given status of j6/j7, not worth properly testing.
							check = false;
						}
						if (version < 8 && tree instanceof JCIdent) {
							// explicit `super()` invocations do not appear to have end pos in j6/7.
							if ("super".equals("" + ((JCIdent) tree).name)) check = false;
						}
						
						if (tree instanceof JCVariableDecl && (((JCVariableDecl) tree).mods.flags & Javac.GENERATED_MEMBER) != 0) return;
						
						if (check && tree.pos == -1) fail(craftFailMsg("Start", astContext));
						
						if (check && Javac.getEndPosition(tree, unit) == -1) {
							fail(craftFailMsg("End", astContext));
						}
					} finally {
						try {
							super.scan(tree);
						} finally {
							astContext.pop();
						}
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
	
	public static class ValidateTypesProcessor extends TreeProcessor {
		@Override void processCompilationUnit(final JCCompilationUnit unit) {
			final Stack<JCTree> parents = new Stack<JCTree>();
			parents.add(unit);
			
			unit.accept(new TreeScanner() {
				private JCTree parent;
				@Override public void scan(JCTree tree) {
					parent = parents.peek();
					
					parents.push(tree);
					super.scan(tree);
					parents.pop();
				}
				
				@Override public void visitClassDef(JCClassDecl tree) {
					// Skip anonymous or local classes, they have no symbol
					if (!(parent instanceof JCClassDecl || parent instanceof JCCompilationUnit)) return;
					
					validateSymbol(tree, tree.sym);
					super.visitClassDef(tree);
				};

				@Override public void visitMethodDef(JCMethodDecl tree) {
					validateSymbol(tree, tree.sym);
					super.visitMethodDef(tree);
				}
				
				@Override public void visitVarDef(JCVariableDecl tree) {
					// Skip local variables
					if (!(parent instanceof JCClassDecl || parent instanceof JCMethodDecl)) return;
					
					validateSymbol(tree, tree.sym);
					super.visitVarDef(tree);
				}
				
				private void validateSymbol(JCTree tree, Symbol sym) {
					if (sym == null) {
						fail("Missing symbol for " + tree);
					}
					// Only classes have enclosed elements, skip everything else
					if (!sym.owner.getKind().isClass()) return;
					
					if (!sym.owner.getEnclosedElements().contains(sym)) {
						fail(tree + " not added to parent");
					}
				}
			});
		}
	}
	
	public static class ValidateNoDuplicateTreeNodeProcessor extends TreeProcessor {

		private String craftFailMsg(Collection<JCTree> astContext) {
			StringBuilder msg = new StringBuilder();
			for (JCTree t : astContext) {
				msg.append("\n  ").append(t.getClass().getSimpleName());
				String asStr = t.toString();
				if (asStr.length() < 80) msg.append(": ").append(asStr);
				else if (t instanceof JCClassDecl) msg.append(": ").append(((JCClassDecl) t).name);
				else if (t instanceof JCMethodDecl) msg.append(": ").append(((JCMethodDecl) t).name);
				else if (t instanceof JCVariableDecl) msg.append(": ").append(((JCVariableDecl) t).name);
			}
			return msg.append("\n-------").toString();
		}
		
		@Override
		void processCompilationUnit(JCCompilationUnit unit) {
			final Deque<JCTree> parents = new ArrayDeque<JCTree>();
			parents.add(unit);
			
			final Map<JCTree, List<JCTree>> knownTreeNode = new IdentityHashMap<JCTree, List<JCTree>>();
			
			unit.accept(new TreeScanner() {
				private JCTree parent;
				
				@Override
				public void scan(JCTree tree) {
					parent = parents.peek();
					
					if (tree == null) return;
					if (tree instanceof JCPrimitiveTypeTree) return;
					// javac generates duplicates for record members
					if (tree instanceof JCVariableDecl && (((JCVariableDecl) tree).mods.flags & Javac.GENERATED_MEMBER) != 0) return;
					
					List<JCTree> knownNodeContext = knownTreeNode.put(tree, new ArrayList<JCTree>(parents));
					if (knownNodeContext != null) {
						// javac generates two JCVariableDecl elements for 'int a, b;'
						if (parent instanceof JCVariableDecl) {
							if (tree instanceof JCModifiers) return;
							if (tree instanceof JCIdent) return;
						}
						
						fail("Node " + tree + " found twice:" + craftFailMsg(knownNodeContext) + craftFailMsg(parents));
					}
					
					parents.push(tree);
					super.scan(tree);
					parents.pop();
				}
				
				/**
				 * We always generate shallow copies for annotations
				 */
				@Override
				public void visitAnnotation(JCAnnotation tree) {
					return;
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
					// Happens if a package-info.java doesn't contain a package declaration.
					// https://github.com/projectlombok/lombok/issues/2184
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
