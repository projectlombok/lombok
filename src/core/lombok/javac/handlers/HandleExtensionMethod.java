/*
 * Copyright (C) 2012-2025 The Project Lombok Authors.
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
package lombok.javac.handlers;

import static com.sun.tools.javac.code.Flags.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import static lombok.javac.handlers.JavacResolver.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ElementKind;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.ExtensionMethod;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.spi.Provides;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

/**
 * Handles the {@link ExtensionMethod} annotation for javac.
 */
@Provides
@HandlerPriority(66560) // 2^16 + 2^10; we must run AFTER HandleVal which is at 2^16
public class HandleExtensionMethod extends JavacAnnotationHandler<ExtensionMethod> {
	@Override
	public void handle(final AnnotationValues<ExtensionMethod> annotation, final JCAnnotation source, final JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.EXTENSION_METHOD_FLAG_USAGE, "@ExtensionMethod");
		
		deleteAnnotationIfNeccessary(annotationNode, ExtensionMethod.class);
		JavacNode typeNode = annotationNode.up();
		boolean isClassEnumInterfaceOrRecord = isClassEnumInterfaceOrRecord(typeNode);
		
		if (!isClassEnumInterfaceOrRecord) {
			annotationNode.addError("@ExtensionMethod can only be used on a class, an enum, an interface or a record");
			return;
		}
		
		boolean suppressBaseMethods = annotation.getInstance().suppressBaseMethods();
		
		List<Object> extensionProviders = annotation.getActualExpressions("value");
		if (extensionProviders.isEmpty()) {
			annotationNode.addError(String.format("@%s has no effect since no extension types were specified.", ExtensionMethod.class.getName()));
			return;
		}
		final List<Extension> extensions = getExtensions(annotationNode, extensionProviders);
		if (extensions.isEmpty()) return;
		
		new ExtensionMethodReplaceVisitor(annotationNode, extensions, suppressBaseMethods).replace();
		
		annotationNode.rebuild();
	}
	
	
	public List<Extension> getExtensions(final JavacNode typeNode, final List<Object> extensionProviders) {
		List<Extension> extensions = new ArrayList<Extension>();
		for (Object extensionProvider : extensionProviders) {
			if (!(extensionProvider instanceof JCFieldAccess)) continue;
			JCFieldAccess provider = (JCFieldAccess) extensionProvider;
			if (!("class".equals(provider.name.toString()))) continue;
			Type providerType = CLASS.resolveMember(typeNode, provider.selected);
			if (providerType == null) continue;
			if ((providerType.tsym.flags() & (INTERFACE | ANNOTATION)) != 0) continue;
			
			extensions.add(getExtension(typeNode, (ClassType) providerType));
		}
		return extensions;
	}
	
	public Extension getExtension(final JavacNode typeNode, final ClassType extensionMethodProviderType) {
		List<MethodSymbol> extensionMethods = new ArrayList<MethodSymbol>();
		TypeSymbol tsym = extensionMethodProviderType.asElement();
		if (tsym != null) for (Symbol member : tsym.getEnclosedElements()) {
			if (member.getKind() != ElementKind.METHOD) continue;
			MethodSymbol method = (MethodSymbol) member;
			if ((method.flags() & STATIC) == 0) continue;
			if ((method.flags() & PUBLIC) == 0) continue;
			if (method.params().isEmpty()) continue;
			extensionMethods.add(method);
		}
		return new Extension(extensionMethods, tsym);
	}
	
	private static class Extension {
		final List<MethodSymbol> extensionMethods;
		final TypeSymbol extensionProvider;
		
		public Extension(List<MethodSymbol> extensionMethods, TypeSymbol extensionProvider) {
			this.extensionMethods = extensionMethods;
			this.extensionProvider = extensionProvider;
		}
	}
	
	private static class ExtensionMethodReplaceVisitor extends TreeScanner<Void, Void> {
		final JavacNode annotationNode;
		final List<Extension> extensions;
		final boolean suppressBaseMethods;
		final Set<String> names = new HashSet<String>();
		private JCTree resolvedMember;
		private Map<JCTree, JCTree> resolution;
		private final Set<JCTree> changed = new HashSet<JCTree>();
		
		public ExtensionMethodReplaceVisitor(JavacNode annotationNode, List<Extension> extensions, boolean suppressBaseMethods) {
			this.annotationNode = annotationNode;
			this.extensions = extensions;
			this.suppressBaseMethods = suppressBaseMethods;
			
			for (Extension extension : extensions) {
				for (MethodSymbol methodSymbol : extension.extensionMethods) {
					names.add(methodSymbol.name.toString());
				}
			}
		}
		
		public void replace() {
			annotationNode.up().get().accept(this, null);
		}
		
		@Override
		public Void visitMethodInvocation(final MethodInvocationTree tree, final Void p) {
			scan(tree.getTypeArguments(), p);
			scan(tree.getMethodSelect(), p);
			handleMethodCall((JCMethodInvocation) tree);
			scan(tree.getArguments(), p);
			return null;
		}
		
		private void handleMethodCall(final JCMethodInvocation methodCall) {
			JavacNode methodCallNode = annotationNode.getAst().get(methodCall);
			
			if (methodCallNode == null) {
				// This should mean the node does not exist in the source at all. This is the case for generated nodes, such as implicit super() calls.
				return;
			}
			
			JavacNode surroundingType = upToTypeNode(methodCallNode);
			
			TypeSymbol surroundingTypeSymbol = ((JCClassDecl)surroundingType.get()).sym;
			JCExpression receiver = receiverOf(methodCall);
			String methodName = methodNameOf(methodCall);
			
			if (!names.contains(methodName)) return;
			if ("this".equals(receiver.toString()) || "this".equals(methodName) || "super".equals(methodName)) return;
			Map<JCTree, JCTree> resolution = resolve(methodCallNode, suppressBaseMethods ? receiver : methodCall);
			
			JCTree resolvedMethodCall = resolution.get(methodCall);
			if (resolvedMethodCall == null || resolvedMethodCall.type == null) return;
			if (!suppressBaseMethods && !(resolvedMethodCall.type instanceof ErrorType)) return;
			
			JCTree resolvedReceiver = resolution.get(receiver);
			if (resolvedReceiver == null || resolvedReceiver.type == null) return;
			Type receiverType = resolvedReceiver.type;
			if (receiverType.isErroneous()) return;
			
			// Skip static method access
			Symbol sym = null;
			if (resolvedReceiver instanceof JCIdent) {
				sym = ((JCIdent) resolvedReceiver).sym;
			} else if (resolvedReceiver instanceof JCFieldAccess) {
				sym = ((JCFieldAccess) resolvedReceiver).sym;
			}
			if (sym instanceof ClassSymbol) return;
			
			Types types = Types.instance(annotationNode.getContext());
			for (Extension extension : extensions) {
				TypeSymbol extensionProvider = extension.extensionProvider;
				if (surroundingTypeSymbol == extensionProvider) continue;
				for (MethodSymbol extensionMethod : extension.extensionMethods) {
					if (!methodName.equals(extensionMethod.name.toString())) continue;
					Type extensionMethodType = extensionMethod.type;
					if (!MethodType.class.isInstance(extensionMethodType) && !ForAll.class.isInstance(extensionMethodType)) continue;
					Type firstArgType = types.erasure(extensionMethodType.asMethodType().argtypes.get(0));
					if (!types.isAssignable(receiverType, firstArgType)) continue;
					// The other calls resolve the same when the replaced call keeps its type
					boolean sameType = MethodType.class.isInstance(extensionMethodType) && !(resolvedMethodCall.type instanceof ErrorType) && types.isSameType(extensionMethodType.getReturnType(), resolvedMethodCall.type);
					methodCall.args = methodCall.args.prepend(receiver);
					methodCall.meth = chainDotsString(annotationNode, extensionProvider.toString() + "." + methodName);
					recursiveSetGeneratedBy(methodCall.meth, methodCallNode);
					if (!sameType) changed.add(methodCall);
					return;
				}
			}
		}
		
		// Attributing the member is the expensive part, so its result is reused until a call that may change a type
		// is replaced, and then only attributed again for an expression that can see that type.
		private Map<JCTree, JCTree> resolve(final JavacNode methodCallNode, final JCTree dependent) {
			JCTree member = outermostMember(methodCallNode);
			if (resolution == null || member == null || member != resolvedMember || (!changed.isEmpty() && dependsOnChange(dependent))) {
				resolution = new JavacResolution(methodCallNode.getContext()).resolveMethodMember(methodCallNode);
				resolvedMember = member;
				changed.clear();
			}
			return resolution;
		}
		
		// A replaced call, or a local variable or parameter, which takes its type from an expression of the member.
		private boolean dependsOnChange(final JCTree tree) {
			final boolean[] found = {false};
			new TreeScanner<Void, Void>() {
				@Override public Void scan(com.sun.source.tree.Tree node, Void p) {
					if (found[0] || node == null) return null;
					if (changed.contains(node) || isLocal(node)) {
						found[0] = true;
						return null;
					}
					return super.scan(node, p);
				}
			}.scan(tree, null);
			return found[0];
		}
		
		private boolean isLocal(final com.sun.source.tree.Tree node) {
			if (!(node instanceof JCIdent)) return false;
			JCTree resolved = resolution.get((JCTree) node);
			Symbol sym = resolved instanceof JCIdent ? ((JCIdent) resolved).sym : null;
			return sym instanceof VarSymbol && sym.owner instanceof MethodSymbol;
		}
		
		// The method, field or initializer that JavacResolution.resolveMethodMember copies and attributes.
		private JCTree outermostMember(final JavacNode node) {
			JCTree member = null;
			for (JavacNode n = node; n != null; n = n.up()) {
				Kind kind = n.getKind();
				if (kind == Kind.METHOD || kind == Kind.FIELD || kind == Kind.INITIALIZER) member = n.get();
			}
			return member;
		}
		
		private String methodNameOf(final JCMethodInvocation methodCall) {
			if (methodCall.meth instanceof JCIdent) {
				return ((JCIdent) methodCall.meth).name.toString();
			} else {
				return ((JCFieldAccess) methodCall.meth).name.toString();
			}
		}
		
		private JCExpression receiverOf(final JCMethodInvocation methodCall) {
			if (methodCall.meth instanceof JCIdent) {
				return annotationNode.getTreeMaker().Ident(annotationNode.toName("this"));
			} else {
				return ((JCFieldAccess) methodCall.meth).selected;
			}
		}
	}
}
