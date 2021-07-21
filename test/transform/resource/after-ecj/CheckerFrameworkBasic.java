import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.With;
@Data @AllArgsConstructor @Accessors(chain = true) class CheckerFrameworkBasic {
  private final @With int x;
  private final int y;
  private int z;
  /**
   * @return {@code this}.
   */
  public @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") CheckerFrameworkBasic withX(final int x) {
    return ((this.x == x) ? this : new CheckerFrameworkBasic(x, this.y, this.z));
  }
  public @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  public @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
  public @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") int getZ() {
    return this.z;
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBasic setZ(final int z) {
    this.z = z;
    return this;
  }
  public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof CheckerFrameworkBasic)))
        return false;
    final CheckerFrameworkBasic other = (CheckerFrameworkBasic) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getX() != other.getX()))
        return false;
    if ((this.getY() != other.getY()))
        return false;
    if ((this.getZ() != other.getZ()))
        return false;
    return true;
  }
  protected @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof CheckerFrameworkBasic);
  }
  public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getX());
    result = ((result * PRIME) + this.getY());
    result = ((result * PRIME) + this.getZ());
    return result;
  }
  public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((((("CheckerFrameworkBasic(x=" + this.getX()) + ", y=") + this.getY()) + ", z=") + this.getZ()) + ")");
  }
  public @java.lang.SuppressWarnings("all") CheckerFrameworkBasic(final int x, final int y, final int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }
}