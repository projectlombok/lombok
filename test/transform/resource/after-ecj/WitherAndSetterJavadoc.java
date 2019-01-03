import lombok.experimental.Wither;
class WitherAndSetterJavadoc {
  @lombok.Setter @lombok.experimental.Wither int i;
  @lombok.Setter @lombok.experimental.Wither int j;
  WitherAndSetterJavadoc(int i, int j) {
    super();
    this.i = i;
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") WitherAndSetterJavadoc withI(final int i) {
    return ((this.i == i) ? this : new WitherAndSetterJavadoc(i, this.j));
  }
  public @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") WitherAndSetterJavadoc withJ(final int j) {
    return ((this.j == j) ? this : new WitherAndSetterJavadoc(this.i, j));
  }
}
