// version 14:
import lombok.NonNull;
record NonNullOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public NonNullOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public void method(@NonNull String param) {
    if ((param == null))
        {
          throw new java.lang.NullPointerException("param is marked non-null but is null");
        }
    String asd = "a";
  }
  public @java.lang.SuppressWarnings("all") NonNullOnRecord(final String a, final String b) {
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
