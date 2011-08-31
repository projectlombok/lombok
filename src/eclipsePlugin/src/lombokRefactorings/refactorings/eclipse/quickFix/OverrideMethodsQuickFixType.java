package lombokRefactorings.refactorings.eclipse.quickFix;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.refactorings.eclipse.EditorBasedRefactoringType;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.UnimplementedCodeFix;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

public class OverrideMethodsQuickFixType extends EditorBasedRefactoringType{

	@SuppressWarnings("restriction")
	@Override
	public void doRefactoring(JavaEditor editor,
			ICompilationUnit iCompilationUnit, RefactoringRequest request)
	throws Exception {

//
//		CompilationUnit unit = RefactoringUtils.parse(iCompilationUnit);
//		IJavaAnnotation annotation = null;
//
//		for (int i = 0; i<=20; i++) {
//
//			annotation = (IJavaAnnotation) ((JavaEditor) editor).gotoAnnotation(true); 
//			if(annotation.getId() == 67109264){
//				break;
//			}
//			if(i == 20) {
//				System.err.println("No errors found to complete this quickfix. Check if your test is written correctly");
//			}
//		}
//
//		int start = 110;//((TextSelection) editor.getSelectionProvider().getSelection()).getOffset();
//		int lenght = 6;((TextSelection) editor.getSelectionProvider().getSelection()).getLength();
//
//		IProblemLocation location = new ProblemLocation(start, lenght, annotation);
//		IProposableFix proposal = UnimplementedCodeFix.createAddUnimplementedMethodsFix(unit, location);
//		IProgressMonitor monitor = new NullProgressMonitor();
//
//		try{
//		CompilationUnitChange change = proposal.createChange(monitor);
//		change.perform(monitor);
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(iCompilationUnit);
		parser.setFocalPosition(110);
		ASTNode createAST = parser.createAST(new NullProgressMonitor());
		
		CompilationUnit unit = (CompilationUnit) RefactoringASTParser.parseWithASTProvider(iCompilationUnit, true, null);
		
		IEditorPart editor1;
		try {
			editor1 = EditorUtility.openInEditor(iCompilationUnit);
			ISelection selection = new TextSelection(110,0);
			((JavaEditor) editor1).getSelectionProvider().setSelection(selection); 
			IJavaAnnotation annotation = null;
			
			for (int i = 0; i<=20; i++) {
				
				annotation = (IJavaAnnotation) ((JavaEditor) editor1).gotoAnnotation(true); 
				if(annotation.getId() == 67109264){
					break;
				}
			}
			
			IProblemLocation location = new ProblemLocation(110, 6, annotation);
			System.out.println(location.getCoveringNode((CompilationUnit) unit).getParent() instanceof TypeDeclaration);
			IProposableFix proposal = UnimplementedCodeFix.createAddUnimplementedMethodsFix(unit, location);
			IProgressMonitor monitor = new NullProgressMonitor();
			
			TypeDeclaration declaration = (TypeDeclaration) location.getCoveringNode((CompilationUnit) unit).getParent();
			
			
			CompilationUnitChange change1 = proposal.createChange(monitor);
			change1.perform(monitor);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
