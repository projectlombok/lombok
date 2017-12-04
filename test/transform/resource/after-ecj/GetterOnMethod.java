class GetterOnMethod {
  public @interface Test {
  }
  @lombok.Getter() int i;
  @lombok.Getter() int j;
  @lombok.Getter() int k;
  GetterOnMethod() {
    super();
  }
  public @Deprecated @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @java.lang.Deprecated @Test @java.lang.SuppressWarnings("all") int getJ() {
    return this.j;
  }
  public @java.lang.Deprecated @Test @java.lang.SuppressWarnings("all") int getK() {
    return this.k;
  }
}
