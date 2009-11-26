/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
package lombok.delombok;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import lombok.delombok.CommentPreservingParser.ParseResult;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class TestSourceFiles {
	
	private static CommentPreservingParser parser;
	
	private static final File BEFORE_FOLDER = new File("test/delombok/resource/before");
	private static final File AFTER_FOLDER = new File("test/delombok/resource/after");

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@BeforeClass
	public static void init() {
		Context c = new Context();
		Options.instance(c).put(OptionName.ENCODING, "utf-8");
		parser = new CommentPreservingParser(c);
	}
	
	@Test
	public void testSources() throws Exception {
		File[] listFiles = BEFORE_FOLDER.listFiles();
		for (File file : listFiles) {
			ParseResult parseResult = parser.parseFile(file.toString());
			StringWriter writer = new StringWriter();
			parseResult.print(writer);
			compare(file.getName(), readAfter(file), writer.toString());
		}
	}
	
	private void compare(String name, String expectedFile, String actualFile) {
		String[] expectedLines = expectedFile.split("(\\r?\\n)");
		String[] actualLines = actualFile.split("(\\r?\\n)");
		int size = Math.min(expectedLines.length, actualLines.length);
		for (int i = 0; i < size; i++) {
			String expected = expectedLines[i];
			String actual = actualLines[i];
			if (!expected.equals(actual)) {
				fail(String.format("Difference in line %s(%d):\n`%s`\n`%s`\n", name, i, expected, actual));
			}
		}
		if (expectedLines.length > actualLines.length) {
			fail(String.format("Missing line %s(%d): %s\n", name, size, expectedLines[size]));
		}
		if (expectedLines.length < actualLines.length) {
			fail(String.format("Extra line %s(%d): %s\n", name, size, actualLines[size]));
		}
	}

	private String readAfter(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(AFTER_FOLDER, file.getName())));
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line);
			result.append(LINE_SEPARATOR);
		}
		reader.close();
		return result.toString();
	}
}
