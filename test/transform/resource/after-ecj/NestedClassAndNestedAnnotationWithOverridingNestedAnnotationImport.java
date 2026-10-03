package test.lombok;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import test.lombok.other.Other.TA;
public class NestedClassAndNestedAnnotationWithOverridingNestedAnnotationImport {
  public static @RequiredArgsConstructor @Getter class Inner {
    private final @TA int someVal;
    public @java.lang.SuppressWarnings("all") Inner(final @TA int someVal) {
      super();
      this.someVal = someVal;
    }
    public @TA @java.lang.SuppressWarnings("all") int getSomeVal() {
      return this.someVal;
    }
  }
  public static class Other {
    public @Retention(RetentionPolicy.RUNTIME) @interface TA {
    }
    public Other() {
      super();
    }
  }
  public NestedClassAndNestedAnnotationWithOverridingNestedAnnotationImport() {
    super();
  }
}
