import lombok.experimental.Wither;
class SetterAndWitherJavadoc {
  @lombok.Setter @lombok.experimental.Wither int i;
  @lombok.Setter @lombok.experimental.Wither int j;
  SetterAndWitherJavadoc(int i, int j) {
    super();
    this.i = i;
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") SetterAndWitherJavadoc withI(final int i) {
    return ((this.i == i) ? this : new SetterAndWitherJavadoc(i, this.j));
  }
  public @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") SetterAndWitherJavadoc withJ(final int j) {
    return ((this.j == j) ? this : new SetterAndWitherJavadoc(this.i, j));
  }
}
