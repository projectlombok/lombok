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
package lombok;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * Simulates more or less what happens in eclipse
 */
public class RunTestsViaEclipse extends AbstractRunTests {
	protected static CompilerOptions ecjCompilerOptions() {
		CompilerOptions options = new CompilerOptions();
		options.complianceLevel = ClassFileConstants.JDK1_6;
		options.sourceLevel = ClassFileConstants.JDK1_6;
		options.targetJDK = ClassFileConstants.JDK1_6;
		options.parseLiteralExpressionsAsConstants = true;
		return options;
	}
	
	@Override public void transformCode(final StringBuilder messages, StringWriter result, File file) throws Throwable {
		// setup parser and compiler
		final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
		final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.ENGLISH);
		final INameEnvironment nameEnvironment = new LombokTestNameEnvironment();
		final ICompilerRequestor compilerRequestor = new LombokTestCompilerRequestor();
		final ISourceElementRequestor sourceElementRequestor = new LombokTestSourceElementRequestor();
		final CompilerOptions options = ecjCompilerOptions();
		final LombokTestCompiler jdtCompiler = new LombokTestCompiler(nameEnvironment, policy, options, compilerRequestor, problemFactory);
		final SourceElementParser parser = new SourceElementParser(sourceElementRequestor, problemFactory, options, true, true);
		
		// read the file
		final String source = readFile(file);
		final CompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), file.getName(), "UTF-8");
		
		
		// parse
		final CompilationUnitDeclaration cud = parser.parseCompilationUnit(sourceUnit, true, null);
		// build
		jdtCompiler.lookupEnvironment.buildTypeBindings(cud, null);
		jdtCompiler.lookupEnvironment.completeTypeBindings(cud, true);
		// process
		if (cud.scope != null) cud.scope.verifyMethods(jdtCompiler.lookupEnvironment.methodVerifier());
		
		// handle problems
		final CategorizedProblem[] problems = cud.compilationResult.getAllProblems();
		
		if (problems != null) for (CategorizedProblem p : problems) {
			messages.append(String.format("%d %s %s\n", p.getSourceLineNumber(), p.isError() ? "error" : p.isWarning() ? "warning" : "unknown", p.getMessage()));
		}
		
		// set transformed code
		result.append(cud.toString());
	}
	
	private static class LombokTestCompiler extends Compiler {
		public LombokTestCompiler(INameEnvironment nameEnvironment, IErrorHandlingPolicy policy, CompilerOptions ecjCompilerOptions, ICompilerRequestor requestor, IProblemFactory problemFactory) {
			super(nameEnvironment, policy, ecjCompilerOptions, requestor, problemFactory);
		}
		
		@Override protected void handleInternalException(Throwable e, CompilationUnitDeclaration ud, CompilationResult result) {
			throw new RuntimeException(e);
		}
		
		@Override public void beginToCompile(ICompilationUnit[] sourceUnits) {
			super.beginToCompile(sourceUnits);
		}
	};
	
	private static class LombokTestCompilerRequestor implements ICompilerRequestor {
		public void acceptResult(CompilationResult result) {
		}
	}
	
	private static class LombokTestSourceElementRequestor implements ISourceElementRequestor {

		@Override public void acceptAnnotationTypeReference(char[][] annotation, int sourceStart, int sourceEnd) {
			// do nothing
		}

		@Override public void acceptAnnotationTypeReference(char[] annotation, int sourcePosition) {
			// do nothing
		}

		@Override public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {
			// do nothing
		}

		@Override public void acceptFieldReference(char[] fieldName, int sourcePosition) {
			// do nothing
		}

		@Override public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand, int modifiers) {
			// do nothing
		}

		@Override public void acceptLineSeparatorPositions(int[] positions) {
			// do nothing
		}

		@Override public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {
			// do nothing
		}

		@Override public void acceptPackage(ImportReference importReference) {
			// do nothing
		}

		@Override public void acceptProblem(CategorizedProblem problem) {
			// do nothing
		}

		@Override public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {
			// do nothing
		}

		@Override public void acceptTypeReference(char[] typeName, int sourcePosition) {
			// do nothing
		}

		@Override public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {
			// do nothing
		}

		@Override public void acceptUnknownReference(char[] name, int sourcePosition) {
			// do nothing
		}

		@Override public void enterCompilationUnit() {
			// do nothing
		}

		@Override public void enterConstructor(MethodInfo methodInfo) {
			// do nothing
		}

		@Override public void enterField(FieldInfo fieldInfo) {
			// do nothing
		}

		@Override public void enterInitializer(int declarationStart, int modifiers) {
			// do nothing
		}

		@Override public void enterMethod(MethodInfo methodInfo) {
			// do nothing
		}

		@Override public void enterType(TypeInfo typeInfo) {
			// do nothing
		}

		@Override public void exitCompilationUnit(int declarationEnd) {
			// do nothing
		}

		@Override public void exitConstructor(int declarationEnd) {
			// do nothing
		}

		@Override public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
			// do nothing
		}

		@Override public void exitInitializer(int declarationEnd) {
			// do nothing
		}

		@Override public void exitMethod(int declarationEnd, Expression defaultValue) {
			// do nothing
		}

		@Override public void exitType(int declarationEnd) {
			// do nothing
		}
	}
	
	private static class LombokTestNameEnvironment implements INameEnvironment {
		Map<String, Boolean> packagesCache = new HashMap<String, Boolean>();
		
		public LombokTestNameEnvironment() {
			packagesCache.put("before", true);
		}

		public NameEnvironmentAnswer findType(final char[][] compoundTypeName) {
			final StringBuffer result = new StringBuffer();
			for (int i = 0; i < compoundTypeName.length; i++) {
				if (i != 0) {
					result.append('.');
				}
				result.append(compoundTypeName[i]);
			}
			return findType(result.toString());
		}
		
		public NameEnvironmentAnswer findType(final char[] typeName, final char[][] packageName) {
			final StringBuffer result = new StringBuffer();
			for (int i = 0; i < packageName.length; i++) {
				result.append(packageName[i]);
				result.append('.');
			}
			result.append(typeName);
			return findType(result.toString());
		}
		
		protected byte[] getClassDefinition(String name) {
			name = name.replace(".", "/") + ".class";
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
			if (is == null) {
				return null;
			}
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int count;
				while ((count = is.read(buffer, 0, buffer.length)) > 0) {
					os.write(buffer, 0, count);
				}
				return os.toByteArray();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		private NameEnvironmentAnswer findType(final String name) {
			try {
				byte[] bytes = getClassDefinition(name);
				if (bytes != null) {
					ClassFileReader classFileReader;
					classFileReader = new ClassFileReader(bytes, name.toCharArray(), true);
					return new NameEnvironmentAnswer(classFileReader, null);
				}
				return null;
			} catch (ClassFormatException e) {
				throw new RuntimeException(e);
			}
		}
		
		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
			final StringBuilder sb = new StringBuilder();
			if (parentPackageName != null) {
				for (char[] p : parentPackageName) {
					sb.append(p).append("/");
				}
			}
			sb.append(packageName);
			String name = sb.toString();
			if (packagesCache.containsKey(name)) {
				return packagesCache.get(name).booleanValue();
			}
			
			if (name.startsWith("before") || (getClassDefinition(name) != null)) {
				packagesCache.put(name, false);
				return false;
			}
			packagesCache.put(name, true);
			return true;
		}
		
		public void cleanup() {
		}
	}
}
