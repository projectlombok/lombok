// version 14:
import lombok.experimental.WithBy;
public record WithByOnRecordComponent(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithByOnRecordComponent withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecordComponent(transformer.apply(this.a), this.b);
  }
}
