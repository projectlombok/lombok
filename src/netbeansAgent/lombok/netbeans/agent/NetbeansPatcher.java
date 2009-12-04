/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
package lombok.netbeans.agent;

import java.lang.instrument.Instrumentation;

import lombok.core.Agent;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.scripts.ScriptBuilder;

/**
 * This is a java-agent that patches some of netbeans's classes so that lombok is initialized as Javac TaskListener,
 * allowing us to change AST nodes anytime netbeans parses source code. It also fixes some of the places in netbeans that
 * can't deal with generated code.
 * 
 * The hard work on figuring out where to patch has been done by Jan Lahoda (jlahoda@netbeans.org)
 */
public class NetbeansPatcher extends Agent {
	@Override
	public void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected) throws Exception {
		registerPatchScripts(instrumentation, injected);
	}
	
	private static void registerPatchScripts(Instrumentation instrumentation, boolean reloadExistingClasses) {
		ScriptManager sm = new ScriptManager();
		sm.registerTransformer(instrumentation);
		
		patchNetbeansClassLoader(sm);
		patchNetbeansJavac(sm);
		patchNetbeansMissingPositionAwareness(sm);
		
		if (reloadExistingClasses) sm.reloadClasses(instrumentation);
	}
	
	private static void patchNetbeansClassLoader(ScriptManager sm) {
		sm.addScript(ScriptBuilder.exitEarly()
				.transplant().request(StackRequest.PARAM1, StackRequest.PARAM2)
				.target(new MethodTarget("org.netbeans.StandardModule$OneModuleClassLoader", "<init>"))
				.decisionMethod(new Hook("lombok/netbeans/agent/PatchFixes", "addSelfToClassLoader", "(Lorg/netbeans/Module;Ljava/util/List;)V"))
				.build());
		sm.addScript(ScriptBuilder.exitEarly()
				.transplant()
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.target(new MethodTarget("org.netbeans.ProxyClassLoader", "getResource"))
				.decisionMethod(new Hook("lombok/netbeans/agent/PatchFixes", "getResource_decision", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Z"))
				.valueMethod(new Hook("lombok/netbeans/agent/PatchFixes", "getResource_value", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/net/URL;"))
				.build());
		sm.addScript(ScriptBuilder.exitEarly()
				.transplant()
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.target(new MethodTarget("org.netbeans.ProxyClassLoader", "getResources"))
				.decisionMethod(new Hook("lombok/netbeans/agent/PatchFixes", "getResources_decision", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Z"))
				.valueMethod(new Hook("lombok/netbeans/agent/PatchFixes", "getResources_value", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/util/Enumeration;"))
				.build());
		sm.addScript(ScriptBuilder.exitEarly()
				.transplant()
				.target(new MethodTarget("org.netbeans.ProxyClassLoader", "loadClass"))
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.decisionMethod(new Hook("lombok/netbeans/agent/PatchFixes", "loadClass_decision", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Z"))
				.valueMethod(new Hook("lombok/netbeans/agent/PatchFixes", "loadClass_value", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;"))
				.build());
	}
	
	private static void patchNetbeansJavac(ScriptManager sm) {
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.transplant()
				.target(new MethodTarget("com.sun.tools.javac.api.JavacTaskImpl", "setTaskListener"))
				.wrapMethod(new Hook("lombok/netbeans/agent/PatchFixes", "fixContentOnSetTaskListener",
						"(Lcom/sun/tools/javac/api/JavacTaskImpl;Lcom/sun/source/util/TaskListener;)V"))
				.build());
		
		sm.addScript(ScriptBuilder.wrapReturnValue()
				.request(StackRequest.RETURN_VALUE, StackRequest.PARAM1)
				.transplant()
				.target(new MethodTarget("org.netbeans.modules.java.source.parsing.JavacParser", "createJavacTask",
						"com.sun.tools.javac.api.JavacTaskImpl",
						"org.netbeans.api.java.source.ClasspathInfo", "javax.tools.DiagnosticListener", "java.lang.String", "boolean",
						"com.sun.tools.javac.api.ClassNamesForFileOraculum", "com.sun.tools.javac.util.CancelService"))
				.wrapMethod(new Hook("lombok/netbeans/agent/PatchFixes", "addTaskListenerWhenCallingJavac",
						"(Lcom/sun/tools/javac/api/JavacTaskImpl;Lorg/netbeans/api/java/source/ClasspathInfo;)V"))
				.build());
	}
	
	private static void patchNetbeansMissingPositionAwareness(ScriptManager sm) {
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.netbeans.modules.java.editor.overridden.ComputeAnnotations",
						"createAnnotations"))
				.methodToReplace(new Hook("com/sun/source/util/Trees", "getTree",
						"(Ljavax/lang/model/element/Element;)Lcom/sun/source/tree/Tree;"))
				.replacementMethod(new Hook("lombok/netbeans/agent/PatchFixes", "returnNullForGeneratedNode",
						"(Lcom/sun/source/util/Trees;Ljavax/lang/model/element/Element;Ljava/lang/Object;)" +
						"Lcom/sun/source/tree/Tree;"))
				.requestExtra(StackRequest.PARAM1).transplant()
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.netbeans.modules.java.source.parsing.FindMethodRegionsVisitor",
						"visitMethod"))
				.methodToReplace(new Hook("com/sun/source/util/SourcePositions", "getEndPosition",
						"(Lcom/sun/source/tree/CompilationUnitTree;Lcom/sun/source/tree/Tree;)J"))
				.replacementMethod(new Hook("lombok/netbeans/agent/PatchFixes", "returnMinus1ForGeneratedNode",
						"(Lcom/sun/source/util/SourcePositions;Lcom/sun/source/tree/CompilationUnitTree;Lcom/sun/source/tree/Tree;)J"))
				.transplant().build());
		
		sm.addScript(ScriptBuilder.wrapMethodCall()
				.target(new MethodTarget("org.netbeans.modules.java.source.save.CasualDiff", "filterHidden"))
				.methodToWrap(new Hook("java/lang/Iterable", "iterator", "()L/java/util/Iterator;"))
				.wrapMethod(new Hook("lombok/netbeans/agent/PatchFixes", "filterGenerated",
						"(Ljava/util/Iterator;)L/java/util/Iterator;"))
				.transplant().build());
	}
}
