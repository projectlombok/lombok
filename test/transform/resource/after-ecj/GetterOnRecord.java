import lombok.Getter;
@Getter record GetterOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public GetterOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
