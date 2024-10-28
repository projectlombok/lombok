import lombok.Builder;
import lombok.Value;
public @Builder class BuilderDefaultsArray {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderDefaultsArrayBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int[] x$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean x$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String[][] y$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean y$set;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArrayBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArray.BuilderDefaultsArrayBuilder x(final int[] x) {
      this.x$value = x;
      x$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArray.BuilderDefaultsArrayBuilder y(final java.lang.String[][] y) {
      this.y$value = y;
      y$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArray build() {
      int[] x$value = this.x$value;
      if ((! this.x$set))
          x$value = BuilderDefaultsArray.$default$x();
      java.lang.String[][] y$value = this.y$value;
      if ((! this.y$set))
          y$value = BuilderDefaultsArray.$default$y();
      return new BuilderDefaultsArray(x$value, y$value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderDefaultsArray.BuilderDefaultsArrayBuilder(x$value=" + java.util.Arrays.toString(this.x$value)) + ", y$value=") + java.util.Arrays.deepToString(this.y$value)) + ")");
    }
  }
  @Builder.Default int[] x;
  @Builder.Default java.lang.String[][] y;
  private static @java.lang.SuppressWarnings("all") @lombok.Generated int[] $default$x() {
    return new int[]{1, 2};
  }
  private static @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String[][] $default$y() {
    return new java.lang.String[][]{};
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArray(final int[] x, final java.lang.String[][] y) {
    super();
    this.x = x;
    this.y = y;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderDefaultsArray.BuilderDefaultsArrayBuilder builder() {
    return new BuilderDefaultsArray.BuilderDefaultsArrayBuilder();
  }
}
