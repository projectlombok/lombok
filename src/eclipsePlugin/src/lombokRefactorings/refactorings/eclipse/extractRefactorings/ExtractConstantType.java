package lombokRefactorings.refactorings.eclipse.extractRefactorings;

import java.util.regex.Matcher;

import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;
/**
 * Performs the extract constant refactoring; use <code>ExtractConstant(constantToExtract, newName)</code>
 * 
 * @param start	The start of the selection which should be extracted to a constant
 * @param length The length of the selection which should be extracted to a constant
 * @param newName The name which should be given to the constant
 * 
 * @author SaskiaW
 * 
 * @throws CoreException
 */
@SuppressWarnings("restriction")
public class ExtractConstantType extends AbstractExtractRefactoring {
	@Override
	public void run(RefactoringRequest request) throws Exception {
			String newName = RefactoringUtils.getNewName(request);
			Matcher targetMatch = RefactoringUtils.findTarget(request); 
			int start = targetMatch.start();
			int length = targetMatch.end()-start; 
			
			ExtractConstantRefactoring refactor = new ExtractConstantRefactoring(request.getCompilationUnit(), start, length);
			refactor.setConstantName(newName);
			RefactoringUtils.performRefactoring(refactor);
	}

}
