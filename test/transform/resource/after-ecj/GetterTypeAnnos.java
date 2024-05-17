import lombok.Getter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) @interface TB {
}
class GetterTypeAnnos {
  @Getter @TA @TB List<String> foo;
  GetterTypeAnnos() {
    super();
  }
  public @TA @java.lang.SuppressWarnings("all") @lombok.Generated List<String> getFoo() {
    return this.foo;
  }
}
