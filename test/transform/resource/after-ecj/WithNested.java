public interface WithNested<Z> {
  @lombok.RequiredArgsConstructor class IAmStaticReally {
    final @lombok.With String x;
    /**
     * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
     */
    public @java.lang.SuppressWarnings("all") WithNested.IAmStaticReally withX(final String x) {
      return ((this.x == x) ? this : new WithNested.IAmStaticReally(x));
    }
    public @java.lang.SuppressWarnings("all") IAmStaticReally(final String x) {
      super();
      this.x = x;
    }
  }
}
