package lombokRefactorings.refactorings;
public class RefactoringNotFoundException extends Exception {
	private static final long serialVersionUID = 5280826789498734405L;
	private String refactoringName;
	private String extraMessage = "";

	public RefactoringNotFoundException(String refactoringName) {
		this.refactoringName = refactoringName;
	}

	public String toString() {
		return ("Refactoring not (yet) supported: " + refactoringName + ". Did you specify an existing refactoring?" + extraMessage);
	}

	public String getRefactoringName() {
		return refactoringName;
	}
	
	public void addMessage(String addToMessage){
		extraMessage = extraMessage.concat("\n" + addToMessage);
	}
}