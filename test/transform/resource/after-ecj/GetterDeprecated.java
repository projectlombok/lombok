import lombok.Getter;
class GetterDeprecated {
  @Deprecated @Getter int annotation;
  @Getter int javadoc;
  GetterDeprecated() {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated int getAnnotation() {
    return this.annotation;
  }
  /**
   * @deprecated
   */
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated int getJavadoc() {
    return this.javadoc;
  }
}
