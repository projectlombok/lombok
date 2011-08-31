package lombokRefactorings.refactorings.eclipse.renameRefactorings;


import org.eclipse.jdt.internal.corext.refactoring.rename.RenameJavaProjectProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;
@SuppressWarnings("restriction")
public class RenameJavaProjectType extends AbstractRenameRefactoring {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		RenameJavaProjectProcessor proc = new RenameJavaProjectProcessor(request.getCompilationUnit().getJavaProject());
		proc.setNewElementName(getNewName(request));
		RenameRefactoring ref = new RenameRefactoring(proc);
		RefactoringUtils.performRefactoring(ref);
	}
}
