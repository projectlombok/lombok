import lombok.experimental.Wither;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
class WitherTypeAnnos {
  final @Wither @TA @TB List<String> foo;
  WitherTypeAnnos(@TA @TB List<String> foo) {
    super();
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") WitherTypeAnnos withFoo(final @TA List<String> foo) {
    return ((this.foo == foo) ? this : new WitherTypeAnnos(foo));
  }
}
