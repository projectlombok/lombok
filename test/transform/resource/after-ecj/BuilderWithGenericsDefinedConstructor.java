import java.util.List;
@lombok.Builder class BuilderWithGenericsDefinedConstructor<T extends Number, V extends T> {
  public static @java.lang.SuppressWarnings("all") class BuilderWithGenericsDefinedConstructorBuilder<T extends Number, V extends T> {
    private @java.lang.SuppressWarnings("all") V a;
    @java.lang.SuppressWarnings("all") BuilderWithGenericsDefinedConstructorBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithGenericsDefinedConstructor.BuilderWithGenericsDefinedConstructorBuilder<T, V> a(final V a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithGenericsDefinedConstructor<T, V> build() {
      return new BuilderWithGenericsDefinedConstructor<T, V>(this.a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderWithGenericsDefinedConstructor.BuilderWithGenericsDefinedConstructorBuilder(a=" + this.a) + ")");
    }
  }
  private V a;
  public BuilderWithGenericsDefinedConstructor(T b) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") <T extends Number, V extends T>BuilderWithGenericsDefinedConstructor.BuilderWithGenericsDefinedConstructorBuilder<T, V> builder() {
    return new BuilderWithGenericsDefinedConstructor.BuilderWithGenericsDefinedConstructorBuilder<T, V>();
  }
}