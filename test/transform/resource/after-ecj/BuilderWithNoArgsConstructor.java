@lombok.NoArgsConstructor @lombok.Builder class BuilderWithNoArgsConstructor {
  public static @java.lang.SuppressWarnings("all") class BuilderWithNoArgsConstructorBuilder {
    private @java.lang.SuppressWarnings("all") int plower;
    private @java.lang.SuppressWarnings("all") Long pUpper;
    private @java.lang.SuppressWarnings("all") long _foo;
    private @java.lang.SuppressWarnings("all") String __bar;
    @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructorBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder plower(final int plower) {
      this.plower = plower;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder pUpper(final Long pUpper) {
      this.pUpper = pUpper;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder _foo(final long _foo) {
      this._foo = _foo;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder __bar(final String __bar) {
      this.__bar = __bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor build() {
      return new BuilderWithNoArgsConstructor(this.plower, this.pUpper, this._foo, this.__bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder(plower=" + this.plower) + ", pUpper=") + this.pUpper) + ", _foo=") + this._foo) + ", __bar=") + this.__bar) + ")");
    }
  }
  private int plower;
  private Long pUpper;
  private long _foo;
  private String __bar;
  @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor(final int plower, final Long pUpper, final long _foo, final String __bar) {
    super();
    this.plower = plower;
    this.pUpper = pUpper;
    this._foo = _foo;
    this.__bar = __bar;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder builder() {
    return new BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder();
  }
  public @java.lang.SuppressWarnings("all") BuilderWithNoArgsConstructor() {
    super();
  }
}