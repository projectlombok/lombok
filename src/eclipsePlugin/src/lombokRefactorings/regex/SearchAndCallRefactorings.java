package lombokRefactorings.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import lombokRefactorings.activator.LombokPlugin;
import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.RefactoringFactory;
import lombokRefactorings.refactorings.Refactorings;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Class that searches for and executes refactoring
 *         requests in the source code. We have defined a syntax for this: see
 *         {@link findRefactorComments} for the general syntax and
 *         {@link Refactorings} for more specific conventions.
 * @author PeterB & MaartenT 
 */
public class SearchAndCallRefactorings {
	String source;
	private ICompilationUnit iCompilationUnit;
	private String sourceName = "";
	private String projectName = "";
	private IType iType;

	/**
	 * Setup method to initialize the project and source name, also sets the
	 * source code.
	 * 
	 * @param projectName
	 * @param sourceName
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	public SearchAndCallRefactorings(String projectName, String sourceName)
			throws CoreException {

		this.projectName = projectName;
		this.sourceName = sourceName;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		project.open(null);

		IJavaProject javaProject = JavaCore.create(project);
		iType = javaProject.findType(sourceName);
		iCompilationUnit = iType.getCompilationUnit();
		source = iCompilationUnit.getSource();
	}

	/**
	 * Runs the regex on the set source code. Resets the source code before a
	 * regex is executed.
	 * 
	 * @author PeterB
	 * @throws CoreException
	 */
	public void runRefactorings(List<String> refactoringTags)
			throws CoreException {
		for (String tagName : refactoringTags) {
			try {
				RefactoringRequest request = new RefactoringRequest(tagName,iCompilationUnit);				
				if (request.getRefactoringName().equalsIgnoreCase("done")
						| request.getRefactoringName().toLowerCase()
								.startsWith("failed")) {
					//Refactoring has been done or failed already
					System.out.println("Skipping tag "+tagName);
				}else{
					//Run refactoring
					IRefactoringType refactoring = RefactoringFactory.create(request.getRefactoringName());
					refactoring.run(request);
//					renameTag(tagName, " Done");
				}
			} catch (Exception e) {
				System.err.println(" Refactoring "+tagName+" failed: " + e);
//				LombokPlugin.getDefault().getAstManager().addFailure(iCompilationUnit.getCorrespondingResource().getName(), "refactoring : NAME could not be executed.");
//				renameTag(tagName, " Failed: " +e.toString());
				e.printStackTrace();
			}
		}
		// Save the file to be on the safe side
		iCompilationUnit.getWorkingCopy(null).commitWorkingCopy(true, null);
	}

	/**
	 * Returns all tag names that are in the source code.
	 * 
	 * @author PeterB & MaartenT
	 * @param source
	 * @return the number of tests in the source code
	 * @throws JavaModelException
	 */
	public List<String> findAllTags() throws JavaModelException {
		List<String> allTagNames = new ArrayList<String>();
		
		//Search for closing tags
		String closingTag = "/\\*\\s*?" + ":([^\\*/]*?):" + "\\s*?\\*/";
			for(Matcher matcher : RegexUtilities.findAllRegex(closingTag, source)){
				allTagNames.add(source.substring(matcher.start(1),matcher.end(1)));
			}
			
		return allTagNames;
	}

	/**
	 * A private method to renew the source code. Only to be used after the
	 * workspace has been set.
	 * 
	 * @author PeterB
	 * @throws CoreException
	 */
	private void refreshSource() throws CoreException {
		if (!(projectName.equals("") && sourceName.equals(""))) {
			source = iCompilationUnit.getSource();
		} else {
			System.err.println("Set projectName and set sourecName");
		}
	}

	/**
	 * Replaces the refactoring request tag with the string "Done", eg. changes
	 * / *1: ExtractMethod(myMethod) * / to / *1: Done * /
	 * 
	 * @throws JavaModelException
	 * @author MaartenT
	 */
	private void renameTag(String tagName, String message) {
		try {
			refreshSource();
			Matcher matcher = RegexUtilities.findRegex("/\\*\\s*?" + tagName
					+ "\\s*?:(.*?\\))" + ".*?:\\s*?" + tagName + "\\s*?\\*/",
					source);
			if (matcher != null) {
				TextEdit edit = new ReplaceEdit(matcher.start(1),
						matcher.end(1) - matcher.start(1), message);
				iCompilationUnit.applyTextEdit(edit, null);
				iCompilationUnit.getWorkingCopy(null).commitWorkingCopy(true,
						null);
			}
		} catch (Exception e1) {
			System.err.println("Can't rename refactoring signature, tag name: "+tagName);
			e1.printStackTrace();
		}
	}

}
