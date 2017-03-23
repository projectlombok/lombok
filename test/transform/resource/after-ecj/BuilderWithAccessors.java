@lombok.Builder @lombok.experimental.Accessors(prefix = {"p", "_"}) class BuilderWithAccessors {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderWithAccessorsBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int plower;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int upper;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int foo;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int _bar;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder upper(final int upper) {
      this.upper = upper;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder foo(final int foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder _bar(final int _bar) {
      this._bar = _bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessors build() {
      return new BuilderWithAccessors(plower, upper, foo, _bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((((("BuilderWithAccessors.BuilderWithAccessorsBuilder(plower=" + this.plower) + ", upper=") + this.upper) + ", foo=") + this.foo) + ", _bar=") + this._bar) + ")");
    }
  }
  private final int plower;
  private final int pUpper;
  private int _foo;
  private int __bar;
  @java.beans.ConstructorProperties({"plower", "upper", "foo", "_bar"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessors(final int plower, final int upper, final int foo, final int _bar) {
    super();
    this.plower = plower;
    this.pUpper = upper;
    this._foo = foo;
    this.__bar = _bar;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithAccessorsBuilder builder() {
    return new BuilderWithAccessorsBuilder();
  }
}
