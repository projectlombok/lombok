/*
 * Copyright (C) 2022-2023 The Project Lombok Authors.
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
package lombok.eclipse;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.runner.Description;

public class SetupBeforeAfterTest extends SetupTest {
	
	private File before;
	private File after;
	
	@Override
	protected void starting(Description description) {
		super.starting(description);
		
		before = new File(root, "/before/");
		after = new File(root, "/after/");
		
		try {
			copyBeforeFiles();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void succeeded(Description description) {
		try {
			compareWithAfter();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void compareWithAfter() throws FileNotFoundException, JavaModelException {
		for (ICompilationUnit result : packageFragment.getCompilationUnits()) {
			assertEquals(getContent(new File(after, result.getElementName())), result.getSource());
		}
	}
	
	void copyBeforeFiles() throws JavaModelException, FileNotFoundException {
		for (File file : before.listFiles()) {
			createCompilationUnit(file, packageFragment);
		}
		
	}
}
