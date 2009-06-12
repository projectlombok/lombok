package java.lombok;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Allows you to inject the lombok classes into any classloader, even if that classloader does not
 * know how to find the lombok classes.
 * 
 * Example: Injecting lombok's Eclipse Parser patching code into eclipse's OSGi BundleLoader.
 * 
 * @author rzwitserloot
 */
public class ClassLoaderWorkaround {
	static RuntimeException sneakyThrow(Throwable t) {
		if ( t == null ) throw new NullPointerException("t");
		ClassLoaderWorkaround.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
	
	private static boolean initialized;
	private static Method transform;
	
	public static void transformCompilationUnitDeclaration(Object parser, Object cud) throws Exception {
		initialize(cud);
		try {
			transform.invoke(null, parser, cud);
		} catch ( InvocationTargetException e ) {
			throw sneakyThrow(e.getCause());
		}
	}
	
	private static void initialize(Object cud) throws ClassNotFoundException {
		if ( initialized ) {
			if ( transform == null ) throw new ClassNotFoundException("lombok.eclipse.TransformEclipseAST");
			return;
		}
		
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
			Class<?> c = loader.loadClass("lombok.eclipse.TransformEclipseAST");
			for ( Method m : c.getMethods() ) {
				if ( m.getName().equals("transform") ) {
					Class<?>[] types = m.getParameterTypes();
					if ( types.length != 2 ) continue;
					if ( !types[0].getName().equals("org.eclipse.jdt.internal.compiler.parser.Parser") ) continue;
					if ( !types[1].getName().equals("org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration") ) continue;
					transform = m;
					break;
				}
			}
		} catch ( ClassNotFoundException ignore ) {}
		initialized = true;
	}
}
