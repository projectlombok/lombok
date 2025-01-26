// version 14:
import lombok.NonNull;
public record NonNullExistingConstructorOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public NonNullExistingConstructorOnRecord(@NonNull String b) {
    this("default", b);
    if ((b == null))
        {
          throw new java.lang.NullPointerException("b is marked non-null but is null");
        }
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullExistingConstructorOnRecord(@NonNull String a, @NonNull String b) {
    super();
    if ((a == null))
        {
          throw new java.lang.NullPointerException("a is marked non-null but is null");
        }
    if ((b == null))
        {
          throw new java.lang.NullPointerException("b is marked non-null but is null");
        }
    this.a = a;
    this.b = b;
  }
}
