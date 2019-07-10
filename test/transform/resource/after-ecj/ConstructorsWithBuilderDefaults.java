import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;
final @NoArgsConstructor(force = true) @AllArgsConstructor @Builder @Value class ConstructorsWithBuilderDefaults {
  public static @java.lang.SuppressWarnings("all") class ConstructorsWithBuilderDefaultsBuilder {
    private @java.lang.SuppressWarnings("all") int x;
    private @java.lang.SuppressWarnings("all") boolean x$set;
    private @java.lang.SuppressWarnings("all") int y;
    @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaultsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaultsBuilder x(final int x) {
      this.x = x;
      x$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaultsBuilder y(final int y) {
      this.y = y;
      return this;
    }
    public @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaults build() {
      return new ConstructorsWithBuilderDefaults((x$set ? x : ConstructorsWithBuilderDefaults.$default$x()), y);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(x=" + this.x) + ", y=") + this.y) + ")");
    }
  }
  private final @Builder.Default int x;
  private final int y;
  private static @java.lang.SuppressWarnings("all") int $default$x() {
    return 5;
  }
  public static @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaultsBuilder builder() {
    return new ConstructorsWithBuilderDefaultsBuilder();
  }
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
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
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    result = ((result * PRIME) + this.getY());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ConstructorsWithBuilderDefaults(x=" + this.getX()) + ", y=") + this.getY()) + ")");
  }
  public @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaults() {
    super();
    this.y = 0;
    this.x = ConstructorsWithBuilderDefaults.$default$x();
  }
  public @java.lang.SuppressWarnings("all") ConstructorsWithBuilderDefaults(final int x, final int y) {
    super();
    this.x = x;
    this.y = y;
  }
}
