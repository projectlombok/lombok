package lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Data {
	/**
	 * If you specify a static constructor name, then the generated constructor will be private, and
	 * instead a static factory method is created that other classes can use to create instances.
	 * We suggest the name: "of", like so:
	 * 
	 * <pre>
	 *     public @Data(staticConstructor = "of") class Point { final int x, y; }
	 * </pre>
	 * 
	 * Default: No static constructor, instead the normal constructor is public.
	 */
	String staticConstructor() default "";
}
