import lombok.Builder;
import lombok.AllArgsConstructor;

public @Builder @AllArgsConstructor class BuilderExcludesWithAllArgsConstructor {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderExcludesWithAllArgsConstructorBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated long x;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int z;

    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructorBuilder() {
      super();
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder x(final long x) {
      this.x = x;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder y(final int y) {
      this.y = y;
      return this;
    }

    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructor build() {
      return new BuilderExcludesWithAllArgsConstructor(this.x, this.y, this.z);
    }

    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder(x=" + this.x) + ", y=") + this.y) + ", z=") + this.z) + ")");
    }
  }

  long x;
  int y;
  @Builder.Exclude int z;

  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder builder() {
    return new BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder();
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderExcludesWithAllArgsConstructor(final long x, final int y, final int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }
}