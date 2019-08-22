/*
 * Copyright (C) 2009-2018 The Project Lombok Authors.
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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import lombok.DirectoryRunner.FileTester;
import lombok.core.LombokConfiguration;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.ConfigurationKeysLoader;
import lombok.core.configuration.ConfigurationResolver;
import lombok.core.configuration.ConfigurationResolverFactory;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;
import lombok.transform.TestLombokFilesIdempotent;

public abstract class AbstractRunTests {
	private final File dumpActualFilesHere;
	
	public AbstractRunTests() {
		this.dumpActualFilesHere = findPlaceToDumpActualFiles();
	}
	
	public final FileTester createTester(final DirectoryRunner.TestParams params, final File file, String platform, int version) throws IOException {
		ConfigurationKeysLoader.LoaderLoader.loadAllConfigurationKeys();
		AssertionError directiveFailure = null;
		LombokTestSource sourceDirectives = null;
		try {
			sourceDirectives = LombokTestSource.readDirectives(file);
			if (sourceDirectives.isIgnore()) return null;
			if (!sourceDirectives.versionWithinLimit(version)) return null;
			if (!sourceDirectives.runOnPlatform(platform)) return null;
		} catch (AssertionError ae) {
			directiveFailure = ae;
		}
		
		String fileName = file.getName();
		final LombokTestSource expected = LombokTestSource.read(params.getAfterDirectory(), params.getMessagesDirectory(), fileName);
		
		if (expected.isIgnore()) return null;
		if (!expected.versionWithinLimit(params.getVersion())) return null;
		if (!expected.versionWithinLimit(version)) return null;
		if (expected.isSkipIdempotent() && params instanceof TestLombokFilesIdempotent) return null;
		
		final LombokTestSource sourceDirectives_ = sourceDirectives;
		final AssertionError directiveFailure_ = directiveFailure;
		return new FileTester() {
			@Override public void runTest() throws Throwable {
				if (directiveFailure_ != null) throw directiveFailure_;
				LinkedHashSet<CompilerMessage> messages = new LinkedHashSet<CompilerMessage>();
				StringWriter writer = new StringWriter();
				
				LombokConfiguration.overrideConfigurationResolverFactory(new ConfigurationResolverFactory() {
					@Override public ConfigurationResolver createResolver(URI sourceLocation) {
						return sourceDirectives_.getConfiguration();
					}
				});
				
				boolean changed = transformCode(messages, writer, file, sourceDirectives_.getSpecifiedEncoding(), sourceDirectives_.getFormatPreferences());
				boolean forceUnchanged = sourceDirectives_.forceUnchanged() || sourceDirectives_.isSkipCompareContent();
				if (params.expectChanges() && !forceUnchanged && !changed) messages.add(new CompilerMessage(-1, -1, true, "not flagged modified"));
				if (!params.expectChanges() && changed) messages.add(new CompilerMessage(-1, -1, true, "unexpected modification"));
				
				compare(file.getName(), expected, writer.toString(), messages, params.printErrors(), sourceDirectives_.isSkipCompareContent() || expected.isSkipCompareContent());
			}
		};
	}
	
	protected abstract boolean transformCode(Collection<CompilerMessage> messages, StringWriter result, File file, String encoding, Map<String, String> formatPreferences) throws Throwable;
	
	protected String readFile(File file) throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line);
			result.append("\n");
		}
		reader.close();
		return result.toString();
	}
	
	private static File findPlaceToDumpActualFiles() {
		String location = System.getProperty("lombok.tests.dump_actual_files");
		if (location != null) {
			File dumpActualFilesHere = new File(location);
			dumpActualFilesHere.mkdirs();
			return dumpActualFilesHere;
		}
		return null;
	}
	
	private static void dumpToFile(File file, String content) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(content.getBytes("UTF-8"));
		} finally {
			fos.close();
		}
	}
	
	private static void dumpToFile(File file, Collection<CompilerMessage> content) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			for (CompilerMessage message : content) {
				fos.write(CompilerMessageMatcher.asCompilerMessageMatcher(message).toString().getBytes("UTF-8"));
				fos.write('\n');
			}
		} finally {
			fos.close();
		}
	}
	
	private void compare(String name, LombokTestSource expected, String actualFile, LinkedHashSet<CompilerMessage> actualMessages, boolean printErrors, boolean skipCompareContent) throws Throwable {
		if (!skipCompareContent) try {
			compareContent(name, expected.getContent(), actualFile);
		} catch (Throwable e) {
			if (printErrors) {
				System.out.println("***** " + name + " *****");
				System.out.println(e.getMessage());
				System.out.println("**** Expected ******");
				System.out.println(expected.getContent());
				System.out.println("****  Actual  ******");
				System.out.println(actualFile);
				if (actualMessages != null && !actualMessages.isEmpty()) {
					System.out.println("**** Actual Errors *****");
					for (CompilerMessage actualMessage : actualMessages) {
						System.out.println(actualMessage);
					}
				}
				System.out.println("*******************");
			}
			if (dumpActualFilesHere != null) {
				dumpToFile(new File(dumpActualFilesHere, name), actualFile);
			}
			throw e;
		}
		
		try {
			compareMessages(name, expected.getMessages(), actualMessages);
		} catch (Throwable e) {
			if (printErrors) {
				System.out.println("***** " + name + " *****");
				System.out.println(e.getMessage());
				System.out.println("**** Expected ******");
				for (CompilerMessageMatcher expectedMessage : expected.getMessages()) {
					System.out.println(expectedMessage);
				}
				System.out.println("****  Actual  ******");
				for (CompilerMessage actualMessage : actualMessages) {
					System.out.println(actualMessage);
				}
				System.out.println("*******************");
			}
			if (dumpActualFilesHere != null) {
				dumpToFile(new File(dumpActualFilesHere, name + ".messages"), actualMessages);
			}
			throw e;
		}
	}
	
	@SuppressWarnings("null") /* eclipse bug workaround; it falsely thinks stuffAc will always be null. */
	private static void compareMessages(String name, LombokImmutableList<CompilerMessageMatcher> expected, LinkedHashSet<CompilerMessage> actual) {
		Iterator<CompilerMessageMatcher> expectedIterator = expected.iterator();
		Iterator<CompilerMessage> actualIterator = actual.iterator();
		
		CompilerMessage stuffAc = null;
		while (true) {
			boolean exHasNext = expectedIterator.hasNext();
			boolean acHasNext = stuffAc != null || actualIterator.hasNext();
			if (!exHasNext && !acHasNext) break;
			if (exHasNext && acHasNext) {
				CompilerMessageMatcher cmm = expectedIterator.next();
				CompilerMessage cm = stuffAc == null ? actualIterator.next() : stuffAc;
				if (cmm.matches(cm)) continue;
				if (cmm.isOptional()) stuffAc = cm;
				fail(String.format("[%s] Expected message '%s' but got message '%s'", name, cmm, cm));
				throw new AssertionError("fail should have aborted already.");
			}
			
			while (expectedIterator.hasNext()) {
				CompilerMessageMatcher next = expectedIterator.next();
				if (next.isOptional()) continue;
				fail(String.format("[%s] Expected message '%s' but ran out of actual messages", name, next));
			}
			if (acHasNext) fail(String.format("[%s] Unexpected message: %s", name, actualIterator.next()));
			break;
		}
	}
	
	private static void compareContent(String name, String expectedFile, String actualFile) {
		String[] expectedLines = expectedFile.split("(\\r?\\n)");
		String[] actualLines = actualFile.split("(\\r?\\n)");
		
		for (int i = 0; i < expectedLines.length; i++) {
			if (expectedLines[i].isEmpty() || expectedLines[i].startsWith("//")) expectedLines[i] = "";
			else break;
		}
		for (int i = 0; i < actualLines.length; i++) {
			if (actualLines[i].isEmpty() || actualLines[i].startsWith("//")) actualLines[i] = "";
			else break;
		}
		expectedLines = removeBlanks(expectedLines);
		actualLines = removeBlanks(actualLines);
		
		int size = Math.min(expectedLines.length, actualLines.length);
		if (size == 0 && expectedLines.length + actualLines.length > 0) {
			Assert.fail("Missing / empty expected file: " + name);
		}
		
		for (int i = 0; i < size; i++) {
			String expected = trimRight(expectedLines[i]);
			String actual = trimRight(actualLines[i]);
			assertEquals(String.format("Difference in %s on line %d", name, i + 1), expected, actual);
		}
		if (expectedLines.length > actualLines.length) {
			fail(String.format("Missing line %d in generated %s: %s", size + 1, name, expectedLines[size]));
		}
		if (expectedLines.length < actualLines.length) {
			fail(String.format("Extra line %d in generated %s: %s", size + 1, name, actualLines[size]));
		}
	}
	
	private static String trimRight(String in) {
		int endIdx = in.length() - 1;
		while (endIdx > -1 && Character.isWhitespace(in.charAt(endIdx))) {
			endIdx--;
		}
		
		return in.substring(0, endIdx);
	}
	
	private static String[] removeBlanks(String[] in) {
		List<String> out = new ArrayList<String>();
		for (String s : in) {
			if (!s.trim().isEmpty()) out.add(s);
		}
		return out.toArray(new String[0]);
	}
}
