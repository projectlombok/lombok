@lombok.Data class DataConfiguration {
  final int x;
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
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
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataConfiguration);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("DataConfiguration(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated DataConfiguration(final int x) {
    super();
    this.x = x;
  }
  private @java.lang.SuppressWarnings("all") @lombok.Generated DataConfiguration() {
    super();
    this.x = 0;
  }
}
