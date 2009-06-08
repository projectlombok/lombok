package java.lombok;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Allows you to load a class off of any place that is injected into a class loader (which doesn't know how to load the class you're injecting).
 * 
 * Example: Injecting lombok's Eclipse Parser patching code into eclipse's OSGi BundleLoader.
 * 
 * @author rzwitserloot
 */
public class ClassLoaderWorkaround {
	private static boolean initialized;
	private static Method m;
	
	public static void transformCompilationUnitDeclaration(Object cud) throws Exception {
		if ( !initialized ) initialize(cud);
		if ( m == null ) throw new ClassNotFoundException("lombok.agent.eclipse.TransformCompilationUnitDeclaration");
		m.invoke(null, cud);
	}
	
	private static void initialize(Object cud) {
		final ClassLoader parent = cud.getClass().getClassLoader();
		ClassLoader loader = new ClassLoader() {
			@Override public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				if ( name.startsWith("lombok.") ) {
					InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(name.replace(".", "/") + ".class");
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					
					byte[] b = new byte[65536];
					try {
						while ( true ) {
							int r = in.read(b);
							if ( r == -1 ) break;
							if ( r > 0 ) out.write(b, 0, r);
						}
						
						in.close();
						byte[] data = out.toByteArray();
						Class<?> result = defineClass(name, data, 0, data.length);
						if ( resolve ) resolveClass(result);
						return result;
					} catch ( IOException e ) {
						throw new ClassNotFoundException();
					}
				} else {
					try {
						Class<?> result = ClassLoader.getSystemClassLoader().loadClass(name);
						if ( resolve ) resolveClass(result);
						return result;
					} catch ( ClassNotFoundException e ) {
						Class<?> result = parent.loadClass(name);
						if ( resolve ) resolveClass(result);
						return result;
					}
				}
			}
		};
		
		try {
			Class<?> c = loader.loadClass("lombok.agent.eclipse.TransformCompilationUnitDeclaration");
			for ( Method m : c.getMethods() ) {
				if ( m.getName().equals("transform") ) {
					ClassLoaderWorkaround.m = m;
					break;
				}
			}
		} catch ( ClassNotFoundException ignore ) {}
		initialized = true;
	}
}
