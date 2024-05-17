import lombok.Setter;
class SetterDeprecated {
  @Deprecated @Setter int annotation;
  @Setter int javadoc;
  SetterDeprecated() {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated void setAnnotation(final int annotation) {
    this.annotation = annotation;
  }
  /**
   * @deprecated
   */
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated void setJavadoc(final int javadoc) {
    this.javadoc = javadoc;
  }
}
