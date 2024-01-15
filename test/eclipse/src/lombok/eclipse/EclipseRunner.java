/*
 * Copyright (C) 2022 The Project Lombok Authors.
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
package lombok.eclipse;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.container.Module;
import org.eclipse.osgi.container.Module.StartOptions;
import org.eclipse.osgi.container.Module.State;
import org.eclipse.osgi.internal.location.EquinoxLocations;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * We have to load the osgi/eclipse classes before we run the test case. This is only possible using a custom runner.
 *
 */
public class EclipseRunner extends Runner {
	private Runner runner;
	private boolean debug = false;
	private static boolean setupDone;
	
	public EclipseRunner(Class<?> klass) throws InitializationError {
		try {
			if (!setupDone) {
				debug = System.getProperty("lombok.debug") != null;
				startEclipse(System.getProperty("lombok.testenv"));
				setupDone = true;
			}
		} catch (Throwable e) {
			throw new InitializationError(e);
		}
		
		runner = new BlockJUnit4ClassRunner(klass);
		
	}
	
	@Override
	public Description getDescription() {
		return runner.getDescription();
	}
	
	@Override
	public void run(RunNotifier notifier) {
		runner.run(notifier);
	}
	
	private void startEclipse(String path) throws Exception {
		Map<String, String> initialProperties = new HashMap<String, String>();
		
		File rootDir = new File(path);
		File pluginDir = new File(rootDir, "plugins/");
		
		StringBuilder bundleString = new StringBuilder();
		String osgiPlugin = null;
		File[] bundles = pluginDir.listFiles();
		Arrays.sort(bundles);
		for (File plugin : bundles) {
			String fileName = plugin.getName();
			String bundleName = fileName.substring(0, fileName.indexOf("_"));
			
			if (bundleName.equals("org.eclipse.osgi")) {
				osgiPlugin = plugin.toURI().toString();
			}
			
			if (bundleString.length() > 0) {
				bundleString.append(",");
			}
			bundleString.append(bundleName);
		}
		
		initialProperties.put(EclipseStarter.PROP_BUNDLES, bundleString.toString());
		initialProperties.put(EclipseStarter.PROP_INSTALL_AREA, "file:" + path);
		initialProperties.put(EquinoxLocations.PROP_INSTANCE_AREA, "file:" + path);
		initialProperties.put(EclipseStarter.PROP_FRAMEWORK, osgiPlugin);
		initialProperties.put("osgi.framework.useSystemProperties", "false");
		initialProperties.put(EclipseStarter.PROP_NOSHUTDOWN, "false");
		
		initialProperties.put("osgi.parentClassloader", "fwk");
		initialProperties.put("osgi.frameworkParentClassloader", "fwk");
		initialProperties.put("osgi.contextClassLoaderParent", "ext");
		initialProperties.put("osgi.context.bootdelegation", "*");
		initialProperties.put("org.osgi.framework.bootdelegation", "*");
		
		EclipseStarter.setInitialProperties(initialProperties);
		String[] args = new String[] {"-clean"};
		if (debug) args = new String[] {"-clean", "-console", "-consoleLog", "-debug"};
		BundleContext context = EclipseStarter.startup(args, null);
		
		Map<String, Module> moduleMap = new HashMap<String, Module>();
		for (Bundle b : context.getBundles()) {
			Module module = b.adapt(Module.class);
			moduleMap.put(b.getSymbolicName(), module);
		}
		
		Set<String> startedBundles = new HashSet<String>();
		// Mark the debug bundle as started, it will fail anyway...
		startedBundles.add("org.eclipse.debug.ui");
		
		startBundle(moduleMap, startedBundles, "org.apache.felix.scr");
		startBundle(moduleMap, startedBundles, "org.eclipse.jdt.core.manipulation");
		startBundle(moduleMap, startedBundles, "org.eclipse.jdt.ui");
		
		if (debug) {
			for (Bundle b : context.getBundles()) {
				System.out.println("BUNDLE: " + b.getSymbolicName() + " " + b.getVersion() + " " + b.getLocation() + " " + b.getState());
			}
		}
	}
	
	private void startBundle(Map<String, Module> moduleMap, Set<String> started, String bundle) throws BundleException {
		if (started.contains(bundle)) return;
		
		Module module = moduleMap.get(bundle);
		started.add(bundle);
		
		BundleWiring wiring = module.getBundle().adapt(BundleWiring.class);
		List<BundleWire> requiredWires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
		requiredWires.addAll(wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE));
		
		for (BundleWire bundleWire : requiredWires) {
			startBundle(moduleMap, started, bundleWire.getProvider().getSymbolicName());
		}
		
		startModule(module);
	}
	
	private void startModule(Module module) throws BundleException {
		if (module.getState() == State.LAZY_STARTING || module.getState() == State.RESOLVED) {
			if (debug) System.out.print("Starting bundle " + module.getBundle().getSymbolicName() + "...");
			try {
				module.start(StartOptions.LAZY_TRIGGER);
				if (debug) System.out.println(" [done]");
			} catch (Exception e) {
				if (debug) System.out.println(" [error]");
				e.printStackTrace();
			}
		}
	}
}
