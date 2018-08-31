import lombok.experimental.Wither;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) @interface TA {
}
class WitherTypeAnnos {
  final @Wither @TA List<@TA String> foo;
  WitherTypeAnnos(@TA List<@TA String> foo) {
    super();
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") WitherTypeAnnos withFoo(final @TA List<String> foo) {
    return ((this.foo == foo) ? this : new WitherTypeAnnos(foo));
  }
}
