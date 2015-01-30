import lombok.experimental.Wither;
class WitherPlain {
  @lombok.experimental.Wither int i;
  final @Wither int foo;
  WitherPlain(int i, int foo) {
    super();
    this.i = i;
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") WitherPlain withI(final int i) {
    return ((this.i == i) ? this : new WitherPlain(i, this.foo));
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") WitherPlain withFoo(final int foo) {
    return ((this.foo == foo) ? this : new WitherPlain(this.i, foo));
  }
}
