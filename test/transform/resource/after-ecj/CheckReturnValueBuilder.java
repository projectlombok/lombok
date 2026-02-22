@lombok.Builder class CheckReturnValueBuilder {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class CheckReturnValueBuilderBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name;
    @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilder.CheckReturnValueBuilderBuilder x(final int x) {
      this.x = x;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilder.CheckReturnValueBuilderBuilder name(final String name) {
      this.name = name;
      return this;
    }
    public @lombok.CheckReturnValue @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilder build() {
      return new CheckReturnValueBuilder(this.x, this.name);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("CheckReturnValueBuilder.CheckReturnValueBuilderBuilder(x=" + this.x) + ", name=") + this.name) + ")");
    }
  }
  private final int x;
  private final String name;
  @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilder(final int x, final String name) {
    super();
    this.x = x;
    this.name = name;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueBuilder.CheckReturnValueBuilderBuilder builder() {
    return new CheckReturnValueBuilder.CheckReturnValueBuilderBuilder();
  }
}
