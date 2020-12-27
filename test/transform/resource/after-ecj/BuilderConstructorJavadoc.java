import java.util.List;
class BuilderConstructorJavadoc<T> {
  public static class BuilderConstructorJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int basic;
    private @java.lang.SuppressWarnings("all") int multiline;
    private @java.lang.SuppressWarnings("all") int predef;
    private @java.lang.SuppressWarnings("all") int predefWithJavadoc;
    public BuilderConstructorJavadocBuilder<T> predef(final int x) {
      this.predef = x;
      return this;
    }
    public BuilderConstructorJavadocBuilder<T> predefWithJavadoc(final int x) {
      this.predefWithJavadoc = x;
      return this;
    }
    @java.lang.SuppressWarnings("all") BuilderConstructorJavadocBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> basic(final int basic) {
      this.basic = basic;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> multiline(final int multiline) {
      this.multiline = multiline;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc<T> build() {
      return new BuilderConstructorJavadoc<T>(this.basic, this.multiline, this.predef, this.predefWithJavadoc);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder(basic=" + this.basic) + ", multiline=") + this.multiline) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ")");
    }
  }
  @lombok.Builder BuilderConstructorJavadoc(int basic, int multiline, int predef, int predefWithJavadoc) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> builder() {
    return new BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T>();
  }
}