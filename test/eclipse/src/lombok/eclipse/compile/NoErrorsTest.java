package lombok.eclipse.compile;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class NoErrorsTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void fieldNameConstantsInAnnotation() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("Usage.java");
		
		final List<IProblem> problems = new ArrayList<IProblem>();
		final IProblemRequestor requestor = new IProblemRequestor() {
			@Override
			public void acceptProblem(IProblem problem) {
				problems.add(problem);
			}
			
			@Override
			public void beginReporting() {
				problems.clear();
			}
			
			@Override
			public void endReporting() {
			}
			
			@Override
			public boolean isActive() {
				return true;
			}
		};
		
		WorkingCopyOwner workingCopyOwner = new WorkingCopyOwner() {
			@Override
			public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
				return requestor;
			}
		};
		
		ICompilationUnit workingCopy = cu.getWorkingCopy(workingCopyOwner, null);
		try {
			workingCopy.reconcile(ICompilationUnit.NO_AST, true, true, workingCopy.getOwner(), null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
		
		System.out.println(problems);
		assertTrue(problems.isEmpty());
	}
}
