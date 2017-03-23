import lombok.Builder;
import lombok.Value;
public final @Value @Builder class BuilderDefaults {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderDefaultsBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int x;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean x$set;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String name;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") long z;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean z$set;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsBuilder x(final int x) {
      this.x = x;
      x$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsBuilder name(final String name) {
      this.name = name;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsBuilder z(final long z) {
      this.z = z;
      z$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaults build() {
      return new BuilderDefaults((x$set ? x : BuilderDefaults.$default$x()), name, (z$set ? z : BuilderDefaults.$default$z()));
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((("BuilderDefaults.BuilderDefaultsBuilder(x=" + this.x) + ", name=") + this.name) + ", z=") + this.z) + ")");
    }
  }
  private final @Builder.Default int x;
  private final String name;
  private final @Builder.Default long z;
  private static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int $default$x() {
    return 10;
  }
  private static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") long $default$z() {
    return System.currentTimeMillis();
  }
  @java.beans.ConstructorProperties({"x", "name", "z"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaults(final int x, final String name, final long z) {
    super();
    this.x = x;
    this.name = name;
    this.z = z;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderDefaultsBuilder builder() {
    return new BuilderDefaultsBuilder();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String getName() {
    return this.name;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") long getZ() {
    return this.z;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderDefaults)))
        return false;
    final BuilderDefaults other = (BuilderDefaults) o;
    if ((this.getX() != other.getX()))
        return false;
    final java.lang.Object this$name = this.getName();
    final java.lang.Object other$name = other.getName();
    if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
        return false;
    if ((this.getZ() != other.getZ()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    final java.lang.Object $name = this.getName();
    result = ((result * PRIME) + (($name == null) ? 43 : $name.hashCode()));
    final long $z = this.getZ();
    result = ((result * PRIME) + (int) ($z ^ ($z >>> 32)));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (((((("BuilderDefaults(x=" + this.getX()) + ", name=") + this.getName()) + ", z=") + this.getZ()) + ")");
  }
}
