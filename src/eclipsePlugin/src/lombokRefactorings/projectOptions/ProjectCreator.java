package lombokRefactorings.projectOptions;

import java.util.ArrayList;
import java.util.List;

import lombokRefactorings.TestTypes;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.JUnitCore;
import org.junit.runners.JUnit4;

public class ProjectCreator {
	
	static final String SOURCEFOLDERNAME = "src";

	public ProjectCreator() {
	}
	
	public void createProject(String projectName) throws CoreException {
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		IProject project = workspaceRoot.getProject(projectName);
		project.create(null);
		project.open(null);
		
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID 
				});
		project.setDescription(description, null);

		IJavaProject javaProject = JavaCore.create(project); 
		
		IFolder binFolder = project.getFolder("bin");
		javaProject.setOutputLocation(binFolder.getFullPath(), null);

		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
				
		IClasspathEntry[] defaultJreLibrary = PreferenceConstants.getDefaultJRELibrary();

		
		for(int i = 0; i < defaultJreLibrary.length; i++){
			entries.add(defaultJreLibrary[i]);
		}
		
		if (projectName.equals(TestTypes.TESTFILES.getName())) {
			
			entries.add(JavaCore.newLibraryEntry(new Path("C:/Program Files (x86)/eclipse/plugins/org.junit_4.8.1.v4_8_1_v20100427-1100/junit.jar"), null, null));
		}
		
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
		
		IFolder sourceFolder = project.getFolder(SOURCEFOLDERNAME);
		sourceFolder.create(false, true, null);

		IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(sourceFolder);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(packageRoot.getPath());
		javaProject.setRawClasspath(newEntries, null);
	}


	public static String getSourceFolderName() {
		return SOURCEFOLDERNAME;
	}
}
