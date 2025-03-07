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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import lombok.eclipse.cleanup.CleanupTest;
import lombok.eclipse.compile.NoErrorsTest;
import lombok.eclipse.edit.SelectTest;
import lombok.eclipse.misc.DelegateTest;
import lombok.eclipse.misc.JavadocTest;
import lombok.eclipse.refactoring.ExtractInterfaceTest;
import lombok.eclipse.refactoring.ExtractVariableTest;
import lombok.eclipse.refactoring.InlineTest;
import lombok.eclipse.refactoring.RenameTest;
import lombok.eclipse.references.FindReferencesTest;

@RunWith(Suite.class)
@SuiteClasses({ExtractInterfaceTest.class, RenameTest.class, SelectTest.class, CleanupTest.class, FindReferencesTest.class, InlineTest.class, NoErrorsTest.class, JavadocTest.class, DelegateTest.class, ExtractVariableTest.class})
public class EclipseTests {
	
}
