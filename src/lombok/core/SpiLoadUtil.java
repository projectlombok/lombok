package lombok.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SpiLoadUtil {
	private SpiLoadUtil() {}
	
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
