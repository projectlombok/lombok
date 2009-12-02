package lombok.delombok;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.core.LombokApp;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(LombokApp.class)
public class DelombokApp implements LombokApp {
	@Override public int runApp(List<String> args) throws Exception {
		try {
			Class.forName("com.sun.tools.javac.main.JavaCompiler");
			runDirectly(args);
			return 0;
		} catch (ClassNotFoundException e) {
			//tools.jar is probably not on the classpath. We're going to try and find it, and then load the rest via a ClassLoader that includes tools.jar.
			final File toolsJar = findToolsJar();
			if (toolsJar == null) {
				System.err.println("Can't find tools.jar. Rerun delombok with tools.jar on the classpath.");
				return 1;
			}
			
			final JarFile toolsJarFile = new JarFile(toolsJar);
			
			ClassLoader loader = new ClassLoader() {
				private Class<?>loadStreamAsClass(String name, boolean resolve, InputStream in) throws ClassNotFoundException {
					try {
						try {
							byte[] b = new byte[65536];
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							while (true) {
								int r = in.read(b);
								if (r == -1) break;
								out.write(b, 0, r);
							}
							in.close();
							byte[] data = out.toByteArray();
							Class<?> c = defineClass(name, data, 0, data.length);
							if (resolve) resolveClass(c);
							return c;
						} finally {
							in.close();
						}
					} catch (IOException e2) {
						throw new ClassNotFoundException(name, e2);
					}
				}
				
				@Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
					String rawName = name.replace(".", "/") + ".class";
					JarEntry entry = toolsJarFile.getJarEntry(rawName);
					if (entry == null) {
						if (name.startsWith("lombok.")) return loadStreamAsClass(name, resolve, super.getResourceAsStream(rawName));
						return super.loadClass(name, resolve);
					}
					
					try {
						return loadStreamAsClass(name, resolve, toolsJarFile.getInputStream(entry));
					} catch (IOException e2) {
						throw new ClassNotFoundException(name, e2);
					}
				}
				
				@Override public URL getResource(String name) {
					JarEntry entry = toolsJarFile.getJarEntry(name);
					if (entry == null) return super.getResource(name);
					try {
						return new URL("jar:file:" + toolsJar.getAbsolutePath() + "!" + name);
					} catch (MalformedURLException ignore) {
						return null;
					}
				}
				
				@Override public Enumeration<URL> getResources(final String name) throws IOException {
					JarEntry entry = toolsJarFile.getJarEntry(name);
					final Enumeration<URL> parent = super.getResources(name);
					if (entry == null) return super.getResources(name);
					return new Enumeration<URL>() {
						private boolean first = false;
						@Override public boolean hasMoreElements() {
							return !first || parent.hasMoreElements();
						}
						
						@Override public URL nextElement() {
							if (!first) {
								first = true;
								try {
									return new URL("jar:file:" + toolsJar.getAbsolutePath() + "!" + name);
								} catch (MalformedURLException ignore) {
									return parent.nextElement();
								}
							}
							return parent.nextElement();
						}
					};
				}
			};
			try {
				loader.loadClass("lombok.delombok.Delombok").getMethod("main", String[].class).invoke(null, new Object[] {args.toArray(new String[0])});
			} catch (InvocationTargetException e1) {
				Throwable t = e1.getCause();
				if (t instanceof Error) throw (Error)t;
				if (t instanceof Exception) throw (Exception)t;
				throw e1;
			}
			return 0;
		}
	}
	
	private void runDirectly(List<String> args) {
		Delombok.main(args.toArray(new String[0]));
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
	
	@Override public List<String> getAppAliases() {
		return Arrays.asList("unlombok", "delombok");
	}
	
	@Override public String getAppDescription() {
		return "Applies lombok transformations without compiling your\njava code (so, 'unpacks' lombok annotations and such).";
	}
}

