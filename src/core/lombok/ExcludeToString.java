package lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcludeToString {

    /**
     * Any fields listed here will not be printed in the generated {@code toString} implementation.
     * Mutually exclusive with {@link #of()}.
     * <p>
     *
     *
     * @return A list of fields to exclude.
     */
    String[] exclude() default {};
}