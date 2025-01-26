import lombok.EqualsAndHashCode;
public @EqualsAndHashCode class EqualsAndHashCodeRank {
  @EqualsAndHashCode.Include int a;
  @EqualsAndHashCode.Include(rank = 10) int b;
  @EqualsAndHashCode.Include int c;
  public EqualsAndHashCodeRank() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeRank)))
        return false;
    final EqualsAndHashCodeRank other = (EqualsAndHashCodeRank) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.a != other.a))
        return false;
    if ((this.c != other.c))
        return false;
    if ((this.b != other.b))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeRank);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.a);
    result = ((result * PRIME) + this.c);
    result = ((result * PRIME) + this.b);
    return result;
  }
}
