@lombok.Data class DataConfiguration {
  final int x;
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataConfiguration)))
        return false;
    final DataConfiguration other = (DataConfiguration) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataConfiguration);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("DataConfiguration(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") DataConfiguration(final int x) {
    super();
    this.x = x;
  }
  private @java.lang.SuppressWarnings("all") DataConfiguration() {
    super();
    this.x = 0;
  }
}
