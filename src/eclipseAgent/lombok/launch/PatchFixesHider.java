/*
 * Copyright (C) 2010-2024 The Project Lombok Authors.
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
package lombok.launch;

import static lombok.eclipse.EcjAugments.*;
import static lombok.eclipse.Eclipse.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeRewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.code.CallContext;
import org.eclipse.jdt.internal.corext.refactoring.code.SourceProvider;
import org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor.IncomingMemberVisibilityAdjustment;

import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.permit.Permit;

/** These contain a mix of the following:
 * <ul>
 * <li> 'dependency free' method wrappers that cross the shadowloader barrier.
 * <li> methods that directly patch, <em>but</em>, these should ALWAYS be transplanted.
 * </ul>
 * 
 * <strong>This class lives on the outside of the shadowloader barrier, and as a consequence, cannot access any other lombok code except other
 * code in the {@code lombok.launch} package!</strong>.
 * <p>
 * This class is package private with lots of public inner static classes. This hides all of them from IDE autocomplete dialogs and such but at the JVM
 * level the inner static class are just plain public, which is important, because calls to the contents of these inner static classes are injected into
 * various eclipse classes verbatim, and if they weren't public, the verifier wouldn't accept it.
 */
final class PatchFixesHider {
	
	/** These utility methods are only used 'internally', but because of transplant methods, the class (and its methods) still have to be public! */
	public static final class Util {
		private static ClassLoader shadowLoader;
		
		public static ClassLoader getShadowLoader() {
			if (shadowLoader == null) {
				try {
					Class.forName("lombok.core.LombokNode");
					// If we get here, then lombok is already available.
					shadowLoader = Util.class.getClassLoader();
				} catch (ClassNotFoundException e) {
					// If we get here, it isn't, and we should use the shadowloader.
					shadowLoader = Main.getShadowClassLoader();
				}
			}
			
			return shadowLoader;
		}
		
		public static Class<?> shadowLoadClass(String name) {
			try {
				return Class.forName(name, true, getShadowLoader());
			} catch (ClassNotFoundException e) {
				throw sneakyThrow(e);
			}
		}
		
		public static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
			try {
				return type.getDeclaredMethod(name, parameterTypes);
			} catch (NoSuchMethodException e) {
				throw sneakyThrow(e);
			}
		}
		
		public static Method findMethod(Class<?> type, String name, String... parameterTypes) {
			for (Method m : type.getDeclaredMethods()) {
				if (name.equals(m.getName()) && sameTypes(m.getParameterTypes(), parameterTypes)) {
					return m;
				}
			}
			throw sneakyThrow(new NoSuchMethodException(type.getName() + "::" + name));
		}
		
		public static Method findMethodAnyArgs(Class<?> type, String name) {
			for (Method m : type.getDeclaredMethods()) if (name.equals(m.getName())) return m;
			throw sneakyThrow(new NoSuchMethodException(type.getName() + "::" + name));
		}
		
		public static Object invokeMethod(Method method, Object... args) {
			try {
				return method.invoke(null, args);
			} catch (IllegalAccessException e) {
				throw sneakyThrow(e);
			} catch (InvocationTargetException e) {
				throw sneakyThrow(e.getCause());
			}
		}
		
		private static RuntimeException sneakyThrow(Throwable t) {
			if (t == null) throw new NullPointerException("t");
			Util.<RuntimeException>sneakyThrow0(t);
			return null;
		}
		
		@SuppressWarnings("unchecked")
		private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
			throw (T)t;
		}
		
		private static boolean sameTypes(Class<?>[] types, String[] typeNames) {
			if (types.length != typeNames.length) return false;
			for (int i = 0; i < types.length; i++) {
				if (!types[i].getName().equals(typeNames[i])) return false;
			}
			return true;
		}
		
		private static void prependToClassLoader(ClassLoader currentClassLoader, ClassLoader prepend) {
			try {
				Method prependParentMethod = Permit.getMethod(currentClassLoader.getClass(), "prependParent", ClassLoader.class);
				Permit.invoke(prependParentMethod, currentClassLoader, prepend);
			} catch (Throwable t) {
				// Ignore
			}
		}
		
		private static ClassLoader findJdtCoreClassLoader(ClassLoader classLoader) {
			try {
				Method getBundleMethod = Permit.getMethod(classLoader.getClass(), "getBundle");
				Object bundle = Permit.invoke(getBundleMethod, classLoader);
				
				Method getBundleContextMethod = Permit.getMethod(bundle.getClass(), "getBundleContext");
				Object bundleContext = Permit.invoke(getBundleContextMethod, bundle);
				
				Method getBundlesMethod = Permit.getMethod(bundleContext.getClass(), "getBundles");
				Object[] bundles = (Object[]) Permit.invoke(getBundlesMethod, bundleContext);
				
				for (Object searchBundle : bundles) {
					if (searchBundle.toString().startsWith("org.eclipse.jdt.core_")) {
						Method getModuleClassLoaderMethod = Permit.getMethod(searchBundle.getClass(), "getModuleClassLoader", boolean.class);
						return (ClassLoader) Permit.invoke(getModuleClassLoaderMethod, searchBundle, false);
					}
				}
			} catch (Throwable t) {
				// Ignore
			}
			return null;
		}
	}
	
	/** Contains patch fixes that are dependent on lombok internals. */
	public static final class LombokDeps {
		public static final Method ADD_LOMBOK_NOTES;
		public static final Method POST_COMPILER_BYTES_STRING;
		public static final Method POST_COMPILER_OUTPUTSTREAM;
		public static final Method POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchFixesShadowLoaded");
			ADD_LOMBOK_NOTES = Util.findMethod(shadowed, "addLombokNotesToEclipseAboutDialog", String.class, String.class);
			POST_COMPILER_BYTES_STRING = Util.findMethod(shadowed, "runPostCompiler", byte[].class, String.class);
			POST_COMPILER_OUTPUTSTREAM = Util.findMethod(shadowed, "runPostCompiler", OutputStream.class);
			POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING = Util.findMethod(shadowed, "runPostCompiler", BufferedOutputStream.class, String.class, String.class);
		}
		
		public static String addLombokNotesToEclipseAboutDialog(String origReturnValue, String key) {
			try {
				return (String) Util.invokeMethod(LombokDeps.ADD_LOMBOK_NOTES, origReturnValue, key);
			} catch (Throwable t) {
				return origReturnValue;
			}
		}
		
		public static byte[] runPostCompiler(byte[] bytes, String fileName) {
			return (byte[]) Util.invokeMethod(LombokDeps.POST_COMPILER_BYTES_STRING, bytes, fileName);
		}
		
		public static OutputStream runPostCompiler(OutputStream out) throws IOException {
			return (OutputStream) Util.invokeMethod(LombokDeps.POST_COMPILER_OUTPUTSTREAM, out);
		}
		
		public static BufferedOutputStream runPostCompiler(BufferedOutputStream out, String path, String name) throws IOException {
			return (BufferedOutputStream) Util.invokeMethod(LombokDeps.POST_COMPILER_BUFFEREDOUTPUTSTREAM_STRING_STRING, out, path, name);
		}
	}
	
	public static final class ModuleClassLoading {
		public static void parserClinit() {
			ClassLoader jdtCoreClassLoader = Util.findJdtCoreClassLoader(Parser.class.getClassLoader());
			ClassLoader currentClassLoader = ModuleClassLoading.class.getClassLoader();
			Util.prependToClassLoader(currentClassLoader, jdtCoreClassLoader);
		}
	}
	
	public static final class Transform {
		private static Method TRANSFORM;
		private static Method TRANSFORM_SWAPPED;
		
		private static synchronized void init(ClassLoader prepend) {
			if (TRANSFORM != null) return;
			
			Main.prependClassLoader(prepend);
			ClassLoader currentClassLoader = Transform.class.getClassLoader();
			Util.prependToClassLoader(currentClassLoader, prepend);
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.TransformEclipseAST");
			TRANSFORM = Util.findMethodAnyArgs(shadowed, "transform");
			TRANSFORM_SWAPPED = Util.findMethodAnyArgs(shadowed, "transform_swapped");
		}
		
		public static void transform(Object parser, Object ast) throws IOException {
			init(parser.getClass().getClassLoader());
			Util.invokeMethod(TRANSFORM, parser, ast);
		}
		
		public static void transform_swapped(Object ast, Object parser) throws IOException {
			init(parser.getClass().getClassLoader());
			Util.invokeMethod(TRANSFORM_SWAPPED, ast, parser);
		}
	}
	
	/** Contains patch code to support {@code @Delegate} */
	public static final class Delegate {
		private static final Method HANDLE_DELEGATE_FOR_TYPE;
		private static final Method ADD_GENERATED_DELEGATE_METHODS;
		public static final Method IS_DELEGATE_SOURCE_METHOD;
		public static final Method RETURN_ELEMENT_INFO;
		
		static {
			Class<?> shadowedPortal = Util.shadowLoadClass("lombok.eclipse.agent.PatchDelegatePortal");
			HANDLE_DELEGATE_FOR_TYPE = Util.findMethod(shadowedPortal, "handleDelegateForType", Object.class);
			ADD_GENERATED_DELEGATE_METHODS = Util.findMethod(shadowedPortal, "addGeneratedDelegateMethods", Object.class, Object.class);
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchDelegate");
			IS_DELEGATE_SOURCE_METHOD = Util.findMethod(shadowed, "isDelegateSourceMethod", Object.class);
			RETURN_ELEMENT_INFO = Util.findMethod(shadowed, "returnElementInfo", Object.class);
		}
		
		public static boolean handleDelegateForType(Object classScope) {
			return (Boolean) Util.invokeMethod(HANDLE_DELEGATE_FOR_TYPE, classScope);
		}
		
		public static Object[] addGeneratedDelegateMethods(Object returnValue, Object javaElement) {
			return (Object[]) Util.invokeMethod(ADD_GENERATED_DELEGATE_METHODS, returnValue, javaElement);
		}
		
		public static boolean isDelegateSourceMethod(Object sourceMethod) {
			return (Boolean) Util.invokeMethod(IS_DELEGATE_SOURCE_METHOD, sourceMethod);
		}
		
		public static Object returnElementInfo(Object delegateSourceMethod) {
			return Util.invokeMethod(RETURN_ELEMENT_INFO, delegateSourceMethod);
		}
	}
	
	/** Contains patch code to support {@code val} (eclipse specific) */
	public static final class ValPortal {
		private static final Method COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE;
		private static final Method COPY_INITIALIZATION_OF_LOCAL_DECLARATION;
		private static final Method ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT;
		private static final Method ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchValEclipsePortal");
			COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE = Util.findMethod(shadowed, "copyInitializationOfForEachIterable", Object.class);
			COPY_INITIALIZATION_OF_LOCAL_DECLARATION = Util.findMethod(shadowed, "copyInitializationOfLocalDeclaration", Object.class);
			ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT = Util.findMethod(shadowed, "addFinalAndValAnnotationToVariableDeclarationStatement", Object.class, Object.class, Object.class);
			ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION = Util.findMethod(shadowed, "addFinalAndValAnnotationToSingleVariableDeclaration", Object.class, Object.class, Object.class);
		}

		public static void copyInitializationOfForEachIterable(Object parser) {
			Util.invokeMethod(COPY_INITIALIZATION_OF_FOR_EACH_ITERABLE, parser);
		}
		
		public static void copyInitializationOfLocalDeclaration(Object parser) {
			Util.invokeMethod(COPY_INITIALIZATION_OF_LOCAL_DECLARATION, parser);
		}
		
		public static void addFinalAndValAnnotationToVariableDeclarationStatement(Object converter, Object out, Object in) {
			Util.invokeMethod(ADD_FINAL_AND_VAL_ANNOTATION_TO_VARIABLE_DECLARATION_STATEMENT, converter, out, in);
		}
		
		public static void addFinalAndValAnnotationToSingleVariableDeclaration(Object converter, Object out, Object in) {
			Util.invokeMethod(ADD_FINAL_AND_VAL_ANNOTATION_TO_SINGLE_VARIABLE_DECLARATION, converter, out, in);
		}
	}
	
	/** Contains patch code to support {@code val} (eclipse and ecj) */
	public static final class Val {
		private static final String BLOCK_SCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
		private static final String LOCAL_DECLARATION_SIG = "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration";
		private static final String FOREACH_STATEMENT_SIG = "org.eclipse.jdt.internal.compiler.ast.ForeachStatement";
		
		private static final Method HANDLE_VAL_FOR_LOCAL_DECLARATION;
		private static final Method HANDLE_VAL_FOR_FOR_EACH;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchVal");
			HANDLE_VAL_FOR_LOCAL_DECLARATION = Util.findMethod(shadowed, "handleValForLocalDeclaration", LOCAL_DECLARATION_SIG, BLOCK_SCOPE_SIG);
			HANDLE_VAL_FOR_FOR_EACH = Util.findMethod(shadowed, "handleValForForEach", FOREACH_STATEMENT_SIG, BLOCK_SCOPE_SIG);
		}
		
		public static boolean handleValForLocalDeclaration(Object local, Object scope) {
			return (Boolean) Util.invokeMethod(HANDLE_VAL_FOR_LOCAL_DECLARATION, local, scope);
		}
		
		public static boolean handleValForForEach(Object forEach, Object scope) {
			return (Boolean) Util.invokeMethod(HANDLE_VAL_FOR_FOR_EACH, forEach, scope);
		}
		
		/** 
		 * Patches local declaration to not call .resolveType() on the initializer expression if we've already done so (calling it twice causes weird errors) 
		 * This and the next method must be transplanted so that the return type is loaded in the correct class loader
		 */
		public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
			if (expr.resolvedType != null) return expr.resolvedType;
			try {
				return expr.resolveType(scope);
			} catch (NullPointerException e) {
				return null;
			} catch (ArrayIndexOutOfBoundsException e) {
				// This will occur internally due to for example 'val x = mth("X");', where mth takes 2 arguments.
				return null;
			}
		}
		
		public static TypeBinding skipResolveInitializerIfAlreadyCalled2(Expression expr, BlockScope scope, LocalDeclaration decl) {
			if (decl != null && LocalDeclaration.class.equals(decl.getClass()) && expr.resolvedType != null) return expr.resolvedType;
			try {
				return expr.resolveType(scope);
			} catch (NullPointerException e) {
				return null;
			} catch (ArrayIndexOutOfBoundsException e) {
				// This will occur internally due to for example 'val x = mth("X");', where mth takes 2 arguments.
				return null;
			}
		}
	}
	
	/** Contains patch code to support {@code @ExtensionMethod} */
	public static final class ExtensionMethod {
		private static final String MESSAGE_SEND_SIG = "org.eclipse.jdt.internal.compiler.ast.MessageSend";
		private static final String TYPE_BINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
		private static final String SCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.Scope";
		private static final String BLOCK_SCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
		private static final String TYPE_BINDINGS_SIG = "[Lorg.eclipse.jdt.internal.compiler.lookup.TypeBinding;";
		private static final String PROBLEM_REPORTER_SIG = "org.eclipse.jdt.internal.compiler.problem.ProblemReporter";
		private static final String METHOD_BINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.MethodBinding";
		private static final String AST_NODE_SIG = "org.eclipse.jdt.internal.compiler.ast.ASTNode";
		
		private static final Method RESOLVE_TYPE;
		private static final Method ERROR_NO_METHOD_FOR;
		private static final Method INVALID_METHOD, INVALID_METHOD2;
		private static final Method NON_STATIC_ACCESS_TO_STATIC_METHOD;
		private static final Method MODIFY_METHOD_PATTERN;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchExtensionMethod");
			RESOLVE_TYPE = Util.findMethod(shadowed, "resolveType", TYPE_BINDING_SIG, MESSAGE_SEND_SIG, BLOCK_SCOPE_SIG);
			ERROR_NO_METHOD_FOR = Util.findMethod(shadowed, "errorNoMethodFor", PROBLEM_REPORTER_SIG, MESSAGE_SEND_SIG, TYPE_BINDING_SIG, TYPE_BINDINGS_SIG);
			INVALID_METHOD = Util.findMethod(shadowed, "invalidMethod", PROBLEM_REPORTER_SIG, MESSAGE_SEND_SIG, METHOD_BINDING_SIG);
			INVALID_METHOD2 = Util.findMethod(shadowed, "invalidMethod", PROBLEM_REPORTER_SIG, MESSAGE_SEND_SIG, METHOD_BINDING_SIG, SCOPE_SIG);
			NON_STATIC_ACCESS_TO_STATIC_METHOD = Util.findMethod(shadowed, "nonStaticAccessToStaticMethod", PROBLEM_REPORTER_SIG, AST_NODE_SIG, METHOD_BINDING_SIG, MESSAGE_SEND_SIG);
			MODIFY_METHOD_PATTERN = Util.findMethod(shadowed, "modifyMethodPattern", Object.class);
		}
		
		public static Object resolveType(Object resolvedType, Object methodCall, Object scope) {
			return Util.invokeMethod(RESOLVE_TYPE, resolvedType, methodCall, scope);
		}
		
		public static void errorNoMethodFor(Object problemReporter, Object messageSend, Object recType, Object params) {
			Util.invokeMethod(ERROR_NO_METHOD_FOR, problemReporter, messageSend, recType, params);
		}
		
		public static void invalidMethod(Object problemReporter, Object messageSend, Object method) {
			Util.invokeMethod(INVALID_METHOD, problemReporter, messageSend, method);
		}
		
		public static void invalidMethod(Object problemReporter, Object messageSend, Object method, Object scope) {
			Util.invokeMethod(INVALID_METHOD2, problemReporter, messageSend, method, scope);
		}
		
		public static void nonStaticAccessToStaticMethod(Object problemReporter, Object location, Object method, Object messageSend) {
			Util.invokeMethod(NON_STATIC_ACCESS_TO_STATIC_METHOD, problemReporter, location, method, messageSend);
		}
		
		public static Object modifyMethodPattern(Object original) {
			return Util.invokeMethod(MODIFY_METHOD_PATTERN, original);
		}
	}
	
	/** Contains patch code to support Javadoc for generated methods */
	public static final class Javadoc {
		private static final Method GET_HTML;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchJavadoc");
			GET_HTML = Util.findMethod(shadowed, "getHTMLContentFromSource", Object.class, String.class, Object.class);
		}
		
		public static String getHTMLContentFromSource(String original, IJavaElement member) {
			return (String) Util.invokeMethod(GET_HTML, null, original, member);
		}
		
		public static String getHTMLContentFromSource(String original, Object instance, IJavaElement member) {
			return (String) Util.invokeMethod(GET_HTML, instance, original, member);
		}
	}
	
	/**
	 * Contains a mix of methods: ecj only, ecj+eclipse, and eclipse only. As a consequence, _EVERY_ method from here used for ecj MUST be
	 * transplanted, as ecj itself cannot load this class (signatures refer to things that don't exist in ecj-only mode).
	 * <p>
	 * Because of usage of transplant(), a bunch of these contain direct code and don't try to cross the shadowloader barrier.
	 */
	public static final class PatchFixes {
		public static boolean isGenerated(org.eclipse.jdt.core.dom.ASTNode node) {
			boolean result = false;
			try {
				result = ((Boolean)node.getClass().getField("$isGenerated").get(node)).booleanValue();
				if (!result && node.getParent() != null && node.getParent() instanceof org.eclipse.jdt.core.dom.QualifiedName) {
					result = isGenerated(node.getParent());
				}
			} catch (Exception e) {
				// better to assume it isn't generated
			}
			return result;
		}

		public static boolean isGenerated(org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
			boolean result = false;
			try {
				result = node.getClass().getField("$generatedBy").get(node) != null;
			} catch (Exception e) {
				// better to assume it isn't generated
			}
			return result;
		}

		public static boolean isGenerated(org.eclipse.jdt.core.IMember member) {
			boolean result = false;
			try {
				result = member.getNameRange().getLength() <= 0 || member.getNameRange().equals(member.getSourceRange());
			} catch (JavaModelException e) {
				// better to assume it isn't generated
			}
			return result;
		}
		
		public static boolean isBlockedVisitorAndGenerated(org.eclipse.jdt.core.dom.ASTNode node, org.eclipse.jdt.core.dom.ASTVisitor visitor) {
			if (visitor == null) return false;
			
			String className = visitor.getClass().getName();
			if (!(className.startsWith("org.eclipse.jdt.internal.corext.fix") || className.startsWith("org.eclipse.jdt.internal.ui.fix") || className.startsWith("org.eclipse.jdt.ls.core.internal.semantictokens.SemanticTokensVisitor"))) return false;
			if (className.equals("org.eclipse.jdt.internal.corext.fix.VariableDeclarationFixCore$WrittenNamesFinder")) return false;
			
			return isGenerated(node);
		}
		
		public static boolean isListRewriteOnGeneratedNode(org.eclipse.jdt.core.dom.rewrite.ListRewrite rewrite) {
			return isGenerated(rewrite.getParent());
		}
		
		public static boolean returnFalse(java.lang.Object object) {
			return false;
		}
		
		public static boolean returnTrue(java.lang.Object object) {
			return true;
		}
		
		@java.lang.SuppressWarnings({"unchecked", "rawtypes"}) public static java.util.List removeGeneratedNodes(java.util.List list) {
			try {
				java.util.List realNodes = new java.util.ArrayList(list.size());
				for (java.lang.Object node : list) {
					if(!isGenerated(((org.eclipse.jdt.core.dom.ASTNode)node))) {
						realNodes.add(node);
					}
				}
				return realNodes;
			} catch (Exception e) {
			}
			return list;
		}
		
		public static java.lang.String getRealMethodDeclarationSource(java.lang.String original, Object processor, org.eclipse.jdt.core.dom.MethodDeclaration declaration) throws Exception {
			if (!isGenerated(declaration)) return original;
			
			List<org.eclipse.jdt.core.dom.Annotation> annotations = new ArrayList<org.eclipse.jdt.core.dom.Annotation>();
			for (Object modifier : declaration.modifiers()) {
				if (modifier instanceof org.eclipse.jdt.core.dom.Annotation) {
					org.eclipse.jdt.core.dom.Annotation annotation = (org.eclipse.jdt.core.dom.Annotation)modifier;
					String qualifiedAnnotationName = annotation.resolveTypeBinding().getQualifiedName();
					if (!"java.lang.Override".equals(qualifiedAnnotationName) && !"java.lang.SuppressWarnings".equals(qualifiedAnnotationName)) annotations.add(annotation);
				}
			}
			
			StringBuilder signature = new StringBuilder();
			addAnnotations(annotations, signature);
			
			try {
				if ((Boolean)processor.getClass().getDeclaredField("fPublic").get(processor)) signature.append("public ");
				if ((Boolean)processor.getClass().getDeclaredField("fAbstract").get(processor)) signature.append("abstract ");
			} catch (Throwable t) { }
			
			signature
				.append(declaration.getReturnType2().toString())
				.append(" ").append(declaration.getName().getFullyQualifiedName())
				.append("(");
			
			boolean first = true;
			for (Object parameter : declaration.parameters()) {
				if (!first) signature.append(", ");
				first = false;
				// We should also add the annotations of the parameters
				signature.append(parameter);
			}
			
			signature.append(");");
			return signature.toString();
		}
		
		// part of getRealMethodDeclarationSource(...)
		public static void addAnnotations(List<org.eclipse.jdt.core.dom.Annotation> annotations, StringBuilder signature) {
			/*
			 * We SHOULD be able to handle the following cases:
			 * @Override
			 * @Override()
			 * @SuppressWarnings("all")
			 * @SuppressWarnings({"all", "unused"})
			 * @SuppressWarnings(value = "all")
			 * @SuppressWarnings(value = {"all", "unused"})
			 * @EqualsAndHashCode(callSuper=true, of="id")
			 * 
			 * Currently, we only seem to correctly support:
			 * @Override
			 * @Override() N.B. We lose the parentheses here, since there are no values. No big deal.
			 * @SuppressWarnings("all")
			 */
			for (org.eclipse.jdt.core.dom.Annotation annotation : annotations) {
				List<String> values = new ArrayList<String>();
				if (annotation.isSingleMemberAnnotation()) {
					org.eclipse.jdt.core.dom.SingleMemberAnnotation smAnn = (org.eclipse.jdt.core.dom.SingleMemberAnnotation) annotation;
					values.add(smAnn.getValue().toString());
				} else if (annotation.isNormalAnnotation()) {
					org.eclipse.jdt.core.dom.NormalAnnotation normalAnn = (org.eclipse.jdt.core.dom.NormalAnnotation) annotation;
					for (Object value : normalAnn.values()) values.add(value.toString());
				}
				
				signature.append("@").append(annotation.getTypeName().getFullyQualifiedName());
				if (!values.isEmpty()) {
					signature.append("(");
					boolean first = true;
					for (String string : values) {
						if (!first) signature.append(", ");
						first = false;
						signature.append('"').append(string).append('"');
					}
					signature.append(")");
				}
				signature.append(" ");
			}
		}
		
		public static org.eclipse.jdt.core.dom.MethodDeclaration getRealMethodDeclarationNode(org.eclipse.jdt.core.dom.MethodDeclaration original, org.eclipse.jdt.core.IMethod sourceMethod, org.eclipse.jdt.core.dom.CompilationUnit cuUnit) throws JavaModelException {
			if (!isGenerated(original)) return original;
			
			IType declaringType = sourceMethod.getDeclaringType();
			Stack<IType> typeStack = new Stack<IType>();
			while (declaringType != null) {
				typeStack.push(declaringType);
				declaringType = declaringType.getDeclaringType();
			}
			
			IType rootType = typeStack.pop();
			org.eclipse.jdt.core.dom.AbstractTypeDeclaration typeDeclaration = findTypeDeclaration(rootType, cuUnit.types());
			while (!typeStack.isEmpty() && typeDeclaration != null) {
				typeDeclaration = findTypeDeclaration(typeStack.pop(), typeDeclaration.bodyDeclarations());
			}
			
			String targetMethodName = sourceMethod.getElementName();
			List<String> targetMethodParameterTypes = new ArrayList<String>();
			for (String parameterType : sourceMethod.getParameterTypes()) {
				targetMethodParameterTypes.add(org.eclipse.jdt.core.Signature.toString(parameterType));
			}
			
			if (typeStack.isEmpty() && typeDeclaration != null) {
				for (Object declaration : typeDeclaration.bodyDeclarations()) {
					if (declaration instanceof org.eclipse.jdt.core.dom.MethodDeclaration) {
						org.eclipse.jdt.core.dom.MethodDeclaration methodDeclaration = (org.eclipse.jdt.core.dom.MethodDeclaration) declaration;
						
						if (!methodDeclaration.getName().toString().equals(targetMethodName)) continue;
						if (methodDeclaration.parameters().size() != targetMethodParameterTypes.size()) continue;
						if (!isGenerated(methodDeclaration)) continue;
						
						boolean parameterTypesEquals = true;
						for (int i = 0; i < methodDeclaration.parameters().size(); i++) {
							SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) methodDeclaration.parameters().get(i);
							if (!variableDeclaration.getType().toString().equals(targetMethodParameterTypes.get(i))) {
								parameterTypesEquals = false;
								break;
							}
						}
						if (parameterTypesEquals) {
							return methodDeclaration;
						}
					}
				}
			}
			return original;
		}
		
		// part of getRealMethodDeclarationNode
		public static org.eclipse.jdt.core.dom.AbstractTypeDeclaration findTypeDeclaration(IType searchType, List<?> nodes) {
			for (Object object : nodes) {
				if (object instanceof org.eclipse.jdt.core.dom.AbstractTypeDeclaration) {
					org.eclipse.jdt.core.dom.AbstractTypeDeclaration typeDeclaration = (org.eclipse.jdt.core.dom.AbstractTypeDeclaration) object;
					if (typeDeclaration.getName().toString().equals(searchType.getElementName()))
						return typeDeclaration;
				}
			}
			return null;
		}
		
		public static int getSourceEndFixed(int sourceEnd, org.eclipse.jdt.internal.compiler.ast.ASTNode node) throws Exception {
			if (sourceEnd == -1) {
				org.eclipse.jdt.internal.compiler.ast.ASTNode object = (org.eclipse.jdt.internal.compiler.ast.ASTNode)node.getClass().getField("$generatedBy").get(node);
				if (object != null) {
					return object.sourceEnd;
				}
			}
			return sourceEnd;
		}
		
		public static int fixRetrieveStartingCatchPosition(int original, int start) {
			return original == -1 ? start : original;
		}
		
		public static int fixRetrieveIdentifierEndPosition(int original, int start, int end) {
			if (original == -1) return end;
			if (original < start) return end;
			return original;
		}
		
		public static int fixRetrieveEllipsisStartPosition(int original, int end) {
			return original == -1 ? end : original;
		}
		
		public static int fixRetrieveStartBlockPosition(int original, int start) {
			return original == -1 ? start : original;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int original, int end) {
//			if (original == -1) {
//				Thread.dumpStack();
//			}
			 return original == -1 ? end : original;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int retVal, AbstractMethodDeclaration amd) {
			if (retVal != -1 || amd == null) return retVal;
			boolean isGenerated = ASTNode_generatedBy.get(amd) != null;
			if (isGenerated) return amd.declarationSourceEnd;
			return -1;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int retVal, FieldDeclaration fd) {
			if (retVal != -1 || fd == null) return retVal;
			boolean isGenerated = ASTNode_generatedBy.get(fd) != null;
			if (isGenerated) return fd.declarationSourceEnd;
			return -1;
		}
		
		public static int fixRetrieveProperRightBracketPosition(int retVal, Type type) {
			if (retVal != -1 || type == null) return retVal;
			if (isGenerated(type)) return type.getStartPosition() + type.getLength() - 1;
			return -1;
		}
		
		public static final int ALREADY_PROCESSED_FLAG = 0x800000;  //Bit 24
		
		public static boolean checkBit24(Object node) throws Exception {
			int bits = (Integer)(node.getClass().getField("bits").get(node));
			return (bits & ALREADY_PROCESSED_FLAG) != 0;
		}
		
		public static boolean skipRewritingGeneratedNodes(org.eclipse.jdt.core.dom.ASTNode node) throws Exception {
			return ((Boolean) node.getClass().getField("$isGenerated").get(node)).booleanValue();
		}
		
		public static void setIsGeneratedFlag(org.eclipse.jdt.core.dom.ASTNode domNode,
				org.eclipse.jdt.internal.compiler.ast.ASTNode internalNode) throws Exception {
			
			if (internalNode == null || domNode == null) return;
			boolean isGenerated = ASTNode_generatedBy.get(internalNode) != null;
			if (isGenerated) domNode.getClass().getField("$isGenerated").set(domNode, true);
		}
		
		public static void setIsGeneratedFlagForName(org.eclipse.jdt.core.dom.Name name, Object internalNode) throws Exception {
			if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
				boolean isGenerated = ASTNode_generatedBy.get((org.eclipse.jdt.internal.compiler.ast.ASTNode) internalNode) != null;
				if (isGenerated) name.getClass().getField("$isGenerated").set(name, true);
			}
		}
		
		public static RewriteEvent[] listRewriteHandleGeneratedMethods(RewriteEvent parent) {
			RewriteEvent[] children = parent.getChildren();
			List<RewriteEvent> newChildren = new ArrayList<RewriteEvent>();
			List<RewriteEvent> modifiedChildren = new ArrayList<RewriteEvent>();
			for (int i = 0; i < children.length; i++) {
				RewriteEvent child = children[i];
				boolean isGenerated = isGenerated((org.eclipse.jdt.core.dom.ASTNode) child.getOriginalValue());
				if (isGenerated) {
					boolean isReplacedOrRemoved = child.getChangeKind() == RewriteEvent.REPLACED || child.getChangeKind() == RewriteEvent.REMOVED;
					boolean convertingFromMethod = child.getOriginalValue() instanceof org.eclipse.jdt.core.dom.MethodDeclaration;
					if (isReplacedOrRemoved && convertingFromMethod && child.getNewValue() != null) {
						modifiedChildren.add(new NodeRewriteEvent(null, child.getNewValue()));
					}
				} else {
					newChildren.add(child);
				}
			}
			// Since Eclipse doesn't honor the "insert at specified location" for already existing members,
			// we'll just add them last
			newChildren.addAll(modifiedChildren);
			return newChildren.toArray(new RewriteEvent[0]);
		}
		
		public static int getTokenEndOffsetFixed(TokenScanner scanner, int token, int startOffset, Object domNode) throws CoreException {
			boolean isGenerated = false;
			try {
				isGenerated = (Boolean) domNode.getClass().getField("$isGenerated").get(domNode);
			} catch (Exception e) {
				// If this fails, better to break some refactor scripts than to crash eclipse.
			}
			if (isGenerated) return -1;
			return scanner.getTokenEndOffset(token, startOffset);
		}
		
		public static IMethod[] removeGeneratedMethods(IMethod[] methods) throws Exception {
			List<IMethod> result = new ArrayList<IMethod>();
			for (IMethod m : methods) {
				if (!isGenerated(m)) result.add(m);
			}
			return result.size() == methods.length ? methods : result.toArray(new IMethod[0]);
		}
		
		public static SearchResultGroup[] createFakeSearchResult(SearchResultGroup[] returnValue,
				Object/*
						 * org.eclipse.jdt.internal.corext.refactoring.rename.
						 * RenameFieldProcessor
						 */ processor) throws Exception {
			if (returnValue == null || returnValue.length == 0) {
				// if no matches were found, check if Data annotation is present on the class
				Field declaredField = processor.getClass().getDeclaredField("fField");
				if (declaredField != null) {
					declaredField.setAccessible(true);
					SourceField fField = (SourceField) declaredField.get(processor);
					IAnnotation dataAnnotation = fField.getDeclaringType().getAnnotation("Data");
					if (dataAnnotation != null) {
						// add fake item, to make refactoring checks pass
						return new SearchResultGroup[] {new SearchResultGroup(null, new SearchMatch[1])};
					}
				}
			}
			return returnValue;
		}
		
		public static SimpleName[] removeGeneratedSimpleNames(SimpleName[] in) throws Exception {
			Field f = SimpleName.class.getField("$isGenerated");
			
			int count = 0;
			for (int i = 0; i < in.length; i++) {
				if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) count++;
			}
			if (count == in.length) return in;
			SimpleName[] newSimpleNames = new SimpleName[count];
			count = 0;
			for (int i = 0; i < in.length; i++) {
				if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) newSimpleNames[count++] = in[i];
			}
			return newSimpleNames;
		}
		
		public static Name[] removeGeneratedNames(Name[] in) throws Exception {
			Field f = Name.class.getField("$isGenerated");
			
			int count = 0;
			for (int i = 0; i < in.length; i++) {
				if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) count++;
			}
			if (count == in.length) return in;
			Name[] newNames = new Name[count];
			count = 0;
			for (int i = 0; i < in.length; i++) {
				if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) newNames[count++] = in[i];
			}
			return newNames;
		}
		
		public static Annotation[] convertAnnotations(Annotation[] out, IAnnotatable annotatable) {
			IAnnotation[] in;
			
			try {
				in = annotatable.getAnnotations();
			} catch (Exception e) {
				return out;
			}
			
			if (out == null) return null;
			int toWrite = 0;
			
			for (int idx = 0; idx < out.length; idx++) {
				String oName = new String(out[idx].type.getLastToken());
				boolean found = false;
				for (IAnnotation i : in) {
					String name = i.getElementName();
					int li = name.lastIndexOf('.');
					if (li > -1) name = name.substring(li + 1);
					if (name.equals(oName)) {
						found = true;
						break;
					}
				}
				if (!found) out[idx] = null;
				else toWrite++;
			}
			
			Annotation[] replace = out;
			if (toWrite < out.length) {
				replace = new Annotation[toWrite];
				int idx = 0;
				for (int i = 0; i < out.length; i++) {
					if (out[i] == null) continue;
					replace[idx++] = out[i];
				}
			}
			
			return replace;
		}
		
		public static String getRealNodeSource(String original, org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
			if (!isGenerated(node)) return original;
			
			return node.toString();
		}
		
		public static java.lang.String getRealNodeSource(java.lang.String original, org.eclipse.jdt.core.dom.ASTNode node) throws Exception {
			if (!isGenerated(node)) return original;
			
			return node.toString();
		}
		
		public static boolean skipRewriteVisibility(IncomingMemberVisibilityAdjustment adjustment) {
			return isGenerated(adjustment.getMember());
		}
		
		public static String[] getRealCodeBlocks(String[] blocks, SourceProvider sourceProvider, CallContext callContext) {
			MethodDeclaration methodDeclaration = sourceProvider.getDeclaration();
			if (!isGenerated(methodDeclaration)) {
				return blocks;
			}
			
			try {
				// Replace parameter references with actual argument
				AST ast = methodDeclaration.getAST();
				List<?> parameters = methodDeclaration.parameters();
				for (int i = 0; i < parameters.size(); i++) {
					SingleVariableDeclaration param = (SingleVariableDeclaration) parameters.get(i);
					Object data = param.getProperty("org.eclipse.jdt.internal.corext.refactoring.code.ParameterData");
					List<SimpleName> names = Permit.get(Permit.permissiveGetField(data.getClass(), "fReferences"), data);
					
					for (SimpleName simpleName : names) {
						ASTNode copy = ASTNode.copySubtree(ast, callContext.arguments[i]);
						simpleName.getParent().setStructuralProperty(simpleName.getLocationInParent(), copy);
					}
				}
				// Convert AST to source
				StringBuilder sb = new StringBuilder();
				for (Object statement : methodDeclaration.getBody().statements()) {
					if (callContext.callMode != ASTNode.RETURN_STATEMENT && statement instanceof ReturnStatement) {
						ReturnStatement returnStatement = (ReturnStatement) statement;
						sb.append(returnStatement.getExpression());
					} else {
						sb.append(statement);
					}
				}
				return new String[] {sb.toString().trim()};
			} catch (Throwable e) {
				return blocks;
			}
		}
	}
	
	public static class FieldInitializer {
		public static final Field INFO_STACK;
		public static final Field FIELD_INFO;
		public static final Field SOURCE_FIELD_ELEMENT_INFO;
		public static final Field INITIALIZATION_SOURCE;
		public static final Field NODE;
		public static final boolean INITIALIZED;
		
		static {
			INFO_STACK = Permit.permissiveGetField(CompilationUnitStructureRequestor.class, "infoStack");
			FIELD_INFO = Permit.permissiveGetField(CompilationUnitStructureRequestor.class, "$fieldInfo");
			SOURCE_FIELD_ELEMENT_INFO = Permit.permissiveGetField(CompilationUnitStructureRequestor.class, "$sourceFieldElementInfo");
			INITIALIZATION_SOURCE = Permit.permissiveGetField(SourceFieldElementInfo.class, "initializationSource");
			NODE = Permit.permissiveGetField(FieldInfo.class, "node");
			INITIALIZED = INFO_STACK != null && FIELD_INFO != null && SOURCE_FIELD_ELEMENT_INFO != null && INITIALIZATION_SOURCE != null && NODE != null;
		}
		
		public static boolean storeFieldInfo(CompilationUnitStructureRequestor compilationUnitStructureRequestor) {
			try {
				if (INITIALIZED) {
					Stack<?> infoStack = Permit.get(INFO_STACK, compilationUnitStructureRequestor);
					Object fieldInfo = infoStack.peek();
					Permit.set(FIELD_INFO, compilationUnitStructureRequestor, fieldInfo);
				}
			} catch (Exception e) {
				// do not break eclipse
			}
			return false;
		}
		public static void storeSourceFieldElementInfo(SourceFieldElementInfo fieldInfo, CompilationUnitStructureRequestor compilationUnitStructureRequestor) {
			try {
				if (INITIALIZED) {
					Permit.set(SOURCE_FIELD_ELEMENT_INFO, compilationUnitStructureRequestor, fieldInfo);
				}
			} catch (Exception e) {
				// do not break eclipse
			}
		}
		
		public static void overwriteInitializer(CompilationUnitStructureRequestor compilationUnitStructureRequestor) {
			try {
				if (INITIALIZED) {
					FieldInfo fieldInfo = Permit.get(FIELD_INFO, compilationUnitStructureRequestor);
					Permit.set(FIELD_INFO, compilationUnitStructureRequestor, null);
					
					SourceFieldElementInfo sourceFieldElementInfo = Permit.get(SOURCE_FIELD_ELEMENT_INFO,compilationUnitStructureRequestor);
					Permit.set(SOURCE_FIELD_ELEMENT_INFO, compilationUnitStructureRequestor, null);
					
					if (sourceFieldElementInfo.getInitializationSource() != null) {
						AbstractVariableDeclaration node = Permit.get(NODE, fieldInfo);
						
						if (PatchFixes.isGenerated(node)) {
							Permit.set(INITIALIZATION_SOURCE, sourceFieldElementInfo, node.initialization.toString().toCharArray());
						}
					}
				}
			} catch (Exception e) {
				// do not break eclipse
			}
		}
	}
	
	public static class Tests {
		public static StringBuffer printMethod(AbstractMethodDeclaration methodDeclaration, int tab, StringBuffer output, TypeDeclaration type) {
			return (StringBuffer) printMethod(methodDeclaration, tab, (Object) output, type);
		}
		
		public static StringBuilder printMethod(AbstractMethodDeclaration methodDeclaration, int tab, StringBuilder output, TypeDeclaration type) {
			return (StringBuilder) printMethod(methodDeclaration, tab, (Object) output, type);
		}
		
		public static Object printMethod(AbstractMethodDeclaration methodDeclaration, int tab, Object output, TypeDeclaration type) {
			Map<String, String> docs = CompilationUnit_javadoc.get(methodDeclaration.compilationResult.compilationUnit);
			Method printIndent = Permit.permissiveGetMethod(org.eclipse.jdt.internal.compiler.ast.ASTNode.class, "printIndent", int.class, output.getClass());
			if (docs != null) {
				String signature = EclipseHandlerUtil.getSignature(type, methodDeclaration);
				String rawJavadoc = docs.get(signature);
				if (rawJavadoc != null) {
					for (String line : rawJavadoc.split("\r?\n")) {
						try {
							Appendable sb = (Appendable) Permit.invoke(printIndent, null, tab, output);
							sb.append(line).append("\n");
						} catch (Throwable e) {
							// Ignore
						}
					}
				}
			}
			Method printMethodDeclaration = Permit.permissiveGetMethod(AbstractMethodDeclaration.class, "print", int.class, output.getClass());
			Permit.invokeSneaky(printMethodDeclaration, methodDeclaration, tab, output);
			return output;
		}
		
		public static Object getBundle(Object original, Class<?> c) {
			if (original != null) {
				return original;
			}
			
			CodeSource codeSource = c.getProtectionDomain().getCodeSource();
			if (codeSource == null) {
				return null;
			}
			
			String jar = codeSource.getLocation().getFile();
			String bundleName = jar.substring(jar.lastIndexOf("/") + 1, jar.indexOf("_"));
			
			org.osgi.framework.Bundle[] bundles = org.eclipse.core.runtime.adaptor.EclipseStarter.getSystemBundleContext().getBundles();
			for (org.osgi.framework.Bundle bundle : bundles) {
				if (bundleName.equals(bundle.getSymbolicName())) {
					return bundle;
				}
			}
			return null;
		}
		
		public static boolean isImplicitCanonicalConstructor(AbstractMethodDeclaration method, Object parameter) {
			return (method.bits & IsCanonicalConstructor) != 0 && (method.bits & IsImplicit) != 0;
		}
		
		public static StringBuffer returnStringBuffer(Object p1, StringBuffer buffer) {
			return buffer;
		}

		public static StringBuilder returnStringBuilder(Object p1, StringBuilder buffer) {
			return buffer;
		}
	}
}
