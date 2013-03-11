package lombok.eclipse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;

public class EclipseProjectSearcher {
	private final JavaProject project;
	
	public EclipseProjectSearcher(JavaProject project) {
		this.project = project;
	}
	
	private static final String[] ENTRY_KINDS = {"", "CPE_LIBRARY", "CPE_PROJECT", "CPE_SOURCE", "CPE_VARIABLE", "CPE_CONTAINER"};
	
	
	public static JavaProject getProject(String projectName) {
		Project depProjWrapper = (Project) ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (depProjWrapper == null) return null;
		if (!JavaProject.hasJavaNature(depProjWrapper)) return null;
		return (JavaProject) JavaCore.create(depProjWrapper);
	}
	
	public List<String> findAllWithName(String pathSpec) throws Exception {
		List<IPath> list = new ArrayList<IPath>();
		findAllWithName0(list, new HashSet<String>(), pathSpec, project, false);
		System.out.println("-------RESULTS [" + pathSpec + "]:");
		System.out.println(list);
		System.out.println("---------------");
		
		for (IPath path : list) {
			try {
				System.out.println(path + ": " + ResourcesPlugin.getWorkspace().getRoot().getFile(path.append(pathSpec)).getLocationURI());
			} catch (Exception ignore) {
				try {
					System.out.println(path + ": " + ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(pathSpec)).getLocationURI());
				} catch (Exception e) {
				}
			}
			
			// Check if we got through (didn't double-exept out), and then check if the resulting file URI
			// actually exists.
			//
			// Then, check if 'path' itself is a jar, and if so, go root around in there.
		}
		
		System.out.println("---------------");
		
		return null;
	}
	
	private void findAllWithName0(List<IPath> list, Set<String> coveredProjectNames, String pathSpec, JavaProject proj, boolean exportedOnly) throws Exception {
		if (proj == null) return;
//		if (System.currentTimeMillis() > 0) return;
		
		
		/*
		 * 
		 Research conclusions:
		 
		  * Eclipse is CRAZY.
		  * Take a path, for which you don't know if it's project relative or not (nor does eclipse!!! yes, crazy), append the resource you are looking for (whether you know if its a jar or a path),
		    and ask eclipse if it exists as a file. If yes, you have your answer.
		  * Then, check if the path is 1 or 0 long, in that case, there is no answer.
		  * Then, check if the path itself is a file and if yes, try and open it as a zip. Possibly try appending exclamationmark-resource and ask eclipse, maybe it has jar-style handling built in.
		  * Cache the heck out of this because this code is called after just about every keystroke.
		  * We should probably do something about duplicate detection and collapse, maybe just grab the textual content of those IPaths (they sure as hell don't contain anything useful beyond that), and
		    toss em in a set.
		  
		 */
		if (!coveredProjectNames.add(proj.getElementName())) return;
		
		for (IClasspathEntry rawEntry : proj.getResolvedClasspath(true)) {
			ClasspathEntry entry = (ClasspathEntry) rawEntry;
			if (exportedOnly && !entry.isExported() && entry.entryKind != IClasspathEntry.CPE_SOURCE) continue;
			
			switch (entry.entryKind) {
			case IClasspathEntry.CPE_PROJECT:
				String projName = entry.path.lastSegment();
				JavaProject depProj = getProject(projName);
				findAllWithName0(list, coveredProjectNames, pathSpec, depProj, true);
				break;
			case IClasspathEntry.CPE_SOURCE:
				list.add(entry.path);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				list.add(entry.path);
				break;
			default:
				System.out.println("Wot's this then? " + entry);
			}
		}
	}
}
