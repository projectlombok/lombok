@lombok.Builder class BuilderWithSuperObjectsDefinedConstructor {
  public static @java.lang.SuppressWarnings("all") class BuilderWithSuperObjectsDefinedConstructorBuilder {
    private @java.lang.SuppressWarnings("all") String __bar;
    private @java.lang.SuppressWarnings("all") Long pUpper;
    @java.lang.SuppressWarnings("all") BuilderWithSuperObjectsDefinedConstructorBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithSuperObjectsDefinedConstructor.BuilderWithSuperObjectsDefinedConstructorBuilder __bar(final String __bar) {
      this.__bar = __bar;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithSuperObjectsDefinedConstructor.BuilderWithSuperObjectsDefinedConstructorBuilder pUpper(final Long pUpper) {
      this.pUpper = pUpper;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithSuperObjectsDefinedConstructor build() {
      return new BuilderWithSuperObjectsDefinedConstructor(this.__bar, this.pUpper);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderWithSuperObjectsDefinedConstructor.BuilderWithSuperObjectsDefinedConstructorBuilder(__bar=" + this.__bar) + ", pUpper=") + this.pUpper) + ")");
    }
  }
  private String __bar;
  private Long pUpper;
  public BuilderWithSuperObjectsDefinedConstructor(CharSequence a, Number b) {
    super();
    __bar = (String) a;
    pUpper = (Long) b;
  }
  public static @java.lang.SuppressWarnings("all") BuilderWithSuperObjectsDefinedConstructor.BuilderWithSuperObjectsDefinedConstructorBuilder builder() {
    return new BuilderWithSuperObjectsDefinedConstructor.BuilderWithSuperObjectsDefinedConstructorBuilder();
  }
}