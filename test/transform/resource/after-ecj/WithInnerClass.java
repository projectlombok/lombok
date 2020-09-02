public class WithInnerClass<Z> {
  @lombok.RequiredArgsConstructor class Inner<Y> {
    final @lombok.With String x;
    public @java.lang.SuppressWarnings("all") WithInnerClass<Z>.Inner<Y> withX(final String x) {
      return ((this.x == x) ? this : new WithInnerClass.Inner(x));
    }
    public @java.lang.SuppressWarnings("all") Inner(final String x) {
      super();
      this.x = x;
    }
  }
  public WithInnerClass() {
    super();
  }
}