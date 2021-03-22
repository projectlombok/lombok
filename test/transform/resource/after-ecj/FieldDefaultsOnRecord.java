// version 14:
@lombok.experimental.FieldDefaults(makeFinal = true) record FieldDefaultsOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public FieldDefaultsOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
