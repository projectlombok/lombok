package lombok.javac.apt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Lombok;


class PKG {
	private PKG() {}
	
	static boolean isInstanceOf(Object o, String className) {
		if ( o == null ) return false;
		return isInstanceOf(o.getClass(), className);
	}
	
	static boolean isInstanceOf(Class<?> c, String className) {
		if ( c == Object.class || c == null ) return false;
		
		if ( c.getName().equals(className) ) return true;
		
		if ( isInstanceOf(c.getSuperclass(), className) ) return true;
		for ( Class<?> iface : c.getInterfaces() ) {
			if ( isInstanceOf(iface, className) ) return true;
		}
		
		return false;
	}
	
	static byte[] readResource(String name) {
		return readResource(PKG.class.getClassLoader(), name);
	}
	
	static byte[] readResource(ClassLoader loader, String name) {
		InputStream in = loader.getResourceAsStream(name);
		if ( in == null ) throw Lombok.sneakyThrow(new IOException("Not found: " + name));
		
		try {
			return readStream(in);
		} catch (IOException e) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	static byte[] readStream(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[65536];
		while ( true ) {
			int r = in.read(b);
			if ( r == -1 ) break;
			if ( r > 0 ) baos.write(b, 0, r);
		}
		return baos.toByteArray();
	}
}
