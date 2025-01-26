@lombok.experimental.Accessors(makeFinal = true) class AccessorsMakeFinal1 {
  private @lombok.Setter @lombok.experimental.Accessors(fluent = true) String test;
  AccessorsMakeFinal1() {
    super();
  }
  /**
   * @return {@code this}.
   */
  public final @java.lang.SuppressWarnings("all") @lombok.Generated AccessorsMakeFinal1 test(final String test) {
    this.test = test;
    return this;
  }
}
