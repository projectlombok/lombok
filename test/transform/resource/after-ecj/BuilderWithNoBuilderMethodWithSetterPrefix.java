import lombok.Builder;
@Builder(toBuilder = true,builderMethodName = "",setterPrefix = "with") class BuilderWithNoBuilderMethodWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderWithNoBuilderMethodWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") String a;
    @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodWithSetterPrefixBuilder withA(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodWithSetterPrefix build() {
      return new BuilderWithNoBuilderMethodWithSetterPrefix(a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithNoBuilderMethodWithSetterPrefix.BuilderWithNoBuilderMethodWithSetterPrefixBuilder(a=" + this.a) + ")");
    }
  }
  private String a = "";
  @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodWithSetterPrefix(final String a) {
    super();
    this.a = a;
  }
  public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodWithSetterPrefixBuilder toBuilder() {
    return new BuilderWithNoBuilderMethodWithSetterPrefixBuilder().withA(this.a);
  }
}
