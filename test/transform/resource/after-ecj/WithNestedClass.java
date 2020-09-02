public class WithNestedClass<Z> {
  static @lombok.RequiredArgsConstructor class Inner<Z> {
    final @lombok.With String x;
    public @java.lang.SuppressWarnings("all") WithNestedClass.Inner<Z> withX(final String x) {
      return ((this.x == x) ? this : new WithNestedClass.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") Inner(final String x) {
      super();
      this.x = x;
    }
  }
  public WithNestedClass() {
    super();
  }
}