import lombok.With;
class WithPlain {
  @lombok.With int i;
  final @With int foo;
  WithPlain(int i, int foo) {
    super();
    this.i = i;
    this.foo = foo;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") WithPlain withI(final int i) {
    return ((this.i == i) ? this : new WithPlain(i, this.foo));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") WithPlain withFoo(final int foo) {
    return ((this.foo == foo) ? this : new WithPlain(this.i, foo));
  }
}
