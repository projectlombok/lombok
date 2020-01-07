public @lombok.Builder class BuilderWithBadNames {
  public static @java.lang.SuppressWarnings("all") class BuilderWithBadNamesBuilder {
    private @java.lang.SuppressWarnings("all") String build;
    private @java.lang.SuppressWarnings("all") String toString;
    @java.lang.SuppressWarnings("all") BuilderWithBadNamesBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNames.BuilderWithBadNamesBuilder build(final String build) {
      this.build = build;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNames.BuilderWithBadNamesBuilder toString(final String toString) {
      this.toString = toString;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNames build() {
      return new BuilderWithBadNames(this.build, this.toString);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderWithBadNames.BuilderWithBadNamesBuilder(build=" + this.build) + ", toString=") + this.toString) + ")");
    }
  }
  String build;
  String toString;
  @java.lang.SuppressWarnings("all") BuilderWithBadNames(final String build, final String toString) {
    super();
    this.build = build;
    this.toString = toString;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithBadNames.BuilderWithBadNamesBuilder builder() {
    return new BuilderWithBadNames.BuilderWithBadNamesBuilder();
  }
}
