import lombok.Setter;
class SetterOnParam {
  @lombok.Setter() int i;
  @lombok.Setter() int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setI(final @SuppressWarnings("all") int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setJ(final @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int j) {
    this.j = j;
  }
  SetterOnParam() {
    super();
  }
}
@lombok.Setter() class SetterOnClassOnParam {
  int i;
  int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setJ(final int j) {
    this.j = j;
  }
  SetterOnClassOnParam() {
    super();
  }
}
@lombok.Setter() class SetterOnClassAndOnAFieldParam {
  int i;
  @lombok.Setter() int j;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setJ(final @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int j) {
    this.j = j;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setI(final int i) {
    this.i = i;
  }
  SetterOnClassAndOnAFieldParam() {
    super();
  }
}