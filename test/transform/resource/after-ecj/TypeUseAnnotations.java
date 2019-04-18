import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) @interface TA {
  int x();
}
class TypeUseAnnotations {
  @lombok.Getter List<@TA(x = 5) String> foo;
  TypeUseAnnotations() {
    super();
  }
  public @java.lang.SuppressWarnings("all") List<@TA(x = 5) String> getFoo() {
    return this.foo;
  }
}