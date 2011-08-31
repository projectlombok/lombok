package lombokRefactorings.folderOptions;

import static lombokRefactorings.TestTypes.BEFORE;
import static lombokRefactorings.TestTypes.DELOMBOKED;
import static lombokRefactorings.TestTypes.DELOMBOKED_THEN_REFACTORED;
import static lombokRefactorings.TestTypes.EXPECTED;
import static lombokRefactorings.TestTypes.REFACTORED;
import static lombokRefactorings.TestTypes.REFACTORED_THEN_DELOMBOKED;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombokRefactorings.TestTypes;
import lombokRefactorings.activator.LombokPlugin;
import lombokRefactorings.guiAction.LombokResourceAction;
import lombokRefactorings.projectOptions.ProjectCreator;
import lombokRefactorings.projectOptions.ProjectManager;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class FolderManager {
	
	private final static Map<TestTypes, IFolder> folders = new HashMap<TestTypes, IFolder>();

	private final LombokResourceAction action;

	private final ProjectManager projectManager;
	
	public FolderManager(LombokResourceAction action, ProjectManager projectManager) throws CoreException {
		this.action = action;
		this.projectManager = projectManager;
		copyFiles();
	}
	
	/**
	 * First deletes the existing folders and then creates a new project. 
	 * Creates the test folder and subfolders and copies it to the source of the project. 
	 * @throws CoreException 
	 */
	private void copyFiles() throws CoreException {		
		try {
			projectManager.deleteProject(projectManager.getProject(TestTypes.TESTFILES));
			projectManager.createProjects();

			initializeFolders();
			
			copyTestsToBeforeFolder(action.getResource());
			
			createSubFolders(folders.get(REFACTORED));
			createSubFolders(folders.get(DELOMBOKED));
			createSubFolders(folders.get(DELOMBOKED_THEN_REFACTORED));
			createSubFolders(folders.get(REFACTORED_THEN_DELOMBOKED));
			
			FileGenerator.correctPackageDeclarations(folders.get(BEFORE));
			FileGenerator.correctPackageDeclarations(folders.get(EXPECTED));

			projectManager.refreshProjects();
		}
		catch (Exception e) {
			System.err.println("Copy files failed: " + e);
			e.printStackTrace();
		}
	}

	private void initializeFolders() {
		for (TestTypes type: TestTypes.values()) {
			IFolder srcFolder = getSrcFolder(type);
			if (srcFolder != null) {
				folders.put(type, srcFolder);
			}
		}
		cleanSubFolders();
	}
	
	private void cleanSubFolders() {
		for (TestTypes type: TestTypes.values()) {
			deleteSubFolders(folders.get(type));
		}
	}

	private IFolder getSrcFolder(TestTypes type){
		return (IFolder) projectManager.getProject(type).findMember(ProjectCreator.getSourceFolderName());
	}
	
	public static File toFile(IFolder folder){
		return folder.getRawLocation().toFile();
	}
	
	private void deleteSubFolders(IFolder deletable) {

		if (deletable.getName().equals("src")) {
			try {
				for (IResource deletableSubFolder : deletable.members()) {
					deleteSubFolders((IFolder) deletableSubFolder);
				}
			} catch (CoreException e) {
				System.err.println("Going to subFolder Failed: " + deletable.getName());
			}
		}
		
		else if (deletable != null) {
			try {
				deletable.delete(true, null);
			} catch (CoreException e) {
				System.err.println("Deleting file failed: " + deletable.getName());
			}
		}
	}
	
	private void copyTestsToBeforeFolder(IResource resource) throws CoreException {
		if (resource != null) {
			Path path = new Path(getSrcFolder(BEFORE).getFullPath().toString() + "/" + resource.getName());
			resource.copy(path, true, null);
			if (resource instanceof IFolder) {
				removeExpectedFolder(folders.get(BEFORE));
				checkForExpectedFolder((IFolder) resource);
			}
		}
	}
	
	private void removeExpectedFolder(IFolder folder) throws CoreException {
		for (IResource resource: folder.members()) {
			if (resource.getClass().getSimpleName().equals("Folder") && !(resource.getName().equalsIgnoreCase("expected"))) {
				removeExpectedFolder((IFolder) resource);
			}
			else if (resource.getClass().getSimpleName().equals("Folder")) {
				resource.delete(true, null);
			}
		}
	}
	
	private void checkForExpectedFolder(IFolder folder) throws CoreException {
		checkForExpectedFolder(folder, folder);
	}
	
	private void checkForExpectedFolder(IFolder folder, IFolder testFolder) throws CoreException {
		for (IResource resource: folder.members()) {
			if (resource.getClass().getSimpleName().equals("Folder")){
				if (resource.getName().equalsIgnoreCase("expected")) {
					projectManager.refreshProjects();
					Path path = new Path(getSrcFolder(EXPECTED).getFullPath().toString() + "/" + resource.getLocation().makeRelativeTo(testFolder.getLocation()).toString());
					resource.copy(path, true, null);
				}
				else {
					Path path = new Path(getSrcFolder(EXPECTED).getLocation().toString() + "/" + resource.getLocation().makeRelativeTo(testFolder.getLocation()).toString());
					File folderSub = new File(path.toString());
					folderSub.mkdir();
				}
				checkForExpectedFolder((IFolder) resource);
			}
		}
	}
	
	/**
	 * Used by copyFiles() to create the subfolders of the before folder in the other folders.
	 * 
	 * @param folderName : Is the name of the name of the folder to which the folders of before are copied
	 * @throws CoreException
	 * @throws IOException
	 */
	private void createSubFolders(IFolder folderName) throws CoreException, IOException {
		
		File pathFolder = new File(folderName.getLocation().toString());

		createSubFolders(folders.get(BEFORE), pathFolder);
	}

	/**
	 * Creates the subfolders of the folders created by createFolders.
	 * 
	 * @param folder Folder used to look through and call back recursively.
	 * @param pathFolder Used to get the path in which to set the next folder.
	 * @throws CoreException
	 * @throws IOException
	 */
	private void createSubFolders(IFolder folder, File pathFolder) throws CoreException, IOException {
		for (IResource resource : folder.members()) {
			if (resource.getClass().getSimpleName().equals("Folder") && !(resource.getName().equals("expected"))) {
				File folderSub = new File(pathFolder.getCanonicalPath() + "/" + resource.getName());
				folderSub.mkdir();
				createSubFolders((IFolder) resource, folderSub);
			}
		}
	}

	public IFolder getFolder(TestTypes folder) {
		return folders.get(folder);
	}
}
