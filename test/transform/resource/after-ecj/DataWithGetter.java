@lombok.Data @lombok.Getter class DataWithGetter {
  private int x;
  private int y;
  private final String z;
  public @java.beans.ConstructorProperties({"z"}) @java.lang.SuppressWarnings("all") DataWithGetter(final String z) {
    super();
    this.z = z;
  }
  public @java.lang.SuppressWarnings("all") void setX(final int x) {
    this.x = x;
  }
  public @java.lang.SuppressWarnings("all") void setY(final int y) {
    this.y = y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataWithGetter)))
        return false;
    final DataWithGetter other = (DataWithGetter) o;
    if ((! other.canEqual(this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    if ((this.getY() != other.getY()))
        return false;
    if (((this.getZ() == null) ? (other.getZ() != null) : (! this.getZ().equals(other.getZ()))))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataWithGetter);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    result = ((result * PRIME) + this.getY());
    result = ((result * PRIME) + ((this.getZ() == null) ? 0 : this.getZ().hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((((("DataWithGetter(x=" + this.getX()) + ", y=") + this.getY()) + ", z=") + this.getZ()) + ")");
  }
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
  public @java.lang.SuppressWarnings("all") String getZ() {
    return this.z;
  }
}