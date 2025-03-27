public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Getter @lombok.Setter class JacksonizedAccessors {
  private @com.fasterxml.jackson.annotation.JsonProperty("intValue") int intValue;
  public JacksonizedAccessors() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.annotation.JsonProperty("intValue") int intValue() {
    return this.intValue;
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.annotation.JsonProperty("intValue") JacksonizedAccessors intValue(final int intValue) {
    this.intValue = intValue;
    return this;
  }
}