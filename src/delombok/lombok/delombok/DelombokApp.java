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
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.core.LombokApp;
import lombok.permit.Permit;
import lombok.spi.Provides;

@Provides
public class DelombokApp extends LombokApp {
	@Override public int runApp(List<String> args) throws Exception {
		try {
			Class.forName("com.sun.tools.javac.main.JavaCompiler");
			runDirectly(args);
			return 0;
		} catch (ClassNotFoundException e) {
			Class<?> delombokClass = loadDelombok(args);
			if (delombokClass == null) {
				return 1;
			}
			try {
				Permit.invoke(Permit.getMethod(loadDelombok(args), "main", String[].class), null, new Object[] {args.toArray(new String[0])});
			} catch (InvocationTargetException e1) {
				Throwable t = e1.getCause();
				if (t instanceof Error) throw (Error) t;
				if (t instanceof Exception) throw (Exception) t;
				throw e1;
			}
			return 0;
		}
	}
	
	public static Class<?> loadDelombok(List<String> args) throws Exception {
		//tools.jar is probably not on the classpath. We're going to try and find it, and then load the rest via a ClassLoader that includes tools.jar.
		final File toolsJar = findToolsJar();
		if (toolsJar == null) {
			String examplePath = "/path/to/tools.jar";
			if (File.separator.equals("\\")) examplePath = "C:\\path\\to\\tools.jar";
			StringBuilder sb = new StringBuilder();
			for (String arg : args) {
				if (sb.length() > 0) sb.append(' ');
				if (arg.contains(" ")) {
					sb.append('"').append(arg).append('"');
				} else {
					sb.append(arg);
				}
			}
			
			System.err.printf("Can't find tools.jar. Rerun delombok as: java -cp lombok.jar%1$s%2$s lombok.launch.Main delombok %3$s\n",
					File.pathSeparator, examplePath, sb.toString());
			return null;
		}
		
		// The jar file is used for the lifetime of the classLoader, therefore the lifetime of delombok.
		// Since we only read from it, not closing it should not be a problem.
		@SuppressWarnings({"resource", "all"}) final JarFile toolsJarFile = new JarFile(toolsJar);
		
		ClassLoader loader = new ClassLoader(DelombokApp.class.getClassLoader()) {
			private Class<?> loadStreamAsClass(String name, boolean resolve, InputStream in) throws ClassNotFoundException {
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
				} catch (Exception e2) {
					throw new ClassNotFoundException(name, e2);
				}
			}
			
			@Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				String rawName, altName; {
					String binName = name.replace(".", "/");
					rawName = binName + ".class";
					altName = binName + ".SCL.lombok";
				}
				JarEntry entry = toolsJarFile.getJarEntry(rawName);
				if (entry == null) {
					if (name.startsWith("lombok.")) {
						InputStream res = getParent().getResourceAsStream(rawName);
						if (res == null) res = getParent().getResourceAsStream(altName);
						return loadStreamAsClass(name, resolve, res);
					}
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
		return loader.loadClass("lombok.delombok.Delombok");
	}
	
	private void runDirectly(List<String> args) {
		Delombok.main(args.toArray(new String[0]));
	}
	
	private static File findToolsJar() {
		try {
			File toolsJar = findToolsJarViaRT();
			if (toolsJar != null) return toolsJar;
		} catch (Throwable ignore) {
			//fallthrough
		}
		
		try {
			File toolsJar = findToolsJarViaProperties();
			if (toolsJar != null) return toolsJar;
		} catch (Throwable ignore) {
			//fallthrough
		}
		
		try {
			File toolsJar = findToolsJarViaEnvironment();
			return toolsJar;
		} catch (Throwable ignore) {
			//fallthrough
		}
		
		return null;
	}
	
	private static File findToolsJarViaEnvironment() {
		for (Map.Entry<String, String> s : System.getenv().entrySet()) {
			if ("JAVA_HOME".equalsIgnoreCase(s.getKey())) {
				return extensiveCheckToolsJar(new File(s.getValue()));
			}
		}
		
		return null;
	}
	
	private static File findToolsJarViaProperties() {
		File home = new File(System.getProperty("java.home", "."));
		return extensiveCheckToolsJar(home);
	}
	
	private static File extensiveCheckToolsJar(File base) {
		File toolsJar = checkToolsJar(base);
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(base, "lib"));
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(base.getParentFile(), "lib"));
		if (toolsJar != null) return toolsJar;
		toolsJar = checkToolsJar(new File(new File(base, "jdk"), "lib"));
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
		return Arrays.asList("unlombok");
	}
	
	@Override public String getAppDescription() {
		return "Applies lombok transformations without compiling your\njava code (so, 'unpacks' lombok annotations and such).";
	}
}

