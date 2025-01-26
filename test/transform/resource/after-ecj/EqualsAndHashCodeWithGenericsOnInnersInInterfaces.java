public interface EqualsAndHashCodeWithGenericsOnInnersInInterfaces<A> {
  @lombok.EqualsAndHashCode class Inner<B> {
    int x;
    Inner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner)))
          return false;
      final EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner<?> other = (EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner<?>) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.x != other.x))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
      return (other instanceof EqualsAndHashCodeWithGenericsOnInnersInInterfaces.Inner);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.x);
      return result;
    }
  }
}
