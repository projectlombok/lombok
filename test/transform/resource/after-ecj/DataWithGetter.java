@lombok.Data @lombok.Getter class DataWithGetter {
  private int x;
  private int y;
  private final String z;
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setX(final int x) {
    this.x = x;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setY(final int y) {
    this.y = y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataWithGetter)))
        return false;
    final DataWithGetter other = (DataWithGetter) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    if ((this.getY() != other.getY()))
        return false;
    final java.lang.Object this$z = this.getZ();
    final java.lang.Object other$z = other.getZ();
    if (((this$z == null) ? (other$z != null) : (! this$z.equals(other$z))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataWithGetter);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    result = ((result * PRIME) + this.getY());
    final java.lang.Object $z = this.getZ();
    result = ((result * PRIME) + (($z == null) ? 43 : $z.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("DataWithGetter(x=" + this.getX()) + ", y=") + this.getY()) + ", z=") + this.getZ()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated DataWithGetter(final String z) {
    super();
    this.z = z;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getY() {
    return this.y;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getZ() {
    return this.z;
  }
}
