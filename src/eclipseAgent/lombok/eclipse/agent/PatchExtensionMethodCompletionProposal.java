/*
 * Copyright (C) 2012 The Project Lombok Authors.
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
package lombok.eclipse.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.eclipse.agent.PatchExtensionMethod.Extension;
import lombok.eclipse.agent.PatchExtensionMethod.Reflection;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public class PatchExtensionMethodCompletionProposal {

	
	public static IJavaCompletionProposal[] getJavaCompletionProposals(IJavaCompletionProposal[] javaCompletionProposals,
			CompletionProposalCollector completionProposalCollector) {
		
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>(Arrays.asList(javaCompletionProposals));
		if (canExtendCodeAssist(proposals)) {
			IJavaCompletionProposal firstProposal = proposals.get(0);
			int replacementOffset = getReplacementOffset(firstProposal);
			for (Extension extension : PatchExtensionMethod.getExtensionMethods(completionProposalCollector)) {
				for (MethodBinding method : extension.extensionMethods) {
					ExtensionMethodCompletionProposal newProposal = new ExtensionMethodCompletionProposal(replacementOffset);
					copyNameLookupAndCompletionEngine(completionProposalCollector, firstProposal, newProposal);
					ASTNode node = PatchExtensionMethod.getAssistNode(completionProposalCollector);
					newProposal.setMethodBinding(method, node);
					createAndAddJavaCompletionProposal(completionProposalCollector, newProposal, proposals);
				}
			}
		}
		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}
	
	private static void copyNameLookupAndCompletionEngine(CompletionProposalCollector completionProposalCollector, IJavaCompletionProposal proposal,
			InternalCompletionProposal newProposal) {
		
		try {
			InternalCompletionContext context = (InternalCompletionContext) Reflection.contextField.get(completionProposalCollector);
			InternalExtendedCompletionContext extendedContext = (InternalExtendedCompletionContext) Reflection.extendedContextField.get(context);
			LookupEnvironment lookupEnvironment = (LookupEnvironment) Reflection.lookupEnvironmentField.get(extendedContext);
			Reflection.nameLookupField.set(newProposal, ((SearchableEnvironment) lookupEnvironment.nameEnvironment).nameLookup);
			Reflection.completionEngineField.set(newProposal, lookupEnvironment.typeRequestor);
		} catch (IllegalAccessException ignore) {
			// ignore
		}
	}
	
	private static void createAndAddJavaCompletionProposal(CompletionProposalCollector completionProposalCollector, CompletionProposal newProposal,
			List<IJavaCompletionProposal> proposals) {
		
		try {
			proposals.add((IJavaCompletionProposal) Reflection.createJavaCompletionProposalMethod.invoke(completionProposalCollector, newProposal));
		} catch (Exception ignore) {
			// ignore
		}
	}
	
	private static boolean canExtendCodeAssist(List<IJavaCompletionProposal> proposals) {
		return !proposals.isEmpty() && Reflection.isComplete();
	}
	
	private static int getReplacementOffset(IJavaCompletionProposal proposal) {
		try {
			return Reflection.replacementOffsetField.getInt(proposal);
		} catch (Exception ignore) {
			return 0;
		}
	}
}
