// version 14:
import lombok.experimental.WithBy;
public record WithByOnRecordComponent(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public WithByOnRecordComponent( String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @java.lang.SuppressWarnings("all") WithByOnRecordComponent withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecordComponent(transformer.apply(this.a), this.b);
  }
}
