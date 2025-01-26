import lombok.AllArgsConstructor;
public @AllArgsConstructor class ConstructorJavadoc {
  private int fieldName;
  private int fieldName2;
  /**
   * Creates a new {@code ConstructorJavadoc} instance.
   * 
   * @param fieldName Hello, World1
   * @param fieldName2 Sky is blue
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorJavadoc(final int fieldName, final int fieldName2) {
    super();
    this.fieldName = fieldName;
    this.fieldName2 = fieldName2;
  }
}
