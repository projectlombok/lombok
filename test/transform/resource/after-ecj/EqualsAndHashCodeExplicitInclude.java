@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true) class EqualsAndHashCodeExplicitInclude {
  int x;
  EqualsAndHashCodeExplicitInclude() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeExplicitInclude)))
        return false;
    final EqualsAndHashCodeExplicitInclude other = (EqualsAndHashCodeExplicitInclude) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeExplicitInclude);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = 1;
    return result;
  }
}
