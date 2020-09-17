package lombok.eclipseCreate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CreateEclipseDebugTarget {
	private Map<String, String> args;
	private StringBuilder launchContent = new StringBuilder();
	
	private static class InvalidCommandLineException extends Exception {
		InvalidCommandLineException(String msg) {
			super(msg);
			
		}
		InvalidCommandLineException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
	public static void main(String[] args) throws Exception {
		CreateEclipseDebugTarget instance = new CreateEclipseDebugTarget();
		try {
			instance.args = parseArgs(args);
			if (instance.args.isEmpty()) throw new InvalidCommandLineException("");
			instance.go();
		} catch (InvalidCommandLineException e) {
			String msg = e.getMessage();
			if (!msg.isEmpty()) System.err.println("ERROR: " + msg);
			if (e.getCause() != null) {
				e.getCause().printStackTrace();
			}
			printCommandLineHelp();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void go() throws InvalidCommandLineException, IOException {
		prologue();
		classpath();
		epilogue();
		String n = getArgString("name");
		File f = new File(n + ".launch").getCanonicalFile();
		
		OutputStream out = new FileOutputStream(f);
		try {
			out.write(launchContent.toString().getBytes("UTF-8"));
		} finally {
			out.close();
		}
		
		System.out.println("Debug target created: " + f);
	}
	
	private void prologue() throws InvalidCommandLineException {
		String type = getArgString("testType");
		
		launchContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		launchContent.append("<launchConfiguration type=\"org.eclipse.jdt.junit.launchconfig\">\n");
		launchContent.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n");
		launchContent.append("\t\t<listEntry value=\"/lombok/test/core/src/").append(type.replace(".", "/")).append(".java\"/>\n");
		launchContent.append("\t</listAttribute>\n");
		launchContent.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n");
		launchContent.append("\t\t<listEntry value=\"1\"/>\n");
		launchContent.append("\t</listAttribute>\n");
		
		if (getArgBoolean("favorite")) {
			launchContent.append("\t<listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">\n");
			launchContent.append("\t\t<listEntry value=\"org.eclipse.debug.ui.launchGroup.debug\"/>\n");
			launchContent.append("\t</listAttribute>\n");
		}
		
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.junit.CONTAINER\" value=\"\"/>\n");
		launchContent.append("\t<booleanAttribute key=\"org.eclipse.jdt.junit.KEEPRUNNING_ATTR\" value=\"false\"/>\n");
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.junit.TESTNAME\" value=\"\"/>\n");
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.junit.TEST_KIND\" value=\"org.eclipse.jdt.junit.loader.junit4\"/>\n");
		launchContent.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>\n");
	}
	
	private void classpath() throws InvalidCommandLineException {
		launchContent.append("\t<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">\n");
		
		String self; try {
			self = new File("..").getCanonicalPath();
		} catch (IOException e) {
			throw new InvalidCommandLineException("Cannot obtain canonical path to parent directory", e);
		}
		
		String bootpath = getBootPath();
		
		launchContent.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry internalArchive=&quot;/lombok/bin/main&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
		for (Map.Entry<String, String> entry : args.entrySet()) {
			if (!entry.getKey().startsWith("conf.")) continue;
			String v = entry.getValue();
			if (v.equals("NONE")) continue;
			String[] files = v.split(Pattern.quote(File.pathSeparator));
			for (String file : files) {
				String n;
				try {
					n = new File(file).getCanonicalPath();
				} catch (IOException e) {
					throw new InvalidCommandLineException("Cannot obtain canonical path to dependency " + file, e);
				}
				if (n.startsWith(self)) {
					launchContent.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry internalArchive=&quot;");
					launchContent.append(n.substring(self.length()));
					launchContent.append("&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
				}
			}
		}
		if (bootpath != null) launchContent.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry internalArchive=&quot;/lombok/" + bootpath + "&quot; path=&quot;5&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
		launchContent.append("\t</listAttribute>\n");
	}
	
	private void epilogue() throws InvalidCommandLineException {
		String type = getArgString("testType");
		
		launchContent.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"false\"/>\n");
		String jvmTarget = getArgString("jvmTarget");
		String bootpath = getBootPath();
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-").append(jvmTarget).append("\"/>\n");
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"").append(type).append("\"/>\n");
		launchContent.append("\t<listAttribute key=\"org.eclipse.jdt.launching.MODULEPATH\"/>\n");
		launchContent.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"lombok\"/>\n");
		if (getArgBoolean("shadowLoaderBased")) {
			launchContent.append("<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-javaagent:dist/lombok.jar -Dshadow.override.lombok=${project_loc:lombok}/bin/main");
			for (Map.Entry<String, String> entry : args.entrySet()) {
				if (!entry.getKey().startsWith("conf.")) continue;
				launchContent.append(File.pathSeparator).append(entry.getValue());
			}
			if (bootpath != null) launchContent.append(" -Ddelombok.bootclasspath=" + bootpath);
		}
		launchContent.append("\"/>\n</launchConfiguration>\n");
	}
	
	private String getBootPath() {
		String bp = args.get("bootpath");
		if (bp == null || bp.isEmpty() || bp.equals("0")) return null;
		File f = new File(bp);
		if (!f.isAbsolute()) return bp;
		String r = new File(".").getAbsolutePath();
		if (r.endsWith(".")) r = r.substring(0, r.length() - 1);
		if (bp.startsWith(r)) return bp.substring(r.length());
		throw new IllegalStateException("Cannot reconstruct relative path; base: " + r + " is not a parent of " + bp);
	}
	
	private String getArgString(String key) throws InvalidCommandLineException {
		String v = args.get(key);
		if (v == null) throw new InvalidCommandLineException("mandatory argument '" + key + "' missing");
		return v;
	}
	
	private boolean getArgBoolean(String key) throws InvalidCommandLineException {
		String v = args.get(key);
		if (v == null) return false;
		if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("f")) return false;
		return true;
	}
	
	private static void printCommandLineHelp() {
		System.err.println("CreateEclipseDebugTarget\n" +
			"   name=Lombok-test BaseJavac 11           # Sets the name of the debug target to make\n" +
			"   testType=lombok.RunJavacAndBaseTests    # The test class file that this debug target should run\n" +
			"   shadowLoaderBased                       # Add the VM options to use lombok as an agent and pass the classpath to the shadow loader. Needed for ECJ/Eclipse.\n" +
			"   conf.test=foo:bar:baz                   # Where 'test' is an ivy conf name, and 'foo' is a path to a jar, relativized vs. current directory.\n" +
			"   favorite                                # Should the debug target be marked as favourite?\n" +
			"");
	}
	
	private static Map<String, String> parseArgs(String[] args) throws IllegalArgumentException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		for (String arg : args) {
			int idx = arg.indexOf('=');
			String key = (idx == -1 ? arg : arg.substring(0, idx)).trim();
			String value = (idx == -1 ? "" : arg.substring(idx + 1)).trim();
			String existing = map.get(key);
			if (existing != null) {
				if (key.startsWith("conf.")) {
					value = existing + File.pathSeparator + value;
				} else {
					throw new IllegalArgumentException("Duplicate argument not allowed: " + key);
				}
			}
			map.put(key, value);
		}
		return map;
	}
}