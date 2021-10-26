/*
 * Copyright (C) 2009-2021 The Project Lombok Authors.
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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import lombok.eclipse.Eclipse;
import lombok.javac.Javac;

public class DirectoryRunner extends Runner {
	/** Add 1 or more file names to reduce the testset to just the named file(s). No files = test it all. */
	private static final List<String> DEBUG_FOCUS_ON_FILE = Arrays.asList(
		);
	
	public enum Compiler {
		DELOMBOK {
			@Override public int getVersion() {
				return Javac.getJavaCompilerVersion();
			}
		},
		JAVAC {
			@Override public int getVersion() {
				return DELOMBOK.getVersion();
			}
		},
		ECJ {
			@Override public int getVersion() {
				String javaVersionString = System.getProperty("compiler.compliance.level");
				return javaVersionString != null ? Integer.parseInt(javaVersionString) : Eclipse.getEcjCompilerVersion();
			}
		};
		
		public abstract int getVersion();
	}
	
	public static abstract class TestParams {
		public abstract Compiler getCompiler();
		public abstract boolean printErrors();
		public abstract File getBeforeDirectory();
		public abstract File getAfterDirectory();
		public abstract File getMessagesDirectory();
		/** Version of the JDK dialect that the compiler can understand; for example, if you return '7', you should know what try-with-resources is. */
		public int getVersion() {
			return getCompiler().getVersion();
		}
		
		public boolean accept(File file) {
			return true;
		}
		
		public abstract boolean expectChanges();
		public String testNamePrefix() {
			return "";
		}
	}
	
	private static final FileFilter JAVA_FILE_FILTER = new FileFilter() {
		@Override public boolean accept(File file) {
			if (!file.isFile() || !file.getName().endsWith(".java")) return false;
			boolean positiveFilter = false;
			for (String dfof : DEBUG_FOCUS_ON_FILE) {
				if (dfof.isEmpty()) continue;
				if (!dfof.endsWith(".java")) dfof = dfof + ".java";
				boolean invert = dfof.startsWith("!");
				if (invert) dfof = dfof.substring(1);
				positiveFilter = positiveFilter || !invert;
				int starIdx = dfof.indexOf('*');
				if (starIdx == -1) {
					if (file.getName().equals(dfof)) return !invert;
				} else {
					if (file.getName().startsWith(dfof.substring(0, starIdx)) && file.getName().endsWith(dfof.substring(starIdx + 1))) return !invert;
				}
			}
			return !positiveFilter;
		}
	};
	
	private final Description description;
	private final Map<String, Description> tests = new TreeMap<String, Description>();
	private final Throwable failure;
	private final TestParams params;
	
	public DirectoryRunner(Class<?> testClass) throws Exception {
		description = Description.createSuiteDescription(testClass);
		
		this.params = (TestParams) testClass.getConstructor().newInstance();
		
		Throwable error = null;
		try {
			addTests(testClass);
		} catch (Throwable t) {
			error = t;
		}
		this.failure = error;
	}
	
	private void addTests(Class<?> testClass) throws Exception {
		for (File file : params.getBeforeDirectory().listFiles(JAVA_FILE_FILTER)) {
			if (!params.accept(file)) continue;
			Description testDescription = Description.createTestDescription(testClass, this.params.testNamePrefix() + file.getName());
			description.addChild(testDescription);
			tests.put(file.getName(), testDescription);
		}
	}
	
	@Override
	public Description getDescription() {
		return description;
	}
	
	@Override
	public void run(RunNotifier notifier) {
		if (failure != null) {
			reportInitializationFailure(notifier, description, failure);
			return;
		}
		
		for (Map.Entry<String, Description> entry : tests.entrySet()) {
			Description testDescription = entry.getValue();
			
			FileTester tester;
			try {
				tester = createTester(entry.getKey());
			} catch (IOException e) {
				reportInitializationFailure(notifier, testDescription, e);
				continue;
			}
			
			if (tester == null) {
				notifier.fireTestIgnored(testDescription);
				continue;
			}
			
			notifier.fireTestStarted(testDescription);
			try {
				tester.runTest();
			} catch (Throwable t) {
				notifier.fireTestFailure(new Failure(testDescription, t));
			}
			notifier.fireTestFinished(testDescription);
		}
	}
	
	private void reportInitializationFailure(RunNotifier notifier, Description description, Throwable throwable) {
		notifier.fireTestStarted(description);
		notifier.fireTestFailure(new Failure(description, throwable));
		notifier.fireTestFinished(description);
	}
	
	private FileTester createTester(String fileName) throws IOException {
		File file = new File(params.getBeforeDirectory(), fileName);
		switch (params.getCompiler()) {
		case DELOMBOK:
			return new RunTestsViaDelombok().createTester(params, file, "javac", params.getVersion());
		case ECJ:
			String platform = RunTestsViaEcj.eclipseAvailable() ? "eclipse" : "ecj";
			return new RunTestsViaEcj().createTester(params, file, platform, params.getVersion());
		default:
		case JAVAC:
			throw new UnsupportedOperationException();
		}
	}
	
	public interface FileTester {
		void runTest() throws Throwable;
	}
}
