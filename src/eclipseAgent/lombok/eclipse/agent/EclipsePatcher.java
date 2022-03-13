/*
 * Copyright (C) 2009-2021 The Project Lombok Authors.
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

import static lombok.patcher.scripts.ScriptBuilder.*;
import static lombok.eclipse.agent.EclipseLoaderPatcher.OSGI_TYPES;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.core.AgentLauncher;
import lombok.patcher.Filter;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.TargetMatcher;
import lombok.patcher.TransplantMapper;
import lombok.patcher.scripts.ScriptBuilder;

/**
 * This is a java-agent that patches some of eclipse's classes so AST Nodes are handed off to Lombok
 * for modification before Eclipse actually uses them to compile, render errors, show code outlines,
 * create auto-completion dialogs, and anything else eclipse does with java code. See the *Transformer
 * classes in this package for more information about which classes are transformed and how they are
 * transformed.
 */
public class EclipsePatcher implements AgentLauncher.AgentLaunchable {
	@Override public void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected, Class<?> launchingContext) throws Exception {
		registerPatchScripts(instrumentation, injected, launchingContext);
	}
	
	private static void registerPatchScripts(Instrumentation instrumentation, boolean reloadExistingClasses, Class<?> launchingContext) {
		ScriptManager sm = new ScriptManager();
		sm.registerTransformer(instrumentation);
		sm.setFilter(new Filter() {
			@Override public boolean shouldTransform(ClassLoader loader, String className, Class<?> classBeingDefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
				if (loader != null) {					
					if (loader.getClass().getName().startsWith("org.sonar.classloader.")) return false; // Relevant to bug #2351
					if (loader.toString().contains("com.alexnederlof:jasperreports-plugin")) return false; //Relevant to bug #1036
				}
				if (!(loader instanceof URLClassLoader)) return true;
				ClassLoader parent = loader.getParent();
				if (parent == null) return true;
				return !parent.getClass().getName().startsWith("org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader");
			}
		});
		
		final boolean forceBaseResourceNames = shouldForceBaseResourceNames();
		sm.setTransplantMapper(new TransplantMapper() {
			public String mapResourceName(int classFileFormatVersion, String resourceName) {
				if (classFileFormatVersion < 50 || forceBaseResourceNames) return resourceName;
				return "Class50/" + resourceName;
			}
		});
		
		EclipseLoaderPatcher.patchEquinoxLoaders(sm, launchingContext);
		patchCatchReparse(sm);
		patchIdentifierEndReparse(sm);
		patchRetrieveEllipsisStartPosition(sm);
		patchRetrieveRightBraceOrSemiColonPosition(sm);
		patchRetrieveProperRightBracketPosition(sm);
		patchRetrieveStartBlockPosition(sm);
		patchSetGeneratedFlag(sm);
		patchDomAstReparseIssues(sm);
		patchHideGeneratedNodes(sm);
		patchPostCompileHookEclipse(sm);
		patchFixSourceTypeConverter(sm);
		patchListRewriteHandleGeneratedMethods(sm);
		patchSyntaxAndOccurrencesHighlighting(sm);
		patchSortMembersOperation(sm);
		patchExtractInterfaceAndPullUp(sm);
		patchAboutDialog(sm);
		patchEclipseDebugPatches(sm);
		patchJavadoc(sm);
		patchASTConverterLiterals(sm);
		patchASTNodeSearchUtil(sm);
		
		patchPostCompileHookEcj(sm);
		
		patchAvoidReparsingGeneratedCode(sm);
		patchLombokizeAST(sm);
		patchEcjTransformers(sm);
		patchExtensionMethod(sm);
		patchRenameField(sm);
		patchNullCheck(sm);
		
		if (reloadExistingClasses) sm.reloadClasses(instrumentation);
	}
	
	private static boolean shouldForceBaseResourceNames() {
		String shadowOverride = System.getProperty("shadow.override.lombok", "");
		if (shadowOverride == null || shadowOverride.length() == 0) return false;
		for (String part : shadowOverride.split("\\s*" + (File.pathSeparatorChar == ';' ? ";" : ":") + "\\s*")) {
			if (part.equalsIgnoreCase("lombok.jar")) return false;
		}
		return true;
	}
	
	private static void patchRenameField(ScriptManager sm) {
		/* RefactoringSearchEngine.search will not return results when renaming field and Data Annotation is present. Return a fake Element to make checks pass */
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor", "checkAccessorDeclarations", "org.eclipse.ltk.core.refactoring.RefactoringStatus", "org.eclipse.core.runtime.IProgressMonitor", "org.eclipse.jdt.core.IMethod"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine", "search", "org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup[]", "org.eclipse.jdt.core.search.SearchPattern","org.eclipse.jdt.core.search.IJavaSearchScope","org.eclipse.core.runtime.IProgressMonitor","org.eclipse.ltk.core.refactoring.RefactoringStatus"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "createFakeSearchResult", "org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup[]", "org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup[]", "java.lang.Object"))
				.requestExtra(StackRequest.THIS)
				.transplant().build());
		
		/* Filter search results which are Generated and based on Fields, e.g. Generated getters/setters */
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor", "addAccessorOccurrences", "void", "org.eclipse.core.runtime.IProgressMonitor", "org.eclipse.jdt.core.IMethod", "java.lang.String","java.lang.String","org.eclipse.ltk.core.refactoring.RefactoringStatus"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup", "getSearchResults", "org.eclipse.jdt.core.search.SearchMatch[]"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGenerated", "org.eclipse.jdt.core.search.SearchMatch[]", "org.eclipse.jdt.core.search.SearchMatch[]"))
				.transplant().build());
	}
	
	private static void patchExtractInterfaceAndPullUp(ScriptManager sm) {
		/* Fix sourceEnding for generated nodes to avoid null pointer */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.SourceElementNotifier", "notifySourceElementRequestor", "void", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration", "org.eclipse.jdt.internal.compiler.ast.TypeDeclaration", "org.eclipse.jdt.internal.compiler.ast.ImportReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt", "get", "int", "java.lang.Object"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getSourceEndFixed", "int", "int", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.requestExtra(StackRequest.PARAM1)
				.transplant().build());
		
		/* Make sure the generated source element is found instead of the annotation */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
			.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.ExtractInterfaceProcessor", "createMethodDeclaration", "void",
				"org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite", 
				"org.eclipse.jdt.core.dom.rewrite.ASTRewrite",
				"org.eclipse.jdt.core.dom.AbstractTypeDeclaration", 
				"org.eclipse.jdt.core.dom.MethodDeclaration"
			))
			.methodToWrap(new Hook("org.eclipse.jface.text.IDocument", "get", "java.lang.String", "int", "int"))
			.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getRealMethodDeclarationSource", "java.lang.String", "java.lang.String", "java.lang.Object", "org.eclipse.jdt.core.dom.MethodDeclaration"))
			.requestExtra(StackRequest.THIS, StackRequest.PARAM4)
			.transplant().build());
		
		/* Get real node source instead of the annotation */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
			.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.HierarchyProcessor", "createPlaceholderForSingleVariableDeclaration", "org.eclipse.jdt.core.dom.SingleVariableDeclaration",
				"org.eclipse.jdt.core.dom.SingleVariableDeclaration",
				"org.eclipse.jdt.core.ICompilationUnit",
				"org.eclipse.jdt.core.dom.rewrite.ASTRewrite"
			))
			.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.HierarchyProcessor", "createPlaceholderForType", "org.eclipse.jdt.core.dom.Type",
				"org.eclipse.jdt.core.dom.Type",
				"org.eclipse.jdt.core.ICompilationUnit",
				"org.eclipse.jdt.core.dom.rewrite.ASTRewrite"
			))
			.methodToWrap(new Hook("org.eclipse.jdt.core.IBuffer", "getText", "java.lang.String", "int", "int"))
			.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getRealNodeSource", "java.lang.String", "java.lang.String", "org.eclipse.jdt.core.dom.ASTNode"))
			.requestExtra(StackRequest.PARAM1)
			.transplant()
			.build());
		
		/* Do not add @Override's for generated methods */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.rewrite.ListRewrite", "insertFirst"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isListRewriteOnGeneratedNode", "boolean", "org.eclipse.jdt.core.dom.rewrite.ListRewrite"))
				.request(StackRequest.THIS)
				.transplant().build());
		
		/* Do not add comments for generated methods */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.ExtractInterfaceProcessor", "createMethodComment"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.core.dom.ASTNode"))
				.request(StackRequest.PARAM2)
				.transplant().build());
		
		/* Do not add a modifier to the generating annotation during pull up 
		 * 
		 * Example: Pull up a protected method (canEqual()/@EqualsAndHashCode)
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor$IncomingMemberVisibilityAdjustment", "rewriteVisibility"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "skipRewriteVisibility", "boolean", "org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor$IncomingMemberVisibilityAdjustment"))
				.request(StackRequest.THIS)
				.transplant()
				.build());
		
		/*
		 * ImportRemover sometimes removes lombok imports if a generated method/type gets changed. Skipping all generated nodes fixes this behavior.
		 * 
		 * Example: Create a class (Use.java) that uses a generated method (Test t; t.toString();) and pull up this generated method.
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.ImportRemover", "registerRemovedNode", "void", "org.eclipse.jdt.core.dom.ASTNode"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.core.dom.ASTNode"))
				.request(StackRequest.PARAM1)
				.transplant()
				.build());
	}
	
	private static void patchAboutDialog(ScriptManager sm) {
		/*
		 * Add a line about lombok (+ version info) to eclipse's about dialog.
		 * This is doable without patching, but we intentionally patch it so that presence of the lombok info
		 * in the about dialog can be used to ascertain that patching in general is doing something.
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.core.internal.runtime.Product", "getProperty", "java.lang.String", "java.lang.String"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$LombokDeps", "addLombokNotesToEclipseAboutDialog", "java.lang.String", "java.lang.String", "java.lang.String"))
				.request(StackRequest.RETURN_VALUE, StackRequest.PARAM1)
				.transplant().build());
	}
	
	private static void patchSyntaxAndOccurrencesHighlighting(ScriptManager sm) {
		/*
		 * Skip generated nodes for "visual effects" (syntax highlighting && highlight occurrences)
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.search.OccurrencesFinder", "addUsage"))
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.search.OccurrencesFinder", "addWrite"))
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingReconciler$PositionCollector", "visit", "boolean", "org.eclipse.jdt.core.dom.SimpleName"))
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingReconciler$PositionCollector", "visitLiteral", "boolean", "org.eclipse.jdt.core.dom.Expression"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.core.dom.ASTNode"))
				.valueMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "returnFalse", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1)
				.build());
	}
	
	private static void patchListRewriteHandleGeneratedMethods(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteAnalyzer$ListRewriter", "rewriteList"))
				.methodToReplace(new Hook("org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent", "getChildren", "org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent[]"))
				.replacementMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "listRewriteHandleGeneratedMethods", "org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent[]", "org.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent"))
				.build());
	}

	private static void patchSortMembersOperation(ScriptManager sm) {
		/* Fixes "sort members" action with @Data @Log
		 * I would have liked to patch sortMembers, but kept getting a VerifyError: Illegal type in constant pool
		 * So now I just patch all calling methods
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.CompilationUnit"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.CompilationUnit", "types", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.AnnotationTypeDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.AnnotationTypeDeclaration", "bodyDeclarations", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.AnonymousClassDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.AnonymousClassDeclaration", "bodyDeclarations", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.TypeDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.TypeDeclaration", "bodyDeclarations", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.EnumDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.EnumDeclaration", "bodyDeclarations", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SortElementsOperation$2", "visit", "boolean", "org.eclipse.jdt.core.dom.EnumDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.EnumDeclaration", "enumConstants", "java.util.List"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNodes", "java.util.List", "java.util.List"))
				.transplant().build());
	}
	
	private static void patchDomAstReparseIssues(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteAnalyzer", "visit"))
				.methodToReplace(new Hook("org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner", "getTokenEndOffset", "int", "int", "int"))
				.replacementMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getTokenEndOffsetFixed", "int", "org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner", "int", "int", "java.lang.Object"))
				.requestExtra(StackRequest.PARAM1)
				.transplant()
				.build());
		
	}
	
	private static void patchPostCompileHookEclipse(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder", "writeClassFileContents"))
				.target(new MethodTarget("org.eclipse.jdt.internal.core.builder.AbstractImageBuilder", "writeClassFileContents"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.compiler.ClassFile", "getBytes", "byte[]"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$LombokDeps", "runPostCompiler", "byte[]", "byte[]", "java.lang.String"))
				.requestExtra(StackRequest.PARAM3)
				.build());
	}
	
	private static void patchPostCompileHookEcj(ScriptManager sm) {
		sm.addScriptIfNotWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.tool.EclipseCompilerImpl", "outputClassFiles"))
				.methodToWrap(new Hook("javax.tools.JavaFileObject", "openOutputStream", "java.io.OutputStream"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$LombokDeps", "runPostCompiler", "java.io.OutputStream", "java.io.OutputStream"))
				.transplant().build());
		
		sm.addScriptIfNotWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.util.Util", "writeToDisk"))
				.methodToWrap(new Hook("java.io.BufferedOutputStream", "<init>", "void", "java.io.OutputStream", "int"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$LombokDeps", "runPostCompiler", "java.io.BufferedOutputStream", "java.io.BufferedOutputStream", "java.lang.String", "java.lang.String"))
				.requestExtra(StackRequest.PARAM2, StackRequest.PARAM3)
				.transplant().build());
	}
	
	private static void patchHideGeneratedNodes(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder", "findByNode", "org.eclipse.jdt.core.dom.SimpleName[]", "org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.core.dom.SimpleName"))
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder", "findByBinding"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedSimpleNames", "org.eclipse.jdt.core.dom.SimpleName[]",
						"org.eclipse.jdt.core.dom.SimpleName[]"))
				.request(StackRequest.RETURN_VALUE).build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder", "findByNode", "org.eclipse.jdt.core.dom.Name[]", "org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.core.dom.Name"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedNames", "org.eclipse.jdt.core.dom.Name[]",
						"org.eclipse.jdt.core.dom.Name[]"))
				.request(StackRequest.RETURN_VALUE).build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
			.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTNode", "accept", "void", "org.eclipse.jdt.core.dom.ASTVisitor"))
			.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isBlockedVisitorAndGenerated", "boolean", "org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.core.dom.ASTVisitor"))
			.request(StackRequest.THIS, StackRequest.PARAM1)
			.build());
		
		patchRefactorScripts(sm);
		patchFormatters(sm);
	}
	
	private static void patchFormatters(ScriptManager sm) {
		// before Eclipse Mars
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.setSymbolDuringMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.formatter.DefaultCodeFormatter", "formatCompilationUnit"))
				.callToWrap(new Hook("org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil", "parseCompilationUnit", "org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration", "char[]", "java.util.Map", "boolean"))
				.symbol("lombok.disable")
				.build());
		
		// Eclipse Mars and beyond
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.setSymbolDuringMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.formatter.DefaultCodeFormatter", "parseSourceCode"))
				.callToWrap(new Hook("org.eclipse.jdt.core.dom.ASTParser", "createAST", "org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.core.runtime.IProgressMonitor"))
				.symbol("lombok.disable")
				.build());
	}
	
	private static void patchRefactorScripts(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.rewrite.ASTRewrite", "replace"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.rewrite.ASTRewrite", "remove"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "skipRewritingGeneratedNodes", "boolean",
						"org.eclipse.jdt.core.dom.ASTNode"))
				.transplant().request(StackRequest.PARAM1).build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor", "addConstructorRenames"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.IType", "getMethods", "org.eclipse.jdt.core.IMethod[]"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "removeGeneratedMethods", "org.eclipse.jdt.core.IMethod[]",
						"org.eclipse.jdt.core.IMethod[]"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.TempOccurrenceAnalyzer", "visit", "boolean", "org.eclipse.jdt.core.dom.SimpleName"))
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameAnalyzeUtil$ProblemNodeFinder$NameNodeVisitor", "visit", "boolean", "org.eclipse.jdt.core.dom.SimpleName"))
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeParameterProcessor$RenameTypeParameterVisitor", "visit", "boolean", "org.eclipse.jdt.core.dom.SimpleName"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.core.dom.ASTNode"))
				.valueMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "returnTrue", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1)
				.transplant().build());
	}
	
	private static void patchCatchReparse(ScriptManager sm) {
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveStartingCatchPosition"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveStartingCatchPosition", "int", "int", "int"))
				.transplant().request(StackRequest.RETURN_VALUE, StackRequest.PARAM1).build());
	}
	
	private static void patchIdentifierEndReparse(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveIdentifierEndPosition"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveIdentifierEndPosition", "int", "int", "int", "int"))
				.transplant().request(StackRequest.RETURN_VALUE, StackRequest.PARAM1, StackRequest.PARAM2).build());
	}
	
	private static void patchRetrieveEllipsisStartPosition(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveEllipsisStartPosition"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveEllipsisStartPosition", "int", "int", "int"))
				.transplant().request(StackRequest.RETURN_VALUE, StackRequest.PARAM2).build());
	}
	
	private static void patchRetrieveStartBlockPosition(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveStartBlockPosition"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveStartBlockPosition", "int", "int", "int"))
				.transplant().request(StackRequest.RETURN_VALUE, StackRequest.PARAM2).build());
	}
	
	private static void patchRetrieveRightBraceOrSemiColonPosition(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
			.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "boolean", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
			.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ASTConverter", "retrieveRightBraceOrSemiColonPosition", "int", "int", "int"))
			.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveRightBraceOrSemiColonPosition", "int", "int", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
			.requestExtra(StackRequest.PARAM2)
			.transplant()
			.build());
		
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "boolean", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ASTConverter", "retrieveRightBrace", "int", "int", "int"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveRightBraceOrSemiColonPosition", "int", "int", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
				.requestExtra(StackRequest.PARAM2)
				.transplant()
				.build());
		
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.FieldDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ASTConverter", "retrieveRightBrace", "int", "int", "int"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveRightBraceOrSemiColonPosition", "int", "int", "org.eclipse.jdt.internal.compiler.ast.FieldDeclaration"))
				.requestExtra(StackRequest.PARAM1)
				.transplant()
				.build());
		
//		sm.addScript(ScriptBuilder.wrapReturnValue()
//				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveRightBraceOrSemiColonPosition"))
//				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveRightBrace"))
//				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveRightBraceOrSemiColonPosition", "int", "int", "int"))
//				.transplant().request(StackRequest.RETURN_VALUE, StackRequest.PARAM2).build());
	}

	private static void patchRetrieveProperRightBracketPosition(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
			.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "extractSubArrayType", "org.eclipse.jdt.core.dom.ArrayType", "org.eclipse.jdt.core.dom.ArrayType", "int", "int"))
			.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ASTConverter", "retrieveProperRightBracketPosition", "int", "int", "int"))
			.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveProperRightBracketPosition", "int", "int", "org.eclipse.jdt.core.dom.Type"))
			.requestExtra(StackRequest.PARAM1)
			.transplant()
			.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
			.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToArray", "org.eclipse.jdt.core.dom.ArrayType", "org.eclipse.jdt.core.dom.Type", "int", "int", "int", "org.eclipse.jdt.internal.compiler.ast.Annotation[][]"))
			.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ASTConverter", "retrieveProperRightBracketPosition", "int", "int", "int"))
			.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "fixRetrieveProperRightBracketPosition", "int", "int", "org.eclipse.jdt.core.dom.Type"))
			.requestExtra(StackRequest.PARAM1)
			.transplant()
			.build());
	}

	private static void patchSetGeneratedFlag(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.ASTNode")
				.fieldName("$generatedBy")
				.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
				.setPublic().setTransient().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.core.dom.ASTNode")
				.fieldName("$isGenerated").fieldType("Z")
				.setPublic().setTransient().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new TargetMatcher() {
					@Override public boolean matches(String classSpec, String methodName, String descriptor) {
						if (!"convert".equals(methodName)) return false;
						
						List<String> fullDesc = MethodTarget.decomposeFullDesc(descriptor);
						if ("V".equals(fullDesc.get(0))) return false;
						if (fullDesc.size() < 2) return false;
						if (!fullDesc.get(1).startsWith("Lorg/eclipse/jdt/internal/compiler/ast/")) return false;
						return true;
					}
					
					@Override public String describe() {
						return "ASTConverter:[all relevant]";
					}
					
					@Override public Collection<String> getAffectedClasses() {
						return Collections.singleton("org.eclipse.jdt.core.dom.ASTConverter");
					}
				}).request(StackRequest.PARAM1, StackRequest.RETURN_VALUE)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "boolean", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
				.request(StackRequest.PARAM2, StackRequest.RETURN_VALUE)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToFieldDeclaration", "org.eclipse.jdt.core.dom.FieldDeclaration", "org.eclipse.jdt.internal.compiler.ast.FieldDeclaration"))
/* Targets beneath are only patched because the resulting dom nodes should be marked if generated.
 * However I couldn't find a usecase where these were actually used
 */
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.NameReference"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToVariableDeclarationExpression", "org.eclipse.jdt.core.dom.VariableDeclarationExpression", "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToSingleVariableDeclaration", "org.eclipse.jdt.core.dom.SingleVariableDeclaration", "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToVariableDeclarationFragment", "org.eclipse.jdt.core.dom.VariableDeclarationFragment", "org.eclipse.jdt.internal.compiler.ast.FieldDeclaration"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToVariableDeclarationFragment", "org.eclipse.jdt.core.dom.VariableDeclarationFragment", "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertToVariableDeclarationStatement", "org.eclipse.jdt.core.dom.VariableDeclarationStatement", "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "createBaseType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference", "long[]", "org.eclipse.jdt.internal.compiler.ast.Annotation[][]", "char[][]", "int", "int", "boolean"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "createQualifiedType", "org.eclipse.jdt.core.dom.QualifiedType", "org.eclipse.jdt.internal.compiler.ast.TypeReference", "long[]", "org.eclipse.jdt.internal.compiler.ast.Annotation[][]", "char[][]", "int", "org.eclipse.jdt.core.dom.Type"))
/* Targets above are only patched because the resulting dom nodes should be marked if generated. */
				.request(StackRequest.PARAM1, StackRequest.RETURN_VALUE)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant().build());
		
		/* Set generated flag for SimpleNames */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new TargetMatcher() {
					@Override public boolean matches(String classSpec, String methodName, String descriptor) {
						if (!methodName.startsWith("convert")) return false;
						
						List<String> fullDesc = MethodTarget.decomposeFullDesc(descriptor);
						if (fullDesc.size() < 2) return false;
						if (!fullDesc.get(1).startsWith("Lorg/eclipse/jdt/internal/compiler/ast/")) return false;
						
						return true;
					}
					
					@Override public String describe() {
						return "ASTConverter::(all relevant)";
					}
					
					@Override public Collection<String> getAffectedClasses() {
						return Collections.singleton("org.eclipse.jdt.core.dom.ASTConverter");
					}
				}).methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "boolean", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM2)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.ASTNode", "boolean", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.Block", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM2)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.PrimitiveType", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant()
				.build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleType", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant()
				.build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.ParameterizedType", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant()
				.build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convertType", "org.eclipse.jdt.core.dom.Type", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.QualifiedType", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant()
				.build());

		/* Set generated flag for QualifiedNames */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "int", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "int", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM4)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "int", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "int", "org.eclipse.jdt.internal.compiler.ast.TypeReference"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.QualifiedName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM4)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM3)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setQualifiedNameNameAndSourceRanges", "org.eclipse.jdt.core.dom.QualifiedName", "char[][]", "long[]", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.QualifiedName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM3)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setTypeNameForAnnotation", "void", "org.eclipse.jdt.internal.compiler.ast.Annotation", "org.eclipse.jdt.core.dom.Annotation"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "setTypeNameForAnnotation", "void", "org.eclipse.jdt.internal.compiler.ast.Annotation", "org.eclipse.jdt.core.dom.Annotation"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.dom.QualifiedName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "setIsGeneratedFlagForName", "void",
						"org.eclipse.jdt.core.dom.Name", "java.lang.Object"))
				.transplant().build());
	}
	
	private static void patchAvoidReparsingGeneratedCode(ScriptManager sm) {
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.MethodDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.transplant()
				.request(StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration", "boolean"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.transplant()
				.request(StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.Initializer",
						"org.eclipse.jdt.internal.compiler.ast.TypeDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.transplant()
				.request(StackRequest.PARAM1).build());
	}
	
	private static void patchLombokizeAST(ScriptManager sm) {
		sm.addScript(ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration")
				.fieldName("$lombokAST").fieldType("Ljava/lang/Object;")
				.setPublic().setTransient().build());
		
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		final String CUD_SIG = "org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration";
		final String OBJECT_SIG = "java.lang.Object";
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "getMethodBodies", "void", CUD_SIG))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Transform", "transform", "void", OBJECT_SIG, OBJECT_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "endParse", CUD_SIG, "int"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Transform", "transform_swapped", "void", OBJECT_SIG, OBJECT_SIG))
				.request(StackRequest.THIS, StackRequest.RETURN_VALUE).build());
	}
	
	private static void patchEcjTransformers(ScriptManager sm) {
		addPatchesForDelegate(sm);
		addPatchesForVal(sm);
		addPatchesForValEclipse(sm);
	}
	
	private static void addPatchesForDelegate(ScriptManager sm) {
		final String CLASSSCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.ClassScope";
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(CLASSSCOPE_SIG, "buildFieldsAndMethods", "void"))
				.request(StackRequest.THIS)
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$Delegate", "handleDelegateForType", "boolean", "java.lang.Object"))
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.setSymbolDuringMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SelectionRequestor", "acceptSourceMethod"))
				.callToWrap(new Hook("org.eclipse.jdt.core.IType", "getMethods", "org.eclipse.jdt.core.IMethod[]"))
				.symbol("lombok.skipdelegates")
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.fieldName("$delegateMethods")
				.fieldType("Ljava/util/Map;")
				.setPublic()
				.setTransient()
				.targetClass("org.eclipse.jdt.internal.core.CompilationUnit")
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.internal.core.SourceTypeElementInfo", "getChildren", "org.eclipse.jdt.core.IJavaElement[]"))
				.request(StackRequest.RETURN_VALUE, StackRequest.THIS)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Delegate", "addGeneratedDelegateMethods", "java.lang.Object[]", "java.lang.Object", "java.lang.Object"))
				.build());
	}
	
	private static void addPatchesForValEclipse(ScriptManager sm) {
		final String LOCALDECLARATION_SIG = "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration";
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		final String VARIABLEDECLARATIONSTATEMENT_SIG = "org.eclipse.jdt.core.dom.VariableDeclarationStatement";
		final String SINGLEVARIABLEDECLARATION_SIG = "org.eclipse.jdt.core.dom.SingleVariableDeclaration";
		final String ASTCONVERTER_SIG = "org.eclipse.jdt.core.dom.ASTConverter";
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.fieldName("$initCopy")
				.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
				.setPublic()
				.setTransient()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.LocalDeclaration")
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.fieldName("$iterableCopy")
				.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
				.setPublic()
				.setTransient()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.LocalDeclaration")
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "consumeExitVariableWithInitialization", "void"))
				.request(StackRequest.THIS)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$ValPortal", "copyInitializationOfLocalDeclaration", "void", "java.lang.Object"))
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "consumeEnhancedForStatementHeader", "void"))
				.request(StackRequest.THIS)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$ValPortal", "copyInitializationOfForEachIterable", "void", "java.lang.Object"))
				.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(ASTCONVERTER_SIG, "setModifiers", "void", VARIABLEDECLARATIONSTATEMENT_SIG, LOCALDECLARATION_SIG))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$ValPortal", "addFinalAndValAnnotationToVariableDeclarationStatement",
						"void", "java.lang.Object", "java.lang.Object", "java.lang.Object"))
				.request(StackRequest.THIS, StackRequest.PARAM1, StackRequest.PARAM2).build());
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(ASTCONVERTER_SIG, "setModifiers", "void", SINGLEVARIABLEDECLARATION_SIG, LOCALDECLARATION_SIG))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$ValPortal", "addFinalAndValAnnotationToSingleVariableDeclaration",
						"void", "java.lang.Object", "java.lang.Object", "java.lang.Object"))
				.request(StackRequest.THIS, StackRequest.PARAM1, StackRequest.PARAM2).build());
	}
	
	private static void addPatchesForVal(ScriptManager sm) {
		final String LOCALDECLARATION_SIG = "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration";
		final String FOREACHSTATEMENT_SIG = "org.eclipse.jdt.internal.compiler.ast.ForeachStatement";
		final String EXPRESSION_SIG = "org.eclipse.jdt.internal.compiler.ast.Expression";
		final String BLOCKSCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
		final String TYPEBINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
		final String OBJECT_SIG = "java.lang.Object";
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(LOCALDECLARATION_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$Val", "handleValForLocalDeclaration", "boolean", OBJECT_SIG, OBJECT_SIG))
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget(LOCALDECLARATION_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.target(new MethodTarget(LOCALDECLARATION_SIG, "resolve", "void", BLOCKSCOPE_SIG, "boolean"))
				.methodToReplace(new Hook(EXPRESSION_SIG, "resolveType", TYPEBINDING_SIG, BLOCKSCOPE_SIG))
				.requestExtra(StackRequest.THIS)
				.replacementMethod(new Hook("lombok.launch.PatchFixesHider$Val", "skipResolveInitializerIfAlreadyCalled2", TYPEBINDING_SIG, EXPRESSION_SIG, BLOCKSCOPE_SIG, LOCALDECLARATION_SIG))
				.transplant()
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget(FOREACHSTATEMENT_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.methodToReplace(new Hook(EXPRESSION_SIG, "resolveType", TYPEBINDING_SIG, BLOCKSCOPE_SIG))
				.replacementMethod(new Hook("lombok.launch.PatchFixesHider$Val", "skipResolveInitializerIfAlreadyCalled", TYPEBINDING_SIG, EXPRESSION_SIG, BLOCKSCOPE_SIG))
				.transplant()
				.build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(FOREACHSTATEMENT_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$Val", "handleValForForEach", "boolean", OBJECT_SIG, OBJECT_SIG))
				.build());
	}
	
	private static void patchFixSourceTypeConverter(ScriptManager sm) {
		final String SOURCE_TYPE_CONVERTER_SIG = "org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter";
		final String I_ANNOTATABLE_SIG = "org.eclipse.jdt.core.IAnnotatable";
		final String ANNOTATION_SIG = "org.eclipse.jdt.internal.compiler.ast.Annotation";
		
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(SOURCE_TYPE_CONVERTER_SIG, "convertAnnotations", ANNOTATION_SIG + "[]", I_ANNOTATABLE_SIG))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "convertAnnotations", ANNOTATION_SIG + "[]", ANNOTATION_SIG + "[]", I_ANNOTATABLE_SIG))
				.request(StackRequest.PARAM1, StackRequest.RETURN_VALUE).build());
	}
	
	private static void patchEclipseDebugPatches(ScriptManager sm) {
		final String ASTNODE_SIG = "org.eclipse.jdt.core.dom.ASTNode";
		final String PATCH_DEBUG = "lombok.eclipse.agent.PatchDiagnostics";
		
		sm.addScriptIfWitness(OSGI_TYPES, exitEarly()
				.target(new MethodTarget(ASTNODE_SIG, "setSourceRange", "void", "int", "int"))
				.request(StackRequest.THIS)
				.request(StackRequest.PARAM1)
				.request(StackRequest.PARAM2)
				.decisionMethod(new Hook(PATCH_DEBUG, "setSourceRangeCheck", "boolean", "java.lang.Object", "int", "int"))
				.build());
	}
	
	private static void patchExtensionMethod(ScriptManager sm) {
		final String PATCH_EXTENSIONMETHOD = "lombok.launch.PatchFixesHider$ExtensionMethod";
		final String PATCH_EXTENSIONMETHOD_COMPLETIONPROPOSAL_PORTAL = "lombok.eclipse.agent.PatchExtensionMethodCompletionProposalPortal";
		final String MESSAGE_SEND_SIG = "org.eclipse.jdt.internal.compiler.ast.MessageSend";
		final String TYPE_BINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
		final String SCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.Scope";
		final String BLOCK_SCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
		final String TYPE_BINDINGS_SIG = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding[]";
		final String PROBLEM_REPORTER_SIG = "org.eclipse.jdt.internal.compiler.problem.ProblemReporter";
		final String METHOD_BINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.MethodBinding";
		final String COMPLETION_PROPOSAL_COLLECTOR_SIG = "org.eclipse.jdt.ui.text.java.CompletionProposalCollector";
		final String I_JAVA_COMPLETION_PROPOSAL_SIG = "org.eclipse.jdt.ui.text.java.IJavaCompletionProposal[]";
		final String AST_NODE = "org.eclipse.jdt.internal.compiler.ast.ASTNode";
		final String OBJECT_SIG = "java.lang.Object";
		
		sm.addScript(wrapReturnValue()
			.target(new MethodTarget(MESSAGE_SEND_SIG, "resolveType", TYPE_BINDING_SIG, BLOCK_SCOPE_SIG))
			.request(StackRequest.RETURN_VALUE)
			.request(StackRequest.THIS)
			.request(StackRequest.PARAM1)
			.wrapMethod(new Hook(PATCH_EXTENSIONMETHOD, "resolveType", OBJECT_SIG, OBJECT_SIG, OBJECT_SIG, OBJECT_SIG))
			.cast()
			.build());
		
		sm.addScript(replaceMethodCall()
			.target(new MethodTarget(MESSAGE_SEND_SIG, "resolveType", TYPE_BINDING_SIG, BLOCK_SCOPE_SIG))
			.methodToReplace(new Hook(PROBLEM_REPORTER_SIG, "errorNoMethodFor", "void", MESSAGE_SEND_SIG, TYPE_BINDING_SIG, TYPE_BINDINGS_SIG))
			.replacementMethod(new Hook(PATCH_EXTENSIONMETHOD, "errorNoMethodFor", "void", OBJECT_SIG, OBJECT_SIG, OBJECT_SIG, OBJECT_SIG))
			.build());
		
		sm.addScript(replaceMethodCall()
			.target(new MethodTarget(MESSAGE_SEND_SIG, "resolveType", TYPE_BINDING_SIG, BLOCK_SCOPE_SIG))
			.methodToReplace(new Hook(PROBLEM_REPORTER_SIG, "invalidMethod", "void", MESSAGE_SEND_SIG, METHOD_BINDING_SIG))
			.replacementMethod(new Hook(PATCH_EXTENSIONMETHOD, "invalidMethod", "void", OBJECT_SIG, OBJECT_SIG, OBJECT_SIG))
			.build());
		
		// Since eclipse mars; they added a param.
		sm.addScript(replaceMethodCall()
			.target(new MethodTarget(MESSAGE_SEND_SIG, "resolveType", TYPE_BINDING_SIG, BLOCK_SCOPE_SIG))
			.methodToReplace(new Hook(PROBLEM_REPORTER_SIG, "invalidMethod", "void", MESSAGE_SEND_SIG, METHOD_BINDING_SIG, SCOPE_SIG))
			.replacementMethod(new Hook(PATCH_EXTENSIONMETHOD, "invalidMethod", "void", OBJECT_SIG, OBJECT_SIG, OBJECT_SIG, OBJECT_SIG))
			.build());
		
		sm.addScript(replaceMethodCall()
			.target(new MethodTarget(MESSAGE_SEND_SIG, "resolveType", TYPE_BINDING_SIG, BLOCK_SCOPE_SIG))
			.methodToReplace(new Hook(PROBLEM_REPORTER_SIG, "nonStaticAccessToStaticMethod", "void", AST_NODE, METHOD_BINDING_SIG))
			.replacementMethod(new Hook(PATCH_EXTENSIONMETHOD, "nonStaticAccessToStaticMethod", "void", OBJECT_SIG, OBJECT_SIG, OBJECT_SIG, OBJECT_SIG))
			.requestExtra(StackRequest.THIS)
			.build());
		
		sm.addScriptIfWitness(OSGI_TYPES, wrapReturnValue()
			.target(new MethodTarget(COMPLETION_PROPOSAL_COLLECTOR_SIG, "getJavaCompletionProposals", I_JAVA_COMPLETION_PROPOSAL_SIG))
			.request(StackRequest.RETURN_VALUE)
			.request(StackRequest.THIS)
			.wrapMethod(new Hook(PATCH_EXTENSIONMETHOD_COMPLETIONPROPOSAL_PORTAL, "getJavaCompletionProposals", I_JAVA_COMPLETION_PROPOSAL_SIG, "java.lang.Object[]", "java.lang.Object"))
			.build());
	}

	private static void patchNullCheck(ScriptManager sm) {
		/* Avoid warnings caused by the null check generated for lombok.NonNull if NonNullByDefault is used. */

		/* Avoid "Redundant null check: comparing '@NonNull String' against null" */
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.problem.ProblemReporter", "expressionNonNullComparison", "boolean", "org.eclipse.jdt.internal.compiler.ast.Expression", "boolean"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.valueMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "returnTrue", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1)
				.transplant().build());

		/* Avoid "Dead code" */
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.problem.ProblemReporter", "fakeReachable", "void", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.decisionMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "isGenerated", "boolean", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.request(StackRequest.PARAM1)
				.transplant().build());
	}
	
	private static void patchJavadoc(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2", "getHTMLContent", "java.lang.String", "org.eclipse.jdt.core.IJavaElement", "boolean"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2", "getHTMLContentFromSource", "java.lang.String", "org.eclipse.jdt.core.IJavaElement"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Javadoc", "getHTMLContentFromSource", "java.lang.String", "java.lang.String", "org.eclipse.jdt.core.IJavaElement"))
				.requestExtra(StackRequest.PARAM1)
				.build());
		
		/* This is a copy for the language server implementation that also supports markdown */
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.ls.core.internal.javadoc.JavadocContentAccess2", "getHTMLContent", "java.lang.String", "org.eclipse.jdt.core.IJavaElement", "boolean"))
				.methodToWrap(new Hook("org.eclipse.jdt.ls.core.internal.javadoc.JavadocContentAccess2", "getHTMLContentFromSource", "java.lang.String", "org.eclipse.jdt.core.IJavaElement"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Javadoc", "getHTMLContentFromSource", "java.lang.String", "java.lang.String", "org.eclipse.jdt.core.IJavaElement"))
				.requestExtra(StackRequest.PARAM1)
				.build());
		
		/* This is an older version that uses IMember instead of IJavaElement */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2", "getHTMLContent", "java.lang.String", "org.eclipse.jdt.core.IMember", "boolean"))
				.methodToWrap(new Hook("org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2", "getHTMLContentFromSource", "java.lang.String", "org.eclipse.jdt.core.IMember"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$Javadoc", "getHTMLContentFromSource", "java.lang.String", "java.lang.String", "org.eclipse.jdt.core.IJavaElement"))
				.requestExtra(StackRequest.PARAM1)
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.compiler.ast.TypeDeclaration", "printBody", "java.lang.StringBuffer", "int", "java.lang.StringBuffer"))
				.methodToReplace(new Hook("org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration", "print", "java.lang.StringBuffer", "int", "java.lang.StringBuffer"))
				.replacementMethod(new Hook("lombok.launch.PatchFixesHider$Javadoc", "printMethod", "java.lang.StringBuffer", "org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration", "int", "java.lang.StringBuffer", "org.eclipse.jdt.internal.compiler.ast.TypeDeclaration"))
				.requestExtra(StackRequest.THIS)
				.build());

		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.addField()
				.fieldName("$javadoc")
				.fieldType("Ljava/util/Map;")
				.setPublic()
				.setTransient()
				.targetClass("org.eclipse.jdt.internal.core.CompilationUnit")
				.build());
	}

	private static void patchASTConverterLiterals(ScriptManager sm) {
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.Expression", "org.eclipse.jdt.internal.compiler.ast.StringLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.Expression", "org.eclipse.jdt.internal.compiler.ast.TextBlock"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.CharacterLiteral", "org.eclipse.jdt.internal.compiler.ast.CharLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.DoubleLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.FloatLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.LongLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.IntLiteral"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "convert", "org.eclipse.jdt.core.dom.NumberLiteral", "org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue"))
				.methodToWrap(new Hook("java.lang.String", "<init>", "void", "char[]", "int", "int"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getRealNodeSource", "java.lang.String", "java.lang.String", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant()
				.build());
	}
	
	private static void patchASTNodeSearchUtil(ScriptManager sm) {
		/*
		 * If an annotation generates more than one method the normal node search returns one of them instead of the right one. 
		 * This patch method also compares method name and method parameter types to identify the right method.
		 */
		sm.addScriptIfWitness(OSGI_TYPES, ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil", "getMethodDeclarationNode", "org.eclipse.jdt.core.dom.MethodDeclaration", "org.eclipse.jdt.core.IMethod", "org.eclipse.jdt.core.dom.CompilationUnit"))
				.wrapMethod(new Hook("lombok.launch.PatchFixesHider$PatchFixes", "getRealMethodDeclarationNode", "org.eclipse.jdt.core.dom.MethodDeclaration", "org.eclipse.jdt.core.dom.MethodDeclaration", "org.eclipse.jdt.core.IMethod", "org.eclipse.jdt.core.dom.CompilationUnit"))
				.request(StackRequest.RETURN_VALUE, StackRequest.PARAM1, StackRequest.PARAM2)
				.transplant()
				.build());
	}

}
