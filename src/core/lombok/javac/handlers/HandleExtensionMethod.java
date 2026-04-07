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

import java.util.*;

import javax.lang.model.element.ElementKind;

import lombok.ConfigurationKeys;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.configuration.TypeName;
import lombok.experimental.ExtensionMethod;
import lombok.javac.Javac;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.spi.Provides;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@link ExtensionMethod} annotation for javac.
 */
@Provides(JavacASTVisitor.class)
@HandlerPriority(66560) // 2^16 + 2^10; we must run AFTER HandleVal which is at 2^16
public class HandleExtensionMethod extends JavacASTAdapter {
	@Override public void visitType(JavacNode typeNode, JCClassDecl type) {
		boolean isClassEnumInterfaceOrRecord = isClassEnumInterfaceOrRecord(typeNode);

		AnnotationValues<ExtensionMethod> extensionMethod = null;
		JavacNode source = typeNode;

		boolean suppressBaseMethodsIsExplicit = false;
		ExtensionMethod em = null;
		for (JavacNode jn : typeNode.down()) {
			if (jn.getKind() != AST.Kind.ANNOTATION) continue;
			JCAnnotation ann = (JCAnnotation) jn.get();
			JCTree typeTree = ann.annotationType;
			if (typeTree == null) continue;
			String typeTreeToString = typeTree.toString();
			if (!typeTreeToString.equals("ExtensionMethod") && !typeTreeToString.equals("lombok.experimental.ExtensionMethod")) continue;
			if (!typeMatches(ExtensionMethod.class, jn, typeTree)) continue;

			source = jn;
			extensionMethod = createAnnotation(ExtensionMethod.class, jn);
			deleteAnnotationIfNeccessary(jn, ExtensionMethod.class);

			suppressBaseMethodsIsExplicit = extensionMethod.isExplicit("suppressBaseMethods");

			handleExperimentalFlagUsage(jn, ConfigurationKeys.EXTENSION_METHOD_FLAG_USAGE, "@ExtensionMethod");

			em = extensionMethod.getInstance();
			if (!isClassEnumInterfaceOrRecord) {
				jn.addError("@ExtensionMethod can only be used on a class, an enum, an interface or a record");
				return;
			}
			break;
		}

		boolean defaultSuppressBaseMethods = suppressBaseMethodsIsExplicit ? true : !Boolean.FALSE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.EXTENSION_METHOD_SUPPRESS_BASE_METHODS));
		List<Extension> defaultExtensions = findDefaultExtensions(typeNode);

		List<Object> extensionProviders = extensionMethod != null ? extensionMethod.getActualExpressions("value") : Collections.emptyList();
		if (extensionMethod != null && extensionProviders.isEmpty() && !defaultExtensions.isEmpty()) {
			source.addWarning("@ExtensionMethod has no effect since no extension types were specified.");
			return;
		}

		final List<Extension> extensions = getExtensions(source, extensionProviders);
		if (extensions.isEmpty() && defaultExtensions.isEmpty()) return;
        extensions.addAll(defaultExtensions);

		boolean emSuppressBaseMethods = (extensionMethod != null && suppressBaseMethodsIsExplicit) ? em.suppressBaseMethods() : defaultSuppressBaseMethods;

		new ExtensionMethodReplaceVisitor(source, extensions, emSuppressBaseMethods).replace();

		source.rebuild();
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

	public List<Extension> findDefaultExtensions(JavacNode typeNode) {
		java.util.List<TypeName> configuredDefaults = typeNode.getAst().readConfiguration(ConfigurationKeys.EXTENSION_METHOD_DEFAULT_EXTENSIONS);
		if (configuredDefaults.isEmpty()) return Collections.<Extension>emptyList();

		List<Extension> extensions = new ArrayList<Extension>();
		for (TypeName cn : configuredDefaults) {
			Name name = typeNode.toName(cn.getName());

			Object module = null;
			if (Javac.getJavaCompilerVersion() >= 9) {
				module = typeNode.getSymbolTable().inferModule(Convert.packagePart(name));
				if (module == null) {
					module = typeNode.getSymbolTable().unnamedModule;
				}
			}
			ClassSymbol classSymbol = Javac.resolveIdent(JavaCompiler.instance(typeNode.getContext()), module, cn.getName());
			if ((classSymbol.flags() & (INTERFACE | ANNOTATION)) != 0) continue;

			extensions.add(getExtension(typeNode, (ClassType) classSymbol.type));
		}
		return extensions;
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
			Map<JCTree, JCTree> resolution = new JavacResolution(methodCallNode.getContext()).resolveMethodMember(methodCallNode);
			
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
					methodCall.args = methodCall.args.prepend(receiver);
					methodCall.meth = chainDotsString(annotationNode, extensionProvider.toString() + "." + methodName);
					recursiveSetGeneratedBy(methodCall.meth, methodCallNode);
					return;
				}
			}
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
