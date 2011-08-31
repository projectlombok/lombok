package lombokRefactorings.refactorings.eclipse.renameRefactorings;




import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

@SuppressWarnings("restriction")
public class RenamePackageType extends AbstractRenameRefactoring {
	@Override
	public void run(RefactoringRequest request) throws Exception {
		// TODO think about this question: is it possible to have types in a compilation unit that belong to a different package?
		IPackageFragment iPackageFragment = request.getCompilationUnit().getAllTypes()[0].getPackageFragment();
		RenamePackageProcessor processor = new RenamePackageProcessor(iPackageFragment);
		processor.setNewElementName(getNewName(request));
		RefactoringUtils.performRefactoring(new RenameRefactoring(processor));
	}
}
