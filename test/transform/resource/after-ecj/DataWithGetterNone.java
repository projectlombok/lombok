@lombok.Data @lombok.Getter(lombok.AccessLevel.NONE) class DataWithGetterNone {
  private int x;
  private int y;
  private final String z;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setX(final int x) {
    this.x = x;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setY(final int y) {
    this.y = y;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataWithGetterNone)))
        return false;
    final DataWithGetterNone other = (DataWithGetterNone) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    if ((this.y != other.y))
        return false;
    final java.lang.Object this$z = this.z;
    final java.lang.Object other$z = other.z;
    if (((this$z == null) ? (other$z != null) : (! this$z.equals(other$z))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataWithGetterNone);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    result = ((result * PRIME) + this.y);
    final java.lang.Object $z = this.z;
    result = ((result * PRIME) + (($z == null) ? 43 : $z.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (((((("DataWithGetterNone(x=" + this.x) + ", y=") + this.y) + ", z=") + this.z) + ")");
  }
  public @java.beans.ConstructorProperties({"z"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") DataWithGetterNone(final String z) {
    super();
    this.z = z;
  }
}
