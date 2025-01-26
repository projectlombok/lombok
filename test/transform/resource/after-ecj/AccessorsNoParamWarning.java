@lombok.experimental.Accessors(fluent = true) class AccessorsNoParams {
  private @lombok.Getter @lombok.experimental.Accessors String otherFieldWithOverride = "";
  AccessorsNoParams() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String otherFieldWithOverride() {
    return this.otherFieldWithOverride;
  }
}
@lombok.experimental.Accessors class AccessorsNoParams2 {
  private @lombok.Setter boolean foo;
  AccessorsNoParams2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setFoo(final boolean foo) {
    this.foo = foo;
  }
}
