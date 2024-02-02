/*
 * Copyright (C) 2010-2024 The Project Lombok Authors.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.internal.runtime.PlatformActivator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.spi.IRegistryProvider;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
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
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import lombok.eclipse.Eclipse;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class RunTestsViaEcj extends AbstractRunTests {
	protected CompilerOptions ecjCompilerOptions(TestParameters parameters) {
		CompilerOptions options = new CompilerOptions();
		Map<String, String> warnings = new HashMap<String, String>();
		
		long ecjCompilerVersionConstant = Eclipse.getLatestEcjCompilerVersionConstant();
		long ecjCompilerVersion = Eclipse.getEcjCompilerVersion();
		if (parameters.getSourceVersion() != null) {
			ecjCompilerVersionConstant = (parameters.getSourceVersion() + 44) << 16;
			ecjCompilerVersion = parameters.getSourceVersion();
		} else {
			// Preview features are only allowed if the maximum compiler version is equal to the source version
			warnings.put("org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures", "enabled");
		}
		options.complianceLevel = ecjCompilerVersionConstant;
		options.sourceLevel = ecjCompilerVersionConstant;
		options.targetJDK = ecjCompilerVersionConstant;
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
		warnings.put(CompilerOptions.OPTION_ReportUnusedLocal, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedLabel, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedImport, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, "ignore");
		warnings.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, "warning");
		warnings.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, "warning");
		warnings.put("org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures", "ignore");
		warnings.put(CompilerOptions.OPTION_Source, (ecjCompilerVersion < 9 ? "1." : "") + ecjCompilerVersion);
		warnings.put("org.eclipse.jdt.core.compiler.codegen.useStringConcatFactory", "disabled");
		options.set(warnings);
		return options;
	}
	
	protected IErrorHandlingPolicy ecjErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
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
	
	private ICompilationUnit getSourceUnit(File file, String source) {
		if (eclipseAvailable()) return new TestCompilationUnitEclipse(file.getName(), source);
		return new TestCompilationUnitEcj(file.getName(), source);
	}
	
	@Override
	public TransformationResult transformCode(File file, final TestParameters parameters) throws Throwable {
		final TransformationResult result = new TransformationResult();
		final AtomicReference<CompilationResult> compilationResult_ = new AtomicReference<CompilationResult>();
		final AtomicReference<CompilationUnitDeclaration> compilationUnit_ = new AtomicReference<CompilationUnitDeclaration>();
		ICompilerRequestor bitbucketRequestor = new ICompilerRequestor() {
			@Override public void acceptResult(CompilationResult result) {
				compilationResult_.set(result);
			}
		};
		
		String source = readFile(file);
		char[] sourceArray = source.toCharArray();
		final ICompilationUnit sourceUnit;
		try {
			sourceUnit = getSourceUnit(file, source);
		} catch (Throwable t) {
			t.printStackTrace();
			result.setChanged(false);
			return result;
		}
		
		Compiler ecjCompiler = new Compiler(createFileSystem(file), ecjErrorHandlingPolicy(), ecjCompilerOptions(parameters), bitbucketRequestor, new DefaultProblemFactory(Locale.ENGLISH)) {
			@Override protected synchronized void addCompilationUnit(ICompilationUnit inUnit, CompilationUnitDeclaration parsedUnit) {
				if (inUnit == sourceUnit) {
					compilationUnit_.set(parsedUnit);
					if (parameters.isVerifyDiet()) {
						result.setOutput(parsedUnit.toString());
					}
				}
				super.addCompilationUnit(inUnit, parsedUnit);
			}
		};
		
		// initializeEclipseBundles();
		
		ecjCompiler.compile(new ICompilationUnit[] {sourceUnit});
		
		CompilationResult compilationResult = compilationResult_.get();
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		
		if (problems != null) for (CategorizedProblem p : problems) {
			result.addMessage(new CompilerMessage(p.getSourceLineNumber(), p.getSourceStart(), p.isError(), p.getMessage()));
		}
		
		CompilationUnitDeclaration cud = compilationUnit_.get();
		
		if (cud == null) {
			result.setOutput("---- No CompilationUnit provided by ecj ----");
		} else {
			if (!parameters.isVerifyDiet()) {
				String output = cud.toString();
				// starting somewhere around ecj16, the print code is a bit too cavalier with printing modifiers.
				output = output.replace("non-sealed @val", "@val");
				result.setOutput(output);
			}
			
			if (eclipseAvailable()) {
				EclipseDomConversion.toDomAst(cud, sourceArray);
			}
		}
		
		result.setChanged(true);
		return result;
	}
	
	@SuppressWarnings("unused")
	private static class EclipseInitializer {
		static void initializeEclipseBundles() throws Exception {
			// This code does not work yet, it's research-in-progress.
			// The problem is that parts of the eclipse handler (in `PatchValEclipse` and friends) do not work unless
			// an actual eclipse exists; PatchVal causes code to run that will end up running `ResourcesPlugin.getWorkspace()`, which
			// goes down a rabbit hole of pinging off of various static fields (or `getX()` calls which return static fields), all
			// of which are `null` until the plugin they belong to is properly initialized.
			// This code is work in progress to 'hack' the initialization of each plugin one-by-one, but I doubt this is the right
			// way to do it, as I bet it's fragile (will break when eclipse updates rather easily), and who knows how many fields
			// and things need to be initialized.
			// A better plan would be to start an actual, real eclipse, by telling `EclipseStarter.startup` to launch some sort of
			// application (or at least a bunch of bundles/products/apps, including the JDT). This will then take long enough that
			// it'll need to be cached and re-used for each test or the Eclipse test run would take far too long.
			
			BundleContext context = EclipseStarter.startup(new String[0], null);
			RegistryFactory.setDefaultRegistryProvider(new IRegistryProvider() {
				private final ExtensionRegistry REG = new ExtensionRegistry(null, null, null);
				@Override public IExtensionRegistry getRegistry() {
					return REG;
				}
			});
			new Activator().start(context);
			new PlatformActivator().start(context);
			for (Bundle b : context.getBundles()) System.out.println("BUNDLE: " + b.getSymbolicName());
			new ResourcesPlugin().start(context);
			JavaModelManager.getJavaModelManager().startup();
		}
	}
	
	static boolean eclipseAvailable() {
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
			
			org.eclipse.jdt.internal.core.CompilationUnit ccu = new org.eclipse.jdt.internal.core.CompilationUnit(null, null, null) {
				@Override public char[] getContents() {
					return source;
				}
			};
			return AST.convertCompilationUnit(4, cud, options, false, ccu, 0, null);
		}
	}
	
	private FileSystem createFileSystem(File file) {
		List<String> classpath = new ArrayList<String>();
		if (new File("bin/main").exists()) classpath.add("bin/main");
		classpath.add("dist/lombok.jar");
		classpath.add("build/teststubs");
		if (bootRuntimePath == null || bootRuntimePath.isEmpty()) throw new IllegalStateException("System property delombok.bootclasspath is not set; set it to the rt of java6 or java8");
		classpath.add(bootRuntimePath);
		for (File f : new File("lib/test").listFiles()) {
			String fn = f.getName();
			if (fn.length() < 4) continue;
			if (!fn.substring(fn.length() - 4).toLowerCase().equals(".jar")) continue;
			classpath.add("lib/test/" + fn);
		}
		for (Iterator<String> i = classpath.iterator(); i.hasNext();) {
			if (FileSystem.getClasspath(i.next(), "UTF-8", null) == null) {
				i.remove();
			}
		}
		return new FileSystem(classpath.toArray(new String[0]), new String[] {file.getAbsolutePath()}, "UTF-8");
	}
	
	private static final class TestCompilationUnitEcj implements ICompilationUnit {
		private final char[] name, source, mainTypeName;
		
		TestCompilationUnitEcj(String name, String source) {
			this.source = source.toCharArray();
			this.name = name.toCharArray();
			
			char[] fileNameCharArray = getFileName();
			int start = CharOperation.lastIndexOf(File.separatorChar, fileNameCharArray) + 1;
			int end = CharOperation.lastIndexOf('.', fileNameCharArray);
			if (end == -1) {
				end = fileNameCharArray.length;
			}
			mainTypeName = CharOperation.subarray(fileNameCharArray, start, end);
		}
		
		@Override public char[] getFileName() {
			return name;
		}
		
		@Override public char[] getContents() {
			return source;
		}
		
		@Override public char[] getMainTypeName() {
			return mainTypeName;
		}
		
		@Override public char[][] getPackageName() {
			return null;
		}

		@Override public boolean ignoreOptionalProblems() {
			return false;
		}
	}
	
	private static final class TestCompilationUnitEclipse extends org.eclipse.jdt.internal.core.CompilationUnit {
		private final char[] source;
		private final char[] mainTypeName;
		
		private TestCompilationUnitEclipse(String name, String source) {
			super(null, name, DefaultWorkingCopyOwner.PRIMARY);
			this.source = source.toCharArray();
			
			char[] fileNameCharArray = getFileName();
			int start = CharOperation.lastIndexOf(File.separatorChar, fileNameCharArray) + 1;
			int end = CharOperation.lastIndexOf('.', fileNameCharArray);
			if (end == -1) {
				end = fileNameCharArray.length;
			}
			mainTypeName = CharOperation.subarray(fileNameCharArray, start, end);
		}
		
		@Override public char[] getContents() {
			return source;
		}
		
		@Override public char[] getMainTypeName() {
			return mainTypeName;
		}
		
		@Override public boolean ignoreOptionalProblems() {
			return false;
		}
		
		@Override public char[][] getPackageName() {
			return null;
		}
		
		public char[] getModuleName() {
			return null;
		}
	}
}
