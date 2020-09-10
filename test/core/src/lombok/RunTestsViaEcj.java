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
package lombok;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.eclipse.Eclipse;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class RunTestsViaEcj extends AbstractRunTests {
	protected CompilerOptions ecjCompilerOptions() {
		CompilerOptions options = new CompilerOptions();
		options.complianceLevel = Eclipse.getLatestEcjCompilerVersionConstant();
		options.sourceLevel = Eclipse.getLatestEcjCompilerVersionConstant();
		options.targetJDK = Eclipse.getLatestEcjCompilerVersionConstant();
		options.docCommentSupport = false;
		options.parseLiteralExpressionsAsConstants = true;
		options.inlineJsrBytecode = true;
		options.reportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable = false;
		options.reportUnusedDeclaredThrownExceptionIncludeDocCommentReference = false;
		options.reportUnusedDeclaredThrownExceptionWhenOverriding = false;
		options.reportUnusedParameterIncludeDocCommentReference = false;
		options.reportUnusedParameterWhenImplementingAbstract = false;
		options.reportUnusedParameterWhenOverridingConcrete = false;
		options.reportDeadCodeInTrivialIfStatement = false;
		options.generateClassFiles = false;
		Map<String, String> warnings = new HashMap<String, String>();
		warnings.put(CompilerOptions.OPTION_ReportUnusedLocal, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedLabel, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedImport, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, "ignore");
		warnings.put("org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures", "enabled");
		warnings.put("org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures", "ignore");
		int ecjVersion = Eclipse.getEcjCompilerVersion();
		warnings.put(CompilerOptions.OPTION_Source, (ecjVersion < 9 ? "1." : "") + ecjVersion);
		options.set(warnings);
		return options;
	}
	
	protected IErrorHandlingPolicy ecjErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return true;
			}
			
			public boolean proceedOnErrors() {
				return false;
			}
			
			@SuppressWarnings("all") // Added to the interface in later ecj version.
			public boolean ignoreAllErrors() {
				return false;
			}
		};
	}
	
	@Override
	public boolean transformCode(Collection<CompilerMessage> messages, StringWriter result, File file, String encoding, Map<String, String> formatPreferences, int minVersion) throws Throwable {
		final AtomicReference<CompilationResult> compilationResult_ = new AtomicReference<CompilationResult>();
		final AtomicReference<CompilationUnitDeclaration> compilationUnit_ = new AtomicReference<CompilationUnitDeclaration>();
		ICompilerRequestor bitbucketRequestor = new ICompilerRequestor() {
			@Override public void acceptResult(CompilationResult result) {
				compilationResult_.set(result);
			}
		};
		
		String source = readFile(file);
		char[] sourceArray = source.toCharArray();
		final org.eclipse.jdt.internal.compiler.batch.CompilationUnit sourceUnit = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(sourceArray, file.getName(), encoding == null ? "UTF-8" : encoding);
		
		Compiler ecjCompiler = new Compiler(createFileSystem(file, minVersion), ecjErrorHandlingPolicy(), ecjCompilerOptions(), bitbucketRequestor, new DefaultProblemFactory(Locale.ENGLISH)) {
			@Override protected synchronized void addCompilationUnit(ICompilationUnit inUnit, CompilationUnitDeclaration parsedUnit) {
				if (inUnit == sourceUnit) compilationUnit_.set(parsedUnit);
				super.addCompilationUnit(inUnit, parsedUnit);
			}
		};
		
		ecjCompiler.compile(new ICompilationUnit[] {sourceUnit});
		
		CompilationResult compilationResult = compilationResult_.get();
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		
		if (problems != null) for (CategorizedProblem p : problems) {
			messages.add(new CompilerMessage(p.getSourceLineNumber(), p.getSourceStart(), p.isError(), p.getMessage()));
		}
		
		CompilationUnitDeclaration cud = compilationUnit_.get();
		
		if (cud == null) result.append("---- No CompilationUnit provided by ecj ----");
		else result.append(cud.toString());
		
		if (eclipseAvailable()) {
			EclipseDomConversion.toDomAst(cud, sourceArray);
		}
		
		return true;
	}
	
	private boolean eclipseAvailable() {
		try {
			Class.forName("org.eclipse.jdt.core.dom.CompilationUnit");
		} catch (Throwable t) {
			return false;
		}
		
		return true;
	}
	
	private static final String bootRuntimePath = System.getProperty("delombok.bootclasspath");
	
	private static class EclipseDomConversion {
		static CompilationUnit toDomAst(CompilationUnitDeclaration cud, final char[] source) {
			Map<String, String> options = new HashMap<String, String>();
			options.put(JavaCore.COMPILER_SOURCE, "11");
			options.put("org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures", "enabled");
			try {
				org.eclipse.jdt.internal.core.CompilationUnit ccu = new org.eclipse.jdt.internal.core.CompilationUnit(null, null, null) {
					@Override public char[] getContents() {
						return source;
					}
				};
				return AST.convertCompilationUnit(4, cud, options, false, ccu, 0, null);
			} catch (SecurityException e) {
				try {
					debugClasspathConflicts("org/eclipse/jdt/internal/compiler");
				} catch (Exception e2) {
					throw Lombok.sneakyThrow(e2);
				}
				throw e;
			}
		}
	}
	
	@SuppressWarnings({"all"})
	private static void debugClasspathConflicts(String prefixToLookFor) throws Exception {
		String[] paths = System.getProperty("java.class.path").split(":");
		for (String p : paths) {
			Path cp = Paths.get(p);
			if (Files.isDirectory(cp)) {
				if (Files.isDirectory(cp.resolve(prefixToLookFor))) System.out.println("** DIR-BASED: " + cp);
			} else if (Files.isRegularFile(cp)) {
				JarFile jf = new JarFile(cp.toFile());
				try {
					Enumeration<JarEntry> jes = jf.entries();
					while (jes.hasMoreElements()) {
						JarEntry je = jes.nextElement();
						if (je.getName().startsWith(prefixToLookFor)) {
							System.out.println("** JAR-BASED: " + cp);
							break;
						}
					}
				} finally {
					jf.close();
				}
			} else {
				System.out.println("** MISSING: " + cp);
			}
		}
	}
	
	private FileSystem createFileSystem(File file, int minVersion) {
		List<String> classpath = new ArrayList<String>();
		for (Iterator<String> i = classpath.iterator(); i.hasNext();) {
			if (FileSystem.getClasspath(i.next(), "UTF-8", null) == null) {
				i.remove();
			}
		}
		if (new File("bin").exists()) classpath.add("bin");
		classpath.add("dist/lombok.jar");
		if (bootRuntimePath == null || bootRuntimePath.isEmpty()) throw new IllegalStateException("System property delombok.bootclasspath is not set; set it to the rt of java6 or java8");
		classpath.add(bootRuntimePath);
		for (File f : new File("lib/test").listFiles()) {
			String fn = f.getName();
			if (fn.length() < 4) continue;
			if (!fn.substring(fn.length() - 4).toLowerCase().equals(".jar")) continue;
			classpath.add("lib/test/" + fn);
		}
		return new FileSystem(classpath.toArray(new String[0]), new String[] {file.getAbsolutePath()}, "UTF-8");
	}
}
