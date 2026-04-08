import lombok.With;
class CheckReturnValueWith {
  final @With int x;
  final @With String name;
  CheckReturnValueWith(int x, String name) {
    super();
    this.x = x;
    this.name = name;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @lombok.CheckReturnValue @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueWith withX(final int x) {
    return ((this.x == x) ? this : new CheckReturnValueWith(x, this.name));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @lombok.CheckReturnValue @java.lang.SuppressWarnings("all") @lombok.Generated CheckReturnValueWith withName(final String name) {
    return ((this.name == name) ? this : new CheckReturnValueWith(this.x, name));
  }
}
