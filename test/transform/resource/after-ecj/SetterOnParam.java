import lombok.Setter;
class SetterOnParam {
  @lombok.Setter() int i;
  @lombok.Setter() int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setI(final @SuppressWarnings("all") int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setJ(final @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int j) {
    this.j = j;
  }
  SetterOnParam() {
    super();
  }
}
@lombok.Setter() class SetterOnClassOnParam {
  int i;
  int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setJ(final int j) {
    this.j = j;
  }
  SetterOnClassOnParam() {
    super();
  }
}
@lombok.Setter() class SetterOnClassAndOnAFieldParam {
  int i;
  @lombok.Setter() int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setJ(final @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setI(final int i) {
    this.i = i;
  }
  SetterOnClassAndOnAFieldParam() {
    super();
  }
}