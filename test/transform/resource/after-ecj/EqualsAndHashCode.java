@lombok.EqualsAndHashCode class EqualsAndHashCode {
  int x;
  boolean[] y;
  Object[] z;
  String a;
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCode)))
        return false;
    final EqualsAndHashCode other = (EqualsAndHashCode) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    if ((! java.util.Arrays.equals(this.y, other.y)))
        return false;
    if ((! java.util.Arrays.deepEquals(this.z, other.z)))
        return false;
    if (((this.a == null) ? (other.a != null) : (! this.a.equals((java.lang.Object) other.a))))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCode);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.x);
    result = ((result * PRIME) + java.util.Arrays.hashCode(this.y));
    result = ((result * PRIME) + java.util.Arrays.deepHashCode(this.z));
    result = ((result * PRIME) + ((this.a == null) ? 0 : this.a.hashCode()));
    return result;
  }
  EqualsAndHashCode() {
    super();
  }
}
final @lombok.EqualsAndHashCode class EqualsAndHashCode2 {
  int x;
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCode2)))
        return false;
    final EqualsAndHashCode2 other = (EqualsAndHashCode2) o;
    if ((this.x != other.x))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
  EqualsAndHashCode2() {
    super();
  }
}
final @lombok.EqualsAndHashCode(callSuper = false) class EqualsAndHashCode3 extends EqualsAndHashCode {
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCode3)))
        return false;
    final EqualsAndHashCode3 other = (EqualsAndHashCode3) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCode3);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    int result = 1;
    return result;
  }
  EqualsAndHashCode3() {
    super();
  }
}
@lombok.EqualsAndHashCode(callSuper = true) class EqualsAndHashCode4 extends EqualsAndHashCode {
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCode4)))
        return false;
    final EqualsAndHashCode4 other = (EqualsAndHashCode4) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((! super.equals(o)))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCode4);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + super.hashCode());
    return result;
  }
  EqualsAndHashCode4() {
    super();
  }
}
