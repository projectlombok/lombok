@lombok.ToString class ToStringExplicitIncludeConf {
  int x;
  @lombok.ToString.Include int y;
  ToStringExplicitIncludeConf() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("ToStringExplicitIncludeConf(y=" + this.y) + ")");
  }
}
