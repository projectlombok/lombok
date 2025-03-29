/*
 * Copyright (C) 2022-2024 The Project Lombok Authors.
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

import static org.junit.Assert.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

public class RefactoringUtils {
	public static void performRefactoring(RefactoringProcessor proccessor) throws CoreException {
		performRefactoring(new ProcessorBasedRefactoring(proccessor));
	}
	
	public static void performRefactoring(Refactoring refactoring) throws CoreException {
		CheckConditionsOperation checkConditionsOperation = new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		CreateChangeOperation change = new CreateChangeOperation(checkConditionsOperation, RefactoringStatus.FATAL);
		final PerformChangeOperation perform = new PerformChangeOperation(change);
		
		ResourcesPlugin.getWorkspace().run(perform, null);
		
		assertEquals("Condition failed", "<OK\n>", change.getConditionCheckingStatus().toString());
		assertTrue("Perform failed", perform.changeExecuted());
	}
}
