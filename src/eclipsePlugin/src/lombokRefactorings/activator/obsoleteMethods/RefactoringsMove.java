package lombokRefactorings.activator.obsoleteMethods;

import lombokRefactorings.refactorings.Refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.structure.*;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RefactoringsMove extends Refactorings {
	private static final int FIELD = 1;
	private static final int PARAMETER = 0;

	/**
	 * Performs a refactoring in which an instance method (that is, a non-static
	 * method) is moved to another class.
	 * 
	 * @param start
	 *            The starting index of the method declaration of the method
	 *            which should be moved.
	 * @param targetName
	 *            The name of the class to which the method should be moved.
	 * @author MaartenT
	 * @param targetType
	 * @throws CoreException
	 */
	@SuppressWarnings("all")
	public static void performMoveInstanceMethodRefactoring(int start,
			String targetType, String targetName) throws CoreException {
		IMethod method = (IMethod) iCompilationUnit.getElementAt(start);
		CodeGenerationSettings settings = JavaPreferencesSettings
				.getCodeGenerationSettings(javaProject);
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(
				method, settings);
		processor.checkInitialConditions(new SubProgressMonitor(
				new NullProgressMonitor(), 8));
		IVariableBinding[] targets = processor.getPossibleTargets();
		IVariableBinding target = null;
		for (int i = 0; i < targets.length; i++) {
			IVariableBinding candidate = targets[i];
			if (matches(targetName, targetType, candidate)) {
				target = candidate;
				processor.setTarget(target);
				MoveRefactoring refactor = new MoveRefactoring(processor);
				performRefactoring(refactor);
				return;
			}
		}
	}
	
	public static void performMoveStaticElementRefactoring(int[] starts,
			String targetType, String targetName) throws CoreException {
		//TODO:
//		MoveStaticMembersProcessor processor = new MoveStaticMembersProcessor(members, settings)
	}
	public static void performMoveField(int start){
		
	}
	/**
	 * Returns whether the name and type of the proposed target are the same as that of a given variable binding (a certain field or parameter).
	 * @param proposedName compare to the name of candidate
	 * @param proposedType compare to the type of candidate
	 * @param candidate compare to proposed name & type 
	 * @return
	 */
	private static boolean matches(String proposedName, String proposedType, IVariableBinding candidate) {
		return candidate.getName().equals(proposedName)
				&&((proposedType.equalsIgnoreCase("parameter"))  && !candidate.isField()) 
				|| (proposedType.equalsIgnoreCase("field")  	 &&  candidate.isField());
	}
}
