@lombok.Data @lombok.ToString(doNotUseGetters = true) class DataExtended {
  int x;
  public @java.lang.SuppressWarnings("all") DataExtended() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.SuppressWarnings("all") void setX(final int x) {
    this.x = x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((o == null))
        return false;
    if ((o.getClass() != this.getClass()))
        return false;
    final DataExtended other = (DataExtended) o;
    if ((this.getX() != other.getX()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("DataExtended(x=" + this.x) + ")");
  }
}
