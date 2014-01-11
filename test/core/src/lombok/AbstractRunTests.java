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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import lombok.core.LombokImmutableList;
import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public abstract class AbstractRunTests {
	private final File dumpActualFilesHere;
	
	public AbstractRunTests() {
		this.dumpActualFilesHere = findPlaceToDumpActualFiles();
	}
	
	public boolean compareFile(DirectoryRunner.TestParams params, File file) throws Throwable {
		LombokTestSource sourceDirectives = LombokTestSource.readDirectives(file);
		if (sourceDirectives.isIgnore()) return false;
		
		String fileName = file.getName();
		LombokTestSource expected = LombokTestSource.read(params.getAfterDirectory(), params.getMessagesDirectory(), fileName);
		
		if (expected.isIgnore() || !expected.versionWithinLimit(params.getVersion())) return false;
		
		LinkedHashSet<CompilerMessage> messages = new LinkedHashSet<CompilerMessage>();
		StringWriter writer = new StringWriter();
		transformCode(messages, writer, file, sourceDirectives.getConfLines());
		
		compare(file.getName(), expected, writer.toString(), messages, params.printErrors());
		return true;
	}
	
	protected abstract void transformCode(Collection<CompilerMessage> messages, StringWriter result, File file, LombokImmutableList<String> confLines) throws Throwable;
	
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
	
	private void compare(String name, LombokTestSource expected, String actualFile, LinkedHashSet<CompilerMessage> actualMessages, boolean printErrors) throws Throwable {
		if (expected.getContent() != null) try {
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
	
	private static void compareMessages(String name, LombokImmutableList<CompilerMessageMatcher> expected, LinkedHashSet<CompilerMessage> actual) {
		Iterator<CompilerMessageMatcher> expectedIterator = expected.iterator();
		Iterator<CompilerMessage> actualIterator = actual.iterator();
		
		while (true) {
			boolean exHasNext = expectedIterator.hasNext();
			boolean acHasNext = actualIterator.hasNext();
			if (!exHasNext && !acHasNext) break;
			if (exHasNext && acHasNext) {
				CompilerMessageMatcher cmm = expectedIterator.next();
				CompilerMessage cm = actualIterator.next();
				if (cmm.matches(cm)) continue;
				fail(String.format("[%s] Expected message '%s' but got message '%s'", name, cmm, cm));
				throw new AssertionError("fail should have aborted already.");
			}
			if (exHasNext) fail(String.format("[%s] Expected message '%s' but ran out of actual messages", name, expectedIterator.next()));
			if (acHasNext) fail(String.format("[%s] Unexpected message: %s", name, actualIterator.next()));
			throw new AssertionError("fail should have aborted already.");
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
		if (size == 0) return;
		
		for (int i = 0; i < size; i++) {
			String expected = expectedLines[i];
			String actual = actualLines[i];
			assertEquals(String.format("Difference in %s on line %d", name, i + 1), expected, actual);
		}
		if (expectedLines.length > actualLines.length) {
			fail(String.format("Missing line %d in generated %s: %s", size + 1, name, expectedLines[size]));
		}
		if (expectedLines.length < actualLines.length) {
			fail(String.format("Extra line %d in generated %s: %s", size + 1, name, actualLines[size]));
		}
	}
	
	private static String[] removeBlanks(String[] in) {
		List<String> out = new ArrayList<String>();
		for (String s : in) {
			if (!s.trim().isEmpty()) out.add(s);
		}
		return out.toArray(new String[0]);
	}
}
