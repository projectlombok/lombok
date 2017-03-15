class ValueParent {
  ValueParent() {
    super();
  }
}
final @lombok.Value class ValueCallSuper extends ValueParent {
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ValueCallSuper)))
        return false;
    final ValueCallSuper other = (ValueCallSuper) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((! super.equals(o)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean canEqual(final java.lang.Object other) {
    return (other instanceof ValueCallSuper);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + super.hashCode());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return "ValueCallSuper()";
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ValueCallSuper() {
    super();
  }
}
