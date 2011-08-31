package lombokRefactorings.refactorings.eclipse.extractRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

@SuppressWarnings("restriction")
public class ExtractSuperClassType extends
		AbstractExtractRefactoring {
	/**
	 * Performs the extract superclass refactoring.
	 * 
	 * @param members The members which should be pulled up to the new superclass
	 * @param newName The new name which should be given to the superclass
	 * @param forceRemoveMethods Boolean indicating if the methods which are pulled up should also be removed
	 * 
	 * @author SaskiaW & MaartenT
	 * 
	 * @throws CoreException
	 */
	@Override
	public void run(RefactoringRequest request) throws Exception {
		// Boolean indicating if methods should be removed from subclass
		boolean forceRemoveMethods = true;

		CodeGenerationSettings settings = JavaPreferencesSettings
				.getCodeGenerationSettings(request.getCompilationUnit().getJavaProject());
		settings.createComments = false;
		IMethod[] iMethodArray = request.findMethods().toArray(new IMethod[0]);

		
		// Creating the processor and setting all the required preferences
		ExtractSupertypeProcessor processor;
		if(iMethodArray.length == 0){
			System.err.println("No methods found to extract to superclass");
			//TODO: handle if no methods are extracted
			iMethodArray = null;
		}
		processor = new ExtractSupertypeProcessor(
				iMethodArray, settings);
		processor.setTypeName(RefactoringUtils.getNewName(request));
		//TODO: handle types to extract better
		processor.setTypesToExtract(new IType[] { request.getCompilationUnit().getTypes()[0] });
		
		processor.setCreateMethodStubs(true);

		if (forceRemoveMethods) {
			processor.setDeletedMethods(iMethodArray);
		}
		Refactoring refactoring = new ProcessorBasedRefactoring(processor);
		RefactoringUtils.performRefactoring(refactoring);
	}
}
