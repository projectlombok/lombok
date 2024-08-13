import lombok.Builder;

public @Builder class BuilderExcludesWarnings {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderExcludesWarningsBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated long x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;

    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarningsBuilder() {
      super();
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarnings.BuilderExcludesWarningsBuilder x(final long x) {
      this.x = x;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarnings.BuilderExcludesWarningsBuilder y(final int y) {
      this.y = y;
      return this;
    }

    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarnings build() {
      return new BuilderExcludesWarnings(this.x, this.y);
    }

    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderExcludesWarnings.BuilderExcludesWarningsBuilder(x=" + this.x) + ", y=") + this.y) + ")");
    }
  }

  long x;
  int y;
  @Builder.Exclude int z;

  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarnings(final long x, final int y) {
    super();
    this.x = x;
    this.y = y;
  }

  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWarnings.BuilderExcludesWarningsBuilder builder() {
    return new BuilderExcludesWarnings.BuilderExcludesWarningsBuilder();
  }
}

class NoBuilderButHasExcludes {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class NoBuilderButHasExcludesBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated long x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int z;

    @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludesBuilder() {
      super();
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder x(final long x) {
      this.x = x;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder y(final int y) {
      this.y = y;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder z(final int z) {
      this.z = z;
      return this;
    }

    public @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludes build() {
      return new NoBuilderButHasExcludes(this.x, this.y, this.z);
    }

    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder(x=" + this.x) + ", y=") + this.y) + ", z=") + this.z) + ")");
    }
  }

  long x;
  int y;
  @Builder.Exclude int z;

  public @Builder NoBuilderButHasExcludes(long x, int y, int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public static @java.lang.SuppressWarnings("all") @lombok.Generated NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder builder() {
    return new NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder();
  }
}