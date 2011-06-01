import lombok.Setter;
class SetterPlain {
  @lombok.Setter int i;
  @Setter int foo;
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final int foo) {
    this.foo = foo;
  }
  SetterPlain() {
    super();
  }
}