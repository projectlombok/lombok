package lombokRefactorings.refactorings.eclipse.moveRefactorings;

import lombokRefactorings.refactorings.RefactoringUtils;
import lombokRefactorings.regex.RefactoringRequest;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgDestination;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
/**
 * Moves all Java elements tagged by <code>/*n: here :n*&#47;</code> which are not static or instance methods; syntax for the refactoring request is <code>/*n: GenericMove(package.targetClass) :n*&#47;</code>.
 * It will work on static and instance methods, but they have separate processors (see {@link MoveInstanceMethodType} and {@link MoveStaticElementsType}) and will probably not produce expected results. 
 */
@SuppressWarnings("restriction")
public class GenericMoveType extends AbstractMoveRefactoring {

	@Override
	public void run(RefactoringRequest request) throws Exception {
		ICompilationUnit iCompilationUnit = request.getCompilationUnit();
		IJavaElement[] elements = request.findElements().toArray(new IJavaElement[0]);
		//TODO: When do you pass resources to the policy?
		IResource[] resources = {}; 
		IMovePolicy policy= ReorgPolicyFactory.createMovePolicy(resources, elements);
		if (policy.canEnable()) {
			JavaMoveProcessor processor= new JavaMoveProcessor(policy);
			Refactoring refactoring= new MoveRefactoring(processor);
						
//			processor.setCreateTargetQueries(new CreateTargetQueries(wizard));
//			processor.setReorgQueries(new ReorgQueries(wizard));
			
			IJavaElement destinationObj = iCompilationUnit.getJavaProject().findType("lombok.refactoring.tests.tt");
			System.out.println(destinationObj.getPath());
			IReorgDestination destination = ReorgDestinationFactory.createDestination(destinationObj);
			
//			processor.setDestination(destination);
			policy.setDestination(destination);
			processor.createChange(new NullProgressMonitor());
			RefactoringUtils.performRefactoring(refactoring);
		}
	}

}
