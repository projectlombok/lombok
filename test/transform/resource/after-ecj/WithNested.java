public interface WithNested<Z> {
  @lombok.RequiredArgsConstructor class IAmStaticReally {
    final @lombok.With String x;
    public @java.lang.SuppressWarnings("all") WithNested.IAmStaticReally withX(final String x) {
      return ((this.x == x) ? this : new WithNested.IAmStaticReally(x));
    }
    public @java.lang.SuppressWarnings("all") IAmStaticReally(final String x) {
      super();
      this.x = x;
    }
  }
}
