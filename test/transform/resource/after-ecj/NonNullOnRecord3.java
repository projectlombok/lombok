// version 14:
import lombok.NonNull;
public record NonNullOnRecord3(String a) {
/* Implicit */  private final String a;
  public NonNullOnRecord3(String a) {
    super();
    .a = a;
  }
  public NonNullOnRecord3(String a) {
    super();
    this.a = a;
  }
  public void method(@NonNull String param) {
    if ((param == null))
        {
          throw new java.lang.NullPointerException("param is marked non-null but is null");
        }
    String asd = "a";
  }
}
