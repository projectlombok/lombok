@lombok.Data @lombok.ToString(doNotUseGetters = true) class DataExtended {
  int x;
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setX(final int x) {
    this.x = x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
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
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataExtended);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    return result;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated DataExtended() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("DataExtended(x=" + this.x) + ")");
  }
}
