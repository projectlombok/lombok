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
package lombok.eclipse.refactoring;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupBeforeAfterTest;

@RunWith(EclipseRunner.class)
public class RenameTest {
	
	@Rule
	public SetupBeforeAfterTest setup = new SetupBeforeAfterTest();
//	
	@Test
	public void simple() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		renameFieldProcessor.setRenameGetter(true);
		renameFieldProcessor.setRenameSetter(true);
		
		performRefactoring(renameFieldProcessor);
	}
	
	@Test
	public void withGetter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		renameFieldProcessor.setRenameGetter(true);
		
		performRefactoring(renameFieldProcessor);
	}
	
	@Test
	public void withGetterDifferentFile() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		renameFieldProcessor.setRenameGetter(true);
		
		performRefactoring(renameFieldProcessor);
	}
	
	@Test
	public void builderField() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		
		performRefactoring(renameFieldProcessor);
	}
	
	@Test
	public void extensionMethod() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("Extension.java");
		IType type = cu.findPrimaryType();
		IMethod method = type.getMethods()[0];
		
		RenameMethodProcessor renameMethodProcessor = new RenameNonVirtualMethodProcessor(method);
		renameMethodProcessor.setNewElementName("newTest");
		
		performRefactoring(renameMethodProcessor);
	}
	
	@Test
	public void data() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		renameFieldProcessor.setRenameGetter(true);
		renameFieldProcessor.setRenameSetter(true);
		
		performRefactoring(renameFieldProcessor);
	}
	
	@Test
	public void nestedClass() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IType nestedType = type.getType("Nested");
		IField field = nestedType.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		renameFieldProcessor.setRenameGetter(true);
		renameFieldProcessor.setRenameSetter(true);
		
		performRefactoring(renameFieldProcessor);
	}
}
