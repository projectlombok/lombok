package lombokRefactorings.refactorings.eclipse.extractRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

@SuppressWarnings("restriction")
public class ExtractSuperClassRefactoringType extends
		AbstractExtractRefactoring {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		// Boolean indicating if methods should be removed from subclass
		boolean forceRemoveMethods = true;

		CodeGenerationSettings settings = JavaPreferencesSettings
				.getCodeGenerationSettings(request.getCompilationUnit().getJavaProject());
		settings.createComments = false;

		// Creating the processor and setting all the required preferences
		ExtractSupertypeProcessor processor = new ExtractSupertypeProcessor(
				(IMember[]) request.findMethods().toArray(), settings);
		processor.setTypeName(RefactoringUtils.getNewName(request));
		processor.setMembersToMove(request.getMembersArray());
		//TODO: Is it OK to return all types?
		processor.setTypesToExtract(request.getCompilationUnit().getTypes());
		processor.setCreateMethodStubs(true);

		if (forceRemoveMethods) {
			processor.setDeletedMethods(request.getMethodsArray());
		}
		ProcessorBasedRefactoring refactoring = new ProcessorBasedRefactoring(processor);
		RefactoringUtils.performRefactoring(refactoring);
	}
}
