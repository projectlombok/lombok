@lombok.Builder @lombok.experimental.Accessors(prefix = {"p", "_"}) class BuilderWithAccessors {
  public static @java.lang.SuppressWarnings("all") class BuilderWithAccessorsBuilder {
    private @java.lang.SuppressWarnings("all") int plower;
    private @java.lang.SuppressWarnings("all") int upper;
    private @java.lang.SuppressWarnings("all") int foo;
    private @java.lang.SuppressWarnings("all") int _bar;
    @java.lang.SuppressWarnings("all") BuilderWithAccessorsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithAccessors.BuilderWithAccessorsBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithAccessors.BuilderWithAccessorsBuilder upper(final int upper) {
      this.upper = upper;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithAccessors.BuilderWithAccessorsBuilder foo(final int foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithAccessors.BuilderWithAccessorsBuilder _bar(final int _bar) {
      this._bar = _bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithAccessors build() {
      return new BuilderWithAccessors(this.plower, this.upper, this.foo, this._bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderWithAccessors.BuilderWithAccessorsBuilder(plower=" + this.plower) + ", upper=") + this.upper) + ", foo=") + this.foo) + ", _bar=") + this._bar) + ")");
    }
  }
  private final int plower;
  private final int pUpper;
  private int _foo;
  private int __bar;
  @java.lang.SuppressWarnings("all") BuilderWithAccessors(final int plower, final int upper, final int foo, final int _bar) {
    super();
    this.plower = plower;
    this.pUpper = upper;
    this._foo = foo;
    this.__bar = _bar;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithAccessors.BuilderWithAccessorsBuilder builder() {
    return new BuilderWithAccessors.BuilderWithAccessorsBuilder();
  }
}
