package lombokRefactorings.refactorings.eclipse.inlineRefactorings;

import java.util.regex.Matcher;

import org.eclipse.jdt.core.JavaModelException;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.regex.RefactoringRequest;
import lombokRefactorings.regex.RegexUtilities;

/**
 * Abstract class for all inline refactorings
 * @author Maarten
 *
 */
abstract class AbstractInlineRefactoring implements IRefactoringType{
	/**
	 * First parameter is always the target
	 * @param request
	 * @return
	 * @throws JavaModelException
	 */
	Matcher findTarget(RefactoringRequest request) throws JavaModelException{
		return RegexUtilities.findRegex(request.getParameter(0), request.getCompilationUnit().getSource(), request.getOpeningTagMatcher().end(), request.getClosingTagMatcher().start());
	}
}
