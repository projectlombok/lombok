package lombokRefactorings.refactorings;

import lombokRefactorings.refactorings.eclipse.EclipseRefactorings;

/**
 * This class may be modified to support multiple IDEs.
 * 
 * @author MaartenT
 * 
 */
public class RefactoringFactory {
	public static IRefactoringType create(String name) throws Exception {
		return EclipseRefactorings.getByName(name).getRefactoringType();
	}

	/*
	 * For multiple IDE support: private static final int ECLIPSE_ID = 0;
	 * private static final int NETBEANS_ID = 1; public static MyRefactoring
	 * create(RefactoringRequest request, int ideID) throws
	 * NoRefactoringFoundException { switch(ideID){ case 0: ... break; } return
	 * null;
	 */
}
