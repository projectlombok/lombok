final @lombok.Value class ValueWithJavaBeansSpecCapitalization {
  private final int aField;
  public @java.lang.SuppressWarnings("all") int getaField() {
    return this.aField;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ValueWithJavaBeansSpecCapitalization)))
        return false;
    final ValueWithJavaBeansSpecCapitalization other = (ValueWithJavaBeansSpecCapitalization) o;
    if ((this.getaField() != other.getaField()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getaField());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("ValueWithJavaBeansSpecCapitalization(aField=" + this.getaField()) + ")");
  }
  public @java.lang.SuppressWarnings("all") ValueWithJavaBeansSpecCapitalization(final int aField) {
    super();
    this.aField = aField;
  }
}
