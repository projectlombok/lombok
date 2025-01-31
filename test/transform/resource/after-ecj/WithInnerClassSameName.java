
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
    public @java.lang.SuppressWarnings("all") WithInnerClassSameName<Z>.Inner<Y> withX(final String x) {
      return ((this.x == x) ? this : new WithInnerClassSameName.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") Inner(final String x) {
      super();
      this.x = x;
    }
  }
  WithInnerClassSameName() {
    super();
  }
}