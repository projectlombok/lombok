/*
 * Copyright (C) 2024 The Project Lombok Authors.
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
package lombok.eclipse.misc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class DelegateTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void model() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("DelegateModel.java");
		
		WorkingCopyOwner workingCopyOwner = new WorkingCopyOwner() {};
		ICompilationUnit workingCopy = cu.getWorkingCopy(workingCopyOwner, null);
		try {
			workingCopy.reconcile(ICompilationUnit.NO_AST, true, true, workingCopy.getOwner(), null);
			
			assertThat(workingCopy.findPrimaryType().getMethods().length, is(not(0)));
			
			IMethod runMethod = workingCopy.findPrimaryType().getMethods()[0];
			assertEquals(runMethod.getElementName(), "run");
			
			ISourceRange sourceRange = runMethod.getSourceRange();
			assertNotNull(sourceRange);
			assertEquals(sourceRange.getOffset(), 84);
			assertEquals(sourceRange.getLength(), 9);
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}
}
