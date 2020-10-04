package org.checkerframework.checker.calledmethods.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface NotCalledMethods {
  /**
   * Methods that have been called, on any expression whose type is annotated.
   *
   * @return methods that have been called
   */
  public String[] value() default {};
}