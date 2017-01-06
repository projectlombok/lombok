public @lombok.Getter @lombok.ToString @lombok.RequiredArgsConstructor enum DataOnEnum {
  A("hello"),
  private final String someField;
  <clinit>() {
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated String getSomeField() {
    return this.someField;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
    return (("DataOnEnum(someField=" + this.getSomeField()) + ")");
  }
  private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated DataOnEnum(final String someField) {
    super();
    this.someField = someField;
  }
}
