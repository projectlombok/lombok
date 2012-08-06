public @lombok.Getter @lombok.ToString @lombok.RequiredArgsConstructor enum DataOnEnum {
  A("hello"),
  private final String someField;
  <clinit>() {
  }
  public @java.lang.SuppressWarnings("all") String getSomeField() {
    return this.someField;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("DataOnEnum(someField=" + this.getSomeField()) + ")");
  }
  private @java.lang.SuppressWarnings("all") DataOnEnum(final String someField) {
    super();
    this.someField = someField;
  }
}
