package lombokRefactorings.folderOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import lombok.delombok.Delombok;
import lombokRefactorings.activator.LombokPlugin;
import lombokRefactorings.projectOptions.ProjectCreator;
import lombokRefactorings.projectOptions.ProjectManager;
import lombokRefactorings.regex.SearchAndCallRefactorings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class FileGenerator {
	
	private static ProjectManager projectManager = new ProjectManager();
	
	/**
	 * Recursively delomboks the files in the inputFolder and places the delomboked files in the outputFolder.
	 * 
	 * @param folder Folder used to look through and call back recursively.
	 * @throws CoreException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */	
	public static void delombokFilesInFolder(IFolder inputFolder, IFolder outputFolder) throws CoreException, IOException, URISyntaxException {
		
		File outputFile = null;
		File inputFile = null;
		String name = "";
		
		for (IResource resource :inputFolder.members()) {
			if (resource.getClass().getSimpleName().equals("Folder")) {
				for (IResource outputResource :outputFolder.members()) {
					if (resource.getClass().getSimpleName().equals("Folder") &&
							resource.getName().equals(outputResource.getName())) {
						delombokFilesInFolder((IFolder) resource, (IFolder) outputResource);
					}
				}
			}
			else{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try {
					Delombok delombok = new Delombok();
					delombok.setVerbose(false);
					delombok.setForceProcess(true);
					
					delombok.setCharset("UTF-8");
					delombok.setFeedback(new PrintStream(stream));
					
					outputFile = new File(outputFolder.getRawLocation().toFile(), resource.getName());
					name = resource.getName();
					inputFile = new File(inputFolder.getRawLocation().toFile().toString());
					
					if (outputFile != null && inputFile != null && !"".equals(name)) {
						FileWriter writer = new FileWriter(outputFile);
						delombok.addFile(inputFile, name);
						delombok.setWriter(writer);
						if (!delombok.delombok()) {
							LombokPlugin.getDefault().getAstManager().addFailure(resource.getName(), "Delombok failed");
						}
						projectManager.refreshProjects();
					}
					String delombokFeedback = stream.toString();
				}
				finally {
					stream.close();
				}
			}
		}
	}
	
	public static void refactorFilesInFolder(IFolder inputFolder, IFolder outputFolder) throws JavaModelException, CoreException, IOException {
		refactorFilesInFolder(inputFolder, inputFolder, outputFolder);
	}

	/**
	 * Recursively executes the refactorings in the files in the folder.
	 * 
	 * @param inputFolder Folder used to look through and call back recursively.
	 * @throws CoreException
	 * @throws IOException
	 * @throws JavaModelException
	 */
	private static void refactorFilesInFolder(IFolder inputFolder, IFolder parent, IFolder outputFolder) throws CoreException, IOException,
			JavaModelException {
		for (IResource inputResource :inputFolder.members()) {
			
			if (inputResource.getClass().getSimpleName().equals("Folder")) {
				for (IResource outputResource :outputFolder.members()) {
					if (outputResource.getClass().getSimpleName().equals("Folder") &&
							inputResource.getName().equals(outputResource.getName())) {
						refactorFilesInFolder((IFolder) inputResource, parent, (IFolder) outputResource);
					}
				}
			}
			else {
				ICompilationUnit iCompilationUnit = JavaCore.createCompilationUnitFrom((IFile)inputResource);
				
				File outputFile = null;
				String sourceName = null;
				
				if (inputFolder.getName().equals(outputFolder.getName())) {
					String name = outputFolder.getProjectRelativePath().toString() + "." + inputResource.getName();
					name = name.replaceAll("/", ".");
					name = name.substring(0, name.lastIndexOf(".java"));
					name = name.substring(4);
					sourceName = name;
					outputFile = new File(outputFolder.getRawLocation().toFile(), inputResource.getName());
				}
				if (inputFolder.getName().equals(outputFolder.getName())) {
					String name = outputFolder.getProjectRelativePath().toString() + "." + inputResource.getName();
					name = name.replaceAll("/", ".");
					name = name.substring(0, name.lastIndexOf(".java"));
					name = name.substring(4);
					sourceName = name;
					outputFile = new File(outputFolder.getRawLocation().toFile(), inputResource.getName());
				}
				
				FileWriter outputWriter = new FileWriter(outputFile);
				
				outputWriter.write(iCompilationUnit.getSource());
				
				outputWriter.close();

				projectManager.refreshProjects();
				
				String projectName = outputFolder.getProject().getName();
				
				SearchAndCallRefactorings searchAndCallRefactorings = new SearchAndCallRefactorings(projectName, sourceName);
				searchAndCallRefactorings.runRefactorings(searchAndCallRefactorings.findAllTags());
			}
		}
	}

	/**
	 * Recursively adds packages to the files in the folder.
	 * 
	 * @param folder Folder used to look through and call back recursively.
	 * @throws CoreException
	 * @throws PartInitException
	 * @throws JavaModelException
	 */
	public static void correctPackageDeclarations(IFolder folder) throws CoreException, PartInitException,
			JavaModelException {
		
		for (IResource resource :folder.members()) {
			
			if (resource.getClass().getSimpleName().equals("Folder")) {
				correctPackageDeclarations((IFolder) resource);
			}
			else {
				String packageName = resource.getProjectRelativePath().toString();
				packageName = packageName.substring(0, packageName.lastIndexOf(resource.getName())-1);
				if (packageName.equals(ProjectCreator.getSourceFolderName())) {
					packageName = "";
				}
				else {
					packageName = packageName.replaceAll("/", ".");
					packageName = packageName.substring(4);
					packageName = "package " + packageName + ";";
				}
				
				IWorkbenchPage page = null;
				
				if (PlatformUI.getWorkbench().getWorkbenchWindows().length > 0) {
					IWorkbenchWindow workbench = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
					if (workbench.getPages().length > 0) {
						for (IWorkbenchPage searchPage: workbench.getPages()) {
							if ("Workspace - Java".equals(searchPage.getLabel())) {
								page = searchPage;
							}
						}
					}
					else {
						System.err.println("No page selected");
					}
				}
				else {
					System.err.println("No workbench selected");
				}
				
				IFile file = ((IFile) resource);
				IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
				IEditorPart editor = page.openEditor(new FileEditorInput(file), desc.getId());
				ICompilationUnit iCompilationUnit = JavaCore.createCompilationUnitFrom(file);
				if (iCompilationUnit.getSource().contains("package")) {
					String source = iCompilationUnit.getSource();
					int start = source.indexOf("package");
					int length = source.indexOf(";", source.indexOf("package")) - source.indexOf("package") + 1;
					TextEdit edit = new ReplaceEdit(start, length, packageName);
					iCompilationUnit.applyTextEdit(edit, null);
				}
				else {
					TextEdit edit = new InsertEdit(0, (packageName + "\n\n"));
					iCompilationUnit.applyTextEdit(edit, null);
				}
				iCompilationUnit.getWorkingCopy(null).commitWorkingCopy(true, null);
				page.closeEditor(editor, true);
			}
		}
	}
}
