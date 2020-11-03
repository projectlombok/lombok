@lombok.Builder class BuilderWithUserDefinedConstructor {
  public static @java.lang.SuppressWarnings("all") class BuilderWithUserDefinedConstructorBuilder {
    private @java.lang.SuppressWarnings("all") int plower;
    private @java.lang.SuppressWarnings("all") int pUpper;
    private @java.lang.SuppressWarnings("all") int _foo;
    private @java.lang.SuppressWarnings("all") int __bar;
    @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder pUpper(final int pUpper) {
      this.pUpper = pUpper;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder _foo(final int _foo) {
      this._foo = _foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder __bar(final int __bar) {
      this.__bar = __bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor build() {
      return new BuilderWithUserDefinedConstructor(this.plower, this.pUpper, this._foo, this.__bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder(plower=" + this.plower) + ", pUpper=") + this.pUpper) + ", _foo=") + this._foo) + ", __bar=") + this.__bar) + ")");
    }
  }
  private int plower;
  private int pUpper;
  private int _foo;
  private int __bar;
  BuilderWithUserDefinedConstructor(int plower, int pUpper) {
    super();
    this.plower = plower;
    this.pUpper = pUpper;
  }
  @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor(final int plower, final int pUpper, final int _foo, final int __bar) {
    super();
    this.plower = plower;
    this.pUpper = pUpper;
    this._foo = _foo;
    this.__bar = __bar;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder builder() {
    return new BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder();
  }
}
