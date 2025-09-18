public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Getter @lombok.Setter class JacksonizedAccessorsTransient {
  private transient int intValue;
  public JacksonizedAccessorsTransient() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int intValue() {
    return this.intValue;
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsTransient intValue(final int intValue) {
    this.intValue = intValue;
    return this;
  }
}