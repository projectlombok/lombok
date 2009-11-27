package lombok.delombok;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.core.LombokApp;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(LombokApp.class)
public class DelombokApp implements LombokApp {
	@Override public int runApp(String[] args) throws Exception {
		try {
			Class.forName("com.sun.tools.javac.main.JavaCompiler");
			runDirectly(args);
			return 0;
		} catch (ClassNotFoundException e) {
			//tools.jar is probably not on the classpath. We're going to try and find it, and then load the rest via a ClassLoader that includes tools.jar.
			File toolsJar = findToolsJar();
			if (toolsJar == null) {
				System.err.println("Can't find tools.jar. Rerun delombok with tools.jar on the classpath.");
				return 1;
			}
			
			URLClassLoader loader = new URLClassLoader(new URL[] {toolsJar.toURI().toURL()});
			try {
				loader.loadClass("lombok.delombok.Delombok").getMethod("main", String[].class).invoke(null, new Object[] {args});
			} catch (InvocationTargetException e1) {
				Throwable t = e1.getCause();
				if (t instanceof Error) throw (Error)t;
				if (t instanceof Exception) throw (Exception)t;
				throw e1;
			}
			return 0;
		}
	}
	
	private void runDirectly(String[] args) {
		Delombok.main(args);
	}
	
	private static File findToolsJar() {
		try {
			File toolsJar = findToolsJarViaRT();
			if (toolsJar != null) return toolsJar;
		} catch (Throwable ignore) {}
		
		return findToolsJarViaProperties();
	}
	
	private static File findToolsJarViaProperties() {
		File home = new File(System.getProperty("java.home", "."));
		File toolsJar = checkToolsJar(home);
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(home, "lib"));
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(home.getParentFile(), "lib"));
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(new File(home, "jdk"), "lib"));
		if (toolsJar != null) return toolsJar;
		return null;
	}
	
	private static File findToolsJarViaRT() {
		String url = ClassLoader.getSystemClassLoader().getResource("java/lang/String.class").toString();
		if (!url.startsWith("jar:file:")) return null;
		url = url.substring("jar:file:".length());
		int idx = url.indexOf('!');
		if (idx == -1) return null;
		url = url.substring(0, idx);
		
		File toolsJar = checkToolsJar(new File(url).getParentFile());
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(new File(url).getParentFile().getParentFile().getParentFile(), "lib"));
		if (toolsJar != null) return toolsJar;
		return null;
	}
	
	private static File checkToolsJar(File d) {
		if (d.getName().equals("tools.jar") && d.isFile() && d.canRead()) return d;
		d = new File(d, "tools.jar");
		if (d.getName().equals("tools.jar") && d.isFile() && d.canRead()) return d;
		return null;
	}
	
	@Override public String getAppName() {
		return "delombok";
	}
}

