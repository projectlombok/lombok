@lombok.Data class GetterSetterJavadoc1 {
  private int fieldName;
  public @java.lang.SuppressWarnings("all") int getFieldName() {
    return this.fieldName;
  }
  public @java.lang.SuppressWarnings("all") void setFieldName(final int fieldName) {
    this.fieldName = fieldName;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof GetterSetterJavadoc1)))
        return false;
    final @java.lang.SuppressWarnings("all") GetterSetterJavadoc1 other = (GetterSetterJavadoc1) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getFieldName() != other.getFieldName()))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof GetterSetterJavadoc1);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + this.getFieldName());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("GetterSetterJavadoc1(fieldName=" + this.getFieldName()) + ")");
  }
  public @java.lang.SuppressWarnings("all") GetterSetterJavadoc1() {
    super();
  }
}
class GetterSetterJavadoc2 {
  private @lombok.Getter @lombok.Setter int fieldName;
  GetterSetterJavadoc2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getFieldName() {
    return this.fieldName;
  }
  public @java.lang.SuppressWarnings("all") void setFieldName(final int fieldName) {
    this.fieldName = fieldName;
  }
}
class GetterSetterJavadoc3 {
  private @lombok.Getter @lombok.Setter int fieldName;
  GetterSetterJavadoc3() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getFieldName() {
    return this.fieldName;
  }
  public @java.lang.SuppressWarnings("all") void setFieldName(final int fieldName) {
    this.fieldName = fieldName;
  }
}