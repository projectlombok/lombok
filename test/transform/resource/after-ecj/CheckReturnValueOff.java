import lombok.With;
class CheckReturnValueOff {
  final @With int x;
  CheckReturnValueOff(int x) {
    super();
    this.x = x;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueOff withX(final int x) {
    return ((this.x == x) ? this : new CheckReturnValueOff(x));
  }
}
