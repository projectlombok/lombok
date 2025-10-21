package test.lombok;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
public class TwiceNestedClassAndUpperAnnotation {
  public static class Other {
    public static @RequiredArgsConstructor @Getter class OtherInner {
      private final @TA int someVal;
      public @java.lang.SuppressWarnings("all") OtherInner(final @TA int someVal) {
        super();
        this.someVal = someVal;
      }
      public @TA @java.lang.SuppressWarnings("all") int getSomeVal() {
        return this.someVal;
      }
    }
    public Other() {
      super();
    }
  }
  public @Retention(RetentionPolicy.RUNTIME) @interface TA {
  }
  public TwiceNestedClassAndUpperAnnotation() {
    super();
  }
}
