/*
 * Copyright (C) 2026 The Project Lombok Authors.
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

import static org.junit.Assert.assertSame;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

public class PatchFixesHiderTest {
	@Test
	public void findsClassLoaderViaBundleWiring() throws Exception {
		final ClassLoader expected = new ClassLoader() {};
		Object bundle = new BundleWithWiring(expected);
		assertSame(expected, findJdtCoreClassLoader(bundle));
	}

	@Test
	public void fallsBackToLegacyEquinoxApi() throws Exception {
		final ClassLoader expected = new ClassLoader() {};
		Object bundle = new LegacyBundle(expected);
		assertSame(expected, findJdtCoreClassLoader(bundle));
	}

	private static ClassLoader findJdtCoreClassLoader(Object jdtCoreBundle) throws Exception {
		ClassLoader loader = new BundleClassLoader(new SourceBundle(new BundleContext(jdtCoreBundle)));
		URL classes = new File("build/lombok-main/Class50").toURI().toURL();
		Class<?> util = Class.forName("lombok.launch.PatchFixesHider$Util", true, new URLClassLoader(new URL[] {classes}, PatchFixesHiderTest.class.getClassLoader()));
		Method method = util.getDeclaredMethod("findJdtCoreClassLoader", ClassLoader.class);
		method.setAccessible(true);
		return (ClassLoader) method.invoke(null, loader);
	}

	private static class BundleClassLoader extends ClassLoader {
		private final Object bundle;

		BundleClassLoader(Object bundle) {
			this.bundle = bundle;
		}

		public Object getBundle() {
			return bundle;
		}
	}

	private static class SourceBundle {
		private final Object context;

		SourceBundle(Object context) {
			this.context = context;
		}

		public Object getBundleContext() {
			return context;
		}
	}

	private static class BundleContext {
		private final Object bundle;

		BundleContext(Object bundle) {
			this.bundle = bundle;
		}

		public Object[] getBundles() {
			return new Object[] {bundle};
		}
	}

	private static class BundleWithWiring {
		private final ClassLoader classLoader;

		BundleWithWiring(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		public Object adapt(final Class<?> type) {
			return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, new InvocationHandler() {
				@Override public Object invoke(Object proxy, Method method, Object[] args) {
					if ("getClassLoader".equals(method.getName())) return classLoader;
					return null;
				}
			});
		}

		@Override public String toString() {
			return "org.eclipse.jdt.core_1.0.0";
		}
	}

	private static class LegacyBundle {
		private final ClassLoader classLoader;

		LegacyBundle(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		public ClassLoader getModuleClassLoader(boolean ignored) {
			return classLoader;
		}

		@Override public String toString() {
			return "org.eclipse.jdt.core_1.0.0";
		}
	}
}
