import lombok.With;
@With record WithOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public WithOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @java.lang.SuppressWarnings("all") WithOnRecord withA(final String a) {
    return ((this.a == a) ? this : new WithOnRecord(a, this.b));
  }
  public @java.lang.SuppressWarnings("all") WithOnRecord withB(final String b) {
    return ((this.b == b) ? this : new WithOnRecord(this.a, b));
  }
}
