/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.core.Agent;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.TargetMatcher;
import lombok.patcher.equinox.EquinoxClassLoader;
import lombok.patcher.scripts.ScriptBuilder;

/**
 * This is a java-agent that patches some of eclipse's classes so AST Nodes are handed off to Lombok
 * for modification before Eclipse actually uses them to compile, render errors, show code outlines,
 * create auto-completion dialogs, and anything else eclipse does with java code. See the *Transformer
 * classes in this package for more information about which classes are transformed and how they are
 * transformed.
 */
public class EclipsePatcher extends Agent {
	@Override
	public void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected) throws Exception {
		registerPatchScripts(instrumentation, injected, injected);
	}
	
	private static void registerPatchScripts(Instrumentation instrumentation, boolean reloadExistingClasses, boolean ecjOnly) {
		ScriptManager sm = new ScriptManager();
		sm.registerTransformer(instrumentation);
		if (!ecjOnly) {
			EquinoxClassLoader.addPrefix("lombok.");
			EquinoxClassLoader.registerScripts(sm);
		}
		
		patchAvoidReparsingGeneratedCode(sm);
		
		if (!ecjOnly) {
			patchLombokizeAST(sm);
			patchCatchReparse(sm);
			patchSetGeneratedFlag(sm);
			patchHideGeneratedNodes(sm);
			patchLiveDebug(sm);
		}
		
		if (reloadExistingClasses) sm.reloadClasses(instrumentation);
	}
	
	private static void patchHideGeneratedNodes(ScriptManager sm) {
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder", "findByNode"))
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder", "findByBinding"))
				.wrapMethod(new Hook("lombok.eclipse.agent.PatchFixes", "removeGeneratedSimpleNames", "org.eclipse.jdt.core.dom.SimpleName[]",
						"org.eclipse.jdt.core.dom.SimpleName[]"))
				.request(StackRequest.RETURN_VALUE).build());
		
		patchRefactorScripts(sm);
		patchFormatters(sm);
	}
	
	private static void patchFormatters(ScriptManager sm) {
		sm.addScript(ScriptBuilder.setSymbolDuringMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.ui.text.java.JavaFormattingStrategy", "format", "void"))
				.callToWrap(new Hook("org.eclipse.jdt.internal.corext.util.CodeFormatterUtil", "reformat", "org.eclipse.text.edits.TextEdit",
						"int", "java.lang.String", "int", "int", "int", "java.lang.String", "java.util.Map"))
				.symbol("lombok.disable").build());
	}
	
	private static void patchRefactorScripts(ScriptManager sm) {
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.rewrite.ASTRewrite", "replace"))
				.target(new MethodTarget("org.eclipse.jdt.core.dom.rewrite.ASTRewrite", "remove"))
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchFixes", "skipRewritingGeneratedNodes", "boolean",
						"org.eclipse.jdt.core.dom.ASTNode"))
				.transplant().request(StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor", "addConstructorRenames"))
				.methodToWrap(new Hook("org.eclipse.jdt.core.IType", "getMethods", "org.eclipse.jdt.core.IMethod[]"))
				.wrapMethod(new Hook("lombok.eclipse.agent.PatchFixes", "removeGeneratedMethods", "org.eclipse.jdt.core.IMethod[]",
						"org.eclipse.jdt.core.IMethod[]"))
				.transplant().build());
	}
	
	private static void patchCatchReparse(ScriptManager sm) {
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget("org.eclipse.jdt.core.dom.ASTConverter", "retrieveStartingCatchPosition"))
				.wrapMethod(new Hook("lombok.eclipse.agent.PatchFixes", "fixRetrieveStartingCatchPosition", "int", "int"))
				.transplant().request(StackRequest.PARAM1).build());
	}
	
	private static void patchSetGeneratedFlag(ScriptManager sm) {
		sm.addScript(ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.ASTNode")
				.fieldName("$generatedBy")
				.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
				.setPublic().setTransient().build());
		
		sm.addScript(ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.core.dom.ASTNode")
				.fieldName("$isGenerated").fieldType("Z")
				.setPublic().setTransient().build());
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new TargetMatcher() {
					@Override public boolean matches(String classSpec, String methodName, String descriptor) {
						if (!"convert".equals(methodName)) return false;
						
						List<String> fullDesc = MethodTarget.decomposeFullDesc(descriptor);
						if ("V".equals(fullDesc.get(0))) return false;
						if (fullDesc.size() < 2) return false;
						if (!fullDesc.get(1).startsWith("Lorg/eclipse/jdt/internal/compiler/ast/")) return false;
						return true;
					}
					
					@Override public Collection<String> getAffectedClasses() {
						return Collections.singleton("org.eclipse.jdt.core.dom.ASTConverter");
					}
				}).request(StackRequest.PARAM1, StackRequest.RETURN_VALUE)
				.wrapMethod(new Hook("lombok.eclipse.agent.PatchFixes", "setIsGeneratedFlag", "void",
						"org.eclipse.jdt.core.dom.ASTNode", "org.eclipse.jdt.internal.compiler.ast.ASTNode"))
				.transplant().build());
		
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new TargetMatcher() {
					@Override public boolean matches(String classSpec, String methodName, String descriptor) {
						if (!methodName.startsWith("convert")) return false;
						
						List<String> fullDesc = MethodTarget.decomposeFullDesc(descriptor);
						if (fullDesc.size() < 2) return false;
						if (!fullDesc.get(1).startsWith("Lorg/eclipse/jdt/internal/compiler/ast/")) return false;
						
						return true;
					}
					
					@Override public Collection<String> getAffectedClasses() {
						return Collections.singleton("org.eclipse.jdt.core.dom.ASTConverter");
					}
				}).methodToWrap(new Hook("org.eclipse.jdt.core.dom.SimpleName", "<init>", "void", "org.eclipse.jdt.core.dom.AST"))
				.requestExtra(StackRequest.PARAM1)
				.wrapMethod(new Hook("lombok.eclipse.agent.PatchFixes", "setIsGeneratedFlagForSimpleName", "void",
						"org.eclipse.jdt.core.dom.SimpleName", "java.lang.Object"))
				.transplant().build());
	}
	
	/**
	 * XXX LIVE DEBUG
	 * 
	 * Adds patches to improve error reporting in cases of extremely rare bugs that are hard to reproduce. These should be removed
	 * once the issue has been solved!
	 */
	private static void patchLiveDebug(ScriptManager sm) {
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(
						"org.eclipse.jdt.internal.compiler.ast.MethodDeclaration", "resolveStatements", "void"))
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchFixes", "debugPrintStateOfScope", "boolean", "java.lang.Object"))
				.request(StackRequest.THIS).build());
	}
	
	private static void patchAvoidReparsingGeneratedCode(ScriptManager sm) {
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.MethodDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration"))
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration", "boolean"))
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(PARSER_SIG, "parse", "void",
						"org.eclipse.jdt.internal.compiler.ast.Initializer",
						"org.eclipse.jdt.internal.compiler.ast.TypeDeclaration",
						"org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration"))
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchFixes", "checkBit24", "boolean", "java.lang.Object"))
				.request(StackRequest.PARAM1).build());
	}
	
	private static void patchLombokizeAST(ScriptManager sm) {
		sm.addScript(ScriptBuilder.addField()
				.targetClass("org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration")
				.fieldName("$lombokAST").fieldType("Ljava/lang/Object;")
				.setPublic().setTransient().build());
		
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		final String CUD_SIG = "org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration";
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "getMethodBodies", "void", CUD_SIG))
				.wrapMethod(new Hook("lombok.eclipse.TransformEclipseAST", "transform", "void", PARSER_SIG, CUD_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1).build());
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.target(new MethodTarget(PARSER_SIG, "endParse", CUD_SIG, "int"))
				.wrapMethod(new Hook("lombok.eclipse.TransformEclipseAST", "transform_swapped", "void", CUD_SIG, PARSER_SIG))
				.request(StackRequest.THIS, StackRequest.RETURN_VALUE).build());
	}
}
