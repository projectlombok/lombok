public @lombok.Builder(setterPrefix = "with") class BuilderWithBadNamesWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderWithBadNamesWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") String build;
    private @java.lang.SuppressWarnings("all") String toString;
    @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefixBuilder withBuild(final String build) {
      this.build = build;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefixBuilder withToString(final String toString) {
      this.toString = toString;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefix build() {
      return new BuilderWithBadNamesWithSetterPrefix(build, toString);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderWithBadNamesWithSetterPrefix.BuilderWithBadNamesWithSetterPrefixBuilder(build=" + this.build) + ", toString=") + this.toString) + ")");
    }
  }
  String build;
  String toString;
  @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefix(final String build, final String toString) {
    super();
    this.build = build;
    this.toString = toString;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithBadNamesWithSetterPrefixBuilder builder() {
    return new BuilderWithBadNamesWithSetterPrefixBuilder();
  }
}
