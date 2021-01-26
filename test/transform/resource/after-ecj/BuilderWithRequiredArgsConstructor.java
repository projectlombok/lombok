@lombok.RequiredArgsConstructor @lombok.Builder class BuilderWithRequiredArgsConstructor {
  public static @java.lang.SuppressWarnings("all") class BuilderWithRequiredArgsConstructorBuilder {
    private @java.lang.SuppressWarnings("all") int plower;
    private @java.lang.SuppressWarnings("all") Long pUpper;
    private @java.lang.SuppressWarnings("all") long _foo;
    private @java.lang.SuppressWarnings("all") String __bar;
    @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructorBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder pUpper(final Long pUpper) {
      this.pUpper = pUpper;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder _foo(final long _foo) {
      this._foo = _foo;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder __bar(final String __bar) {
      this.__bar = __bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor build() {
      return new BuilderWithRequiredArgsConstructor(this.plower, this.pUpper, this._foo, this.__bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder(plower=" + this.plower) + ", pUpper=") + this.pUpper) + ", _foo=") + this._foo) + ", __bar=") + this.__bar) + ")");
    }
  }
  private int plower;
  private Long pUpper;
  private final long _foo;
  private final String __bar;
  @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor(final int plower, final Long pUpper, final long _foo, final String __bar) {
    super();
    this.plower = plower;
    this.pUpper = pUpper;
    this._foo = _foo;
    this.__bar = __bar;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder builder() {
    return new BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder();
  }
  public @java.lang.SuppressWarnings("all") BuilderWithRequiredArgsConstructor(final long _foo, final String __bar) {
    super();
    this._foo = _foo;
    this.__bar = __bar;
  }
}