@lombok.Getter @lombok.Setter class JavadocMultiline {
  private java.util.List<Boolean> booleans;
  private java.util.List<Boolean> booleans2;
  JavadocMultiline() {
    super();
  }
  /**
   * This is a list of booleans.
   * 
   * @return A list of booleans to set for this object. This is a Javadoc return that is long
   *         enough to wrap to multiple lines.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.util.List<Boolean> getBooleans() {
    return this.booleans;
  }
  /**
   * This is a list of booleans.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.util.List<Boolean> getBooleans2() {
    return this.booleans2;
  }
  /**
   * This is a list of booleans.
   * 
   * @param booleans A list of booleans to set for this object. This is a Javadoc param that is
   *        long enough to wrap to multiple lines.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setBooleans(final java.util.List<Boolean> booleans) {
    this.booleans = booleans;
  }
  /**
   * This is a list of booleans.
   * 
   * @param booleans A list of booleans to set for this object. This is a Javadoc param that is
   *        long enough to wrap to multiple lines.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setBooleans2(final java.util.List<Boolean> booleans2) {
    this.booleans2 = booleans2;
  }
}
