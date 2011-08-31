package lombokRefactorings.guiAction;

import java.io.PrintStream;

import lombokRefactorings.TestTypes;
import lombokRefactorings.activator.LombokPlugin;
import lombokRefactorings.folderOptions.FolderManager;
import lombokRefactorings.folderOptions.TestFolderBuilder.FolderBuilderException;
import lombokRefactorings.folderOptions.TestFolderBuilderImpl;
import lombokRefactorings.projectOptions.ProjectManager;
import lombokRefactorings.unitTestOptions.AstManager;
import lombokRefactorings.unitTestOptions.TestRunnerBuilder;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class LombokResourceAction implements IObjectActionDelegate {
	// TODO remove or close where appropriate 
	public static final PrintStream DEBUG_PRINTER = createPrinter("Debug console");
	private IResource resource;
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection != null && !selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				resource = ((IResource) ((IStructuredSelection) selection).getFirstElement());
			}
		}
	}
	
	public void run(IAction action) {	
		DEBUG_PRINTER.println(resource.toString());

		try {
			LombokPlugin.getDefault().setAstManager(new AstManager());
			final ProjectManager projectManager = new ProjectManager();
			final FolderManager folderManager = new FolderManager(this, projectManager);
			
			LombokPlugin.getDefault().setFolderManager(folderManager);
			LombokPlugin.getDefault().setProjectManager(projectManager);
		
			IFolder refactoredThenDelombokedFolder = TestFolderBuilderImpl.create(folderManager, TestTypes.BEFORE)
					.refactor(TestTypes.REFACTORED)
					.delombok(TestTypes.REFACTORED_THEN_DELOMBOKED)
					.build();
			
			IFolder delombokedThenRefactoredFolder = TestFolderBuilderImpl.create(folderManager, TestTypes.BEFORE)
					.delombok(TestTypes.DELOMBOKED)
					.refactor(TestTypes.DELOMBOKED_THEN_REFACTORED)
					.build();
			
			IFolder expected = folderManager.getFolder(TestTypes.EXPECTED);

			AstManager astManager = LombokPlugin.getDefault().getAstManager().initializeMaps(
					refactoredThenDelombokedFolder, 
					delombokedThenRefactoredFolder, 
					expected
			);

			IType runner = new TestRunnerBuilder(projectManager.getProject(TestTypes.TESTFILES), astManager)
				.setTests(folderManager.getFolder(TestTypes.DELOMBOKED_THEN_REFACTORED))
				.build();
			
			new JUnitLaunchShortcut().launch(new StructuredSelection(runner), "run");

//			projectManager.deleteProjects();
		} catch (CoreException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (FolderBuilderException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	

	private static PrintStream createPrinter(String name) {
		MessageConsole myConsole;
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				myConsole = (MessageConsole) existing[i];
		// no console found, so create a new one
		myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return new PrintStream(myConsole.newMessageStream());
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// We are not interested in parts
	}

	public IResource getResource() {
		return resource;
	}
}
