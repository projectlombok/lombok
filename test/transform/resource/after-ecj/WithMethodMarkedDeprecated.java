import lombok.With;
class WithMethodMarkedDeprecated {
  @Deprecated @With int annotation;
  @With int javadoc;
  WithMethodMarkedDeprecated(int annotation, int javadoc) {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WithMethodMarkedDeprecated withAnnotation(final int annotation) {
    return ((this.annotation == annotation) ? this : new WithMethodMarkedDeprecated(annotation, this.javadoc));
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WithMethodMarkedDeprecated withJavadoc(final int javadoc) {
    return ((this.javadoc == javadoc) ? this : new WithMethodMarkedDeprecated(this.annotation, javadoc));
  }
}
