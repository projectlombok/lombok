package lombokRefactorings.refactorings.eclipse.quickFix;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.surround.SurroundWithTryCatchRefactoring;

public class SurroundWithTryCatchType extends AbstractQuickFix {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		ICompilationUnit compilationUnit = request.getCompilationUnit();

		int begin = request.getOpeningTagMatcher().end();
		int length1 = request.getClosingTagMatcher().start() - begin;

		SurroundWithTryCatchRefactoring refactoring = SurroundWithTryCatchRefactoring
				.create(compilationUnit, begin, length1);

		RefactoringUtils.performRefactoring(refactoring);
	}
}
