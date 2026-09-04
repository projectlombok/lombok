public class WithInnerClass<Z> {
  @lombok.RequiredArgsConstructor class Inner<Y> {
    final @lombok.With String x;
    /**
     * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated WithInnerClass<Z>.Inner<Y> withX(final String x) {
      return ((this.x == x) ? this : new WithInnerClass.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final String x) {
      super();
      this.x = x;
    }
  }
  public WithInnerClass() {
    super();
  }
}