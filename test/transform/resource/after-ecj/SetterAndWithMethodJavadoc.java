import lombok.With;
class SetterAndWithMethodJavadoc {
  @lombok.Setter @lombok.With int i;
  @lombok.Setter @lombok.With int j;
  SetterAndWithMethodJavadoc(int i, int j) {
    super();
    this.i = i;
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") SetterAndWithMethodJavadoc withI(final int i) {
    return ((this.i == i) ? this : new SetterAndWithMethodJavadoc(i, this.j));
  }
  public @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") SetterAndWithMethodJavadoc withJ(final int j) {
    return ((this.j == j) ? this : new SetterAndWithMethodJavadoc(this.i, j));
  }
}
