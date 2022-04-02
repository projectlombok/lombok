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
package lombok.launch;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

/**
 * This Java agent does not transform bytecode, but acts as a watcher that can
 * figure out when it is appropriate to load Lombok itself within a Maven
 * execution.
 * 
 * It relies on several facts:
 * <ul>
 * <li>maven-compiler-plugin contains an {@code AbstractCompilerMojo} class that
 * compiler instances extend.
 * <li>Maven loaders are {@code ClassRealms}, which extend {@code URLClassLoader}.
 * <li>Each plugin dependency in the <em>pom.xml </em>is represented as a file URL on the
 * ClassRealm that points to the artifact.
 * <li>URLs to Maven artifacts contain the group and artifact ids
 * ({@code [...]/groupid/artifactid/ver/artifactid-ver.jar}).
 * <li>The Lombok Java agent class is {@code lombok.launch.Agent}.
 * </ul>
 * Given all of the above, the transformer simply waits for {@code AbstractCompilerMojo}
 * to be loaded, then uses the loader to find the path to the Lombok jar file,
 * and finally loads the Lombok agent using reflection.
 */
public final class MavenEcjBootstrapAgent {
	private static final String MAVEN_COMPILER_TRIGGER_CLASS = "org/apache/maven/plugin/compiler/AbstractCompilerMojo";
	private static final String LOMBOK_URL_IDENTIFIER = "/org/projectlombok/lombok/";
	private static final String LOMBOK_AGENT_CLASS = "lombok.launch.Agent";
	private static final byte[] NOT_TRANSFORMED = null;
	
	private MavenEcjBootstrapAgent() {}
	
	public static void premain(final String agentArgs, final Instrumentation instrumentation) {
		instrumentation.addTransformer(new ClassFileTransformer() {
			@Override public byte[] transform(final ClassLoader loader, final String className, final Class<?> cbr, final ProtectionDomain pd, final byte[] cfb) throws IllegalClassFormatException {
				if (MAVEN_COMPILER_TRIGGER_CLASS.equals(className)) {
					for (final URL url : ((URLClassLoader) loader).getURLs()) {
						if (url.getPath().contains(LOMBOK_URL_IDENTIFIER)) {
							try {
								instrumentation.appendToSystemClassLoaderSearch(new JarFile(url.getPath()));
								MavenEcjBootstrapAgent.class.getClassLoader().loadClass(LOMBOK_AGENT_CLASS).getDeclaredMethod("premain", String.class, Instrumentation.class).invoke(null, agentArgs, instrumentation);
								instrumentation.removeTransformer(this);
								break;
							} catch (Exception e) {
								// There are no appropriate loggers available at
								// this point in time.
								e.printStackTrace(System.err);
							}
						}
					}
				}
				return NOT_TRANSFORMED;
			}
		});
	}
}
