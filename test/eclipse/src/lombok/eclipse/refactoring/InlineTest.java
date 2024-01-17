/*
 * Copyright (C) 2023 The Project Lombok Authors.
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
package lombok.eclipse.refactoring;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupBeforeAfterTest;

@RunWith(EclipseRunner.class)
public class InlineTest {
	
	@Rule
	public SetupBeforeAfterTest setup = new SetupBeforeAfterTest();
	
	@Test
	public void getter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("InlineGetter.java");
		
		CompilationUnit compilationUnit = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(cu, true);
		
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 139, 0));
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 200, 0));
	}
	
	@Test
	public void setter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("InlineSetter.java");
		
		CompilationUnit compilationUnit = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(cu, true);
		
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 126, 0));
	}
}
