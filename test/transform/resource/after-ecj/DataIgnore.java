@lombok.Data class DataIgnore {
  final int x;
  String $name;
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataIgnore)))
        return false;
    final DataIgnore other = (DataIgnore) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataIgnore);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("DataIgnore(x=" + this.getX()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated DataIgnore(final int x) {
    super();
    this.x = x;
  }
}
