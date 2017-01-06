import lombok.Setter;
class SetterPlain {
  @lombok.Setter int i;
  @Setter int foo;
  SetterPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setFoo(final int foo) {
    this.foo = foo;
  }
}
