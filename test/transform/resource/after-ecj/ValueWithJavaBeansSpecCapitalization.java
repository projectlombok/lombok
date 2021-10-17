final @lombok.Value @lombok.experimental.Accessors(javaBeansSpecCapitalization = true) class ValueWithJavaBeansSpecCapitalization {
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
final @lombok.Value class ValueWithoutJavaBeansSpecCapitalization {
  private final int aField;
  public @java.lang.SuppressWarnings("all") int getAField() {
    return this.aField;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ValueWithoutJavaBeansSpecCapitalization)))
        return false;
    final ValueWithoutJavaBeansSpecCapitalization other = (ValueWithoutJavaBeansSpecCapitalization) o;
    if ((this.getAField() != other.getAField()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getAField());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("ValueWithoutJavaBeansSpecCapitalization(aField=" + this.getAField()) + ")");
  }
  public @java.lang.SuppressWarnings("all") ValueWithoutJavaBeansSpecCapitalization(final int aField) {
    super();
    this.aField = aField;
  }
}
