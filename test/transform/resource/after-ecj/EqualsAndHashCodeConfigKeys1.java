@lombok.EqualsAndHashCode class EqualsAndHashCodeConfigKeys1Parent {
  EqualsAndHashCodeConfigKeys1Parent() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeConfigKeys1Parent)))
        return false;
    final EqualsAndHashCodeConfigKeys1Parent other = (EqualsAndHashCodeConfigKeys1Parent) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeConfigKeys1Parent);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = 1;
    return result;
  }
}
@lombok.EqualsAndHashCode class EqualsAndHashCodeConfigKeys1 extends EqualsAndHashCodeConfigKeys1Parent {
  int x;
  EqualsAndHashCodeConfigKeys1() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeConfigKeys1)))
        return false;
    final EqualsAndHashCodeConfigKeys1 other = (EqualsAndHashCodeConfigKeys1) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeConfigKeys1);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
}
