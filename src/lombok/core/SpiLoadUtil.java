package lombok.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Lombok;

public class SpiLoadUtil {
	private SpiLoadUtil() {}
	
	public static <C> Iterator<C> findServices(Class<C> target) throws IOException {
		return findServices(target, Thread.currentThread().getContextClassLoader());
	}
	
	public static <C> Iterator<C> findServices(final Class<C> target, final ClassLoader loader) throws IOException {
		Enumeration<URL> resources = loader.getResources("META-INF/services/" + target.getName());
		final Set<String> entries = new LinkedHashSet<String>();
		while ( resources.hasMoreElements() ) {
			URL url = resources.nextElement();
			readServicesFromUrl(entries, url);
		}
		
		final Iterator<String> names = entries.iterator();
		return new Iterator<C>() {
			public boolean hasNext() {
				return names.hasNext();
			}
			
			public C next() {
				try {
					return target.cast(Class.forName(names.next(), true, loader).newInstance());
				} catch ( Throwable t ) {
					throw Lombok.sneakyThrow(t);
				}
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private static void readServicesFromUrl(Collection<String> list, URL url) throws IOException {
		InputStream in = url.openStream();
		try {
			if ( in == null ) return;
			BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ( true ) {
				String line = r.readLine();
				if ( line == null ) break;
				int idx = line.indexOf('#');
				if ( idx != -1 ) line = line.substring(0, idx);
				line = line.trim();
				if ( line.length() == 0 ) continue;
				list.add(line);
			}
		} finally {
			try { in.close(); } catch ( Throwable ignore ) {}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Annotation> findAnnotationClass(Class<?> c, Class<?> base) {
		if ( c == Object.class || c == null ) return null;
		for ( Type iface : c.getGenericInterfaces() ) {
			if ( iface instanceof ParameterizedType ) {
				ParameterizedType p = (ParameterizedType)iface;
				if ( !base.equals(p.getRawType()) ) continue;
				Type target = p.getActualTypeArguments()[0];
				if ( target instanceof Class<?> ) {
					if ( Annotation.class.isAssignableFrom((Class<?>) target) ) {
						return (Class<? extends Annotation>) target;
					}
				}
				
				throw new ClassCastException("Not an annotation type: " + target);
			}
		}
		
		Class<? extends Annotation> potential = findAnnotationClass(c.getSuperclass(), base);
		if ( potential != null ) return potential;
		for ( Class<?> iface : c.getInterfaces() ) {
			potential = findAnnotationClass(iface, base);
			if ( potential != null ) return potential;
		}
		
		return null;
	}
}
