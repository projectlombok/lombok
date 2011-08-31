/**
 * 
 */
package lombokRefactorings.refactorings;

import lombokRefactorings.regex.RefactoringRequest;

/**
 * @author MaartenT
 *
 */
public interface IRefactoringType {
	/**
	 * Runs the refactoring by running IDE-specific commands.
 	 * @param request The refactoring request with all information relevant to the specific refactoring, most of it implicit (eg. where the tags start and end, the java project this refactoring should be performed in)
	 * @return A runnable refactoring
	 * @throws RefactoringFailedException 
	 */
	public void run(RefactoringRequest request) throws Exception;
}

