package test.lombok;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
public class NestedClassAndAnnotationNestedInNonImportedSibling {
  public static @RequiredArgsConstructor @Getter class Inner {
    private final @Other.OtherNested.TA int someVal;
    public @java.lang.SuppressWarnings("all") Inner(final @Other.OtherNested.TA int someVal) {
      super();
      this.someVal = someVal;
    }
    public @Other.OtherNested.TA @java.lang.SuppressWarnings("all") int getSomeVal() {
      return this.someVal;
    }
  }
  public static class Other {
    public static class OtherNested {
      public @Retention(RetentionPolicy.RUNTIME) @interface TA {
      }
      public OtherNested() {
        super();
      }
    }
    public Other() {
      super();
    }
  }
  public NestedClassAndAnnotationNestedInNonImportedSibling() {
    super();
  }
}
