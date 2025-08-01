// version 14:
import lombok.experimental.WithBy;
public @WithBy record WithByOnRecord(String a, String b) {
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithByOnRecord withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecord(transformer.apply(this.a), this.b);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithByOnRecord withBBy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecord(this.a, transformer.apply(this.b));
  }
}
