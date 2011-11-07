/*
 * Copyright (C) 2010 The Project Lombok Authors.
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
import java.util.Locale;

import lombok.eclipse.TransformEclipseAST;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class RunTestsViaEcj extends AbstractRunTests {
	protected static CompilerOptions ecjCompilerOptions() {
		CompilerOptions options = new CompilerOptions();
		options.complianceLevel = ClassFileConstants.JDK1_6;
		options.sourceLevel = ClassFileConstants.JDK1_6;
		options.targetJDK = ClassFileConstants.JDK1_6;
		options.parseLiteralExpressionsAsConstants = true;
		return options;
	}
	
	@Override
	public void transformCode(final StringBuilder messages, StringWriter result, File file) throws Throwable {
		ProblemReporter problemReporter = new ProblemReporter(new IErrorHandlingPolicy() {
			public boolean proceedOnErrors() {
				return true;
			}
			
			public boolean stopOnFirstError() {
				return false;
			}
		}, ecjCompilerOptions(), new DefaultProblemFactory(Locale.ENGLISH));
		
		Parser parser = new Parser(problemReporter, true);
		String source = readFile(file);
		CompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), file.getName(), "UTF-8");
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		CompilationUnitDeclaration cud = parser.parse(sourceUnit, compilationResult);
		
		TransformEclipseAST.transform(parser, cud);
		
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		
		if (problems != null) for (CategorizedProblem p : problems) {
			messages.append(String.format("%d %s %s\n", p.getSourceLineNumber(), p.isError() ? "error" : p.isWarning() ? "warning" : "unknown", p.getMessage()));
		}
		
		result.append(cud.toString());
	}
}
