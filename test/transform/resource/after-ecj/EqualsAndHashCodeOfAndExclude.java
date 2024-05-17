final @lombok.EqualsAndHashCode(of = {"x"}) class EqualsAndHashCodeOf {
  int x;
  int y;
  EqualsAndHashCodeOf() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeOf)))
        return false;
    final EqualsAndHashCodeOf other = (EqualsAndHashCodeOf) o;
    if ((this.x != other.x))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
}
final @lombok.EqualsAndHashCode(exclude = {"y"}) class EqualsAndHashCodeExclude {
  int x;
  int y;
  EqualsAndHashCodeExclude() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeExclude)))
        return false;
    final EqualsAndHashCodeExclude other = (EqualsAndHashCodeExclude) o;
    if ((this.x != other.x))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
}
