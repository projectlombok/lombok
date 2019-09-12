import lombok.experimental.*;
class WitherLegacyStar {
  @Wither int i;
  WitherLegacyStar(int i) {
    super();
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") WitherLegacyStar withI(final int i) {
    return ((this.i == i) ? this : new WitherLegacyStar(i));
  }
}