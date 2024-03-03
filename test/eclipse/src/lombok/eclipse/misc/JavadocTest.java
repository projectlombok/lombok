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

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class JavadocTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void getterSetter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("Javadoc.java");
		
		IJavaElement getterInvocation = cu.codeSelect(306, 0)[0];
		String getterHtmlContent = JavadocContentAccess2.getHTMLContent(getterInvocation, true);
		assertEquals("Getter section<dl><dt>Returns:</dt><dd> Sky is blue3</dd></dl>", getterHtmlContent);
		
		IJavaElement setterInvocation = cu.codeSelect(320, 0)[0];
		String setterHtmlContent = JavadocContentAccess2.getHTMLContent(setterInvocation, true);
		assertEquals("Setter section<dl><dt>Parameters:</dt><dd><b>fieldName</b>  Hello, World3</dd><dd><b>field</b> </dd></dl>", setterHtmlContent);
	}
}
