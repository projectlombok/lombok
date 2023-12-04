package lombok.eclipse.refactoring;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupBeforeAfterTest;

@RunWith(EclipseRunner.class)
public class InlineTest {
	
	@Rule
	public SetupBeforeAfterTest setup = new SetupBeforeAfterTest();
	
	@Test
	public void getter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("InlineGetter.java");
		
		CompilationUnit compilationUnit = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(cu, true);
		
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 139, 0));
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 200, 0));
	}
	
	@Test
	public void setter() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("InlineSetter.java");
		
		CompilationUnit compilationUnit = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(cu, true);
		
		performRefactoring(InlineMethodRefactoring.create(cu, compilationUnit, 126, 0));
	}
}
