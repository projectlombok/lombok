package lombok.eclipse.refactoring;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractInterfaceProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupBeforeAfterTest;

@RunWith(EclipseRunner.class)
public class ExtractInterfaceTest {
	
	@Rule
	public SetupBeforeAfterTest setup = new SetupBeforeAfterTest();
	
	@Test
	public void simple() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		
		ExtractInterfaceProcessor extractInterfaceProcessor = new ExtractInterfaceProcessor(type, JavaPreferencesSettings.getCodeGenerationSettings(setup.getJavaProject()));
		extractInterfaceProcessor.setExtractedMembers(type.getMethods());
		extractInterfaceProcessor.setTypeName("Interface");
		
		performRefactoring(extractInterfaceProcessor);
	}
	
	@Test
	public void usage() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		IType type = cu.findPrimaryType();
		
		ExtractInterfaceProcessor extractInterfaceProcessor = new ExtractInterfaceProcessor(type, JavaPreferencesSettings.getCodeGenerationSettings(setup.getJavaProject()));
		extractInterfaceProcessor.setExtractedMembers(type.getMethods());
		extractInterfaceProcessor.setTypeName("Interface");
		extractInterfaceProcessor.setReplace(true);
		
		performRefactoring(extractInterfaceProcessor);
	}
	
}
