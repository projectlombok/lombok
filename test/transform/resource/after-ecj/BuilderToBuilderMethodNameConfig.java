import lombok.Builder;
@Builder(toBuilder = true) class BuilderToBuilderMethodNameConfig {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderToBuilderMethodNameConfigBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int age;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfigBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder name(final String name) {
      this.name = name;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder age(final int age) {
      this.age = age;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig build() {
      return new BuilderToBuilderMethodNameConfig(this.name, this.age);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder(name=" + this.name) + ", age=") + this.age) + ")");
    }
  }
  private String name;
  private int age;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig(final String name, final int age) {
    super();
    this.name = name;
    this.age = age;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder builder() {
    return new BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder mutate() {
    return new BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder().name(this.name).age(this.age);
  }
}
