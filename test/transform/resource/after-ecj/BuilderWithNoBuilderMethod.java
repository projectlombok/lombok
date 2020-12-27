import lombok.Builder;
@Builder(toBuilder = true,builderMethodName = "") class BuilderWithNoBuilderMethod {
  public static @java.lang.SuppressWarnings("all") class BuilderWithNoBuilderMethodBuilder {
    private @java.lang.SuppressWarnings("all") String a;
    @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethodBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder a(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethod build() {
      return new BuilderWithNoBuilderMethod(this.a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder(a=" + this.a) + ")");
    }
  }
  private String a = "";
  @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethod(final String a) {
    super();
    this.a = a;
  }
  public @java.lang.SuppressWarnings("all") BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder toBuilder() {
    return new BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder().a(this.a);
  }
}
