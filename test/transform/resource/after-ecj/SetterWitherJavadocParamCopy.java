class SetterWitherJavadocParamCopy {
  private @lombok.Setter @lombok.experimental.Wither int fieldName;
  public SetterWitherJavadocParamCopy(int f) {
    super();
    this.fieldName = f;
  }
  public @java.lang.SuppressWarnings("all") void setFieldName(final int fieldName) {
    this.fieldName = fieldName;
  }
  public @java.lang.SuppressWarnings("all") SetterWitherJavadocParamCopy withFieldName(final int fieldName) {
    return ((this.fieldName == fieldName) ? this : new SetterWitherJavadocParamCopy(fieldName));
  }
}
