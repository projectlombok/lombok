import java.util.List;
@lombok.Builder class BuilderJavadoc<T> {
  public static class BuilderJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int basic;
    private @java.lang.SuppressWarnings("all") int getsetwith;
    private @java.lang.SuppressWarnings("all") int predef;
    private @java.lang.SuppressWarnings("all") int predefWithJavadoc;
    public BuilderJavadocBuilder<T> predef(final int x) {
      this.predef = (x * 10);
      return this;
    }
    public BuilderJavadocBuilder<T> predefWithJavadoc(final int x) {
      this.predefWithJavadoc = (x * 100);
      return this;
    }
    @java.lang.SuppressWarnings("all") BuilderJavadocBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> basic(final int basic) {
      this.basic = basic;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadocBuilder<T> getsetwith(final int getsetwith) {
      this.getsetwith = getsetwith;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> build() {
      return new BuilderJavadoc<T>(basic, getsetwith, predef, predefWithJavadoc);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderJavadoc.BuilderJavadocBuilder(basic=" + this.basic) + ", getsetwith=") + this.getsetwith) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ")");
    }
  }
  private final int basic;
  private @lombok.Getter @lombok.Setter @lombok.experimental.Wither int getsetwith;
  private final int predef;
  private final int predefWithJavadoc;
  @java.lang.SuppressWarnings("all") BuilderJavadoc(final int basic, final int getsetwith, final int predef, final int predefWithJavadoc) {
    super();
    this.basic = basic;
    this.getsetwith = getsetwith;
    this.predef = predef;
    this.predefWithJavadoc = predefWithJavadoc;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderJavadocBuilder<T> builder() {
    return new BuilderJavadocBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") int getGetsetwith() {
    return this.getsetwith;
  }
  public @java.lang.SuppressWarnings("all") void setGetsetwith(final int getsetwith) {
    this.getsetwith = getsetwith;
  }
  public @java.lang.SuppressWarnings("all") BuilderJavadoc<T> withGetsetwith(final int getsetwith) {
    return ((this.getsetwith == getsetwith) ? this : new BuilderJavadoc<T>(this.basic, getsetwith, this.predef, this.predefWithJavadoc));
  }
}
