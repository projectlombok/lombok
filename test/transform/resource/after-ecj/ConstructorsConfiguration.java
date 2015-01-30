@lombok.AllArgsConstructor class ConstructorsConfiguration {
  int x;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorsConfiguration(final int x) {
    super();
    this.x = x;
  }
}
@lombok.AllArgsConstructor(suppressConstructorProperties = false) class ConstructorsConfigurationExplicit {
  int x;
  public @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorsConfigurationExplicit(final int x) {
    super();
    this.x = x;
  }
}
