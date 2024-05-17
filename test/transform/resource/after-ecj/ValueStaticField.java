import lombok.Value;
final @Value class ValueStaticField {
  static int x;
  static String PASSWORD = "Ken sent me";
  <clinit>() {
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ValueStaticField)))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int result = 1;
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return "ValueStaticField()";
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated ValueStaticField() {
    super();
  }
}
