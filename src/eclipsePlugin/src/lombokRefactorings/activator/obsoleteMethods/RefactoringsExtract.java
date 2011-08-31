package lombokRefactorings.activator.obsoleteMethods;

//import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import java.util.*;

import lombokRefactorings.refactorings.Refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;


public class RefactoringsExtract extends Refactorings {

	/**
	 * Performs the extract method refactoring.
	 * 
	 * @param start
	 * @param length
	 * @param newName
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	public static void performExtractMethodRefactoring(int start, int length, String newName) throws CoreException {

		ExtractMethodRefactoring refactor= new ExtractMethodRefactoring(iCompilationUnit, start, length);
		refactor.setMethodName(newName);
		performRefactoring(refactor);
	}	
	
	/**
	 * Performs the extract local variable refactoring
	 * 
	 * @param start The start of the initialization which should be extracted to a local variable.
	 * @param length The length of the initialization which should be extracted to a local variable
	 * @param newName The name which should be given to the extracted local variable
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performExtractLocalVariableRefactoring(int start, int length, String newName) throws CoreException {
		
		ExtractTempRefactoring refactor= new ExtractTempRefactoring(iCompilationUnit, start, length);
		refactor.setTempName(newName);
		performRefactoring(refactor);
	}
	/**
	 * Performs the extract constant refactoring
	 * 
	 * @param start	The start of the selection which should be extracted to a constant
	 * @param length The length of the selection which should be extracted to a constant
	 * @param newName The name which should be given to the constant
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performExtractConstantRefactoring(int start, int length, String newName) throws CoreException {
		
		ExtractConstantRefactoring refactor= new ExtractConstantRefactoring(iCompilationUnit, start, length);
		refactor.setConstantName(newName);
		performRefactoring(refactor);
	}
		
	/**
	 * Performs the extract superclass refactoring.
	 * 
	 * @param members The members which should be pulled up to the new superclass
	 * @param newName The new name which should be given to the superclass
	 * @param forceRemoveMethods Boolean indicating if the methods which are pulled up should also be removed
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performExtractSuperclassRefactoring(IMember[] members, String newName, boolean forceRemoveMethods) throws CoreException {

		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(javaProject);
		settings.createComments= false;
		
		//Creating the processor and setting all the required preferences
		ExtractSupertypeProcessor processor = new ExtractSupertypeProcessor(members, settings);
		processor.setTypeName("newName");
		processor.setMembersToMove(members);
		processor.setTypesToExtract(new IType[] {iType});
		processor.setCreateMethodStubs(true);
		
		if(forceRemoveMethods){
			processor.setDeletedMethods(findMethods(members));
		}
		
		//Performing the refactoring
		Refactoring refactor = new ProcessorBasedRefactoring(processor);		
		performRefactoring(refactor);
		
	}
	
	public static void performExtractInterfaceRefactoring(int start, int length, String newName) throws CoreException {
		
	}
	public static void performExtractClassRefactoring(int start, int length, String newName) throws CoreException {
		
	}
}
