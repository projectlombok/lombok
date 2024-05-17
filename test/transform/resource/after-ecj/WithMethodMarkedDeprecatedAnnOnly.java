import lombok.With;
class WithMethodMarkedDeprecatedAnnOnly {
  @Deprecated @With int annotation;
  WithMethodMarkedDeprecatedAnnOnly(int annotation) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated WithMethodMarkedDeprecatedAnnOnly withAnnotation(final int annotation) {
    return ((this.annotation == annotation) ? this : new WithMethodMarkedDeprecatedAnnOnly(annotation));
  }
}
