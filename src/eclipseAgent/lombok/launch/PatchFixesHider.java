/*
 * Copyright (C) 2010-2015 The Project Lombok Authors.
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lombok.eclipse.EclipseAugments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeRewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

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
		
		public static Class<?> shadowLoadClass(String name) {
			try {
				if (shadowLoader == null) {
					try {
						Class.forName("lombok.core.LombokNode");
						// If we get here, then lombok is already available.
						shadowLoader = Util.class.getClassLoader();
					} catch (ClassNotFoundException e) {
						// If we get here, it isn't, and we should use the shadowloader.
						shadowLoader = Main.createShadowClassLoader();
					}
				}
				
				return Class.forName(name, true, shadowLoader);
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
	
	public static final class Transform {
		private static final Method TRANSFORM;
		private static final Method TRANSFORM_SWAPPED;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.TransformEclipseAST");
			TRANSFORM = Util.findMethod(shadowed, "transform", Parser.class, CompilationUnitDeclaration.class);
			TRANSFORM_SWAPPED = Util.findMethod(shadowed, "transform_swapped", CompilationUnitDeclaration.class, Parser.class);
		}
		
		public static void transform(Parser parser, CompilationUnitDeclaration ast) throws IOException {
			Util.invokeMethod(TRANSFORM, parser, ast);
		}
		
		public static void transform_swapped(CompilationUnitDeclaration ast, Parser parser) throws IOException {
			Util.invokeMethod(TRANSFORM_SWAPPED, ast, parser);
		}
	}
	
	/** Contains patch code to support {@code @Delegate} */
	public static final class Delegate {
		private static final Method HANDLE_DELEGATE_FOR_TYPE;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchDelegatePortal");
			HANDLE_DELEGATE_FOR_TYPE = Util.findMethod(shadowed, "handleDelegateForType", Object.class);
		}
		
		public static boolean handleDelegateForType(Object classScope) {
			return (Boolean) Util.invokeMethod(HANDLE_DELEGATE_FOR_TYPE, classScope);
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
		private static final Method SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED;
		private static final Method SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2;
		private static final Method HANDLE_VAL_FOR_LOCAL_DECLARATION;
		private static final Method HANDLE_VAL_FOR_FOR_EACH;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchVal");
			SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED = Util.findMethod(shadowed, "skipResolveInitializerIfAlreadyCalled", Expression.class, BlockScope.class);
			SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2 = Util.findMethod(shadowed, "skipResolveInitializerIfAlreadyCalled2", Expression.class, BlockScope.class, LocalDeclaration.class);
			HANDLE_VAL_FOR_LOCAL_DECLARATION = Util.findMethod(shadowed, "handleValForLocalDeclaration", LocalDeclaration.class, BlockScope.class);
			HANDLE_VAL_FOR_FOR_EACH = Util.findMethod(shadowed, "handleValForForEach", ForeachStatement.class, BlockScope.class);
		}
		
		public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
			return (TypeBinding) Util.invokeMethod(SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED, expr, scope);
		}
		
		public static TypeBinding skipResolveInitializerIfAlreadyCalled2(Expression expr, BlockScope scope, LocalDeclaration decl) {
			return (TypeBinding) Util.invokeMethod(SKIP_RESOLVE_INITIALIZER_IF_ALREADY_CALLED2, expr, scope, decl);
		}
		
		public static boolean handleValForLocalDeclaration(LocalDeclaration local, BlockScope scope) {
			return (Boolean) Util.invokeMethod(HANDLE_VAL_FOR_LOCAL_DECLARATION, local, scope);
		}
		
		public static boolean handleValForForEach(ForeachStatement forEach, BlockScope scope) {
			return (Boolean) Util.invokeMethod(HANDLE_VAL_FOR_FOR_EACH, forEach, scope);
		}
	}
	
	/** Contains patch code to support {@code @ExtensionMethod} */
	public static final class ExtensionMethod {
		private static final Method RESOLVE_TYPE;
		private static final Method ERROR_NO_METHOD_FOR;
		private static final Method INVALID_METHOD, INVALID_METHOD2;
		
		static {
			Class<?> shadowed = Util.shadowLoadClass("lombok.eclipse.agent.PatchExtensionMethod");
			RESOLVE_TYPE = Util.findMethod(shadowed, "resolveType", TypeBinding.class, MessageSend.class, BlockScope.class);
			ERROR_NO_METHOD_FOR = Util.findMethod(shadowed, "errorNoMethodFor", ProblemReporter.class, MessageSend.class, TypeBinding.class, TypeBinding[].class);
			INVALID_METHOD = Util.findMethod(shadowed, "invalidMethod", ProblemReporter.class, MessageSend.class, MethodBinding.class);
			INVALID_METHOD2 = Util.findMethod(shadowed, "invalidMethod", ProblemReporter.class, MessageSend.class, MethodBinding.class, Scope.class);
		}
		
		public static TypeBinding resolveType(TypeBinding resolvedType, MessageSend methodCall, BlockScope scope) {
			return (TypeBinding) Util.invokeMethod(RESOLVE_TYPE, resolvedType, methodCall, scope);
		}
		
		public static void errorNoMethodFor(ProblemReporter problemReporter, MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
			Util.invokeMethod(ERROR_NO_METHOD_FOR, problemReporter, messageSend, recType, params);
		}
		
		public static void invalidMethod(ProblemReporter problemReporter, MessageSend messageSend, MethodBinding method) {
			Util.invokeMethod(INVALID_METHOD, problemReporter, messageSend, method);
		}
		
		public static void invalidMethod(ProblemReporter problemReporter, MessageSend messageSend, MethodBinding method, Scope scope) {
			Util.invokeMethod(INVALID_METHOD2, problemReporter, messageSend, method, scope);
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
			
			if ((Boolean)processor.getClass().getDeclaredField("fPublic").get(processor)) signature.append("public ");
			if ((Boolean)processor.getClass().getDeclaredField("fAbstract").get(processor)) signature.append("abstract ");
			
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
				
				signature.append("@").append(annotation.resolveTypeBinding().getQualifiedName());
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
		
		public static org.eclipse.jdt.core.dom.MethodDeclaration getRealMethodDeclarationNode(org.eclipse.jdt.core.IMethod sourceMethod, org.eclipse.jdt.core.dom.CompilationUnit cuUnit) throws JavaModelException {
			MethodDeclaration methodDeclarationNode = ASTNodeSearchUtil.getMethodDeclarationNode(sourceMethod, cuUnit);
			if (isGenerated(methodDeclarationNode)) {
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
				
				if (typeStack.isEmpty() && typeDeclaration != null) {
					String methodName = sourceMethod.getElementName();
					for (Object declaration : typeDeclaration.bodyDeclarations()) {
						if (declaration instanceof org.eclipse.jdt.core.dom.MethodDeclaration) {
							org.eclipse.jdt.core.dom.MethodDeclaration methodDeclaration = (org.eclipse.jdt.core.dom.MethodDeclaration) declaration;
							if (methodDeclaration.getName().toString().equals(methodName)) {
								return methodDeclaration;
							}
						}
					}
				}
			}
			return methodDeclarationNode;
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
		
		public static int fixRetrieveIdentifierEndPosition(int original, int end) {
			return original == -1 ? end : original;
		}
		
		public static int fixRetrieveEllipsisStartPosition(int original, int end) {
			return original == -1 ? end : original;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int original, int end) {
//			if (original == -1) {
//				Thread.dumpStack();
//			}
			 return original == -1 ? end : original;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int retVal, AbstractMethodDeclaration amd) {
			if (retVal != -1 || amd == null) return retVal;
			boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get(amd) != null;
			if (isGenerated) return amd.declarationSourceEnd;
			return -1;
		}
		
		public static int fixRetrieveRightBraceOrSemiColonPosition(int retVal, FieldDeclaration fd) {
			if (retVal != -1 || fd == null) return retVal;
			boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get(fd) != null;
			if (isGenerated) return fd.declarationSourceEnd;
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
			boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get(internalNode) != null;
			if (isGenerated) domNode.getClass().getField("$isGenerated").set(domNode, true);
		}
		
		public static void setIsGeneratedFlagForName(org.eclipse.jdt.core.dom.Name name, Object internalNode) throws Exception {
			if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
				boolean isGenerated = EclipseAugments.ASTNode_generatedBy.get((org.eclipse.jdt.internal.compiler.ast.ASTNode) internalNode) != null;
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
			return newChildren.toArray(new RewriteEvent[newChildren.size()]);
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
				if (m.getNameRange().getLength() > 0 && !m.getNameRange().equals(m.getSourceRange())) result.add(m);
			}
			return result.size() == methods.length ? methods : result.toArray(new IMethod[result.size()]);
		}
		
		public static SearchMatch[] removeGenerated(SearchMatch[] returnValue) {
			List<SearchMatch> result = new ArrayList<SearchMatch>();
			for (int j = 0; j < returnValue.length; j++) {
				SearchMatch searchResult = returnValue[j];
				if (searchResult.getElement() instanceof IField) {
					IField field = (IField) searchResult.getElement();
					
					// can not check for value=lombok because annotation is
					// not fully resolved
					IAnnotation annotation = field.getAnnotation("Generated");
					if (annotation != null) {
						// Method generated at field location, skip
						continue;
					}
					
				}
				result.add(searchResult);
			}
			return result.toArray(new SearchMatch[result.size()]);
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
	}
}
