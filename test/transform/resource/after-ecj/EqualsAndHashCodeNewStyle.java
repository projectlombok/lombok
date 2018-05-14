import lombok.EqualsAndHashCode;
public @EqualsAndHashCode class EqualsAndHashCodeNewStyle {
  @EqualsAndHashCode.Include int b;
  double c;
  int f;
  @EqualsAndHashCode.Include int d;
  int g;
  @EqualsAndHashCode.Exclude int j;
  public EqualsAndHashCodeNewStyle() {
    super();
  }
  @EqualsAndHashCode.Include int f() {
    return 0;
  }
  @EqualsAndHashCode.Include(replaces = "g") long i() {
    return 0;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeNewStyle)))
        return false;
    final EqualsAndHashCodeNewStyle other = (EqualsAndHashCodeNewStyle) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.b != other.b))
        return false;
    if ((java.lang.Double.compare(this.c, other.c) != 0))
        return false;
    if ((this.d != other.d))
        return false;
    if ((this.f() != other.f()))
        return false;
    if ((this.i() != other.i()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeNewStyle);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.b);
    final long $c = java.lang.Double.doubleToLongBits(this.c);
    result = ((result * PRIME) + (int) ($c ^ ($c >>> 32)));
    result = ((result * PRIME) + this.d);
    result = ((result * PRIME) + this.f());
    final long $$i = this.i();
    result = ((result * PRIME) + (int) ($$i ^ ($$i >>> 32)));
    return result;
  }
}
