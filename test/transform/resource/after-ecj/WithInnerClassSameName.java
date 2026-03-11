
interface A {
  class Inner<X> {
    Inner() {
      super();
    }
  }
}
class WithInnerClassSameName<Z> {
  @lombok.RequiredArgsConstructor class Inner<Y> implements A {
    final @lombok.With String x;
    /**
     * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated WithInnerClassSameName<Z>.Inner<Y> withX(final String x) {
      return ((this.x == x) ? this : new WithInnerClassSameName.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final String x) {
      super();
      this.x = x;
    }
  }
  WithInnerClassSameName() {
    super();
  }
}