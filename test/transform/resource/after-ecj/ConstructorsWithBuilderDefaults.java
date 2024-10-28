import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;
final @NoArgsConstructor(force = true) @AllArgsConstructor @Builder @Value class ConstructorsWithBuilderDefaults {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class ConstructorsWithBuilderDefaultsBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int x$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean x$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaultsBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder x(final int x) {
      this.x$value = x;
      x$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder y(final int y) {
      this.y = y;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults build() {
      int x$value = this.x$value;
      if ((! this.x$set))
          x$value = ConstructorsWithBuilderDefaults.$default$x();
      return new ConstructorsWithBuilderDefaults(x$value, this.y);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ")");
    }
  }
  private final @Builder.Default int x;
  private final int y;
  private static @java.lang.SuppressWarnings("all") @lombok.Generated int $default$x() {
    return 5;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder builder() {
    return new ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getY() {
    return this.y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ConstructorsWithBuilderDefaults)))
        return false;
    final ConstructorsWithBuilderDefaults other = (ConstructorsWithBuilderDefaults) o;
    if ((this.getX() != other.getX()))
        return false;
    if ((this.getY() != other.getY()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    result = ((result * PRIME) + this.getY());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("ConstructorsWithBuilderDefaults(x=" + this.getX()) + ", y=") + this.getY()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults() {
    super();
    this.y = 0;
    this.x = ConstructorsWithBuilderDefaults.$default$x();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults(final int x, final int y) {
    super();
    this.x = x;
    this.y = y;
  }
}
