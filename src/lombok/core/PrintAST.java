package lombok.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface PrintAST {
	String outfile() default "";
	
	/**
	 * Normally, the printer will print each node focusing on the node (E.g. classname, and such). By setting printContent to true,
	 * methods, initializers, and other statement-containing elements actually print their java code instead of element class names.
	 */
	boolean printContent() default false;
}
