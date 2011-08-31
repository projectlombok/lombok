package lombokRefactorings.activator.obsoleteMethods;

import lombokRefactorings.refactorings.Refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameJavaProjectProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameLocalVariableProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameSourceFolderProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RefactoringsRename extends Refactorings {

	/**
	 * Performs the rename method arguments refactoring
	 * 
	 * @param start The start of the name of the argument
	 * @param length The length of the name of the argument
	 * @param newName The new name which should be given to the argument
	 * @throws CoreException
	 */
	public static void performRenameMethodArgumentsRefactoring(int start, int length, String newName) throws CoreException {
		
		performRenameLocalVariableRefactoring(start, length, newName);
	}
	
	public static void performRenamePackageRefactoring(String newName) throws CoreException {
		
		RenamePackageProcessor processor = new RenamePackageProcessor(iPackageFragment);
		processor.setNewElementName(newName);
		RenameRefactoring refactor = new RenameRefactoring(processor);
		performRefactoring(refactor);
	}
	
	/**
	 * Performs the Rename Class refactoring
	 * 
	 * @param newName The new name which should be given to the class
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performRenameClassRefactoring(String newName) throws CoreException{
		
		RenameTypeProcessor processor = new RenameTypeProcessor(iType);
		processor.setNewElementName(newName);
		RenameRefactoring refactor = new RenameRefactoring(processor);
		performRefactoring(refactor);
	}
	
	/**
	 * Performs the rename field refactoring.
	 * 
	 * @param start The first position of the name of the field which should be renamed
	 * @param length The length of the name of the field which should be renamed
	 * @param newName The new name which should be given to the field
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performRenameFieldRefactoring(int start, int length, String newName) throws CoreException {

			ITextSelection selection = new TextSelection(start,length);
			IField iField = (IField) SelectionConverter.codeResolve(iCompilationUnit, selection)[0];		
			RenameFieldProcessor processor = new RenameFieldProcessor(iField);
			processor.setNewElementName(newName);
			RenameRefactoring refactor = new RenameRefactoring(processor);
			performRefactoring(refactor);
	}
	
	/**
	 * @param newName The new name which should be given to 
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performRenameSourceFolderRefactoring(String newName) throws CoreException {
		
		RenameSourceFolderProcessor processor = new RenameSourceFolderProcessor(iPackageFragmentRoot);
		processor.setNewElementName(newName);
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		performRefactoring(refactoring);
	}
	
	/**
	 * Performs the rename local variable refactoring.
	 * 
	 * @param start The first position of the name of the local variable which should be renamed
	 * @param length The length of the name of the local variable which should be renamed
	 * @param newName The new name which should be given to the local variable
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performRenameLocalVariableRefactoring(int start, int length, String newName) throws CoreException {
		
		ITextSelection selection = new TextSelection(start,length);		
		LocalVariable localVar = (LocalVariable) SelectionConverter.codeResolve(iCompilationUnit, selection)[0];
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(localVar);
		processor.setNewElementName(newName);
		RenameRefactoring refactor = new RenameRefactoring(processor);
		performRefactoring(refactor);
	}
	
	/**
	 * Performs the rename method refactoring
	 * 
	 * @param start The start position of the method. The start is the beginning of the declaration of the method.
	 * @param newName Specifies the new name which should be given to the method
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performRenameMethodRefactoring(int start, String newName) throws CoreException {

		RenameMethodProcessor processor = new RenameMethodProcessor((IMethod) iCompilationUnit.getElementAt(start)){
			@Override
			public String getDelegateUpdatingTitle(boolean plural) {
				return null;
			}
		};
		processor.setNewElementName(newName);
		RenameRefactoring refactor = new RenameRefactoring(processor);
		performRefactoring(refactor);
	}
	
	/**
	 * Renames the project to the new name.
	 * 
	 * @param newName
	 * @throws CoreException
	 */
	public static void performRenameJavaProjectRefactoring(String newName) throws CoreException{
		
		RenameJavaProjectProcessor proc = new RenameJavaProjectProcessor(javaProject);
		proc.setNewElementName(newName);
		RenameRefactoring ref = new RenameRefactoring(proc);
		performRefactoring(ref);
	}
	
}
