package lombokRefactorings.actions;

import lombokRefactorings.regex.SearchAndCallRefactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SingleRunRefactoringTests implements IWorkbenchWindowActionDelegate {
	private static IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public SingleRunRefactoringTests() {
	}
	
	/**
	 * Method used to execute the reactorings at startup
	 * @throws CoreException 
	 */
	public static void run() throws CoreException {
//		try{
//			String projectName = "refactoringTestEnvironment";
//						
//			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//			IProject project = root.getProject(projectName);
//			project.open(null /* IProgressMonitor */);
//
//			IPath srcPath = new Path("/refactoringTestEnvironment/src/test");
//			
//			IFolder deleteFolder = ((IFolder) project.findMember("src/test"));			
//			deleteFolder.delete(true, null);
//			
//			IFolder copyFolder = ((IFolder) project.findMember("test"));			
//			copyFolder.copy(srcPath, false, null);
//			
//			IFolder beforeFolder = ((IFolder) project.findMember("src/test/before"));
//			
//			for(IResource resource :beforeFolder.members()){
//				ICompilationUnit iCompilationUnit = JavaCore.createCompilationUnitFrom(((IFile) resource));
//				System.out.println(iCompilationUnit.getSource());
//			}
//		}
//		catch (Exception e){
//			System.out.println(e);
//		}	
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action){
		
		String projectName = "refactoringTestEnvironment";
		String sourceName = "lombok.refactoring.tests.TestAll";
		String sourceFolderName = "src";

		try {
			SearchAndCallRefactorings searchAndCallRefactorings = new SearchAndCallRefactorings(projectName, sourceName);
			searchAndCallRefactorings.runRefactorings(searchAndCallRefactorings.findAllTags());
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		MessageDialog.openInformation(
				window.getShell(),
				"Refactorings done",
				"All refactoring tests are done.");
	}
	
	public static void nextRefactoring(int number){
		MessageDialog.openInformation(
				window.getShell(),
				"popup",
				"next refactoring: " + (number));
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		SingleRunRefactoringTests.window = window;
	}
}