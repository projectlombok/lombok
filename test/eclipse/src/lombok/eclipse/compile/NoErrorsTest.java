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
package lombok.eclipse.compile;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class NoErrorsTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void fieldNameConstantsInAnnotation() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("Usage.java");
		
		final List<IProblem> problems = new ArrayList<IProblem>();
		final IProblemRequestor requestor = new IProblemRequestor() {
			@Override
			public void acceptProblem(IProblem problem) {
				problems.add(problem);
			}
			
			@Override
			public void beginReporting() {
				problems.clear();
			}
			
			@Override
			public void endReporting() {
			}
			
			@Override
			public boolean isActive() {
				return true;
			}
		};
		
		WorkingCopyOwner workingCopyOwner = new WorkingCopyOwner() {
			@Override
			public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
				return requestor;
			}
		};
		
		ICompilationUnit workingCopy = cu.getWorkingCopy(workingCopyOwner, null);
		try {
			workingCopy.reconcile(ICompilationUnit.NO_AST, true, true, workingCopy.getOwner(), null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
		
		System.out.println(problems);
		assertTrue(problems.isEmpty());
	}
}
