/*
 * Copyright (C) 2023 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
