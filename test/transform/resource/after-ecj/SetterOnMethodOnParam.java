class SetterOnMethodOnParam {
  public @interface Test {
  }
  @lombok.Setter() int i;
  @lombok.Setter() int j;
  @lombok.Setter() int k;
  SetterOnMethodOnParam() {
    super();
  }
  public @Deprecated @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.Deprecated @Test @java.lang.SuppressWarnings("all") void setJ(final @Test int j) {
    this.j = j;
  }
  public @java.lang.Deprecated @Test @java.lang.SuppressWarnings("all") void setK(final @Test int k) {
    this.k = k;
  }
}