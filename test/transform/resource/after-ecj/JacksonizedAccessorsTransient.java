public @lombok.extern.jackson.Jacksonized @lombok.experimental.Accessors(fluent = true) @lombok.Getter @lombok.Setter class JacksonizedAccessorsTransient {
  private transient @com.fasterxml.jackson.annotation.JsonIgnore int intValue;
  private transient @com.fasterxml.jackson.annotation.JsonIgnore long longValue;
  private @com.fasterxml.jackson.annotation.JsonIgnore double doubleValue;
  public JacksonizedAccessorsTransient() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int intValue() {
    return this.intValue;
  }
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated long longValue() {
    return this.longValue;
  }
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated double doubleValue() {
    return this.doubleValue;
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsTransient intValue(final int intValue) {
    this.intValue = intValue;
    return this;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsTransient longValue(final long longValue) {
    this.longValue = longValue;
    return this;
  }
  /**
   * @return {@code this}.
   */
  public @com.fasterxml.jackson.annotation.JsonIgnore @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedAccessorsTransient doubleValue(final double doubleValue) {
    this.doubleValue = doubleValue;
    return this;
  }
}