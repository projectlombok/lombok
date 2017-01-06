import lombok.Getter;
class GetterDeprecated {
  @Deprecated @Getter int annotation;
  @Getter int javadoc;
  GetterDeprecated() {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getAnnotation() {
    return this.annotation;
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getJavadoc() {
    return this.javadoc;
  }
}
