public class WithNestedClass<Z> {
  static @lombok.RequiredArgsConstructor class Inner<Z> {
    final @lombok.With String x;
    /**
     * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated WithNestedClass.Inner<Z> withX(final String x) {
      return ((this.x == x) ? this : new WithNestedClass.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final String x) {
      super();
      this.x = x;
    }
  }
  public WithNestedClass() {
    super();
  }
}