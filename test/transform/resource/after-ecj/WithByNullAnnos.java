import java.util.List;
public @lombok.RequiredArgsConstructor class WithByNullAnnos {
  final @lombok.experimental.WithBy List<String> test;
  public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated WithByNullAnnos withTestBy(final java.util.function. @org.checkerframework.checker.nullness.qual.NonNull Function<? super List<String>, ? extends List<String>> transformer) {
    return new WithByNullAnnos(transformer.apply(this.test));
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithByNullAnnos(final List<String> test) {
    super();
    this.test = test;
  }
}
