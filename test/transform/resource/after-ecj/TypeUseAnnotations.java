import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) @interface TA {
  int x();
}
class TypeUseAnnotations {
  class Inner {
    Inner() {
      super();
    }
  }
  @lombok.Getter List<@TA(x = 5) String> foo;
  @lombok.Getter List<TypeUseAnnotations.@TA(x = 5) Inner> bar;
  TypeUseAnnotations() {
    super();
  }
  public @java.lang.SuppressWarnings("all") List<@TA(x = 5) String> getFoo() {
    return this.foo;
  }
  public @java.lang.SuppressWarnings("all") List<TypeUseAnnotations.@TA(x = 5) Inner> getBar() {
    return this.bar;
  }
}