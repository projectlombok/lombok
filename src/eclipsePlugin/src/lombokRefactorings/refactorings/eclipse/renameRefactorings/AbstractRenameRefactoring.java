package lombokRefactorings.refactorings.eclipse.renameRefactorings;

import java.util.regex.Matcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;
import lombokRefactorings.regex.RegexUtilities;

public abstract class AbstractRenameRefactoring implements IRefactoringType{
	int targetStart;
	int targetLength;
	/**
	 * Performs the actual refactoring for the given refactoring subclass.
	 * 
	 * @param refactor The refactoring which should be performed
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	protected void performRefactoring(Refactoring refactor) throws CoreException {
		PerformRefactoringOperation op = new PerformRefactoringOperation(refactor, CheckConditionsOperation.ALL_CONDITIONS);
		op.run(null);
	}
	
	protected String getNewName(RefactoringRequest request){
		return request.getParameter(request.getParameters().size()-1);
	}
	
	protected void setTargetStartAndLength(RefactoringRequest request) throws JavaModelException {
		targetStart = RefactoringUtils.getTargetStartAndLength(request)[0];
		targetLength = RefactoringUtils.getTargetStartAndLength(request)[1];
	}
}