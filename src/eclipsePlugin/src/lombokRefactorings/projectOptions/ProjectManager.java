package lombokRefactorings.projectOptions;

import java.util.HashMap;
import java.util.Map;

import lombokRefactorings.TestTypes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
public class ProjectManager {
	
	private final Map<TestTypes, IProject> projects = new HashMap<TestTypes, IProject>();
	
	public ProjectManager() {
		for (TestTypes type: TestTypes.values()) {
			projects.put(type, initializeProject(type));			
		}
	}
	
	private IProject initializeProject (TestTypes type) {		
		IProject project = null;
		
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();	
			project = root.getProject(type.getName());
			if (project.exists()) {
				project.open(null);
			}
		} catch (CoreException e) {
			System.err.println("Initializing project failed: " + e);
		}
		
		return project;
	}
	
	public void createProjects() throws CoreException {
		ProjectCreator projectCreator = new ProjectCreator();
		
		for (TestTypes type: TestTypes.values()) {
				deleteProject(getProject(type));
				projectCreator.createProject(type.getName());
		}
	}
	
	public void deleteProjects() throws CoreException {
		for (TestTypes type: TestTypes.values()) {
			if (!type.equals(TestTypes.TESTFILES)) {
				deleteProject(getProject(type));
			}
		}
	}
	
	public void deleteProject(IProject project) throws CoreException {
		if(project.exists()) {
			project.delete(true, true, null);
		}
	}
	
	public void refreshProjects() throws CoreException{
		for (IProject project: projects.values()) {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}
	
	public IProject getProject(TestTypes type) {
		return projects.get(type);
	}
}
