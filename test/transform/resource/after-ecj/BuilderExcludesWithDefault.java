import lombok.Builder;

public @Builder class BuilderExcludesWithDefault {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderExcludesWithDefaultBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated long x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int z$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean z$set;

    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefaultBuilder() {
      super();
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder x(final long x) {
      this.x = x;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder y(final int y) {
      this.y = y;
      return this;
    }

    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefault build() {
      int z$value = this.z$value;
      if ((! this.z$set))
          z$value = BuilderExcludesWithDefault.$default$z();
      return new BuilderExcludesWithDefault(this.x, this.y, z$value);
    }

    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder(x=" + this.x) + ", y=") + this.y) + ", z$value=") + this.z$value) + ")");
    }
  }

  long x;
  int y;
  @Builder.Exclude @Builder.Default int z;

  private static @java.lang.SuppressWarnings("all") @lombok.Generated int $default$z() {
    return 5;
  }

  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefault(final long x, final int y, final int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder builder() {
    return new BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder();
  }
}