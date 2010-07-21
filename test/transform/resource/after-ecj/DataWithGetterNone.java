@lombok.Data @lombok.Getter(lombok.AccessLevel.NONE) class DataWithGetterNone {
  private int x;
  private int y;
  private final String z;
  public @java.beans.ConstructorProperties({"z"}) @java.lang.SuppressWarnings("all") DataWithGetterNone(final String z) {
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
    if ((o == null))
        return false;
    if ((o.getClass() != this.getClass()))
        return false;
    final DataWithGetterNone other = (DataWithGetterNone) o;
    if ((this.x != other.x))
        return false;
    if ((this.y != other.y))
        return false;
    if (((this.z == null) ? (other.z != null) : (! this.z.equals(other.z))))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.x);
    result = ((result * PRIME) + this.y);
    result = ((result * PRIME) + ((this.z == null) ? 0 : this.z.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((((("DataWithGetterNone(x=" + this.x) + ", y=") + this.y) + ", z=") + this.z) + ")");
  }
}