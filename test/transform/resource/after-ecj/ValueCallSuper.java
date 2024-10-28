class ValueParent {
  ValueParent() {
    super();
  }
}
final @lombok.Value class ValueCallSuper extends ValueParent {
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
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
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof ValueCallSuper);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = super.hashCode();
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return "ValueCallSuper()";
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated ValueCallSuper() {
    super();
  }
}
