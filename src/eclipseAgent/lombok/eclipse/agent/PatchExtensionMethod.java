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

import static lombok.eclipse.handlers.EclipseHandlerUtil.createAnnotation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.TransformEclipseAST;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.experimental.ExtensionMethod;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public class PatchExtensionMethod {
	private static class Extension {
		List<MethodBinding> extensionMethods;
		boolean suppressBaseMethods;
	}
	
	private static class PostponedNoMethodError implements PostponedError {
		private final ProblemReporter problemReporter;
		private final MessageSend messageSend;
		private final TypeBinding recType;
		private final TypeBinding[] params;
		
		PostponedNoMethodError(ProblemReporter problemReporter, MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
			this.problemReporter = problemReporter;
			this.messageSend = messageSend;
			this.recType = recType;
			this.params = params;
		}
		
		public void fire() {
			problemReporter.errorNoMethodFor(messageSend, recType, params);
		}
	}
	
	private static class PostponedInvalidMethodError implements PostponedError {
		private final ProblemReporter problemReporter;
		private final MessageSend messageSend;
		private final MethodBinding method;
		
		PostponedInvalidMethodError(ProblemReporter problemReporter, MessageSend messageSend, MethodBinding method) {
			this.problemReporter = problemReporter;
			this.messageSend = messageSend;
			this.method = method;
		}
		
		public void fire() {
			problemReporter.invalidMethod(messageSend, method);
		}
	}
	
	private static interface PostponedError {
		public void fire();
	}
	
	public static EclipseNode getTypeNode(TypeDeclaration decl) {
		CompilationUnitDeclaration cud = decl.scope.compilationUnitScope().referenceContext;
		EclipseAST astNode = TransformEclipseAST.getAST(cud, false);
		EclipseNode node = astNode.get(decl);
		if (node == null) {
			astNode = TransformEclipseAST.getAST(cud, true);
			node = astNode.get(decl);
		}
		return node;
	}
	
	public static Annotation getAnnotation(Class<? extends java.lang.annotation.Annotation> expectedType, EclipseNode node) {
		TypeDeclaration decl = (TypeDeclaration) node.get();
		if (decl.annotations != null) for (Annotation ann : decl.annotations) {
			if (EclipseHandlerUtil.typeMatches(expectedType, node, ann.type)) return ann;
		}
		return null;
	}
	
	private static EclipseNode upToType(EclipseNode typeNode) {
		EclipseNode node = typeNode;
		do {
			node = node.up();
		} while ((node != null) && (node.getKind() != Kind.TYPE));
		return node;
	}
	
	private static List<Extension> getApplicableExtensionMethods(EclipseNode typeNode, Annotation ann, TypeBinding receiverType) {
		List<Extension> extensions = new ArrayList<Extension>();
		if ((typeNode != null) && (ann != null) && (receiverType != null)) {
			BlockScope blockScope = ((TypeDeclaration) typeNode.get()).initializerScope;
			EclipseNode annotationNode = typeNode.getNodeFor(ann);
			AnnotationValues<ExtensionMethod> annotation = createAnnotation(ExtensionMethod.class, annotationNode);
			boolean suppressBaseMethods = false;
			try {
				suppressBaseMethods = annotation.getInstance().suppressBaseMethods();
			} catch (AnnotationValueDecodeFail fail) {
				fail.owner.setError(fail.getMessage(), fail.idx);
			}
			for (Object extensionMethodProvider : annotation.getActualExpressions("value")) {
				if (extensionMethodProvider instanceof ClassLiteralAccess) {
					TypeBinding binding = ((ClassLiteralAccess) extensionMethodProvider).type.resolveType(blockScope);
					if (binding == null) continue;
					if (!binding.isClass() && !binding.isEnum()) continue;
					Extension e = new Extension();
					e.extensionMethods = getApplicableExtensionMethodsDefinedInProvider(typeNode, (ReferenceBinding) binding, receiverType);
					e.suppressBaseMethods = suppressBaseMethods;
					extensions.add(e);
				}
			}
		}
		return extensions;
	}
	
	private static List<MethodBinding> getApplicableExtensionMethodsDefinedInProvider(EclipseNode typeNode, ReferenceBinding extensionMethodProviderBinding,
			TypeBinding receiverType) {
		
		List<MethodBinding> extensionMethods = new ArrayList<MethodBinding>();
		CompilationUnitScope cuScope = ((CompilationUnitDeclaration) typeNode.top().get()).scope;
		for (MethodBinding method : extensionMethodProviderBinding.methods()) {
			if (!method.isStatic()) continue;
			if (!method.isPublic()) continue;
			if (method.parameters == null || method.parameters.length == 0) continue;
			TypeBinding firstArgType = method.parameters[0];
			if (receiverType.isProvablyDistinct(firstArgType) && !receiverType.isCompatibleWith(firstArgType.erasure())) continue;
			TypeBinding[] argumentTypes = Arrays.copyOfRange(method.parameters, 1, method.parameters.length);
			if ((receiverType instanceof ReferenceBinding) && ((ReferenceBinding) receiverType).getExactMethod(method.selector, argumentTypes, cuScope) != null) continue;
			extensionMethods.add(method);
		}
		return extensionMethods;
	}
	
	private static final Map<MessageSend, PostponedError> ERRORS = new WeakHashMap<MessageSend, PostponedError>();
	
	public static void errorNoMethodFor(ProblemReporter problemReporter, MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
		ERRORS.put(messageSend, new PostponedNoMethodError(problemReporter, messageSend, recType, params));
	}
	
	public static void invalidMethod(ProblemReporter problemReporter, MessageSend messageSend, MethodBinding method) {
		ERRORS.put(messageSend, new PostponedInvalidMethodError(problemReporter, messageSend, method));
	}
	
	public static TypeBinding resolveType(TypeBinding resolvedType, MessageSend methodCall, BlockScope scope) {
		List<Extension> extensions = new ArrayList<Extension>();
		TypeDeclaration decl = scope.classScope().referenceContext;
		
		EclipseNode owningType = null;
		
		for (EclipseNode typeNode = getTypeNode(decl); typeNode != null; typeNode = upToType(typeNode)) {
			Annotation ann = getAnnotation(ExtensionMethod.class, typeNode);
			if (ann != null) {
				extensions.addAll(0, getApplicableExtensionMethods(typeNode, ann, methodCall.receiver.resolvedType));
				if (owningType == null) owningType = typeNode;
			}
		}
		
		if (methodCall.binding == null) return resolvedType;
		
		for (Extension extension : extensions) {
			if (!extension.suppressBaseMethods && !(methodCall.binding instanceof ProblemMethodBinding)) continue;
			for (MethodBinding extensionMethod : extension.extensionMethods) {
				if (!Arrays.equals(methodCall.selector, extensionMethod.selector)) continue;
				ERRORS.remove(methodCall);
				if (methodCall.receiver instanceof ThisReference) {
					methodCall.receiver.bits &= ~ASTNode.IsImplicitThis;
				}
				List<Expression> arguments = new ArrayList<Expression>();
				arguments.add(methodCall.receiver);
				if (methodCall.arguments != null) arguments.addAll(Arrays.asList(methodCall.arguments));
				List<TypeBinding> argumentTypes = new ArrayList<TypeBinding>();
				argumentTypes.add(methodCall.receiver.resolvedType);
				if (methodCall.binding.parameters != null) argumentTypes.addAll(Arrays.asList(methodCall.binding.parameters));
				MethodBinding fixedBinding = scope.getMethod(extensionMethod.declaringClass, methodCall.selector, argumentTypes.toArray(new TypeBinding[0]), methodCall);
				if (fixedBinding instanceof ProblemMethodBinding) {
					if (fixedBinding.declaringClass != null) {
						scope.problemReporter().invalidMethod(methodCall, fixedBinding);
					}
				} else {
					for (int i = 0, iend = arguments.size(); i < iend; i++) {
						Expression arg = arguments.get(i);
						if (fixedBinding.parameters[i].isArrayType() != arg.resolvedType.isArrayType()) break;
						if (arg.resolvedType.isArrayType()) {
							if (arg instanceof MessageSend) {
								((MessageSend) arg).valueCast = arg.resolvedType;
							}
						}
						if (!fixedBinding.parameters[i].isBaseType() && arg.resolvedType.isBaseType()) {
							int id = arg.resolvedType.id;
							arg.implicitConversion = TypeIds.BOXING | (id + (id << 4)); // magic see TypeIds
						} else if (fixedBinding.parameters[i].isBaseType() && !arg.resolvedType.isBaseType()) {
							int id = fixedBinding.parameters[i].id;
							arg.implicitConversion = TypeIds.UNBOXING | (id + (id << 4)); // magic see TypeIds
						}
					}
					methodCall.arguments = arguments.toArray(new Expression[0]);
					
					methodCall.receiver = createNameRef(extensionMethod.declaringClass, methodCall);
					methodCall.actualReceiverType = extensionMethod.declaringClass;
					methodCall.binding = fixedBinding;
					methodCall.resolvedType = methodCall.binding.returnType;
				}
				return methodCall.resolvedType;
			}
		}
		
		PostponedError error = ERRORS.get(methodCall);
		if (error != null) error.fire();
		
		ERRORS.remove(methodCall);
		return resolvedType;
	}
	
	private static NameReference createNameRef(TypeBinding typeBinding, ASTNode source) {
		long p = ((long) source.sourceStart << 32) | source.sourceEnd;
		char[] pkg = typeBinding.qualifiedPackageName();
		char[] basename = typeBinding.qualifiedSourceName();
		
		StringBuilder sb = new StringBuilder();
		if (pkg != null) sb.append(pkg);
		if (sb.length() > 0) sb.append(".");
		sb.append(basename);
		
		String tName = sb.toString();
		
		if (tName.indexOf('.') == -1) {
			return new SingleNameReference(basename, p);
		} else {
			char[][] sources;
			String[] in = tName.split("\\.");
			sources = new char[in.length][];
			for (int i = 0; i < in.length; i++) sources[i] = in[i].toCharArray();
			long[] poss = new long[in.length];
			Arrays.fill(poss, p);
			return new QualifiedNameReference(sources, poss, source.sourceStart, source.sourceEnd);
		}
	}
	
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
	
	private static TypeBinding getFirstParameterType(TypeDeclaration decl, CompletionProposalCollector completionProposalCollector) {
		TypeBinding firstParameterType = null;
		ASTNode node = getAssistNode(completionProposalCollector);
		if (node == null) return null;
		if (!(node instanceof CompletionOnQualifiedNameReference) && !(node instanceof CompletionOnSingleNameReference) && !(node instanceof CompletionOnMemberAccess)) return null;
		
		if (node instanceof NameReference) {
			Binding binding = ((NameReference) node).binding;
			if ((node instanceof SingleNameReference) && (((SingleNameReference) node).token.length == 0)) {
				firstParameterType = decl.binding;
			} else if (binding instanceof VariableBinding) {
				firstParameterType = ((VariableBinding) binding).type;
			} else if (binding instanceof TypeBinding) {
				firstParameterType = (TypeBinding) binding;
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
	
	private static class Reflection {
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
		
		private static boolean isComplete() {
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
			object.setAccessible(true);
			return object;
		}
	}
}
