package lombokRefactorings.refactorings.eclipse.inlineRefactorings;

import java.util.regex.Matcher;

import lombokRefactorings.refactorings.*;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
@SuppressWarnings("restriction")
public class InlineLocalVariableType extends AbstractInlineRefactoring {
	/**
	 * Performs an inline operation on local variables
	 * 
	 * @param start The start position of the name of the local variable which should be inlined
	 * @param length The length of the name of the variable which should be inlined
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	@Override
	public void run(RefactoringRequest request)
			throws RefactoringFailedException, Exception{
		Matcher targetMatch = findTarget(request);
		int selectionStart = targetMatch.start();
		int selectionLength = targetMatch.end() - targetMatch.start();
		
		System.out.println(request.getCompilationUnit().getSource().substring(targetMatch.start()-10, targetMatch.end()+10));
		
		InlineTempRefactoring refactor = new InlineTempRefactoring(request.getCompilationUnit(), selectionStart, selectionLength);
		
		RefactoringUtils.performRefactoring(refactor);	
	}
}
