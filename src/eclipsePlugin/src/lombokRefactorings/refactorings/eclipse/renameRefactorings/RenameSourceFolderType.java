package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameSourceFolderProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
/**
 * For performing the rename source folder refactoring; use <code>RenameSourceFolder(newName)</code>.
 */
@SuppressWarnings("restriction")
public class RenameSourceFolderType extends AbstractRenameRefactoring{
	@Override
	public void run(RefactoringRequest request) throws Exception {
		IPackageFragmentRoot iPackageFragmentRoot = RefactoringUtils.findPackageFragmentRoot(request.getCompilationUnit());		
		RenameSourceFolderProcessor processor = new RenameSourceFolderProcessor(iPackageFragmentRoot);		
		processor.setNewElementName(getNewName(request));
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		RefactoringUtils.performRefactoring(refactoring);
	}
	

}
