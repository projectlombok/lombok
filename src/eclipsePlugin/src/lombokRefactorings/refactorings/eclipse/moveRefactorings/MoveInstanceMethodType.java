package lombokRefactorings.refactorings.eclipse.moveRefactorings;

import lombokRefactorings.refactorings.*;
import lombokRefactorings.regex.RefactoringRequest;
import lombokRefactorings.regex.RegexUtilities;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

/**
 * Performs a refactoring in which an instance method (that is, a non-static
 * method) is moved to another class.
 * 
 * @param start
 *            The starting index of the method declaration of the method which
 *            should be moved.
 * @param targetName
 *            The name of the class to which the method should be moved.
 * @author MaartenT
 * @param targetType
 * @throws CoreException
 * @see GenericMoveType
 * @see MoveStaticElementsType
 */
@SuppressWarnings("restriction")
public class MoveInstanceMethodType extends AbstractMoveRefactoring {
	/**
	 * Returns whether the name and type of the proposed target are the same as
	 * that of a given variable binding (a certain field or parameter).
	 * 
	 * @param proposedName
	 *            compare to the name of candidate
	 * @param proposedType
	 *            compare to the type of candidate
	 * @param candidate
	 *            compare to proposed name & type
	 * @return
	 */
	private static boolean matches(String proposedName, String proposedType,
			IVariableBinding candidate) {
		return candidate.getName().equals(proposedName)
				&& ((proposedType.equalsIgnoreCase("parameter")) && !candidate
						.isField())
				|| (proposedType.equalsIgnoreCase("field") && candidate
						.isField());
	}

	@Override
	public void run(RefactoringRequest request) throws Exception {
		String targetType = request.getParameter(1);
		String targetName = request.getParameter(2);
		ICompilationUnit iCompilationUnit = request.getCompilationUnit();
		int firstChar = RegexUtilities.findRegex("\\s*?([^\\s])",
				iCompilationUnit.getSource(),
				request.getOpeningTagMatcher().end(1),
				request.getClosingTagMatcher().start(1)).start();
		IMethod method;
		method = (IMethod) iCompilationUnit.getElementAt(firstChar);
		CodeGenerationSettings settings = JavaPreferencesSettings
				.getCodeGenerationSettings(iCompilationUnit.getJavaProject());
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(
				method, settings);
		processor.checkInitialConditions(new SubProgressMonitor(
				new NullProgressMonitor(), 8));
		IVariableBinding[] targets = processor.getPossibleTargets();
		for (int i = 0; i < targets.length; i++) {
			IVariableBinding candidate = targets[i];
			// System.out.println("Candidate: " + candidate);
			if (matches(targetName, targetType, candidate)) {
				IVariableBinding target = candidate;
				processor.setTarget(target);

				break;
			}
		}
		MoveRefactoring refactor = new MoveRefactoring(processor);
		RefactoringUtils.performRefactoring(refactor);
	}
}
