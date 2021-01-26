@lombok.Builder class BuilderWithUserDefinedConstructorVarArgs {
  public static @java.lang.SuppressWarnings("all") class BuilderWithUserDefinedConstructorVarArgsBuilder {
    private @java.lang.SuppressWarnings("all") int plower;
    private @java.lang.SuppressWarnings("all") int pUpper;
    private @java.lang.SuppressWarnings("all") int _foo;
    private @java.lang.SuppressWarnings("all") int __bar;
    @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgsBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder pUpper(final int pUpper) {
      this.pUpper = pUpper;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder _foo(final int _foo) {
      this._foo = _foo;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder __bar(final int __bar) {
      this.__bar = __bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs build() {
      return new BuilderWithUserDefinedConstructorVarArgs(this.plower, this.pUpper, this._foo, this.__bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder(plower=" + this.plower) + ", pUpper=") + this.pUpper) + ", _foo=") + this._foo) + ", __bar=") + this.__bar) + ")");
    }
  }
  private int plower;
  private int pUpper;
  private int _foo;
  private int __bar;
  BuilderWithUserDefinedConstructorVarArgs(int... args) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder builder() {
    return new BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder();
  }
}