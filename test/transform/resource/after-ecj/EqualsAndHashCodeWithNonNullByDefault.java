import javax.annotation.ParametersAreNonnullByDefault;
@lombok.EqualsAndHashCode @ParametersAreNonnullByDefault class EqualsAndHashCodeWithNonNullByDefault {
  EqualsAndHashCodeWithNonNullByDefault() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final @javax.annotation.Nullable java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeWithNonNullByDefault)))
        return false;
    final EqualsAndHashCodeWithNonNullByDefault other = (EqualsAndHashCodeWithNonNullByDefault) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final @javax.annotation.Nullable java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeWithNonNullByDefault);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = 1;
    return result;
  }
}
