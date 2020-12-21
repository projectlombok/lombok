import lombok.With;
class WithMethodMarkedDeprecated {
  @Deprecated @With int annotation;
  @With int javadoc;
  WithMethodMarkedDeprecated(int annotation, int javadoc) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WithMethodMarkedDeprecated withAnnotation(final int annotation) {
    return ((this.annotation == annotation) ? this : new WithMethodMarkedDeprecated(annotation, this.javadoc));
  }
  /**
   * @deprecated
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WithMethodMarkedDeprecated withJavadoc(final int javadoc) {
    return ((this.javadoc == javadoc) ? this : new WithMethodMarkedDeprecated(this.annotation, javadoc));
  }
}
