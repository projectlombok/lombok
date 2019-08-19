import lombok.Builder;
@Builder(buildMethodName = "") class BuilderWithNoBuildMethod {
  public static @java.lang.SuppressWarnings("all") class BuilderWithNoBuildMethodBuilder {
    private @java.lang.SuppressWarnings("all") String a;
    @java.lang.SuppressWarnings("all") BuilderWithNoBuildMethodBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNoBuildMethodBuilder a(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithNoBuildMethod.BuilderWithNoBuildMethodBuilder(a=" + this.a) + ")");
    }
  }
  private String a;
  @java.lang.SuppressWarnings("all") BuilderWithNoBuildMethod(final String a) {
    super();
    this.a = a;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithNoBuildMethodBuilder builder() {
    return new BuilderWithNoBuildMethodBuilder();
  }
}
