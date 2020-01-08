import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault class EqualsAndHashCodeWithNNBD {
  static @lombok.EqualsAndHashCode @org.eclipse.jdt.annotation.NonNullByDefault class Inner {
    Inner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.@javax.annotation.Nullable @org.eclipse.jdt.annotation.Nullable Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof EqualsAndHashCodeWithNNBD.Inner)))
          return false;
      final EqualsAndHashCodeWithNNBD.Inner other = (EqualsAndHashCodeWithNNBD.Inner) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof EqualsAndHashCodeWithNNBD.Inner);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int result = 1;
      return result;
    }
  }
  EqualsAndHashCodeWithNNBD() {
    super();
  }
}