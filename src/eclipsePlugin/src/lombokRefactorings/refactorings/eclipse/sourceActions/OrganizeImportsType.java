package lombokRefactorings.refactorings.eclipse.sourceActions;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.RefactoringFailedException;
import lombokRefactorings.refactorings.eclipse.EditorBasedRefactoringType;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

@SuppressWarnings("restriction")
public class OrganizeImportsType extends EditorBasedRefactoringType implements IRefactoringType {

//	@Override
//	public void run(RefactoringRequest request)
//			throws RefactoringFailedException, PartInitException, JavaModelException {
//
//		IEditorPart editor;
//			ICompilationUnit unit = request.getCompilationUnit();
//			editor = EditorUtility.openInEditor(unit);
//			if (editor instanceof JavaEditor) {
//
//				JavaEditor javaEditor = (JavaEditor) editor;
//				OrganizeImportsAction orgImp = new OrganizeImportsAction(
//						javaEditor);
//				orgImp.run(request.getCompilationUnit());
//				request.getCompilationUnit().getWorkingCopy(null).commitWorkingCopy(true, null);
//				javaEditor.close(true);
//			} else {
//				throw new RefactoringFailedException("Organize imports failed");
//			}
//	}

	@Override
	public void doRefactoring(JavaEditor editor,
			ICompilationUnit iCompilationUnit, RefactoringRequest request)
			throws Exception {

		OrganizeImportsAction orgImp = new OrganizeImportsAction(
				editor);
		orgImp.run(request.getCompilationUnit());
	}
}
