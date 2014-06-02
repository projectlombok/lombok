/*
 * Copyright (C) 2014 The Project Lombok Authors.
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
package lombok.core.configuration;

import static lombok.ConfigurationKeys.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestConfiguration {
	
	@Test
	public void testDisplayVerbose() throws Exception {
		
		@SuppressWarnings("unchecked") 
		Collection<ConfigurationKey<?>> keys = Arrays.asList(ACCESSORS_FLAG_USAGE, ACCESSORS_CHAIN, ACCESSORS_PREFIX, LOG_ANY_FIELD_NAME);
		
		String baseName = "test/configuration/resource/configurationRoot/";
		File directory = new File(baseName);
		String normalizedName = new File(directory.getAbsoluteFile().toURI().normalize()).toString().replace('\\', '/') + "/";
		Collection<String> paths = Arrays.asList(normalizedName + "d1/d11", normalizedName + "d1/d12", normalizedName + "d1/d11/d111", normalizedName + "d1/d11/d111/f1.txt");
		
		ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
		ByteArrayOutputStream rawErr = new ByteArrayOutputStream();
		PrintStream outStream = new PrintStream(rawOut);
		PrintStream errStream = new PrintStream(rawErr);
		
		int result = new ConfigurationApp().redirectOutput(outStream, errStream).display(keys, true, paths, true);
		
		outStream.flush();
		errStream.flush();
		
		String out = new String(rawOut.toByteArray()).replace("\r\n", "\n").replace('\\', '/').replaceAll(Pattern.quote(normalizedName) + "|" + Pattern.quote(baseName), "BASE/").trim();
		String err = new String(rawErr.toByteArray()).replace("\r\n", "\n").replace('\\', '/').replaceAll(Pattern.quote(normalizedName) + "|" + Pattern.quote(baseName), "BASE/").trim();
		
		checkContent(directory, out, "out");
		checkContent(directory, err, "err");
		assertEquals(0, result);
	}

	private void checkContent(File dir, String actual, String type) throws Exception {
		String expected = fileToString(new File(dir, type + ".txt")).trim();
		if (!expected.equals(actual)) {
			System.out.printf("**** Expected %s:\n", type);
			System.out.println(expected);
			System.out.printf("**** Actual %s:\n", type);
			System.out.println(actual);
			System.out.println("****");
		}
		assertEquals(expected, actual);
	}
	
	static String fileToString(File configFile) throws Exception {
		byte[] b = new byte[65536];
		FileInputStream fis = new FileInputStream(configFile);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (true) {
				int r = fis.read(b);
				if (r == -1) break;
				out.write(b, 0, r);
			}
			return new String(out.toByteArray(), "UTF-8");
		} finally {
			fis.close();
		}
	}
}
