@lombok.EqualsAndHashCode class EqualsAndHashCodeWithExistingMethods {
  int x;
  EqualsAndHashCodeWithExistingMethods() {
    super();
  }
  public int hashCode() {
    return 42;
  }
}
final @lombok.EqualsAndHashCode class EqualsAndHashCodeWithExistingMethods2 {
  int x;
  EqualsAndHashCodeWithExistingMethods2() {
    super();
  }
  public boolean equals(Object other) {
    return false;
  }
}
final @lombok.EqualsAndHashCode(callSuper = true) class EqualsAndHashCodeWithExistingMethods3 extends EqualsAndHashCodeWithExistingMethods {
  int x;
  EqualsAndHashCodeWithExistingMethods3() {
    super();
  }
  private boolean canEqual(Object other) {
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeWithExistingMethods3)))
        return false;
    final EqualsAndHashCodeWithExistingMethods3 other = (EqualsAndHashCodeWithExistingMethods3) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((! super.equals(o)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = ((result * PRIME) + this.x);
    return result;
  }
}
