import lombok.Data;
import org.jspecify.annotations.NonNull;
@Data class DataWithJspecifyTypeUseNonNull {
  private @NonNull String prop1;
  private java.lang.@NonNull String prop2;
  public @java.lang.SuppressWarnings("all") @lombok.Generated @NonNull String getProp1() {
    return this.prop1;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.@NonNull String getProp2() {
    return this.prop2;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setProp1(final @NonNull String prop1) {
    if ((prop1 == null))
        {
          throw new java.lang.NullPointerException("prop1 is marked non-null but is null");
        }
    this.prop1 = prop1;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setProp2(final java.lang.@NonNull String prop2) {
    if ((prop2 == null))
        {
          throw new java.lang.NullPointerException("prop2 is marked non-null but is null");
        }
    this.prop2 = prop2;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof DataWithJspecifyTypeUseNonNull)))
        return false;
    final DataWithJspecifyTypeUseNonNull other = (DataWithJspecifyTypeUseNonNull) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    final java.lang.Object this$prop1 = this.getProp1();
    final java.lang.Object other$prop1 = other.getProp1();
    if (((this$prop1 == null) ? (other$prop1 != null) : (! this$prop1.equals(other$prop1))))
        return false;
    final java.lang.Object this$prop2 = this.getProp2();
    final java.lang.Object other$prop2 = other.getProp2();
    if (((this$prop2 == null) ? (other$prop2 != null) : (! this$prop2.equals(other$prop2))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof DataWithJspecifyTypeUseNonNull);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $prop1 = this.getProp1();
    result = ((result * PRIME) + (($prop1 == null) ? 43 : $prop1.hashCode()));
    final java.lang.Object $prop2 = this.getProp2();
    result = ((result * PRIME) + (($prop2 == null) ? 43 : $prop2.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("DataWithJspecifyTypeUseNonNull(prop1=" + this.getProp1()) + ", prop2=") + this.getProp2()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated DataWithJspecifyTypeUseNonNull(final @NonNull String prop1, final java.lang.@NonNull String prop2) {
    super();
    if ((prop1 == null))
        {
          throw new java.lang.NullPointerException("prop1 is marked non-null but is null");
        }
    if ((prop2 == null))
        {
          throw new java.lang.NullPointerException("prop2 is marked non-null but is null");
        }
    this.prop1 = prop1;
    this.prop2 = prop2;
  }
}
