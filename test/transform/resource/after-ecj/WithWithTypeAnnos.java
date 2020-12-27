import lombok.With;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
class WithWithTypeAnnos {
  final @With @TA @TB List<String> foo;
  WithWithTypeAnnos(@TA @TB List<String> foo) {
    super();
    this.foo = foo;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithWithTypeAnnos withFoo(final @TA List<String> foo) {
    return ((this.foo == foo) ? this : new WithWithTypeAnnos(foo));
  }
}
