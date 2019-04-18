import lombok.Setter;
class SetterOnMethod {
  @lombok.Setter() int i;
  @lombok.Setter() int j;
  public @Deprecated @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  SetterOnMethod() {
    super();
  }
}
@lombok.Setter() class SetterOnClassOnMethod {
  int i;
  int j;
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  SetterOnClassOnMethod() {
    super();
  }
}
@lombok.Setter() class SetterOnClassAndOnAField {
  int i;
  @lombok.Setter() int j;
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void setJ(final int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  SetterOnClassAndOnAField() {
    super();
  }
}