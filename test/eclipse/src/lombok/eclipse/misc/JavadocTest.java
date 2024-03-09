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

import static org.junit.Assert.assertThat;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.hamcrest.CoreMatchers;
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
		assertThat(getterHtmlContent, CoreMatchers.containsString("Getter section"));
		assertThat(getterHtmlContent, CoreMatchers.containsString("Returns:"));
		assertThat(getterHtmlContent, CoreMatchers.containsString("Sky is blue3"));
		
		IJavaElement setterInvocation = cu.codeSelect(320, 0)[0];
		String setterHtmlContent = JavadocContentAccess2.getHTMLContent(setterInvocation, true);
		assertThat(setterHtmlContent, CoreMatchers.containsString("Setter section"));
		assertThat(setterHtmlContent, CoreMatchers.containsString("Parameters:"));
		assertThat(setterHtmlContent, CoreMatchers.containsString("fieldName"));
		assertThat(setterHtmlContent, CoreMatchers.containsString("Hello, World3"));
		assertThat(setterHtmlContent, CoreMatchers.containsString("field"));
	}
}
