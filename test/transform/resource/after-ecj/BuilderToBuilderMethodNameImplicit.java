import lombok.Builder;
@Builder(toBuilderMethodName = "mutate") class BuilderToBuilderMethodNameImplicit {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderToBuilderMethodNameImplicitBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int age;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicitBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder name(final String name) {
      this.name = name;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder age(final int age) {
      this.age = age;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit build() {
      return new BuilderToBuilderMethodNameImplicit(this.name, this.age);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder(name=" + this.name) + ", age=") + this.age) + ")");
    }
  }
  private String name;
  private int age;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit(final String name, final int age) {
    super();
    this.name = name;
    this.age = age;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder builder() {
    return new BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder mutate() {
    return new BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder().name(this.name).age(this.age);
  }
}
