class SetterOnParamAndOnMethod {
  @lombok.Setter() int i;
  public @Deprecated @java.lang.SuppressWarnings("all") void setI(final @SuppressWarnings("all") int i) {
    this.i = i;
  }
  SetterOnParamAndOnMethod() {
    super();
  }
}