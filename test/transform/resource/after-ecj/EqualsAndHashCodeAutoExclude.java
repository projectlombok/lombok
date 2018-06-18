@lombok.EqualsAndHashCode class EqualsAndHashCodeAutoExclude {
  int x;
  String $a;
  transient String b;
  EqualsAndHashCodeAutoExclude() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeAutoExclude)))
        return false;
    final EqualsAndHashCodeAutoExclude other = (EqualsAndHashCodeAutoExclude) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeAutoExclude);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
}
@lombok.EqualsAndHashCode class EqualsAndHashCodeAutoExclude2 {
  int x;
  @lombok.EqualsAndHashCode.Include String $a;
  transient @lombok.EqualsAndHashCode.Include String b;
  EqualsAndHashCodeAutoExclude2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeAutoExclude2)))
        return false;
    final EqualsAndHashCodeAutoExclude2 other = (EqualsAndHashCodeAutoExclude2) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    final java.lang.Object this$$a = this.$a;
    final java.lang.Object other$$a = other.$a;
    if (((this$$a == null) ? (other$$a != null) : (! this$$a.equals(other$$a))))
        return false;
    final java.lang.Object this$b = this.b;
    final java.lang.Object other$b = other.b;
    if (((this$b == null) ? (other$b != null) : (! this$b.equals(other$b))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeAutoExclude2);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    final java.lang.Object $$a = this.$a;
    result = ((result * PRIME) + (($$a == null) ? 43 : $$a.hashCode()));
    final java.lang.Object $b = this.b;
    result = ((result * PRIME) + (($b == null) ? 43 : $b.hashCode()));
    return result;
  }
}
