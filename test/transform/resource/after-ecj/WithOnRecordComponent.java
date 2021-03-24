// version 14:
import lombok.With;
public record WithOnRecordComponent(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public WithOnRecordComponent( String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnRecordComponent withA(final String a) {
    return ((this.a == a) ? this : new WithOnRecordComponent(a, this.b));
  }
}
