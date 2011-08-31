package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.refactoring.rename.*;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * Is able to perform rename method refactoring on a nonvirtual (nonextendable, so final or private) method. Will rename a virtual method as well, but I don't know whether it will rename extended methods of it. Probably not.
 * @author MaartenT
 *
 */
@SuppressWarnings("restriction")
public class RenameNonVirtualMethodType extends AbstractRenameRefactoring {
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
			RenameMethodProcessor processor  = new RenameNonVirtualMethodProcessor(method);
			processor.setNewElementName(getNewName(request));
			RefactoringUtils
					.performRefactoring(new RenameRefactoring(processor));
		} else {
			System.err.println("Canceling Rename method: element "
					+ element.getElementName() + " should be a method.");
		}
	}
}
