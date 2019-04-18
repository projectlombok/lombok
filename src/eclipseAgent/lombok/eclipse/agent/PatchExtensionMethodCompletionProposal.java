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

import static lombok.eclipse.agent.PatchExtensionMethod.*;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.eclipse.EclipseNode;
import lombok.eclipse.agent.PatchExtensionMethod.Extension;
import lombok.experimental.ExtensionMethod;
import lombok.permit.Permit;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public class PatchExtensionMethodCompletionProposal {
	public static IJavaCompletionProposal[] getJavaCompletionProposals(IJavaCompletionProposal[] javaCompletionProposals,
			CompletionProposalCollector completionProposalCollector) {
		
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>(Arrays.asList(javaCompletionProposals));
		if (canExtendCodeAssist(proposals)) {
			IJavaCompletionProposal firstProposal = proposals.get(0);
			int replacementOffset = getReplacementOffset(firstProposal);
			for (Extension extension : getExtensionMethods(completionProposalCollector)) {
				for (MethodBinding method : extension.extensionMethods) {
					ExtensionMethodCompletionProposal newProposal = new ExtensionMethodCompletionProposal(replacementOffset);
					copyNameLookupAndCompletionEngine(completionProposalCollector, firstProposal, newProposal);
					ASTNode node = getAssistNode(completionProposalCollector);
					newProposal.setMethodBinding(method, node);
					createAndAddJavaCompletionProposal(completionProposalCollector, newProposal, proposals);
				}
			}
		}
		return proposals.toArray(new IJavaCompletionProposal[0]);
	}
	
	
	private static List<Extension> getExtensionMethods(CompletionProposalCollector completionProposalCollector) {
		List<Extension> extensions = new ArrayList<Extension>();
		ClassScope classScope = getClassScope(completionProposalCollector);
		if (classScope != null) {
			TypeDeclaration decl = classScope.referenceContext;
			TypeBinding firstParameterType = getFirstParameterType(decl, completionProposalCollector);
			for (EclipseNode typeNode = getTypeNode(decl); typeNode != null; typeNode = upToType(typeNode)) {
				Annotation ann = getAnnotation(ExtensionMethod.class, typeNode);
				extensions.addAll(0, getApplicableExtensionMethods(typeNode, ann, firstParameterType));
			}
		}
		return extensions;
	}
	
	static TypeBinding getFirstParameterType(TypeDeclaration decl, CompletionProposalCollector completionProposalCollector) {
		TypeBinding firstParameterType = null;
		ASTNode node = getAssistNode(completionProposalCollector);
		if (node == null) return null;
		if (!(node instanceof CompletionOnQualifiedNameReference) && !(node instanceof CompletionOnSingleNameReference) && !(node instanceof CompletionOnMemberAccess)) return null;
		
		// Never offer on 'super.<autocomplete>'.
		if (node instanceof FieldReference && ((FieldReference)node).receiver instanceof SuperReference) return null;
		
		if (node instanceof NameReference) {
			Binding binding = ((NameReference) node).binding;
			// Unremark next block to allow a 'blank' autocomplete to list any extensions that apply to the current scope, but make sure we're not in a static context first, which this doesn't do.
			// Lacking good use cases, and having this particular concept be a little tricky on javac, means for now we don't support extension methods like this. this.X() will be fine, though.
			
/*			if ((node instanceof SingleNameReference) && (((SingleNameReference) node).token.length == 0)) {
				firstParameterType = decl.binding;
			} else */if (binding instanceof VariableBinding) {
				firstParameterType = ((VariableBinding) binding).type;
			}
		} else if (node instanceof FieldReference) {
			firstParameterType = ((FieldReference) node).actualReceiverType;
		}
		return firstParameterType;
	}
	
	private static ASTNode getAssistNode(CompletionProposalCollector completionProposalCollector) {
		try {
			InternalCompletionContext context = (InternalCompletionContext) Reflection.contextField.get(completionProposalCollector);
			InternalExtendedCompletionContext extendedContext = (InternalExtendedCompletionContext) Reflection.extendedContextField.get(context);
			if (extendedContext == null) return null;
			return (ASTNode) Reflection.assistNodeField.get(extendedContext);
		} catch (Exception ignore) {
			return null;
		}
	}
	
	private static ClassScope getClassScope(CompletionProposalCollector completionProposalCollector) {
		ClassScope scope = null;
		try {
			InternalCompletionContext context = (InternalCompletionContext) Reflection.contextField.get(completionProposalCollector);
			InternalExtendedCompletionContext extendedContext = (InternalExtendedCompletionContext) Reflection.extendedContextField.get(context);
			if (extendedContext != null) {
				Scope assistScope = ((Scope) Reflection.assistScopeField.get(extendedContext));
				if (assistScope != null) {
					scope = assistScope.classScope();
				}
			}
		} catch (IllegalAccessException ignore) {
			// ignore
		}
		return scope;
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
	
	private static int getReplacementOffset(Object proposal) {
		try {
			return Reflection.replacementOffsetField.getInt(proposal);
		} catch (Exception ignore) {
			return 0;
		}
	}
	
	static class Reflection {
		public static final Field replacementOffsetField;
		public static final Field contextField;
		public static final Field extendedContextField;
		public static final Field assistNodeField;
		public static final Field assistScopeField;
		public static final Field lookupEnvironmentField;
		public static final Field completionEngineField;
		public static final Field nameLookupField;
		public static final Method createJavaCompletionProposalMethod;

		static {
			replacementOffsetField = accessField(AbstractJavaCompletionProposal.class, "fReplacementOffset");
			contextField = accessField(CompletionProposalCollector.class, "fContext");
			extendedContextField = accessField(InternalCompletionContext.class, "extendedContext");
			assistNodeField = accessField(InternalExtendedCompletionContext.class, "assistNode");
			assistScopeField = accessField(InternalExtendedCompletionContext.class, "assistScope");
			lookupEnvironmentField = accessField(InternalExtendedCompletionContext.class, "lookupEnvironment");
			completionEngineField = accessField(InternalCompletionProposal.class, "completionEngine");
			nameLookupField = accessField(InternalCompletionProposal.class, "nameLookup");
			createJavaCompletionProposalMethod = accessMethod(CompletionProposalCollector.class, "createJavaCompletionProposal", CompletionProposal.class);
		}
		
		static boolean isComplete() {
			Object[] requiredFieldsAndMethods = { replacementOffsetField, contextField, extendedContextField, assistNodeField, assistScopeField, lookupEnvironmentField, completionEngineField, nameLookupField, createJavaCompletionProposalMethod };
			for (Object o : requiredFieldsAndMethods) if (o == null) return false;
			return true;
		}
		
		private static Field accessField(Class<?> clazz, String fieldName) {
			try {
				return makeAccessible(clazz.getDeclaredField(fieldName));
			} catch (Exception e) {
				return null;
			}
		}
		
		private static Method accessMethod(Class<?> clazz, String methodName, Class<?> parameter) {
			try {
				return makeAccessible(clazz.getDeclaredMethod(methodName, parameter));
			} catch (Exception e) {
				return null;
			}
		}
		
		private static <T extends AccessibleObject> T makeAccessible(T object) {
			return Permit.setAccessible(object);
		}
	}
}
