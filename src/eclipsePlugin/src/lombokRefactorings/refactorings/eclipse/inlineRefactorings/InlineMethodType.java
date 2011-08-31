package lombokRefactorings.refactorings.eclipse.inlineRefactorings;


import java.util.regex.Matcher;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;

@SuppressWarnings("restriction")
public class InlineMethodType extends AbstractInlineRefactoring {		
	/**
	 * Performs an inline operation on methods
	 * 
	 * @param start The start of the method name which should be inlined
	 * @param length The length of the method name which should be inlined
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	@Override
	public void run(RefactoringRequest request) throws Exception {
		Matcher targetMatcher = findTarget(request);
		int start = targetMatcher.start();
		int length = targetMatcher.end() - start;
		ICompilationUnit iCompilationUnit = request.getCompilationUnit();
		InlineMethodRefactoring refactor = InlineMethodRefactoring.create(iCompilationUnit, RefactoringUtils.parse(iCompilationUnit), start,length );
		RefactoringUtils.performRefactoring(refactor);
	}

}
