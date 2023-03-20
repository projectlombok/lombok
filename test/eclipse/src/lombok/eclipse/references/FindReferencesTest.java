package lombok.eclipse.references;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;

@RunWith(EclipseRunner.class)
public class FindReferencesTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@Test
	public void extensionMethod() throws Exception {
		ICompilationUnit extensionCu = setup.getPackageFragment().getCompilationUnit("Extension.java");
		IType type = extensionCu.findPrimaryType();
		List<SearchMatch> firstResult = searchInProject(type.getMethods()[0]);
		assertEquals(firstResult.size(), 2);
		
		ICompilationUnit usageCu = setup.getPackageFragment().getCompilationUnit("Usage.java");
		List<SearchMatch> secondResult = searchInProject(usageCu.codeSelect(170, 0)[0]);
		assertEquals(secondResult.size(), 2);
	}

	private List<SearchMatch> searchInProject(IJavaElement element) throws CoreException, JavaModelException {
		CollectingSearchRequestor requestor = new CollectingSearchRequestor();
		SearchEngine engine = new SearchEngine();
		engine.search(
			SearchPattern.createPattern(element, IJavaSearchConstants.ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH), 
			new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
			SearchEngine.createJavaSearchScope(new IJavaElement[] { setup.getJavaProject() }), 
			requestor, 
			null
		);
		
		return requestor.getResults();
	}
}
