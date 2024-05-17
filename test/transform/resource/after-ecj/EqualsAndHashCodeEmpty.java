@lombok.EqualsAndHashCode class EqualsAndHashCodeEmpty {
  EqualsAndHashCodeEmpty() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeEmpty)))
        return false;
    final EqualsAndHashCodeEmpty other = (EqualsAndHashCodeEmpty) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeEmpty);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = 1;
    return result;
  }
}
@lombok.EqualsAndHashCode(callSuper = true) class EqualsAndHashCodeEmptyWithSuper extends EqualsAndHashCodeEmpty {
  EqualsAndHashCodeEmptyWithSuper() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeEmptyWithSuper)))
        return false;
    final EqualsAndHashCodeEmptyWithSuper other = (EqualsAndHashCodeEmptyWithSuper) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((! super.equals(o)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeEmptyWithSuper);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = super.hashCode();
    return result;
  }
}
