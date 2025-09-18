public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Getter @lombok.Setter class JacksonizedAccessorsTransient {
  private transient @com.fasterxml.jackson.annotation.JsonIgnore int intValue;
  public JacksonizedAccessorsTransient() {
    super();
  }
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated int intValue() {
    return this.intValue;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsTransient intValue(final int intValue) {
    this.intValue = intValue;
    return this;
  }
}