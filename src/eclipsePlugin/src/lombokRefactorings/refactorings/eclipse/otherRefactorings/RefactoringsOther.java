//package lombokRefactorings.refactorings.eclipse.otherRefactorings;
//TODO
//import lombokRefactorings.refactorings.ImInDisagreementWithTheseClasses.Refactorings;
//
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.core.commands.operations.IOperationHistory;
//import org.eclipse.core.commands.operations.IUndoContext;
//import org.eclipse.core.commands.operations.IUndoableOperation;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.jdt.core.IMember;
//import org.eclipse.jdt.core.IMethod;
//import org.eclipse.jdt.core.IType;
//import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
//import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
//import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
//import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
//import org.eclipse.ltk.core.refactoring.Refactoring;
//import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.operations.IWorkbenchOperationSupport;
//
//public class RefactoringsOther extends Refactorings {
//
//	public RefactoringsOther(String projectName, String sourceName,
//			String sourceFolderName) {
//		super(projectName, sourceName, sourceFolderName);
//	}
//
//	public static void performPullUpRefactoring(IMember[] members, String className) throws CoreException {
////		TODO: FINISH REFACTORING		
////		CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(javaProject);
////		settings.createComments= false;
////		
////		//Creating the processor and setting all the required preferences
////		PullUpRefactoringProcessor processor = new PullUpRefactoringProcessor(members, settings);
////		processor.setTypeName("newName");
////		processor.setMembersToMove(members);
////		processor.setTypesToExtract(new IType[] {iType});
////		processor.setCreateMethodStubs(true);
////		
////		if(forceRemoveMethods){
////			processor.setDeletedMethods(findMethods(members));
////		}
////		
////		//Performing the refactoring
////		Refactoring refactor = new ProcessorBasedRefactoring(processor);		
////		performRefactoring(refactor);
//	}
//	
//	public static void performPushDownRefactoring() throws CoreException {
//		
//		
//	}
//	
//	public static void performUseSupertypeWherePossibleRefactoring() throws CoreException {
//		
//		
//	}
//	
//	public static void performUndo() throws ExecutionException{
//		
//		
//		IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
//		//TextViewerUndoManager
//		IOperationHistory operationHistory = operationSupport.getOperationHistory();
//		//IUndoContext context = operationSupport.getUndoContext();
//		IUndoContext context= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
//		IStatus status = operationHistory.undo(context, null, null);
//	}	
//}
