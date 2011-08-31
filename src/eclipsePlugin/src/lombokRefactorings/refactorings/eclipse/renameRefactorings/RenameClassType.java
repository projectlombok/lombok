package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import lombokRefactorings.refactorings.*;
import lombokRefactorings.regex.RefactoringRequest;

@SuppressWarnings("restriction")
public class RenameClassType extends AbstractRenameRefactoring {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		String newName = getNewName(request);
		ICompilationUnit compilationUnit = request.getCompilationUnit();
		// TODO test it
		// TODO make it so that main type is renamed, in the stead of the first type in the list of getTypes() (this might be always the main type though, idk)
		RenameTypeProcessor processor = new RenameTypeProcessor(compilationUnit.getTypes()[0]);
		processor.setNewElementName(newName);
		RefactoringUtils.performRefactoring((new RenameRefactoring(processor)));
		
		
		System.out.println();
		System.out.println();
		if(compilationUnit.getParent().getElementType() == IJavaElement.PACKAGE_FRAGMENT){
			compilationUnit = ((IPackageFragment) compilationUnit.getParent()).getCompilationUnit(newName+".java");
		}
		System.out.println(compilationUnit);
		System.out.println("Done");
	}
}