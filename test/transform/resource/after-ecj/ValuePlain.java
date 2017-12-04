import lombok.Value;
final @lombok.Value class Value1 {
  private final int x;
  private final String name;
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") String getName() {
    return this.name;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof Value1)))
        return false;
    final Value1 other = (Value1) o;
    if ((this.getX() != other.getX()))
        return false;
    final java.lang.Object this$name = this.getName();
    final java.lang.Object other$name = other.getName();
    if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    final java.lang.Object $name = this.getName();
    result = ((result * PRIME) + (($name == null) ? 43 : $name.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("Value1(x=" + this.getX()) + ", name=") + this.getName()) + ")");
  }
  public @java.lang.SuppressWarnings("all") Value1(final int x, final String name) {
    super();
    this.x = x;
    this.name = name;
  }
}
@Value @lombok.experimental.NonFinal class Value2 {
  public final int x;
  private final String name;
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") String getName() {
    return this.name;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof Value2)))
        return false;
    final Value2 other = (Value2) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    final java.lang.Object this$name = this.getName();
    final java.lang.Object other$name = other.getName();
    if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof Value2);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    final java.lang.Object $name = this.getName();
    result = ((result * PRIME) + (($name == null) ? 43 : $name.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("Value2(x=" + this.getX()) + ", name=") + this.getName()) + ")");
  }
  public @java.lang.SuppressWarnings("all") Value2(final int x, final String name) {
    super();
    this.x = x;
    this.name = name;
  }
}
final @Value class Value3 {
  private @lombok.experimental.NonFinal int x;
  private final int y;
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof Value3)))
        return false;
    final Value3 other = (Value3) o;
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
    return (((("Value3(x=" + this.getX()) + ", y=") + this.getY()) + ")");
  }
  public @java.lang.SuppressWarnings("all") Value3(final int x, final int y) {
    super();
    this.x = x;
    this.y = y;
  }
}
