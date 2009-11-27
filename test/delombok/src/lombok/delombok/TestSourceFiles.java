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

import lombok.ReflectionFileTester;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestSourceFiles {
	private static final String AFTER = "test/delombok/resource/after";
	private static final String BEFORE = "test/delombok/resource/before";
	
	private static final ReflectionFileTester tester = new ReflectionFileTester(BEFORE, AFTER);
	
	@BeforeClass
	public static void verify() {
		tester.verify(TestSourceFiles.class);
	}
	
	@Test
	public void testAnnotation() throws Exception {
		tester.test();
	}
	
	@Test
	public void testCast() throws Exception {
		tester.test();
	}
	
	@Test
	public void testForLoop() throws Exception {
		tester.test();
	}
	
	@Test
	public void testWithComments() throws Exception {
		tester.test();
	}
}
