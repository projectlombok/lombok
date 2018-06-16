public class EqualsAndHashCodeWithGenericsOnInners<A> {
  @lombok.EqualsAndHashCode class Inner<B> {
    int x;
    Inner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof EqualsAndHashCodeWithGenericsOnInners.Inner)))
          return false;
      final EqualsAndHashCodeWithGenericsOnInners<?>.Inner<?> other = (EqualsAndHashCodeWithGenericsOnInners<?>.Inner<?>) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.x != other.x))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof EqualsAndHashCodeWithGenericsOnInners.Inner);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.x);
      return result;
    }
  }
  public EqualsAndHashCodeWithGenericsOnInners() {
    super();
  }
}
