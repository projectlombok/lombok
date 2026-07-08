@lombok.Builder(builderClassName = "Builder") class BuilderClassNameCollisionQualified<T> {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class Builder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.function.Function<T, String> mapper;
    @java.lang.SuppressWarnings("all") @lombok.Generated Builder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderClassNameCollisionQualified.Builder<T> mapper(final java.util.function.Function<T, String> mapper) {
      this.mapper = mapper;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderClassNameCollisionQualified<T> build() {
      return new BuilderClassNameCollisionQualified<T>(this.mapper);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderClassNameCollisionQualified.Builder(mapper=" + this.mapper) + ")");
    }
  }
  private java.util.function.Function<T, String> mapper;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderClassNameCollisionQualified(final java.util.function.Function<T, String> mapper) {
    super();
    this.mapper = mapper;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>BuilderClassNameCollisionQualified.Builder<T> builder() {
    return new BuilderClassNameCollisionQualified.Builder<T>();
  }
}
