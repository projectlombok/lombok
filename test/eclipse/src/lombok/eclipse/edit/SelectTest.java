package lombok.eclipse.edit;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class SelectTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void builderField() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("id");
		
		ISourceRange sourceRange = field.getNameRange();
		IJavaElement[] codeSelect = cu.codeSelect(sourceRange.getOffset(), sourceRange.getLength());
		
		assertEquals(1, codeSelect.length);
		assertEquals(field, codeSelect[0]);
	}
	
	@Test
	public void superbuilderField() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		IField field = type.getField("id");
		
		ISourceRange sourceRange = field.getNameRange();
		IJavaElement[] codeSelect = cu.codeSelect(sourceRange.getOffset(), sourceRange.getLength());
		
		assertEquals(1, codeSelect.length);
		assertEquals(field, codeSelect[0]);
	}
}
