@lombok.Data class DataIgnore {
  final int x;
  String $name;
  public @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") DataIgnore(final int x) {
    super();
    this.x = x;
  }
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataIgnore)))
        return false;
    final @java.lang.SuppressWarnings("all") DataIgnore other = (DataIgnore) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataIgnore);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("DataIgnore(x=" + this.getX()) + ")");
  }
}
