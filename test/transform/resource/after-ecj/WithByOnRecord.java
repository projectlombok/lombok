// version 14:
import lombok.experimental.WithBy;
public @WithBy record WithByOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public WithByOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @java.lang.SuppressWarnings("all") WithByOnRecord withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecord(transformer.apply(this.a), this.b);
  }
  public @java.lang.SuppressWarnings("all") WithByOnRecord withBBy(final java.util.function.Function<? super String, ? extends String> transformer) {
    return new WithByOnRecord(this.a, transformer.apply(this.b));
  }
}
