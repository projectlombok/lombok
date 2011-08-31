/**
 * 
 */
package lombokRefactorings.refactorings;

/**
 * Non-IDE-specific exception that is thrown when a refactoring can't be performed succesfully
 * @author MaartenT
 *
 */
public class RefactoringFailedException extends Exception {
	private static final long serialVersionUID = -1144430075093765093L;

	public RefactoringFailedException(String refactoringName){
		super("Could not perform exception: "+refactoringName);
	}
}