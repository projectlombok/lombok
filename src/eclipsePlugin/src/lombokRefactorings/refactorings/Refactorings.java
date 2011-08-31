package lombokRefactorings.refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
@SuppressWarnings("restriction")
public abstract class Refactorings {
	
	protected static IWorkspaceRoot root;
	protected static IProject project;
	protected static IJavaProject javaProject;
	protected static IType iType;
	protected static ICompilationUnit iCompilationUnit;
	protected static IPackageFragment iPackageFragment;
	protected static IPackageFragmentRoot iPackageFragmentRoot;
	
	public static ICompilationUnit getCompilationUnit() {
		
		return iCompilationUnit;
	}
	
	
	/**
	 * Sets the required fields to perform refactorings. These have to be set 
	 * before any refactoring can be done.
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	public static void setUp(String projectName, String sourceName, String sourceFolderName) throws CoreException{
		root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(projectName);
		project.open(null /* IProgressMonitor */);
		
		javaProject = JavaCore.create(project);
		iType = javaProject.findType(sourceName);
		iPackageFragment = iType.getPackageFragment();
		iCompilationUnit = iType.getCompilationUnit();
		iPackageFragmentRoot = javaProject.findPackageFragmentRoot(new Path ("/" + projectName + "/" + sourceFolderName));
	}
	
	/**
	 * Performs the actual refactoring for the given refactoring subclass.
	 * 
	 * @param refactor The refactoring which should be performed
	 * 
	 * @author PeterB
	 * 
	 * @throws CoreException
	 */
	protected static void performRefactoring(Refactoring refactor) throws CoreException {
		
		PerformRefactoringOperation op = new PerformRefactoringOperation(refactor, CheckConditionsOperation.ALL_CONDITIONS);
		op.run(null);
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
	
	protected static ASTNode parse(ICompilationUnit unit) throws JavaModelException {
		
		org.eclipse.jdt.core.dom.CompilationUnit node= RefactoringASTParser.parseWithASTProvider(unit, true, null);
		return node;
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
	
}
