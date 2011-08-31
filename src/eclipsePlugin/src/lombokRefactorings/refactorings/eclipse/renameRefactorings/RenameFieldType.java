package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import java.util.regex.Matcher;

import lombokRefactorings.refactorings.RefactoringFailedException;
import lombokRefactorings.regex.RefactoringRequest;
import lombokRefactorings.regex.RegexUtilities;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

@SuppressWarnings("restriction")
public class RenameFieldType extends AbstractRenameRefactoring {

	/* First parameter denotes the current name, last parameter the new name
	 * @see lombokRefactorings.refactorings.MyRefactoring#create(lombokRefactorings.regex.RefactoringRequest)
	 */
	@Override
	public void run(RefactoringRequest request) throws RefactoringFailedException {
		try {
			ICompilationUnit iCompilationUnit = request.getCompilationUnit();
			String newName = getNewName(request);
			//Find occurrence of oldName
			Matcher targetMatch = RegexUtilities.findRegex(request.getParameter(0), iCompilationUnit.getSource(), request.getOpeningTagMatcher().end(), request.getClosingTagMatcher().start());
			int targetStart = targetMatch.start();
			int targetLength = targetMatch.end() - targetMatch.start();
			ITextSelection selection = new TextSelection(targetStart,targetLength);
			IJavaElement[] elementsFound = SelectionConverter.codeResolve(iCompilationUnit, selection);
			if(elementsFound.length < 1){
				throw new RefactoringFailedException("Could not find elements at selection: "+selection); 
			}else{
				IField iField = (IField) elementsFound[0];
				RenameFieldProcessor processor = new RenameFieldProcessor(iField);
				processor.setNewElementName(newName);
				Refactoring refactoring = new RenameRefactoring(processor);
				performRefactoring(refactoring);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RefactoringFailedException(request.getRefactoringName());
		}
	}
}
