package lombok.eclipse;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

public class RefactoringUtils {
	public static void performRefactoring(RefactoringProcessor proccessor) throws CoreException {
		performRefactoring(new ProcessorBasedRefactoring(proccessor));
	}
	
	public static void performRefactoring(Refactoring refactoring) throws CoreException {
		CheckConditionsOperation checkConditionsOperation = new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		CreateChangeOperation change = new CreateChangeOperation(checkConditionsOperation, RefactoringStatus.FATAL);
		final PerformChangeOperation perform = new PerformChangeOperation(change);
		
		ResourcesPlugin.getWorkspace().run(perform, null);
		
		assertTrue("Condition failed", change.getConditionCheckingStatus().isOK());
		assertTrue("Perform failed", perform.changeExecuted());
	}
}
