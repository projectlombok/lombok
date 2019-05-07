package lombok.extern.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Causes lombok to generate a custom logger field.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/Log">the project lombok features page for lombok log annotations</a>.
 * <p>
 * Example:
 * <pre>
 * &#64;CustomLogger
 * public class LogExample {
 * }
 * </pre>
 *
 * and a lombok.config file with this:
 * <pre>
 * lombok.log.loggerTypeName=com.foo.logging.Logger
 * lombok.log.loggerFactoryName=com.foo.logging.LogManager.getLogger
 * </pre>
 *
 * will generate:
 *
 * <pre>
 * public class LogExample {
 *     private static final com.foo.logging.Logger log = com.foo.logging.LogManager.getLogger(LogExample.class);
 * }
 * </pre>
 *
 * This annotation is valid for classes and enumerations.<br>
 * @see <a href="https://google.github.io/flogger/">com.google.common.flogger</a>
 * @see lombok.extern.apachecommons.CommonsLog &#64;CommonsLog
 * @see lombok.extern.java.Log &#64;Log
 * @see lombok.extern.log4j.Log4j &#64;Log4j
 * @see lombok.extern.log4j.Log4j2 &#64;Log4j2
 * @see lombok.extern.slf4j.Slf4j &#64;Slf4j
 * @see lombok.extern.slf4j.XSlf4j &#64;XSlf4j
 * @see lombok.extern.jbosslog.JBossLog &#64;JBossLog
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CustomLogger {

    /** @return The category of the constructed Logger. By default, it will use the type where the annotation is placed. */
    String topic() default "";
}
