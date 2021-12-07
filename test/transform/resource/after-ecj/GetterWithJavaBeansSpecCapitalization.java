class GetterWithJavaBeansSpecCapitalization {
  @lombok.Getter int a;
  @lombok.Getter int aField;
  GetterWithJavaBeansSpecCapitalization() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getA() {
    return this.a;
  }
  public @java.lang.SuppressWarnings("all") int getaField() {
    return this.aField;
  }
}