import lombok.With;
class WithMethodMarkedDeprecatedAnnOnly {
  @Deprecated @With int annotation;
  WithMethodMarkedDeprecatedAnnOnly(int annotation) {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WithMethodMarkedDeprecatedAnnOnly withAnnotation(final int annotation) {
    return ((this.annotation == annotation) ? this : new WithMethodMarkedDeprecatedAnnOnly(annotation));
  }
}

