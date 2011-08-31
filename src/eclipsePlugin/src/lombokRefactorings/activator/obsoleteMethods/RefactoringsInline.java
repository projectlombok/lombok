package lombokRefactorings.activator.obsoleteMethods;

import lombokRefactorings.refactorings.Refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;

public class RefactoringsInline extends Refactorings {

	/**
	 * Performs an inline operation on static fields.
	 * 
	 * @param start The start position of the name of the static variable which should be inlined;
	 * @param length The length of the static field name which should be inlined
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performInlineStaticFieldRefactoring(int start, int length ) throws CoreException {
		
		InlineConstantRefactoring refactor = new InlineConstantRefactoring(iCompilationUnit, (org.eclipse.jdt.core.dom.CompilationUnit) parse(iCompilationUnit), start,length );
		performRefactoring(refactor);
	}
	
	/**
	 * Performs an inline operation on methods
	 * 
	 * @param start The start of the method name which should be inlined
	 * @param length The length of the method name which should be inlined
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performInlineMethodRefactoring(int start, int length) throws CoreException {
		
		InlineMethodRefactoring refactor = InlineMethodRefactoring.create(iCompilationUnit, (org.eclipse.jdt.core.dom.CompilationUnit) parse(iCompilationUnit), start,length );
		performRefactoring(refactor);
	}
	
	/**
	 * Performs an inline operation on local variables
	 * 
	 * @param start The start position of the name of the local variable which should be inlined
	 * @param length The length of the name of the variable which should be inlined
	 * 
	 * @author SaskiaW
	 * 
	 * @throws CoreException
	 */
	public static void performInlineLocalVariableRefactoring(int start, int length) throws CoreException {
		
		InlineTempRefactoring refactor = new InlineTempRefactoring(iCompilationUnit, (org.eclipse.jdt.core.dom.CompilationUnit) parse(iCompilationUnit) , start, length);
		PerformRefactoringOperation op = new PerformRefactoringOperation(refactor,CheckConditionsOperation.ALL_CONDITIONS);
		op.run(null);
	}
}
