package lombok.experimental;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * ?.
 */
@Target({FIELD, LOCAL_VARIABLE})
@Retention(SOURCE)
public @interface SafeCall {
}
