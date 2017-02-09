@lombok.Data @lombok.ToString(doNotUseGetters = true) class DataExtended {
  int x;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setX(final int x) {
    this.x = x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataExtended)))
        return false;
    final DataExtended other = (DataExtended) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataExtended);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    return result;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") DataExtended() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("DataExtended(x=" + this.x) + ")");
  }
}
