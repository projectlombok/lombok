package lombokRefactorings.refactorings.eclipse.extractRefactorings;
import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;


@SuppressWarnings("restriction")
/**
 * For performing the extract method refactoring; use ExtractMethod(newName) and eveything within the opening and closing tags will be extracted.
 */
public class ExtractMethodType extends AbstractExtractRefactoring {
	@Override
	public void run(RefactoringRequest request) throws Exception {
		String newName = RefactoringUtils.getNewName(request);
		int start = request.getOpeningTagMatcher().end();
		int length = request.getClosingTagMatcher().start()
				- start;
		
		ExtractMethodRefactoring refactor = new ExtractMethodRefactoring(
				request.getCompilationUnit(), start, length);
		refactor.setMethodName(newName);
		RefactoringUtils.performRefactoring(refactor);
	}
}
