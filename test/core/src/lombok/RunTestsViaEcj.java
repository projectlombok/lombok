/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class RunTestsViaEcj extends AbstractRunTests {
	protected CompilerOptions ecjCompilerOptions() {
		CompilerOptions options = new CompilerOptions();
		options.complianceLevel = ClassFileConstants.JDK1_6;
		options.sourceLevel = ClassFileConstants.JDK1_6;
		options.targetJDK = ClassFileConstants.JDK1_6;
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
		options.docCommentSupport = false;
		Map<String, String> warnings = new HashMap<String, String>();
		warnings.put(CompilerOptions.OPTION_ReportUnusedLocal, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedLabel, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedImport, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, "ignore");
		options.set(warnings);
		return options;
	}
	
	protected IErrorHandlingPolicy ecjErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {
			@Override public boolean stopOnFirstError() {
				return true;
			}
			
			@Override public boolean proceedOnErrors() {
				return false;
			}
		};
	}
	
	@Override
	public void transformCode(final StringBuilder messages, StringWriter result, File file) throws Throwable {
		final AtomicReference<CompilationResult> compilationResult_ = new AtomicReference<CompilationResult>();
		final AtomicReference<CompilationUnitDeclaration> compilationUnit_ = new AtomicReference<CompilationUnitDeclaration>();
		ICompilerRequestor bitbucketRequestor = new ICompilerRequestor() {
			@Override public void acceptResult(CompilationResult result) {
				compilationResult_.set(result);
			}
		};
		
		List<String> classpath = new ArrayList<String>();
		classpath.addAll(Arrays.asList(System.getProperty("sun.boot.class.path").split(File.pathSeparator)));
		classpath.add("dist/lombok.jar");
		classpath.add("lib/test/commons-logging.jar");
		classpath.add("lib/test/slf4j-api.jar");
		classpath.add("lib/test/log4j.jar");
		FileSystem fileAccess = new FileSystem(classpath.toArray(new String[0]), new String[] {file.getAbsolutePath()}, "UTF-8");
		
		String source = readFile(file);
		final CompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), file.getName(), "UTF-8");
		
		Compiler ecjCompiler = new Compiler(fileAccess, ecjErrorHandlingPolicy(), ecjCompilerOptions(), bitbucketRequestor, new DefaultProblemFactory(Locale.ENGLISH)) {
			@Override protected synchronized void addCompilationUnit(ICompilationUnit inUnit, CompilationUnitDeclaration parsedUnit) {
				if (inUnit == sourceUnit) compilationUnit_.set(parsedUnit);
				super.addCompilationUnit(inUnit, parsedUnit);
			}
		};
		
		ecjCompiler.compile(new ICompilationUnit[] {sourceUnit});
		
		CompilationResult compilationResult = compilationResult_.get();
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		
		if (problems != null) for (CategorizedProblem p : problems) {
			messages.append(String.format("%d %s %s\n", p.getSourceLineNumber(), p.isError() ? "error" : p.isWarning() ? "warning" : "unknown", p.getMessage()));
		}
		
		CompilationUnitDeclaration cud = compilationUnit_.get();
		
		result.append(cud.toString());
	}
}
