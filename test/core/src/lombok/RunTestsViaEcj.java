/*
 * Copyright (C) 2010-2014 The Project Lombok Authors.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import lombok.eclipse.Eclipse;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
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
		warnings.put(CompilerOptions.OPTION_Source, "1." + Eclipse.getEcjCompilerVersion());
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
	public boolean transformCode(Collection<CompilerMessage> messages, StringWriter result, File file, String encoding, Map<String, String> formatPreferences) throws Throwable {
		final AtomicReference<CompilationResult> compilationResult_ = new AtomicReference<CompilationResult>();
		final AtomicReference<CompilationUnitDeclaration> compilationUnit_ = new AtomicReference<CompilationUnitDeclaration>();
		ICompilerRequestor bitbucketRequestor = new ICompilerRequestor() {
			@Override public void acceptResult(CompilationResult result) {
				compilationResult_.set(result);
			}
		};
		
		String source = readFile(file);
		final CompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), file.getName(), encoding == null ? "UTF-8" : encoding);
		
		Compiler ecjCompiler = new Compiler(createFileSystem(file), ecjErrorHandlingPolicy(), ecjCompilerOptions(), bitbucketRequestor, new DefaultProblemFactory(Locale.ENGLISH)) {
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
		
		return true;
	}
	
	private FileSystem createFileSystem(File file) {
		List<String> classpath = new ArrayList<String>();
		for (Iterator<String> i = classpath.iterator(); i.hasNext();) {
			if (FileSystem.getClasspath(i.next(), "UTF-8", null) == null) {
				i.remove();
			}
		}
		if (new File("bin").exists()) classpath.add("bin");
		classpath.add("dist/lombok.jar");
		classpath.add("lib/oracleJDK8Environment/rt.jar");
		classpath.add("lib/test/commons-logging-commons-logging.jar");
		classpath.add("lib/test/org.slf4j-slf4j-api.jar");
		classpath.add("lib/test/org.slf4j-slf4j-ext.jar");
		classpath.add("lib/test/log4j-log4j.jar");
		classpath.add("lib/test/org.apache.logging.log4j-log4j-api.jar");
		classpath.add("lib/test/org.jboss.logging-jboss-logging.jar");
		classpath.add("lib/test/com.google.guava-guava.jar");
		classpath.add("lib/test/com.google.code.findbugs-findbugs.jar");
		classpath.add("lib/test/com.google.flogger-flogger.jar");
		return new FileSystem(classpath.toArray(new String[0]), new String[] {file.getAbsolutePath()}, "UTF-8");
	}
}
