package lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows destructuring of an object into local variables by invoking getter methods
 * for the specified field names on the initialized value of the annotated local variable.
 */
@Documented
@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.SOURCE)
public @interface Destructure {
    /**
     * The field names to extract via their corresponding getter methods.
     * For a field named {@code foo}, the handler will try {@code getFoo()} and then {@code isFoo()}.
     */
    String[] value();
}