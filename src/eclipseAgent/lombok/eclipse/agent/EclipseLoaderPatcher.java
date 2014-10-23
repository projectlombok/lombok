package lombok.eclipse.agent;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import lombok.patcher.ClassRootFinder;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.scripts.ScriptBuilder;

public class EclipseLoaderPatcher {
	public static boolean overrideLoadDecide(ClassLoader original, String name, boolean resolve) {
		return name.startsWith("lombok.");
	}
	
	public static Class<?> overrideLoadResult(ClassLoader original, String name, boolean resolve) throws ClassNotFoundException {
		try {
			Field shadowLoaderField = original.getClass().getDeclaredField("lombok$shadowLoader");
			ClassLoader shadowLoader = (ClassLoader) shadowLoaderField.get(original);
			if (shadowLoader == null) {
				String jarLoc = (String) original.getClass().getDeclaredField("lombok$location").get(null);
				System.out.println(jarLoc);
				JarFile jf = new JarFile(jarLoc);
				InputStream in = null;
				try {
					ZipEntry entry = jf.getEntry("lombok/launch/ShadowClassLoader.class");
					in = jf.getInputStream(entry);
					byte[] bytes = new byte[65536];
					int len = 0;
					while (true) {
						int r = in.read(bytes, len, bytes.length - len);
						if (r == -1) break;
						len += r;
						if (len == bytes.length) throw new IllegalStateException("lombok.launch.ShadowClassLoader too large.");
					}
					in.close();
					Class<?> shadowClassLoaderClass; {
						Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
						defineClassMethod.setAccessible(true);
						shadowClassLoaderClass = (Class<?>) defineClassMethod.invoke(original, "lombok.launch.ShadowClassLoader", bytes, 0, len);
					}
					Constructor<?> constructor = shadowClassLoaderClass.getDeclaredConstructor(ClassLoader.class, String.class, String.class, String[].class);
					constructor.setAccessible(true);
					shadowLoader = (ClassLoader) constructor.newInstance(original, "lombok", jarLoc, new String[] {"lombok."});
					shadowLoaderField.set(original, shadowLoader);
				} finally {
					if (in != null) in.close();
					jf.close();
				}
			}
			
			if (resolve) {
				Method m = shadowLoader.getClass().getDeclaredMethod("loadClass", String.class, boolean.class);
				m.setAccessible(true);
				return (Class<?>) m.invoke(shadowLoader, name, true);
			} else {
				return shadowLoader.loadClass(name);
			}
		} catch (Exception ex) {
			Throwable t = ex;
			if (t instanceof InvocationTargetException) t = t.getCause();
			if (t instanceof RuntimeException) throw (RuntimeException) t;
			if (t instanceof Error) throw (Error) t;
			throw new RuntimeException(t);
		}
	}
	
	private static final String SELF_NAME = "lombok.eclipse.agent.EclipseLoaderPatcher";
	
	public static void patchEquinoxLoaders(ScriptManager sm, Class<?> launchingContext) {
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.target(new MethodTarget("org.eclipse.osgi.framework.adapter.core.AbstractClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.target(new MethodTarget("org.eclipse.osgi.internal.loader.ModuleClassLoader", "loadClass",
						"java.lang.Class", "java.lang.String", "boolean"))
				.decisionMethod(new Hook(SELF_NAME, "overrideLoadDecide", "boolean", "java.lang.ClassLoader", "java.lang.String", "boolean"))
				.valueMethod(new Hook(SELF_NAME, "overrideLoadResult", "java.lang.Class", "java.lang.ClassLoader", "java.lang.String", "boolean"))
				.transplant()
				.request(StackRequest.THIS, StackRequest.PARAM1, StackRequest.PARAM2).build());
		
		sm.addScript(ScriptBuilder.addField().setPublic()
				.fieldType("Ljava/lang/ClassLoader;")
				.fieldName("lombok$shadowLoader")
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
