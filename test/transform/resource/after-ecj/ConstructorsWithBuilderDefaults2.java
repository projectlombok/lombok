import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;
final @Builder @Value class ConstructorsWithBuilderDefaults<T> {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class ConstructorsWithBuilderDefaultsBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.List<T> z$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean z$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated T x$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean x$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated T q;
    @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaultsBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder<T> z(final java.util.List<T> z) {
      this.z$value = z;
      z$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder<T> x(final T x) {
      this.x$value = x;
      x$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder<T> q(final T q) {
      this.q = q;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults<T> build() {
      java.util.List<T> z$value = this.z$value;
      if ((! this.z$set))
          z$value = ConstructorsWithBuilderDefaults.<T>$default$z();
      T x$value = this.x$value;
      if ((! this.x$set))
          x$value = ConstructorsWithBuilderDefaults.<T>$default$x();
      return new ConstructorsWithBuilderDefaults<T>(z$value, x$value, this.q);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(z$value=" + this.z$value) + ", x$value=") + this.x$value) + ", q=") + this.q) + ")");
    }
  }
  private final @Builder.Default java.util.List<T> z;
  private final @Builder.Default T x;
  private final T q;
  private static @java.lang.SuppressWarnings("all") @lombok.Generated <T>java.util.List<T> $default$z() {
    return new java.util.ArrayList<T>();
  }
  private static @java.lang.SuppressWarnings("all") @lombok.Generated <T>T $default$x() {
    return null;
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults(final java.util.List<T> z, final T x, final T q) {
    super();
    this.z = z;
    this.x = x;
    this.q = q;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder<T> builder() {
    return new ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.util.List<T> getZ() {
    return this.z;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated T getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated T getQ() {
    return this.q;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ConstructorsWithBuilderDefaults)))
        return false;
    final ConstructorsWithBuilderDefaults<?> other = (ConstructorsWithBuilderDefaults<?>) o;
    final java.lang.Object this$z = this.getZ();
    final java.lang.Object other$z = other.getZ();
    if (((this$z == null) ? (other$z != null) : (! this$z.equals(other$z))))
        return false;
    final java.lang.Object this$x = this.getX();
    final java.lang.Object other$x = other.getX();
    if (((this$x == null) ? (other$x != null) : (! this$x.equals(other$x))))
        return false;
    final java.lang.Object this$q = this.getQ();
    final java.lang.Object other$q = other.getQ();
    if (((this$q == null) ? (other$q != null) : (! this$q.equals(other$q))))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $z = this.getZ();
    result = ((result * PRIME) + (($z == null) ? 43 : $z.hashCode()));
    final java.lang.Object $x = this.getX();
    result = ((result * PRIME) + (($x == null) ? 43 : $x.hashCode()));
    final java.lang.Object $q = this.getQ();
    result = ((result * PRIME) + (($q == null) ? 43 : $q.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("ConstructorsWithBuilderDefaults(z=" + this.getZ()) + ", x=") + this.getX()) + ", q=") + this.getQ()) + ")");
  }
  private @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorsWithBuilderDefaults() {
    super();
    this.q = null;
    this.z = ConstructorsWithBuilderDefaults.$default$z();
    this.x = ConstructorsWithBuilderDefaults.$default$x();
  }
}
