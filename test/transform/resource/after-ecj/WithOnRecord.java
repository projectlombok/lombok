// version 14:
import lombok.With;
public @With record WithOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithOnRecord withA(final String a) {
    return ((this.a == a) ? this : new WithOnRecord(a, this.b));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated WithOnRecord withB(final String b) {
    return ((this.b == b) ? this : new WithOnRecord(this.a, b));
  }
}
