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
	
	@Test
	public void simple() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("string");
		
		RenameFieldProcessor renameFieldProcessor = new RenameFieldProcessor(field);
		renameFieldProcessor.setNewElementName("newString");
		
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
}
