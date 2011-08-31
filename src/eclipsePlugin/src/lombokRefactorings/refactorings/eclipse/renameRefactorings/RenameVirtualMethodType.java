package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * Is able to perform rename method refactoring on a virtual (extendable, so nonfinal and nonprivate) method. Will rename a nonvirtual method as well, but I don't know whether it renames non-extended classes that have the same name.
 * @author MaartenT
 *
 */
@SuppressWarnings("restriction")
public class RenameVirtualMethodType extends AbstractRenameRefactoring {
	/* (non-Javadoc)
	 * @see lombokRefactorings.refactorings.IRefactoringType#run(lombokRefactorings.regex.RefactoringRequest)
	 */
	@Override
	public void run(RefactoringRequest request) throws Exception {
		// TODO test it out
		setTargetStartAndLength(request);
		IJavaElement element = request.getCompilationUnit().getElementAt(
				targetStart);
		if (element.getElementType() == IJavaElement.METHOD) {
			IMethod method = ((IMethod) element);
			RenameMethodProcessor processor  = new RenameVirtualMethodProcessor(method);
			processor.setNewElementName(getNewName(request));
			RefactoringUtils
					.performRefactoring(new RenameRefactoring(processor));
		} else {
			System.err.println("Canceling Rename method: element "
					+ element.getElementName() + " should be a method.");
		}
	}
}
