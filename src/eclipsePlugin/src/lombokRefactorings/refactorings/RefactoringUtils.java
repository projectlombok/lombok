package lombokRefactorings.refactorings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import lombokRefactorings.regex.RefactoringRequest;
import lombokRefactorings.regex.RegexUtilities;




import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ui.PartInitException;


@SuppressWarnings("restriction")
public class RefactoringUtils {
	/**
	 * Performs the actual refactoring for the given refactoring subclass.
	 * 
	 * @param refactor The refactoring which should be performed
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	public static void performRefactoring(Refactoring refactor) throws CoreException {	
		new PerformRefactoringOperation(refactor, CheckConditionsOperation.ALL_CONDITIONS).run(null);
	}
	
	
	/**
	 * Finds and returns the first parent of any java element which is a package fragment root, most of the time this is the source folder named 'src'.  
	 * @param javaElement element for which to find the package fragment root
	 * @return The package fragment root. Null if there is no package fragment root (such as when the parameter is a package fragment root itself or a java project). 
	 * @author MaartenT 
	 */
	public static IPackageFragmentRoot findPackageFragmentRoot(IJavaElement javaElement){
		while(javaElement.getParent() != null){
			if(javaElement.getParent().getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT){
				return (IPackageFragmentRoot) javaElement.getParent();
			}
			javaElement = javaElement.getParent();
		}
		return null;
	}
	
	/**
	 * Parses an ICompilationUnit into a CompilationUnit
	 * 
	 * @param unit The ICompilationUnit of which a CompilationUnit should be made
	 * 
	 * @author SaskiaW
	 * 
	 * @return A CompilationUnit corresponding to the ICompilationUnit
	 * 
	 * @throws JavaModelException
	 */
	public static CompilationUnit parse(ICompilationUnit unit) {
		return (CompilationUnit) RefactoringASTParser.parseWithASTProvider(unit, true, null);
	}

	protected static IMethod[] findMethods(IMember[] members) {
		
		List<IMethod> methods = new ArrayList<IMethod>();
		
		for(IMember element : members) {
			
			if(element instanceof IMethod) {
				methods.add((IMethod) element);
			}
		}
		
		return methods.toArray(new IMethod[0]);
	}
	
	public static int[] getTargetStartAndLength(RefactoringRequest request) throws JavaModelException {
		Matcher m = RegexUtilities.findRegex(request.getParameter(0), request.getCompilationUnit().getSource(), request.getOpeningTagMatcher().end(), request.getClosingTagMatcher().start());
		int[] result = new int[2];
		result[0] = m.start();
		result[1] = m.end() - result[0];
		
		return result;
	}
	
	/**
	 * Last parameter is always the new name
	 * @param request
	 * @return
	 */
	public static String getNewName(RefactoringRequest request){
		return request.getParameter(request.getParameters().size()-1);
	}
	/**
	 * First parameter is always the target
	 * @param request
	 * @return
	 * @throws JavaModelException
	 */
	public static Matcher findTarget(RefactoringRequest request) throws JavaModelException{
		return RegexUtilities.findRegex(request.getParameter(0), request.getCompilationUnit().getSource(), request.getOpeningTagMatcher().end(), request.getClosingTagMatcher().start());
	}
}
