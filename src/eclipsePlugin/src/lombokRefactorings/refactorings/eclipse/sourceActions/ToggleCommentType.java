package lombokRefactorings.refactorings.eclipse.sourceActions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.eclipse.EditorBasedRefactoringType;
import lombokRefactorings.regex.RefactoringRequest;

/**
 * Type to toggle comments between the regex tags.
 * 
 * @author SaskiaW
 *
 */
@SuppressWarnings("restriction")
public class ToggleCommentType extends EditorBasedRefactoringType implements IRefactoringType {

	@Override
	public void doRefactoring(JavaEditor editor,
			ICompilationUnit iCompilationUnit, RefactoringRequest request)
			throws Exception {
	
		Event event = new Event();
		List<KeyStroke> keystrokes = new ArrayList<KeyStroke>(3);

		/*This is the keystroke corresponding to CTRL+/. The first int is the modifier code which you can obtain via:
		KeyLookupFactory.getSWTKeyLookup().formalModifierLookup("CTRL")
		The second int corresponding to the /. You can lookup several codes in the SWT class.*/
		KeyStroke total = KeyStroke.getInstance(262144,47);		
		keystrokes.add(0,total);
		keystrokes.add(1,null);
		keystrokes.add(2,null);
		WorkbenchKeyboard keywork = new WorkbenchKeyboard(Workbench.getInstance());
		keywork.press(keystrokes,event);
	}

}
