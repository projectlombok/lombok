public @lombok.Builder class BuilderWithBadNames {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithBadNamesBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String build;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String toString;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNamesBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNames.BuilderWithBadNamesBuilder build(final String build) {
      this.build = build;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNames.BuilderWithBadNamesBuilder toString(final String toString) {
      this.toString = toString;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNames build() {
      return new BuilderWithBadNames(this.build, this.toString);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderWithBadNames.BuilderWithBadNamesBuilder(build=" + this.build) + ", toString=") + this.toString) + ")");
    }
  }
  String build;
  String toString;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNames(final String build, final String toString) {
    super();
    this.build = build;
    this.toString = toString;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithBadNames.BuilderWithBadNamesBuilder builder() {
    return new BuilderWithBadNames.BuilderWithBadNamesBuilder();
  }
}
