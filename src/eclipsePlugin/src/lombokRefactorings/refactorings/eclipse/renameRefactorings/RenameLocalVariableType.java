package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.*;

import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameLocalVariableProcessor;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.text.*;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
/**
 * Performs the RenameLocalVariable refactoring, syntax <code>RenameLocalVariable(oldName, newName)</code>.
 * 
 *
 */
@SuppressWarnings("restriction")
public class RenameLocalVariableType extends
		AbstractRenameRefactoring {
	@Override
	public void run(RefactoringRequest request) throws Exception {
		setTargetStartAndLength(request);

		ITextSelection selection = new TextSelection(targetStart, targetLength);
		LocalVariable localVar = (LocalVariable) SelectionConverter
				.codeResolve(request.getCompilationUnit(), selection)[0];
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(
				localVar);
		processor.setNewElementName(getNewName(request));
		RefactoringUtils.performRefactoring(new RenameRefactoring(processor));

	}
}
