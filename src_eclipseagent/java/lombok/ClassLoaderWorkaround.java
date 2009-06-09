package java.lombok;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private static boolean initialized;
	private static Method transformCompilationUnitDeclaration;
	private static Method transformMethodDeclaration;
	private static Method transformConstructorDeclaration;
	private static Method transformInitializer;
	
	public static void transformCompilationUnitDeclaration(Object parser, Object cud) throws Exception {
		initialize(cud);
		transformCompilationUnitDeclaration.invoke(null, parser, cud);
	}
	
	public static void transformMethodDeclaration(Object parser, Object methodDeclaration) throws Exception {
		initialize(methodDeclaration);
		transformMethodDeclaration.invoke(null, parser, methodDeclaration);
	}
	
	public static void transformConstructorDeclaration(Object parser, Object constructorDeclaration) throws Exception {
		initialize(constructorDeclaration);
		transformConstructorDeclaration.invoke(null, parser, constructorDeclaration);
	}
	
	public static void transformInitializer(Object parser, Object initializer) throws Exception {
		initialize(initializer);
		transformInitializer.invoke(null, parser, initializer);
	}
	
	private static void initialize(Object cud) throws ClassNotFoundException {
		if ( initialized ) {
			if ( transformInitializer == null ) throw new ClassNotFoundException("lombok.eclipse.TransformEclipseAST");
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
					if ( m.getParameterTypes().length >= 2 ) {
						Class<?> astType = m.getParameterTypes()[1];
						String astName = astType.getName();
						astName = astName.substring(astName.lastIndexOf('.') + 1);
						if ( astName.equals("CompilationUnitDeclaration") ) transformCompilationUnitDeclaration = m;
						else if ( astName.equals("MethodDeclaration") ) transformMethodDeclaration = m;
						else if ( astName.equals("ConstructorDeclaration") ) transformConstructorDeclaration = m;
						else if ( astName.equals("Initializer") ) transformInitializer = m;
					}
				}
			}
		} catch ( ClassNotFoundException ignore ) {}
		initialized = true;
	}
}
