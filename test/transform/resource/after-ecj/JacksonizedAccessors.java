public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Getter @lombok.Setter class JacksonizedAccessors {
  private @com.fasterxml.jackson.annotation.JsonProperty("intValue") int intValue;
  public JacksonizedAccessors() {
    super();
  }
  public @com.fasterxml.jackson.annotation.JsonProperty("intValue") @java.lang.SuppressWarnings("all") @lombok.Generated int intValue() {
    return this.intValue;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonProperty("intValue") @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessors intValue(final int intValue) {
    this.intValue = intValue;
    return this;
  }
}