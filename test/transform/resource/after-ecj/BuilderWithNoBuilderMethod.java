import lombok.Builder;
@Builder(toBuilder = true,builderMethodName = "") class BuilderWithNoBuilderMethod {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithNoBuilderMethodBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNoBuilderMethodBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder a(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNoBuilderMethod build() {
      return new BuilderWithNoBuilderMethod(this.a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder(a=" + this.a) + ")");
    }
  }
  private String a = "";
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNoBuilderMethod(final String a) {
    super();
    this.a = a;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder toBuilder() {
    return new BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder().a(this.a);
  }
}
