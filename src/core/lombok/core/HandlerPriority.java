package lombok.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerPriority {
	int value();
	
	/**
	 * This can be used to differentiate 2 handlers with the same value to be at a different handler priority anyway.
	 * <strong>DO NOT USE THIS</strong> unless someone has been crowding out the numbers and there's no room left.
	 */
	int subValue() default 0;
}
