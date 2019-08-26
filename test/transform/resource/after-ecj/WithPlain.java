import lombok.With;
class WithPlain {
  @lombok.With int i;
  final @With int foo;
  WithPlain(int i, int foo) {
    super();
    this.i = i;
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") WithPlain withI(final int i) {
    return ((this.i == i) ? this : new WithPlain(i, this.foo));
  }
  public @java.lang.SuppressWarnings("all") WithPlain withFoo(final int foo) {
    return ((this.foo == foo) ? this : new WithPlain(this.i, foo));
  }
}
