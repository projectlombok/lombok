package lombokRefactorings.refactorings.eclipse.sourceActions;

import java.util.List;
import java.util.regex.Matcher;

import lombokRefactorings.refactorings.RefactoringFailedException;
import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;

/**
 * For calls to methods that don't exist, use <code>CreateUnresolvedMethod()</code> with <code>here tag</code> tags just before the method. 
 * @author MaartenT
 *
 */
@SuppressWarnings("restriction")
public class CreateUnresolvedMethodType extends AbstractQuickFix {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		ChangeCorrectionProposal addMethod = null; //The object we're going to apply		
		
		ICompilationUnit cu = request.getCompilationUnit();
		
		List<Matcher> hereTags = request.findHereTags();
		if(hereTags.size() < 1){return;}
		Matcher hereTag = hereTags.get(0);
		int start = hereTag.end(); 
		int length = 0; //Should actually be the size of the unresolved method name. But oh well, this works. 
		AssistContext assist = new AssistContext(cu, start, length);
		CompilationUnit astRoot= assist.getASTRoot();
		IDocument document = new SimpleDocument(cu.getSource());
		
		/* Create problem */
		if(!(EditorUtility.openInEditor(cu) instanceof JavaEditor)){
			return;
		}
		JavaEditor editor = (JavaEditor) EditorUtility.openInEditor(cu);
		ISelection selection = new TextSelection(start,length);
			if(!(EditorUtility.openInEditor(cu) instanceof JavaEditor)){return;}
		editor.getSelectionProvider().setSelection(selection);
			if(!(editor.gotoAnnotation(true) instanceof IJavaAnnotation)){return;}
		IJavaAnnotation annotation = (IJavaAnnotation) editor.gotoAnnotation(true);
		IProblemLocation problem = new ProblemLocation(start, length, annotation);
		
		/* Values ripped from UnresolvedElementsSubProcessor.getMethodProposals() */		
		ASTNode selectedNode= problem.getCoveringNode(astRoot);
		if (!(selectedNode instanceof SimpleName)) {
			return;
		}
		SimpleName nameNode= (SimpleName) selectedNode;
		String methodName = nameNode.getIdentifier();
		Expression sender = null;
		boolean isSuperInvocation = false;
		List arguments;
		
		ASTNode invocationNode= nameNode.getParent();
		if (invocationNode instanceof MethodInvocation) {
			MethodInvocation methodImpl= (MethodInvocation) invocationNode;
			arguments= methodImpl.arguments();
			sender= methodImpl.getExpression();
			isSuperInvocation= false;
		} else {
			return;
		}

		
			/* All this stuff is lifted from UnresolvedElementsSubProcessor*/
						//		private static void addNewMethodProposals(ICompilationUnit cu, CompilationUnit astRoot, Expression sender, List arguments, boolean isSuperInvocation, ASTNode invocationNode, String methodName, Collection proposals) throws JavaModelException {
			ITypeBinding nodeParentType= Bindings.getBindingOfParentType(invocationNode);
			ITypeBinding binding= null;
						//			if (sender != null) {
						//				binding= sender.resolveTypeBinding();
						//			} else {
				binding= nodeParentType;
				if (isSuperInvocation && binding != null) {
					binding= binding.getSuperclass();
//				}
			}
			if (binding != null && binding.isFromSource()) {
				ITypeBinding senderDeclBinding= binding.getTypeDeclaration();

				ICompilationUnit targetCU= ASTResolving.findCompilationUnitForBinding(cu, astRoot, senderDeclBinding);
				if (targetCU != null) {
					String label;
					Image image;
					ITypeBinding[] parameterTypes= getParameterTypes(arguments);
					if (parameterTypes != null) {
						String sig= ASTResolving.getMethodSignature(methodName, parameterTypes, false);

						if (ASTResolving.isUseableTypeInContext(parameterTypes, senderDeclBinding, false)) {
							if (nodeParentType == senderDeclBinding) {
								label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_description, sig);
								image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
							} else {
								label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_other_description, new Object[] { sig, BasicElementLabels.getJavaElementName(senderDeclBinding.getName()) } );
								image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
							}
							addMethod = (new NewMethodCorrectionProposal(label, targetCU, invocationNode, arguments, senderDeclBinding, 5, image));
						}
						if (senderDeclBinding.isNested() && cu.equals(targetCU) && sender == null && Bindings.findMethodInHierarchy(senderDeclBinding, methodName, (ITypeBinding[]) null) == null) { // no covering method
							ASTNode anonymDecl= astRoot.findDeclaringNode(senderDeclBinding);
							if (anonymDecl != null) {
								senderDeclBinding= Bindings.getBindingOfParentType(anonymDecl.getParent());
								if (!senderDeclBinding.isAnonymous() && ASTResolving.isUseableTypeInContext(parameterTypes, senderDeclBinding, false)) {
									String[] args= new String[] { sig, ASTResolving.getTypeSignature(senderDeclBinding) };
									label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_other_description, args);
									image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
									addMethod =(new NewMethodCorrectionProposal(label, targetCU, invocationNode, arguments, senderDeclBinding, 5, image));
								}
							}
						}
					}
				}
			}
		/* After all this work the proposal might still be null, so check it! */
		if(addMethod != null){
			addMethod.apply(document);
		}else{
			throw new RefactoringFailedException(request.getRefactoringName());
		}
	}
	 
	/**
	 * Copied method from {@link UnresolvedElementsSubProcessor}
	 * @param args
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static ITypeBinding[] getParameterTypes(List args) {
		ITypeBinding[] params= new ITypeBinding[args.size()];
		for (int i= 0; i < args.size(); i++) {
			Expression expr= (Expression) args.get(i);
			ITypeBinding curr= Bindings.normalizeTypeBinding(expr.resolveTypeBinding());
			if (curr != null && curr.isWildcardType()) {
				curr= ASTResolving.normalizeWildcardType(curr, true, expr.getAST());
			}
			if (curr == null) {
				curr= expr.getAST().resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
			}
			params[i]= curr;
		}
		return params;
	}
}
