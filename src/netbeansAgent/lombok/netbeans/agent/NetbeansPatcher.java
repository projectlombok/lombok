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

import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.equinox.EquinoxClassLoader;
import lombok.patcher.scripts.ScriptBuilder;

public class NetbeansPatcher {
	private NetbeansPatcher() {}
	
	public static void agentmain(@SuppressWarnings("unused") String agentArgs, Instrumentation instrumentation) throws Exception {
		registerPatchScripts(instrumentation, true);
	}
	
	public static void premain(@SuppressWarnings("unused") String agentArgs, Instrumentation instrumentation) throws Exception {
		registerPatchScripts(instrumentation, false);
	}
	
	private static void registerPatchScripts(Instrumentation instrumentation, boolean reloadExistingClasses) {
		ScriptManager sm = new ScriptManager();
		sm.registerTransformer(instrumentation);
		EquinoxClassLoader.addPrefix("lombok.");
		EquinoxClassLoader.registerScripts(sm);
		
		patchNetbeansJavac(sm);
		patchNetbeansMissingPositionAwareness(sm);
		
		if (reloadExistingClasses) sm.reloadClasses(instrumentation);
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
				.request()
				.transplant()
				.target(new MethodTarget("org.netbeans.modules.java.source.parsing.JavacParser", "createJavacTask",
						"com.sun.tools.javac.api.JavacTaskImpl",
						"ClasspathInfo", "DiagnosticListener", "java.lang.String", "boolean",
						"ClassNamesForFileOraculum", "CancelService"))
				.wrapMethod(new Hook("lombok/netbeans/agent/PatchFixes", "addTaskListenerWhenCallingJavac",
						"()V"))
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
				.requestExtra(StackRequest.PARAM1)
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget("org.netbeans.modules.java.source.parsing.FindMethodRegionsVisitor",
						"visitMethod"))
				.methodToReplace(new Hook("com/sun/source/util/SourcePositions", "getEndPosition",
						"(Lcom/sun/source/tree/CompilationUnitTree;Lcom/sun/source/tree/Tree;)J"))
				.replacementMethod(new Hook("lombok/netbeans/agent/PatchFixes", "returnMinus1ForGeneratedNode",
						"(Lcom/sun/source/util/SourcePositions;Lcom/sun/source/tree/CompilationUnitTree;Lcom/sun/source/tree/Tree;)J"))
				.build());
	}
}
