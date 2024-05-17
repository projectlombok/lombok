import lombok.experimental.*;
class WitherLegacyStar {
  @Wither int i;
  WitherLegacyStar(int i) {
    super();
    this.i = i;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated WitherLegacyStar withI(final int i) {
    return ((this.i == i) ? this : new WitherLegacyStar(i));
  }
}
