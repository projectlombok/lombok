import lombok.Builder;

public @Builder class BuilderExcludes {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderExcludesBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated long x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int z;

    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesBuilder() {
      super();
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludes.BuilderExcludesBuilder x(final long x) {
      this.x = x;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludes.BuilderExcludesBuilder y(final int y) {
      this.y = y;
      return this;
    }

    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludes build() {
      return new BuilderExcludes(this.x, this.y, this.z);
    }

    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderExcludes.BuilderExcludesBuilder(x=" + this.x) + ", y=") + this.y) + ", z=") + this.z) + ")");
    }
  }

  long x;
  int y;
  @Builder.Exclude int z;

  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludes(final long x, final int y, final int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludes.BuilderExcludesBuilder builder() {
    return new BuilderExcludes.BuilderExcludesBuilder();
  }
}
