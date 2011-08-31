package lombokRefactorings.refactorings.eclipse;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.RefactoringFailedException;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

@SuppressWarnings("restriction")
public abstract class EditorBasedRefactoringType implements IRefactoringType {
	
	/**
	 * Does preliminary and finishing work for a refactoring. This means, for extending classes, that doRefactoring() should be implemented instead of this method. 
	 */
	@Override
	public void run(RefactoringRequest request) throws Exception {
		
		ICompilationUnit iCompilationUnit = request.getCompilationUnit();
		JavaEditor editor = openEditor(iCompilationUnit);
		setSelection(editor, request);
		doRefactoring(editor, iCompilationUnit, request);
		save(iCompilationUnit);
		closeEditor(editor);
	}

	/**
	 * Performs the refactoring-specific actions.
	 * @param editor
	 * @param iCompilationUnit
	 * @param request
	 * @throws Exception
	 */
	public abstract void doRefactoring(JavaEditor editor, ICompilationUnit iCompilationUnit, RefactoringRequest request) throws Exception;

	final private void closeEditor(JavaEditor editor) {	
		editor.close(true);
	}

	final private void save(ICompilationUnit iCompilationUnit) throws JavaModelException {
		iCompilationUnit.getWorkingCopy(null).commitWorkingCopy(true, null);
	}
 
	
	/**
	 * Default implementation. The selection will be set beginning where the begin tag ends and ends where the endtag begins.
	 * 
	 * @param editor
	 * @param request
	 *
	 * @author SaskiaW
	 *
	 */
	protected void setSelection(JavaEditor editor, RefactoringRequest request){
		
		int begin = request.getOpeningTagMatcher().end();
		int length = request.getClosingTagMatcher().start() - begin;
		ISelection selection = new TextSelection(begin, length);
		editor.getSelectionProvider().setSelection(selection); 
	}

	final private JavaEditor openEditor(ICompilationUnit iCompilationUnit) throws RefactoringFailedException, CoreException {
		IEditorPart localEditor;
		localEditor = EditorUtility.openInEditor(iCompilationUnit);
		if (localEditor instanceof JavaEditor) {
			iCompilationUnit.getResource().getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			return (JavaEditor) localEditor;
		} else {
			throw new RefactoringFailedException("Cannot open the compilation unit in a javaEditor");
		}
	}


}
