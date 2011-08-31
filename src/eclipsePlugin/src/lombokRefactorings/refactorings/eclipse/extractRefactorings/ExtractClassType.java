package lombokRefactorings.refactorings.eclipse.extractRefactorings;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.*;
import org.eclipse.jdt.internal.corext.refactoring.scripting.*;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractClassDescriptor;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;
@SuppressWarnings("restriction")
/**
 * For the extract class refactoring; use <code>ExtractClass()</code>.
 */
public class ExtractClassType extends AbstractExtractRefactoring {
	
	@Override
	public void run(RefactoringRequest request) throws Exception {
		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(request.getCompilationUnit().getJavaProject());
		settings.createComments= false;
		//TODO: Add functionality: which classes to extract, etc
		ExtractClassContribution contribution = new ExtractClassContribution();
		ExtractClassDescriptor descriptor = (ExtractClassDescriptor) contribution.createDescriptor();
		ExtractClassRefactoring refactor = new ExtractClassRefactoring(descriptor);
		RefactoringUtils.performRefactoring(refactor);
	}
}
