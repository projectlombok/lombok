public @lombok.Getter @lombok.ToString @lombok.RequiredArgsConstructor enum DataOnEnum {
  A("hello"),
  private final String someField;
  <clinit>() {
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getSomeField() {
    return this.someField;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("DataOnEnum." + this.name()) + "(someField=") + this.getSomeField()) + ")");
  }
  private @java.lang.SuppressWarnings("all") @lombok.Generated DataOnEnum(final String someField) {
    super();
    this.someField = someField;
  }
}
