package test.lombok.other;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Other {

  @Retention(RetentionPolicy.RUNTIME)
  public @interface TA {
  }
}
