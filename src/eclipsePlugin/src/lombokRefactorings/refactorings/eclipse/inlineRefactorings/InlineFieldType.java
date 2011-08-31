package lombokRefactorings.refactorings.eclipse.inlineRefactorings;

import java.util.regex.Matcher;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineConstantRefactoring;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

/**
 * Performs an inline operation on static fields.
 * 
 * @author SaskiaW
 * 
 * @throws CoreException
 */
@SuppressWarnings("restriction")
public class InlineFieldType extends AbstractInlineRefactoring {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		Matcher targetMatcher = findTarget(request);
		int start = targetMatcher.start();
		int length = targetMatcher.end() - start;
		ICompilationUnit iCompilationUnit = request.getCompilationUnit();
		
		System.out.println(iCompilationUnit.getElementAt(start).getHandleIdentifier());
		//TODO also instance fields
		InlineConstantRefactoring refactor = new InlineConstantRefactoring(
				iCompilationUnit, RefactoringUtils.parse(iCompilationUnit),
				start, length);
		RefactoringUtils.performRefactoring(refactor);
	}
}
