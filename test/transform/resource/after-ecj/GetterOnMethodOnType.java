@lombok.Getter() class GetterOnMethodOnType {
  private int test;
  private String name;
  GetterOnMethodOnType() {
    super();
  }
  public @Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated int getTest() {
    return this.test;
  }
  public @Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated String getName() {
    return this.name;
  }
}
