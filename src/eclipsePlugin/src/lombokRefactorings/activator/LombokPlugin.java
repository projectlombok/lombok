package lombokRefactorings.activator;

import lombokRefactorings.folderOptions.FolderManager;
import lombokRefactorings.projectOptions.ProjectManager;
import lombokRefactorings.unitTestOptions.AstManager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LombokPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "lombokRefactoringTests"; //$NON-NLS-1$
	private FolderManager folderManager = null;
	private ProjectManager projectManager = null;
	private AstManager astManager = null;
	
	// The shared instance
	private static LombokPlugin plugin;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LombokPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public void setFolderManager(FolderManager manager) {
		this.folderManager = manager;
	}

	public FolderManager getFolderManager() {
		if (folderManager != null) {
			return folderManager;
		}
		else {
			System.err.println("Manager is null!");
		}
		
		return null;
	}

	public void setProjectManager(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}

	public ProjectManager getProjectManager() {
		if (projectManager != null) {
			return projectManager;
		}
		else {
			System.err.println("Manager is null!");
		}
		
		return null;
	}
	
	public void setAstManager(AstManager astManager) {
		this.astManager = astManager;
	}
	
	public AstManager getAstManager() {
		if (astManager != null) {
			return astManager;
		}
		else {
			astManager = new AstManager();
		}
		
		return null;
	}
}
