/*
 * Copyright (C) 2015 The Project Lombok Authors.
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

import lombok.patcher.ClassRootFinder;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.scripts.ScriptBuilder;

public class EclipseLoaderPatcher {
	private static final String TRANSPLANTS_CLASS_NAME = "lombok.eclipse.agent.EclipseLoaderPatcherTransplants";
	
	static final String[] OSGI_TYPES = {
		"org/eclipse/osgi/internal/baseadaptor/DefaultClassLoader",
		"org/eclipse/osgi/framework/adapter/core/AbstractClassLoader",
		"org/eclipse/osgi/internal/loader/ModuleClassLoader"
	};
	
	public static void patchEquinoxLoaders(ScriptManager sm, Class<?> launchingContext) {
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.target(new MethodTarget("org.eclipse.osgi.framework.adapter.core.AbstractClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.target(new MethodTarget("org.eclipse.osgi.internal.loader.ModuleClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.decisionMethod(new Hook(TRANSPLANTS_CLASS_NAME, "overrideLoadDecide", "boolean", "java.lang.ClassLoader", "java.lang.String", "boolean"))
				.valueMethod(new Hook(TRANSPLANTS_CLASS_NAME, "overrideLoadResult", "java.lang.Class", "java.lang.ClassLoader", "java.lang.String", "boolean"))
				.transplant()
				.request(StackRequest.THIS, StackRequest.PARAM1, StackRequest.PARAM2).build());
		
		sm.addScript(ScriptBuilder.addField().setPublic().setVolatile()
				.fieldType("Ljava/lang/ClassLoader;")
				.fieldName("lombok$shadowLoader")
				.targetClass("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader")
				.targetClass("org.eclipse.osgi.framework.adapter.core.AbstractClassLoader")
				.targetClass("org.eclipse.osgi.internal.loader.ModuleClassLoader")
				.build());
		
		sm.addScript(ScriptBuilder.addField().setPublic().setVolatile().setStatic()
				.fieldType("Ljava/lang/Class;")
				.fieldName("lombok$shadowLoaderClass")
				.targetClass("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader")
				.targetClass("org.eclipse.osgi.framework.adapter.core.AbstractClassLoader")
				.targetClass("org.eclipse.osgi.internal.loader.ModuleClassLoader")
				.build());
		
		sm.addScript(ScriptBuilder.addField().setPublic().setStatic().setFinal()
				.fieldType("Ljava/lang/String;")
				.fieldName("lombok$location")
				.targetClass("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader")
				.targetClass("org.eclipse.osgi.framework.adapter.core.AbstractClassLoader")
				.targetClass("org.eclipse.osgi.internal.loader.ModuleClassLoader")
				.value(ClassRootFinder.findClassRootOfClass(launchingContext))
				.build());
	}
}
