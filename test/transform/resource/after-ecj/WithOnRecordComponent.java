import lombok.With;
record WithOnRecordComponent(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public WithOnRecordComponent( String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @java.lang.SuppressWarnings("all") WithOnRecordComponent withA(final String a) {
    return ((this.a == a) ? this : new WithOnRecordComponent(a, this.b));
  }
}
