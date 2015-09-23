import lombok.Value;
final @Value class ValueStaticField {
  private static int x;
  private static final String PASSWORD = "Ken sent me";
  <clinit>() {
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ValueStaticField)))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int hashCode() {
    int result = 1;
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return "ValueStaticField()";
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ValueStaticField() {
    super();
  }
}