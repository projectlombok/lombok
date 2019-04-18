import lombok.experimental.Wither;
class WitherDeprecated {
  @Deprecated @Wither int annotation;
  @Wither int javadoc;
  WitherDeprecated(int annotation, int javadoc) {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WitherDeprecated withAnnotation(final int annotation) {
    return ((this.annotation == annotation) ? this : new WitherDeprecated(annotation, this.javadoc));
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") WitherDeprecated withJavadoc(final int javadoc) {
    return ((this.javadoc == javadoc) ? this : new WitherDeprecated(this.annotation, javadoc));
  }
}
