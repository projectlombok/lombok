import lombok.Setter;
class SetterDeprecated {
  @Deprecated @Setter int annotation;
  @Setter int javadoc;
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void setAnnotation(final int annotation) {
    this.annotation = annotation;
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void setJavadoc(final int javadoc) {
    this.javadoc = javadoc;
  }
  SetterDeprecated() {
    super();
  }
}
