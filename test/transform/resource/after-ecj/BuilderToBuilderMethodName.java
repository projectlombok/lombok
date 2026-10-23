import lombok.Builder;
@Builder(toBuilder = true,toBuilderMethodName = "mutate") class BuilderToBuilderMethodName {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderToBuilderMethodNameBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int age;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder name(final String name) {
      this.name = name;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder age(final int age) {
      this.age = age;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName build() {
      return new BuilderToBuilderMethodName(this.name, this.age);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder(name=" + this.name) + ", age=") + this.age) + ")");
    }
  }
  private String name;
  private int age;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName(final String name, final int age) {
    super();
    this.name = name;
    this.age = age;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder builder() {
    return new BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder mutate() {
    return new BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder().name(this.name).age(this.age);
  }
}
